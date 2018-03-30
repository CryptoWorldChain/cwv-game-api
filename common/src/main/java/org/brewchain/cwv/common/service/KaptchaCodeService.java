package org.brewchain.cwv.common.service;

import java.awt.image.BufferedImage;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.brewchain.cwv.common.service.Sms.PBMsgVerificationDeal;
import org.brewchain.cwv.common.service.Sms.PTPSCommand;
import org.brewchain.cwv.common.service.Sms.PTPSModule;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorProps;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.beans.FramePacket;

/**
 * 获取图片验证码
 * @author leo
 *
 */
@NActorProvider
@Slf4j
@Data
public class KaptchaCodeService extends SessionModules<PBMsgVerificationDeal> {
	
	private static Producer captchaProducer = getProducer();

	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.MSG.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.SMS.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PBMsgVerificationDeal pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		HttpServletRequest request = pack.getHttpServerletRequest();
		HttpServletResponse response = pack.getHttpServerletResponse();
		try {
			// Set to expire far in the past.
			response.setDateHeader("Expires", 0);
			// Set standard HTTP/1.1 no-cache headers.
			response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
			// Set IE extended HTTP/1.1 no-cache headers (use addHeader).
			response.addHeader("Cache-Control", "post-check=0, pre-check=0");
			// Set standard HTTP/1.0 no-cache header.
			response.setHeader("Pragma", "no-cache");
			// return a jpeg
			response.setContentType("image/jpeg");
			
			// create the text for the image
			String capText = captchaProducer.createText();

			// store the text in the session
			request.getSession().setAttribute(Constants.KAPTCHA_SESSION_KEY, capText);

			// create the image with the text
			BufferedImage bi = captchaProducer.createImage(capText);

			ServletOutputStream out = response.getOutputStream();

			// write the data out
			ImageIO.write(bi, "jpg", out);
			try {
				out.flush();
			} finally {
				out.close();
			}
		} catch (Exception e) {
			log.warn("KaptchaCodeService onPBPacket error...", e);
		}
	}
	
	public static Producer getProducer() {
		Properties properties = new Properties();
		//无边框
		properties.setProperty("kaptcha.border", "no");
		//渲染效果：水纹：WaterRipple；鱼眼：FishEyeGimpy；阴影：ShadowGimpy
		properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.WaterRipple");
		properties.setProperty("kaptcha.border.color", "105,179,90");
		//验证码颜色
		properties.setProperty("kaptcha.textproducer.font.color", "black");
		properties.setProperty("kaptcha.image.width", "250");
		properties.setProperty("kaptcha.textproducer.font.size", "80");
		properties.setProperty("kaptcha.image.height", "90");
		properties.setProperty("kaptcha.session.key", "code");
		//生成几个验证码
		properties.setProperty("kaptcha.textproducer.char.length", "5");
		properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
		
		//和登录框背景颜色一致
		
		properties.setProperty("kaptcha.background.clear.from", "247,247,247");
		properties.setProperty("kaptcha.background.clear.to", "247,247,247");
		Config config = new Config(properties);

		DefaultKaptcha captchaProducer = new DefaultKaptcha();
		captchaProducer.setConfig(config);
		return captchaProducer;
	}
}
