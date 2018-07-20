package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.cwv.game.job.PropertyJobHandle;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
/**
 * 房产交易
 * @author Moon
 * @date 2018-04-23
 */
@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
@Instantiate(name="Property_Exchange_Invoker")
public class PropertyExchangeInvoker implements ActorService {
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;
	
	/**
	 *申请记录是否存在
	 * 	存在：返回操作失败
	 * 	不存在 ：
	 * 		生成申请记录（交易ID（主键），买入地址，卖出地址，金额）
	 * 		发起转账交易
	 * 			交易成功：返回买入申请成功扣除买入账户余额
	 * 			交易失败：回滚账户扣除金额 
	 */
	
	/**
	 * 购买房产
	 * 申请记录是否存在
	 * 	存在：返回操作失败
	 * 	不存在 ：
	 * 		生成申请记录（交易ID（主键），买入地址，卖出地址，金额）
	 * 		发起转账交易
	 * 			交易成功：返回买入申请成功扣除买入账户余额
	 * 			交易失败：回滚账户扣除金额
	 * @param buyAddress
	 * @param pwd
	 * @return
	 */
	public RespCreateTransaction.Builder buyProperty(CWVMarketExchange exchange){
		
		
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		return ret.setRetCode(1);
	}
	
	
	/**
	 * 卖出房产
	 * @param sellAddress
	 * @param propertyId
	 * @param price
	 * @param charge
	 * @return
	 */
	
	public RespCreateTransaction.Builder sellProperty(String sellAddress, String cryptoToken, double price){
		return wltHelper.createTx(new BigDecimal(price), PropertyJobHandle.MARKET_EXCHANGE_AGENT, sellAddress, "house", cryptoToken);
	}


	public RespCreateTransaction.Builder cancelExchange(String address, String exchangeId) {
		
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		return ret.setRetCode(1);
	}
	
	
}
