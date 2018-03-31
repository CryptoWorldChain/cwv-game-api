package org.brewchain.cwv.game.service;

import org.brewchain.cwv.service.game.Game.PBGameCountry;
import org.brewchain.cwv.service.game.Game.PBGameMap;
import org.brewchain.cwv.service.game.Game.PBGameProperty;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry;
import org.brewchain.cwv.service.game.Game.PTPSCommand;
import org.brewchain.cwv.service.game.Game.PTPSModule;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;
import org.brewchain.cwv.service.game.Game.PRetRefGameMap;
import org.brewchain.cwv.service.game.Game.PRetRefGameMap.PRetMap;
import org.brewchain.cwv.service.game.Game.PRetRefGameProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

@NActorProvider
@Slf4j
@Data
public class GameMapPropertyService extends SessionModules<PBGameProperty> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
//	@ActorRequire
//	Daos sysDaos;
	
	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.GMP.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.GGA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameProperty pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetRefGameProperty.Builder ret = PRetRefGameProperty.newBuilder();
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
	private void baffle(PBGameProperty pb,PRetRefGameProperty.Builder ret){
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		
		
	}
	
	
}
