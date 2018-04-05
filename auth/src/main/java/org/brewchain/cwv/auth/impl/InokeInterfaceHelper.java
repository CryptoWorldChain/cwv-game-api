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

	public static String checkMsgCode(String phone, String code, String type, IPacketSender sender) {
		Map<String,String> jsonMap = new HashMap<>();
		jsonMap.put("phone", phone);
		jsonMap.put("code", code);
		jsonMap.put("type", type);
		String url = "http://localhost:8000/cwv/sms/pbver.do";
		Map<String, Object> jsonRet = send(sender,url,jsonMap);
		String retCode =jsonRet.get("ret_code") == null? ReturnCodeMsgEnum.MSV_ERROR.getRetCode() : jsonRet.get("ret_code").toString();
		return retCode;
	}
	
	private static Map<String, Object> send(IPacketSender sender,String url,Map<String,String> jsonMap){
		
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
