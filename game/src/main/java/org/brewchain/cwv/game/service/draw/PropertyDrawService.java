package org.brewchain.cwv.game.service.draw;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.service.game.Draw.PDrawCommand;
import org.brewchain.cwv.service.game.Draw.PDrawModule;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDraw;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord;
import org.brewchain.cwv.service.game.Draw.PSCommonDraw;
import org.brewchain.cwv.service.game.Draw.PSPropertyDrawRecord;
import org.brewchain.cwv.service.game.Game.PRetCommon;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

@NActorProvider
@Slf4j
@Data
public class PropertyDrawService extends SessionModules<PSCommonDraw> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire(name="Daos")
	Daos dao;
		
	@ActorRequire(name="Property_Helper")
	PropertyHelper propertyHelper;
	
	@Override
	public String[] getCmds() {
		return new String[] { PDrawCommand.PDS.name() };
	}

	@Override
	public String getModule() {
		return PDrawModule.GDA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSCommonDraw pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetPropertyDraw.Builder ret = PRetPropertyDraw.newBuilder();
		try{
			propertyHelper.drawProperty(pack, pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("GetPropertyDrawRecordService getPropertyDrawRecord  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
