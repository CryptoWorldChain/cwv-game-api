package org.brewchain.cwv.common.service;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.fc.brewchain.bcapi.EncAPI;
import org.fc.brewchain.bcapi.KeyPairs;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
@Instantiate(name="Chain_Helper")
public class ChainHelper implements ActorService {

	@ActorRequire(name = "bc_encoder", scope = "global")
	EncAPI encApi;
	private final String ACT_CAC = "http://127.0.0.1:8000/cwv/act/pbcac.do";
	public KeyPairs genKey(String zjc) {
		return encApi.genKeys(zjc);
	}
	
	public KeyPairs genKey() {
		return encApi.genKeys();
	}


}
