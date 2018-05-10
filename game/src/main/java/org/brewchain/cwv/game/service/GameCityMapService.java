package org.brewchain.cwv.game.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMap;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMapExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PBGameMap;
import org.brewchain.cwv.service.game.Game.PRetRefGameMap;
import org.brewchain.cwv.service.game.Game.PRetRefGameMap.PRetMap;
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
public class GameCityMapService extends SessionModules<PBGameMap> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire(name="Daos")
	Daos daos;
	
	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.GCM.name() };
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
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
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
		
		CWVGameMapExample mapExample = new CWVGameMapExample();
		CWVGameMapExample.Criteria criteria = mapExample.createCriteria();
		criteria.andStatusEqualTo("1").andIsDisplayEqualTo("1").andGameCityIdEqualTo(Integer.parseInt(pb.getCityId()));
		
		if(StringUtils.isNotBlank(pb.getShotName())){
			criteria.andMapNameLikeInsensitive("%"+pb.getShotName()+"%");
		}

		if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			mapExample.setLimit(page.getLimit());
			mapExample.setOffset(page.getOffset());
			
			ret.setTotalCount(daos.gameMapDao.countByExample(mapExample)+"");
		}
		List<Object> maps = daos.gameMapDao.selectByExample(mapExample);
		for(Object mapObj : maps){
			CWVGameMap map = (CWVGameMap) mapObj;
			PRetMap.Builder pMap = PRetMap.newBuilder();
			pMap.setMapId(map.getMapId()+"");
			pMap.setMapName(map.getMapName());
			pMap.setCityId(map.getGameCityId()+"");
			pMap.setTamplate(map.getTemplate()+"");
			pMap.setPropertyCount("100");//假数据
			pMap.setPropertySellCount("80");
			pMap.setAveragePrice("50000000");
			ret.addMaps(pMap);
		}
	}
}
