package org.brewchain.cwv.game.service.user;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome;
import org.brewchain.cwv.service.game.User.PSPropertyIncome;
import org.brewchain.cwv.service.game.User.PUserCommand;
import org.brewchain.cwv.service.game.User.PUserModule;

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
public class PropertyIncomeService extends SessionModules<PSPropertyIncome> {
	
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
		return new String[] { PUserCommand.PIS.name() };
	}

	@Override
	public String getModule() {
		return PUserModule.GUA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSPropertyIncome pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetPropertyIncome.Builder ret = PRetPropertyIncome.newBuilder();
		try{
			propertyHelper.propertyIncome(pack, pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("WalletAccountService waletAccount  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
