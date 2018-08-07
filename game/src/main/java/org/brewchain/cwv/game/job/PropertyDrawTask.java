package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuy;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
import org.brewchain.cwv.game.enums.TransHashTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.wallet.service.Wallet.AccountValueImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionInputImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionOutputImpl;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetAccount;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 房产竞拍状态处理定时任务
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class PropertyDrawTask implements Runnable {

	private PropertyHelper propertyHelper ;
	
	public PropertyDrawTask(PropertyHelper propertyHelper) {
		this.propertyHelper = propertyHelper;
	}

	@Override
	public void run() {
		log.info("PropertyDrawTask start ....");
		try {
			//随机数处理
			drawRandom(propertyHelper.getDao());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			//房产transfer
			drawGroupProcess(propertyHelper.getDao());
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		try {
			//房产transfer
			drawTransStatusGroup();
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		
		log.info("PropertyDrawTask ended ....");
	}
	
	
	private void drawRandom(Daos dao) {
		CWVMarketDrawExample drawExample = new CWVMarketDrawExample();
		drawExample.createCriteria().andChainStatusIsNull()
		.andChainStatusRandomEqualTo(ChainTransStatusEnum.START.getKey())
		.andChainTransHashIsNotNull();//		.andchainStatus;
		List<Object> list = dao.bidDao.selectByExample(drawExample);
		
		for(Object o : list) {
			CWVMarketDraw draw = (CWVMarketDraw) o;
			String status = TransactionStatusTask.getTransStatus(propertyHelper, draw.getChainTransHashRandom(), "");
			
			RespGetTxByHash.Builder respGetTxByHash = propertyHelper.getWltHelper().getTxInfo(draw.getChainTransHashRandom());
			
			if(StringUtils.isEmpty(status)) {
				continue;
			}
			
			if(status.equals(ChainTransStatusEnum.DONE.getValue())){
				//获取随机数
				String random = respGetTxByHash.getTransaction().getResult();
				String token = getTokenByRandom(random);
				draw.setPropertyToken(token);
				draw.setChainRandom(random);
				draw.setChainStatusRandom(ChainTransStatusEnum.DONE.getKey());
				
				propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
				
			}
			else if(status.equals(ChainTransStatusEnum.ERROR.getValue())){
				draw.setChainStatusRandom(ChainTransStatusEnum.ERROR.getKey());
				
				propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
				
				drawRollBack(draw.getUserId());
			}
				
			
		}
		
	}
	

	private String getTokenByRandom(String random) {
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		String superUserId = propertyHelper.getCommonHelper().getSysSettingValue("super_user");
		
		example.createCriteria().andUserIdEqualTo(Integer.parseInt(superUserId)).andGameMapIdIsNotNull()
				.andPropertyTypeEqualTo("2").addCriterion(
						"property_id not in ( select property_id from cwv_market_draw where chain_status != '-1' or chain_status is null )");

		example.setOffset((int) Long.parseLong(random) - 1);
		Object o = propertyHelper.getDao().gamePropertyDao.selectOneByExample(example);

		final CWVGameProperty gameProperty = (CWVGameProperty) o;

		// add by murphy
//		String sysPropertyAddress = propertyHelper.getWltHelper().getWltUrl("sys_property_addr");// sys_property_addr
//																				// 获取系统参数
//		if (StringUtils.isBlank(sysPropertyAddress)) {
//			log.error("未找到sys_property_addr系统参数");
//			throw new IllegalArgumentException("抽奖系统出现错误，暂时无法抽奖");
//		}
//		RespGetAccount.Builder supAccount = propertyHelper.getWltHelper().getAccountInfo(sysPropertyAddress);// 房产超级账户
//		// 遍历判断账户是否有此房产，并获取cryptoToken
//		if (supAccount.getRetCode() != 1) {
//			log.error("未找到超级账户相关信息");
//			throw new IllegalArgumentException("抽奖系统出现错误，暂时无法抽奖");
//		}
//		List<AccountCryptoValueImpl> cryptosList = supAccount.getAccount().getCryptosList();
//		if (cryptosList.isEmpty()) {
//			log.error("未找到超级账户的erc721信息");
//			throw new IllegalArgumentException("房产已全部发放完毕，暂时无法抽奖");
//		}
		
//		String cryptoToken = null;
//		for (int i = 0; i < cryptosList.size(); i++) {
//			if (!cryptosList.get(i).getSymbol().equals(CryptoTokenEnum.CYT_HOUSE.getValue())) {
//				continue;// 如果不是房产类型的erc721，跳出继续搜索
//			}
//			List<AccountCryptoTokenImpl> tokens = cryptosList.get(i).getTokensList();
//			if (tokens.isEmpty()) {
//				log.error("超级账户无 [symbol=" + CryptoTokenEnum.CYT_HOUSE.getValue() + "]这个房产信息");
//				throw new IllegalArgumentException("房产已全部发放完毕，暂时无法抽奖");
//			}
//			for (int j = 0; j < tokens.size(); j++) {
//				if (tokens.get(j).getCode().equals(gameProperty.getPropertyId() + "")) {
//					cryptoToken = tokens.get(j).getHash();
//				}
//			}
//		}
//		if (StringUtils.isBlank(cryptoToken)) {
//			log.error("超级账户无 [propertyId=" + gameProperty.getPropertyId() + "]类型的信息");
//			throw new IllegalArgumentException("抽奖系统异常，请重新抽奖");
//		}
		
		
		return gameProperty.getCryptoToken();
	}

	private void drawGroupProcess(Daos dao){
//		SessionFilter.userMap
		CWVMarketDrawExample drawExample = new CWVMarketDrawExample();
		drawExample.createCriteria().andChainStatusIsNull()
		.andChainStatusRandomEqualTo(ChainTransStatusEnum.DONE.getKey())
		.andChainRandomIsNotNull()
		.andPropertyTokenIsNotNull();
		List<Object> list = dao.bidDao.selectByExample(drawExample);
		String outputAddress = "";
//		for(Object o : list) {
//			CWVMarketDraw draw = (CWVMarketDraw) o;
//			String token = draw.getPropertyToken();
//			RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(new BigDecimal(0), outputAddress, PropertyJobHandle.SYS_PROPERTY_ADDR, "house", token);
//			if(ret.getRetCode() == 1) {
//				draw.setChainStatus(ChainTransStatusEnum.START.getKey());
//				draw.setChainTransHash(ret.getTxHash());
//				propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
//				
//			}else if(ret.getRetCode() == -1 ){
//				drawRollBack(draw.getUserId());
//			}
//			
//		}
		
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		//获取发起发账户nonce
		
		RespGetAccount.Builder accountMap = propertyHelper.getWltHelper().getAccountInfo(PropertyJobHandle.SYS_PROPERTY_ADDR);
		if(accountMap==null){
			respCreateTransaction.setRetMsg("查询账户发生错误");
			respCreateTransaction.setRetCode(-1);
			log.debug("查询账户发生错误");
			return ;
		}
		
		List<MultiTransactionInputImpl.Builder> inputs = new ArrayList<>();
		List<MultiTransactionOutputImpl.Builder> outputs = new ArrayList<>();
		AccountValueImpl account = accountMap.getAccount();
		if(list == null || list.isEmpty())
			return;
		for(Object o : list) {
			CWVMarketDraw draw = (CWVMarketDraw) o;
			//token input
			MultiTransactionInputImpl.Builder inputToken = MultiTransactionInputImpl.newBuilder();
			inputToken.setAddress(accountMap.getAddress());//发起方地址 *
			inputToken.setNonce(account.getNonce());//交易次数 *
			inputToken.setCryptoToken(draw.getPropertyToken());
			inputToken.setSymbol("house");
			inputs.add(inputToken);
			
			//token output
			MultiTransactionOutputImpl.Builder outputToken = MultiTransactionOutputImpl.newBuilder();
			outputToken.setAddress(draw.getUserAddress());//接收方地址 *
			outputToken.setCryptoToken(draw.getPropertyToken());
			outputToken.setSymbol("house");
			outputs.add(outputToken);
		}
		
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(inputs, outputs);
		
		if(ret.getRetCode() == 1) {
			
			for(Object o : list){
				CWVMarketDraw draw = (CWVMarketDraw) o;
				draw.setChainStatus(ChainTransStatusEnum.START.getKey());
				draw.setChainTransHash(ret.getTxHash());
			}
			dao.exchangeBuyDao.batchUpdate(list);
		}else if(ret.getRetCode() == -1 ){
			for(Object o : list){
				CWVMarketDraw draw = (CWVMarketDraw) o;
				draw.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
			}
			dao.exchangeBuyDao.batchUpdate(list);
			drawRollBackList(list);
		}
	}
	/**
	 * 回滚抽奖次数
	 * @param list
	 */
	private void drawRollBackList(List<Object> list) {
		for(Object o : list){
			CWVMarketDraw draw = (CWVMarketDraw) o;
			try {
				drawRollback(draw);
			} catch (Exception e) {
				// TODO: 人工处理
				log.error("drawRollBack error : drawId="+draw.getDrawId());
			}
			
		}
		
	}

	/**
	 * 回滚抽奖次数
	 * @param userId
	 */
	private void drawRollBack(Integer userId) {
		CWVUserWallet userWallet = propertyHelper.getWalletHelper().getUserAccount(userId, CoinEnum.CWB);
		userWallet.setDrawCount(userWallet.getDrawCount()+1);
		propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(userWallet);
	}
	
	
	/**
	 * 抽奖交易状态批量处理
	 */
	private void drawTransStatusGroup(){
		CWVMarketDrawExample drawExample = new CWVMarketDrawExample();
		drawExample.createCriteria().andChainStatusEqualTo(ChainTransStatusEnum.START.getKey());
		List<Object> list = propertyHelper.getDao().drawDao.selectByExample(drawExample);
		
		//存储交易HASH
		HashSet transStatusSet = new HashSet<String>();
		
		for(Object o : list){
			final CWVMarketDraw draw = (CWVMarketDraw) o;
			
			try {
				if(transStatusSet.contains(draw.getChainTransHash())) {
					continue ;
				}else {
					
					String status = TransactionStatusTask.getTransStatus(propertyHelper, draw.getChainTransHash(), TransHashTypeEnum.DRAW.getValue());
					
					if(StringUtils.isEmpty(status)) {
						continue;
					}
					
					if(status.equals(ChainTransStatusEnum.DONE.getValue())) {
						draw.setChainStatus(ChainTransStatusEnum.DONE.getKey());
						final CWVGameProperty property =  new CWVGameProperty();
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
					else if(status.equals(ChainTransStatusEnum.ERROR.getValue())) {
						draw.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
						propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {
							
							@Override
							public Object doInTransaction() {
								propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
								drawRollback(draw);
								return null;
							}
						});
						
					}
				}
			
			} catch (Exception e) {
				log.error("draw:drawId="+draw.getDrawId()+"====drawTransStatusGroup done exception \n"+e.getStackTrace());
			}
		}
	}
	
	
	private void drawTransStatusGroupBack(){
		CWVMarketDrawExample drawExample = new CWVMarketDrawExample();
		drawExample.createCriteria().andChainStatusIsNull()
		.andChainStatusRandomEqualTo(ChainTransStatusEnum.DONE.getKey())
		.andChainRandomIsNotNull();
		List<Object> list = propertyHelper.getDao().drawDao.selectByExample(drawExample);
		
		//存储交易HASH
		HashSet transStatusSet = new HashSet<String>();
		
		for(Object o : list){
			final CWVMarketDraw draw = (CWVMarketDraw) o;
			
			try {
				if(transStatusSet.contains(draw.getChainTransHash())) {
					continue ;
				}else {
					
					String status = TransactionStatusTask.getTransStatus(propertyHelper, draw.getChainTransHash(), TransHashTypeEnum.DRAW.getValue());
					
					if(StringUtils.isEmpty(status)) {
						continue;
					}
					
					if(status.equals(ChainTransStatusEnum.DONE.getValue())) {
						draw.setChainStatus(ChainTransStatusEnum.DONE.getKey());
						final CWVGameProperty property =  new CWVGameProperty();
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
					else if(status.equals(ChainTransStatusEnum.ERROR.getValue())) {
						draw.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
						propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {
							
							@Override
							public Object doInTransaction() {
								propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
								drawRollback(draw);
								return null;
							}
						});
						
					}
				}
			
			} catch (Exception e) {
				log.error("draw:drawId="+draw.getDrawId()+"====drawTransStatusGroup done exception \n"+e.getStackTrace());
			}
		}
	}

	private void drawRollback(CWVMarketDraw draw) {

		try {
			CWVUserWallet wallet = propertyHelper.getWalletHelper()
					.getUserAccount(draw.getUserId(), CoinEnum.CWB); 
			wallet.setDrawCount(wallet.getDrawCount()+1);
			
		} catch (Exception e) {
			// TODO: 人工插入表
			
		}
		
	}
	 
	
}
