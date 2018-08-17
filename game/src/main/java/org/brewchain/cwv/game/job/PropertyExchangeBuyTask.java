package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameTxManage;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuy;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.MarketTypeEnum;
import org.brewchain.cwv.game.enums.TransHashTypeEnum;
import org.brewchain.cwv.game.enums.TransactionTypeEnum;
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
			exchangeBuyGroupProcess(propertyHelper.getDao());
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		
		try {
			exchangeBuyRollbackGroupProcess(propertyHelper.getDao());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		log.info("PropertyExchangeBuyTask ended ....");
	}
	
	/**
	 *  group交易失败 退款买家
	 * @param dao
	 */
	private void exchangeBuyRollbackGroupProcess(Daos dao) {
		//查询group状态失败的交易
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		buyExample.createCriteria()
		.andChainStatusGroupEqualTo(ChainTransStatusEnum.ERROR.getKey())
		.andChainTransHashGroupIsNotNull()
		.andChainStatusRollbackIsNull()
		;//hash为null的情况，已在group执行失败时处理
		final List<Object> list = dao.exchangeBuyDao.selectByExample(buyExample);
		
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
			input.setAmount(buy.getAmount().toString() );
			inputs.add(input);
			

			//amount output
			MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
			output.setAddress(buy.getBuyerAddress());//接收方地址 *
			output.setAmount(input.getAmount());
			
			outputs.add(output);
			
		}
		
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(inputs, outputs);
		
		if(ret.getRetCode() == 1) {
			//添加交易管理记录
			propertyHelper.getCommonHelper().txManageAdd(TransactionTypeEnum.EXCHANGE_BUY_AMOUNT_ROLLBACK.getKey(),ret.getTxHash());
			
			//更新购买申请
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatusRollback(ChainTransStatusEnum.START.getKey());
				buy.setChainTransHashRollback(ret.getTxHash());
			}
			dao.exchangeBuyDao.batchUpdate(list);
		}else if(ret.getRetCode() == -1 ){
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatusRollback(ChainTransStatusEnum.ERROR.getKey());
			}
			dao.exchangeBuyDao.batchUpdate(list);
		}
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
		final List<Object> list = dao.exchangeBuyDao.selectByExample(buyExample);
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
			
			BigDecimal charge = propertyHelper.chargeProcess(buy.getAmount().doubleValue());
			//amount input
			MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
			input.setAddress(accountMap.getAddress());//发起方地址 *
			input.setNonce(account.getNonce());//交易次数 *
			input.setAmount(buy.getAmount().toString() );
			input.setSymbol(PropertyJobHandle.PROPERTY_SYMBOL);
			input.setCryptoToken(buy.getPropertyToken());
			inputs.add(input);
			
			//amount output
			MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
			output.setAddress(buy.getSellerAddress());//接收方地址 *
			output.setAmount(buy.getAmount().subtract(charge).toString());
			outputs.add(output);
			

			//amount charge output
			MultiTransactionOutputImpl.Builder outputCharge = MultiTransactionOutputImpl.newBuilder();
			outputCharge.setAddress(PropertyJobHandle.SYS_INCOME_ADDRESS);//接收方地址 *
			outputCharge.setAmount(charge.toString());
			outputs.add(outputCharge);
			
			//token output
			MultiTransactionOutputImpl.Builder outputToken = MultiTransactionOutputImpl.newBuilder();
			outputToken.setAddress(buy.getBuyerAddress());//接收方地址 *
			outputToken.setCryptoToken(buy.getPropertyToken());
			outputToken.setSymbol(PropertyJobHandle.PROPERTY_SYMBOL);
			outputs.add(outputToken);
		}
		
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(inputs, outputs);
		//添加交易管理记录
		propertyHelper.getCommonHelper().txManageAdd(TransactionTypeEnum.EXCHANGE_BUY_GROUP.getKey(),ret.getTxHash());
		if(ret.getRetCode() == 1) {
			
			List<String> tokens = new ArrayList<>();
			
			List<Integer> exchangeIds = new ArrayList<>();
			//更新购买申请
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatusGroup(ChainTransStatusEnum.START.getKey());
				buy.setChainTransHashGroup(ret.getTxHash());
				tokens.add(buy.getPropertyToken());
				exchangeIds.add(buy.getExchangeId());
			}
			dao.exchangeBuyDao.batchUpdate(list);
			//更新交易
			CWVMarketExchangeExample exchangeExample = new CWVMarketExchangeExample();
			exchangeExample.createCriteria().andExchangeIdIn(exchangeIds);
			CWVMarketExchange exchange = new CWVMarketExchange();
			exchange.setChainStatus(ChainTransStatusEnum.START.getKey());
			exchange.setChainTransHash(ret.getTxHash());
			
			dao.exchangeDao.updateByExampleSelective(exchange, exchangeExample);
			
			//更新房产
			CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
			propertyExample.createCriteria().andCryptoTokenIn(tokens);
			CWVGameProperty record = new CWVGameProperty();
			record.setChainStatus(ChainTransStatusEnum.START.getKey());
			record.setChainTransHash(ret.getTxHash());
			dao.gamePropertyDao.updateByExampleSelective(record, propertyExample);
			
		}else if(ret.getRetCode() == -1 ){
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatusGroup(ChainTransStatusEnum.ERROR.getKey());
			}
			try {
				dao.exchangeBuyDao.batchUpdate(list);
				dao.exchangeBuyDao.doInTransaction(new TransactionExecutor() {
					@Override
					public Object doInTransaction() {
						exchangeBuyRollBackList(list);
						return null;
					}
				});
			} catch (Exception e) {
				//加入日志管理
				log.error(TransactionTypeEnum.EXCHANGE_BUY_GROUP.getValue()+"====>\n"+e.getStackTrace());
				
				for(Object o : list){
					CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
					propertyHelper.getCommonHelper().marketExceptionAdd(TransactionTypeEnum.EXCHANGE_BUY_GROUP, buy.getExchangeId(), String.format("执行回退买家 [%s] 金额 [%s]",buy.getBuyerAddress(),buy.getAmount()));
					
				}
			}
			
			
		}
		
	}
	

	private void exchangeBuyGroupAmountRollBack(String chainTransHashGroup) {
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
			
			try {
				Thread.currentThread().sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			
			exchangeBuyRollBack(buy);
			
		}
//		
//		CWVUserWallet userWallet = propertyHelper.getWalletHelper().getUserAccount(userId, CoinEnum.CWB);
//		userWallet.setDrawCount(userWallet.getDrawCount()+1);
//		propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(userWallet);
	}
	
	/**
	 * 发起回滚交易single
	 * @param userId
	 */
	private void exchangeBuyRollBack(CWVMarketExchangeBuy buy) {
			
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(buy.getAmount(), buy.getBuyerAddress(), PropertyJobHandle.SYS_PROPERTY_ADDR);
		if(ret.getRetCode() == 1){
			// 回滚交易字段赋值
			buy.setChainStatusRollback(ChainTransStatusEnum.START.getKey());
			buy.setChainTransHashRollback(ret.getTxHash());
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
				String status = TransactionStatusTask.getTransStatus(propertyHelper,buy.getChainTransHash(), TransHashTypeEnum.EXCHANGE_BUY.getValue());
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
			if(transStatusSet.contains(buy.getChainTransHashGroup())) {
				continue ;
			}else {
				String status = TransactionStatusTask.getTransStatus(propertyHelper, buy.getChainTransHashGroup(), TransHashTypeEnum.EXCHANGE_BUY_GROUP.getValue());
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
			if(transStatusSet.contains(buy.getChainTransHashRollback())) {
				continue ;
			}else {
				
				String status = TransactionStatusTask.getTransStatus(propertyHelper, buy.getChainTransHashRollback(), TransHashTypeEnum.EXCHANGE_BUY_ROLLBACK_GROUP.getValue());
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

	/**
	 * 退款成功回滚处理
	 * @param chainTransHashGroup
	 */
	private void exchangeBuyRollBackDone(String chainTransHashGroup) {
		//回滚申请记录
		updateTransRollBackStatus(chainTransHashGroup, ChainTransStatusEnum.DONE.getKey());
		//退款成功 处理
		updateExchangeGroup(chainTransHashGroup, ChainTransStatusEnum.DONE.getKey());
		//房产状态更新
		exchangeBuyGroupPropertyRollBack(chainTransHashGroup);
		
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
				updateExchangeGroup(chainTransHashGroup,ChainTransStatusEnum.DONE.getKey());
				
				//回滚相关买入交易
				exchangeBuyGroupPropertyRollBack(chainTransHashGroup);
				
				//回滚买家金额
				exchangeBuyGroupAmountRollBack(chainTransHashGroup);
				return null;
			}

		});
		
	}
	
	private void exchangeBuyGroupPropertyRollBack(String chainTransHashGroup) {
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andChainTransHashEqualTo(chainTransHashGroup);
		CWVGameProperty property = new CWVGameProperty();
		property.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		propertyHelper.getDao().gamePropertyDao.updateByExampleSelective(property, example);
		
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

	/**
	 * 后买房产转账交易失败回滚
	 * @param buy
	 */
	private void exchangeBuyError(CWVMarketExchangeBuy buy) {
		//设置购买申请状态
		buy.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		propertyHelper.getDao().exchangeBuyDao.updateByPrimaryKeySelective(buy);
		
		//回滚交易状态
		CWVMarketExchange exchange = new CWVMarketExchange();
		exchange.setExchangeId(buy.getExchangeId());
		exchange.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		exchange.setChainTransHash(null);
		propertyHelper.getDao().exchangeDao.updateByPrimaryKeySelective(exchange);
		
		//回滚房产状态
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		CWVGameProperty property = new CWVGameProperty();
		property.setChainStatus(ChainTransStatusEnum.DONE.getKey());
		property.setChainTransHash(null);
		propertyHelper.getDao().gamePropertyDao.updateByExampleSelective(property, example);
		
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
