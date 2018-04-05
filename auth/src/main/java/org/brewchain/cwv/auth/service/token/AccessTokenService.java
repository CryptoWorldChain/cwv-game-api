package org.brewchain.cwv.auth.service.token;

import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.impl.TokenHelper;
import org.fc.hzq.service.sys.Token.PRetRefreshToken;
import org.fc.hzq.service.sys.Token.PSAccessToken;
import org.fc.hzq.service.sys.Token.PTOKENCommand;
import org.fc.hzq.service.sys.Token.PTOKENModule;

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
public class AccessTokenService extends SessionModules<PSAccessToken> {

	// @ActorRequire
	// FctProcessHelper fctProcessHelper;
	// @ActorRequire(scope = "global")
	// SessionManager sm;

	@ActorRequire
	TokenHelper tokenHelper;

	@Override
	public String[] getCmds() {
		return new String[] { PTOKENCommand.ATS.name() };
	}

	@Override
	public String getModule() {
		return PTOKENModule.TKN.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PSAccessToken pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetRefreshToken.Builder ret = PRetRefreshToken.newBuilder();

		try {
			tokenHelper.accessToken(pack, pb, ret);
		} catch (Exception e) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_EXCEPTION.getRetCode()).setRetMsg(ReturnCodeMsgEnum.LIN_EXCEPTION.getRetMsg());
			log.warn("AccessTokenService onPBPacket error...",e);
		}

		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

}
