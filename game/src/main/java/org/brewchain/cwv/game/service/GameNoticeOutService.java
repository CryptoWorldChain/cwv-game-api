package org.brewchain.cwv.game.service;

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
	
	private final String NOTICE_OUT_URL = "http:/localhost/api/msg/subcription";
	
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
		
		Map<String,String> jsonMap = new HashMap<>();
		jsonMap.put("user_id", "1");
		jsonMap.put("topic", pb.getNoticeType());
		jsonMap.put("pagesize", pb.getPageSize());
		jsonMap.put("pageidx", pb.getPageIndex());
		jsonMap.put("pagenum", pb.getPageNum());
		String jsonStr = JsonSerializer.formatToString(jsonMap);
		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "GET", NOTICE_OUT_URL);
	
		val yearMeasureRet = sender.send(pp,30000);
		List<Map<String,String>> jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), List.class);
		
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		
		for(Map<String,String> coun : jsonRet){
			PRetNoticeOut.Builder notice = PRetNoticeOut.newBuilder();
			notice.setNoticeContent(coun.get("content"));
			notice.setNoticeType(coun.get("topic"));
			ret.addNotices(notice);
		}
	}
	
}
