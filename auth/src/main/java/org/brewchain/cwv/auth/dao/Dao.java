package org.brewchain.cwv.auth.dao;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.auth.dao.CWVAuthUserDao;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthRefreshToken;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.common.entity.CWVCommonCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddress;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameTxManage;
import org.brewchain.cwv.dbgens.sys.dao.CWVSysUrlResourceDao;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysUrlResource;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradePwd;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;

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
@Instantiate(name = "Dao")
public class Dao implements ActorService, IJPAClient {
	
	@StoreDAO
	public OJpaDAO<CWVAuthRefreshToken> tokenDao;
	
	@StoreDAO
	public OJpaDAO<CWVAuthUser> userDao;
	
	@StoreDAO
	public OJpaDAO<CWVUserWallet> walletDao;
	
	@StoreDAO
	public OJpaDAO<CWVUserTradePwd> tradeDao;
	
	@StoreDAO
	public OJpaDAO<CWVCommonCountry> commonCountryDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameCountry> gameCountryDao;
	
	
	@StoreDAO
	public OJpaDAO<CWVSysSetting> settingDao;
	
	@StoreDAO
	public OJpaDAO<CWVSysUrlResource> urlResouceDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameContractAddress> contractAddressDao;
	
	@StoreDAO
	public OJpaDAO<CWVGameTxManage> txManageDao;
	
	@StoreDAO
	public OJpaDAO<CWVUserPropertyIncome> incomeDao;
	
	@Override
	public void onDaoServiceAllReady() {
		log.debug("TPS ---AllDao Ready........");
	}

	@Override
	public void onDaoServiceReady(DomainDaoSupport arg0) {

	}
	
	public boolean isReady() {
		if (userDao != null 
				&& CWVAuthUserDao.class.isInstance(userDao)
				&& urlResouceDao != null 
				&& CWVSysUrlResourceDao.class.isInstance(urlResouceDao)
				) {
			return true;
		}
		return false;
	}
}
