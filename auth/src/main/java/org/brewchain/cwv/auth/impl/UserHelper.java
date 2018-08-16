package org.brewchain.cwv.auth.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.dao.Dao;
import org.brewchain.cwv.auth.enums.PropertyTypeEnum;
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
import org.brewchain.cwv.dbgens.common.entity.CWVCommonCountry;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonCountryExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSettingExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncomeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradePwd;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradePwdExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.wallet.service.Wallet.RetNewAddress;
import org.fc.hzq.service.sys.User.PRetCommon;
import org.fc.hzq.service.sys.User.PRetCommon.Builder;
import org.fc.hzq.service.sys.User.PRetLogin;
import org.fc.hzq.service.sys.User.PSCommon;
import org.fc.hzq.service.sys.User.PSLogin;
import org.fc.hzq.service.sys.User.PSRegistry;
import org.fc.hzq.service.sys.User.PSSendMsgCode;
import org.fc.hzq.service.sys.User.UserInfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.ojpa.api.TransactionExecutor;
import onight.tfw.otransio.api.IPacketSender;
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
@Instantiate(name = "User_Helper")
public class UserHelper implements ActorService {

	@ActorRequire(name = "Dao")
	Dao dao;

	@ActorRequire(name = "Session_Manager")
	SessionManager sessionManager;

	@ActorRequire(name = "Token_Helper")
	TokenHelper tokenHelper;

	@ActorRequire(name = "http", scope = "global")
	IPacketSender sender;
	
	@ActorRequire(name="Wlt_Helper")
	WltHelper wltHelper;

	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	enum MsgCodeType {
		REG("1"), // 注册
		STP("2"), // 交易密码
		RSP("3"), // 重置登陆密码
		SPS("4");// 设置登陆密码
		private String value;

		MsgCodeType(String value) {
			this.value = value;
		}
	}

	public HashSet<String> msgCodeLoginType = new HashSet<String>() {
		{
			add(MsgCodeType.STP.value);
			add(MsgCodeType.RSP.value);
			add(MsgCodeType.SPS.value);
		}
	};

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
		if (StringUtils.isEmpty(pb.getCountryCode())) {
			throw new IllegalArgumentException("国家不能为空");
		}

		if (StringUtils.isEmpty(pb.getPhoneVerifyCode())) {
			throw new IllegalArgumentException("短信验证码不能为空");
		}
		if (StringUtils.isEmpty(pb.getPhoneCode())) {
			throw new IllegalArgumentException("手机代码不能为空");
		}
		// 1.2 短信验证码 前端调取 common by leo 验证码校验接口

		HashMap<String, String> jsonMapPhone = new HashMap<>();
		jsonMapPhone.put("phone", pb.getPhone());
		jsonMapPhone.put("code", pb.getPhoneVerifyCode());
		jsonMapPhone.put("type", MsgCodeType.REG.value);
		jsonMapPhone = InokeInterfaceHelper.checkMsgCode(jsonMapPhone, sender);

		if (!ReturnCodeMsgEnum.SUCCESS.getRetCode().equals(jsonMapPhone.get("ret_code"))) {
			ret.setRetCode(jsonMapPhone.get("ret_code")).setRetMsg(jsonMapPhone.get("ret_msg"));
			return;
		}

		// 1.3 用户重复性校验
		CWVAuthUser user = getUserByPhone(pb.getPhone());
		if (user != null) {
			ret.setRetCode(ReturnCodeMsgEnum.REG_DUPLICATE_PHONE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.REG_DUPLICATE_PHONE.getRetMsg());
			return;
		}

// 1.4 用户昵称重复性校验
		CWVAuthUserExample authUserExample = new CWVAuthUserExample();
		authUserExample.createCriteria().andUserNameEqualTo(pb.getUserName());
		Object o = dao.userDao.selectOneByExample(authUserExample);

		if (o != null) {
			ret.setRetCode(ReturnCodeMsgEnum.REG_DUPLICATE_NAME.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.REG_DUPLICATE_NAME.getRetMsg());
			return ;
		}
		
		if(StringUtils.isNotBlank(pb.getNickName())){
			byte[] str=pb.getNickName().getBytes();
			if(str.length>18){
				ret.setRetCode(ReturnCodeMsgEnum.REG_ERROR_NICKNAME.getRetCode())
				.setRetMsg(ReturnCodeMsgEnum.REG_ERROR_NICKNAME.getRetMsg());
				return;
			}
		}
		
		// 2 初始化用户数据（包含默认值）
		final CWVAuthUser authUser = new CWVAuthUser();
		authUser.setNickName(StringUtils.isEmpty(pb.getNickName()) ? pb.getUserName() : pb.getNickName());
		authUser.setUserName(pb.getUserName());
		CWVCommonCountryExample countryExample = new CWVCommonCountryExample();
		countryExample.createCriteria().andRegionCodeEqualTo(pb.getCountryCode());
//		CWVCommonCountry countryOb = (CWVCommonCountry) dao.commonCountryDao.selectOneByExample(countryExample);
//		authUser.setCountryId(countryOb.getCountryId());
		authUser.setCountryCode(pb.getCountryCode());
		// if(pb.getPhoneCode() !=null && pb.getPhoneCode().equals(""))
		// authUser.setPhone(phone);
		authUser.setPhone(pb.getPhone());
		String salt = "$1$" + pb.getPhone().substring(7);
		String pwdMd5 = getPwdMd5(pb.getPassword(), salt);
		authUser.setPassword(pwdMd5);
		authUser.setSalt(salt);

		// 初始化数据
		authUser.setCreatedTime(new Date());
		authUser.setStatus("1");
		authUser.setValidatePhone("1");
		// 3 保存用户

		// 创建账户 TODO
		this.dao.userDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {
				RetNewAddress.Builder address=  wltHelper.createAccount("");
				if(address.getRetCode()!=1){
					throw new IllegalArgumentException("获取钱包账户失败");
				}
				RetNewAddress.Builder incomeAddress=  wltHelper.createAccount("");
				if(address.getRetCode()!=1){
					throw new IllegalArgumentException("获取钱包账户失败");
				}
				dao.userDao.insertIfNoExist(authUser);
				CWVAuthUser userInsert = getUserByPhone(authUser.getPhone());
				
				CWVUserWallet userWallet = new CWVUserWallet();
				userWallet.setAccount(address.getAddress());
				userWallet.setIncomeAddress(incomeAddress.getAddress());
				userWallet.setBalance(new BigDecimal(0));
				userWallet.setCoinIcon("http://cwc.icon");
				userWallet.setCoinType(Byte.valueOf("0"));
				userWallet.setCreateTime(new Date());
				userWallet.setDrawCount(0);
				userWallet.setTopupBalance(new BigDecimal(0));
				userWallet.setUpdateTime(new Date());
				userWallet.setUserId(userInsert.getUserId());
				userWallet.setIncomeFunctional(new BigDecimal(0));
				userWallet.setIncomeOrdinary(new BigDecimal(0));
				userWallet.setIncomeTypical(new BigDecimal(0));
				dao.walletDao.insertSelective(userWallet);
				return null;
			}
		});

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
		pack.getHttpServerletRequest().getSession();
		// 1 校验入参
		if (!ValidatorUtil.isMobile(pb.getPhone())) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetMsg());
			return;
		}

		// 2 用户查询 及 密码校验
		CWVAuthUser authUser = getUserByPhone(pb.getPhone());

		if (authUser == null) {
			ret.setRetCode(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.LIN_ERROR_PHONE_PWD.getRetMsg());
			return;
		}

		String pwdMd5 = getPwdMd5(pb.getPassword(), authUser.getSalt());

		if (!pwdMd5.equals(authUser.getPassword())) {
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
		CWVCommonCountryExample countryExample = new CWVCommonCountryExample();
		countryExample.createCriteria().andRegionCodeEqualTo(authUser.getCountryCode());
		CWVCommonCountry countryOb = (CWVCommonCountry) dao.commonCountryDao.selectOneByExample(countryExample);

		userInfo.setPhoneCode(countryOb.getPhoneCode());
		CWVSysSettingExample example = new CWVSysSettingExample();
		example.createCriteria().andNameEqualTo("super_user");
		CWVSysSetting supperSet = (CWVSysSetting) dao.settingDao.selectOneByExample(example);
		if (supperSet.getValue().equals(authUser.getUserId().toString()))
			userInfo.setIsSupper("1");
		else {
			userInfo.setIsSupper("0");
		}
		// TODO 图片服务器返回URL 或者返回头像接口
//		userInfo.setImageUrl(authUser.getImageUrl() == null ? "" : authUser.getImageUrl());
		
		//用户头像信息 国家Id 国家name
		userInfo.setCountryId(authUser.getCountryId()+"");
		if(authUser.getCountryId() != null ) {
			CWVGameCountry country = new CWVGameCountry();
			country.setCountryId(authUser.getCountryId());
			country = dao.gameCountryDao.selectByPrimaryKey(country);
			if(country != null) {
				userInfo.setCountryName(country.getCountryName());
			}
		}
		
		//收益状态红点处理
		CWVUserPropertyIncomeExample incomeExample = new CWVUserPropertyIncomeExample();
		CWVUserPropertyIncomeExample.Criteria criteria = incomeExample.createCriteria();
		criteria.andUserIdEqualTo(authUser.getUserId()).andMasterEqualTo(1);
		// criteria.andStatusEqualTo((byte) 0);// 新建收益
		criteria.andPropertyIdIsNull();// property_id为null为统计数据
		criteria.andChainStatusEqualTo((byte) 1);
		incomeExample.setOrderByClause(" income_id desc ");

		
		// 根据类型查询房产
		List<Object> list = dao.incomeDao.selectByExample(incomeExample);
		// 未领取收益
		for(Object o : list) {
			CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
			
			if(income.getStatus().intValue()==0 && income.getChainStatus().intValue()==1 && income.getChainStatusClaim() == null)             {
				if(income.getType().toString().equals(PropertyTypeEnum.ORDINARY.getValue())) {
					userInfo.setIncomeOrdinary(1);
				} else if(income.getType().toString().equals(PropertyTypeEnum.ORDINARY.getValue())) {
					userInfo.setIncomeOrdinary(1);
				} else if(income.getType().toString().equals(PropertyTypeEnum.ORDINARY.getValue())) {
					userInfo.setIncomeOrdinary(1);
				}
			}
			
			
		}
		
		
		// 查询交易密码
		CWVUserTradePwd tradePwd = getTradePwd(authUser.getUserId());
		userInfo.setTradePwdSet(tradePwd == null ? "0" : "1");
		// 查询余额
		userInfo.setAccountBalance("0");
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

	public CWVAuthUser getUserById(Integer userId) {
		CWVAuthUser user = new CWVAuthUser();
		user.setUserId(userId);
		user = dao.userDao.selectByPrimaryKey(user);
		return user;
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

		byte[] str=pb.getNickName().getBytes();
		if(str.length>18){
			ret.setRetCode(ReturnCodeMsgEnum.REG_ERROR_NICKNAME.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.REG_ERROR_NICKNAME.getRetMsg());
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
		if (StringUtils.isEmpty(pb.getPhoneVerifyCode())) {
			ret.setRetCode(ReturnCodeMsgEnum.STP_ERROR_PHONE_CODE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.STP_ERROR_PHONE_CODE.getRetMsg());
			return;
		} else {
			HashMap<String, String> jsonMapPhone = new HashMap<>();
			jsonMapPhone.put("phone", authUser.getPhone());
			jsonMapPhone.put("code", pb.getPhoneVerifyCode());
			jsonMapPhone.put("type", MsgCodeType.STP.value); // 设置交易密码
			jsonMapPhone = InokeInterfaceHelper.checkMsgCode(jsonMapPhone, sender);

			if (!ReturnCodeMsgEnum.SUCCESS.getRetCode().equals(jsonMapPhone.get("ret_code"))) {
				ret.setRetCode(jsonMapPhone.get("ret_code")).setRetMsg(jsonMapPhone.get("ret_msg"));
				return;
			}
		}
		// 创建trade对象更新信息
		CWVUserTradePwd userTrade = new CWVUserTradePwd();
		userTrade.setUserId(authUser.getUserId());
		userTrade.setCreatedTime(new Date());
		String tradePwdMd5 = getPwdMd5(pb.getPassword(), authUser.getSalt());
		userTrade.setTradePassword(tradePwdMd5);

		// 查询原有交易密码
		CWVUserTradePwd tradePwdOld = getTradePwd(authUser.getUserId());
		if (tradePwdOld == null) {
			userTrade.setCreatedTime(new Date());
			dao.tradeDao.insert(userTrade);
		} else {
//			if (!getPwdMd5(pb.getPasswordOld(), authUser.getSalt()).equals(tradePwdOld.getTradePassword())) {
//				ret.setRetCode(ReturnCodeMsgEnum.STP_ERROR_PWD_OLD.getRetCode())
//						.setRetMsg(ReturnCodeMsgEnum.STP_ERROR_PWD_OLD.getRetMsg());
//				return;
//			}
			if (getPwdMd5(pb.getPassword(), authUser.getSalt()).equals(tradePwdOld.getTradePassword())) {
				ret.setRetCode(ReturnCodeMsgEnum.STP_DUPLICATE_PWD.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.STP_DUPLICATE_PWD.getRetMsg());
				return;
			}

			userTrade.setTradeId(tradePwdOld.getTradeId());
			dao.tradeDao.updateByPrimaryKeySelective(userTrade);
		}

		ret.setRetCode(ReturnCodeMsgEnum.STP_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.STP_SUCCESS.getRetMsg());
	}

	/**
	 * 获取交易密码
	 * 
	 * @param userId
	 * @return
	 */
	public CWVUserTradePwd getTradePwd(Integer userId) {
		CWVUserTradePwdExample example = new CWVUserTradePwdExample();
		CWVUserTradePwdExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userId);
		List<Object> listTrade = dao.tradeDao.selectByExample(example);
		return listTrade == null || listTrade.isEmpty() ? null : (CWVUserTradePwd) listTrade.get(0);
	}

	/**
	 * 通过salt MD5 生成密码
	 * 
	 * @param pwd
	 * @param salt
	 * @return
	 */
	public String getPwdMd5(String pwd, String salt) {
		return Md5Crypt.md5Crypt(pwd.getBytes(), salt);
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
			ret.setRetCode(ReturnCodeMsgEnum.RSP_ERROR_PHONE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.RSP_ERROR_PHONE.getRetMsg());
			return;
		}
		// 1.2校验电话与当前用户是否一致

		CWVAuthUser authUser = getUserByPhone(pb.getPhone());

		if (authUser == null) {

			// TODO 次数限制 +1
			ret.setRetCode(ReturnCodeMsgEnum.RSP_ERROR_PHONE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.RSP_ERROR_PHONE.getRetMsg());
			return;
		}

		// 校验重复密码
		String pwdMd5 = getPwdMd5(pb.getPassword(), authUser.getSalt());
		if (pwdMd5.equals(authUser.getPassword())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("无法更新相同密码");
			return;
		}
		// 1.3校验短信验证码

		if (StringUtils.isEmpty(pb.getPhoneVerifyCode())) {
			ret.setRetCode(ReturnCodeMsgEnum.RSP_ERROR_PHONE_CODE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.RSP_ERROR_PHONE_CODE.getRetMsg());
			return;
		} else {
			HashMap<String, String> jsonMapPhone = new HashMap<>();
			jsonMapPhone.put("phone", authUser.getPhone());
			jsonMapPhone.put("code", pb.getPhoneVerifyCode());
			jsonMapPhone.put("type", MsgCodeType.RSP.value); // 重置登陆密码
			jsonMapPhone = InokeInterfaceHelper.checkMsgCode(jsonMapPhone, sender);

			if (!ReturnCodeMsgEnum.SUCCESS.getRetCode().equals(jsonMapPhone.get("ret_code"))) {
				ret.setRetCode(jsonMapPhone.get("ret_code")).setRetMsg(jsonMapPhone.get("ret_msg"));
				return;
			}
		}

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

		if (model == null) {
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

		ret.setRetCode(ReturnCodeMsgEnum.LOU_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.LOU_SUCCESS.getRetMsg());

	}

	/**
	 * 设置头像
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void setIconName(FramePacket pack, PSCommon pb, PRetCommon.Builder ret) {
		if (StringUtils.isEmpty(pb.getCountryId())) {
			throw new IllegalArgumentException("国家Id不能为空");
		}

		CWVGameCountry country = new CWVGameCountry();
		country.setCountryId(Integer.parseInt(pb.getCountryId()));
		Object o = dao.commonCountryDao.selectByPrimaryKey(country);
		//
		if (o == null) {
			ret.setRetCode(ReturnCodeMsgEnum.SHI_ERROR_COUNTRY_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.SHI_ERROR_COUNTRY_ID.getRetMsg());
			return;
		}

		CWVAuthUser authUser = getCurrentUser(pack);
		authUser.setCountryId(Integer.parseInt(pb.getCountryId()));
		dao.userDao.updateByPrimaryKeySelective(authUser);
		ret.setRetCode(ReturnCodeMsgEnum.SHI_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SHI_SUCCESS.getRetMsg());
	}

	public void setIconNameBack(FramePacket pack, PSCommon pb, PRetCommon.Builder ret) {
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
			log.warn("UserHelper invokeImageService error...", e);
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
			log.warn("UserHelper getHeadImage error...", e);
		}

	}

	public CWVAuthUser getCurrentUser(FramePacket pack) {
		CWVAuthUser authUser = new CWVAuthUser();
		SubjectModel model = tokenHelper.getUserSub(pack.getExtHead().getSMID());
		authUser.setUserId(model.getUid());
		authUser = dao.userDao.selectByPrimaryKey(authUser);
		return authUser;
	}

	public void sendMsgCode(FramePacket pack, PSSendMsgCode pb, PRetCommon.Builder ret) {
		// 校验类型
		if (msgCodeLoginType.contains(pb.getType()) && !existsPhone(pb.getPhone())) {// 校验登陆者手机号
			ret.setRetCode(ReturnCodeMsgEnum.SMC_ERROR_PHONE.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SMC_ERROR_PHONE.getRetMsg());
			return;
		}

		if (StringUtils.isEmpty(pb.getPhoneCode())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			ret.setRetMsg("手机代码不能为空");
			return;
		}

		if (StringUtils.isEmpty(pb.getType())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			ret.setRetMsg("类型不能为空");
			return;
		}

		if(MsgCodeType.REG.value.equals(pb.getType())) {
			if (StringUtils.isEmpty(pb.getCode())) {
				ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
				ret.setRetMsg("图片验证码不能为空");
				return;
			}
			// 注册验证码
			HashMap<String, String> jsonMap = new HashMap<>();
			jsonMap.put("code", pb.getCode());
			jsonMap = InokeInterfaceHelper.checkCode(jsonMap, sender);

			if (!ReturnCodeMsgEnum.SUCCESS.getRetCode().equals(jsonMap.get("ret_code"))) {
				ret.setRetCode(ReturnCodeMsgEnum.SMC_ERROR_CODE.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.SMC_ERROR_CODE.getRetMsg());
				return;
			}
		}
		HashMap<String, String> jsonMap = new HashMap<>();
		jsonMap.put("phone", pb.getPhone());
		jsonMap.put("country_code", pb.getPhoneCode());
		jsonMap.put("type", pb.getType());

		jsonMap = InokeInterfaceHelper.getMsgCode(jsonMap, sender);

		ret.setRetCode(jsonMap.get("ret_code"));
		ret.setRetMsg(jsonMap.get("ret_msg"));
	}

	/**
	 * 手机号是否存在
	 * 
	 * @param phone
	 * @return
	 */
	private boolean existsPhone(String phone) {
		CWVAuthUserExample example = new CWVAuthUserExample();
		example.createCriteria().andPhoneEqualTo(phone);
		List<Object> list = dao.userDao.selectByExample(example);
		if (list != null && !list.isEmpty())
			return true;
		return false;
	}


	
	/**
	 * 修改密码（需要先登陆）
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void setPwd(FramePacket pack, PSLogin pb, PRetCommon.Builder ret) {

		if(!StringUtils.isEmpty(pb.getPasswordOld())){
			// 1 校验入参
			// 1.1 格式校验
			if (!ValidatorUtil.isPassword(pb.getPasswordOld())) {
				ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
						.setRetMsg(ValidateEnum.PASSWORD.getVerifyMsg());
				return;
			}

			// 1.2查询当前用户

			CWVAuthUser authUser = getCurrentUser(pack);

			if (authUser == null) {
				// TODO 次数限制 +1
				ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("用户未登录");
				return;
			}

			if (!getPwdMd5(pb.getPasswordOld(), authUser.getSalt()).equals(authUser.getPassword())) {
				ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_PWD_OLD.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_PWD_OLD.getRetMsg());
				return;
			}

			// 2 更新密码
			ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
			return ;
		}
		
		// 1 校验入参
		// 1.1 格式校验
		if (!ValidatorUtil.isPassword(pb.getPassword())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
					.setRetMsg(ValidateEnum.PASSWORD.getVerifyMsg());
			return;
		}

		// 1.2查询当前用户

		CWVAuthUser authUser = getCurrentUser(pack);

		if (authUser == null) {
			// TODO 次数限制 +1
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("用户未登录");
			return;
		}

		if (StringUtils.isEmpty(pb.getPhoneVerifyCode())) {
			ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_PHONE_CODE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_PHONE_CODE.getRetMsg());
			return;
		} else {
			HashMap<String, String> jsonMapPhone = new HashMap<>();
			jsonMapPhone.put("phone", authUser.getPhone());
			jsonMapPhone.put("code", pb.getPhoneVerifyCode());
			jsonMapPhone.put("type", MsgCodeType.SPS.value); // 设置登陆密码
			jsonMapPhone = InokeInterfaceHelper.checkMsgCode(jsonMapPhone, sender);

			if (!ReturnCodeMsgEnum.SUCCESS.getRetCode().equals(jsonMapPhone.get("ret_code"))) {
				ret.setRetCode(jsonMapPhone.get("ret_code")).setRetMsg(jsonMapPhone.get("ret_msg"));
				return;
			}
		}

		// 校验重复密码
		String pwdMd5 = getPwdMd5(pb.getPassword(), authUser.getSalt());
		if (authUser.getPassword().equals(pwdMd5)) {
			ret.setRetCode(ReturnCodeMsgEnum.SPS_DUPLICATE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.SPS_DUPLICATE_PWD.getRetMsg());
			return;
		}

		// 2 更新密码
		authUser.setPassword(pwdMd5);
		dao.userDao.updateByPrimaryKeySelective(authUser);

		ret.setRetCode(ReturnCodeMsgEnum.SPS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SPS_SUCCESS.getRetMsg());

	}
	
	public void duplicateInfo(PSRegistry pb, PRetCommon.Builder ret) {

		if (StringUtils.isEmpty(pb.getPhone()) && StringUtils.isEmpty(pb.getUserName())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("手机或者用户名不能为空");
			return;
		}

		if (!StringUtils.isEmpty(pb.getPhone())) {
			CWVAuthUser authUser = getUserByPhone(pb.getPhone());
			if (authUser != null) {
				ret.setRetCode(ReturnCodeMsgEnum.DIS_DUPLICATE_PHONE.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.DIS_DUPLICATE_PHONE.getRetMsg());
			} else {
				ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
			}
		} else if (!StringUtils.isEmpty(pb.getUserName())) {

			CWVAuthUserExample authUserExample = new CWVAuthUserExample();
			authUserExample.createCriteria().andUserNameEqualTo(pb.getUserName());
			Object o = dao.userDao.selectOneByExample(authUserExample);

			if (o != null) {
				ret.setRetCode(ReturnCodeMsgEnum.DIS_DUPLICATE_NAME.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.DIS_DUPLICATE_NAME.getRetMsg());
			} else {
				ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
			}
		}

	}

}
