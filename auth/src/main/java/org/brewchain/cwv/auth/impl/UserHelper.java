package org.brewchain.cwv.auth.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.dao.Dao;
import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.filter.SessionManager;
import org.brewchain.cwv.auth.util.DateUtil;
import org.brewchain.cwv.auth.util.ValidatorUtil;
import org.brewchain.cwv.auth.util.ValidatorUtil.ValidateEnum;
import org.brewchain.cwv.auth.util.jwt.Constant;
import org.brewchain.cwv.auth.util.jwt.SubjectModel;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthRefreshTokenExample;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUserExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTrade;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradeExample;
import org.fc.hzq.service.sys.User.PRetCommon;
import org.fc.hzq.service.sys.User.PRetCommon.Builder;
import org.fc.hzq.service.sys.User.PRetLogin;
import org.fc.hzq.service.sys.User.PSCommon;
import org.fc.hzq.service.sys.User.PSLogin;
import org.fc.hzq.service.sys.User.PSRegistry;
import org.fc.hzq.service.sys.User.UserInfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.UUIDGenerator;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
/**
 * 用户service
 * 
 * @author Moon
 * @date 2018-03-30
 */
public class UserHelper implements ActorService {

	@ActorRequire
	Dao dao;

	@ActorRequire
	SessionManager sessionManager;

	@ActorRequire
	TokenHelper tokenHelper;

	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	/**
	 * 注册用户
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void registerProcess(FramePacket pack, PSRegistry pb, PRetCommon.Builder ret) {

		// 1 校验参数
		// 1.1 参数格式
		if (!ValidatorUtil.isMobile(pb.getPhone())) {
			throw new IllegalArgumentException(ValidateEnum.MOBILE.getVerifyMsg());
		}
		if (!ValidatorUtil.isPassword(pb.getPassword())) {
			throw new IllegalArgumentException(ValidateEnum.PASSWORD.getVerifyMsg());
		}

		// 国家代码
		if (pb.getCountryId() == 0) {
			throw new IllegalArgumentException("国家不能为空");
		}

		// 1.2 短信验证码 前端调取 common by leo 验证码校验接口

		// 1.3 用户重复性校验
		CWVAuthUser authUser = getUserByPhone(pb.getPhone());
		if (authUser != null) {
			ret.setRetCode(ReturnCodeMsgEnum.REG_DUPLICATE_PHONE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.REG_DUPLICATE_PHONE.getRetMsg());
			return;
		}

		// 2 初始化用户数据（包含默认值）
		authUser = new CWVAuthUser();
		authUser.setNickName(pb.getNickName());
		authUser.setUserName(pb.getUserName());
		authUser.setCountryId(pb.getCountryId());
		// if(pb.getPhoneCode() !=null && pb.getPhoneCode().equals(""))
		// authUser.setPhone(phone);

		authUser.setPhone(pb.getPhone());
		String salt = "$1$" + pb.getPhone().substring(7);
		String pwdMd5 = Md5Crypt.md5Crypt(pb.getPassword().getBytes(), salt);
		authUser.setPassword(pwdMd5);
		authUser.setSalt(salt);

		// 初始化数据
		authUser.setCreatedTime(new Date());
		authUser.setStatus("1");
		authUser.setValidatePhone("1");
		// 3 保存用户

		this.dao.userDao.insert(authUser);
		ret.setRetCode(ReturnCodeMsgEnum.REG_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.REG_SUCCESS.getRetMsg());

	}

	/**
	 * 用户登陆
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void login(FramePacket pack, PSLogin pb, PRetLogin.Builder ret) {

		// 1 校验入参
		if (!ValidatorUtil.isMobile(pb.getPhone())) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetMsg());
			return;
		}

		// TODO 校验验证码 调取 common by leo
		// 暂时去掉
		// if (false) {
		// ret.setRetCode(ReturnCodeMsgEnum.LIN_ERROR_CODE.getRetCode())
		// .setRetMsg(ReturnCodeMsgEnum.LIN_ERROR_CODE.getRetMsg());
		// return;
		// }

		// 2 用户查询 及 密码校验
		CWVAuthUser authUser = getUserByPhone(pb.getPhone());

		String pwdMd5 = Md5Crypt.md5Crypt(pb.getPassword().getBytes(), authUser.getSalt());

		if (authUser == null || !pwdMd5.equals(authUser.getPassword())) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetMsg());
			return;
		}

		// 3 设置token
		String refreshToken = UUIDGenerator.generate();
		tokenHelper.tokenSetting(pack, authUser, refreshToken);
		// 5 登陆记录
		// TODO

		// 设置返回信息
		ret.setTokenType(Constant.TOKEN_TYPE);
		ret.setAccessToken(pack.getExtHead().getSMID());
		ret.setExpiresIn(Constant.JWT_TTL / 1000l);
		ret.setRefreshToken(refreshToken);
		// set userInfo
		UserInfo.Builder userInfo = UserInfo.newBuilder();
		userInfo.setUid(authUser.getUserId() + "");
		userInfo.setNickName(StringUtils.isEmpty(authUser.getNickName()) ? "" : authUser.getNickName());
		userInfo.setPhone(authUser.getPhone());
		//TODO 图片服务器返回URL 或者返回头像接口
		userInfo.setImageUrl("/cwv/usr/pbghi.do");
		ret.setUserInfo(userInfo);
		ret.setExpiresIn(Constant.JWT_TTL / 1000l);
		ret.setRetCode(ReturnCodeMsgEnum.LIN_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.LIN_SUCCESS.getRetMsg());

	}

	public CWVAuthUser getUserByPhone(String phone) {
		CWVAuthUserExample example = new CWVAuthUserExample();
		CWVAuthUserExample.Criteria criteria = example.createCriteria();
		criteria.andPhoneEqualTo(phone);
		List<Object> userList = dao.userDao.selectByExample(example);
		// 用户不存在
		if (userList == null || userList.isEmpty()) {
			return null;
		}
		return (CWVAuthUser) userList.get(0);
	}

	/**
	 * 设置用户昵称
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void setNickName(FramePacket pack, UserInfo pb, Builder ret) {

		// 校验入参
		if (StringUtils.isEmpty(pb.getNickName())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("昵称不能为空");
			return;
		}

		// 更新昵称
		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());
		CWVAuthUser authUserUpdate = new CWVAuthUser();
		authUserUpdate.setUserId(model.getUid());
		authUserUpdate.setNickName(pb.getNickName());
		dao.userDao.updateByPrimaryKeySelective(authUserUpdate);

		ret.setRetCode(ReturnCodeMsgEnum.SNN_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SNN_SUCCESS.getRetMsg());
	}

	/**
	 * 设置交易密码
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void setTxPwd(FramePacket pack, PSLogin pb, Builder ret) {

		// 校验参数
		if (!ValidatorUtil.isPassword(pb.getPassword())) {
			throw new IllegalArgumentException(ValidateEnum.PASSWORD.getVerifyMsg());
		}
		// 更新交易密码
		// 查询用户获取salt
		CWVAuthUser authUser = this.getCurrentUser(pack);
		// 创建trade对象更新信息
		CWVUserTrade userTrade = new CWVUserTrade();
		userTrade.setUserId(authUser.getUserId());
		userTrade.setCreatedTime(new Date());
		String tradePwdMd5 = Md5Crypt.md5Crypt(pb.getPassword().getBytes(), authUser.getSalt());
		userTrade.setTradePassword(tradePwdMd5);

		// 查询原有交易密码
		CWVUserTradeExample example = new CWVUserTradeExample();
		CWVUserTradeExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(authUser.getUserId());
		List<Object> listTrade = dao.tradeDao.selectByExample(example);
		if (listTrade == null || listTrade.isEmpty()) {
			userTrade.setCreatedTime(new Date());
			dao.tradeDao.insert(userTrade);
		} else {
			CWVUserTrade old = (CWVUserTrade) listTrade.get(0);
			userTrade.setTradeId(old.getTradeId());
			;
			dao.tradeDao.updateByPrimaryKeySelective(userTrade);
		}

		ret.setRetCode(ReturnCodeMsgEnum.STP_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.STP_SUCCESS.getRetMsg());
	}

	/**
	 * 重置密码
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void resetPwd(FramePacket pack, PSLogin pb, Builder ret) {

		// 1 校验入参
		// 1.1 格式校验
		if (!ValidatorUtil.isMobile(pb.getPhone())) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetMsg());
			return;
		}
		// 1.2校验电话与当前用户是否一致

		CWVAuthUser authUser = getCurrentUser(pack);

		if (!authUser.getPhone().equals(pb.getPhone())) {

			// TODO 次数限制 +1
			ret.setRetCode(ReturnCodeMsgEnum.RSP_ERROR_PHONE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.RSP_ERROR_PHONE.getRetMsg());
			return;
		}

		// 校验重复密码
		String pwdMd5 = Md5Crypt.md5Crypt(pb.getPassword().getBytes(), authUser.getSalt());
		if (authUser.getPassword().equals(pwdMd5)) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("无法更新相同密码");
			return;
		}
		// 1.3校验短信验证码
		// if(StringUtils.isEmpty(pb.getPhoneVerifyCode())){
		// ret.setRetCode(ReturnCodeMsgEnum.RSP_ERROR_CODE.getRetCode())
		// .setRetMsg(ReturnCodeMsgEnum.RSP_ERROR_CODE.getRetMsg());
		// return;
		// }else {
		// // TODO 调取common by leo 校验短信验证码
		// }

		// 2 更新密码
		CWVAuthUser authUserUpdate = new CWVAuthUser();
		authUserUpdate.setUserId(authUser.getUserId());
		authUserUpdate.setPassword(pwdMd5);
		dao.userDao.updateByPrimaryKeySelective(authUserUpdate);

		ret.setRetCode(ReturnCodeMsgEnum.RSP_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.RSP_SUCCESS.getRetMsg());

	}

	/**
	 * 用户注销
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void logout(FramePacket pack, PSLogin pb, PRetCommon.Builder ret) {
		
		
		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());

		if(model == null){
			ret.setRetCode(ReturnCodeMsgEnum.LOU_SUCCESS.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.LOU_SUCCESS.getRetMsg());
			return;
		}
		// 设置access_token无效

		tokenHelper.destroyAccessToken(pack.getExtHead().getSMID());

		// 设置refresh_token无效

		CWVAuthRefreshTokenExample refreshTokenExample = new CWVAuthRefreshTokenExample();
		CWVAuthRefreshTokenExample.Criteria criteria = refreshTokenExample.createCriteria();
		criteria.andUserIdEqualTo(model.getUid());
		dao.tokenDao.deleteByExample(refreshTokenExample);

		ret.setRetCode(ReturnCodeMsgEnum.LOU_SUCCESS.getRetCode())
				.setRetMsg(ReturnCodeMsgEnum.LOU_SUCCESS.getRetMsg());

	}

	public void setIconName(FramePacket pack, PSCommon pb, PRetCommon.Builder ret) {
		if (StringUtils.isEmpty(pb.getFiledata())) {
			throw new IllegalArgumentException("文件不能为空");
		}

		//

		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());
		String filePath = invokeImageService(model.getUid(), pb.getFiledata());

		if (StringUtils.isEmpty(filePath)) {
			throw new IllegalArgumentException("图片上传异常");
		}
		CWVAuthUser authUser = new CWVAuthUser();
		authUser.setUserId(model.getUid());
		authUser.setImageUrl(filePath);
		dao.userDao.updateByPrimaryKeySelective(authUser);
		ret.setRetCode(ReturnCodeMsgEnum.SHI_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SHI_SUCCESS.getRetMsg());
		ret.setImageUrl("/cwv/usr/pbghi.do");
	}

	/**
	 * 调取图片服务器
	 * 
	 * @param userId
	 * @param fileData
	 * @return 图片地址
	 */
	private String invokeImageService(int userId, String fileDataBase64) {
		String base64 = fileDataBase64;
		String fileInfo = base64.substring(0, base64.indexOf(",") + 1);
		String base64Image = base64.substring(base64.indexOf(",") + 1);
		String imageType = fileInfo.substring(fileInfo.lastIndexOf("/") + 1, fileInfo.lastIndexOf(";"));
		byte[] imageBytes = Base64.decodeBase64(base64Image);
		ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes);

		String imageUrl = null;
		try {

			BufferedImage image = ImageIO.read(imageStream);
			String relativeFilePath = "auth/fileUpload/icon/" + userId + "_" + DateUtil.getNowDate() + "." + imageType;
			File imageFile = new File(relativeFilePath);
			if (imageFile.exists()) {
				ImageIO.write(image, imageType, imageFile);// 可以相对路径也可绝对路径
			} else {
				if (!imageFile.getParentFile().exists()) {
					if (!imageFile.getParentFile().mkdirs()) {
						throw new IllegalArgumentException("生成头像失败");
					} else {
						if (imageFile.createNewFile()) {
							ImageIO.write(image, imageType, imageFile);// 可以相对路径也可绝对路径
						} else {
							throw new IllegalArgumentException("生成头像失败");
						}
					}
				}
			}
			imageUrl = "" + relativeFilePath;
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("图片上传异常");
		}

		return imageUrl;
	}

	/**
	 * 获取头像
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void getHeadImage(FramePacket pack, PSCommon pb, Builder ret) {
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		HttpServletRequest request = pack.getHttpServerletRequest();
		HttpServletResponse response = pack.getHttpServerletResponse();

		// 将验证码输入到session中，用来验证
		CWVAuthUser authUser = getCurrentUser(pack);
		try {

			if (StringUtils.isEmpty(authUser.getImageUrl())) {
				response.getOutputStream().close();
				return;
			}

			FileInputStream fis = new FileInputStream(authUser.getImageUrl()); // 创建输入流
			String imageType = authUser.getImageUrl().substring(authUser.getImageUrl().lastIndexOf(".") + 1);
			response.setContentType("image/" + imageType);
			byte[] byt = new byte[fis.available()];
			// StringBuilder str = new StringBuilder();// 不建议用String
			fis.read(byt); // 从输入流中读取一定数量的字节，并将其存储在缓冲区数组 b 中。以整数形式返回实际读取的字节数。
			// for (byte bs : byt) {
			// str.append(Integer.toBinaryString(bs));
			//
			// }
			response.getOutputStream().write(byt);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private CWVAuthUser getCurrentUser(FramePacket pack) {
		CWVAuthUser authUser = new CWVAuthUser();
		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());
		authUser.setUserId(model.getUid());
		authUser = dao.userDao.selectByPrimaryKey(authUser);
		return authUser;
	}

}
