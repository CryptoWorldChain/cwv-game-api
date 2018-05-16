package org.brewchain.cwv.game.job;

import java.util.Date;

import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.util.DateUtil;

/**
 * 房产竞拍状态处理定时任务
 * @author Moon
 * @date 2018-05-15
 */
public class PropertyBidTask implements Runnable {

	enum PropertyBidStatus{
		PREVIEW("0"),// 预竞拍
		BIDDING("1"), //竞拍中
		NOTICE("2"); //公示中
		private String value;
		PropertyBidStatus(String value){
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
	private Daos dao ;
	
	public PropertyBidTask(Daos dao) {
		this.dao = dao;
	}
	
	@Override
	public void run() {
		
		biddingSet(dao);
		
		noticeSet(dao);
	}
	
	
	public void biddingSet(Daos dao){
//		SessionFilter.userMap
		CWVMarketBidExample bidExample = new CWVMarketBidExample();
		bidExample.createCriteria().andStatusEqualTo((byte)0)
		.andAuctionStartLessThanOrEqualTo(new Date())
		.andAuctionEndGreaterThanOrEqualTo(new Date());
		int count = dao.bidDao.countByExample(bidExample);
		if(count>0) {
			CWVMarketBid record = new CWVMarketBid();
			record.setStatus(Byte.parseByte(PropertyBidStatus.BIDDING.getValue()));
			dao.bidDao.updateByExampleSelective(record, bidExample);
		}
		
		
	}
	
	public void noticeSet(Daos dao){
//		SessionFilter.userMap
		CWVMarketBidExample bidExample = new CWVMarketBidExample();
		bidExample.createCriteria().andStatusLessThanOrEqualTo((byte)1)
		.andAuctionEndLessThanOrEqualTo(new Date());
		int count = dao.bidDao.countByExample(bidExample);
		if(count>0) {
			CWVMarketBid record = new CWVMarketBid();
			record.setStatus(Byte.parseByte(PropertyBidStatus.NOTICE.getValue()));
			dao.bidDao.updateByExampleSelective(record, bidExample);
		}
		
		
	}
	 

}
