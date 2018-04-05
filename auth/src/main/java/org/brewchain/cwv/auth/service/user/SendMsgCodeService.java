package org.brewchain.cwv.auth.service.user;

import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.fc.hzq.service.sys.User.PRetCommon;
import org.fc.hzq.service.sys.User.PSRegistry;
import org.fc.hzq.service.sys.User.PSSendMsgCode;
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
public class SendMsgCodeService extends SessionModules<PSSendMsgCode> {

	// @ActorRequire
	// FctProcessHelper fctProcessHelper;
	// @ActorRequire(scope = "global")
	// SessionManager sm;

	@ActorRequire
	UserHelper userHelper;

	@Override
	public String[] getCmds() {
		return new String[] { PUSERCommand.SMC.name() };
	}

	@Override
	public String getModule() {
		return PUSERModule.USR.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PSSendMsgCode pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetCommon.Builder ret = PRetCommon.newBuilder();

		try {
			userHelper.sendMsgCode(pack, pb, ret);
			
		} catch (IllegalArgumentException e) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("" + e.getMessage());
		} catch (Exception e) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_EXCEPTION.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.LIN_EXCEPTION.getRetMsg());
			log.warn("SendMsgCodeService sendMsgCode error...",e);
		}

		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
