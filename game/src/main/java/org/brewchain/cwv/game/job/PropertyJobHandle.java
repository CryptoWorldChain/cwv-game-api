package org.brewchain.cwv.game.job;

import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.helper.PropertyHelper;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.ntrans.api.ActWrapper;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.proxy.IActor;

/**
 * 竞拍状态定时任务
 * @author Moon
 * @date 2018-05-10
 */
@NActorProvider
@Slf4j
@Data
public class PropertyJobHandle extends ActWrapper implements ActorService, IActor {
	
	private static ScheduledExecutorService service = null;
	private final static int POOL_SIZE = 100;
	
	//间隔时间
	private final int numIntervalTime = 10;
	//线程大小
	private final int numThredSize = 5;
	private final int numZero = 0;
	private boolean bool = true;
	
	/**
	 * 房产超级账户地址
	 */
	public static String SYS_PROPERTY_ADDR = null;
	
	/**
	 * 手续费比例
	 */
	public static String EXCHANGE_CHARGE = null;
	
	public static String SYS_INCOME_ADDRESS = null ; //房產收益地址
	
	public static String MARKET_EXCHANGE_AGENT = null ; //房產收益地址
	
	
	@ActorRequire(name="Daos", scope = "global")
	Daos dao;
	
	@ActorRequire(name="Property_Helper")
	PropertyHelper propertyHelper;
	
	/**
	 * 查询失败处理，错误次数超过3次 加入交易人工处理表cwv_sys_tx_handle
	 */
	public static HashMap<String,Integer> queryErrorTXMap = new HashMap<>();
	@Override
	public void onDaoServiceAllReady() {
		try {
			log.info("PropertyBidJobHandle start ");
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(5000L);
						log.info("waiting for loading the dao beans ....");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(propertyHelper != null ) {
						if(SYS_PROPERTY_ADDR == null)
							SYS_PROPERTY_ADDR = propertyHelper.getCommonHelper().getSysSettingValue("sys_property_addr");
						if(EXCHANGE_CHARGE == null)
							EXCHANGE_CHARGE = propertyHelper.getCommonHelper().getSysSettingValue("exchange_charge");
						if(SYS_INCOME_ADDRESS == null)
							SYS_INCOME_ADDRESS = propertyHelper.getCommonHelper().getSysSettingValue("sys_income_address");
						if(MARKET_EXCHANGE_AGENT == null)
							MARKET_EXCHANGE_AGENT = propertyHelper.getCommonHelper().getSysSettingValue("market_exchange_agent");
						
						
						log.info("the dao beans loading success....");
						service = getService();
						if (service != null) {
							// 延迟0， 间隔1， 单位：SECONDS
//								service.scheduleAtFixedRate(new Runnable() {
//									public void run() {
//										try {
//											entrustEngineTask.execute();
//										} catch (Exception e) {
//											log.error("PropertyIncomeJobHandle startService run error...", e);
//										}
//									}
//								}, numZero, numOne, TimeUnit.MINUTES);
//								bool = false;
							// 分发任务
//								ExecutorService es = Executors.newFixedThreadPool(POOL_SIZE);
//								ExecutorService esSub = Executors.newFixedThreadPool(POOL_SIZE);
//								 延迟0， 间隔1， 单位：SECONDS
							log.info("job service start....");
//							service.scheduleAtFixedRate(new PropertyBidTask(propertyHelper), numZero, numIntervalTime, TimeUnit.SECONDS);
//							//任务开启时间 设置
//							service.scheduleAtFixedRate(new PropertyIncomeTask(propertyHelper), numZero, PropertyIncomeTask.DAY_PERIOD, TimeUnit.MINUTES);
//							//任务开启时间 设置
							service.scheduleAtFixedRate(new PropertyIncomeTask(propertyHelper), numZero, PropertyIncomeTask.DAY_PERIOD, TimeUnit.MINUTES);
							
							//任务开启时间 设置
							service.scheduleAtFixedRate(new TransactionStatusTask(propertyHelper), numZero, 5, TimeUnit.SECONDS);
							service.scheduleAtFixedRate(new PropertyExchangeBuyTask(propertyHelper), numZero, 5, TimeUnit.SECONDS);
//							service.scheduleAtFixedRate(new RandomInitTask(propertyHelper), numZero, 5, TimeUnit.SECONDS);
							service.scheduleAtFixedRate(new PropertyDrawTask(propertyHelper), numZero, 5, TimeUnit.SECONDS);
							
						}
					}
				}
			}).start();
		} catch (Exception e) {
			log.warn("PropertyIncomeJobHandle onDaoServiceAllReady error...", e);
		}
	}

	public ScheduledExecutorService getService() {
		try {
			if (service != null) {
				service.shutdown();
			}
			service = new ScheduledThreadPoolExecutor(numThredSize,
					new BasicThreadFactory.Builder().namingPattern("entrust-engine-schedule-pool-%d").daemon(true).build());
		} catch (Exception e) {
			log.warn("PropertyIncomeJobHandle getService error...",e);
		}
		
		return service;
	}

	private void startService() throws InterruptedException {
		if (service != null) {
			// 延迟0， 间隔1， 单位：SECONDS
//			service.scheduleAtFixedRate(new Runnable() {
//				public void run() {
//					try {
//						entrustEngineTask.execute();
//					} catch (Exception e) {
//						log.error("PropertyIncomeJobHandle startService run error...", e);
//					}
//				}
//			}, numZero, numOne, TimeUnit.MINUTES);
//			bool = false;
			// 分发任务
//			ExecutorService es = Executors.newFixedThreadPool(POOL_SIZE);
//			ExecutorService esSub = Executors.newFixedThreadPool(POOL_SIZE);
//			 延迟0， 间隔1， 单位：SECONDS
//			service.scheduleAtFixedRate(new PropertyBidTask(propertyHelper), numZero, numIntervalTime, TimeUnit.MINUTES);
//			service.scheduleAtFixedRate(new PropertyIncomeTask(propertyHelper), numZero, numIntervalTime, TimeUnit.MINUTES);
//			
		}

	}

}
