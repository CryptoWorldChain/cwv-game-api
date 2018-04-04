package org.brewchain.cwv.auth.impl;

import java.util.HashMap;
import java.util.Map;

import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.fc.hzq.service.sys.User.PRetCommon;

import com.google.protobuf.InvalidProtocolBufferException;

import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FixHeader;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.JsonSerializer;

@Slf4j
public class CheckCode {

	public static String checkCode(String code, IPacketSender sender) {

		Map<String,String> jsonMap = new HashMap<>();
		jsonMap.put("code", code);
		String jsonStr = JsonSerializer.formatToString(jsonMap);
		String url = "http://localhost:8000/cwv/sms/pbmsv.do";
		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", url);
		Map<String,Object> jsonRet = send(sender, url, jsonMap);
		String retCode =jsonRet.get("ret_code") == null? ReturnCodeMsgEnum.MSV_ERROR.getRetCode() : jsonRet.get("ret_code").toString();
		return retCode;
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
		String jsonStr = JsonSerializer.formatToString(jsonMap);
		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", url);
		val yearMeasureRet = sender.send(pp,30000);
		return JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), Map.class);
	}
	
	

}
