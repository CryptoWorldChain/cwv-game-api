package org.brewchain.cwv.auth.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.dao.Dao;
import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.filter.SessionFilter;
import org.brewchain.cwv.auth.filter.SessionManager;
import org.brewchain.cwv.auth.util.jwt.CheckResult;
import org.brewchain.cwv.auth.util.jwt.Constant;
import org.brewchain.cwv.auth.util.jwt.GsonUtil;
import org.brewchain.cwv.auth.util.jwt.SubjectModel;
import org.brewchain.cwv.auth.util.jwt.TokenMgr;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthRefreshToken;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthRefreshTokenExample;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.fc.hzq.service.sys.Token.PRetRefreshToken;
import org.fc.hzq.service.sys.Token.PRetRefreshToken.Builder;
import org.fc.hzq.service.sys.Token.PSAccessToken;
import org.fc.hzq.service.sys.Token.PSRefreshToken;

import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.beans.ExtHeader;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.UUIDGenerator;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
/**
 * token service层
 * 
 * @author Moon
 * @date 2018-03-30
 */
@Instantiate(name = "Token_Helper")
public class TokenHelper implements ActorService {

	@ActorRequire(name = "Dao")
	Dao dao;

	@ActorRequire(name = "Session_Manager")
	SessionManager sessionManager;

	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	/**
	 * 根据用户信息 设置token
	 * 
	 * @param pack
	 * @param user
	 */
	public void tokenSetting(FramePacket pack, CWVAuthUser user, String refreshToken) {

		//创建access_token
		SubjectModel userSub = new SubjectModel(user.getUserId(), user.getNickName());
		String smid = TokenMgr.createJWT(Constant.JWT_ID, TokenMgr.generalSubject(userSub), Constant.JWT_TTL);
		pack.putHeader(ExtHeader.SESSIONID, smid);
		pack.putHeader(ExtHeader.PACK_SESSION, smid);
		ExtHeader extHeader = pack.getExtHead();
		
		extHeader.append(SessionFilter.STR_SESSION_SMID, smid);
		extHeader.append(SessionFilter.STR_RECEIVE_TIME, new Date());
		//设置refresh_token
		ExtHeader.addCookie(pack.getHttpServerletResponse(), "refresh_token", refreshToken);
		
		//删除 redis access_token
		destroyAccessToken(smid);
		//删除 旧 refresh_token
		destroyRefreshToken(user.getUserId());
		//插入新的refresh_token
		insertRefreshToken(user.getUserId(),refreshToken);

	}
	/**
	 * 根据用户id删除原有token
	 * @param userId
	 */
	private void destroyRefreshToken(int userId){

		CWVAuthRefreshTokenExample example = new CWVAuthRefreshTokenExample();
		CWVAuthRefreshTokenExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userId);
		dao.tokenDao.deleteByExample(example);
	}

	/**
	 * 插入新的token
	 * @param userId
	 * @param refreshToken
	 */
	private void insertRefreshToken( int userId, String refreshToken) {
		CWVAuthRefreshToken authRefreshToken = new CWVAuthRefreshToken();
		authRefreshToken.setRefreshToken(refreshToken);
		authRefreshToken.setExpires(DateUtils.addMilliseconds(new Date(), (int) Constant.JWT_TTL_REFRESH));
		authRefreshToken.setUserId(userId);
		dao.tokenDao.insert(authRefreshToken);
	}
	/**
	 * 获取
	 * 
	 * @param pack
	 * @return
	 */
	public SubjectModel getUserSub(String token) {
		CheckResult checkResult = TokenMgr.validateJWT(token);
		if(checkResult.isSuccess())
			return getUserSubByCheck(checkResult);
		else
			return null;
	}
	
	/**
	 * 根据校验结果查询 model
	 * @param checkResult
	 * @return
	 */
	private SubjectModel getUserSubByCheck(CheckResult checkResult) {
		Claims claims = checkResult.getClaims();
		return GsonUtil.jsonStrToObject(claims.getSubject(), SubjectModel.class);
	}

	public void refreshToken(FramePacket pack, PSRefreshToken pb, PRetRefreshToken.Builder ret) {

		// 校验refresh_token
		CWVAuthRefreshTokenExample example = new CWVAuthRefreshTokenExample();
		CWVAuthRefreshTokenExample.Criteria criteria = example.createCriteria();
		criteria.andRefreshTokenEqualTo(pb.getRefreshToken());
		List<Object> list = dao.tokenDao.selectByExample(example);

		if (list == null || list.isEmpty()) {
			ret.setRetCode(ReturnCodeMsgEnum.RTS_ERROR_TOKEN.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.RTS_ERROR_TOKEN.getRetMsg());
			return;
		}

		CWVAuthRefreshToken oldRefresh = (CWVAuthRefreshToken) list.get(0);
		if(oldRefresh.getExpires().compareTo(new Date()) <0) {
			ret.setRetCode(ReturnCodeMsgEnum.RTS_ERROR_TOKEN.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.RTS_ERROR_TOKEN.getRetMsg());
			return;
		}
		
		oldRefresh.setExpires(DateUtils.addMilliseconds(new Date(), (int) Constant.JWT_TTL_REFRESH));
		dao.tokenDao.updateByPrimaryKeySelective(oldRefresh);
		
		int userId = oldRefresh.getUserId();

		//验证通过
			
			//设置token 并删除原有token
		CWVAuthUser authUser = new CWVAuthUser();
		authUser.setUserId(userId);
		authUser = dao.userDao.selectByPrimaryKey(authUser);
		String refreshToken = UUIDGenerator.generate();
		this.tokenSetting(pack, authUser,refreshToken);
		
		// 返回数据
		ret.setAccessToken(pack.getExtHead().getSMID());
		ret.setRefreshToken(refreshToken);
		ret.setTokenType(Constant.TOKEN_TYPE);
		ret.setExpiresIn((Constant.JWT_TTL / 1000) + "");

		ret.setRetCode(ReturnCodeMsgEnum.RTS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.RTS_SUCCESS.getRetMsg());

	}

	/**
	 * 删除access_token
	 * 
	 * @param smid
	 */
	public void destroyAccessToken(String smid) {
		// TODO Auto-generated method stub
		// redis 删除
	}

	/**
	 * 校验access_token
	 * 
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void accessToken(FramePacket pack, PSAccessToken pb, Builder ret) {
		// 校验是否有效
		CheckResult resultAccess = TokenMgr.validateJWT(pb.getAccessToken());
		if (!resultAccess.isSuccess()) {
			ret.setRetCode(ReturnCodeMsgEnum.ATS_ERROR_TOKEN.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.ATS_ERROR_TOKEN.getRetMsg());
		}

		// 查询redis是否存在
		SubjectModel model = this.getUserSubByCheck(resultAccess);
		String redisToken = redisAccessToken(model.getUid());
		if (!pb.getAccessToken().equals(redisToken)) {
			ret.setRetCode(ReturnCodeMsgEnum.ATS_ERROR_TOKEN.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.ATS_ERROR_TOKEN.getRetMsg());
			return;
		}

		ret.setRetCode(ReturnCodeMsgEnum.ATS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.ATS_SUCCESS.getRetMsg());

	}

	/**
	 * 查询redis缓存的access_token
	 * 
	 * @param uid
	 *            用户ID
	 * @return
	 */
	private String redisAccessToken(int uid) {
		// TODO Auto-generated method stub
		return null;
	}

}
