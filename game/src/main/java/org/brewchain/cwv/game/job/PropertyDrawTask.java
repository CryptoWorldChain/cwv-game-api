package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.PropertyBidStatusEnum;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
import org.brewchain.cwv.game.enums.TransHashTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 房产竞拍状态处理定时任务
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class PropertyDrawTask implements Runnable {

	private PropertyHelper propertyHelper ;
	
	public PropertyDrawTask(PropertyHelper propertyHelper) {
		this.propertyHelper = propertyHelper;
	}

	@Override
	public void run() {
		log.info("PropertyDrawTask start ....");
		
		drawProcess(propertyHelper.getDao());
		
		drawTransStatus();
		log.info("PropertyDrawTask ended ....");
	}
	
	
	private void drawProcess(Daos dao){
//		SessionFilter.userMap
		CWVMarketDrawExample drawExample = new CWVMarketDrawExample();
		drawExample.createCriteria().andChainStatusIsNull();
		List<Object> list = dao.bidDao.selectByExample(drawExample);
		String outputAddress = "";
		for(Object o : list) {
			CWVMarketDraw draw = (CWVMarketDraw) o;
			String token = draw.getChainContract();
			RespCreateTransaction.Builder ret = propertyHelper.getWltHelper().createTx(new BigDecimal(0), outputAddress, PropertyJobHandle.SYS_PROPERTY_ADDR, "house", token);
			if(ret.getRetCode() == 1) {
				draw.setChainStatus(ChainTransStatusEnum.START.getKey());
				draw.setChainTransHash(ret.getTxHash());
			}else if(ret.getRetCode() == -1 ){
				drawRollBack(draw.getUserId());
			}
			
		}
		
	}
	
	/**
	 * 回滚抽奖次数
	 * @param userId
	 */
	private void drawRollBack(Integer userId) {
		CWVUserWallet userWallet = propertyHelper.getWalletHelper().getUserAccount(userId, CoinEnum.CWB);
		userWallet.setDrawCount(userWallet.getDrawCount()+1);
		propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(userWallet);
	}
	
	/**
	 * 抽奖交易状态处理
	 */
	private void drawTransStatus(){
		CWVMarketDrawExample drawExample = new CWVMarketDrawExample();
		drawExample.createCriteria().andChainStatusIsNull();
		List<Object> list = propertyHelper.getDao().bidDao.selectByExample(drawExample);
		for(Object o : list){
			final CWVMarketDraw draw = (CWVMarketDraw) o;
			HashMap busiMap = new HashMap<String,String>();
			busiMap.put("drawId", draw.getDrawId());
			busiMap.put("txHash", draw.getChainTransHash());
			
			String status = TransactionStatusTask.getTransStatus(propertyHelper, draw.getChainTransHash(), TransHashTypeEnum.DRAW.getValue(), busiMap);
			
			if(StringUtils.isEmpty(status)) {
				continue;
			}
			
			if(status.equals(ChainTransStatusEnum.DONE.getValue())) {
				draw.setChainStatus(ChainTransStatusEnum.DONE.getKey());
				final CWVGameProperty property =  new CWVGameProperty();
				property.setPropertyId(draw.getPropertyId());
				property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
				property.setUserId(draw.getUserId());
				property.setLastPrice(new BigDecimal("1000"));
				property.setLastPriceTime(new Date());
				
				propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {
					
					@Override
					public Object doInTransaction() {

						propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
						propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
						
						return null;
					}
				});
			}
			else if(status.equals(ChainTransStatusEnum.ERROR.getValue())) {
				draw.setChainStatus(ChainTransStatusEnum.ERROR.getKey());
				propertyHelper.getDao().drawDao.doInTransaction(new TransactionExecutor() {
					
					@Override
					public Object doInTransaction() {
						propertyHelper.getDao().drawDao.updateByPrimaryKeySelective(draw);
						drawRollback(draw);
						return null;
					}
				});
				
			}
		}
	}

	private void drawRollback(CWVMarketDraw draw) {

		CWVUserWallet wallet = propertyHelper.getWalletHelper()
				.getUserAccount(draw.getUserId(), CoinEnum.CWB); 
		wallet.setDrawCount(wallet.getDrawCount()+1);
		
	}
	 
	
}
