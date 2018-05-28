package org.brewchain.cwv.common.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.brewchain.account.core.AccountHelper;
import org.brewchain.account.gens.Act.AccountCryptoToken;
import org.brewchain.account.gens.Sys.RespAddCryptoToken;
import org.brewchain.cwv.common.dao.SysDaos;
import org.brewchain.cwv.common.service.Chain.PChainCommand;
import org.brewchain.cwv.common.service.Chain.PChainModule;
import org.brewchain.cwv.common.service.Chain.ReqCreateAccount;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.fc.brewchain.bcapi.EncAPI;

import com.google.protobuf.ByteString;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.conf.PropHelper;
import onight.tfw.outils.serialize.JsonSerializer;


@NActorProvider
@Slf4j
@Data
public class InitCrpytoService extends SessionModules<ReqCreateAccount> {

	@Override
	public String[] getCmds() {		
		return new String[] { PChainCommand.CPT.name() };
	}

	@Override
	public String getModule() {
		return PChainModule.CHN.name();
	}
	public String toString(){
		return "PTRStransfers";
	}
	
	@ActorRequire(name="Sys_Daos")
	SysDaos sysDaos;
	
	@ActorRequire(name = "bc_encoder", scope = "global")
	EncAPI encApi;
	
	@ActorRequire(name = "Account_Helper", scope = "global")
	AccountHelper oAccountHelper;

	
	@Override
	public void onPBPacket(final FramePacket pack, ReqCreateAccount pb, final CompleteHandler handler) {
		Date startTime = new Date();
		String address = pb.getAddress();
		System.out.println("开始导入cryptoToken:"+startTime);
		log.debug("开始导入cryptoToken:"+startTime);
		
		Date date = new Date();
		
		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		propertyExample.setLimit(Integer.parseInt(pb.getCount()));
		List<Object> properties = sysDaos.getPropertyDao().selectByExample(propertyExample);
		
		Date startTime2 = new Date();
		System.out.println("获取数据完成:"+((startTime2.getTime()-startTime.getTime())/1000));
		log.debug("获取数据完成:"+((startTime2.getTime()-startTime.getTime())/1000));
		
		
		RespAddCryptoToken.Builder oRespAddCryptoToken = RespAddCryptoToken.newBuilder();
		int count=0;
		String symbol2 = "HOUSE";
		
		ArrayList<AccountCryptoToken.Builder> tokensList = new ArrayList<>();
			
		System.out.println("获取数据个数:"+properties.size());
		log.debug("获取数据个数:"+properties.size());	
		for(int i = 0;i<properties.size();i++){
			try {
				CWVGameProperty property = (CWVGameProperty) properties.get(i);
				AccountCryptoToken.Builder oAccountCryptoToken = AccountCryptoToken.newBuilder();
				oAccountCryptoToken.setCode(property.getPropertyId()+"");
				oAccountCryptoToken.setIndex(property.getPropertyId()-1);
				oAccountCryptoToken.setName(property.getPropertyName());
				oAccountCryptoToken.setTimestamp(date.getTime());
				oAccountCryptoToken.setTotal(1000000);
				oAccountCryptoToken.setHash(ByteString.copyFrom(encApi.sha256Encode(oAccountCryptoToken.build().toByteArray())));
				oAccountCryptoToken.setOwner(ByteString.copyFrom(encApi.hexDec(address)));
				oAccountCryptoToken.setNonce(0);
				tokensList.add(oAccountCryptoToken);
			} catch (Exception e) {
				System.out.println("第【"+i+"】条数据错误，错误原因："+e.getMessage());
				log.debug("第【"+i+"】条数据错误，错误原因："+e.getMessage());
				continue;
			}
		}
		System.out.println("拼装数据个数:"+tokensList.size());
		log.debug("拼装数据个数:"+tokensList.size());
		Date startTime3 = new Date();
		System.out.println("拼装数据完成:"+((startTime3.getTime()-startTime2.getTime())/1000));
		log.debug("拼装数据完成:"+((startTime3.getTime()-startTime2.getTime())/1000));
		Map<String,Object> params = new HashMap<>();
		try {
			long a = oAccountHelper.addCryptoBalances(ByteString.copyFrom(encApi.hexDec(address)).toByteArray(), symbol2,tokensList);
			count+=a;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("AddCryptoTokenImpl error",e);
			oRespAddCryptoToken.setRetCode(-1);
			handler.onFinished(PacketHelper.toPBReturn(pack, oRespAddCryptoToken.build()));

			return;
		}
		
		oRespAddCryptoToken.setRetCode(1);
		oRespAddCryptoToken.setTotal(count);
		
		Date startTime4 = new Date();
		System.out.println("数据入链完成:"+((startTime4.getTime()-startTime3.getTime())/1000));
		log.debug("数据入链完成:"+((startTime4.getTime()-startTime3.getTime())/1000));
		pack.setFbody(JsonSerializer.formatToString(oRespAddCryptoToken.toString()));
		handler.onFinished(PacketHelper.toPBReturn(pack, oRespAddCryptoToken.build()));
	}
	
}