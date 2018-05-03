package org.brewchain.cwv.game.chain;

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
 * 房产竞拍
 * @author Moon
 * @date 2018-04-23
 */
@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
public class PropertyBidInvoker implements ActorService {
	
	/**
	 * 竞拍房产
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public static PRetCommon.Builder auctionProperty(String auctionAddress, String propertyId, String bid_amount ){
		
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		return ret.setRetCode("01").setRetMsg("成功");
	}
	
	
}
