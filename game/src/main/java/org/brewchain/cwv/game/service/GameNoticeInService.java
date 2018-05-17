package org.brewchain.cwv.game.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSCommand;
import org.brewchain.cwv.service.game.notice.GameNotice.GNPSModule;
import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeIn;
import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeIn;

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
public class GameNoticeInService extends SessionModules<PBGameNoticeIn> {
	
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
	
	@Override
	public String[] getCmds() {
		return new String[] { GNPSCommand.GNI.name() };
	}

	@Override
	public String getModule() {
		return GNPSModule.GNA.name();
	}
	
	enum NoticeTopicEnum{
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
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameNoticeIn pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetGameNoticeIn.Builder ret = PRetGameNoticeIn.newBuilder();
		try{
			noticeIn(pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			log.warn("GameNoticeInService noticeIn  error......",e);
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	/**
	 * 挡板
	 */
	private void noticeIn(PBGameNoticeIn pb,PRetGameNoticeIn.Builder ret){
		Map<String,String> jsonMap = new HashMap<>();
		jsonMap.put("topic", NoticeTopicEnum.NOTICE.value);
		jsonMap.put("type", pb.getNoticeType());
		
		if(StringUtils.isEmpty(pb.getNoticeType()) || !pb.getNoticeType().equals("announcement")) {
			ret.setRetCode("02");
			ret.setRetMsg("公告类型错误");
			return;
		}
		Map<String,String> timeMap = new HashMap<>();
		timeMap.put("start_time", pb.getStartTime());
		timeMap.put("end_time", pb.getEndTime());
		timeMap.put("interval", pb.getCyclePeriod()+"");
		
		Map<String,String> dataMap = new HashMap<>();
		String timeStr = JsonSerializer.formatToString(timeMap);
		dataMap.put("time", timeStr);
		dataMap.put("times", pb.getCount()+"");
		dataMap.put("content", pb.getNoticeContent()+"");
		
		String dataStr = JsonSerializer.formatToString(dataMap);

		jsonMap.put("data", dataStr);
		
		String jsonStr = JsonSerializer.formatToString(jsonMap);
		
		String noticeInUrl = commonHelper.getSysSettingValue("ipfs_msg_in");
		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", noticeInUrl);
		val yearMeasureRet = sender.send(pp,30000);
		Map<String,String> jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), Map.class);
		if(jsonRet.get("errcode").equals("000")){
			ret.setRetCode("01");
			ret.setRetMsg("SUCCESS");
		}else{
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg("FAILD");
		}
		
		
		
//		String res = "";
//		PrintWriter out = null;
//		BufferedReader in = null;
//		try {
//			URL realUrl = new URL(NOTICE_IN_URL);
//			HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
//			conn.setRequestProperty("accept", "*/*");
//			conn.setRequestProperty("connection", "Keep-Alive");
//			conn.setDoOutput(true);
//			conn.setDoInput(true);
//			out = new PrintWriter(conn.getOutputStream());
//			StringBuffer sb = new StringBuffer();
//			int i = 0;
//			out.print(jsonStr);
//			out.flush();
//			int httpStatus = conn.getResponseCode();
//			if (httpStatus == 200) {
//				in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));
//				String line;
//				while ((line = in.readLine()) != null) {
//					res += line;
//				}
//			} else {
//				ret = null;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			ret = null;
//		} finally {
//			try {
//				if (out != null) {
//					out.close();
//				}
//				if (in != null) {
//					in.close();
//				}
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//		}
	}
	
}
