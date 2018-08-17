package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddress;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddressExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameTxManage;
import org.brewchain.cwv.dbgens.game.entity.CWVGameTxManageExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuy;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncomeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletTopup;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletTopupExample;
import org.brewchain.cwv.game.chain.BidAuctionRetEnum;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.MarketTypeEnum;
import org.brewchain.cwv.game.enums.PropertyBidStatusEnum;
import org.brewchain.cwv.game.enums.PropertyExchangeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyIncomeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
import org.brewchain.cwv.game.enums.PropertyTypeEnum;
import org.brewchain.cwv.game.enums.TransHashTypeEnum;
import org.brewchain.cwv.game.enums.TransactionTypeEnum;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.cwv.game.helper.GameNoticeHelper;
import org.brewchain.cwv.game.helper.GameNoticeHelper.NoticeTradeTypeEnum;
import org.brewchain.cwv.game.helper.GameNoticeHelper.NoticeTypeEnum;
import org.brewchain.cwv.game.service.GamePropertyChargeService;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 定时任务查询合约交易状态并 更新业务状态
 * 
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class TransactionStatusTask implements Runnable {
	// 防止收益数据冲突
	public static HashSet<Integer> jobIncomeSet = new HashSet<>();

	public final static float RATE = 0.25f;
	public final static float ORDINARY_TO_DIVIDED = 0.4f;
	public final static float TYPICAL_TO_DIVIDED = 0.6f;

	public final static int DAY_PERIOD = 7; // 7日
	public final static String INCOME_TIME = "income_time"; // 7日

	private PropertyHelper propertyHelper;

	public TransactionStatusTask(PropertyHelper propertyHelper) {
		super();
		this.propertyHelper = propertyHelper;
	}

	@Override
	public void run() {

		log.info("TransactionStatusTask start ....");
		try {

			List<Object> list = getTxManageUndone();

			for (Object o : list) {
				CWVGameTxManage manage = (CWVGameTxManage) o;
				String status = getTransStatus(propertyHelper, manage.getTxHash(),
						TransactionTypeEnum.EXCHANGE_SELL.getValue());

				if (StringUtils.isEmpty(status)) {
					continue;
				}

				manageProcess(manage, status);

			}

			// 抽奖
			// List<Object> listDraw = getUndoneDraw();
			// for(Object o : listDraw) {
			// CWVMarketDraw draw = (CWVMarketDraw) o;
			// RespGetTxByHash.Builder respGetTxByHash =
			// propertyHelper.getWltHelper().getTxInfo(draw.getChainTransHash());
			// if(respGetTxByHash.getRetCode() == -1) {//失败
			// log.error("draw:chainTransHash:"+draw.getChainTransHash()+"==>查询异常");
			// continue;
			// }
			// String status = respGetTxByHash.getTransaction().getStatus();
			//
			// if( status == null || status.equals("") ) {
			// continue;
			// }
			//
			// drawProcess(draw,status);
			//
			//
			// }

			// 充值
			List<Object> listTopup = getUndoneTopup();

			for (Object o : listTopup) {
				CWVUserWalletTopup topup = (CWVUserWalletTopup) o;
				String status = getTransStatus(propertyHelper, topup.getTxHash(),
						TransactionTypeEnum.ACCOUNT_RECHARGE.getValue());

				if (StringUtils.isEmpty(status)) {
					continue;
				}

				if (status.equals(ChainTransStatusEnum.DONE.getValue()))
					topupDone(topup);
				else if (status.equals(ChainTransStatusEnum.ERROR.getValue()))
					topupError(topup);

			}
		} catch (Exception e) {
			log.error("TransactionStatusTask exception ==>\n" + e.getStackTrace());
		}
		log.info("TransactionStatusTask ended ....");
	}

	/**
	 * 交易成功处理
	 * 
	 * @param manage
	 */
	private void manageProcess(CWVGameTxManage manage, String status) {

		if (status.equals(ChainTransStatusEnum.DONE.getValue())) {
			manageUpdateStatus(manage, ChainTransStatusEnum.DONE.getKey());
		} else if (status.equals(ChainTransStatusEnum.ERROR.getValue())) {
			manageUpdateStatus(manage, ChainTransStatusEnum.ERROR.getKey());
		}

		// 卖出房产
		if (manage.getType().equals(TransactionTypeEnum.EXCHANGE_SELL.getKey())) {

			try {
				exchangeSellProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask exchangeSellProcess error"+e.getStackTrace());
			}

			// 卖出房产撤销
		} else if (manage.getType().equals(TransactionTypeEnum.EXCHANGE_SELL_CANCEL.getKey())) {
			try {
				exchangeSellCancelProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask exchangeSellCancelProcess error"+e.getStackTrace());
			}
			// 买家转账
		} else if (manage.getType().equals(TransactionTypeEnum.EXCHANGE_BUY_AMOUNT.getKey())) {
			try {
				exchangeBuyAmountProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask exchangeBuyAmountProcess error"+e.getStackTrace());
				
			}
			// 中介 金额转账买家 房产转账买家
		} else if (manage.getType().equals(TransactionTypeEnum.EXCHANGE_BUY_GROUP.getKey())) {
			try {
				exchangeBuyGroupProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask exchangeBuyGroupProcess error"+e.getStackTrace());
				
			}
			// 买家金额
		} else if (manage.getType().equals(TransactionTypeEnum.EXCHANGE_BUY_AMOUNT_ROLLBACK.getKey())) {
			try {
				exchangeBuyAmountRollbackProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask exchangeBuyAmountRollbackProcess error"+e.getStackTrace());
				
			}
			// 创建竞拍
		} else if (manage.getType().equals(TransactionTypeEnum.BID_CREATE.getKey())) {
			try {
				bidCreateProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask bidCreateProcess error"+e.getStackTrace());
			}
			// 撤销创建的竞拍
		} else if (manage.getType().equals(TransactionTypeEnum.BID_CREATE_CANCEL.getKey())) {
			try {
				bidCreateCancelProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask bidCreateCancelProcess error"+e.getStackTrace());
				
			}
			// 竞价
		} else if (manage.getType().equals(TransactionTypeEnum.BID_AUCTION.getKey())) {
			try {
				bidAuctionProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask bidAuctionProcess error"+e.getStackTrace());
				
			}
			// 抽奖随机数
		} else if (manage.getType().equals(TransactionTypeEnum.BID_AUCTION_END.getKey())) {
			try {
				bidAuctionEndProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask bidAuctionEndProcess error"+e.getStackTrace());
				
			}
		} else if (manage.getType().equals(TransactionTypeEnum.DRAW_RANDOM.getKey())) {
			try {
				drawRandomProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask drawRandomProcess error"+e.getStackTrace());
				
			}
			// 分组抽奖交易
		} else if (manage.getType().equals(TransactionTypeEnum.DRAW_GROUP.getKey())) {
			try {
				drawGroupProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask drawGroupProcess error"+e.getStackTrace());
				
			}
			// 发放用户收益
		} else if (manage.getType().equals(TransactionTypeEnum.INCOME_CREATE.getKey())) {
			try {
				incomeCreateProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask incomeCreateProcess error"+e.getStackTrace());
				
			}
			// 领取用户收益
		} else if (manage.getType().equals(TransactionTypeEnum.INCOME_CLAIM.getKey())) {
			try {
				incomeClaimProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask incomeClaimProcess error"+e.getStackTrace());
			}
		} else if (manage.getType().equals(TransactionTypeEnum.CONTRACT_CREATE.getKey())) {
			try {
				contractCreateProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask contractCreateProcess error"+e.getStackTrace());
				
			}
		} else if (manage.getType().equals(TransactionTypeEnum.ACCOUNT_RECHARGE.getKey())) {
			try {
				accountRechargeProcess(manage);
			} catch (Exception e) {
				log.error("TransactionStatusTask accountRechargeProcess error"+e.getStackTrace());
				
			}
		}

	}

	private void accountRechargeProcess(CWVGameTxManage manage) {

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			accountRechargeDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			accountRechargeError(manage);
	}

	private void accountRechargeError(CWVGameTxManage manage) {
		accountUpdateByTxhash(manage.getTxHash(), manage.getStatus());

	}

	private void accountUpdateByTxhash(String txHash, Integer status) {
		CWVUserWalletTopupExample example = new CWVUserWalletTopupExample();
		example.createCriteria().andTxHashEqualTo(txHash);

		CWVUserWalletTopup topup = new CWVUserWalletTopup();
		topup.setChainStatus(status.byteValue());

		propertyHelper.getDao().topupDao.updateByExampleSelective(topup, example);

	}

	private void accountRechargeDone(CWVGameTxManage manage) {
		// 更新交易状态
		CWVUserWalletTopupExample example = new CWVUserWalletTopupExample();
		example.createCriteria().andTxHashEqualTo(manage.getTxHash());

		// 更新账户信息
		List<Object> list = propertyHelper.getDao().topupDao.selectByExample(example);
		for (Object o : list) {
			// 更新交易
			CWVUserWalletTopup topup = (CWVUserWalletTopup) o;
			topup.setChainStatus(manage.getStatus().byteValue());
			// 更新账户
			CWVUserWallet wallet = propertyHelper.getWalletHelper().getUserAccount(topup.getUserId(), CoinEnum.CWB);
			wallet.setAccount(new BigDecimal(wallet.getAccount()).add(topup.getAmount()).toString());

			// 增加交易记录
			final CWVUserTransactionRecord recordClaim = new CWVUserTransactionRecord();
			recordClaim.setCreateTime(new Date());
			recordClaim.setUserId(topup.getUserId());
			recordClaim.setCreateUser(topup.getUserId());
			recordClaim.setDetail("充值成功");
			recordClaim.setGainCost(topup.getAmount());
			recordClaim.setMarketId(topup.getTopupId());
			recordClaim.setType(MarketTypeEnum.RECHARGE.getValue());
			propertyHelper.getDao().userTransactionRecordDao.insert(recordClaim);

		}

	}

	private void incomeCreateProcess(CWVGameTxManage manage) {
		// List<Object> list = getDrawGroupListByTxHash(manage.getTxHash());

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			incomeCreateDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			incomeCreateError(manage);
	}

	private void incomeCreateError(CWVGameTxManage manage) {

		// 更新收益发放状态
		userPropertyIncomeCreateUpdateByTxHash(manage);

	}

	private void incomeClaimProcess(CWVGameTxManage manage) {
		// List<Object> list = getDrawGroupListByTxHash(manage.getTxHash());

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			incomeClaimDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			incomeClaimError(manage);
	}

	/**
	 * 仅限抽奖合约
	 * 
	 * @param manage
	 */
	private void contractCreateProcess(CWVGameTxManage manage) {
		// List<Object> list = getDrawGroupListByTxHash(manage.getTxHash());
		contractUpdateByTxhash(manage.getTxHash(), manage.getChainStatus());

	}

	private void contractUpdateByTxhash(String txHash, Integer status) {
		CWVGameContractAddressExample example = new CWVGameContractAddressExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);
		CWVGameContractAddress address = new CWVGameContractAddress();
		address.setChainStatus(status.byteValue());
		if (status.byteValue() == ChainTransStatusEnum.DONE.getKey()) {
			address.setContractState("1");
		} else if (status.byteValue() == ChainTransStatusEnum.ERROR.getKey()) {
			address.setContractState("2");
		} else {
			return;
		}
		propertyHelper.getDao().contractDao.updateByExampleSelective(address, example);
	}

	private void incomeClaimError(CWVGameTxManage manage) {
		// TODO Auto-generated method stub
		userPropertyIncomeClaimUpdateByTxHash(manage);
	}

	private void userPropertyIncomeCreateUpdateByTxHash(CWVGameTxManage manage) {
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		example.createCriteria().andChainTransHashEqualTo(manage.getTxHash());
		CWVUserPropertyIncome income = new CWVUserPropertyIncome();
		income.setChainStatus(manage.getChainStatus().byteValue());

		propertyHelper.getDao().incomeDao.updateByExampleSelective(income, example);

	}

	private void userPropertyIncomeClaimUpdateByTxHash(CWVGameTxManage manage) {
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		example.createCriteria().andChainTransHashClaimEqualTo(manage.getTxHash());
		CWVUserPropertyIncome income = new CWVUserPropertyIncome();
		income.setChainStatusClaim(manage.getChainStatus().byteValue());

		propertyHelper.getDao().incomeDao.updateByExampleSelective(income, example);
	}

	private void incomeClaimDone(CWVGameTxManage manage) {
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		example.createCriteria().andChainTransHashClaimEqualTo(manage.getTxHash());
		CWVUserPropertyIncome income = new CWVUserPropertyIncome();
		income.setChainStatusClaim(manage.getChainStatus().byteValue());

		propertyHelper.getDao().incomeDao.updateByExampleSelective(income, example);

		CWVUserPropertyIncomeExample incomeExample = new CWVUserPropertyIncomeExample();

		Object o = propertyHelper.getDao().incomeDao.selectOneByExample(example);
		income = (CWVUserPropertyIncome) o;
		// 更新各个房产收益记录状态
		incomeExample.createCriteria().andUserIdEqualTo(income.getUserId()).andTypeEqualTo(income.getType())
				.andPropertyIdIsNotNull();

		CWVUserPropertyIncome record = new CWVUserPropertyIncome();
		record.setStatus(PropertyIncomeStatusEnum.CLAIMED.getValue());
		propertyHelper.getDao().incomeDao.updateByExampleSelective(record, incomeExample);

		// 更新房产信息
		List<Object> list = propertyHelper.getDao().incomeDao.selectByExample(incomeExample);
		for (Object ob : list) {
			CWVUserPropertyIncome incomeProperty = (CWVUserPropertyIncome) ob;
			CWVGameProperty property = new CWVGameProperty();
			property.setPropertyId(incomeProperty.getPropertyId());
			property = propertyHelper.getDao().gamePropertyDao.selectByPrimaryKey(property);

			property.setIncome(property.getIncome().add(incomeProperty.getAmount()));
			propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
		}

		// 更新账户历史收益
		CWVUserWallet wallet = propertyHelper.getWalletHelper().getUserAccount(income.getUserId(), CoinEnum.CWB);
		switch (income.getType().intValue()) {
		case 2:
			wallet.setIncomeOrdinary(wallet.getIncomeOrdinary().add(income.getAmount()));
			break;
		case 1:
			wallet.setIncomeTypical(wallet.getIncomeTypical().add(income.getAmount()));
			break;
		case 3:
			wallet.setIncomeFunctional(wallet.getIncomeFunctional().add(income.getAmount()));
			break;
		default:
			break;
		}

		propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(wallet);

		// 增加交易记录
		final CWVUserTransactionRecord recordClaim = new CWVUserTransactionRecord();
		recordClaim.setCreateTime(new Date());
		recordClaim.setUserId(income.getUserId());
		recordClaim.setCreateUser(income.getUserId());
		recordClaim.setDetail("房产收益");
		recordClaim.setGainCost(income.getAmount());
		recordClaim.setMarketId(income.getIncomeId());
		recordClaim.setType(MarketTypeEnum.INCOME.getValue());
		propertyHelper.getDao().userTransactionRecordDao.insert(recordClaim);

	}

	private void incomeCreateDone(CWVGameTxManage manage) {
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		example.createCriteria().andChainTransHashEqualTo(manage.getTxHash());

		CWVUserPropertyIncome income = new CWVUserPropertyIncome();
		income.setChainStatus(manage.getChainStatus().byteValue());
		income.setMaster(0);
		propertyHelper.getDao().incomeDao.updateByExampleSelective(income, example);

		List<Object> listSlave = propertyHelper.getDao().incomeDao.selectByExample(example);

		CWVUserPropertyIncomeExample exampleMaster = new CWVUserPropertyIncomeExample();
		exampleMaster.createCriteria().andMasterEqualTo(1).andStatusEqualTo(PropertyIncomeStatusEnum.NEW.getValue());

		List<Object> listMaster = propertyHelper.getDao().incomeDao.selectByExample(exampleMaster);
		if (listMaster.isEmpty()) {
			for (Object o : listSlave) {
				CWVUserPropertyIncome incomeO = (CWVUserPropertyIncome) o;
				incomeO.setIncomeId(null);
				incomeO.setMaster(1);
				propertyHelper.getDao().incomeDao.insert(incomeO);
			}

		} else {
			// List<Object> newMaster = new ArrayList<>();
			for (Object o : listSlave) {
				CWVUserPropertyIncome slave = (CWVUserPropertyIncome) o;
				boolean exist = false;
				for (Object m : listMaster) {
					CWVUserPropertyIncome master = (CWVUserPropertyIncome) m;
					if (master.getUserId().equals(slave.getUserId()) && master.getType().equals(slave.getType())) {
						master.setAmount(master.getAmount().add(slave.getAmount()));
						exist = true;
						break;
					}
				}
				if (!exist) {
					slave.setMaster(1);
					slave.setIncomeId(null);
					propertyHelper.getDao().incomeDao.insert(slave);
				}

			}

			propertyHelper.getDao().incomeDao.batchUpdate(listMaster);
		}

	}

	/**
	 * 抽奖房产
	 * 
	 * @param manage
	 */
	private void drawGroupProcess(CWVGameTxManage manage) {
		List<Object> list = getDrawGroupListByTxHash(manage.getTxHash());

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			for (Object o : list) {
				drawGroupDone((CWVMarketDraw) o);
			}
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			for (Object o : list) {
				drawGroupError((CWVMarketDraw) o);
			}
	}

	private void drawGroupError(final CWVMarketDraw draw) {
		draw.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {
				propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
				drawRollBack(draw.getUserId());
				return null;
			}
		});

		// 创建抽奖失败消息
		CWVGameProperty property = propertyHelper.getByPropertyId(draw.getPropertyId());
		String noticeSellContent = propertyHelper.getGameNoticeHelper().noticeTradeTpl(NoticeTradeTypeEnum.DRAW_ERROR,
				property.getPropertyName(), null);
		Calendar c = Calendar.getInstance();
		String startTime = DateUtil.getDayTime(c.getTime());
		propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.USER.getValue(), draw.getUserId() + "",
				startTime, DateUtil.getDayTime(c.getTime()), "5", "3", noticeSellContent);

	}

	private void drawRandomProcess(CWVGameTxManage manage) {
		List<Object> list = getDrawRandomListByTxHash(manage.getTxHash());

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			for (Object o : list) {
				drawRandomDone((CWVMarketDraw) o);
			}
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			for (Object o : list) {
				drawRandomError((CWVMarketDraw) o);
			}
	}

	private void drawRandomDone(CWVMarketDraw draw) {
		RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper()
				.getTxInfo(draw.getChainTransHashRandom());

		// 获取随机数
		String random = respGetTxByHash.getTransaction().getResult();

		String token = getTokenByRandom(random);
		draw.setPropertyToken(token);
		draw.setChainRandom(random);
		draw.setChainStatusRandom(ChainTransStatusEnum.DONE.getKey());

		propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);

	}

	private void drawRandomError(CWVMarketDraw draw) {
		draw.setChainStatusRandom(ChainTransStatusEnum.ERROR.getKey());

		propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);

		drawRollBack(draw.getUserId());
	}

	/**
	 * 回滚抽奖次数
	 * 
	 * @param userId
	 */
	private void drawRollBack(Integer userId) {
		CWVUserWallet userWallet = propertyHelper.getWalletHelper().getUserAccount(userId, CoinEnum.CWB);
		userWallet.setDrawCount(userWallet.getDrawCount() + 1);
		propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(userWallet);
	}

	private String getTokenByRandom(String random) {
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		String superUserId = propertyHelper.getCommonHelper().getSysSettingValue("super_user");

		example.createCriteria().andUserIdEqualTo(Integer.parseInt(superUserId)).andGameMapIdIsNotNull()
				.andPropertyTypeEqualTo("2").addCriterion(
						"property_id not in ( select property_id from cwv_market_draw where chain_status != '-1' or chain_status is null )");

		example.setOffset(Integer.parseInt(random) - 1);
		Object o = propertyHelper.getDao().gamePropertyDao.selectOneByExample(example);

		final CWVGameProperty gameProperty = (CWVGameProperty) o;

		return gameProperty.getCryptoToken();
	}

	private void bidAuctionProcess(CWVGameTxManage manage) {
		List<Object> list = getAuctionListByTxHash(manage.getTxHash());

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			for (Object o : list) {
				bidAuctionDone((CWVMarketAuction) o);
			}
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			for (Object o : list) {
				bidAuctionError((CWVMarketAuction) o);
			}

	}

	private List<Object> getAuctionListByTxHash(String txHash) {
		CWVMarketAuctionExample example = new CWVMarketAuctionExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);

		return propertyHelper.getDao().auctionDao.selectByExample(example);

	}

	private void bidAuctionEndProcess(CWVGameTxManage manage) {
		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey()) {
			RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(manage.getTxHash());
			String retCode = respGetTxByHash.getTransaction().getResult();

			if (Long.parseLong(retCode) != BidAuctionRetEnum.AUCTION_BID_SUCCESS.getRetCode()
					|| Long.parseLong(retCode) != BidAuctionRetEnum.AUCTION_END_INVOKED.getRetCode()) {
				// 更新竞拍
				bidUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());

			} else {
				// 继续发起竞拍结束
				List<Object> list = getBidListByTxHash(manage.getTxHash());
				for (Object o : list) {
					CWVMarketBid bid = (CWVMarketBid) o;
					PropertyBidTask.auctionEnd(bid, propertyHelper);
				}

			}

		} else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey()) {

			// 更新竞拍
			bidUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());
			// 异常处理
			List<Object> list = getBidListByTxHash(manage.getTxHash());
			for (Object o : list) {
				CWVMarketBid bid = (CWVMarketBid) o;
				propertyHelper.getCommonHelper().marketExceptionAdd(TransactionTypeEnum.BID_AUCTION_END,
						bid.getBidId(), String.format("竞拍[%s]结束失败 ", bid.getBidId()));

			}
		}
	}

	/**
	 * 撤销创建竞拍
	 * 
	 * @param manage
	 */
	private void bidCreateCancelProcess(CWVGameTxManage manage) {

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			bidCreateCancelDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			bidCreateCancelError(manage);

	}

	/**
	 * 创建竞拍处理
	 * 
	 * @param manage
	 */
	private void bidCreateProcess(CWVGameTxManage manage) {

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			bidCreateDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			bidCreateError(manage);
	}

	private void exchangeBuyAmountRollbackProcess(CWVGameTxManage manage) {
		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			exchangeBuyAmountRollbackDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			exchangeBuyAmountRollbackError(manage);
	}

	private void exchangeBuyAmountRollbackError(CWVGameTxManage manage) {
		// 更新申请记录退款状态
		exchangeBuyRollbackUpdateStatus(manage.getTxHash(), ChainTransStatusEnum.ERROR.getKey());

		// 加入人工处理
		List<Object> list = getExchangeRollbackListByTxhash(manage.getTxHash());
		for (Object o : list) {
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			propertyHelper.getCommonHelper().marketExceptionAdd(
					TransactionTypeEnum.EXCHANGE_BUY_AMOUNT_ROLLBACK, buy.getExchangeId(),
					String.format("执行回退买家 [%s] 金额 [%s]", buy.getBuyerAddress(), buy.getAmount()));
		}

	}

	private List<Object> getExchangeRollbackListByTxhash(String txHash) {
		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
		example.createCriteria().andChainTransHashRollbackEqualTo(txHash);
		return propertyHelper.getDao().exchangeBuyDao.selectByExample(example);
	}

	private void exchangeBuyAmountRollbackDone(CWVGameTxManage manage) {
		// 更新申请记录退款状态
		exchangeBuyRollbackUpdateStatus(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());

	}

	private void exchangeBuyRollbackUpdateStatus(String txHash, byte status) {
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		buyExample.createCriteria().andChainTransHashRollbackEqualTo(txHash);

		CWVMarketExchangeBuy buy = new CWVMarketExchangeBuy();
		buy.setChainStatusRollback(status);

		propertyHelper.getDao().exchangeBuyDao.updateByExampleSelective(buy, buyExample);

	}

	private void exchangeBuyGroupProcess(CWVGameTxManage manage) {
		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			exchangeBuyGroupDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			exchangeBuyGroupError(manage);
	}

	/**
	 * 
	 * @param manage
	 */
	private void exchangeBuyGroupError(CWVGameTxManage manage) {
		// 更新申请记录信息
		exchangeBuyGroupUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.ERROR.getKey());

		// 退回买家金额 单独任务处理
		// exchangeBuyAmountRollback(manage.getTxHash());
		// 更新交易信息
		exchangeUpdateToSellByTxHash(manage.getTxHash());

		// 更新房产信息
		propertyUpdateToSellByTxHash(manage.getTxHash());

	}

	/**
	 * 用于回滚买入失败 交易更新 出售中状态
	 * 
	 * @param txHash
	 */
	private void exchangeUpdateToSellByTxHash(String txHash) {

		CWVMarketExchangeExample example = new CWVMarketExchangeExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);
		CWVMarketExchange exchange = new CWVMarketExchange();
		exchange.setUserId(exchange.getCreateUser());
		exchange.setStatus(PropertyExchangeStatusEnum.ONSALE.getValue());
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		propertyHelper.getDao().exchangeDao.updateByExampleSelective(exchange, example);

		List<Object> list = propertyHelper.getDao().exchangeDao.selectByExample(example);

		for (Object o : list) {
			CWVMarketExchange exchange2 = (CWVMarketExchange) o;
			// 创建 买入失败消息
			String noticeSellContent = propertyHelper.getGameNoticeHelper()
					.noticeTradeTpl(NoticeTradeTypeEnum.BUY_ERROR, exchange2.getPropertyName(), null);
			Calendar c = Calendar.getInstance();
			String startTime = DateUtil.getDayTime(c.getTime());
			propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.USER.getValue(), exchange.getUserId() + "",
					startTime, DateUtil.getDayTime(c.getTime()), "5", "3", noticeSellContent);

		}

	}

	private void propertyUpdateToSellByTxHash(String txHash) {
		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		propertyExample.createCriteria().andChainTransHashEqualTo(txHash);
		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyStatus(PropertyStatusEnum.ONSALE.getValue());
		property.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		propertyHelper.getDao().gamePropertyDao.updateByExampleSelective(property, propertyExample);

	}

	private void exchangeBuyGroupDone(CWVGameTxManage manage) {
		// 更新申请记录信息
		exchangeBuyGroupUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());

		// 更新交易信息
		exchangeUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());

		// 更新房产信息 增加卖家交易记录
		propertyUpdateByBuyGroupHash(manage);

	}

	private void propertyUpdateByBuyGroupHash(CWVGameTxManage manage) {
		CWVMarketExchangeExample example = new CWVMarketExchangeExample();
		example.createCriteria().andChainTransHashEqualTo(manage.getTxHash());
		List<Object> list = propertyHelper.getDao().exchangeDao.selectByExample(example);
		for (Object o : list) {
			try {
				CWVMarketExchange exchange = (CWVMarketExchange) o;
				CWVGameProperty property = new CWVGameProperty();
				property.setPropertyId(exchange.getPropertyId());
				// 设置价格
				property.setLastPrice(exchange.getSellPrice());
				// 重置收益
				property.setIncome(new BigDecimal("0"));
				// 修改房产状态 未出售
				property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
				// 修改用户
				property.setUserId(exchange.getUserId());
				property.setChainStatus(ChainTransStatusEnum.DONE.getKey());

				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);

				// 买入房产记录
				final CWVUserTransactionRecord recordBuy = new CWVUserTransactionRecord();
				recordBuy.setCreateTime(new Date());
				recordBuy.setUserId(exchange.getUserId());
				recordBuy.setCreateUser(exchange.getUserId());
				recordBuy.setDetail("买入房产");
				recordBuy.setGainCost(exchange.getSellPrice().negate());
				recordBuy.setMarketId(exchange.getExchangeId());
				recordBuy.setType(MarketTypeEnum.EXCHANGE.getValue());
				propertyHelper.getDao().userTransactionRecordDao.insert(recordBuy);

				// 卖出房产记录
				CWVUserTransactionRecord transactionRecord = new CWVUserTransactionRecord();
				transactionRecord.setCreateUser(exchange.getCreateUser());
				transactionRecord.setUserId(exchange.getCreateUser());
				transactionRecord.setCreateTime(new Date());
				transactionRecord.setDetail("卖出房产");
				transactionRecord.setMarketId(exchange.getExchangeId());
				transactionRecord.setType(MarketTypeEnum.EXCHANGE.getValue());
				transactionRecord.setStatus((byte) 0);
				transactionRecord.setGainCost(exchange.getSellPrice());
				propertyHelper.getDao().userTransactionRecordDao.insert(transactionRecord);
				// 扣税
				CWVUserTransactionRecord taxRecord = new CWVUserTransactionRecord();
				taxRecord.setCreateUser(exchange.getCreateUser());
				taxRecord.setUserId(exchange.getCreateUser());
				taxRecord.setCreateTime(new Date());
				taxRecord.setDetail("卖出扣税");
				taxRecord.setMarketId(exchange.getExchangeId());
				taxRecord.setType(MarketTypeEnum.EXCHANGE.getValue());
				taxRecord.setStatus((byte) 0);
				taxRecord.setGainCost(exchange.getTax());
				propertyHelper.getDao().userTransactionRecordDao.insert(taxRecord);

				// 创建 卖出成功消息
				String noticeSellContent = propertyHelper.getGameNoticeHelper()
						.noticeTradeTpl(NoticeTradeTypeEnum.SELL_DONE, property.getPropertyName(), null);
				Calendar c = Calendar.getInstance();
				String startTime = DateUtil.getDayTime(c.getTime());
				propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.USER.getValue(),
						exchange.getCreateUser() + "", startTime, DateUtil.getDayTime(c.getTime()), "5", "3",
						noticeSellContent);
				// 创建 买入成功消息
				String noticeBuycontent = propertyHelper.getGameNoticeHelper()
						.noticeTradeTpl(NoticeTradeTypeEnum.BUY_DONE, property.getPropertyName(), null);
				propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.USER.getValue(),
						exchange.getUserId() + "", startTime, DateUtil.getDayTime(c.getTime()), "5", "3",
						noticeBuycontent);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private void exchangeBuyAmountProcess(CWVGameTxManage manage) {
		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			exchangeBuyAmountDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			exchangeBuyAmountError(manage);
	}

	private void exchangeBuyAmountError(CWVGameTxManage manage) {

		// 更新买入信息
		exchangeBuyAmountUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.ERROR.getKey());

		// 更新交易信息
		exchangeUpdateToSellByTxHash(manage.getTxHash());
		// 更新房产信息 卖出状态
		propertyUpdateToSellByTxHash(manage.getTxHash());
	}

	private void exchangeBuyAmountDone(CWVGameTxManage manage) {
		// 更新 交易买入金额状态
		exchangeBuyAmountUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());
		;

	}

	private void exchangeSellCancelProcess(CWVGameTxManage manage) {
		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			exchangeSellCancelDone(manage);
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			exchangeSellCancelError(manage);
	}

	private List<Object> getDrawRandomListByTxHash(String txHash) {
		CWVMarketDrawExample example = new CWVMarketDrawExample();
		example.createCriteria().andChainTransHashRandomEqualTo(txHash);

		return propertyHelper.getDao().drawDao.selectByExample(example);

	}

	private List<Object> getDrawGroupListByTxHash(String txHash) {
		CWVMarketDrawExample example = new CWVMarketDrawExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);

		return propertyHelper.getDao().drawDao.selectByExample(example);

	}

	private List<Object> getExchangeBuyAmountListByTxHash(String txHash) {
		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);

		return propertyHelper.getDao().exchangeDao.selectByExample(example);

	}

	private List<Object> getExchangeBuyGroupListByTxHash(String txHash) {
		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
		example.createCriteria().andChainTransHashGroupEqualTo(txHash);

		return propertyHelper.getDao().exchangeDao.selectByExample(example);

	}

	private List<Object> getExchangeListByTxHash(String txHash) {
		CWVMarketExchangeExample example = new CWVMarketExchangeExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);

		return propertyHelper.getDao().exchangeDao.selectByExample(example);

	}

	private List<Object> getBidListByTxHash(String txHash) {
		CWVMarketBidExample example = new CWVMarketBidExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);

		return propertyHelper.getDao().bidDao.selectByExample(example);

	}

	/**
	 * 卖出房产逻辑处理
	 * 
	 * @param manage
	 */
	private void exchangeSellProcess(CWVGameTxManage manage) {

		List<Object> list = getExchangeListByTxHash(manage.getTxHash());

		if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.DONE.getKey())
			for (Object o : list) {
				try {
					exchangeSellDone((CWVMarketExchange) o);
				} catch (Exception e) {
					propertyHelper.getCommonHelper().marketExceptionAdd(TransactionTypeEnum.EXCHANGE_SELL, ((CWVMarketExchange) o).getExchangeId(), String.format("exchangeSellDone 执行失败"));
					log.error("TransactionStatusTask exchangeSellDone "+((CWVMarketExchange) o).getExchangeId()+"error"+e.getStackTrace());
				}

			}
		else if (manage.getChainStatus().byteValue() == ChainTransStatusEnum.ERROR.getKey())
			exchangeSellError(manage);
	}

	private void exchangeSellError(CWVGameTxManage manage) {
		// 更新交易
		exchangeUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.ERROR.getKey());
		// 更新房产
		propertyUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());
	}

	private void bidUpdateByTxHash(String txHash, byte status) {
		CWVMarketBidExample example = new CWVMarketBidExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);
		CWVMarketBid bid = new CWVMarketBid();
		bid.setChainStatus(status);
		propertyHelper.getDao().bidDao.updateByExampleSelective(bid, example);

	}

	private void exchangeBuyAmountUpdateByTxHash(String txHash, byte status) {
		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);
		CWVMarketExchangeBuy buy = new CWVMarketExchangeBuy();
		buy.setChainStatus(status);
		;
		propertyHelper.getDao().exchangeBuyDao.updateByExampleSelective(buy, example);

	}

	private void exchangeBuyGroupUpdateByTxHash(String txHash, byte status) {
		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
		example.createCriteria().andChainTransHashGroupEqualTo(txHash);
		CWVMarketExchangeBuy buy = new CWVMarketExchangeBuy();
		buy.setChainStatusGroup(status);
		;
		propertyHelper.getDao().exchangeBuyDao.updateByExampleSelective(buy, example);

	}

	private void exchangeUpdateByTxHash(String txHash, byte status) {
		CWVMarketExchangeExample example = new CWVMarketExchangeExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);
		CWVMarketExchange exchange = new CWVMarketExchange();
		exchange.setChainStatus(status);
		propertyHelper.getDao().exchangeDao.updateByExampleSelective(exchange, example);

	}

	private void propertyUpdateByTxHash(String txHash, byte status) {
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andChainTransHashEqualTo(txHash);
		CWVGameProperty property = new CWVGameProperty();
		property.setChainStatus(status);
		propertyHelper.getDao().gamePropertyDao.updateByExampleSelective(property, example);

	}

	/**
	 * 更新交易管理信息
	 * 
	 * @param manage
	 */
	private void manageUpdateStatus(CWVGameTxManage manage, byte status) {
		manage.setChainStatus((int) status);
		propertyHelper.getDao().txManangeDao.updateByPrimaryKeySelective(manage);
	}

	/**
	 * 获取进行中的交易
	 * 
	 * @param key
	 * @return
	 */
	private List<Object> getTxManageUndone() {
		String setting = propertyHelper.getCommonHelper().getSysSettingValue("job_trans_set");
		CWVGameTxManageExample example = new CWVGameTxManageExample();
		example.createCriteria().andChainStatusEqualTo((int) ChainTransStatusEnum.START.getKey())
				.andTypeIn(Arrays.asList(setting.split(","))).andTxHashIsNotNull().andTxHashNotEqualTo("");
		return propertyHelper.getDao().txManangeDao.selectByExample(example);
	}

	private void topupError(CWVUserWalletTopup topup) {
		// 人工处理转账失败
		topup.setStatus((byte) 2);

	}

	private void topupDone(final CWVUserWalletTopup topup) {

		final CWVUserWallet wallet = propertyHelper.getWalletHelper().getUserAccount(topup.getUserId(), CoinEnum.CWB);

		int countHistory = wallet.getTopupBalance().intValue() / 1000;

		wallet.setTopupBalance(wallet.getTopupBalance().add(topup.getAmount()));
		int countNew = wallet.getTopupBalance().intValue() / 1000;

		wallet.setDrawCount(wallet.getDrawCount() + (countNew - countHistory));
		wallet.setBalance(wallet.getBalance().add(topup.getAmount()));

		topup.setStatus((byte) 1);
		propertyHelper.getDao().walletDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {
				propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(wallet);
				propertyHelper.getDao().topupDao.updateByPrimaryKeySelective(topup);
				return null;
			}
		});

	}

	private List<Object> getUndoneTopup() {
		CWVUserWalletTopupExample example = new CWVUserWalletTopupExample();
		example.createCriteria().andStatusEqualTo((byte) 0);
		// .addCriterion("chain_status ='0'");
		return propertyHelper.getDao().topupDao.selectByExample(example);
	}

	/**
	 * 抽奖房产交易成功
	 * 
	 * @param draw
	 */
	private void drawGroupDone(final CWVMarketDraw draw) {

		draw.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(draw.getPropertyId());
		property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
		property.setUserId(draw.getUserId());
		property.setLastPrice(new BigDecimal("1000"));
		property.setLastPriceTime(new Date());

		propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);

				return null;
			}
		});

	}

	private void bidAuctionError(final CWVMarketAuction auction) {

		auction.setChainStatus(ChainTransStatusEnum.ERROR.getKey());

		CWVMarketBid bidOld = new CWVMarketBid();
		bidOld.setBidId(auction.getBidId());
		final CWVMarketBid bid = propertyHelper.getDao().bidDao.selectByPrimaryKey(bidOld);

		final CWVUserTransactionRecord recordAuction = new CWVUserTransactionRecord();
		// 设置交易记录
		recordAuction.setCreateTime(new Date());
		recordAuction.setCreateUser(auction.getUserId());
		recordAuction.setDetail("竞价退回");
		recordAuction.setUserId(auction.getUserId());

		// 账户金额
		BigDecimal gainCost = auction.getBidPrice().subtract(auction.getBidPrice());

		recordAuction.setGainCost(gainCost);

		propertyHelper.getDao().bidDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				// 竞价信息

				propertyHelper.getDao().auctionDao.updateByPrimaryKeySelective(auction);

				// 3更新竞拍信息 最高价 拥有者 更新时间
				CWVMarketAuctionExample example = new CWVMarketAuctionExample();
				example.createCriteria().andBidIdEqualTo(bid.getBidId()).andStatusEqualTo((byte) 1)
						.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey());
				example.setOrderByClause(" bid_price desc ");
				CWVMarketAuction old = (CWVMarketAuction) propertyHelper.getDao().auctionDao
						.selectOneByExample(example);

				bid.setBidAmount(old.getBidPrice());
				bid.setOwner(old.getUserId());
				bid.setLastUpdateTime(new Date());
				// 更新竞拍信息
				propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
				// 更新竞价用户账户金额

				// 插入竞价用户钱包操作记录
				propertyHelper.getDao().userTransactionRecordDao.insert(recordAuction);
				return null;
			}
		});

	}

	private void bidAuctionDone(final CWVMarketAuction auction) {

		RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(auction.getChainTransHash());

		String retCode = respGetTxByHash.getTransaction().getResult();
		auction.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		// 用于生成记录
		CWVMarketBid marketBid = new CWVMarketBid();
		marketBid.setBidId(auction.getBidId());
		final CWVMarketBid bid = propertyHelper.getDao().bidDao.selectByPrimaryKey(marketBid);
		// 用于生成消息通知
		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(bid.getGamePropertyId());
		property = propertyHelper.getDao().gamePropertyDao.selectByPrimaryKey(property);

		if (Long.parseLong(retCode) != BidAuctionRetEnum.AUCTION_BID_SUCCESS.getRetCode()) {

			auction.setStatus(Byte.parseByte(retCode));
			propertyHelper.getDao().auctionDao.updateByPrimaryKeySelective(auction);
			// 竞价失败消息
			String content = propertyHelper.getGameNoticeHelper().noticeTradeTpl(NoticeTradeTypeEnum.AUCTION_ERROR,
					property.getPropertyName(), null);
			Calendar c = Calendar.getInstance();
			String startTime = DateUtil.getDayTime(c.getTime());
			propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.USER.getValue(), auction.getUserId() + "",
					startTime, DateUtil.getDayTime(c.getTime()), "5", "3",
					content + ":" + BidAuctionRetEnum.getValue(Long.parseLong(retCode)));

			// 竞拍已结束
			if (Long.parseLong(retCode) == BidAuctionRetEnum.AUCTION_BID_IS_END.getRetCode()) {
				// 更新竞拍信息
				PropertyBidTask.noticeSetProcess(bid, propertyHelper);

				// 调取竞拍结束方法
				PropertyBidTask.auctionEnd(bid, propertyHelper);
			}

			return;
		}

		// 发起竞拍账户
		// final CWVUserWallet bidAccount =
		// propertyHelper.getWalletHelper().getUserAccount(Integer.parseInt(bid.getCreateUser()),
		// CoinEnum.CWB);
		// bidAccount.setBalance(bidAccount.getBalance().add(auction.getBidPrice()).subtract(auction.getLastBidPrice()));

		// 竞价失败消息
		String content = propertyHelper.getGameNoticeHelper().noticeTradeTpl(NoticeTradeTypeEnum.AUCTION_DONE,
				property.getPropertyName(), null);
		Calendar c = Calendar.getInstance();
		String startTime = DateUtil.getDayTime(c.getTime());
		propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.USER.getValue(), auction.getUserId() + "",
				startTime, DateUtil.getDayTime(c.getTime()), "5", "3", content);

		// 设置交易记录
		CWVUserTransactionRecord recordAuction = new CWVUserTransactionRecord();
		recordAuction.setCreateTime(new Date());
		recordAuction.setCreateUser(auction.getUserId());
		recordAuction.setDetail("竞价房产");
		recordAuction.setUserId(auction.getUserId());
		recordAuction.setGainCost(auction.getBidPrice().subtract(auction.getLastBidPrice()).negate());
		recordAuction.setMarketId(auction.getBidId());
		recordAuction.setType(MarketTypeEnum.BID.getValue());
		propertyHelper.getDao().userTransactionRecordDao.insert(recordAuction);

		final CWVUserTransactionRecord recordBid = new CWVUserTransactionRecord();
		// 设置交易记录
		recordBid.setCreateTime(new Date());
		recordBid.setCreateUser(Integer.parseInt(bid.getCreateUser()));
		recordBid.setDetail("收到竞价");
		recordBid.setUserId(Integer.parseInt(bid.getCreateUser()));
		recordBid.setGainCost(auction.getBidPrice().subtract(auction.getLastBidPrice()));
		//
		recordBid.setMarketId(bid.getBidId());
		recordBid.setType(MarketTypeEnum.BID.getValue());

		propertyHelper.getDao().bidDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {
				// 竞价信息更新
				propertyHelper.getDao().auctionDao.updateByPrimaryKeySelective(auction);
				// 房产竞拍信息更新
				propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
				// 发起者账户余额更新
				// propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(bidAccount);
				// 发起者账户交易记录
				propertyHelper.getDao().userTransactionRecordDao.insert(recordBid);
				return null;
			}
		});
	}

	private void bidCreateCancelError(CWVGameTxManage manage) {
		// 更新竞拍信息 预竞拍（定时任务处理状态）
		CWVMarketBidExample bidExample = new CWVMarketBidExample();
		bidExample.createCriteria().andChainTransHashEqualTo(manage.getTxHash());
		CWVMarketBid bid = new CWVMarketBid();
		bid.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		bid.setStatus(PropertyBidStatusEnum.PREVIEW.getValue());
		propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);

		// 更新房产 竞拍中
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andChainTransHashEqualTo(manage.getTxHash());
		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyStatus(PropertyStatusEnum.BIDDING.getValue());
		property.setChainStatus(ChainTransStatusEnum.DONE.getKey());

		propertyHelper.getDao().gamePropertyDao.updateByExampleSelective(property, example);
	}

	private void bidCreateCancelDone(CWVGameTxManage manage) {
		// 更新竞拍信息
		bidUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());
		// 更新房产信息
		propertyUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());

	}

	private void bidCreateError(CWVGameTxManage manage) {
		// 更新竞拍信息
		bidUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.ERROR.getKey());

		// 更新房产信息 未出售状态
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andChainTransHashEqualTo(manage.getTxHash());
		CWVGameProperty property = new CWVGameProperty();
		property.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
		propertyHelper.getDao().gamePropertyDao.updateByExampleSelective(property, example);

	}

	private void bidCreateDone(CWVGameTxManage manage) {
		// 更新竞拍信息
		bidUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());
		// 更新房产
		propertyUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());

	}

	private void exchangeSellCancelDone(CWVGameTxManage manage) {

		// 更新交易信息
		exchangeUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());

		// 更新房产信息 设置 房产未出售
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andChainTransHashEqualTo(manage.getTxHash());
		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
		property.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		propertyHelper.getDao().gamePropertyDao.updateByExampleSelective(property, example);

	}

	private void exchangeSoldDone(final CWVMarketExchange exchange) {
		// 更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		// 房产信息

		// 房产信息更新
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setLastPrice(exchange.getSellPrice());
		property.setLastPriceTime(new Date());
		property.setUserId(exchange.getUserId());
		property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());

		final CWVUserWallet userWalletSeller = propertyHelper.getWalletHelper().getUserAccount(exchange.getCreateUser(),
				CoinEnum.CWB);
		userWalletSeller
				.setBalance(userWalletSeller.getBalance().add(exchange.getSellPrice()).subtract(exchange.getTax()));

		final CWVUserTransactionRecord recordSell = new CWVUserTransactionRecord();
		recordSell.setCreateTime(new Date());
		recordSell.setCreateUser(exchange.getUserId());
		recordSell.setDetail("卖出房产");
		recordSell.setGainCost(exchange.getSellPrice().subtract(exchange.getTax()));
		recordSell.setUserId(exchange.getCreateUser());

		propertyHelper.getDao().exchangeDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				// 更新交易
				propertyHelper.getDao().exchangeDao.updateByPrimaryKeySelective(exchange);

				// 更新房产
				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);

				// 用户交易

				// 账户余额
				userWalletSeller.setUpdateTime(new Date());
				propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(userWalletSeller);

				// 账户交易记录
				propertyHelper.getDao().userTransactionRecordDao.insert(recordSell);

				return null;
			}
		});

	}

	/**
	 * 
	 * @param exchange
	 */
	private void exchangeSoldError(final CWVMarketExchange exchange) {
		// 更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		// 回滚交易信息
		exchange.setStatus(PropertyExchangeStatusEnum.ONSALE.getValue());
		exchange.setUserId(null);

		// 买家账户信息
		final CWVUserWallet userWalletBuyer = propertyHelper.getWalletHelper().getUserAccount(exchange.getUserId(),
				CoinEnum.CWB);

		// 设置账户信息
		userWalletBuyer.setBalance(userWalletBuyer.getBalance().add(exchange.getSellPrice()));

		// 设置交易记录
		final CWVUserTransactionRecord recordBuy = new CWVUserTransactionRecord();
		recordBuy.setCreateTime(new Date());
		recordBuy.setCreateUser(exchange.getUserId());
		recordBuy.setDetail("买入退回");
		recordBuy.setGainCost(exchange.getSellPrice());
		recordBuy.setUserId(exchange.getUserId());

		propertyHelper.getDao().exchangeDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				// 更新交易
				propertyHelper.getDao().exchangeDao.updateByPrimaryKeySelective(exchange);
				// 用户交易
				// 账户余额
				userWalletBuyer.setUpdateTime(new Date());

				propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(userWalletBuyer);
				// 账户交易记录
				propertyHelper.getDao().userTransactionRecordDao.insert(recordBuy);

				return null;
			}
		});

	}

	private void exchangeSellDone(final CWVMarketExchange exchange) {
		// 更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());

		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setPropertyStatus(PropertyStatusEnum.ONSALE.getValue());// 出售中
		property.setChainStatus(ChainTransStatusEnum.DONE.getKey());

		propertyHelper.getDao().exchangeDao.doInTransaction(new TransactionExecutor() {
			@Override
			public Object doInTransaction() {
				// 更新交易
				propertyHelper.getDao().exchangeDao.updateByPrimaryKeySelective(exchange);
				// 更新房产
				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
				return null;
			}
		});

	}

	/**
	 * 购买
	 * 
	 * @param buy
	 */
	private void exchangeBuyDone(CWVMarketExchangeBuy buy) {

		buy.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		propertyHelper.getDao().exchangeBuyDao.updateByPrimaryKeySelective(buy);

	}

	private void exchangeSellCancelError(CWVGameTxManage manage) {
		CWVMarketExchangeExample example = new CWVMarketExchangeExample();
		example.createCriteria().andChainTransHashEqualTo(manage.getTxHash());
		CWVMarketExchange exchange = new CWVMarketExchange();
		// 更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		exchange.setStatus(PropertyExchangeStatusEnum.ONSALE.getValue());
		propertyHelper.getDao().exchangeDao.updateByExampleSelective(exchange, example);
		// 更新房产信息
		propertyUpdateByTxHash(manage.getTxHash(), ChainTransStatusEnum.DONE.getKey());
	}


	/**
	 * 查询交易hash状态
	 * 
	 * @param hash
	 * @param hashType
	 *            参照TransHashEnum
	 * @param busiMap
	 *            业务日志记录MAP参数
	 * @return
	 */
	public static String getTransStatus(PropertyHelper propertyHelper, String hash, String hashType) {
		String status = null;
		RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(hash);
		if (respGetTxByHash.getRetCode() == -1) {// 失败
			Integer count = PropertyJobHandle.queryErrorTXMap.get(hash);
			if (count != null) {
				if (count >= 4) {// 超过五次加入人工处理表
					// TODO: 加入人工处理表
					CWVGameTxManageExample example = new CWVGameTxManageExample();
					CWVGameTxManage txManage = new CWVGameTxManage();
					txManage.setChainStatus((int) ChainTransStatusEnum.EXCEPTION.getKey());
					;
					propertyHelper.getDao().txManangeDao.updateByExample(txManage, example);
					PropertyJobHandle.queryErrorTXMap.remove(hash);
					// 返回查询失败终止查询
					return ChainTransStatusEnum.EXCEPTION.getValue();
				} else
					PropertyJobHandle.queryErrorTXMap.put(hash, count + 1);

			} else {
				PropertyJobHandle.queryErrorTXMap.put(hash, 1);
			}

			StringBuffer sb = new StringBuffer(hashType).append("==>transaction hash check error").append(":::");

			log.warn(sb.toString());
		} else
			status = respGetTxByHash.getTransaction().getStatus();
		return status.equals("-2") ? "" : status;
	}

	/**
	 * 抽奖交易状态批量处理
	 */
	private void drawTransStatusGroup() {
		CWVMarketDrawExample drawExample = new CWVMarketDrawExample();
		drawExample.createCriteria().andChainStatusEqualTo(ChainTransStatusEnum.START.getKey());
		List<Object> list = propertyHelper.getDao().drawDao.selectByExample(drawExample);

		// 存储交易HASH
		HashSet transStatusSet = new HashSet<String>();

		for (Object o : list) {
			final CWVMarketDraw draw = (CWVMarketDraw) o;

			try {
				if (transStatusSet.contains(draw.getChainTransHash())) {
					continue;
				} else {

					String status = TransactionStatusTask.getTransStatus(propertyHelper, draw.getChainTransHash(),
							TransHashTypeEnum.DRAW.getValue());

					if (StringUtils.isEmpty(status)) {
						continue;
					}

					if (status.equals(ChainTransStatusEnum.DONE.getValue())) {
						draw.setChainStatus(ChainTransStatusEnum.DONE.getKey());
						final CWVGameProperty property = new CWVGameProperty();
						property.setPropertyId(draw.getPropertyId());
						property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
						property.setUserId(draw.getUserId());
						property.setLastPrice(new BigDecimal("1000"));
						property.setLastPriceTime(new Date());

						propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {

							@Override
							public Object doInTransaction() {

								propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
								propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);

								return null;
							}
						});
					} else if (status.equals(ChainTransStatusEnum.ERROR.getValue())) {
						draw.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
						propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {

							@Override
							public Object doInTransaction() {
								propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
								drawRollBack(draw.getUserId());
								return null;
							}
						});

					}
				}

			} catch (Exception e) {
				log.error("draw:drawId=" + draw.getDrawId() + "====drawTransStatusGroup done exception \n"
						+ e.getStackTrace());
			}
		}
	}

}
