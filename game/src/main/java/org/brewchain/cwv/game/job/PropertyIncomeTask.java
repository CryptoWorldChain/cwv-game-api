package org.brewchain.cwv.game.job;

import java.util.List;
import java.util.Set;

import org.brewchain.cwv.auth.filter.SessionFilter;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.game.enums.PropertyTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;

/**
 * 房产收益 普通房产25% 标志性房产 25% 功能性房产 25%
 * @author Moon
 * @date 2018-05-15
 */
public class PropertyIncomeTask implements Runnable {

	public final static float RATE = 0.25f;
	
	private PropertyHelper propertyHelper;
	
	
	public PropertyIncomeTask(PropertyHelper propertyHelper) {
		super();
		this.propertyHelper = propertyHelper;
	}


	@Override
	public void run() {
		
		
	}
	
	
	public void getProperties() {
		//查询收益合约 返回总收益
		double totalIncome = 1000000;
		//
		//计算 普通房产+标志性房产
		CWVGamePropertyExample ordinaryExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria1 =  ordinaryExample.createCriteria();
		criteria1.andPropertyTypeEqualTo(PropertyTypeEnum.ORDINARY.getValue());
		criteria1.andUserIdIsNotNull();
		int ordinaryCount = propertyHelper.getDao().gamePropertyDao.countByExample(ordinaryExample);
		
		Set<String> userIdSet = SessionFilter.userMap.keySet();
		
		
		for(String userId : userIdSet) {
			
//			criteria1.andUserIdEqualTo(Integer.parseInt(userId))
//			int count = propertyHelper.getDao().gamePropertyDao.countByExample(propertyExample1);
//			
			
			CWVGamePropertyExample propertyExample2 = new CWVGamePropertyExample();
			CWVGamePropertyExample.Criteria criteria2 =  propertyExample2.createCriteria();
			criteria2.andUserIdEqualTo(Integer.parseInt(userId))
			.andPropertyTypeEqualTo(PropertyTypeEnum.TYPICAL.getValue());
			
			int count = propertyHelper.getDao().gamePropertyDao.countByExample(propertyExample2);
			
			
			
//			List<Object> list = propertyHelper.getDao().gamePropertyDao.selectByExample(propertyExample);
//			
//			for(Object o :list) {
//				
//				
//			}
			
		
		}
	}
	 

}





