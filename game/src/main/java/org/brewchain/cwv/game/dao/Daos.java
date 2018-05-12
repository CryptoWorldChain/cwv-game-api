package org.brewchain.cwv.game.dao;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthRefreshToken;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCity;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameDic;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMap;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradePwd;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletTopup;

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
@Instantiate(name = "Daos")
public class Daos implements ActorService, IJPAClient{

	@StoreDAO
	public OJpaDAO<CWVGameCountry> gameCountryDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameCity> gameCityDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameMap> gameMapDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameProperty> gamePropertyDao;
	
	@StoreDAO
	public OJpaDAO<CWVMarketExchange> exchangeDao;
	
	@StoreDAO
	public OJpaDAO<CWVMarketBid> bidDao;
	
	@StoreDAO
	public OJpaDAO<CWVMarketAuction> auctionDao;
	
	@StoreDAO
	public OJpaDAO<CWVUserTransactionRecord> userTransactionRecordDao;
	
	@StoreDAO
	public OJpaDAO<CWVMarketDraw> drawDao;
	
	@StoreDAO
	public OJpaDAO<CWVUserWallet> walletDao;
	
	@StoreDAO
	public OJpaDAO<CWVUserPropertyIncome> incomeDao;

	@StoreDAO
	public OJpaDAO<CWVUserWalletTopup> topupDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameDic> dicDao;
	
	@StoreDAO
	public OJpaDAO<CWVSysSetting> settingDao;
	
	
	@ActorRequire(name="DB_Provider")
	public DBProvider provider;
	
	@Override
	public void onDaoServiceAllReady() {
		log.debug("cwv-game-api common ---AllDao Ready........");
	}

	@Override
	public void onDaoServiceReady(DomainDaoSupport arg0) {

	}
}
