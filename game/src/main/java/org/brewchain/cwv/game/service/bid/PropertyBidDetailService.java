package org.brewchain.cwv.game.service.bid;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.service.game.Bid.PBidCommand;
import org.brewchain.cwv.service.game.Bid.PBidModule;
import org.brewchain.cwv.service.game.Bid.PRetBidPropertyDetail;
import org.brewchain.cwv.service.game.Bid.PSCommonBid;

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
public class PropertyBidDetailService extends SessionModules<PSCommonBid> {
	
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
		return new String[] { PBidCommand.PBD.name() };
	}

	@Override
	public String getModule() {
		return PBidModule.GBA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSCommonBid pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetBidPropertyDetail.Builder ret = PRetBidPropertyDetail.newBuilder();
		try{
			propertyHelper.bidDetail(pb, ret);
//			bidDetail(pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("GetPropertyBidService getPropertyBid  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

}
