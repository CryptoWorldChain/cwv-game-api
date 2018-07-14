package org.brewchain.cwv.game.chain;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.enums.ContractTypeEnum;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddress;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddressExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.helper.CommonHelper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
@Instantiate(name="Invoker")
public class Invoker {
	
	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
	@ActorRequire(name = "Daos", scope = "global")
	Daos dao;
	
	String getContractAddress(String type){
		
		CWVGameContractAddressExample example = new CWVGameContractAddressExample();
		example.createCriteria().andContractTypeEqualTo(ContractTypeEnum.RANDOM_CONTRACT.getName());
		
		Object o  = dao.contractDao.selectOneByExample(example);
		return o == null ? null : ((CWVGameContractAddress) o).getContractAddress() ;
	}
}
