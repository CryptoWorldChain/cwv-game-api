package org.brewchain.cwv.game.service.exchange;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.service.game.Exchange.PExchangeCommand;
import org.brewchain.cwv.service.game.Exchange.PExchangeModule;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSBuyProperty;
import org.brewchain.cwv.service.game.Exchange.PSCommonExchange;
import org.brewchain.cwv.service.game.Exchange.PSPropertyExchange;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;

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
public class CancelPropertyExchangeService extends SessionModules<PSCommonExchange> {
	
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
		return new String[] { PExchangeCommand.CPE.name() };
	}

	@Override
	public String getModule() {
		return PExchangeModule.GEA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSCommonExchange pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetCommon.Builder ret = PRetCommon.newBuilder();
		try{
			propertyHelper.cancelExchange(pack, pb, ret);
//			buyProperty(pack,pb,ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("BuyPropertyService buyProperty  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

	private void buyProperty(FramePacket pack, PSBuyProperty pb, Builder ret) {
		ret.setRetCode(ReturnCodeMsgEnum.BPS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.BPS_SUCCESS.getRetMsg());
	}
}
