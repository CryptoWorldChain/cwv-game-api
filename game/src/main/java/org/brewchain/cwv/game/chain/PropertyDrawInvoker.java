package org.brewchain.cwv.game.chain;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.game.chain.ret.RetDraw;

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
@Instantiate(name="Property_Draw_Invoker")
public class PropertyDrawInvoker implements ActorService {
	
	private static String CONTRACT_DRAW_URL = "contract_draw_url";
	
	/**
	 * 抽奖房产
	 * @param buyAddress
	 * @return
	 */
	
	public static RetDraw drawProperty(String drawAddress){
		
		RetDraw ret = new RetDraw();
		ret.setRetCode("01");
		ret.setRetMsg("成功");
		ret.setPropertyId("70");
		return ret;
	}
	
	
}