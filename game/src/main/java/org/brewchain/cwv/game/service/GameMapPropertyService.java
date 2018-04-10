package org.brewchain.cwv.game.service;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PBGameProperty;
import org.brewchain.cwv.service.game.Game.PRetProperty;
import org.brewchain.cwv.service.game.Game.PRetRefGameProperty;
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
public class GameMapPropertyService extends SessionModules<PBGameProperty> {
	
//	@ActorRequire
//	AppSmHelper appSmHelper;
//	@ActorRequire
//	TransactionDetailHelper transactionDetailHelper;
//	
	@ActorRequire
	Daos daos;
	
	@Override
	public String[] getCmds() {
		return new String[] { PTPSCommand.GMP.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.GGA.name();
	}
	
	@Override
	public void onPBPacket(final FramePacket pack, final PBGameProperty pb, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetRefGameProperty.Builder ret = PRetRefGameProperty.newBuilder();
		try{
			baffle(pb, ret);
		}catch(Exception e){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg("未知异常");
			e.printStackTrace();
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	/**
	 * 挡板
	 */
	private void baffle(PBGameProperty pb,PRetRefGameProperty.Builder ret){
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		
		if(StringUtils.isEmpty(pb.getMapId())){
			ret.setRetCode("80");
			ret.setRetMsg("地图ID不能为空");
			return;
		}
		
		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria = propertyExample.createCriteria();
		criteria.andIsDisplayEqualTo("1").andGameMapIdEqualTo(Integer.parseInt(pb.getMapId()));
		
		if(StringUtils.isNotBlank(pb.getPropertyName())){
			criteria.andPropertyNameEqualTo(pb.getPropertyName());
		}
		
		if(StringUtils.isNotBlank(pb.getPropertyStatus())){
			criteria.andPropertyStatusEqualTo(pb.getPropertyStatus());
		}
		
		if(StringUtils.isNotBlank(pb.getPropertyType())){
			criteria.andPropertyTypeEqualTo(pb.getPropertyType());
		}
		
		if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			propertyExample.setLimit(page.getLimit());
			propertyExample.setOffset(page.getOffset());
			
			ret.setTotalCount(daos.gamePropertyDao.countByExample(propertyExample)+"");
		}
		List<Object> properties = daos.gamePropertyDao.selectByExample(propertyExample);
		for(Object coun : properties){
			CWVGameProperty property = (CWVGameProperty) coun;
			PRetProperty.Builder pProperty = PRetProperty.newBuilder();
			pProperty.setMapId(property.getGameMapId()+"");
			pProperty.setPropertyId(property.getPropertyId()+"");
			pProperty.setPropertyName(property.getPropertyName());
			pProperty.setPropertyStatus(property.getPropertyStatus());
			pProperty.setPropertyType(property.getPropertyType());
			
			if(pProperty.getPropertyStatus().equals("3") || pProperty.getPropertyStatus().equals("2")){
				pProperty.setOwner("匿名Jack");
				pProperty.setPrice("6987979.000");
			}else if(pProperty.getPropertyStatus().equals("0")
					|| pProperty.getPropertyStatus().equals("1")){
				pProperty.setPrice("0.000");
				pProperty.setOwner("No");
			}
			pProperty.setIncome("32244.00");
			pProperty.setPropertyTemplateId(property.getPropertyTemplateId()+"");
			pProperty.setPropertyTemplate(property.getPropertyTemplate());
			pProperty.setAppearanceType("1");
			ret.addProperties(pProperty);
		}
		
	}
	
	
}
