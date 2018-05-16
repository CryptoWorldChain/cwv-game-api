package org.brewchain.cwv.game.job;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.game.dao.Daos;

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
public class PropertyBidJobHandle extends ActWrapper implements ActorService, IActor {
	
	private static ScheduledExecutorService service = null;
	private final static int POOL_SIZE = 100;

	//间隔时间
	private final int numIntervalTime = 10;
	//线程大小
	private final int numThredSize = 1;
	private final int numZero = 0;
	private boolean bool = true;
	
	@ActorRequire(name="Daos", scope = "global")
	Daos dao;
	
	@Override
	public void onDaoServiceAllReady() {
		try {
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(dao == null) {
						try {
							Thread.sleep(5000L);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					if(dao != null) {
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
							service.scheduleAtFixedRate(new PropertyBidTask(dao), numZero, numIntervalTime, TimeUnit.SECONDS);
							
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
			service.scheduleAtFixedRate(new PropertyBidTask(dao), numZero, numIntervalTime, TimeUnit.MINUTES);
			
		}

	}

}
