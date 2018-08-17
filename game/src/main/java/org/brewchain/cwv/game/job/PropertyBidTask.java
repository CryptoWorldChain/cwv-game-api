package org.brewchain.cwv.game.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.PropertyBidStatusEnum;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
import org.brewchain.cwv.game.enums.TransactionTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 房产竞拍状态处理定时任务
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class PropertyBidTask implements Runnable {

	private PropertyHelper propertyHelper ;
	private HashMap<Integer,Integer> auctionEndRecord ;
	public PropertyBidTask(PropertyHelper propertyHelper) {
		this.propertyHelper = propertyHelper;
	}
	
	@Override
	public void run() {
		log.info("PropertyBidTask start ....");
		try {
			biddingSet(propertyHelper.getDao());
			noticeSet(propertyHelper.getDao());
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	
		log.info("PropertyBidTask ended ....");
	}
	
	
	public void biddingSet(Daos dao){
//		SessionFilter.userMap
		log.info("biddingSet start ....");
		CWVMarketBidExample bidExample = new CWVMarketBidExample();
		bidExample.createCriteria().andStatusEqualTo((byte)0)
		.andAuctionStartLessThanOrEqualTo(new Date())
		.andAuctionEndGreaterThanOrEqualTo(new Date());
		int count = dao.bidDao.countByExample(bidExample);
		if(count>0) {
			CWVMarketBid record = new CWVMarketBid();
			record.setStatus(PropertyBidStatusEnum.BIDDING.getValue());
			dao.bidDao.updateByExampleSelective(record, bidExample);
		}
		
		
	}
	
	public void noticeSet(Daos dao){
//		SessionFilter.userMap
		log.info("noticeSet start ....");
		CWVMarketBidExample bidExample = new CWVMarketBidExample();
		bidExample.createCriteria().andStatusLessThanOrEqualTo((byte)1)
		.andAuctionEndLessThanOrEqualTo(new Date());
		List<Object> list = dao.bidDao.selectByExample(bidExample);
		
		if(list != null && list.size()>0) {
			CWVMarketBid record = new CWVMarketBid();
			record.setStatus(PropertyBidStatusEnum.NOTICE.getValue());
			for(Object o : list) {
				final CWVMarketBid bid = (CWVMarketBid) o;
				noticeSetProcess(bid,propertyHelper);
				//调取合约结束方法-
				auctionEnd(bid, propertyHelper);
			}
//			dao.bidDao.updateByExampleSelective(record, bidExample);
			
		}
		
	}
	
	/**
	 * 竞拍结束处理
	 * @param userId
	 * @param address
	 * @param propertyHelper
	 */
	public static void auctionEnd(CWVMarketBid bid, PropertyHelper propertyHelper) {
		
		CWVUserWallet wallet = propertyHelper.getWalletHelper().getUserAccount(Integer.parseInt(bid.getCreateUser()), CoinEnum.CWB);
		RespCreateTransaction.Builder ret = propertyHelper.getBidInvoker().auctionEnd(wallet.getAccount(), bid.getChainContract());
		if(ret.getRetCode() == 1) {
			
			bid.setChainTransHashEnd(ret.getTxHash());
			bid.setChainStatusEnd(ChainTransStatusEnum.START.getKey());
			propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
			//插入交易管理
			propertyHelper.getCommonHelper().txManageAdd(TransactionTypeEnum.BID_AUCTION_END.getKey(),ret.getTxHash());
			
		}else{//异常管理
			if(bid.getChainStatusEnd() == ChainTransStatusEnum.ERROR.getKey() || bid.getChainStatusEnd() == null){
				try {
					Thread.currentThread().sleep(60*1000*5);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				auctionEnd(bid, propertyHelper);
			}
			propertyHelper.getCommonHelper().marketExceptionAdd(TransactionTypeEnum.BID_AUCTION_END, bid.getBidId(), String.format("竞拍[%s]结束失败   [%s]",bid.getBidId(),ret.getRetMsg()));
			
		}
		
	}
	
	
	public static void noticeSetProcess(final CWVMarketBid bid, final PropertyHelper propertyHelper) {
		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(bid.getGamePropertyId());
		final CWVGameProperty property = propertyHelper.getDao().gamePropertyDao.selectByPrimaryKey(gameProperty);
		
//		if(!property.getPropertyStatus().equals(PropertyStatusEnum.BIDDING.getValue()))
//			continue;
		//查询竞价最高者
		CWVMarketAuction auctionMax = propertyHelper.getMaxAuction(bid.getBidId());
		if(auctionMax !=null) {
			//调取钱包查询竞拍数据
			
			//更新竞拍信息
			CWVAuthUser userMax = propertyHelper.getUserHelper().getUserById(auctionMax.getUserId());
			bid.setOwner(userMax.getUserId());
			bid.setLastPrice(auctionMax.getBidPrice());
			bid.setStatus(PropertyBidStatusEnum.NOTICE.getValue());
			//更新房产信息
			property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
			property.setUserId(userMax.getUserId());
			property.setLastPrice(auctionMax.getBidPrice());
			property.setLastPriceTime(new Date());
			//由于提前扣款 不存在更新账户金额 
			
			//回退为竞拍成功者资金
			CWVMarketAuctionExample auctionExample = new CWVMarketAuctionExample();
			auctionExample.createCriteria().andBidIdEqualTo(bid.getBidId())
			.andUserIdNotEqualTo(userMax.getUserId()).andStatusEqualTo((byte) 1);
			
			final List<Object> listAuction = propertyHelper.getDao().auctionDao.selectByExample(auctionExample);
			
			propertyHelper.getDao().gamePropertyDao.doInTransaction(new TransactionExecutor() {
				
				@Override
				public Object doInTransaction() {
					
					//调取钱包查询竞拍数据
					
					//更新竞拍信息
					propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
					//更新房产信息
					propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
					//由于提前扣款 ，更新账户金额 查询钱包更新
					//由于提前扣款，不存在账户交易记录
					//回退为竞拍成功者资金
					for(Object a : listAuction) {
						CWVMarketAuction aucton = (CWVMarketAuction) a;
						CWVUserWallet aucitonWallet = propertyHelper.getWalletHelper().getUserAccount(aucton.getUserId(), CoinEnum.CWB);
						aucitonWallet.setBalance(aucitonWallet.getBalance().add(aucton.getBidPrice()));
						aucitonWallet.setUpdateTime(new Date());
						aucton.setStatus((byte) 2 );
						propertyHelper.getDao().walletDao.updateByPrimaryKeySelective(aucitonWallet);
						
						propertyHelper.getDao().auctionDao.updateByPrimaryKeySelective(aucton);
					}
					return null;
				}
			});
			
		}else{//流拍
			bid.setStatus(PropertyBidStatusEnum.NOBID.getValue());
			bid.setLastUpdateTime(new Date());
			property.setPropertyStatus(PropertyStatusEnum.NOSALE.getValue());
			propertyHelper.getDao().gamePropertyDao.doInTransaction(new TransactionExecutor() {
				
				@Override
				public Object doInTransaction() {
					propertyHelper.getDao().gamePropertyDao.updateByPrimaryKeySelective(property);
					propertyHelper.getDao().bidDao.updateByPrimaryKeySelective(bid);
					return null;
				}
			});
		}
		
	}
	 
	

}
