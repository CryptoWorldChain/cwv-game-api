package org.brewchain.cwv.auth.filter;

import java.util.Date;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.dao.Dao;
import org.fc.zippo.filter.exception.FilterException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;


@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
@Instantiate(name="Filter_Helper")
public class FilterHelper implements  ActorService{
	
	public final static int SES_TIMTOUT = 60*20;//session超时时间
	
	
	@ActorRequire(name="Session_Manager")
	SessionManager sm;
	@ActorRequire(name="Dao")
	Dao dao;
	
	//防止相互引用死循环
	@Override
	public String toString() {
		return "filterservice:";
	}
	public void updateSession(String smid){
		// 更新session 表---update 时间
//		TXSysOnlineLog onlineRecord = new TXSysOnlineLog();
//		onlineRecord.setUpdateTime(new Date());
//		TXSysOnlineLogExample onlineExample = new TXSysOnlineLogExample();
//		onlineExample.createCriteria().andSessionIdEqualTo(smid);
//		dao.txSysOnlineLogDao.updateByExampleSelective(onlineRecord, onlineExample);
	}

	public SMSession sessionLogic(String smid) {
		
		SMSession session = sm.getSMSesion(smid);
		
		if (session != null) {
			Date lasttime = session.getLastTime();
			checkTimeout(lasttime);
			session.setLastTime(new Date());
			sm.setSMSesion(smid, session);
			return session;
		}
		
		return session;
	}
	public SMSession getDataBaseSession(String smid){
		//当前节点没有session。查询数据库session状态
//		TXSysOnlineLogExample onlineExample = new TXSysOnlineLogExample();
//		TXSysOnlineLogExample.Criteria criteria = onlineExample.createCriteria();
//		criteria.andSessionIdEqualTo(smid);
//		criteria.andStatusEqualTo("1");
//		List<Object> sessionList = dao.txSysOnlineLogDao.selectByExample(onlineExample);
//		//数据库中也没有session信息
//		if (sessionList.size() == 0) {
//			throw new FilterException("filter: [no session]");
//		}
		
		//数据库有session信息
//		TXSysOnlineLog sessionLog = (TXSysOnlineLog) sessionList.get(0);
		// 其他节点有该session，读取数据库，将session其他属性重新赋值
		SMSession smsession =  new SMSession();
		//赋值userinfo
//		String sessionuserId = sessionLog.getUserId();
//		TXTpsUserKey userKey = new TXTpsUserKey();
//		userKey.setTxTpsUserId(sessionuserId);
//		TXTpsUser userRecord = dao.txTpsUserDao.selectByPrimaryKey(userKey);
//		smsession.setTxtpsuser(userRecord);
		
//		//赋值logintime
//		smsession.setLoginTime(sessionLog.getLoginTime());
//				
//		//赋值lasttime
//		checkTimeout(sessionLog.getModifiedTime());
		smsession.setLastTime(new Date());
		
		sm.setSMSesion(smid, smsession);
		SMSession session = sm.getSMSesion(smid);
		return session;
	}
	public String getCostMs(Date date) {
		return new Date().getTime() - date.getTime() + "";
	}
	public void checkTimeout(Date temtime){
		Date nowtime =  new Date();
		int time = (int)(nowtime.getTime() - temtime.getTime())/1000;
		if(time>SES_TIMTOUT){
			throw new FilterException("filter: [session timeout...]");
		};
	}
//	public List<Map<String,Object>> getDataRolemap(String sessionuserId){
//		//map key{urlpath,urltype,urllogout,urlmethod}
//		List<Map<String,Object>> listmap = new ArrayList<Map<String,Object>>();
//		GASSysUserRoleExample userroleExample = new GASSysUserRoleExample();
//		userroleExample.createCriteria().andUserIdEqualTo(sessionuserId).andStatusEqualTo("1");
//		List<Object> userrolelist = daos.gasSysUserRoleDao.selectByExample(userroleExample);
//		if(userrolelist.size()>0){
//			List<String> roleList =  new ArrayList<String>();
//			for(Object obj : userrolelist){
//				GASSysUserRole userrolerecord = (GASSysUserRole)obj;
//				roleList.add(userrolerecord.getRoleId());
//			}
//			
//			GASSysRoleUrlResourceExample resourceExample = new GASSysRoleUrlResourceExample();
//			resourceExample.createCriteria().andRoleIdIn(roleList).andStatusEqualTo("1");
//			List<Object> resourceList = daos.gasSysRoleUrlResourceDao.selectByExample(resourceExample);
//			if(resourceList.size()>0){
//				for(Object objres : resourceList){
//					GASSysRoleUrlResource resourceRecord = (GASSysRoleUrlResource)objres;
//					String resourceId = resourceRecord.getUrlResourceId();
//					GASSysUrlResourceKey key =  new GASSysUrlResourceKey();
//					key.setUrlResourceId(resourceId);
//					GASSysUrlResource urlresourceRecord =  daos.gasSysUrlResourceDao.selectByPrimaryKey(key);
//					if(urlresourceRecord!=null){
//						urlresourceRecord.getUrlLogOut();
//						Map<String,Object> map = new HashMap<String, Object>();
//						map.put("urlpath", urlresourceRecord.getUrlResourcePath());
//						map.put("urltype", urlresourceRecord.getUrlResourceType()+"");
//						map.put("urllogout", urlresourceRecord.getUrlLogOut());
//						map.put("urlmethod", resourceRecord.getUrlMethod());
//						listmap.add(map);
//					}
// 				}
//			}
//		}
//		return listmap;
//	}
	
}
