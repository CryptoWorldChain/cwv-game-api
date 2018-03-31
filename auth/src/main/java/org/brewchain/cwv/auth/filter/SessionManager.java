package org.brewchain.cwv.auth.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.util.jwt.TokenMgr;
import org.fc.zippo.filter.exception.FilterException;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PackHeader;
import onight.tfw.otransio.api.beans.ExtHeader;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.SessionIDGenerator;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
public class SessionManager implements ActorService {

	Map<String, SMSession> sms = new ConcurrentHashMap<>();
	//SessionIDGenerator idGen = new SessionIDGenerator("node1");
	public final static String STR_SESSION_SMID = PackHeader.EXT_IGNORE_RESPONSE + "_smid";
	
	@ActorRequire
	FilterHelper filterHelper;
	
	public SMSession getSMSesion() {

		return null;
	}

	public SMSession setSMSesion(String smid ,SMSession sm) {
		sms.put(smid, sm);
		return sm;
	}
	
	public void rmSMSession(String smid){
		sms.remove(smid);
	}

	public SMSession getSMSesion(String smid) {
		if (StringUtils.isBlank(smid) && !TokenMgr.validateJWT(smid).isSuccess()) {
			throw new FilterException("filter: [smid is not correct]");
		}
		if (StringUtils.isBlank(smid))
		{
			throw new FilterException("filter: [no smid]");
		}
		
		SMSession ret = sms.get(smid);
		
		if(ret==null){
			//如果有smid but得到的session是null  去数据库考察下，哇咔咔
			ret = filterHelper.getDataBaseSession(smid);
		}
		
		return ret;
	}
	
	public SMSession getSMSesion(FramePacket pack) {
//		String smid = (String) pack.getExtHead().get(STR_SESSION_SMID);
		String smid = (String) pack.getExtProp(ExtHeader.SESSIONID);
		if (StringUtils.isBlank(smid) && !SessionIDGenerator.checkSum(smid)) {
			throw new FilterException("filter: [smid is not correct]");
		}
		if (StringUtils.isBlank(smid))
		{
			throw new FilterException("filter: [no smid]");
		}
		
		SMSession ret = sms.get(smid);
		return ret;
	}
	
}
