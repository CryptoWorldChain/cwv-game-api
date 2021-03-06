package org.brewchain.cwv.game.service;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PSCommon;
import org.brewchain.cwv.service.game.Game.PSPropertyGame;
import org.brewchain.cwv.service.game.Game.PTPSCommand;
import org.brewchain.cwv.service.game.Game.PTPSModule;
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
public class PropertyGameTypeListService extends SessionModules<PSCommon> {
	
	@ActorRequire(name="Property_Helper")
	PropertyHelper propertyHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire(name="Daos")
	Daos dao;
	
	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.PGT.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.GGA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSCommon pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		RetCodeMsg.Builder builder = RetCodeMsg.newBuilder();
		
		try{
			propertyHelper.getPropertyGameTypeList(pb, ret, builder);
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
