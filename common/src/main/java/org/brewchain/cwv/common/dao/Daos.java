package org.brewchain.cwv.common.dao;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonCountry;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonSmsVerify;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ojpa.api.DomainDaoSupport;
import onight.tfw.ojpa.api.IJPAClient;
import onight.tfw.ojpa.api.OJpaDAO;
import onight.tfw.ojpa.api.annotations.StoreDAO;

@iPojoBean
@Provides(specifications = { IJPAClient.class, ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
@Instantiate(name="Daos")
public class Daos implements ActorService, IJPAClient{

	@StoreDAO
	public OJpaDAO<CWVCommonSmsVerify> cwvcommonsmsverifyDao;
	
	@StoreDAO
	public OJpaDAO<CWVCommonCountry> cwvcommoncountryDao;
	
	@StoreDAO
	public OJpaDAO<CWVAuthUser> userDao;
	
	@Override
	public void onDaoServiceAllReady() {
		log.debug("cwv-game-api common ---AllDao Ready........");
	}

	@Override
	public void onDaoServiceReady(DomainDaoSupport arg0) {

	}
}
