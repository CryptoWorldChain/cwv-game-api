package org.brewchain.cwv.game.dao;

import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonCountry;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonSmsVerify;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCity;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMap;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.ojpa.api.DomainDaoSupport;
import onight.tfw.ojpa.api.IJPAClient;
import onight.tfw.ojpa.api.OJpaDAO;
import onight.tfw.ojpa.api.annotations.StoreDAO;

@iPojoBean
@Provides(specifications = { IJPAClient.class, ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
public class Daos implements ActorService, IJPAClient{

	@StoreDAO
	public OJpaDAO<CWVGameCountry> gameCountryDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameCity> gameCityDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameMap> gameMapDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameProperty> gamePropertyDao;
	
	@ActorRequire
	public DBProvider provider;
	
	@Override
	public void onDaoServiceAllReady() {
		log.debug("cwv-game-api common ---AllDao Ready........");
	}

	@Override
	public void onDaoServiceReady(DomainDaoSupport arg0) {

	}
}
