package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.game.helper.CommonHelper;
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
	private String CONTRACT_EXCHANGE = this.commonHelper.getSysSettingValue("contract_exchange");
	
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;
	
	
	/**
	 * 购买房产
	 * @param buyAddress
	 * @param pwd
	 * @return
	 */
	public RespCreateTransaction.Builder buyProperty(CWVMarketExchange exchange){
		
		return wltHelper.excuteContract(new BigDecimal(0), wltHelper.getWLT_DCR(), CONTRACT_EXCHANGE);
	}
	
	
	/**
	 * 卖出房产
	 * @param sellAddress
	 * @param propertyId
	 * @param price
	 * @param charge
	 * @return
	 */
	
	public RespCreateTransaction.Builder sellProperty(String sellAddress, String propertyId, double price, double charge  ){
		
		return wltHelper.excuteContract(new BigDecimal(0), wltHelper.getWLT_DCR(), CONTRACT_EXCHANGE);
	}


	public RespCreateTransaction.Builder cancelExchange(String address, String exchangeId) {
		
		return wltHelper.excuteContract(new BigDecimal(0), wltHelper.getWLT_DCR(), CONTRACT_EXCHANGE);
	}
	
	
}
