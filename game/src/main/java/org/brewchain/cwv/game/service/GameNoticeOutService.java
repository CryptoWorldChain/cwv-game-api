//package org.brewchain.cwv.game.service;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.apache.commons.lang3.StringUtils;
//import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
//import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample;
//import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample.Criteria;
//import org.brewchain.cwv.game.dao.Daos;
//import org.brewchain.cwv.game.util.PageUtil;
//import org.brewchain.cwv.service.game.Game.PBGameCountry;
//import org.brewchain.cwv.service.game.Game.PRetRefGameCountry;
//import org.brewchain.cwv.service.game.Game.PTPSCommand;
//import org.brewchain.cwv.service.game.Game.PTPSModule;
//import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;
//import org.brewchain.cwv.service.game.notice.GameNotice.GNPSCommand;
//import org.brewchain.cwv.service.game.notice.GameNotice.GNPSModule;
//import org.brewchain.cwv.service.game.notice.GameNotice.PBGameNoticeOut;
//import org.brewchain.cwv.service.game.notice.GameNotice.PRetGameNoticeOut;
//
//import lombok.Data;
//import lombok.val;
//import lombok.extern.slf4j.Slf4j;
//import onight.oapi.scala.commons.SessionModules;
//import onight.osgi.annotation.NActorProvider;
//import onight.tfw.async.CompleteHandler;
//import onight.tfw.ntrans.api.annotation.ActorRequire;
//import onight.tfw.otransio.api.IPacketSender;
//import onight.tfw.otransio.api.PacketHelper;
//import onight.tfw.otransio.api.beans.FramePacket;
//import onight.tfw.outils.serialize.JsonSerializer;
//
//@NActorProvider
//@Slf4j
//@Data
//public class GameNoticeOutService extends SessionModules<PBGameNoticeOut> {
//	
////	@ActorRequire
////	AppSmHelper appSmHelper;
////	@ActorRequire
////	TransactionDetailHelper transactionDetailHelper;
////	
//	@ActorRequire
//	Daos daos;
//	
//	@ActorRequire(name = "http", scope = "global")
//	IPacketSender sender;
//	
//	private final String WALLET_URL_UAT = "http://wallet-api.test2.hr.fclink.cn/trs/pbuat.do";
//	
//	public String[] getCmds() {
//		return new String[] { GNPSCommand.GNO.name() };
//	}
//
//	@Override
//	public String getModule() {
//		return GNPSModule.GNA.name();
//	}
//	
//	@Override
//	public void onPBPacket(final FramePacket pack, final PBGameNoticeOut pb, final CompleteHandler handler) {
//		
//		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
//		PRetGameNoticeOut.Builder ret = PRetGameNoticeOut.newBuilder();
//		try{
//			baffle(pb, ret);
//		}catch(Exception e){
//			ret.setRetCode("99");
//			ret.setRetMsg(e.getMessage());
//		}
//		// 返回给客户端
//		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
//	}
//	/**
//	 * 挡板
//	 */
//	private void baffle(PBGameNoticeOut pb,PRetGameNoticeOut.Builder ret){
//		
//		Map<String,String> jsonMap = new HashMap<>();
//		jsonMap.put("user_id", "1");
//		jsonMap.put("topic", pb.getNoticeType());
//		String jsonStr = JsonSerializer.formatToString(jsonMap);
//		FramePacket pp = PacketHelper.buildUrlFromJson(jsonStr, "POST", WALLET_URL_REG);
//	
//		val yearMeasureRet = sender.send(pp,30000);
//		Map<String,Object> jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), Map.class);
//		
//		ret.setRetCode("01");
//		ret.setRetMsg("SUCCESS");
//		
//		CWVGameCountryExample countryExample = new CWVGameCountryExample();
//		CWVGameCountryExample.Criteria criteria = countryExample.createCriteria();
//		criteria.andStatusEqualTo("1").andIsDisplayEqualTo("1");
//		
//		if(StringUtils.isNotBlank(pb.getShotName())){
//			criteria.andCountryNameEqualTo(pb.getShotName());
//		}
//		
//		if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
//			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
//			countryExample.setLimit(page.getLimit());
//			countryExample.setOffset(page.getOffset());
//			
//			ret.setTotalCount(daos.gameCountryDao.countByExample(countryExample)+"");
//		}
//		
//		List<Object> countrys = daos.gameCountryDao.selectByExample(countryExample);
//		for(Object coun : countrys){
//			CWVGameCountry country = (CWVGameCountry) coun;
//			PRetCountry.Builder pCountry = PRetCountry.newBuilder();
//			pCountry.setCountryId(country.getCountryId()+"");
//			pCountry.setCountryName(country.getCountryName());
//			pCountry.setMapNumber(country.getMapNum()+"");
//			ret.addCountries(pCountry);
//		}
//	}
//	
//}
