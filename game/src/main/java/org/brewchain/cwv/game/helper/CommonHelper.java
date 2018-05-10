package org.brewchain.cwv.game.helper;

import java.util.List;

import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.game.entity.CWVGameDicExample;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSettingExample;
import org.brewchain.cwv.game.dao.Daos;

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
public class CommonHelper implements ActorService {
	/**
	 * 派息日期
	 */
	public static String INCOMETIME = "income_time"; 

	@ActorRequire
	Daos daos;
	/**
	 * 根据parentKey获取字典数组
	 * @param parentKey
	 * @return
	 */
	public List<Object> getDicEntities(String parentKey) {
		CWVGameDicExample example = new CWVGameDicExample();
		example.createCriteria().andParentKeyEqualTo(parentKey);
		return daos.dicDao.selectByExample(example);
	}
	
	/**
	 * 获取配置字段值
	 * @param key
	 * @return
	 */
	public String getSysSettingValue(String key) {
		CWVSysSettingExample example = new CWVSysSettingExample();
		example.createCriteria().andNameEqualTo(key);
		List<Object> list = daos.settingDao.selectByExample(example);
		return list == null || list.isEmpty() ? null :( (CWVSysSetting) list.get(0)).getValue() ;
	}


}
