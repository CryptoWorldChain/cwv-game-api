package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuy;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.TransHashTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.wallet.service.Wallet.AccountValueImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionInputImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionOutputImpl;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetAccount;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 房产竞拍状态处理定时任务
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class PropertyExchangeBuyTask implements Runnable {

	private PropertyHelper propertyHelper ;
	
	public PropertyExchangeBuyTask(PropertyHelper propertyHelper) {
		this.propertyHelper = propertyHelper;
	}
	
	@Override
	public void run() {
		log.info("PropertyExchangeBuyTask start ....");
		try {
			//买入交易状态处理
			buyTransStatus();
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		
		try {
			//转账状态处理位于TransactionStatusTask
			exchangeBuyGroupProcess(propertyHelper.getDao());
			
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		
		log.info("PropertyExchangeBuyTask ended ....");
	}
	
	/**
	 * 买入操作二group处理
	 * @param dao
	 */
	private void exchangeBuyGroupProcess(Daos dao){
//		SessionFilter.userMap
		//查询转账成功的申请记录
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		buyExample.createCriteria()
		.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey())
		.andChainStatusGroupIsNull();
		List<Object> list = dao.exchangeBuyDao.selectByExample(buyExample);
		String outputAddress = "";
		//整合分组交易
		//发起方详情
		
		
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		//获取发起发账户nonce
		
		RespGetAccount.Builder accountMap = propertyHelper.getWltHelper().getAccountInfo(PropertyJobHandle.MARKET_EXCHANGE_AGENT);
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
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			
			//amount input
			MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
			input.setAddress(accountMap.getAddress());//发起方地址 *
			input.setNonce(account.getNonce());//交易次数 *
			input.setAmount(buy.getAmount().multiply(new BigDecimal(1).subtract(new BigDecimal(PropertyJobHandle.EXCHANGE_CHARGE))).toString() );
			inputs.add(input);
			

			//amount output
			MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
			output.setAddress(buy.getSellerAddress());//接收方地址 *
			output.setAmount(input.getAmount());
			
			outputs.add(output);
			
			//amount charge input
			MultiTransactionInputImpl.Builder inputCharge = MultiTransactionInputImpl.newBuilder();
			inputCharge.setAddress(accountMap.getAddress());//发起方地址 *
			inputCharge.setNonce(account.getNonce());//交易次数 *
			inputCharge.setAmount(buy.getAmount().subtract(new BigDecimal(input.getAmount())).toString() );
			inputs.add(inputCharge);
			

			//amount charge output
			MultiTransactionOutputImpl.Builder outputCharge = MultiTransactionOutputImpl.newBuilder();
			outputCharge.setAddress(PropertyJobHandle.SYS_INCOME_ADDRESS);//接收方地址 *
			outputCharge.setAmount(inputCharge.getAmount());
			
			outputs.add(output);
			
			//token input
			MultiTransactionInputImpl.Builder inputToken = MultiTransactionInputImpl.newBuilder();
			inputToken.setAddress(accountMap.getAddress());//发起方地址 *
			inputToken.setNonce(account.getNonce());//交易次数 *
			inputToken.setCryptoToken(buy.getPropertyToken());
			inputToken.setSymbol("house");
			inputs.add(inputToken);
			
			//token output
			MultiTransactionOutputImpl.Builder outputToken = MultiTransactionOutputImpl.newBuilder();
			outputToken.setAddress(buy.getBuyerAddress());//接收方地址 *
			outputToken.setCryptoToken(buy.getPropertyToken());
			outputToken.setSymbol("house");
			outputs.add(outputToken);
		}
		
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(inputs, outputs);
		if(ret.getRetCode() == 1) {
			
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatusGroup(ChainTransStatusEnum.START.getKey());
				buy.setChainTransHashGroup(ret.getTxHash());
			}
			dao.exchangeBuyDao.batchUpdate(list);
		}else if(ret.getRetCode() == -1 ){
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatusGroup(ChainTransStatusEnum.ERROR.getKey());
			}
			dao.exchangeBuyDao.batchUpdate(list);
			exchangeBuyRollBackList(list);
		}
		
	}
	
	/**
	 * 回滚买家金额 发起回滚交易
	 * @param userId
	 */
	private void exchangeBuyRollBack(String chainTransHashGroup) {
		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
		example.createCriteria().andChainTransHashGroupEqualTo(chainTransHashGroup);
		List<Object> list = propertyHelper.getDao().exchangeBuyDao.selectByExample(example);
		exchangeBuyRollBackList(list);
	}
	
	/**
	 * 回滚买家金额 发起回滚交易
	 * @param userId
	 */
	private void exchangeBuyRollBackList(List<Object> list) {
		
		for(Object o : list) {
			
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			
			exchangeBuyRollBack(buy);
			
		}
//		
//		CWVUserWallet userWallet = propertyHelper.getWalletHelper().getUserAccount(userId, CoinEnum.CWB);
//		userWallet.setDrawCount(userWallet.getDrawCount()+1);
//		propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(userWallet);
	}
	
	/**
	 * 回滚买家金额 发起回滚交易
	 * @param userId
	 */
	private void exchangeBuyRollBack(CWVMarketExchangeBuy buy) {
			
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(buy.getAmount(), buy.getBuyerAddress(), PropertyJobHandle.SYS_PROPERTY_ADDR);
		if(ret.getRetCode() == 1){
			//TODO 回滚交易字段赋值
			propertyHelper.getDao().exchangeBuyDao.updateByPrimaryKeySelective(buy);
		}else{
			//数据库插入日志
//			CWVMarketChainRecord chainRecord
			
			log.error(String.format(" buy error :: rollback error:: [inputaddress: %s ,inputaddress: %s , amount: %s ]",PropertyJobHandle.SYS_PROPERTY_ADDR,buy.getBuyerAddress(),buy.getAmount()));
		}
			
	}
	
	
	/**
	 * 定时任务处理买入房产交易 包含买家转账 以及 二次混合交易
	 */
	private void buyTransStatus() {
		
		CWVMarketExchangeBuyExample buyChain = new CWVMarketExchangeBuyExample();
		buyChain.createCriteria()
		.andChainStatusEqualTo(ChainTransStatusEnum.START.getKey());
		List<Object> listChain = propertyHelper.getDao().exchangeBuyDao.selectByExample(buyChain);
		
		CWVMarketExchangeBuyExample buyChainGroup = new CWVMarketExchangeBuyExample();
		buyChainGroup.createCriteria()
		.andChainStatusGroupEqualTo(ChainTransStatusEnum.START.getKey());
		List<Object> listChainGroup = propertyHelper.getDao().exchangeBuyDao.selectByExample(buyChainGroup);
		
		CWVMarketExchangeBuyExample buyChainRollback = new CWVMarketExchangeBuyExample();
		buyChainRollback.createCriteria()
		.andChainStatusRollbackEqualTo(ChainTransStatusEnum.START.getKey());
		List<Object> listChainRollback = propertyHelper.getDao().exchangeBuyDao.selectByExample(buyChainRollback);
		
		//存储交易HASH
		HashSet transStatusSet = new HashSet<String>();
		for(Object o : listChain) {
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			if(transStatusSet.contains(buy.getChainTransHash())) {
				continue ;
			}else {
				HashMap busiMap = new HashMap<String,String>();
				busiMap.put("exchangeId", buy.getExchangeId());
				busiMap.put("txHash", buy.getChainTransHashGroup());
				
				String status = TransactionStatusTask.getTransStatus(propertyHelper,buy.getChainTransHash(), TransHashTypeEnum.EXCHANGE_BUY.getValue(), busiMap);
				if(StringUtils.isEmpty(status)) 
					continue;
				
				if(status.equals(ChainTransStatusEnum.DONE.getValue()))
					exchangeBuyDone(buy);
				else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
					exchangeBuyError(buy);
				else if (status.equals(ChainTransStatusEnum.EXCEPTION.getValue())) {
					exchangeBuyException(buy);
				}
				transStatusSet.add(buy.getChainTransHash());
			}
			
		}
	
	
		for(Object o : listChainGroup) {
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			if(transStatusSet.contains(buy.getChainTransHash())) {
				continue ;
			}else {
				HashMap busiMap = new HashMap<String,String>();
				busiMap.put("txHash", buy.getChainTransHashGroup());
				
				String status = TransactionStatusTask.getTransStatus(propertyHelper, buy.getChainTransHashGroup(), TransHashTypeEnum.EXCHANGE_BUY_GROUP.getValue(), busiMap);
				if(StringUtils.isEmpty(status)) 
					continue;
				
				if(status.equals(ChainTransStatusEnum.DONE.getValue()))
					exchangeBuyGroupDone(buy.getChainTransHashGroup());
				else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
					exchangeBuyGroupError(buy.getChainTransHashGroup());
				
				transStatusSet.add(buy.getChainTransHash());
			}
		
		}
		
		for(Object o : listChainRollback) {
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			if(transStatusSet.contains(buy.getChainTransHash())) {
				continue ;
			}else {
				HashMap busiMap = new HashMap<String,String>();
				busiMap.put("txHash", buy.getChainTransHashRollback());
				
				String status = TransactionStatusTask.getTransStatus(propertyHelper, buy.getChainTransHashGroup(), TransHashTypeEnum.EXCHANGE_BUY_GROUP.getValue(), busiMap);
				if(StringUtils.isEmpty(status)) 
					continue;
				
				if(status.equals(ChainTransStatusEnum.DONE.getValue()))
					exchangeBuyRollBackDone(buy.getChainTransHashRollback());
				else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
					exchangeBuyRollBackError(buy.getChainTransHashRollback());
				
				transStatusSet.add(buy.getChainTransHash());
			}
		
		}
		
		
	}
	/**
	 * 执行异常
	 * @param buy
	 */
	private void exchangeBuyException(CWVMarketExchangeBuy buy) {
		buy.setChainStatus(ChainTransStatusEnum.EXCEPTION.getKey());
		propertyHelper.getDao().exchangeBuyDao.updateByPrimaryKeySelective(buy);
		
	}

	private void exchangeBuyRollBackError(String chainTransHashRollBack) {

		//更新交易状态
		updateTransRollBackStatus(chainTransHashRollBack,ChainTransStatusEnum.ERROR.getKey());
		//退款失败 人工处理
//		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
//		example.createCriteria().andChainTransHashRollbackEqualTo(chainTransHashRollBack);
//		List<Object> list = propertyHelper.getDao().exchangeBuyDao.selectByExample(example);
//		exchangeBuyRollBack(list);
		
	}

	private void exchangeBuyRollBackDone(String chainTransHashGroup) {
		updateTransRollBackStatus(chainTransHashGroup, ChainTransStatusEnum.DONE.getKey());
		
		//退款成功 处理
		
	}

	/**
	 * group交易状态失败
	 * @param chainTransHashGroup
	 */
	private void exchangeBuyGroupError(final String chainTransHashGroup) {
		propertyHelper.getDao().exchangeBuyDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				//更新交易状态
				updateBuyGroupStatus(chainTransHashGroup,ChainTransStatusEnum.ERROR.getKey());
				//回滚相关买入交易
				updateExchangeGroup(chainTransHashGroup,ChainTransStatusEnum.ERROR.getKey());
				
				//回滚买家金额
				exchangeBuyRollBack(chainTransHashGroup);
				return null;
			}
		});
		
	}

	/**
	 * group交易状态成功
	 * @param chainTransHashGroup
	 */
	private void exchangeBuyGroupDone(final String chainTransHashGroup) {
		propertyHelper.getDao().exchangeBuyDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {

				//更新买入申请表
				updateBuyGroupStatus(chainTransHashGroup,ChainTransStatusEnum.DONE.getKey());
				
				//更新交易
				updateExchangeGroup(chainTransHashGroup,ChainTransStatusEnum.DONE.getKey());
				
				//更新房产
				updatePropertyGroupDone(chainTransHashGroup);
				
				return null;
			}
		});
		
		
	}
	private void updateExchangeGroup(String chainTransHashGroup, byte status) {
		CWVMarketExchangeExample exchangeExample = new CWVMarketExchangeExample();
		exchangeExample.createCriteria().andChainTransHashEqualTo(chainTransHashGroup);
		
		CWVMarketExchange exchange = new CWVMarketExchange();
		exchange.setChainStatus(status);
		propertyHelper.getDao().exchangeDao.updateByExampleSelective(exchange, exchangeExample);
		
	}
	
	/**
	 * 更新房产
	 * @param chainTransHashGroup
	 */
	private void updatePropertyGroupDone(String chainTransHashGroup) {
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andChainTransHashEqualTo(chainTransHashGroup);
		
		CWVGameProperty exchange = new CWVGameProperty();
		
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		propertyHelper.getDao().exchangeDao.updateByExampleSelective(exchange, example);
//		
	}

	/**
	 * 更新交易状态
	 * @param hash
	 * @param status
	 */
	private void updateTransRollBackStatus(String hash, byte status) {
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		buyExample.createCriteria().andChainTransHashGroupEqualTo(hash);
		
		CWVMarketExchangeBuy buy = new CWVMarketExchangeBuy();
		buy.setChainStatusGroup(status);
		propertyHelper.getDao().exchangeBuyDao.updateByExampleSelective(buy, buyExample);
		
	}

	private void exchangeBuyError(CWVMarketExchangeBuy buy) {
		buy.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		propertyHelper.getDao().exchangeBuyDao.updateByPrimaryKeySelective(buy);
		
		//通知买家买入失败
		
	}
	
	private void updateBuyGroupStatus(String hash, byte status) {
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		buyExample.createCriteria().andChainTransHashGroupEqualTo(hash);
		
		CWVMarketExchangeBuy buy = new CWVMarketExchangeBuy();
		buy.setChainStatusGroup(status);
		propertyHelper.getDao().exchangeBuyDao.updateByExampleSelective(buy, buyExample);
		
	}

	/**
	 * 购买
	 * @param buy
	 */
	private void exchangeBuyDone(CWVMarketExchangeBuy buy) {

		buy.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		propertyHelper.getDao().exchangeBuyDao.updateByPrimaryKeySelective(buy);
		
	}
	 
	
}
