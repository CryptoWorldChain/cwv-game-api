package org.brewchain.cwv.common.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.brewchain.cwv.common.dao.Daos;
import org.brewchain.cwv.common.service.Sms.PBSmsDeal;
import org.brewchain.cwv.common.service.Sms.PRetMsgVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PTPSCommand;
import org.brewchain.cwv.common.service.Sms.PTPSModule;
import org.brewchain.cwv.common.util.DateUtil;
import org.brewchain.cwv.common.util.RandomUtill;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonSmsVerify;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.JsonSerializer;
import onight.tfw.outils.serialize.UUIDGenerator;

/**
 * 短信验证码发送接口
 * 
 * @author leo
 *
 */
@NActorProvider
@Slf4j
@Data
public class SmsService extends SessionModules<PBSmsDeal> {

	@ActorRequire
	Daos sysDaos;

	@ActorRequire(name = "http", scope = "global")
	IPacketSender sender;
	// 请求地址
	private String url = "http://intapi.253.com/send/json";

	// API账号，50位以内。必填
	private String account = "I6313616";

	// API账号对应密钥，联系客服获取。必填
	String password = "f3arA7QX5o567f";

	// 短信内容。长度不能超过536个字符。必填
	String msg = "【%s】,您的验证码是：%s,请在5分钟内输入，请勿透露给他人";

	// 手机号码，格式(区号+手机号码)，例如：8615800000000，其中86为中国的区号，区号前不使用00开头,15800000000为接收短信的真实手机号码。5-20位。必填
	String mobile = "";

	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.AUT.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.SMS.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PBSmsDeal pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		HttpServletRequest request = pack.getHttpServerletRequest();
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetMsgVerificationDeal.Builder ret = PRetMsgVerificationDeal.newBuilder();

		try {
			
			// 验证码生成
			String fixVer = RandomUtill.autoNumber(4);
			String strMsg = String.format(msg, "加密世界",fixVer);
			mobile = pb.getCountryCode() + pb.getPhone();
			mobile = deleteStartZero(mobile.trim());
		

			// 验证码入库
			CWVCommonSmsVerify cWVCommonSmsVerify = new CWVCommonSmsVerify();
			cWVCommonSmsVerify.setVerifyId(UUIDGenerator.generate());
			cWVCommonSmsVerify.setIsVerify("0");
			cWVCommonSmsVerify.setPhone(pb.getPhone());
			cWVCommonSmsVerify.setVerifyCode(fixVer);
			cWVCommonSmsVerify.setCountryCode(pb.getCountryCode());
			cWVCommonSmsVerify.setVerifyType(pb.getType());
			//过期时间   默认5分钟
			cWVCommonSmsVerify.setExpires(DateUtil.addMinute(new Date(), 5));

			sysDaos.cwvcommonsmsverifyDao.insertSelective(cWVCommonSmsVerify);
			// 组装发送json
			Map<String, String> jsonMap = new HashMap<>();
			jsonMap.put("account", account);
			jsonMap.put("password", password);
			jsonMap.put("msg", strMsg);
			jsonMap.put("mobile", mobile);
			String jsonStr = JsonSerializer.formatToString(jsonMap);
			// 组装地址及信息
			FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", url);
			// 发送短信
			val yearMeasureRet = sender.send(pp, 30000);
			// 返回值处理
			Map<String, Object> jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()),
					Map.class);

			String code = jsonRet.get("code").toString();
			String msgid = jsonRet.get("msgid").toString();
			String error = jsonRet.get("error").toString();

			log.info("状态码:" + code + ",状态码说明:" + error + ",消息id:" + msgid);

			ret.setRetCode("00");
			ret.setRetMsg("success");
		} catch (Exception e) {
			ret.setRetCode("-1");
			ret.setRetMsg("fails");
			log.warn("SmsService onPBPacket error...", e);
		}

		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	
	public String deleteStartZero(String mobile) {
		String strmobile = mobile;
		BigDecimal bigNum = new BigDecimal(strmobile);
		strmobile = "" + bigNum.toString();
		return strmobile;
	}
}
