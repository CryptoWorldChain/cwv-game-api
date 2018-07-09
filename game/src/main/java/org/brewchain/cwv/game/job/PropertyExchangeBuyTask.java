package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuy;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuyExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.TransHashTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.wallet.service.Wallet.AccountValueImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionInputImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionOutputImpl;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetAccount;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

import lombok.extern.slf4j.Slf4j;

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
		
		//转账状态处理位于TransactionStatusTask
		exchangeBuyGroupProcess(propertyHelper.getDao());
		
		//买入交易状态处理
		buyTransStatus();
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
		.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey());
		List<Object> list = dao.exchangeBuyDao.selectByExample(buyExample);
		String outputAddress = "";
		//整合分组交易
		//发起方详情
		
		
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
		MultiTransactionInputImpl.Builder inputAmount = MultiTransactionInputImpl.newBuilder();
		inputAmount.setAmount("0");//交易金额 *
		inputAmount.setAddress(PropertyJobHandle.SYS_PROPERTY_ADDR);//发起方地址 *
		inputAmount.setNonce(account.getNonce());//交易次数 *
		
		for(Object o : list) {
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			String token = buy.getPropertyToken();
		
			//发起方详情
			MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
			input.setAddress(PropertyJobHandle.SYS_PROPERTY_ADDR);//发起方地址 *
			input.setNonce(account.getNonce());//交易次数 *
			input.setCryptoToken(buy.getPropertyToken());
			input.setSymbol("house");
			inputs.add(input);
			//接收方详情
			MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
			output.setAddress(outputAddress);//接收方地址 *
			output.setCryptoToken(buy.getPropertyToken());
			output.setSymbol("house");
			outputs.add(output);
			
			inputAmount.setAmount(new BigDecimal(inputAmount.getAmount())
					.add(buy.getAmount().multiply(new BigDecimal(PropertyJobHandle.EXCHANGE_RATE))).toString());
		}
		inputs.add(inputAmount);
		
		
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(inputs, outputs);
		if(ret.getRetCode() == 1) {
			
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatus(ChainTransStatusEnum.START.getKey());
				buy.setChainTransHash(ret.getTxHash());
			}
			dao.exchangeBuyDao.batchUpdate(list);
		}else if(ret.getRetCode() == -1 ){
			for(Object o : list){
				CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
				buy.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
			}
			dao.exchangeBuyDao.batchUpdate(list);
			exchangeBuyRollBack(list);
		}
		
	}
	
	/**
	 * 回滚买家金额 发起回滚交易
	 * @param userId
	 */
	private void exchangeBuyRollBack(List<Object> list) {
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
			log.error(String.format(" buy error :: rollback error:: [inputaddress: %s ,inputaddress: %s , amount: %s ]",PropertyJobHandle.SYS_PROPERTY_ADDR,buy.getBuyerAddress(),buy.getAmount()));
		}
			
	}
	
	
	/**
	 * 定时任务处理买入房产交易
	 */
	private void buyTransStatus() {
		
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		buyExample.createCriteria()
		.andChainStatusEqualTo(ChainTransStatusEnum.START.getKey())
		.andChainStatusGroupEqualTo(ChainTransStatusEnum.START.getKey())
//		.andchain
		;
		List<Object> list = propertyHelper.getDao().exchangeBuyDao.selectByExample(buyExample);
		//存储交易HASH
		HashSet transStatusSet = new HashSet<String>();
		for(Object o : list) {
			
			CWVMarketExchangeBuy buy = (CWVMarketExchangeBuy) o;
			if(transStatusSet.contains(buy.getChainTransHash())) {
				continue ;
			}else {
				//买入操作一
				if(buy.getChainStatus().equals(ChainTransStatusEnum.START.getKey())){
					
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
				//买入操作二group	
				}else if(buy.getChainStatusGroup().equals(ChainTransStatusEnum.START.getKey())){
					HashMap busiMap = new HashMap<String,String>();
					busiMap.put("txHash", buy.getChainTransHashGroup());
					
					String status = TransactionStatusTask.getTransStatus(propertyHelper, buy.getChainTransHashGroup(), TransHashTypeEnum.EXCHANGE_BUY_GROUP.getValue(), busiMap);
					if(StringUtils.isEmpty(status)) 
						continue;
					
					if(status.equals(ChainTransStatusEnum.DONE.getValue()))
						exchangeBuyGroupDone(buy.getChainTransHashGroup());
					else if(status.equals(ChainTransStatusEnum.ERROR.getValue()))
						exchangeBuyGroupError(buy.getChainTransHashGroup());
					
				}
				
				
				transStatusSet.add(buy.getChainTransHash());
			}
			
		}
		
		
		
		
	}
	/**
	 * group交易状态失败
	 * @param chainTransHashGroup
	 */
	private void exchangeBuyGroupError(String chainTransHashGroup) {
		//更新交易状态
		updateTransStatus(chainTransHashGroup,ChainTransStatusEnum.ERROR.getKey());
		//回滚相关买入交易
		CWVMarketExchangeBuyExample example = new CWVMarketExchangeBuyExample();
		example.createCriteria().andChainTransHashGroupEqualTo(chainTransHashGroup);
		List<Object> list = propertyHelper.getDao().exchangeBuyDao.selectByExample(example);
		exchangeBuyRollBack(list);
	}

	/**
	 * group交易状态成功
	 * @param chainTransHashGroup
	 */
	private void exchangeBuyGroupDone(String chainTransHashGroup) {
		updateTransStatus(chainTransHashGroup,ChainTransStatusEnum.DONE.getKey());
	}
	/**
	 * 更新交易状态
	 * @param hash
	 * @param status
	 */
	private void updateTransStatus(String hash, byte status) {
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		buyExample.createCriteria().andChainTransHashGroupEqualTo(hash);
		
		CWVMarketExchangeBuy buy = new CWVMarketExchangeBuy();
		buy.setChainStatusGroup(status);
		propertyHelper.getDao().exchangeBuyDao.updateByExampleSelective(buy, buyExample);
		
	}

	private void exchangeBuyError(CWVMarketExchangeBuy buy) {
		buy.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
		propertyHelper.getDao().exchangeBuyDao.updateByPrimaryKeySelective(buy);
		
		exchangeBuyRollBack(buy);
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
