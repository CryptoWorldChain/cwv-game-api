package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.MarketTypeEnum;
import org.brewchain.cwv.game.enums.PropertyBidStatusEnum;
import org.brewchain.cwv.game.enums.PropertyExchangeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
import org.brewchain.cwv.game.enums.PropertyTypeEnum;
import org.brewchain.cwv.game.helper.GameNoticeHelper;
import org.brewchain.cwv.game.helper.GameNoticeHelper.NoticeTradeTypeEnum;
import org.brewchain.cwv.game.helper.GameNoticeHelper.NoticeTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 定时任务查询合约交易状态并 更新业务状态
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class TransactionStatusTask implements Runnable {
	//防止收益数据冲突
	public static HashSet<Integer> jobIncomeSet = new HashSet<>();
	
	public final static float RATE = 0.25f;
	public final static float ORDINARY_TO_DIVIDED = 0.4f;
	public final static float TYPICAL_TO_DIVIDED = 0.6f;
	
	public final static int DAY_PERIOD = 7 ; //7日
	public final static String INCOME_TIME = "income_time" ; //7日
	
	private PropertyHelper propertyHelper;
	
	
	public TransactionStatusTask(PropertyHelper propertyHelper) {
		super();
		this.propertyHelper = propertyHelper;
	}


	@Override
	public void run() {
		log.info("TransactionStatusTask start ....");
		//1 执行交易 卖出，买入，撤销卖出
		List<Object> listExchange = getUndoneExchange();
		for(Object o : listExchange) {
			CWVMarketExchange exchange = (CWVMarketExchange) o;
			RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(exchange.getChainTransHash());
			if(respGetTxByHash.getRetCode() == -1) {//失败
				log.error("exchangeId:"+exchange.getExchangeId()+",chainTransHash:"+exchange.getChainTransHash()+"==>查询异常");
				continue;
			}
			
			String status = respGetTxByHash.getTransaction().getStatus();
			
			if( status == null || status.equals("") ) {
				continue;
			}
			
			exchangeProcess(exchange,status);

		}
		
		//2 执行竞拍  发起竞拍 撤销
		List<Object> listBid = getUndoneBid();
		
		for(Object o : listBid) {
			CWVMarketBid bid = (CWVMarketBid) o;
			RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(bid.getChainTransHash());
			if(respGetTxByHash.getRetCode() == -1) {//失败
				log.error("bidId:"+bid.getBidId()+",chainTransHash:"+bid.getChainTransHash()+"==>查询异常");
				continue;
			}
			String status = respGetTxByHash.getTransaction().getStatus();
			
			if( status == null || status.equals("") ) {
				continue;
			}
			
			bidProcess(bid,status);
			
			
		}
		
		//执行竞价 竞价
		List<Object> listAuction = getUndoneAuction();
		
		for(Object o : listAuction) {
			CWVMarketAuction auction = (CWVMarketAuction) o;
			RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(auction.getChainTransHash());
			if(respGetTxByHash.getRetCode() == -1) {//失败
				log.error("auctionId:"+auction.getBidId()+",chainTransHash:"+auction.getChainTransHash()+"==>查询异常");
				continue;
			}
			String status = respGetTxByHash.getTransaction().getStatus();
			
			if( status == null || status.equals("") ) {
				continue;
			}
			
			auctionProcess(auction,status);
			
			
		}
		//抽奖
		List<Object> listDraw = getUndoneDraw();
		for(Object o : listDraw) {
			CWVMarketDraw draw = (CWVMarketDraw) o;
			RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(draw.getChainTransHash());
			if(respGetTxByHash.getRetCode() == -1) {//失败
				log.error("drawId:"+draw.getDrawId()+",chainTransHash:"+draw.getChainTransHash()+"==>查询异常");
				continue;
			}
			String status = respGetTxByHash.getTransaction().getStatus();
			
			if( status == null || status.equals("") ) {
				continue;
			}
			
			drawProcess(draw,status);
			
			
		}
		
		log.info("TransactionStatusTask ended ....");
	}
	
	
	private void drawProcess(CWVMarketDraw draw, String status) {
		if(status.equals(ChainTransStatusEnum.DONE.getValue()))
			drawDone(draw);
		else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
			drawError(draw);
		
	}

	/**
	 * 抽奖房产交易失败
	 * @param draw
	 */
	private void drawError(final CWVMarketDraw draw) {
		//抽奖合约交易状态
		draw.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		
		final CWVUserWallet wallet = propertyHelper.getWalletHelper().getUserAccount(draw.getUserId(), CoinEnum.CWB);
		wallet.setDrawCount(wallet.getDrawCount()+1);
		
		propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				//更新抽奖信息
				propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
				
				//回退抽奖次数
				propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(wallet);
				
				return null;
			}
		});
		
		
	}

	/**
	 * 抽奖房产交易成功
	 * @param draw
	 */
	private void drawDone(final CWVMarketDraw draw) {

		//抽奖合约交易状态
		draw.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		
		//房产信息
		final CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setUserId(draw.getUserId());
		gameProperty.setPropertyStatus("0");
		gameProperty.setLastPrice(new BigDecimal("1000"));
		
		propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {

				//抽奖信息
				propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
				//房产信息
				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(gameProperty);
				return null;
			}
		});
		// 更新房产信息
		
		
	}

	/**
	 * 竞价处理
	 * @param auction
	 * @param status
	 */
	private void auctionProcess(CWVMarketAuction auction, String status) {
		if(status.equals(ChainTransStatusEnum.DONE.getValue()))
			auctionDone(auction);
		else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
			auctionError(auction);
	}


	private void auctionError(final CWVMarketAuction auction) {
		
		auction.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		
		
		CWVMarketBid bidOld = new CWVMarketBid();
		bidOld.setBidId(auction.getBidId());
		final CWVMarketBid bid = propertyHelper.getDao().bidDao.selectByPrimaryKey(bidOld);
		
		CWVMarketAuctionExample example = new CWVMarketAuctionExample();
		// 竞拍账户
		final CWVUserWallet auctionAccount = propertyHelper.getWalletHelper().getUserAccount(auction.getUserId(), CoinEnum.CWB);

		
		final CWVUserTransactionRecord recordAuction = new CWVUserTransactionRecord();
		// 设置交易记录
		recordAuction.setCreateTime(new Date());
		recordAuction.setCreateUser(auction.getUserId());
		recordAuction.setDetail("竞价退回");
		recordAuction.setUserId(auction.getUserId());
		
		// 账户金额
		BigDecimal gainCost = auction.getBidPrice().subtract(auction.getBidPrice());
		auctionAccount.setBalance(auctionAccount.getBalance().add(gainCost));
	
		recordAuction.setGainCost(gainCost);
		
		propertyHelper.getDao().bidDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				// 竞价信息
				
				propertyHelper.getDao().auctionDao.updateByPrimaryKeySelective(auction);

				// 3更新竞拍信息 最高价 拥有者 更新时间
				CWVMarketAuctionExample example = new CWVMarketAuctionExample();
				example.createCriteria()
				.andBidIdEqualTo(bid.getBidId())
				.andStatusEqualTo((byte) 1)
				.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey());
				example.setOrderByClause(" bid_price desc ");
				CWVMarketAuction old = (CWVMarketAuction) propertyHelper.getDao().auctionDao.selectOneByExample(example);
			
				bid.setBidAmount(old.getBidPrice());
				bid.setOwner(old.getUserId());
				bid.setLastUpdateTime(new Date());
				// 更新竞拍信息
				propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
				// 更新竞价用户账户金额
				auctionAccount.setUpdateTime(new Date());
				propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(auctionAccount);
				// 插入竞价用户钱包操作记录
				propertyHelper.getDao().userTransactionRecordDao.insert(recordAuction);
				return null;
			}
		});
		
		
	}


	private void auctionDone(final CWVMarketAuction auction) {
		
		auction.setChainStatus(ChainTransStatusEnum.DONE.getKey());
	
		CWVMarketBid marketBid = new CWVMarketBid();
		marketBid.setBidId(auction.getBidId());
		marketBid.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		final CWVMarketBid bid = propertyHelper.getDao().bidDao.selectByPrimaryKey(marketBid);
		// 发起竞拍账户
		final CWVUserWallet bidAccount = propertyHelper.getWalletHelper().getUserAccount(Integer.parseInt(bid.getCreateUser()), CoinEnum.CWB);
		bidAccount.setBalance(bidAccount.getBalance().add(auction.getBidPrice()).subtract(auction.getLastBidPrice()));
		
		final CWVUserTransactionRecord recordBid = new CWVUserTransactionRecord();
		// 设置交易记录
		recordBid.setCreateTime(new Date());
		recordBid.setCreateUser(Integer.parseInt(bid.getCreateUser()));
		recordBid.setDetail("收到竞价");
		recordBid.setUserId(Integer.parseInt(bid.getCreateUser()));
		recordBid.setGainCost(auction.getBidPrice().subtract(auction.getLastBidPrice()));
		//
		recordBid.setMarketId(bid.getBidId());
		recordBid.setMarketId(bid.getBidId());
		recordBid.setType(MarketTypeEnum.BID.getValue());
		
		propertyHelper.getDao().bidDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				//竞价信息更新
				propertyHelper.getDao().auctionDao.updateByPrimaryKeySelective(auction);
				//房产竞拍信息更新
				propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
				//发起者账户余额更新
				propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(bidAccount);
				//发起者账户交易记录
				propertyHelper.getDao().userTransactionRecordDao.insert(recordBid);
				return null;
			}
		});
	}


	private void bidProcess(CWVMarketBid bid, String status) {

		//发起竞拍
		if(bid.getStatus().byteValue() == PropertyBidStatusEnum.PREVIEW.getValue()) {
			
			if(status.equals(ChainTransStatusEnum.DONE.getValue()))
				bidCreateDone(bid);
			else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
				bidCreateError(bid);
		}
		
		//取消竞拍
		if(bid.getStatus().byteValue() == PropertyExchangeStatusEnum.CANCEL.getValue()) {
			
			if(status.equals(ChainTransStatusEnum.DONE.getValue()))
				bidCancelDone(bid);
			else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
				bidCancelError(bid);
		}
		
		
	}


	private void bidCancelError(CWVMarketBid bid) {
		bid.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		bid.setStatus(PropertyBidStatusEnum.PREVIEW.getValue());
		propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
	}


	private void bidCancelDone(final CWVMarketBid bid) {
		bid.setStatus(PropertyBidStatusEnum.CANCEL.getValue());
			
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(bid.getGamePropertyId());
		property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
		propertyHelper.getDao().bidDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				
				propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
				return null;
			}
		});
		
	}


	private void bidCreateError(final CWVMarketBid bid) {
		bid.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
		
	}


	private void bidCreateDone(final CWVMarketBid bid) {

		bid.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(bid.getGamePropertyId());
		property.setPropertyStatus(PropertyStatusEnum.BIDDING.getValue());// 竞拍中

		// 更新房产
		propertyHelper.getDao().bidDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				
				propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
				return null;
			}
		});
		
		//创建公告
		CWVAuthUser user = propertyHelper.getUserHelper().getUserById(property.getUserId());
		String noticeContent = propertyHelper.getGameNoticeHelper().noticeTradeTpl(NoticeTradeTypeEnum.BID.getValue(),user.getNickName(),property.getPropertyName(), null );
	
		Calendar c = Calendar.getInstance();
		String startTime = DateUtil.getDayTime(c.getTime());
		c.add(Calendar.MINUTE, 2);
//		propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.TRADE.getValue(), startTime, DateUtil.getDayTime(c.getTime()), "5", "3", noticeContent);
	
		
	}


	private void exchangeProcess(CWVMarketExchange exchange, String status) {
		
		//执行卖出
		if(exchange.getStatus().equals(PropertyExchangeStatusEnum.ONSALE.getValue())) {
			
			if(status.equals(ChainTransStatusEnum.DONE.getValue()))
				exchangeOnsaleDone(exchange);
			else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
				exchangeOnsaleError(exchange);
		}
		//买入
		if(exchange.getStatus().equals(PropertyExchangeStatusEnum.SOLD.getValue())) {
			if(status.equals(ChainTransStatusEnum.DONE.getValue()))
				exchangeSoldDone(exchange);
			else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
				exchangeSoldError(exchange);
			
			
		}
		//撤销
		if(exchange.getStatus().equals(PropertyExchangeStatusEnum.CANCEL.getValue())) {
			if(status.equals(ChainTransStatusEnum.DONE.getValue()))
				exchangeCancelDone(exchange);
			else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
				exchangeCancelError(exchange);
			
			
		}
		
		
	}


	private void exchangeCancelDone(final CWVMarketExchange exchange) {
		//更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		
		//房产信息
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
		propertyHelper.getDao().exchangeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				
				// 更新交易
				propertyHelper.getDao().exchangeDao.updateByPrimaryKeySelective(exchange);
			
				//更新房产
				propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
				return null;
			}
		});
		
		
		
	}


	private void exchangeCancelError(final CWVMarketExchange exchange) {
		//
		//更新交易信息
		exchange.setStatus(PropertyExchangeStatusEnum.ONSALE.getValue());
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		
		propertyHelper.getDao().exchangeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				
				propertyHelper.getDao().exchangeDao.updateByPrimaryKeySelective(exchange);
				
				return null;
			}
		});
		//
		
	}



	private void exchangeSoldDone(final CWVMarketExchange exchange) {
		//更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		//房产信息
		
		
		// 房产信息更新
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setLastPrice(exchange.getSellPrice());
		property.setLastPriceTime(new Date());
		property.setUserId(exchange.getUserId());
		property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
		
		
		final CWVUserWallet userWalletSeller = propertyHelper.getWalletHelper().getUserAccount(exchange.getCreateUser(), CoinEnum.CWB);
		userWalletSeller.setBalance(userWalletSeller.getBalance().add(exchange.getSellPrice()).subtract(exchange.getTax()));

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
		

		//生成公告
		String period =  propertyHelper.getCommonHelper().getSysSettingValue(GameNoticeHelper.NOTICE_TRADE_PERIOD);
		String count = propertyHelper.getCommonHelper().getSysSettingValue(GameNoticeHelper.NOTICE_TRADE_COUNT);
		String noticeContent = null;
		CWVAuthUser authUser = propertyHelper.getUserHelper().getUserById(exchange.getUserId());
		
		if(property.getPropertyType().equals(PropertyTypeEnum.TYPICAL.getValue())) {
			noticeContent = propertyHelper.getGameNoticeHelper().noticeTradeTpl(NoticeTradeTypeEnum.TYPICAL_GET.getValue(),authUser.getNickName(),property.getPropertyName(), "购买房产" );
		}else if(property.getPropertyType().equals(PropertyTypeEnum.FUNCTIONAL.getValue())) {
			noticeContent = propertyHelper.getGameNoticeHelper().noticeTradeTpl(NoticeTradeTypeEnum.FUNCTIONAL_GET.getValue(),authUser.getNickName(),property.getPropertyName(), "购买房产" );
		}
		
//		if(noticeContent != null ) {
//			propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.TRADE.getValue(), null, null, period, count, noticeContent);
//		}
//		if(noticeContent != null ) {
//			Calendar c = Calendar.getInstance();
//			String startTime = DateUtil.getDayTime(c.getTime());
//			c.add(Calendar.MINUTE, 2);
//			propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.TRADE.getValue(), startTime, DateUtil.getDayTime(c.getTime()), "5", "3", noticeContent);
//		}
		
		
	}
	
	/**
	 * 
	 * @param exchange
	 */
	private void exchangeSoldError(final CWVMarketExchange exchange) {
		//更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		//回滚交易信息 
		exchange.setStatus(PropertyExchangeStatusEnum.ONSALE.getValue());
		exchange.setUserId(null);
		
		//买家账户信息
		final CWVUserWallet userWalletBuyer = propertyHelper.getWalletHelper().getUserAccount(exchange.getUserId(), CoinEnum.CWB);
		
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


	private void exchangeOnsaleDone(final CWVMarketExchange exchange) {
		//更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setPropertyStatus(PropertyStatusEnum.ONSALE.getValue());// 出售中
		
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
		
		
		//创建公告
		CWVAuthUser user = propertyHelper.getUserHelper().getUserById(exchange.getCreateUser());
		String noticeContent = propertyHelper.getGameNoticeHelper().noticeTradeTpl(NoticeTradeTypeEnum.SELL.getValue(),user.getNickName(),property.getPropertyName(), null );
		Calendar c = Calendar.getInstance();
		String startTime = DateUtil.getDayTime(c.getTime());
		c.add(Calendar.MINUTE, 2);
//		propertyHelper.getGameNoticeHelper().noticeCreate(NoticeTypeEnum.TRADE.getValue(), startTime, DateUtil.getDayTime(c.getTime()), "5", "3", noticeContent);
			
		
	}


	private void exchangeOnsaleError(CWVMarketExchange exchange) {
		//更新交易信息
		exchange.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		propertyHelper.getDao().exchangeDao.updateByPrimaryKeySelective(exchange);
		
	}
	
	
	private List<Object>  getUndoneExchange() {
		//
		CWVMarketExchangeExample example = new CWVMarketExchangeExample();
		example.createCriteria().andChainStatusEqualTo(ChainTransStatusEnum.START.getKey());
		
		List<Object> list = propertyHelper.getDao().getBidDao().selectByExample(example);
		return list;
	}
	
	private List<Object>  getUndoneAuction() {
		//
		CWVMarketAuctionExample example = new CWVMarketAuctionExample();
		example.createCriteria().andChainStatusEqualTo(ChainTransStatusEnum.START.getKey());
		return propertyHelper.getDao().getAuctionDao().selectByExample(example);
	}
	private List<Object>  getUndoneBid() {
		//
		CWVMarketBidExample example = new CWVMarketBidExample();
		example.createCriteria().andChainStatusEqualTo(ChainTransStatusEnum.START.getKey());
		
		return propertyHelper.getDao().getBidDao().selectByExample(example);
	}
	
	private List<Object>  getUndoneDraw() {
		//
		CWVMarketDrawExample example = new CWVMarketDrawExample();
		example.createCriteria().andChainStatusEqualTo(ChainTransStatusEnum.START.getKey());
		
		return propertyHelper.getDao().getDrawDao().selectByExample(example);
	}
	
	
	/**
	 * 查询交易hash状态
	 * @param hash
	 * @param hashType 参照TransHashEnum
	 * @param busiMap 业务日志记录MAP参数
	 * @return
	 */
	public static String getTransStatus(PropertyHelper propertyHelper,String hash,String hashType, HashMap<String,String> busiMap){
		String status = null;
		RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(hash);
		if(respGetTxByHash.getRetCode() == -1) {//失败
			StringBuffer sb = new StringBuffer(hashType).append("==>transaction hash check error").append(":::");
			for(Map.Entry<String,String>  key : busiMap.entrySet()){
				sb.append(key).append(":").append(busiMap.get(key));
			}
			log.error(sb.toString());
		}else
			status = respGetTxByHash.getTransaction().getStatus();
		return status;
	}

}





