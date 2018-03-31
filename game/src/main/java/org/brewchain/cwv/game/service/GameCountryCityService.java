//package org.brewchain.cwv.game.service;
//
//import org.brewchain.cwv.service.game.Game.PBGameCountry;
//import org.brewchain.cwv.service.game.Game.PRetRefGameCountry;
//import org.brewchain.cwv.service.game.Game.PTPSCommand;
//import org.brewchain.cwv.service.game.Game.PTPSModule;
//import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;
//
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import onight.oapi.scala.commons.SessionModules;
//import onight.osgi.annotation.NActorProvider;
//import onight.tfw.async.CompleteHandler;
//import onight.tfw.otransio.api.PacketHelper;
//import onight.tfw.otransio.api.beans.FramePacket;
//
//@NActorProvider
//@Slf4j
//@Data
//public class GameCountryCityService extends SessionModules<PBGameCountry> {
//	
////	@ActorRequire
////	AppSmHelper appSmHelper;
////	@ActorRequire
////	TransactionDetailHelper transactionDetailHelper;
////	
////	@ActorRequire
////	Daos sysDaos;
//	
//	@Override
//	public String[] getCmds() {
//		return new String[] { PTPSCommand.GCS.name() };
//	}
//
//	@Override
//	public String getModule() {
//		return PTPSModule.GGA.name();
//	}
//	
//	@Override
//	public void onPBPacket(final FramePacket pack, final PBGameCountry pb, final CompleteHandler handler) {
//		
//		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
//		PRetRefGameCountry.Builder ret = PRetRefGameCountry.newBuilder();
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
//	private void baffle(PBGameCountry pb,PRetRefGameCountry.Builder ret){
//		ret.setRetCode("01");
//		ret.setRetMsg("SUCCESS");
//		
//		
//		PRetCountry.Builder country1 = PRetCountry.newBuilder();
//		country1.setMapCount("1270");
//		country1.setShortName("China");
//		country1.setCountryId("2");
//		
//		PRetCountry.Builder country2 = PRetCountry.newBuilder();
//		country2.setMapCount("1010");
//		country2.setShortName("United States");
//		country1.setCountryId("1");
//		
//		PRetCountry.Builder country3 = PRetCountry.newBuilder();
//		country3.setMapCount("510");
//		country3.setShortName("Anguilla");
//		country1.setCountryId("3");
//		
//		PRetCountry.Builder country4 = PRetCountry.newBuilder();
//		country4.setMapCount("235");
//		country4.setShortName("Bahrain");
//		country1.setCountryId("4");
//		
//		PRetCountry.Builder country5 = PRetCountry.newBuilder();
//		country5.setMapCount("1173");
//		country5.setShortName("Canada");
//		country1.setCountryId("5");
//		
//		PRetCountry.Builder country6 = PRetCountry.newBuilder();
//		country6.setMapCount("670");
//		country6.setShortName("Egypt");
//		country1.setCountryId("6");
//		
//		PRetCountry.Builder country7 = PRetCountry.newBuilder();
//		country7.setMapCount("541");
//		country7.setShortName("France");
//		country1.setCountryId("7");
//		
//		PRetCountry.Builder country8 = PRetCountry.newBuilder();
//		country8.setMapCount("197");
//		country8.setShortName("Haiti");
//		country1.setCountryId("8");
//		
//		PRetCountry.Builder country9 = PRetCountry.newBuilder();
//		country9.setMapCount("49");
//		country9.setShortName("Macau");
//		country1.setCountryId("9");
//		
//		PRetCountry.Builder country10 = PRetCountry.newBuilder();
//		country10.setMapCount("27");
//		country10.setShortName("Serbia");
//		country1.setCountryId("10");
//		
//		ret.addCountries(country1);
//		ret.addCountries(country2);
//		ret.addCountries(country3);
//		ret.addCountries(country4);
//		ret.addCountries(country5);
//		ret.addCountries(country6);
//		ret.addCountries(country7);
//		ret.addCountries(country8);
//		ret.addCountries(country9);
//		ret.addCountries(country10);
//	}
//	
//	
//}
