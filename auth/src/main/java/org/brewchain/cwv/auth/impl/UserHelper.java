package org.brewchain.cwv.auth.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.dao.Dao;
import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.filter.SessionManager;
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
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
					.setRetMsg("昵称不能为空");
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
		
		//校验参数
		if(!ValidatorUtil.isPassword(pb.getPassword())) {
			throw new IllegalArgumentException(ValidateEnum.PASSWORD.getVerifyMsg());
		}
		//更新交易密码
			//查询用户获取salt
		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());
		CWVAuthUser authUser = new CWVAuthUser();
		authUser.setUserId(model.getUid());
		authUser = dao.userDao.selectByPrimaryKey(authUser);
			//创建trade对象更新信息
		CWVUserTrade  userTrade = new CWVUserTrade();
		userTrade.setUserId(model.getUid());
		userTrade.setCreatedTime(new Date());
		String tradePwdMd5 = Md5Crypt.md5Crypt(pb.getPassword().getBytes(), authUser.getSalt());
		userTrade.setTradePassword(tradePwdMd5);
		
			// 查询原有交易密码
		CWVUserTradeExample example = new CWVUserTradeExample();
		CWVUserTradeExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(model.getUid());
		List<Object> listTrade = dao.tradeDao.selectByExample(example);
		if (listTrade == null || listTrade.isEmpty()) {
			userTrade.setCreatedTime(new Date());
			dao.tradeDao.insert(userTrade);
		} else {
			CWVUserTrade old = (CWVUserTrade) listTrade.get(0);
			userTrade.setTradeId(old.getTradeId());;
			dao.tradeDao.updateByPrimaryKeySelective(userTrade);
		}
		
		ret.setRetCode(ReturnCodeMsgEnum.STP_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.STP_SUCCESS.getRetMsg());
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

		CWVAuthUser authUser = new CWVAuthUser();
		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());
		authUser.setUserId(model.getUid());
		authUser = dao.userDao.selectByPrimaryKey(authUser);

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
		authUserUpdate.setUserId(model.getUid());
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
	public void logout(FramePacket pack, PSLogin pb, org.fc.hzq.service.sys.User.PRetLogin.Builder ret) {
		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());

		// 设置access_token无效

		tokenHelper.destroyAccessToken(pack.getExtHead().getSMID());

		// 设置refresh_token无效

		CWVAuthRefreshTokenExample refreshTokenExample = new CWVAuthRefreshTokenExample();
		CWVAuthRefreshTokenExample.Criteria criteria = refreshTokenExample.createCriteria();
		criteria.andUserIdEqualTo(model.getUid());
		dao.tokenDao.deleteByExample(refreshTokenExample);

		ret.setRetCode(ReturnCodeMsgEnum.LOUT_SUCCESS.getRetCode())
				.setRetMsg(ReturnCodeMsgEnum.LOUT_SUCCESS.getRetMsg());

	}
}
