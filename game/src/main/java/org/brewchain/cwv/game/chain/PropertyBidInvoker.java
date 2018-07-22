package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.bcvm.CodeBuild;
import org.brewchain.cwv.auth.enums.ContractTypeEnum;
import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.wallet.service.Wallet.RespCreateContractTransaction;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.outils.serialize.JsonSerializer;


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
public class PropertyBidInvoker extends Invoker implements ActorService {
	
	
	public PropertyBidInvoker() {
		super();
	}
	
	/**
	 * 创建竞拍
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public RespCreateContractTransaction.Builder createBid(CWVMarketBid bid, String address, String token ){
		RespCreateContractTransaction.Builder ret = RespCreateContractTransaction.newBuilder();
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
		
		CodeBuild.Result res = wltHelper.buildContract(ContractTypeEnum.AUCTION_CONTRACT.getName());
		if(res==null||StringUtils.isNotBlank(res.error)){
			ret.setRetMsg("创建合约失败，请重新操作");
			ret.setRetCode(-1);
			log.debug("合约编译失败");
			return ret;
		}
		String consData = wltHelper.methodBuild(res, null, token,bid.getAuctionStart().getTime(),
				bid.getAuctionEnd().getTime(),bid.getAuctionEnd().getTime()-bid.getAuctionStart().getTime(),bid.getBidStart(),
				bid.getIncreaseLadder());
		
		ret = wltHelper.createContract(address, new BigDecimal("0"), res.data+consData , ContractTypeEnum.AUCTION_CONTRACT.getName());
		
		return ret.setRetCode(1);
	}
	
	/**
	 * 竞拍房产
	 * @param buyAddress
	 * @param propertyId
	 * @return
	 */
	public RespCreateTransaction.Builder auctionProperty(String auctionAddress, String contractAddress, Double bidAmount ){
		
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		CodeBuild.Result res = wltHelper.buildContract(ContractTypeEnum.AUCTION_CONTRACT.getName());
		
		String data = wltHelper.methodBuild(res, "auctionBid", bidAmount.longValue());
		
		return wltHelper.excuteContract(new BigDecimal("0"), auctionAddress, contractAddress, data);
		
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
