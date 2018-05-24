package org.brewchain.cwv.game.chain;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
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
@Instantiate(name="Property_Bid_Invoker")
public class PropertyBidInvoker implements ActorService {
	private static String CONTRACT_BID_URL = "contract_bid_url";
	/**
	 * 创建竞拍
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public static PRetCommon.Builder createBid(CWVMarketBid bid ){
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		if(bid.getGamePropertyId() == null){
			return ret.setRetCode("80").setRetMsg("房产ID不能为空");
		}
		if(bid.getIncreaseLadder() == null){
			return ret.setRetCode("80").setRetMsg("竞价必须是最小单位竞价的倍数");
		}
		if(bid.getBidStart() == null){
			return ret.setRetCode("80").setRetMsg("竞拍起价不能为空且必须是");
		}
		if(bid.getAuctionStart() == null){
			return ret.setRetCode("80").setRetMsg("竞拍起期不能为空");
		}
		if(bid.getAuctionEnd() == null){
			return ret.setRetCode("80").setRetMsg("竞拍止期不能为空");
		}
		
		return ret.setRetCode("01").setRetMsg("成功");
	}
	
	/**
	 * 竞拍房产
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public static PRetCommon.Builder auctionProperty(String auctionAddress, String propertyId, String bidAmount ){
		
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		return ret.setRetCode("01").setRetMsg("成功");
	}
	
	
}
