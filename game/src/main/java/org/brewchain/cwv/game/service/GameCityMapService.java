package org.brewchain.cwv.game.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMap;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMapExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
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
	Daos dao;
	
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

		PageUtil page = new PageUtil(null, pb.getPageSize());
		mapExample.setLimit(page.getLimit());
		mapExample.setOffset(page.getOffset());
		
		ret.setTotalCount(dao.gameMapDao.countByExample(mapExample)+"");
		
		List<Object> maps = dao.gameMapDao.selectByExample(mapExample);
		
		for(Object mapObj : maps){
			CWVGameMap map = (CWVGameMap) mapObj;
			PRetMap.Builder pMap = PRetMap.newBuilder();
			pMap.setMapId(map.getMapId()+"");
			pMap.setMapName(map.getMapName());
			pMap.setCityId(map.getGameCityId()+"");
			pMap.setTamplate(map.getTemplate()+"");
			//TODO 查询字段
			CWVGamePropertyExample example = new CWVGamePropertyExample();
			
			CWVGamePropertyExample.Criteria criteria2 = example.createCriteria();
					criteria2.andGameMapIdEqualTo(map.getMapId());
			
			
			int propertyCount = dao.gamePropertyDao.countByExample(example);
			pMap.setPropertyCount(propertyCount+"");//假数据
			criteria2.andPropertyStatusIn(
					new ArrayList<String>(){{
						add(PropertyStatusEnum.NOSALE.getValue());
						add(PropertyStatusEnum.ONSALE.getValue());
						}}
					);
			
			int sellCount = dao.gamePropertyDao.getDaosupport().countByExample(example);
			pMap.setPropertySellCount(sellCount+"");
			
			List<Object> list = dao.gamePropertyDao.selectByExample(example);
			BigDecimal sum = new BigDecimal(0);
			for(Object o : list){
				CWVGameProperty gameProperty = (CWVGameProperty) o;
				sum = sum.add(gameProperty.getLastPrice());
			}
			pMap.setAveragePrice(sum.divide(new BigDecimal(sellCount)).intValue()+"");
			ret.addMaps(pMap);
		}
	}
}
