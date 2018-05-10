package org.brewchain.cwv.game.service.user;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.WalletHelper;
import org.brewchain.cwv.service.game.User.PRetAccountTopup;
import org.brewchain.cwv.service.game.User.PRetWalletAccountBalance;
import org.brewchain.cwv.service.game.User.PSAccountTopup;
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
public class AccountTopupService extends SessionModules<PSAccountTopup> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire
	Daos daos;
		
	@ActorRequire
	WalletHelper walletHelper;
	
	@Override
	public String[] getCmds() {
		return new String[] { PUserCommand.ATS.name() };
	}

	@Override
	public String getModule() {
		return PUserModule.GUA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSAccountTopup pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetAccountTopup.Builder ret = PRetAccountTopup.newBuilder();
		try{
			walletHelper.accountTopup(pack, pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("WalletAccountBalanceService waletAccount  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
