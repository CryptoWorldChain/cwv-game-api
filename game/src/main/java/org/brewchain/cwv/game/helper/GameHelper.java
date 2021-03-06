package org.brewchain.cwv.game.helper;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.service.game.Game.PRetGameIntroduction;
import org.brewchain.cwv.service.game.Game.PRetGameIntroduction.Introduction;
import org.brewchain.cwv.service.game.Game.PSCommon;

import freemarker.core.ReturnInstruction.Return;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
/**
 * 用户service
 * 
 * @author Moon
 * @date 2018-03-30
 */
@Instantiate(name="Game_Helper")
public class GameHelper implements ActorService {

	@ActorRequire(name="Daos")
	Daos dao;
	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;

	public void gameIntroduction(PSCommon pb, PRetGameIntroduction.Builder ret) {
		// TODO Auto-generated method stub
		Introduction.Builder intro1 = Introduction.newBuilder();
		intro1.setTitle(commonHelper.getSysSettingValue("game_intro_1"));
		intro1.setContent(commonHelper.getSysSettingValue("game_intro_1"));
		Introduction.Builder intro2 = Introduction.newBuilder();
		intro2.setTitle(commonHelper.getSysSettingValue("game_intro_2"));
		intro2.setContent(commonHelper.getSysSettingValue("game_intro_2"));
		Introduction.Builder intro3 = Introduction.newBuilder();
		intro3.setTitle(commonHelper.getSysSettingValue("game_intro_3"));
		intro3.setContent(commonHelper.getSysSettingValue("game_intro_3"));
		
		ret.addIntrodcution(intro1);
		ret.addIntrodcution(intro2);
		ret.addIntrodcution(intro3);
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
	}


}
