package org.brewchain.cwv.game.helper;

import java.util.List;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.game.entity.CWVGameDicExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameTxManage;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketException;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSettingExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
/**
 * 查询字典以及公共配置相关数据
 * @author Moon
 * @date 2018-05-09
 */
@Instantiate(name="Common_Helper")
public class CommonHelper implements ActorService {
	/**
	 * 派息日期
	 */
	public static String INCOMETIME = "income_time"; 

	@ActorRequire(name="Daos")
	Daos dao;
	/**
	 * 根据parentKey获取字典数组
	 * @param parentKey
	 * @return
	 */
	public List<Object> getDicEntities(String parentKey) {
		CWVGameDicExample example = new CWVGameDicExample();
		example.createCriteria().andParentKeyEqualTo(parentKey);
		return dao.dicDao.selectByExample(example);
	}
	
	/**
	 * 获取配置字段值
	 * @param key
	 * @return
	 */
	public String getSysSettingValue(String key) {
		CWVSysSetting o = getSysSettingEntity(key);
		return  o == null ? null : o.getValue();
	}
	
	public CWVSysSetting getSysSettingEntity(String key) {
		CWVSysSettingExample example = new CWVSysSettingExample();
		example.createCriteria().andNameEqualTo(key);
		Object o = dao.settingDao.selectOneByExample(example);
		return o == null ? null :(CWVSysSetting) o ;
	}
	
	public void updateSysSettingValue(String key,String value) {
		CWVSysSettingExample example = new CWVSysSettingExample();
		example.createCriteria().andNameEqualTo(key);
		CWVSysSetting setting = new CWVSysSetting();
		setting.setValue(value);
		dao.settingDao.updateByExampleSelective(setting, example);
	}
	
	/**
	 * 添加交易记录
	 * @param key
	 * @param txHash
	 */
	public void txManageAdd(String key,String txHash) {
		CWVGameTxManage txManage = new CWVGameTxManage();
		txManage.setChainStatus((int) ChainTransStatusEnum.START.getKey());
		txManage.setTxHash(txHash);
		txManage.setType(key);
		txManage.setStatus(0);
		dao.txManangeDao.insert(txManage);
		
	}
	
	/**
	 * 添加交易异常记录
	 * @param type
	 * @param marketId
	 * @param detail
	 */
	public void marketExceptionAdd(String type, Integer marketId, String detail) {
		CWVMarketException exception = new CWVMarketException();
		exception.setType(type);
		exception.setMarketId(marketId);
		exception.setDescription(detail);
		exception.setStatus(0);
		dao.marketExceptionDao.insert(exception);
		
	}


}
