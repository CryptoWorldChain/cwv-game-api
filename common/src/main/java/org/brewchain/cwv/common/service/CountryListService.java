package org.brewchain.cwv.common.service;

import javax.servlet.http.HttpServletRequest;

import org.brewchain.cwv.common.dao.Daos;
import org.brewchain.cwv.common.service.Country.PBCountryList;
import org.brewchain.cwv.common.service.Country.PRetCountryList;
import org.brewchain.cwv.common.service.Country.PRetCountryList.Countries;
import org.brewchain.cwv.common.service.Country.PTNsdCommand;
import org.brewchain.cwv.common.service.Country.PTNsdModule;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample;

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
 * @author leo
 *
 */
@NActorProvider
@Slf4j
@Data
public class CountryListService extends SessionModules<PBCountryList> {

	@ActorRequire
	Daos sysDaos;
	
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
//			CWVGameCountryExample example = new CWVGameCountryExample();
//			example.createCriteria().and
//			sysDaos.cwvgamecountryDao.selectByExample(example);
			Countries.Builder countries = Countries.newBuilder();
			countries.setDomainCode("AD");//国际域名缩写
			countries.setReginCode("20");//地区代码
			countries.setShortName("Andorra");//简称
			countries.setFullName("Andorra");//全称
			countries.setDescEn("Andorra");//英文描述
			countries.setPhoneCode("376");//电话代码
			ret.addCountries(countries);
			
			Countries.Builder countries1 = Countries.newBuilder();
			countries1.setDomainCode("AU");//国际域名缩写
			countries1.setReginCode("36");//地区代码
			countries1.setShortName("Australia");//简称
			countries1.setFullName("Australia");//全称
			countries1.setDescEn("Australia");//英文描述
			countries1.setPhoneCode("61");//电话代码
			ret.addCountries(countries1);
			
			Countries.Builder countries2 = Countries.newBuilder();
			countries2.setDomainCode("AL");//国际域名缩写
			countries2.setReginCode("313");//地区代码
			countries2.setShortName("Albania");//简称
			countries2.setFullName("Albania");//全称
			countries2.setDescEn("Albania");//英文描述
			countries2.setPhoneCode("355");//电话代码
			ret.addCountries(countries2);
			
			Countries.Builder countries3 = Countries.newBuilder();
			countries3.setDomainCode("AT");//国际域名缩写
			countries3.setReginCode("40");//地区代码
			countries3.setShortName("Austria");//简称
			countries3.setFullName("Austria");//全称
			countries3.setDescEn("Austria");//英文描述
			countries3.setPhoneCode("43");//电话代码
			ret.addCountries(countries3);
			
			Countries.Builder countries4 = Countries.newBuilder();
			countries4.setDomainCode("AZ");//国际域名缩写
			countries4.setReginCode("31");//地区代码
			countries4.setShortName("Azerbaijan");//简称
			countries4.setFullName("Azerbaijan");//全称
			countries4.setDescEn("Azerbaijan");//英文描述
			countries4.setPhoneCode("994");//电话代码
			ret.addCountries(countries4);
			
			ret.setRetCode("01");
			ret.setRetMsg("success");
		} catch (Exception e) {
			ret.setRetCode("99");
			ret.setRetMsg("fails");
		}
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}

}
