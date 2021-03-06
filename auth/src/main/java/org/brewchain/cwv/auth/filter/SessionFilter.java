package org.brewchain.cwv.auth.filter;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.dao.Dao;
import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.auth.util.jwt.CheckResult;
import org.brewchain.cwv.auth.util.jwt.GsonUtil;
import org.brewchain.cwv.auth.util.jwt.SubjectModel;
import org.brewchain.cwv.auth.util.jwt.TokenMgr;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysUrlResource;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysUrlResourceExample;
import org.fc.hzq.service.sys.User.PRetCommon;
import org.fc.zippo.filter.FilterConfig;
import org.fc.zippo.filter.exception.FilterException;

import io.jsonwebtoken.Claims;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.ActWrapper;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PackHeader;
import onight.tfw.otransio.api.PacketFilter;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.SimplePacketFilter;
import onight.tfw.otransio.api.beans.ExtHeader;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.SessionIDGenerator;

@iPojoBean
@Provides(specifications = { PacketFilter.class, ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
public class SessionFilter extends SimplePacketFilter implements PacketFilter, ActorService {
	public final static String STR_RECEIVE_TIME = PackHeader.EXT_IGNORE_RESPONSE + "_recievetime";
	public final static String STR_SESSION_SMID = PackHeader.EXT_IGNORE_RESPONSE + "_smid";
	public final static String STR_REQUEST_METHOD = PackHeader.EXT_IGNORE_RESPONSE + "_method";
	public final static String STR_IS_SESSION = PackHeader.EXT_IGNORE_RESPONSE + "_issession";
	public final static int SES_TIMTOUT = 60 * 20;// session超时时间
	public static HashSet<String> noLoginUrlList ; // 可配置
	public static HashMap<String,String> userMap = new HashMap<String,String>();
	public final static boolean ischeckrole = false; // 测试使用，是否走url验证,可配置，临时添加到代码内

	@ActorRequire(name="Session_Manager")
	SessionManager sm;
	@ActorRequire(name="Filter_Helper")
	FilterHelper filterHelper;

	@Override
	public void destroy(FilterConfig filterConfig) throws FilterException {
		super.destroy(filterConfig);
	}

	@Override
	public int getPriority() {
		return 10;
	}

	@Override
	public void init(FilterConfig filterConfig) throws FilterException {
		super.init(filterConfig);
	}
	
	private void initLoginUrlList() {
		if(noLoginUrlList == null || noLoginUrlList.isEmpty()) {
			noLoginUrlList = new HashSet<>();
			CWVSysUrlResourceExample example = new CWVSysUrlResourceExample();
			example.createCriteria().andUrlResourceTypeEqualTo(2)
			.andStatusEqualTo("1");
			List<Object> list = filterHelper.dao.getUrlResouceDao().selectByExample(example);
			for(Object o : list){
				CWVSysUrlResource url = (CWVSysUrlResource) o;
				noLoginUrlList.add(url.getUrlResourcePath());
			}
		}
		
	}

	@Override
	public String getSimpleName() {
		return "session_filter";
	}

	@Override
	public boolean postRoute(ActWrapper actor, FramePacket pack, CompleteHandler handler) throws FilterException {
		String method = (String) pack.getExtHead().get(STR_REQUEST_METHOD);
		// String smid = (String) pack.getExtHead().get(SESSIONID);
		String smid = (String) pack.getExtProp(ExtHeader.SESSIONID);
		if (!StringUtils.isBlank(smid)) {

			filterHelper.updateSession(smid);
		}
		// 暂时定义GET 通过不做日志处理
		// if ("GET".equals(method)) {
		// return true;
		// }
		// 判断是否走session
		String issession = (String) pack.getExtHead().get(STR_IS_SESSION);
		if ("0".equals(issession)) {
			return true;
		} else {
			// String smid = (String) pack.getExtHead().get(STR_SESSION_SMID);
			SMSession smsession = sm.getSMSesion(smid);
			if (smsession == null) {
				throw new FilterException("filter: [no postroute session");
			}

			// TXTpsUser usrInfo = smsession.getTxtpsuser();

			Date reqTime = (Date) pack.getExtHead().get(STR_RECEIVE_TIME);
			String pathUrl = pack.getHttpServerletRequest().getRequestURI();

			// String logOut = "N";
			// List<Map<String,Object>> dataList = smsession.getDatarolemap();
			// if(dataList!=null&&dataList.size()>0){
			// for(int i=0;i<dataList.size();i++){
			// Map<String,Object> map = dataList.get(i);
			// if(pathUrl.equals((String)map.get("urlpath"))){
			// logOut = (String)map.get("urllogout");
			// }
			// }
			// }

			// GASSysUrlResource dataresource =
			// smsession.getDataresourcemap().get(pathUrl);
			String ip = pack.getExtStrProp(PackHeader.PEER_IP);
			// 操作日志记录
			// if (usrInfo != null) {
			// TXSysAccessLog logRecord = new TXSysAccessLog();
			// logRecord.setAccessLogId(UUIDGenerator.generate());
			// logRecord.setUserId(usrInfo.getTxTpsUserId());
			// logRecord.setUserIp(ip);
			// logRecord.setPbAction(pathUrl);
			// logRecord.setProxyTime(reqTime);
			// logRecord.setResTime(new Date());
			// logRecord.setCostMs(Integer.parseInt(filterHelper.getCostMs(reqTime)));
			// dao.getTxSysAccessLog().insert(logRecord);
			// }
		}

		return super.postRoute(actor, pack, handler);
	}

	SessionIDGenerator idGen = new SessionIDGenerator("node1");

	@Override
	public boolean preRoute(ActWrapper actor, FramePacket pack, CompleteHandler handler) throws FilterException {
		initLoginUrlList();
		String method = pack.getHttpServerletRequest().getMethod();
		String pathUrl = pack.getHttpServerletRequest().getRequestURI();
//		pack.getHttpServerletResponse().setHeader("Access-Control-Allow-Origin", "*");
		pack.getExtHead().append(STR_REQUEST_METHOD, method);

		// 首先判断是否登录请求,如果是登录请求直接返回，进入登录流程
		String url = pathUrl.substring(pathUrl.lastIndexOf("/", pathUrl.lastIndexOf("/")-1), pathUrl.length());

		if (noLoginUrlList.contains(url)) {
			pack.getExtHead().append(STR_IS_SESSION, "0");
			return true;
		}

		// TXSysUrlResourceExample resourceExample = new
		// TXSysUrlResourceExample();
		// resourceExample.createCriteria().andUrlResourcePathEqualTo(pathUrl);
		// List<Object> listRes =
		// dao.txSysUrlResourceDao.selectByExample(resourceExample);
		// if (listRes.size() > 0) {
		// TXSysUrlResource resourceRecord = (TXSysUrlResource) listRes.get(0);
		// int resType = resourceRecord.getUrlResourceType();
		// if (SYSUrlResourceTypeEnum.不需要登录.getValue().equals(resType + "")) {
		// log.debug("pathUrl 请求不需要登录");
		// pack.getExtHead().append(STR_IS_SESSION, "0");
		// return true;
		// }
		// } else {
		// log.debug(" [" + pathUrl + "]==无该访问权限的定义");
		// throw new FilterException("filter: [no '"+pathUrl+"' datarole in
		// system]");
		// }

		String smid = (String) pack.getExtProp(ExtHeader.SESSIONID);
		if (StringUtils.isBlank(smid)) {
			pack.getExtHead().append(STR_IS_SESSION, "0");
			// 无smid 就是没有session，判断当前请求是否走session块验证，即是否走filter
//			throw new FilterException("filter: [this request no session...]");
			PRetCommon.Builder ret = PRetCommon.newBuilder();
			ret.setRetCode(ReturnCodeMsgEnum.FILTER_NO_LOGIN.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.FILTER_NO_LOGIN.getRetMsg());
			// 返回给客户端
			handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
			return false ;
		}

		// 有session，获取当前session
		// SMSession smsession = filterHelper.sessionLogic(smid);
		CheckResult checkResult = TokenMgr.validateJWT(smid);

		if (!checkResult.isSuccess()) {
//			throw new FilterException("filter: [" + checkResult.getMsg() + "]");
			PRetCommon.Builder ret = PRetCommon.newBuilder();
			ret.setRetCode(ReturnCodeMsgEnum.FILTER_CHECK_LOGIN_ERROR.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.FILTER_CHECK_LOGIN_ERROR.getRetMsg());
			// 返回给客户端
			handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
		
			return false ;
		}

		Claims claims = checkResult.getClaims();
		SubjectModel usrInfo = GsonUtil.jsonStrToObject(claims.getSubject(), SubjectModel.class);
		if(!userMap.containsKey(usrInfo.getUid()+"") || !userMap.get(usrInfo.getUid()+"").equals(smid)){
//			throw new FilterException("filter: [" + checkResult.getMsg() + "]");
		
			PRetCommon.Builder ret = PRetCommon.newBuilder();
			ret.setRetCode(ReturnCodeMsgEnum.FILTER_CHECK_LOGIN_ERROR.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.FILTER_CHECK_LOGIN_ERROR.getRetMsg());
			// 返回给客户端
			handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
			return false ;
		}
		pack.getExtHead().append(STR_IS_SESSION, "1");
		pack.getExtHead().append(STR_SESSION_SMID, smid);
		pack.getExtHead().append(STR_RECEIVE_TIME, new Date());

		log.info(" 用户[" + usrInfo.getUty() + "]-访问[" + pathUrl + "] ");

		if (!ischeckrole) {
			log.debug("不判断访问权限");
			return true;
		}
		return true;
		// 开始判断是否有访问权限
		// List<Map<String,Object>> roleList = smsession.getDatarolemap();
		// if(roleList!=null&&roleList.size()>0){
		// for(int i=0;i<roleList.size();i++){
		// Map<String,Object> map = roleList.get(i);
		// if(pathUrl.equals((String)map.get("urlpath"))&&((String)map.get("urlmethod")).contains(method)){
		// log.debug("【"+pathUrl+"】通过filter权限验证");
		// return true;
		// }
		// }
		// throw new FilterException("filter: [request limit '"+pathUrl+"'
		// ,'"+method+"']");
		// }else{
		// throw new FilterException("filter: [request limit '"+pathUrl+"' ]");
		// }

	}
}