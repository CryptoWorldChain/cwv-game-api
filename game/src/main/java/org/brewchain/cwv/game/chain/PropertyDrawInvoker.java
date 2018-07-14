package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.enums.ContractTypeEnum;
import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddress;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddressExample;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.cwv.game.helper.PropertyHelper;
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
@Instantiate(name="Property_Draw_Invoker")
public class PropertyDrawInvoker extends Invoker implements ActorService  {

	private static String CONTRACT_DRAW = "contract_draw";
	

	/**
	 * 抽奖房产
	 * @param buyAddress
	 * @return
	 */
	
	public RespCreateTransaction.Builder drawProperty(String address, int num){
		
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		try {
			String data = wltHelper.excuteContract("1", "getFixedRange",num);

			String contractAddress = this.getContractAddress(ContractTypeEnum.RANDOM_CONTRACT.getName());
			
			if(contractAddress == null) {
				ret.setRetCode(-1);
				ret.setRetMsg("随机合约地址为空，暂不支持抽奖");
				return ret;
			}
			
			ret = wltHelper.excuteContract(new BigDecimal(0), address, contractAddress,data);
			return ret;
		} catch (Exception e) {
			ret.setRetCode(-1);
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			return ret;
		}
	}
	
	
}