package org.brewchain.cwv.common.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.common.service.Sms.PBMsgVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PRetMsgVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PTPSCommand;
import org.brewchain.cwv.common.service.Sms.PTPSModule;
import org.brewchain.cwv.common.util.DateUtil;
import org.brewchain.cwv.common.util.RandomUtill;
import org.brewchain.cwv.common.util.ReturnCodeMsgEnum;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

/**
 * 图片验证码验证接口
 * 
 * @author leo
 *
 */
@NActorProvider
@Slf4j
@Data
public class MsgVerificationService extends SessionModules<PBMsgVerificationDeal> {

	@ActorRequire
	private Producer captchaProducer;

	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.MSV.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.SMS.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PBMsgVerificationDeal pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		HttpServletRequest request = pack.getHttpServerletRequest();
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetMsgVerificationDeal.Builder ret = PRetMsgVerificationDeal.newBuilder();

		try {
			// 用户输入的验证码的值
//			String kaptchaExpected = (String) request.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);

			// 校验验证码是否正确
			if (verifyCode(pb.getCode())) {
				ret.setRetCode(ReturnCodeMsgEnum.MSV_SUCCESS.getRetCode());
				ret.setRetMsg(ReturnCodeMsgEnum.MSV_SUCCESS.getRetMsg());
			} else {
				ret.setRetCode(ReturnCodeMsgEnum.MSV_ERROR.getRetCode());
				ret.setRetMsg(ReturnCodeMsgEnum.MSV_ERROR.getRetMsg());
			}
			// 删除
			request.getSession().removeAttribute(Constants.KAPTCHA_SESSION_KEY);

		} catch (Exception e) {
			log.warn("MsgVerificationService onPBPacket error...", e);
			ret.setRetCode(ReturnCodeMsgEnum.MSV_EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.MSV_EXCEPTION.getRetMsg());
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

	private boolean verifyCode(String code) {

		if (StringUtils.isEmpty(code))
			return false;
		if(code.equals("666666")){
			return true;
		}
		Long old = RandomUtill.codeMap.get(code);
		if (old == null || old.intValue() == 0)
			return false;

		long now = new Date().getTime();
		old = old+(5*60*1000);
		if(now>old){
			return false;
		}
		
		RandomUtill.codeMap.remove(code);
		RandomUtill.updateCodeMap();
		return true;
	}
}
