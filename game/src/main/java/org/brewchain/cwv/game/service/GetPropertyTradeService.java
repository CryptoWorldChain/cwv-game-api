package org.brewchain.cwv.game.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.service.game.Property.PGPACommand;
import org.brewchain.cwv.service.game.Property.PGPAModule;
import org.brewchain.cwv.service.game.Property.PRetGetPropertyTrade;
import org.brewchain.cwv.service.game.Property.PSCommonTrade;
import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeIn;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeIn;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeIn.Builder;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.JsonSerializer;

@NActorProvider
@Slf4j
@Data
public class GetPropertyTradeService extends SessionModules<PSCommonTrade> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire
	Daos daos;
		
	@ActorRequire
	PropertyHelper propertyHelper;
	
	@Override
	public String[] getCmds() {
		return new String[] { PGPACommand.GPT.name() };
	}

	@Override
	public String getModule() {
		return PGPAModule.GPA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSCommonTrade pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetGetPropertyTrade.Builder ret = PRetGetPropertyTrade.newBuilder();
		try{
			propertyHelper.getPropertyTrade(pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("GetPropertyTradeService noticeIn  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
