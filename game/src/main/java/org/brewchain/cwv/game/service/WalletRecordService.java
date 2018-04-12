package org.brewchain.cwv.game.service;

import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.WalletHelper;
import org.brewchain.cwv.service.game.Wallet.PRetWalletAccount;
import org.brewchain.cwv.service.game.Wallet.PRetWalletRecord;
import org.brewchain.cwv.service.game.Wallet.PSWalletRecord;
import org.brewchain.cwv.service.game.Wallet.PWalletCommand;
import org.brewchain.cwv.service.game.Wallet.PWalletModule;

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
public class WalletRecordService extends SessionModules<PSWalletRecord> {
	
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
		return new String[] { PWalletCommand.WAS.name() };
	}

	@Override
	public String getModule() {
		return PWalletModule.GWA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PSWalletRecord pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetWalletRecord.Builder ret = PRetWalletRecord.newBuilder();
		try{
			walletHelper.walletRecord(pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("WalletAccountService waletAccount  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
