package org.brewchain.cwv.game.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSCommand;
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSModule;
import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeOut;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut.PRetNoticeOut;

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

@NActorProvider
@Slf4j
@Data
public class GameNoticeOutService extends SessionModules<PBGameNoticeOut> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire(name="Daos")
	Daos dao;
	
	@ActorRequire(name = "http", scope = "global")
	IPacketSender sender;
	
	private final String NOTICE_OUT_URL = "http://54.169.102.90:80/api/msg/subcription";
	
	public String[] getCmds() {
		return new String[] { GNPSCommand.GNO.name() };
	}

	@Override
	public String getModule() {
		return GNPSModule.GNA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameNoticeOut pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetGameNoticeOut.Builder ret = PRetGameNoticeOut.newBuilder();
		try{
			checkParam(pb);
			baffle(pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	
	
	/**
	 * 挡板
	 */
	private void baffle(PBGameNoticeOut pb,PRetGameNoticeOut.Builder ret){
		
		StringBuffer url = new StringBuffer();
		url.append(NOTICE_OUT_URL);
		url.append("?");
		if(StringUtils.isNotBlank(pb.getUserId())){
			url.append("userid="+pb.getUserId());
			url.append("&");
		}
		url.append("topic="+pb.getNoticeType());
		url.append("&");
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		
		url.append("pagesize="+page.getLimit());
		url.append("&");
		url.append("pageidx="+page.getOffset());
		url.append("&");
		url.append("pagenum="+pb.getPageNum());
		
		FramePacket pp = PacketHelper.buildUrlForGet(url.toString());
		val yearMeasureRet = sender.send(pp,30000);
		Map<String, Object> jsonRet;
		try {
			jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody(),"utf-8"), Map.class);
			if(jsonRet.get("errcode").equals("000")){
				ret.setRetCode("01");
				ret.setRetMsg("SUCCESS");
				List<String> chunk =  (List<String>) jsonRet.get("data");
				for(String jsonChunk : chunk){
					Map<String,String> coun = JsonSerializer.getInstance().deserialize(jsonChunk, Map.class);
					PRetNoticeOut.Builder noticeOut = PRetNoticeOut.newBuilder();
					noticeOut.setNoticeType(coun.get("topic"));
					
					try{
						Map<String,String> content = JsonSerializer.getInstance().deserialize(coun.get("content"), Map.class);
						noticeOut.setNoticeContent(content.get("notice_content"));
//						noticeOut.setNoticeId(content.get("notice_id"));
						if(content.get("count")!=null)
							noticeOut.setCount(Integer.parseInt(content.get("count")));
						if(noticeOut.getNoticeType().equals("announcement")) {
							noticeOut.setStartTime(content.get("start_time"));
							noticeOut.setEndTime(content.get("end_time"));
							noticeOut.setCyclePeriod(Integer.parseInt(content.get("cycle_period")));
						}
						ret.addNotices(noticeOut);
					}catch (Exception e){
						noticeOut.setNoticeContent(coun.get("content"));
						ret.addNotices(noticeOut);
						log.warn("GameNoticeOutService baffle error....",e);
						continue;
						
					}
					
					
				}
			}else{
				ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
				ret.setRetMsg("FAILD");
			}
		} catch (UnsupportedEncodingException e) {
			log.warn("GameNoticeOutService baffle error....",e);
		}
		
	}
	
	private void checkParam(PBGameNoticeOut pb){
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
		}else{
			if(!pb.getNoticeType().equals("announcement")){
				throw new IllegalArgumentException("目前公告类型只支持announcement类型");
			}
		}
	}
	
}
