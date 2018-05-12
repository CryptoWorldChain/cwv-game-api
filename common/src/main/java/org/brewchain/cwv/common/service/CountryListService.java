package org.brewchain.cwv.common.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.common.dao.SysDaos;
import org.brewchain.cwv.common.service.Country.PBCountryList;
import org.brewchain.cwv.common.service.Country.PRetCountryList;
import org.brewchain.cwv.common.service.Country.PRetCountryList.Countries;
import org.brewchain.cwv.common.service.Country.PTNsdCommand;
import org.brewchain.cwv.common.service.Country.PTNsdModule;
import org.brewchain.cwv.common.util.PageUtil;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonCountry;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonCountryExample;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

/**
 * 获取国家列表
 * 
 * @author leo
 *
 */
@NActorProvider
@Slf4j
@Data
public class CountryListService extends SessionModules<PBCountryList> {

	@ActorRequire(name="Sys_Daos")
	SysDaos sysDaos;

	@Override
	public String[] getCmds() {
		return new String[] { PTNsdCommand.COL.name() };
	}

	@Override
	public String getModule() {
		return PTNsdModule.NSD.name();
	}

	@Override
	public void onPBPacket(final FramePacket pack, final PBCountryList pb, final CompleteHandler handler) {

		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		HttpServletRequest request = pack.getHttpServerletRequest();
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		PRetCountryList.Builder ret = PRetCountryList.newBuilder();
		try {
			CWVCommonCountryExample example = new CWVCommonCountryExample();
			CWVCommonCountryExample.Criteria criteria = example.createCriteria();
			if (StringUtils.isNotBlank(pb.getShortName())) {
				criteria.andShortNameLike("%" + pb.getShortName() + "%");
			}
			ret.setTotalCount("0");
			if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
				ret.setTotalCount(sysDaos.cwvcommoncountryDao.countByExample(example)+"");
				
				PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
				example.setLimit(page.getLimit());
				example.setOffset(page.getOffset());
				
			}
//			example.setOrderByClause("sort asc");
			List<Object> list = sysDaos.cwvcommoncountryDao.selectByExample(example);
			for (Object object : list) {
				CWVCommonCountry record = (CWVCommonCountry) object;
				Countries.Builder countries = Countries.newBuilder();
				countries.setDomainCode(record.getDomainCode());// 国际域名缩写
				countries.setReginCode(record.getRegionCode());// 地区代码
				countries.setShortName(record.getShortName());// 简称
				countries.setFullName(record.getFullName());// 全称
				countries.setDescEn(record.getShortName());// 英文描述
				countries.setPhoneCode(record.getPhoneCode());// 电话代码
				ret.addCountries(countries);
			}

			ret.setRetCode("00");
			ret.setRetMsg("success");
		} catch (Exception e) {
			ret.setRetCode("-1");
			ret.setRetMsg("fails");
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

}
