package org.brewchain.cwv.game.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample.Criteria;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PBGameCountry;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry;
import org.brewchain.cwv.service.game.Game.PTPSCommand;
import org.brewchain.cwv.service.game.Game.PTPSModule;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSCommand;
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSModule;
import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeOut;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut.PRetNoticeOut;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.JsonSerializer;

@NActorProvider
@Slf4j
@Data
public class GameNoticeOutService extends SessionModules<PBGameNoticeOut> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire
	Daos daos;
	
	@ActorRequire(name = "http", scope = "global")
	IPacketSender sender;
	
	private final String NOTICE_OUT_URL = "http://54.169.102.90:80/api/msg/subcription";
	
	public String[] getCmds() {
		return new String[] { GNPSCommand.GNO.name() };
	}

	@Override
	public String getModule() {
		return GNPSModule.GNA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameNoticeOut pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetGameNoticeOut.Builder ret = PRetGameNoticeOut.newBuilder();
		try{
			baffle(pb, ret);
		}catch(Exception e){
			ret.setRetCode("99");
			ret.setRetMsg(e.getMessage());
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	/**
	 * 挡板
	 */
	private void baffle(PBGameNoticeOut pb,PRetGameNoticeOut.Builder ret){
		
//		Map<String,String> jsonMap = new HashMap<>();
//		jsonMap.put("userid", pb.getUserId());
//		jsonMap.put("topic", pb.getNoticeType());
//		jsonMap.put("pagesize", pb.getPageSize());
//		jsonMap.put("pageidx", pb.getPageIndex());
//		jsonMap.put("pagenum", pb.getPageNum());
//		String jsonStr = JsonSerializer.formatToString(jsonMap);
		StringBuffer url = new StringBuffer();
		url.append(NOTICE_OUT_URL);
		url.append("?");
		url.append("userid="+pb.getUserId());
		url.append("&");
		url.append("topic="+pb.getNoticeType());
		url.append("&");
		url.append("pagesize="+pb.getPageSize());
		url.append("&");
		url.append("pageidx="+pb.getPageIndex());
		url.append("&");
		url.append("pagenum="+pb.getPageNum());
		
		FramePacket pp = PacketHelper.buildUrlForGet(url.toString());
		val yearMeasureRet = sender.send(pp,30000);
		Map<String, Object> jsonRet;
		try {
			jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody(),"utf-8"), Map.class);
			if(jsonRet.get("errcode").equals("000")){
				ret.setRetCode("01");
				ret.setRetMsg("SUCCESS");
				
				List<String> chunk =  (List<String>) jsonRet.get("data");
				
				for(String jsonChunk : chunk){
					Map<String,String> coun = JsonSerializer.getInstance().deserialize(jsonChunk, Map.class);
					PRetNoticeOut.Builder notice = PRetNoticeOut.newBuilder();
					notice.setNoticeContent(coun.get("content"));
					notice.setNoticeType(coun.get("topic"));
					ret.addNotices(notice);
				}
			}else{
				ret.setRetCode("99");
				ret.setRetMsg("FAILD");
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	
}
