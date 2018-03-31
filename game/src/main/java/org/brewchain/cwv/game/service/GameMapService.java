package org.brewchain.cwv.game.service;

import org.brewchain.cwv.service.game.Game.PBGameCountry;
import org.brewchain.cwv.service.game.Game.PBGameMap;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry;
import org.brewchain.cwv.service.game.Game.PTPSCommand;
import org.brewchain.cwv.service.game.Game.PTPSModule;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;
import org.brewchain.cwv.service.game.Game.PRetRefGameMap;
import org.brewchain.cwv.service.game.Game.PRetRefGameMap.PRetMap;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

@NActorProvider
@Slf4j
@Data
public class GameMapService extends SessionModules<PBGameMap> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
//	@ActorRequire
//	Daos sysDaos;
	
	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.GMS.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.GGA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameMap pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetRefGameMap.Builder ret = PRetRefGameMap.newBuilder();
		try{
			baffle(pb, ret);
		}catch(Exception e){
			ret.setRetCode("99");
			ret.setRetMsg(e.getMessage());
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	/**
	 * 挡板
	 */
	private void baffle(PBGameMap pb,PRetRefGameMap.Builder ret){
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		
		if(pb.getCountryId().equals("1")){
			PRetMap.Builder map1 = PRetMap.newBuilder();
			map1.setCountryId("1");
			map1.setMapId("1");
			map1.setMapName("自由女神像");
			map1.setPropertyCount("100");
			map1.setPropertySellCount("80");
			map1.setAveragePrice("50000000");
			
			PRetMap.Builder map2 = PRetMap.newBuilder();
			map2.setCountryId("1");
			map2.setMapId("2");
			map2.setMapName("白宫");
			map2.setPropertyCount("100");
			map2.setPropertySellCount("80");
			map2.setAveragePrice("50000000");
			
			PRetMap.Builder map3 = PRetMap.newBuilder();
			map3.setCountryId("1");
			map3.setMapId("3");
			map3.setMapName("帝国大厦");
			map3.setPropertyCount("100");
			map3.setPropertySellCount("80");
			map3.setAveragePrice("50000000");
			
			PRetMap.Builder map4 = PRetMap.newBuilder();
			map4.setCountryId("1");
			map4.setMapId("4");
			map4.setMapName("林肯故居");
			map4.setPropertyCount("100");
			map4.setPropertySellCount("80");
			map4.setAveragePrice("50000000");
			
			PRetMap.Builder map5 = PRetMap.newBuilder();
			map5.setCountryId("1");
			map5.setMapId("5");
			map5.setMapName("纽约时报社");
			map5.setPropertyCount("100");
			map5.setPropertySellCount("80");
			map5.setAveragePrice("50000000");
			
			PRetMap.Builder map6 = PRetMap.newBuilder();
			map6.setCountryId("1");
			map6.setMapId("6");
			map6.setMapName("帝国大厦");
			map6.setPropertyCount("100");
			map6.setPropertySellCount("80");
			map6.setAveragePrice("50000000");
			
			PRetMap.Builder map7 = PRetMap.newBuilder();
			map7.setCountryId("1");
			map7.setMapId("7");
			map7.setMapName("林肯故居");
			map7.setPropertyCount("100");
			map7.setPropertySellCount("80");
			map7.setAveragePrice("50000000");
			
			PRetMap.Builder map8 = PRetMap.newBuilder();
			map8.setCountryId("1");
			map8.setMapId("8");
			map8.setMapName("纽约时报社");
			map8.setPropertyCount("100");
			map8.setPropertySellCount("80");
			map8.setAveragePrice("50000000");
			
			PRetMap.Builder map9 = PRetMap.newBuilder();
			map9.setCountryId("1");
			map9.setMapId("9");
			map9.setMapName("帝国大厦");
			map9.setPropertyCount("100");
			map9.setPropertySellCount("80");
			map9.setAveragePrice("50000000");
			
			PRetMap.Builder map10 = PRetMap.newBuilder();
			map10.setCountryId("1");
			map10.setMapId("10");
			map10.setMapName("林肯故居");
			map10.setPropertyCount("100");
			map10.setPropertySellCount("80");
			map10.setAveragePrice("50000000");
			
			PRetMap.Builder map11 = PRetMap.newBuilder();
			map11.setCountryId("1");
			map11.setMapId("11");
			map11.setMapName("纽约时报社");
			map11.setPropertyCount("100");
			map11.setPropertySellCount("80");
			map11.setAveragePrice("50000000");
			
			if(pb.getMapName()!=null&&!"".equals(pb.getMapName())){
				if(map1.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map1);
				}
				if(map2.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map2);
				}
				if(map3.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map3);
				}
				if(map4.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map4);
				}
				if(map5.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map5);
				}
				if(map6.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map6);
				}
				if(map7.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map7);
				}
				if(map8.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map8);
				}
				if(map9.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map9);
				}
				if(map10.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map10);
				}
				if(map11.getMapName().indexOf(pb.getMapName())!=-1){
					ret.addMaps(map11);
				}
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}else{
				
				if(pb.getPageIndex().equals("1")&&pb.getPageSize().equals("10")){
					ret.addMaps(map1);
					ret.addMaps(map2);
					ret.addMaps(map3);
					ret.addMaps(map4);
					ret.addMaps(map5);
					ret.addMaps(map6);
					ret.addMaps(map7);
					ret.addMaps(map8);
					ret.addMaps(map9);
					ret.addMaps(map10);
					ret.setTotalCount("11");
				}else if(pb.getPageIndex().equals("2")&&pb.getPageSize().equals("10")){
					ret.addMaps(map11);
					ret.setTotalCount("11");
				}
			}
			
			
		}
		else if(pb.getCountryId().equals("2")){
			PRetMap.Builder map1 = PRetMap.newBuilder();
			map1.setCountryId("2");
			map1.setMapId("1");
			map1.setMapName("自由女神像");
			map1.setPropertyCount("100");
			map1.setPropertySellCount("80");
			map1.setAveragePrice("50000000");
			
			PRetMap.Builder map2 = PRetMap.newBuilder();
			map2.setCountryId("2");
			map2.setMapId("2");
			map2.setMapName("白宫");
			map2.setPropertyCount("100");
			map2.setPropertySellCount("80");
			map2.setAveragePrice("50000000");
			
			PRetMap.Builder map3 = PRetMap.newBuilder();
			map3.setCountryId("2");
			map3.setMapId("3");
			map3.setMapName("帝国大厦");
			map3.setPropertyCount("100");
			map3.setPropertySellCount("80");
			map3.setAveragePrice("50000000");
			
			PRetMap.Builder map4 = PRetMap.newBuilder();
			map4.setCountryId("2");
			map4.setMapId("4");
			map4.setMapName("林肯故居");
			map4.setPropertyCount("100");
			map4.setPropertySellCount("80");
			map4.setAveragePrice("50000000");
			
			PRetMap.Builder map5 = PRetMap.newBuilder();
			map5.setCountryId("2");
			map5.setMapId("5");
			map5.setMapName("纽约时报社");
			map5.setPropertyCount("100");
			map5.setPropertySellCount("80");
			map5.setAveragePrice("50000000");
			
			if(pb.getMapName()!=null){
				if(pb.getMapName().indexOf(map1.getMapName())!=-1){
					ret.addMaps(map1);
				}
				if(pb.getMapName().indexOf(map2.getMapName())!=-1){
					ret.addMaps(map2);
				}
				if(pb.getMapName().indexOf(map3.getMapName())!=-1){
					ret.addMaps(map3);
				}
				if(pb.getMapName().indexOf(map4.getMapName())!=-1){
					ret.addMaps(map4);
				}
				if(pb.getMapName().indexOf(map5.getMapName())!=-1){
					ret.addMaps(map5);
				}
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}else{
				
				ret.addMaps(map1);
				ret.addMaps(map2);
				ret.addMaps(map3);
				ret.addMaps(map4);
				ret.addMaps(map5);
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}
		}
		else if(pb.getCountryId().equals("3")){
			PRetMap.Builder map1 = PRetMap.newBuilder();
			map1.setCountryId("3");
			map1.setMapId("1");
			map1.setMapName("自由女神像");
			map1.setPropertyCount("100");
			map1.setPropertySellCount("80");
			map1.setAveragePrice("50000000");
			
			PRetMap.Builder map2 = PRetMap.newBuilder();
			map2.setCountryId("3");
			map2.setMapId("2");
			map2.setMapName("白宫");
			map2.setPropertyCount("100");
			map2.setPropertySellCount("80");
			map2.setAveragePrice("50000000");
			
			PRetMap.Builder map3 = PRetMap.newBuilder();
			map3.setCountryId("3");
			map3.setMapId("3");
			map3.setMapName("帝国大厦");
			map3.setPropertyCount("100");
			map3.setPropertySellCount("80");
			map3.setAveragePrice("50000000");
			
			PRetMap.Builder map4 = PRetMap.newBuilder();
			map4.setCountryId("3");
			map4.setMapId("4");
			map4.setMapName("林肯故居");
			map4.setPropertyCount("100");
			map4.setPropertySellCount("80");
			map4.setAveragePrice("50000000");
			
			if(pb.getMapName()!=null){
				if(pb.getMapName().indexOf(map1.getMapName())!=-1){
					ret.addMaps(map1);
				}
				if(pb.getMapName().indexOf(map2.getMapName())!=-1){
					ret.addMaps(map2);
				}
				if(pb.getMapName().indexOf(map3.getMapName())!=-1){
					ret.addMaps(map3);
				}
				if(pb.getMapName().indexOf(map4.getMapName())!=-1){
					ret.addMaps(map4);
				}
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}else{
				
				ret.addMaps(map1);
				ret.addMaps(map2);
				ret.addMaps(map3);
				ret.addMaps(map4);
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}
		}
		else if(pb.getCountryId().equals("4")){
			PRetMap.Builder map1 = PRetMap.newBuilder();
			map1.setCountryId("4");
			map1.setMapId("1");
			map1.setMapName("自由女神像");
			map1.setPropertyCount("100");
			map1.setPropertySellCount("80");
			map1.setAveragePrice("50000000");
			
			PRetMap.Builder map2 = PRetMap.newBuilder();
			map2.setCountryId("4");
			map2.setMapId("2");
			map2.setMapName("白宫");
			map2.setPropertyCount("100");
			map2.setPropertySellCount("80");
			map2.setAveragePrice("50000000");
			
			PRetMap.Builder map3 = PRetMap.newBuilder();
			map3.setCountryId("4");
			map3.setMapId("3");
			map3.setMapName("帝国大厦");
			map3.setPropertyCount("100");
			map3.setPropertySellCount("80");
			map3.setAveragePrice("50000000");
			
			if(pb.getMapName()!=null){
				if(pb.getMapName().indexOf(map1.getMapName())!=-1){
					ret.addMaps(map1);
				}
				if(pb.getMapName().indexOf(map2.getMapName())!=-1){
					ret.addMaps(map2);
				}
				if(pb.getMapName().indexOf(map3.getMapName())!=-1){
					ret.addMaps(map3);
				}
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}else{
				
				ret.addMaps(map1);
				ret.addMaps(map2);
				ret.addMaps(map3);
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}
			
		}
		else if(pb.getCountryId().equals("5")){
			PRetMap.Builder map1 = PRetMap.newBuilder();
			map1.setCountryId("5");
			map1.setMapId("1");
			map1.setMapName("自由女神像");
			map1.setPropertyCount("100");
			map1.setPropertySellCount("80");
			map1.setAveragePrice("50000000");
			
			PRetMap.Builder map2 = PRetMap.newBuilder();
			map2.setCountryId("5");
			map2.setMapId("2");
			map2.setMapName("白宫");
			map2.setPropertyCount("100");
			map2.setPropertySellCount("80");
			map2.setAveragePrice("50000000");
			
			if(pb.getMapName()!=null){
				if(pb.getMapName().indexOf(map1.getMapName())!=-1){
					ret.addMaps(map1);
				}
				if(pb.getMapName().indexOf(map2.getMapName())!=-1){
					ret.addMaps(map2);
				}
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}else{
				
				ret.addMaps(map1);
				ret.addMaps(map2);
				ret.setTotalCount(ret.getMapsBuilderList().size()+"");
			}
		}
		
	}
	
	
}
