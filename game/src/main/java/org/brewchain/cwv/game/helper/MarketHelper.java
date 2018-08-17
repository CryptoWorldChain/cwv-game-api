package org.brewchain.cwv.game.helper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketException;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExceptionExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.MarketExceptionStatusEnum;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.service.game.Game.MarketException;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;
import org.brewchain.cwv.service.game.Game.RetCodeMsg;
import org.brewchain.cwv.service.game.Game.RetData;
import org.brewchain.cwv.service.game.MarketManage.PSMarketException;
import org.brewchain.cwv.service.game.MarketManage.PSMarketExceptionCheck;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
/**
 * 异常交易管理
 * @author Moon
 * @date 2018-05-09
 */
@Instantiate(name="Market_Helper")
public class MarketHelper implements ActorService {
	
	@ActorRequire(name="Daos")
	Daos dao;

	public void marketException(PSMarketException pb, Builder ret,
			org.brewchain.cwv.service.game.Game.RetCodeMsg.Builder codeMsg) {
		CWVMarketExceptionExample example = new CWVMarketExceptionExample();
		CWVMarketExceptionExample.Criteria criteria = example.createCriteria();
		if(StringUtils.isNotEmpty(pb.getId())) {
			criteria.andIdEqualTo(Integer.parseInt(pb.getId()));
		}
		
		if(StringUtils.isNotEmpty(pb.getType())) {
			criteria.andTypeEqualTo(pb.getType());
		}
		
		List<Object> list = dao.getMarketExceptionDao().selectByExample(example);
		RetData.Builder data = RetData.newBuilder();
		for(Object o : list) {
			CWVMarketException exception = (CWVMarketException) o;
			MarketException.Builder eBuilder = MarketException.newBuilder();
			eBuilder.setId(exception.getId());
			eBuilder.setType(exception.getType());
			eBuilder.setRemark(exception.getRemark());
			eBuilder.setMarketId(exception.getMarketId());
			eBuilder.setStatus(exception.getStatus());
			data.addMarketException(eBuilder);
		}
		codeMsg.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setData(data);
	}
	
	
	public void marketExceptionCheck(PSMarketExceptionCheck pb, PRetCommon.Builder ret,
			RetCodeMsg.Builder builder) {
		RetCodeMsg.Builder codeMsg = RetCodeMsg.newBuilder();
		if(StringUtils.isEmpty(pb.getId())) {
			codeMsg.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("异常交易ID不能为空");
			ret.setCodeMsg(codeMsg);
			return ;
		}
		
		if(StringUtils.isEmpty(pb.getStatus())) {
			codeMsg.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("异常交易装填不能为空");
			ret.setCodeMsg(codeMsg);
			return ;
		}
		
		CWVMarketException exception = new CWVMarketException();
		exception.setId(Integer.parseInt(pb.getId()));
		exception.setRemark(pb.getRemark());
		exception.setStatus(Integer.parseInt(pb.getStatus()));
		dao.marketExceptionDao.updateByPrimaryKeySelective(exception);
		codeMsg.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setCodeMsg(codeMsg);
		
	}


}
