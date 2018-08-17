package org.brewchain.cwv.game.service.market.exception;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.MarketHelper;
import org.brewchain.cwv.service.game.Exchange.PSBuyProperty;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;
import org.brewchain.cwv.service.game.Game.RetCodeMsg;
import org.brewchain.cwv.service.game.MarketManage.PMarketECommand;
import org.brewchain.cwv.service.game.MarketManage.PMarketEModule;
import org.brewchain.cwv.service.game.MarketManage.PSMarketException;

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
public class MarketExceptionService extends SessionModules<PSMarketException> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire(name="Daos")
	Daos dao;
		
	@ActorRequire(name="Market_Helper")
	MarketHelper marketHelper;
	
	@Override
	public String[] getCmds() {
		return new String[] { PMarketECommand.MES.name() };
	}

	@Override
	public String getModule() {
		return PMarketEModule.GMA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSMarketException pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		RetCodeMsg.Builder builder = RetCodeMsg.newBuilder();
		try{
			marketHelper.marketException(pb, ret, builder);
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
