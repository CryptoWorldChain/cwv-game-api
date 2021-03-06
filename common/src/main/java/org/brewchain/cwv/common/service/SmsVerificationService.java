package org.brewchain.cwv.common.service;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.common.dao.SysDaos;
import org.brewchain.cwv.common.service.Sms.PBVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PRetVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PTPSCommand;
import org.brewchain.cwv.common.service.Sms.PTPSModule;
import org.brewchain.cwv.common.util.DateUtil;
import org.brewchain.cwv.common.util.ReturnCodeMsgEnum;
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

	@ActorRequire(name="Sys_Daos")
	SysDaos sysDaos;

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
			verify(pack, pb, ret);
		} catch (Exception e) {
			ret.setRetCode(ReturnCodeMsgEnum.VER_EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.VER_EXCEPTION.getRetMsg());
			log.warn("SmsVerificationService onPBPacket error...", e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	public void verify( FramePacket pack, PBVerificationDeal pb,	PRetVerificationDeal.Builder ret ) {
			if("9999".equals(pb.getCode())){
				ret.setRetCode(ReturnCodeMsgEnum.VER_SUCCESS.getRetCode());
				ret.setRetMsg(ReturnCodeMsgEnum.VER_SUCCESS.getRetMsg());
				return;
			}
			
			if(StringUtils.isEmpty(pb.getCode())) {
				
				ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
				ret.setRetMsg("短信验证码不能为空");
				return;
			}
		
			if(!Pattern.matches("^\\d{4}$",pb.getCode())) {
				ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
				ret.setRetMsg("验证码格式错误");
				return;
			}
			
			CWVCommonSmsVerifyExample example = new CWVCommonSmsVerifyExample();
			example.createCriteria().andPhoneEqualTo(pb.getPhone())
			.andVerifyTypeEqualTo(pb.getType())
			.andIsVerifyEqualTo("0");
			example.setOrderByClause("expires desc");
			List<Object> list = sysDaos.cwvcommonsmsverifyDao.selectByExample(example);
			if (list == null || list.isEmpty()) {
				// 没有验证记录
				ret.setRetCode(ReturnCodeMsgEnum.VER_ERROR_EMPTY.getRetCode());
				ret.setRetMsg(ReturnCodeMsgEnum.VER_ERROR_EMPTY.getRetMsg());
			} else if (list.size() > 1) {
				for (Object record : list) {
					CWVCommonSmsVerify cWVCommonSmsVerify = (CWVCommonSmsVerify) record;
					cWVCommonSmsVerify.setIsVerify("1");
				}
				ret.setRetCode(ReturnCodeMsgEnum.VER_ERROR_DUPLICATE.getRetCode());
				ret.setRetMsg(ReturnCodeMsgEnum.VER_ERROR_DUPLICATE.getRetMsg());
			} else {
				CWVCommonSmsVerify cWVCommonSmsVerify = (CWVCommonSmsVerify) list.get(0);
				Date expiresDate = cWVCommonSmsVerify.getExpires();
				int num = DateUtil.compare(new Date(), expiresDate);
				if (num == 1) {
					cWVCommonSmsVerify.setIsVerify("1");
					ret.setRetCode(ReturnCodeMsgEnum.VER_ERROR_EXPIRED.getRetCode());
					ret.setRetMsg(ReturnCodeMsgEnum.VER_ERROR_EXPIRED.getRetMsg());
				} else {
					// 获取对应用户的手机验证码
					String strSmsVer = cWVCommonSmsVerify.getVerifyCode();
					if (StringUtils.equals(strSmsVer, pb.getCode())) {//重置密码
						// 设置成已验证
						cWVCommonSmsVerify.setIsVerify("1");
						ret.setRetCode(ReturnCodeMsgEnum.VER_SUCCESS.getRetCode());
						ret.setRetMsg(ReturnCodeMsgEnum.VER_SUCCESS.getRetMsg());
					}else{
						ret.setRetCode(ReturnCodeMsgEnum.VER_ERROR.getRetCode());
						ret.setRetMsg(ReturnCodeMsgEnum.VER_ERROR.getRetMsg());
					}
					
				}
			}
			
			if (list != null && !list.isEmpty()) {
				sysDaos.cwvcommonsmsverifyDao.batchUpdate(list);
			}
	}
}
