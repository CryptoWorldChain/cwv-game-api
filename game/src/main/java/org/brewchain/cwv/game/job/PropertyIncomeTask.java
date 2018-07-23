package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameTxManage;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncomeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.PropertyIncomeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyIncomeTypeEnum;
import org.brewchain.cwv.game.enums.PropertyTypeEnum;
import org.brewchain.cwv.game.enums.TransactionTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.game.util.Arith;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.wallet.service.Wallet.AccountValueImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionInputImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionOutputImpl;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetAccount;

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
	
	public final static float INCOME_DIVIDED_RATE = 0.25f;
	public final static float ORDINARY_TO_DIVIDED = 0.2f;
	public final static float TYPICAL_TO_DIVIDED = 0.4f;
	public final static float FUNCTIONAL_TO_DIVIDED = 0.4f;
	
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
		Object o = propertyHelper.getCommonHelper().getSysSettingValue(PropertyIncomeTask.INCOME_TIME);
		if(o == null) {
			return;
		}
		CWVSysSetting setting = (CWVSysSetting) o;
		Date incomeTime = null;
		try {
			incomeTime = DateUtil.getDateTime(setting.getValue());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(org.brewchain.cwv.auth.util.DateUtil.compare(new Date(), incomeTime)<0) {
			return;
		}
		log.info("PropertyIncomeTask execute time : "+DateUtil.getDayTime(new Date()));
		//收益处理
		
		
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
		RespGetAccount.Builder account = propertyHelper.getWltHelper().getAccountInfo(PropertyJobHandle.SYS_INCOME_ADDRESS);
		
		if(account.getRetCode() == -1) {
			return ;
		}
		
		double totalIncome = Double.parseDouble(account.getAccount().getBalance());
		
		
		//查询登陆有效用户ID
//		Set<String> userIdSet = SessionFilter.userMap.keySet();
//		if(userIdSet.isEmpty())
//			return ;
//		List<Integer> listUser = new ArrayList<>();
//		for(String userId : userIdSet) {
//			CheckResult checkResult = TokenMgr.validateJWT(SessionFilter.userMap.get(userId));
//			if(checkResult.isSuccess()) {
//				listUser.add(Integer.parseInt(userId));
//			}
//			
//		}
		//
		//计算 普通房产+标志性房产的分工收益
		CWVGamePropertyExample ordinaryExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria1 =  ordinaryExample.createCriteria();
		criteria1.andPropertyTypeEqualTo(PropertyTypeEnum.ORDINARY.getValue());
//		criteria1.andUserIdIn(listUser);
		List<Object> listOrdinary = propertyHelper.getDao().gamePropertyDao.selectByExample(ordinaryExample);
	
		CWVGamePropertyExample typicalExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria2 =  typicalExample.createCriteria();
		criteria2.andPropertyTypeEqualTo(PropertyTypeEnum.TYPICAL.getValue());
//		criteria2.andUserIdIn(listUser);
		List<Object> listTypical = propertyHelper.getDao().gamePropertyDao.selectByExample(typicalExample);
		
		CWVGamePropertyExample functionalExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria3 =  functionalExample.createCriteria();
		criteria3.andPropertyTypeEqualTo(PropertyTypeEnum.FUNCTIONAL.getValue());
//		criteria2.andUserIdIn(listUser);
		List<Object> listFunctional = propertyHelper.getDao().gamePropertyDao.selectByExample(functionalExample);
		
		//分红收益
		double incomeDivided = Arith.mul(totalIncome, INCOME_DIVIDED_RATE) ;
		//单个普通房产收益
//		double singleOrdinary = incomeDivided/((listTypical.size() * TYPICAL_TO_DIVIDED/ORDINARY_TO_DIVIDED) + listOrdinary.size());
		
		double singleOrdinary = Arith.div(incomeDivided, Arith.add(Arith.div(Arith.mul(listTypical.size(), TYPICAL_TO_DIVIDED), ORDINARY_TO_DIVIDED), listOrdinary.size()) );
		BigDecimal singleOrdinaryAmount = new BigDecimal(singleOrdinary).setScale(0, RoundingMode.FLOOR);
		
		//单个功能房产收益
		double singleTypical = Arith.div(Arith.mul(singleOrdinary, TYPICAL_TO_DIVIDED), ORDINARY_TO_DIVIDED) ;
		BigDecimal singleTypicalAmount = new BigDecimal(singleTypical).setScale(0, RoundingMode.FLOOR);
		
		//单个功能房产收益
		double singleFunctional = Arith.div(Arith.mul(singleOrdinary, FUNCTIONAL_TO_DIVIDED),FUNCTIONAL_TO_DIVIDED) ;
		BigDecimal singleFunctionalAmount = new BigDecimal(singleFunctional).setScale(0, RoundingMode.FLOOR);
		
		
		//计算实际总收益
		double remain = singleOrdinaryAmount.longValue() * listOrdinary.size()
				+(singleTypicalAmount.longValue() * listTypical.size())
				+(singleFunctionalAmount.longValue() * listFunctional.size());
		
		HashMap<Integer,BigDecimal> userOrdinaryTotalIncomeMap = new HashMap<>();
		HashMap<Integer,BigDecimal> userTypicalTotalIncomeMap = new HashMap<>();
		HashMap<Integer,BigDecimal> userFunctionalTotalIncomeMap = new HashMap<>();
		//处理普通房产收益
		for(Object o: listOrdinary) {
			CWVGameProperty property = (CWVGameProperty) o;
			dividedIncomeSet(property,singleOrdinaryAmount, userOrdinaryTotalIncomeMap);
		}
		//处理标志性房产收益
		for(Object o: listTypical) {
			CWVGameProperty property = (CWVGameProperty) o;
			dividedIncomeSet(property,singleTypicalAmount, userTypicalTotalIncomeMap);
		}
		//处理功能性房产
		for(Object o: listTypical) {
			CWVGameProperty property = (CWVGameProperty) o;
			dividedIncomeSet(property,singleFunctionalAmount, userFunctionalTotalIncomeMap);
		}
		
		//添加用户普通房产类型收益
		for(Integer userId : userOrdinaryTotalIncomeMap.keySet()) {
			
			CWVUserPropertyIncome income = new CWVUserPropertyIncome();
			income.setUserId(userId);
			income.setAmount(userOrdinaryTotalIncomeMap.get(userId));
			income.setStartTime(new Date());
			income.setStatus(PropertyIncomeStatusEnum.NEW.getValue());
			income.setType(Byte.parseByte(PropertyTypeEnum.ORDINARY.getValue()));
			propertyHelper.getDao().getIncomeDao().insert(income);
			
			
				
		}
		
		//添加用户标志性房产类型收益
		for(Integer userId : userOrdinaryTotalIncomeMap.keySet()) {
			
			CWVUserPropertyIncome income = new CWVUserPropertyIncome();
			income.setUserId(userId);
			income.setAmount(userOrdinaryTotalIncomeMap.get(userId));
			income.setStartTime(new Date());
			income.setStatus(PropertyIncomeStatusEnum.NEW.getValue());
			income.setType(Byte.parseByte(PropertyTypeEnum.TYPICAL.getValue()));
			propertyHelper.getDao().getIncomeDao().insert(income);
				
				
		}
		
		//添加用户功能型房产类型收益
		for(Integer userId : userOrdinaryTotalIncomeMap.keySet()) {
			
			CWVUserPropertyIncome income = new CWVUserPropertyIncome();
			income.setUserId(userId);
			income.setAmount(userOrdinaryTotalIncomeMap.get(userId));
			income.setStartTime(new Date());
			income.setStatus(PropertyIncomeStatusEnum.NEW.getValue());
			income.setType(Byte.parseByte(PropertyTypeEnum.FUNCTIONAL.getValue()));
			propertyHelper.getDao().getIncomeDao().insert(income);
				
		}
		
		
		
	}
	
	/**
	 * 生成用户收益
	 * @param userIncomeMap
	 */
	public void userIncomeCreateProcess() {
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		example.createCriteria().andStatusEqualTo(PropertyIncomeStatusEnum.NEW.getValue())
		.andChainStatusIsNull()
		.andPropertyIdIsNull()
		.andChainStatusClaimIsNull();
		
		List<Object> list = propertyHelper.getDao().incomeDao.selectByExample(example);
	
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		//获取发起发账户nonce
		
		RespGetAccount.Builder accountMap = propertyHelper.getWltHelper().getAccountInfo(PropertyJobHandle.SYS_INCOME_ADDRESS);
		if(accountMap==null){
			respCreateTransaction.setRetMsg("查询账户发生错误");
			respCreateTransaction.setRetCode(-1);
			log.debug("查询账户发生错误");
			return ;
		}
		
		List<MultiTransactionInputImpl.Builder> inputs = new ArrayList<>();
		List<MultiTransactionOutputImpl.Builder> outputs = new ArrayList<>();
		AccountValueImpl account = accountMap.getAccount();
		if(list.isEmpty())
			return;
		Map<Integer,String> userAddressMap = new HashMap<>();
		//amount input
		MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
		input.setAddress(accountMap.getAddress());//发起方地址 *
		input.setNonce(account.getNonce());//交易次数 *
		input.setAmount("0");
		for(Object o : list) {
			CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
			
			if(!userAddressMap.containsKey(income.getUserId())) {
				CWVUserWallet wallet = propertyHelper.getWalletHelper().getUserAccount(income.getUserId(), CoinEnum.CWB);
				userAddressMap.put(income.getUserId(), wallet.getIncomeAddress());
			}
			
			input.setAmount(new BigDecimal(input.getAmount()).add(income.getAmount()).toString());

			//amount output
			MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
			output.setAddress(userAddressMap.get(income.getUserId()));//接收方地址 *
			output.setAmount(input.getAmount());
			
			outputs.add(output);
			
		}
		inputs.add(input);
		RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(inputs, outputs);
		if(ret.getRetCode() == 1) {
			propertyHelper.getCommonHelper().txManageAdd(TransactionTypeEnum.INCOME_CREATE.getKey(), ret.getTxHash());
			
			//根据用户Id更新income
			for(Object o : list){
				CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
				income.setChainStatus(ChainTransStatusEnum.START.getKey());
				income.setChainTransHash(ret.getTxHash());
			}
			propertyHelper.getDao().incomeDao.batchUpdate(list);
			
		}else if(ret.getRetCode() == -1 ){
			for(Object o : list){
				CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
				income.setStatus(PropertyIncomeStatusEnum.EXCEPTION.getValue());
			}
			propertyHelper.getDao().incomeDao.batchUpdate(list);

			log.error("income error : txhash="+ret.getTxHash());
		}
		
	}
	/**
	 * 发放收益成功后，处理用户显示的可领取的收益
	 */
	public void userIncomeCreateMasterSet() {
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		example.createCriteria().andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey())
		.andChainStatusClaimIsNull()
		.andMasterIsNull()
		;
		List<Object> list = propertyHelper.getDao().incomeDao.selectByExample(example);
		if(list.isEmpty())
			return;
		HashMap<Integer,BigDecimal> masterUserIdMap = new HashMap<>();
		for(Object o : list){
			CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
			if(masterUserIdMap.containsKey(income.getUserId())) {
				masterUserIdMap.put(income.getUserId(), masterUserIdMap.get(income.getUserId()).add(income.getAmount()));
			}else{
				masterUserIdMap.put(income.getUserId(), income.getAmount());
			}
			
		}
		
		
		//更新master数据
		CWVUserPropertyIncomeExample masterExample = new CWVUserPropertyIncomeExample();
		masterExample.createCriteria()
		.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey())
		.andMasterEqualTo(1);
		
		List<Object> listMaster = propertyHelper.getDao().incomeDao.selectByExample(masterExample);
		
		for(Integer userId : masterUserIdMap.keySet()){
			
		}
		
//		CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
//		income.setAmount(amount);
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
		.andStatusEqualTo(PropertyIncomeStatusEnum.NEW.getValue())
		.andPropertyIdEqualTo(property.getPropertyId());
		
		Object incomeO = propertyHelper.getDao().incomeDao.selectOneByExample(userPropertyIncomeExample);
		
		if(incomeO == null){
			final CWVUserPropertyIncome ordinaryIncome = new CWVUserPropertyIncome();
			ordinaryIncome.setPropertyId(property.getPropertyId());
			ordinaryIncome.setUserId(property.getUserId());
			ordinaryIncome.setAmount(incomeAmount);
			ordinaryIncome.setStartTime(new Date());
			ordinaryIncome.setStatus(PropertyIncomeStatusEnum.NEW.getValue());
			ordinaryIncome.setType(PropertyIncomeTypeEnum.DIVIDED.getValue());
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





