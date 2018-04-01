package org.brewchain.cwv.game.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCity;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCityExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PBGameCity;
import org.brewchain.cwv.service.game.Game.PBGameCountry;
import org.brewchain.cwv.service.game.Game.PRetRefGameCity;
import org.brewchain.cwv.service.game.Game.PRetRefGameCity.PRetCity;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry;
import org.brewchain.cwv.service.game.Game.PTPSCommand;
import org.brewchain.cwv.service.game.Game.PTPSModule;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

@NActorProvider
@Slf4j
@Data
public class GameCountryCityService extends SessionModules<PBGameCity> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire
	Daos daos;
	
	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.GCC.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.GGA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameCity pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetRefGameCity.Builder ret = PRetRefGameCity.newBuilder();
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
	private void baffle(PBGameCity pb,PRetRefGameCity.Builder ret){
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		
		CWVGameCityExample cityExample = new CWVGameCityExample();
		CWVGameCityExample.Criteria criteria = cityExample.createCriteria();
		criteria.andStatusEqualTo("1").andIsDisplayEqualTo("1").andGameCountryIdEqualTo(Integer.parseInt(pb.getCountryId()));
		
		if(StringUtils.isNotBlank(pb.getShotName())){
			criteria.andCityNameEqualTo(pb.getShotName());
		}

		if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			cityExample.setLimit(page.getLimit());
			cityExample.setOffset(page.getOffset());
			
			ret.setTotalCount(daos.gameCityDao.countByExample(cityExample)+"");
		}
		List<Object> citys = daos.gameCityDao.selectByExample(cityExample);
		for(Object cityObj : citys){
			CWVGameCity city = (CWVGameCity) cityObj;
			PRetCity.Builder pCity = PRetCity.newBuilder();
			pCity.setCountryId(city.getGameCountryId()+"");
			pCity.setCityName(city.getCityName());
			pCity.setMapNumber(city.getMapNum()+"");
			pCity.setCityId(city.getCityId()+"");
			ret.addCities(pCity);
		}
		
	}
	
	
}
