package org.brewchain.cwv.common.service;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.common.service.Chain.PChainCommand;
import org.brewchain.cwv.common.service.Chain.PChainModule;
import org.brewchain.cwv.common.service.Chain.ReqCreateAccount;
import org.brewchain.cwv.common.service.Chain.RespCreateAccount;
import org.fc.brewchain.bcapi.KeyPairs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.serialize.JsonSerializer;

@NActorProvider
@Slf4j
@Data
public class CreateAccountService extends SessionModules<ReqCreateAccount> {
	
	
	@ActorRequire(name = "Chain_Helper", scope = "global")
	ChainHelper chainHelper;
	
	@Override
	public String[] getCmds() {		
		return new String[] { PChainCommand.QAI.name() };
	}

	@Override
	public String getModule() {
		return PChainModule.CHN.name();
	}
	
	
	@Override
	public void onPBPacket(final FramePacket pack, ReqCreateAccount pbo, final CompleteHandler handler) {
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		final RespCreateAccount.Builder ret = RespCreateAccount.newBuilder();
		
		try {
			if(StringUtils.isNotBlank(pbo.getCount())){
				KeyPairs keyPairs = chainHelper.genKey(pbo.getCount());
				ret.setAddress(keyPairs.getAddress());
				ret.setBcuid(keyPairs.getBcuid());
				ret.setPrikey(keyPairs.getPrikey());
				ret.setPubkey(keyPairs.getPubkey());
			}else{
				KeyPairs keyPairs = chainHelper.genKey();
				ret.setAddress(keyPairs.getAddress());
				ret.setBcuid(keyPairs.getBcuid());
				ret.setPrikey(keyPairs.getPrikey());
				ret.setPubkey(keyPairs.getPubkey());
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		pack.setFbody(JsonSerializer.formatToString(ret.toString()));
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}