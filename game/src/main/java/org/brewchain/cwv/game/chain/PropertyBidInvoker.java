package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.enums.ContractTypeEnum;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.cwv.game.job.PropertyJobHandle;
import org.brewchain.wallet.service.Wallet.RespCreateContractTransaction;
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
	public RespCreateContractTransaction.Builder createBid(String address, String cryptoToken ){
		
		RespCreateContractTransaction.Builder ret = wltHelper.createContract(address, new BigDecimal("0"), cryptoToken , ContractTypeEnum.AUCTION_CONTRACT.getName());
		
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
