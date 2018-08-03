package org.brewchain.cwv.game.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCity;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCityExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PBGameCity;
import org.brewchain.cwv.service.game.Game.PRetRefGameCity;
import org.brewchain.cwv.service.game.Game.PRetRefGameCity.PRetCity;
import org.brewchain.cwv.service.game.Game.PTPSCommand;
import org.brewchain.cwv.service.game.Game.PTPSModule;

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
	@ActorRequire(name="Daos")
	Daos dao;
	
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
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
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
		criteria.andStatusEqualTo("1").andGameCountryIdEqualTo(Integer.parseInt(pb.getCountryId()));
		
		if(StringUtils.isNotBlank(pb.getShotName())){
			criteria.andCityNameLikeInsensitive("%"+pb.getShotName()+"%");
		}

		if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			cityExample.setLimit(page.getLimit());
			cityExample.setOffset(page.getOffset());
			
			ret.setTotalCount(dao.gameCityDao.countByExample(cityExample)+"");
		}
		List<Object> citys = dao.gameCityDao.selectByExample(cityExample);
		for(Object cityObj : citys){
			CWVGameCity city = (CWVGameCity) cityObj;
			PRetCity.Builder pCity = PRetCity.newBuilder();
			pCity.setCountryId(city.getGameCountryId()+"");
			pCity.setCityName(city.getCityName());
			pCity.setCityNameZh(city.getCityNameCn());
			pCity.setMapNumber(city.getMapNum()+"");
			pCity.setCityId(city.getCityId()+"");
			pCity.setIsDisplay(Integer.parseInt(city.getIsDisplay()));
			ret.addCities(pCity);
		}
		
	}
	
	
}
