package org.brewchain.cwv.common.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.common.dao.Daos;
import org.brewchain.cwv.common.service.Sms.PBVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PRetVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PTPSCommand;
import org.brewchain.cwv.common.service.Sms.PTPSModule;
import org.brewchain.cwv.common.util.DateUtil;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonSmsVerify;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonSmsVerifyExample;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

/**
 * 短信验证码验证接口
 * 
 * @author leo
 *
 */
@NActorProvider
@Slf4j
@Data
public class SmsVerificationService extends SessionModules<PBVerificationDeal> {

	@ActorRequire
	Daos sysDaos;

	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.VER.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.SMS.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PBVerificationDeal pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		HttpServletRequest request = pack.getHttpServerletRequest();
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetVerificationDeal.Builder ret = PRetVerificationDeal.newBuilder();
		try {
			CWVCommonSmsVerifyExample example = new CWVCommonSmsVerifyExample();
			example.createCriteria().andPhoneEqualTo(pb.getPhone()).andIsVerifyEqualTo("0");
			List<Object> list = sysDaos.cwvcommonsmsverifyDao.selectByExample(example);
			if (list == null || list.isEmpty()) {
				// 没有验证记录
				ret.setRetCode("02");
				ret.setRetMsg("没有短信验证记录，请重新发起短信验证！");
			} else if (list.size() > 1) {
				for (Object record : list) {
					CWVCommonSmsVerify cWVCommonSmsVerify = (CWVCommonSmsVerify) record;
					cWVCommonSmsVerify.setIsVerify("1");
				}
				ret.setRetCode("03");
				ret.setRetMsg("有多条短信验证记录,请重新发起短信验证！");
			} else {
				CWVCommonSmsVerify cWVCommonSmsVerify = (CWVCommonSmsVerify) list.get(0);
				Date expiresDate = cWVCommonSmsVerify.getExpires();
				int num = DateUtil.compare(new Date(), expiresDate);
				if (num == 1) {
					cWVCommonSmsVerify.setIsVerify("1");
					ret.setRetCode("04");
					ret.setRetMsg("验证码已过期，请重新发起短信验证！");
				} else {
					// 获取对应用户的手机验证码
					String strSmsVer = cWVCommonSmsVerify.getVerifyCode();
					if (StringUtils.equals(strSmsVer, pb.getCode())) {
						// 设置成已验证
						cWVCommonSmsVerify.setIsVerify("1");
					}
					ret.setRetCode("01");
					ret.setRetMsg("验证码验证成功！");
				}
			}
			
			if (list != null && !list.isEmpty()) {
				sysDaos.cwvcommonsmsverifyDao.batchUpdate(list);
			}

		} catch (Exception e) {
			ret.setRetCode("99");
			ret.setRetMsg("验证码验证失败！");
			log.warn("SmsVerificationService onPBPacket error...", e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}