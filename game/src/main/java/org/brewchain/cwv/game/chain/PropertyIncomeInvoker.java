package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;
import java.util.List;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.service.game.Game.PRetCommon;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;


/**
 * 房产手
 * 
 * @author Moon
 * @date 2018-04-23
 */
@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
@Instantiate(name="Property_Income_Invoker")
public class PropertyIncomeInvoker implements ActorService {
	
	/**
	 * 创建竞拍
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public static List<Object> getUserPropertiesForIncome(CWVMarketBid bid ){
		
		
		return null;
	}
	
	/**
	 *  领取收益
	 * @param address 领取账户
	 * @param type 类型 1普通房产 2标志性房产 3功能性房产
	 * @param amount 收益金额
	 * @return
	 */
	public static PRetCommon.Builder claimIncome(String address, Byte type, BigDecimal amount ){
		
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		return ret.setRetCode("01").setRetMsg("成功");
	}
	
	
}
