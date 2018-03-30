package org.brewchain.cwv.game.service;

import javax.servlet.http.HttpServletRequest;

import org.brewchain.cwv.dbgens.game.entity.CWVGameCountryExample;
import org.brewchain.cwv.game.dao.Daos;
import org.fc.tx.service.tps.Game.PBCountryList;
import org.fc.tx.service.tps.Game.PRetCountryList;
import org.fc.tx.service.tps.Game.PRetCountryList.Countries;
import org.fc.tx.service.tps.Game.PTPSCommand;
import org.fc.tx.service.tps.Game.PTPSModule;

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
		return new String[] { PTPSCommand.COL.name() };
	}

	@Override
	public String getModule() {
		return PTPSModule.NSD.name();
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
			countries.setDomainCode("AL");//国际域名缩写
			countries.setReginCode("313");//地区代码
			countries.setShortName("Albania");//简称
			countries.setFullName("Republic of Albania");//全称
			countries.setDescEn("Republic of Albania");//英文描述
			countries.setPhoneCode("355");//电话代码
			ret.addCountries(countries);
			
			Countries.Builder countries1 = Countries.newBuilder();
			countries1.setDomainCode("AL");//国际域名缩写
			countries1.setReginCode("313");//地区代码
			countries1.setShortName("Albania");//简称
			countries1.setFullName("Republic of Albania");//全称
			countries1.setDescEn("Republic of Albania");//英文描述
			countries1.setPhoneCode("355");//电话代码
			ret.addCountries(countries1);
			
			Countries.Builder countries2 = Countries.newBuilder();
			countries2.setDomainCode("AL");//国际域名缩写
			countries2.setReginCode("313");//地区代码
			countries2.setShortName("Albania");//简称
			countries2.setFullName("Republic of Albania");//全称
			countries2.setDescEn("Republic of Albania");//英文描述
			countries2.setPhoneCode("355");//电话代码
			ret.addCountries(countries2);
			
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
