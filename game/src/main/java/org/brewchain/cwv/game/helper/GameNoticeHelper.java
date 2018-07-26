package org.brewchain.cwv.game.helper;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeIn;
import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeOut;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeIn;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeIn.Builder;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut.PRetNoticeOut;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.JsonSerializer;

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
@Instantiate(name="Game_Notice_Helper")
public class GameNoticeHelper implements ActorService {

	@ActorRequire(name="Daos")
	Daos dao;
	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;

	@ActorRequire(name = "http", scope = "global")
	IPacketSender sender;
	
	@ActorRequire(name = "User_Helper", scope = "global")
	UserHelper userHelper;
	
	public enum NoticeTopicEnum{
		NOTICE("notice"),
		TRADE("trade"),
		AUCTION("auction");
		private String value;
		NoticeTopicEnum(String value){
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		
	}
	
	public enum NoticeTypeEnum{
		ALL("0"),//无此类型，用于查询所有
		OFFICE("1"),
		USER("2");
		private String value;
		NoticeTypeEnum(String value){
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		
	}
	
	public enum NoticeTradeTypeEnum{
		TYPICAL_GET("0"),//无此类型，用于查询所有
		SELL("1"),
		FUNCTIONAL_GET("2"),
		BID("3");
		private String value;
		NoticeTradeTypeEnum(String value){
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		
	}
	
	public static String NOTICE_TRADE_PERIOD = "notice_trade_period";
	
	public static String NOTICE_TRADE_COUNT = "notice_trade_count";
	
	/**
	 * 新增官方公告
	 */
	public void noticeIn(FramePacket pack, PBGameNoticeIn pb,PRetGameNoticeIn.Builder ret){
		Map<String,String> jsonRet = noticeCreate(pb.getNoticeType(),pb.getUserId(), pb.getStartTime(), pb.getEndTime(), pb.getCyclePeriod()+"", pb.getCount()+"", pb.getNoticeContent());
		if(jsonRet.get("errcode").equals("000")){
			ret.setRetCode("01");
			ret.setRetMsg("SUCCESS");
		}else{
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg("FAILD");
		}
		
	}
	
	public Map<String,String> noticeCreate(String noticeType, String userId, String startTime,String endTime,String cyclePeriod,String count,String noticeContent) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode jsonMap = mapper.createObjectNode();
		jsonMap.put("topic", NoticeTopicEnum.NOTICE.value);
		jsonMap.put("type", noticeType);
		
		
		
		ObjectNode timeMap = mapper.createObjectNode();
		timeMap.put("starttime", startTime);
		timeMap.put("endtime", endTime);
		timeMap.put("interval", cyclePeriod+"");
		
		ObjectNode dataMap = mapper.createObjectNode();
		dataMap.put("time", timeMap);
		dataMap.put("times", count);
		dataMap.put("content", noticeContent);
		dataMap.put("user_id", userId);
		

		jsonMap.put("data", dataMap);
		
		String jsonStr = JsonSerializer.formatToString(jsonMap);
		
		String noticeInUrl = commonHelper.getSysSettingValue("ipfs_msg_in");
		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", noticeInUrl);
		val yearMeasureRet = sender.send(pp,30000);
		return JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), Map.class);
	}
			

	/**
	 * 查询公告
	 */
	public void noticeOut(FramePacket pack, PBGameNoticeOut pb,PRetGameNoticeOut.Builder ret){

		//校验
		if(StringUtils.isBlank(pb.getPageIndex())){
			throw new IllegalArgumentException("页索引不能为空");
		}
		if(StringUtils.isBlank(pb.getPageSize())){
			throw new IllegalArgumentException("页大小不能为空");
		}
		if(StringUtils.isBlank(pb.getPageNum())){
			throw new IllegalArgumentException("页数不能为空");
		}
		if(StringUtils.isBlank(pb.getNoticeType())){
			throw new IllegalArgumentException("消息类型不能为空");
		}
		
		String noticeOutUrl = commonHelper.getSysSettingValue("ipfs_msg_out");
		StringBuffer url = new StringBuffer();
		url.append(noticeOutUrl);
		url.append("?");
		url.append("type="+pb.getNoticeType());
		String userId = "";
		if(NoticeTypeEnum.USER.getValue().equals(pb.getNoticeType())) {
			CWVAuthUser authUser = userHelper.getCurrentUser(pack);
			userId = authUser.getUserId().toString();
			url.append("&");
			url.append("user_id="+userId);
		}
		
		url.append("&");
//		url.append("topic="+pb.getNoticeTopic());
		url.append("topic="+NoticeTopicEnum.NOTICE.getValue());
		url.append("&");
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		
		url.append("pagesize="+page.getLimit());
		url.append("&");
		url.append("pageidx="+page.getOffset());
		url.append("&");
		url.append("ispage="+pb.getPageNum());
		
		FramePacket pp = PacketHelper.buildUrlForGet(url.toString());
		val yearMeasureRet = sender.send(pp,30000);
		Map<String, Object> jsonRet;
		try {
			jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody(),"utf-8"), Map.class);
			if(jsonRet.get("errcode").equals("000")){
				ret.setRetCode("01");
				ret.setRetMsg("SUCCESS");
				List<Map<String,Object>> chunk =  (List<Map<String,Object>>) jsonRet.get("data");
				for(Map<String,Object> coun : chunk){
					PRetNoticeOut.Builder noticeOut = PRetNoticeOut.newBuilder();
						noticeOut.setNoticeContent(coun.get("content").toString());
//						noticeOut.setNoticeId(content.get("notice_id"));
						if(coun.get("starttime") != null)
							noticeOut.setStartTime(coun.get("starttime").toString());
						if(coun.get("endtime") != null)
							noticeOut.setEndTime(coun.get("endtime").toString());
						if(coun.get("times") != null)
							noticeOut.setCount((Integer)coun.get("times"));
						if(coun.get("user_id") != null)
							noticeOut.setUserId(coun.get("user_id").toString());
						if(coun.get("publicity") != null)
							noticeOut.setCyclePeriod((Integer)coun.get("publicity"));
						if(coun.get("interval") != null)
							noticeOut.setCyclePeriod((Integer)coun.get("interval"));
						ret.addNotices(noticeOut);
					
				}
			}else{
				ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
				ret.setRetMsg("FAILD");
			}
		} catch (UnsupportedEncodingException e) {
			log.warn("GameNoticeOutService baffle error....",e);
		}
		
	}

	public String noticeTradeTpl(String type, String nickName, String propertyName, String operate) {

		if(NoticeTradeTypeEnum.TYPICAL_GET.getValue().equals(type)) {
			return "恭喜 “"+nickName+"” 通过 “"+operate+"” 获得标志性房产 “" +propertyName +"” ";
		}else if(NoticeTradeTypeEnum.SELL.getValue().equals(type)){
			return " “"+nickName+"” 开始出售 “" +propertyName +"” ";
		}else if(NoticeTradeTypeEnum.FUNCTIONAL_GET.getValue().equals(type)){
			return "恭喜 “"+nickName+"” 获得功能性房产 “" +propertyName +"” ";
		}else if(NoticeTradeTypeEnum.BID.getValue().equals(type)){
			return " “"+propertyName+"” 已开始公开竞拍，大家可以前往交易所查看 ";
		}
		
		return null;
	}

	public void loginNoticeIn(FramePacket pack, PBGameNoticeIn pb, Builder ret) {

		if(StringUtils.isEmpty(pb.getNoticeContent())){
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("公告内容不能为空");
			return;
		}
		commonHelper.updateSysSettingValue("sys_notice", pb.getNoticeContent());
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		return;
	}
	
	public void loginNoticeOut(FramePacket pack, PBGameNoticeOut pb, PRetGameNoticeOut.Builder ret) {

		PRetNoticeOut.Builder noticeOut = PRetNoticeOut.newBuilder();
		noticeOut.setNoticeContent(commonHelper.getSysSettingValue("sys_notice"));
		ret.addNotices(noticeOut);
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		return;
	}


}
