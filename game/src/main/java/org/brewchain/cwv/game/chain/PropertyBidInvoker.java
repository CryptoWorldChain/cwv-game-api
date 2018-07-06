package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;


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
	
	private String CONTRACT_BID = this.commonHelper.getSysSettingValue("contract_bid");
	
	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
	public PropertyBidInvoker() {
		super();
		CONTRACT_BID = commonHelper.getSysSettingValue("contract_bid");
	}
	
	/**
	 * 创建竞拍
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public RespCreateTransaction.Builder createBid(CWVMarketBid bid ){
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		if(bid.getGamePropertyId() == null){
			return ret.setRetCode(80).setRetMsg("房产ID不能为空");
		}
		if(bid.getIncreaseLadder() == null){
			return ret.setRetCode(80).setRetMsg("竞价必须是最小单位竞价的倍数");
		}
		if(bid.getBidStart() == null){
			return ret.setRetCode(80).setRetMsg("竞拍起价不能为空且必须是");
		}
		if(bid.getAuctionStart() == null){
			return ret.setRetCode(80).setRetMsg("竞拍起期不能为空");
		}
		if(bid.getAuctionEnd() == null){
			return ret.setRetCode(80).setRetMsg("竞拍止期不能为空");
		}
		
		
		return ret.setRetCode(1);
	}
	
	/**
	 * 竞拍房产
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public RespCreateTransaction.Builder auctionProperty(String auctionAddress, String propertyId, String bidAmount ){
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		return ret.setRetCode(1);
		
	}
	
	
	/**
	 * 取消竞拍
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public RespCreateTransaction.Builder cancelBid(CWVMarketBid bid){
		
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		return ret.setRetCode(1);
	}
	
}
