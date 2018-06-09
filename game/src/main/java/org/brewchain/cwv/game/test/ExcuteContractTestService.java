package org.brewchain.cwv.game.test;

import java.math.BigDecimal;
import java.util.Map;

import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.test.service.Test.PWLTTestCommand;
import org.brewchain.test.service.Test.PWLTTestModule;
import org.brewchain.test.service.Test.ReqCreateTx;
import org.brewchain.test.service.Test.ReqExcuteContract;
import org.brewchain.test.service.Test.ReqGetAccountInfo;
import org.brewchain.test.service.Test.RetCreateTx;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
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
public class ExcuteContractTestService extends SessionModules<ReqExcuteContract> {
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
	@Override
	public String[] getCmds() {
		return new String[] { PWLTTestCommand.DCR.name() };
	}

	@Override
	public String getModule() {
		return PWLTTestModule.TTS.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final ReqExcuteContract pb, final CompleteHandler handler) {
		RespCreateTransaction.Builder ret = wltHelper.excuteContract(new BigDecimal(pb.getAmount()), pb.getAddress(), pb.getContractAddress());
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	
}
