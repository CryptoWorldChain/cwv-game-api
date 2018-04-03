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
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSCommand;
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSModule;
import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeIn;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeIn;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut.PRetNoticeOut;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;

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
public class GameNoticeInService extends SessionModules<PBGameNoticeIn> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire
	Daos daos;
	
	@ActorRequire(name = "http", scope = "global")
	IPacketSender sender;
	
	private final String NOTICE_IN_URL = "http:/localhost/api/msg/publication";
	
	
	@Override
	public String[] getCmds() {
		return new String[] { GNPSCommand.GNI.name() };
	}

	@Override
	public String getModule() {
		return GNPSModule.GNA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameNoticeIn pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetGameNoticeIn.Builder ret = PRetGameNoticeIn.newBuilder();
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
	private void baffle(PBGameNoticeIn pb,PRetGameNoticeIn.Builder ret){
		Map<String,String> jsonMap = new HashMap<>();
		jsonMap.put("user_id", "1");
		jsonMap.put("topic", pb.getNoticeType());
		jsonMap.put("content", pb.getNoticeContent());
		String jsonStr = JsonSerializer.formatToString(jsonMap);
		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", NOTICE_IN_URL);
	
		val yearMeasureRet = sender.send(pp,30000);
		Object jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), List.class);
		
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
	}
	
}
