package org.brewchain.cwv.game.service.exchange;

import java.util.HashMap;
import java.util.Map;

import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.service.game.Exchange.PExchangeCommand;
import org.brewchain.cwv.service.game.Exchange.PExchangeModule;
import org.brewchain.test.service.Test.MultiTransactionImpl;
import org.brewchain.test.service.Test.PWLTTestCommand;
import org.brewchain.test.service.Test.PWLTTestModule;
import org.brewchain.test.service.Test.ReqGetAccountInfo;
import org.brewchain.test.service.Test.ReqGetTxInfo;
import org.brewchain.test.service.Test.RetGetTxInfo;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;
import org.brewchain.wallet.service.Wallet.RetNewAddress;

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
public class GetTxInfoService extends SessionModules<ReqGetTxInfo> {
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
	@Override
	public String[] getCmds() {
		return new String[] { PExchangeCommand.QTS.name() };
	}

	@Override
	public String getModule() {
		return PExchangeModule.GEA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final ReqGetTxInfo pb, final CompleteHandler handler) {
		
		RespGetTxByHash.Builder ret = wltHelper.getTxInfo(pb.getTxHash());
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	
}
