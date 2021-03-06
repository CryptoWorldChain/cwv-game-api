package org.brewchain.cwv.auth.service.user;

import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.fc.hzq.service.sys.User.PRetCommon;
import org.fc.hzq.service.sys.User.PUSERCommand;
import org.fc.hzq.service.sys.User.PUSERModule;
import org.fc.hzq.service.sys.User.UserInfo;

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
public class SetNickService extends SessionModules<UserInfo> {

	// @ActorRequire
	// FctProcessHelper fctProcessHelper;
	// @ActorRequire(scope = "global")
	// SessionManager sm;

	@ActorRequire(name="User_Helper")
	UserHelper userHelper;

	@Override
	public String[] getCmds() {
		return new String[] { PUSERCommand.SNN.name() };
	}

	@Override
	public String getModule() {
		return PUSERModule.USR.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final UserInfo pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetCommon.Builder ret = PRetCommon.newBuilder();

		try {
			userHelper.setNickName(pack, pb, ret);
			
		} catch (Exception e) {
			ret.setRetCode(ReturnCodeMsgEnum.SNN_EXCEPTION.getRetCode()).setRetMsg(ReturnCodeMsgEnum.LIN_EXCEPTION.getRetMsg());
			log.warn("SetNickService onPBPacket error...",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
