package org.brewchain.cwv.game.chain;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.game.helper.WalletHelper;
import org.brewchain.cwv.service.game.Game.PRetCommon;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
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
	private static String CONTRACT_DRAW = "contract_draw";
	
	/**
	 * 购买房产
	 * @param buyAddress
	 * @param pwd
	 * @return
	 */
	
	public PRetCommon.Builder buyProperty(String buyAddress, String propertyId){
		
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		return ret.setRetCode("01").setRetMsg("成功");
	}
	
	
	/**
	 * 卖出房产
	 * @param sellAddress
	 * @param propertyId
	 * @param price
	 * @param charge
	 * @return
	 */
	
	public PRetCommon.Builder sellProperty(String sellAddress, String propertyId, double price, double charge  ){
		
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		return ret.setRetCode("01").setRetMsg("成功");
	}


	public PRetCommon.Builder cancelExchange(String address, String exchangeId) {
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		return ret.setRetCode("01").setRetMsg("成功");
	}
	
	
}
