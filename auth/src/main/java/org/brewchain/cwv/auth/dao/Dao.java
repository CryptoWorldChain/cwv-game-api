package org.brewchain.cwv.auth.dao;

import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthRefreshToken;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTrade;

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
public class Dao implements ActorService, IJPAClient {
	
	@StoreDAO
	public OJpaDAO<CWVAuthRefreshToken> tokenDao;
	
	@StoreDAO
	public OJpaDAO<CWVAuthUser> userDao;
	
	@StoreDAO
	public OJpaDAO<CWVUserTrade> tradeDao;
	
	@Override
	public void onDaoServiceAllReady() {
		log.debug("TPS ---AllDao Ready........");
	}

	@Override
	public void onDaoServiceReady(DomainDaoSupport arg0) {

	}
}
