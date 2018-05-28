package org.brewchain.cwv.common.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.common.dao.SysDaos;
import org.brewchain.cwv.common.service.Chain.AccountCryptoToken;
import org.brewchain.cwv.common.service.Chain.PChainCommand;
import org.brewchain.cwv.common.service.Chain.PChainModule;
import org.brewchain.cwv.common.service.Chain.ReqAddCryptoToken;
import org.brewchain.cwv.common.service.Chain.ReqCreateAccount;
import org.brewchain.cwv.common.service.Chain.RespAddCryptoToken;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.fc.brewchain.bcapi.EncAPI;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.Data;
import lombok.val;
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
public class InitCrpytoService2 extends SessionModules<ReqCreateAccount> {

	@Override
	public String[] getCmds() {		
		return new String[] { PChainCommand.QAB.name() };
	}

	@Override
	public String getModule() {
		return PChainModule.CHN.name();
	}
	public String toString(){
		return "PTRStransfers";
	}
	private final String ACT_ACB = "http://127.0.0.1:8000/cwv/sys/pbacb.do";//创建账户

	private PropHelper props = new PropHelper(null);
	
	@ActorRequire(name="http",scope="global")
	IPacketSender sender;
	
	@ActorRequire(name="Sys_Daos")
	SysDaos sysDaos;
	
	@ActorRequire(name = "bc_encoder", scope = "global")
	EncAPI encApi;

	
	@Override
	public void onPBPacket(final FramePacket pack, ReqCreateAccount pb, final CompleteHandler handler) {
		Date startTime = new Date();
		System.out.println("开始cryptoToken初始化:"+startTime);
		log.debug("开始cryptoToken初始化:"+startTime);
		ReqAddCryptoToken.Builder addCryptoToken = ReqAddCryptoToken.newBuilder();
		addCryptoToken.setHexAddress("3097ebf41fc9e309b4131693f5803dce33991238cd");
		
		List<Long> timestamp = new ArrayList<>(); // Token创建时间
		List<Integer> index = new ArrayList<>(); // 该Token发行时的索引
		List<Integer> total = new ArrayList<>(); // 该Token的发行总数量
		List<String> code = new ArrayList<>(); // Token的编号
		List<String> name = new ArrayList<>(); // Token的名称
		List<String> symbol = new ArrayList<>();
		Date date = new Date();
		
		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		propertyExample.setLimit(Integer.parseInt(pb.getCount()));
		List<Object> properties = sysDaos.getPropertyDao().selectByExample(propertyExample);
		
		Date startTime2 = new Date();
		System.out.println("获取数据耗时:"+((startTime2.getTime()-startTime.getTime())/1000));
		log.debug("获取数据耗时:"+((startTime2.getTime()-startTime.getTime())/1000));
		
		for(int i = 0;i<properties.size();i++){
			try {
				CWVGameProperty property = (CWVGameProperty) properties.get(i);
				
				AccountCryptoToken.Builder oAccountCryptoToken = AccountCryptoToken.newBuilder();
				oAccountCryptoToken.setCode(property.getPropertyId()+"");
				oAccountCryptoToken.setIndex(property.getPropertyId()-1);
				oAccountCryptoToken.setName(property.getPropertyName());
				oAccountCryptoToken.setTimestamp(date.getTime());
				oAccountCryptoToken.setTotal(1000000);
				
				//hexHash.add(ByteString.copyFrom(encApi.sha256Encode(oAccountCryptoToken.build().toByteArray())));
				index.add(oAccountCryptoToken.getIndex());
				total.add(oAccountCryptoToken.getTotal());
				timestamp.add(oAccountCryptoToken.getTimestamp());
				symbol.add("House");
				code.add(oAccountCryptoToken.getCode());
				name.add(oAccountCryptoToken.getName());
			} catch (Exception e) {
				//e.printStackTrace();
				continue;
				
			}
			
			
//			addCryptoToken.addTotal(oAccountCryptoToken.getTotal());
//			addCryptoToken.addIndex(oAccountCryptoToken.getIndex());
//			try {
//				addCryptoToken.addHexHash(new String(encApi.sha256Encode(oAccountCryptoToken.build().toByteArray()),"utf-8"));
//			} catch (UnsupportedEncodingException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			addCryptoToken.addTimestamp(oAccountCryptoToken.getTimestamp());
//			addCryptoToken.addName(oAccountCryptoToken.getName());
//			addCryptoToken.addCode(oAccountCryptoToken.getCode());
//			addCryptoToken.addSymbol("House");
		}
		Date startTime3 = new Date();
		System.out.println("拼装传输数据耗时:"+((startTime3.getTime()-startTime2.getTime())/1000));
		log.debug("拼装传输数据耗时:"+((startTime3.getTime()-startTime2.getTime())/1000));
		System.out.println("拼装个数："+index.size());
		log.debug("拼装个数："+index.size());
		Map<String,Object> params = new HashMap<>();
		params.put("hexAddress", "3097ebf41fc9e309b4131693f5803dce33991238cd");
		params.put("timestamp", timestamp);
//		params.put("hexHash", hexHash);
		params.put("index", index);
		params.put("total", total);
		params.put("code", code);
		params.put("name", name);
		params.put("symbol", symbol);
		FramePacket fposttx = PacketHelper.buildUrlFromJson(JsonSerializer.formatToString(params),  "POST", ACT_ACB);
		val txretReg = sender.send(fposttx, 30000);
		Date startTime4 = new Date();
		System.out.println("数据入链耗时:"+((startTime4.getTime()-startTime3.getTime())/1000));
		log.debug("数据入链耗时:"+((startTime4.getTime()-startTime3.getTime())/1000));
		Map<String, Integer> jsonRet = JsonSerializer.getInstance().deserialize(new String(txretReg.getBody()), Map.class);
		RespAddCryptoToken.Builder ret = RespAddCryptoToken.newBuilder() ;
		ret.setRetCode(jsonRet.get("retCode"));
		ret.setTotal(jsonRet.get("total"));
		pack.setFbody(JsonSerializer.formatToString(ret.toString()));
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	
}