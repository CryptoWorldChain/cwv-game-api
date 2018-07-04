package org.brewchain.cwv.game.chain;

import java.math.BigDecimal;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.WltHelper;
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
@Instantiate(name="Property_Draw_Invoker")
public class PropertyDrawInvoker implements ActorService {

	private String CONTRACT_DRAW = this.commonHelper.getSysSettingValue("contract_draw");
	
	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;

	/**
	 * 抽奖房产
	 * @param buyAddress
	 * @return
	 */
	
	public RespCreateTransaction.Builder drawProperty(String drawAddress){
		
		return wltHelper.excuteContract(new BigDecimal(0), wltHelper.getWLT_DCR(), CONTRACT_DRAW);
	}
	
	
}