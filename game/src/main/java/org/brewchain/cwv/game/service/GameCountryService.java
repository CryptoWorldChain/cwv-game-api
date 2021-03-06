package org.brewchain.cwv.game.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PBGameCountry;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry;
import org.brewchain.cwv.service.game.Game.PRetRefGameCountry.PRetCountry;
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
public class GameCountryService extends SessionModules<PBGameCountry> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire(name="Daos")
	Daos dao;
	
	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.GCS.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.GGA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameCountry pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetRefGameCountry.Builder ret = PRetRefGameCountry.newBuilder();
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
	private void baffle(PBGameCountry pb,PRetRefGameCountry.Builder ret){
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		
		CWVGameCountryExample countryExample = new CWVGameCountryExample();
		CWVGameCountryExample.Criteria criteria = countryExample.createCriteria();
		criteria.andStatusEqualTo("1");
		
		if(StringUtils.isNotBlank(pb.getShotName())){
			criteria.andCountryNameLike("%"+pb.getShotName()+"%");
		}
		
		if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			countryExample.setLimit(page.getLimit());
			countryExample.setOffset(page.getOffset());
			
			ret.setTotalCount(dao.gameCountryDao.countByExample(countryExample)+"");
		}
		
		List<Object> countrys = dao.gameCountryDao.selectByExample(countryExample);
		for(Object coun : countrys){
			CWVGameCountry country = (CWVGameCountry) coun;
			PRetCountry.Builder pCountry = PRetCountry.newBuilder();
			pCountry.setCountryId(country.getCountryId()+"");
			pCountry.setCountryName(country.getCountryName());
			pCountry.setCountryNameZh(country.getCountryNameZh());
			pCountry.setMapNumber(country.getMapNum()+"");
			pCountry.setIsDisplay(Integer.parseInt(country.getIsDisplay()));
			
			ret.addCountries(pCountry);
		}
	}
	
}
