package org.brewchain.cwv.auth.impl;

import java.util.HashMap;
import java.util.Map;

import org.brewchain.cwv.auth.util.jwt.Constant;
import org.fc.hzq.service.sys.User.PRetCommon;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.JsonSerializer;

@Slf4j
public class InokeInterfaceHelper {
	/**
	 * 校验验证码
	 * @param jsonMap
	 * @param sender
	 * @return
	 */
	public static HashMap<String,String> checkCode(HashMap<String, String> jsonMap, IPacketSender sender) {

		return send(jsonMap, sender, Constant.URL_VERIFY_CODE);
	}
	
	/**
	 * 校验短信验证码
	 * @param jsonMap
	 * @param sender
	 * @return
	 */
	public static HashMap<String,String> checkMsgCode(HashMap<String, String> jsonMap, IPacketSender sender) {
		
		return send(jsonMap, sender, Constant.URL_VERIFY_PHONE_CODE);
		
	}
	
	/**
	 * 获取短信验证码
	 * @param jsonMap
	 * @param sender
	 * @return
	 */
	public static HashMap<String,String> getMsgCode(HashMap<String, String> jsonMap, IPacketSender sender) {
		
		return send(jsonMap, sender, Constant.URL_GET_PHONE_CODE);
	}
	
	
	private static HashMap<String,String> send(Map<String,String> jsonMap, IPacketSender sender,String url){
		String jsonStr = JsonSerializer.formatToString(jsonMap);
		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", url);
		val yearMeasureRet = sender.send(pp,30000);
		HashMap<String,String> jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), HashMap.class);
		return jsonRet;
	}
	
	

}
