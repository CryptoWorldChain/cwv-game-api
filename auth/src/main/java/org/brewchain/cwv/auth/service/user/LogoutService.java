package org.brewchain.cwv.auth.service.user;

import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.fc.hzq.service.sys.User.PRetCommon;
import org.fc.hzq.service.sys.User.PRetLogin;
import org.fc.hzq.service.sys.User.PSLogin;
import org.fc.hzq.service.sys.User.PSRegistry;
import org.fc.hzq.service.sys.User.PUSERCommand;
import org.fc.hzq.service.sys.User.PUSERModule;

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
public class LogoutService extends SessionModules<PSLogin> {

	// @ActorRequire
	// FctProcessHelper fctProcessHelper;
	// @ActorRequire(scope = "global")
	// SessionManager sm;

	@ActorRequire
	UserHelper userHelper;

	@Override
	public String[] getCmds() {
		return new String[] { PUSERCommand.LOU.name() };
	}

	@Override
	public String getModule() {
		return PUSERModule.USR.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PSLogin pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetLogin.Builder ret = PRetLogin.newBuilder();

		try {
			userHelper.logout(pack, pb, ret);
		} catch (Exception e) {
			ret.setRetCode(ReturnCodeMsgEnum.LOU_EXCEPTION.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.LOU_EXCEPTION.getRetMsg());
			e.printStackTrace();
		}

		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

}
