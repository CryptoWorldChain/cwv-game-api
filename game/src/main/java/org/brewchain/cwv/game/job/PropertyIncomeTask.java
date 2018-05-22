package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.brewchain.cwv.auth.filter.SessionFilter;
import org.brewchain.cwv.auth.util.jwt.CheckResult;
import org.brewchain.cwv.auth.util.jwt.TokenMgr;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSettingExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncomeExample;
import org.brewchain.cwv.game.enums.PropertyIncomeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyIncomeTypeEnum;
import org.brewchain.cwv.game.enums.PropertyTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.game.util.DateUtil;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 房产收益 普通房产25% 标志性房产 25% 功能性房产 25%
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class PropertyIncomeTask implements Runnable {
	//防止收益数据冲突
	public static HashSet<Integer> jobIncomeSet = new HashSet<>();
	
	public final static float RATE = 0.25f;
	public final static float ORDINARY_TO_DIVIDED = 0.4f;
	public final static float TYPICAL_TO_DIVIDED = 0.6f;
	
	public final static int DAY_PERIOD = 7 ; //7日
	public final static String INCOME_TIME = "income_time" ; //7日
	
	private PropertyHelper propertyHelper;
	
	
	public PropertyIncomeTask(PropertyHelper propertyHelper) {
		super();
		this.propertyHelper = propertyHelper;
	}


	@Override
	public void run() {
		log.info("PropertyIncomeTask start ....");
		//判断执行时间
		CWVSysSettingExample example = new CWVSysSettingExample();
		example.createCriteria().andNameEqualTo(PropertyIncomeTask.INCOME_TIME);
		Object o = propertyHelper.getDao().settingDao.selectOneByExample(example);
	
		if(o == null) {
			return;
		}
		CWVSysSetting setting = (CWVSysSetting) o;
		Date incomeTime = null;
		try {
			incomeTime = DateUtil.getDateTime(setting.getValue());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(org.brewchain.cwv.auth.util.DateUtil.compare(new Date(), incomeTime)<0) {
			return;
		}
		log.info("PropertyIncomeTask execute time : "+DateUtil.getDayTime(new Date()));
		
		incomeProcess();
		Calendar a = Calendar.getInstance();
		a.setTime(incomeTime);
		a.add(Calendar.MINUTE, 7);
		setting.setValue(DateUtil.getDayTime(a.getTime()));
		propertyHelper.getDao().settingDao.updateByPrimaryKeySelective(setting);
		log.info("PropertyIncomeTask execute next time : "+setting.getValue());
		
		log.info("PropertyIncomeTask ended ....");
	}
	
	
	public void incomeProcess() {
		//查询收益合约 返回总收益
		double totalIncome = 1000000;
		
		//查询登陆有效用户ID
		Set<String> userIdSet = SessionFilter.userMap.keySet();
		if(userIdSet.isEmpty())
			return;
		List<Integer> listUser = new ArrayList<>();
		for(String userId : userIdSet) {
			CheckResult checkResult = TokenMgr.validateJWT(SessionFilter.userMap.get(userId));
			if(checkResult.isSuccess()) {
				listUser.add(Integer.parseInt(userId));
			}
			
		}
		//
		//计算 普通房产+标志性房产的分工收益
		CWVGamePropertyExample ordinaryExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria1 =  ordinaryExample.createCriteria();
		criteria1.andPropertyTypeEqualTo(PropertyTypeEnum.ORDINARY.getValue());
		criteria1.andUserIdIn(listUser);
		List<Object> listOrdinary = propertyHelper.getDao().gamePropertyDao.selectByExample(ordinaryExample);
	
		CWVGamePropertyExample typicalExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria2 =  typicalExample.createCriteria();
		criteria2.andPropertyTypeEqualTo(PropertyTypeEnum.TYPICAL.getValue());
		criteria2.andUserIdIn(listUser);
		List<Object> listTypical = propertyHelper.getDao().gamePropertyDao.selectByExample(typicalExample);
		
		//分红收益
		double incomeDivided = totalIncome * RATE;
		//单个普通房产收益
		double singleOrdinary = incomeDivided/((listTypical.size() * TYPICAL_TO_DIVIDED/ORDINARY_TO_DIVIDED) + listOrdinary.size());
		BigDecimal singleOrdinaryAmount = new BigDecimal(singleOrdinary).setScale(0, RoundingMode.UNNECESSARY);
		//单个功能房产收益
		double singleTypical = singleOrdinary * TYPICAL_TO_DIVIDED / ORDINARY_TO_DIVIDED;
		BigDecimal singleTypicalAmount = new BigDecimal(singleTypical).setScale(0, RoundingMode.UNNECESSARY);
		
		//计算实际总收益
		double remain = singleOrdinaryAmount.longValue() * listOrdinary.size()
				+(singleTypicalAmount.longValue() * listTypical.size());
		
		HashMap<Integer,BigDecimal> userTotalIncomeMap = new HashMap<>();
		//处理普通房产收益
		for(Object o: listOrdinary) {
			CWVGameProperty property = (CWVGameProperty) o;
			dividedIncomeSet(property,singleOrdinaryAmount, userTotalIncomeMap);
		}
		//处理标志性房产收益
		for(Object o: listTypical) {
			CWVGameProperty property = (CWVGameProperty) o;
			dividedIncomeSet(property,singleTypicalAmount, userTotalIncomeMap);
		}
		//处理单个用户总收益更新
		for(Integer userId : userTotalIncomeMap.keySet()) {
			CWVUserPropertyIncomeExample userPropertyIncomeExample = new CWVUserPropertyIncomeExample();
			userPropertyIncomeExample.createCriteria().andUserIdEqualTo(userId)
			.andStatusEqualTo(Byte.parseByte(PropertyIncomeStatusEnum.NEW.getValue()))
			.andPropertyIdIsNull();
			Object incomeO = propertyHelper.getDao().incomeDao.selectOneByExample(userPropertyIncomeExample);
			
			if(incomeO == null){
				final CWVUserPropertyIncome ordinaryIncome = new CWVUserPropertyIncome();
				ordinaryIncome.setUserId(userId);
				ordinaryIncome.setAmount(userTotalIncomeMap.get(userId));
				ordinaryIncome.setStartTime(new Date());
				ordinaryIncome.setStatus(Byte.parseByte(PropertyIncomeStatusEnum.NEW.getValue()));
				ordinaryIncome.setType(Byte.parseByte(PropertyIncomeTypeEnum.DIVIDED.getValue()));
				propertyHelper.getDao().getIncomeDao().insert(ordinaryIncome);
				
			}else{
				CWVUserPropertyIncome o2 = (CWVUserPropertyIncome) incomeO;
				jobIncomeSet.add(o2.getIncomeId());
				final CWVUserPropertyIncome ordinaryIncome = propertyHelper.getDao().getIncomeDao().selectByPrimaryKey(o2);
				ordinaryIncome.setAmount(ordinaryIncome.getAmount().add(userTotalIncomeMap.get(userId)));
				propertyHelper.getDao().getIncomeDao().insert(ordinaryIncome);
				jobIncomeSet.remove(o2.getIncomeId());
			}
		}
	
		
		
	}
	/**
	 * 分红收益处理 更新房产信息，用户收益记录新增或者更新
	 * @param property
	 * @param singleOrdinary
	 */
	public void dividedIncomeSet(final CWVGameProperty property, BigDecimal incomeAmount, HashMap<Integer,BigDecimal> userMap) {
		property.setIncome(property.getIncome().add(incomeAmount));
		
		CWVUserPropertyIncomeExample userPropertyIncomeExample = new CWVUserPropertyIncomeExample();
		userPropertyIncomeExample.createCriteria().andUserIdEqualTo(property.getUserId())
		.andStatusEqualTo(Byte.parseByte(PropertyIncomeStatusEnum.NEW.getValue()))
		.andPropertyIdEqualTo(property.getPropertyId());
		

		Object incomeO = propertyHelper.getDao().incomeDao.selectOneByExample(userPropertyIncomeExample);
		
		if(incomeO == null){
			final CWVUserPropertyIncome ordinaryIncome = new CWVUserPropertyIncome();
			ordinaryIncome.setPropertyId(property.getPropertyId());
			ordinaryIncome.setUserId(property.getUserId());
			ordinaryIncome.setAmount(incomeAmount);
			ordinaryIncome.setStartTime(new Date());
			ordinaryIncome.setStatus(Byte.parseByte(PropertyIncomeStatusEnum.NEW.getValue()));
			ordinaryIncome.setType(Byte.parseByte(PropertyIncomeTypeEnum.DIVIDED.getValue()));
			propertyHelper.getDao().getIncomeDao().doInTransaction(new TransactionExecutor() {
				@Override
				public Object doInTransaction() {
					propertyHelper.getDao().getIncomeDao().insert(ordinaryIncome);
					propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
					return null;
				}
			});
			
		}else{
			CWVUserPropertyIncome o2 = (CWVUserPropertyIncome) incomeO;
			jobIncomeSet.add(o2.getIncomeId());
			final CWVUserPropertyIncome ordinaryIncome = propertyHelper.getDao().getIncomeDao().selectByPrimaryKey(o2);
			ordinaryIncome.setAmount(ordinaryIncome.getAmount().add(incomeAmount));
			propertyHelper.getDao().getIncomeDao().doInTransaction(new TransactionExecutor() {
				@Override
				public Object doInTransaction() {
					propertyHelper.getDao().getIncomeDao().insert(ordinaryIncome);
					propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
					return null;
				}
			});
			
			jobIncomeSet.remove(o2.getIncomeId());
		}
		
		
		//用户本次总收益
		if(userMap.containsKey(property.getUserId())) {
			userMap.put(property.getUserId(), userMap.get(property.getUserId()).add(incomeAmount));
		}else{
			userMap.put(property.getUserId(),incomeAmount);
		}
		
	}
	 

}





