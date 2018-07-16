package org.brewchain.cwv.game.service.exchange;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.service.game.Draw.PDrawCommand;
import org.brewchain.cwv.service.game.Draw.PDrawModule;
import org.brewchain.cwv.service.game.Draw.PSDrawTxInfo;
import org.brewchain.cwv.service.game.Exchange.PSSellProperty;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.RetCodeMsg;

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
public class DrawTxInfoService extends SessionModules<PSDrawTxInfo> {
	
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
		return new String[] { PDrawCommand.DTI.name() };
	}

	@Override
	public String getModule() {
		return PDrawModule.GDA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSDrawTxInfo pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		RetCodeMsg.Builder builder = RetCodeMsg.newBuilder();
		
		try{
			propertyHelper.getDrawTxInfo(pb, ret, builder);
			ret.setCodeMsg(builder);
		}catch(Exception e){
			builder.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			builder.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			ret.setCodeMsg(builder);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}