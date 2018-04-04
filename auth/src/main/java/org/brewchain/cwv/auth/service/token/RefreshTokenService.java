package org.brewchain.cwv.auth.service.token;

import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.impl.TokenHelper;
import org.fc.hzq.service.sys.Token.PRetRefreshToken;
import org.fc.hzq.service.sys.Token.PSRefreshToken;
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
public class RefreshTokenService extends SessionModules<PSRefreshToken> {

	// @ActorRequire
	// FctProcessHelper fctProcessHelper;
	// @ActorRequire(scope = "global")
	// SessionManager sm;

	@ActorRequire
	TokenHelper tokenHelper;

	@Override
	public String[] getCmds() {
		return new String[] { PTOKENCommand.RTS.name() };
	}

	@Override
	public String getModule() {
		return PTOKENModule.TKN.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PSRefreshToken pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetRefreshToken.Builder ret = PRetRefreshToken.newBuilder();

		try {
			tokenHelper.refreshToken(pack, pb, ret);
		} catch (Exception e) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_EXCEPTION.getRetCode()).setRetMsg(ReturnCodeMsgEnum.LIN_EXCEPTION.getRetMsg());
			log.warn("GetHeadImageService onPBPacket error...",e);
		}

		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

}
