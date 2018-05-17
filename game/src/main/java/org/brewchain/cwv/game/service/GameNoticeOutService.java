package org.brewchain.cwv.game.service;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.cwv.game.service.GameNoticeInService.NoticeTopicEnum;
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
	@ActorRequire(name = "Common_Helper")
	CommonHelper commonHelper;
	
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
		
		String noticeOutUrl = commonHelper.getSysSettingValue("ipfs_msg_out");
		StringBuffer url = new StringBuffer();
		url.append(noticeOutUrl);
		url.append("?");
		url.append("type="+pb.getNoticeType());
		url.append("&");
		url.append("topic="+pb.getNoticeTopic());
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
				List<String> chunk =  (List<String>) jsonRet.get("data");
				for(String jsonChunk : chunk){
					Map<String,String> coun = JsonSerializer.getInstance().deserialize(jsonChunk, Map.class);
					PRetNoticeOut.Builder noticeOut = PRetNoticeOut.newBuilder();
					noticeOut.setNoticeTopic(coun.get("topic"));
					noticeOut.setNoticeType(coun.get("type"));
						Map<String,String> data = JsonSerializer.getInstance().deserialize(coun.get("data"), Map.class);
						noticeOut.setNoticeContent(data.get("content"));
//						noticeOut.setNoticeId(content.get("notice_id"));
						Map<String,String> time = JsonSerializer.getInstance().deserialize(coun.get("time"), Map.class);
						if(noticeOut.getNoticeTopic().equals(NoticeTopicEnum.NOTICE.getValue())) {
							noticeOut.setStartTime(time.get("start_time"));
							noticeOut.setEndTime(time.get("end_time"));
							noticeOut.setCount(Integer.parseInt(time.get("times")));
							noticeOut.setCyclePeriod(Integer.parseInt(data.get("interval")));
						
						}else if(noticeOut.getNoticeTopic().equals(NoticeTopicEnum.AUCTION.getValue())){
							noticeOut.setStartTime(time.get("start_time"));
							noticeOut.setEndTime(time.get("end_time"));
							noticeOut.setCyclePeriod(Integer.parseInt(time.get("publicity")));
						
						}
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
