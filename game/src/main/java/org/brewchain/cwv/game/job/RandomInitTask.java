package org.brewchain.cwv.game.job;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.auth.enums.ContractTypeEnum;
import org.brewchain.cwv.auth.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletExample;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.MarketTypeEnum;
import org.brewchain.cwv.game.enums.PropertyBidStatusEnum;
import org.brewchain.cwv.game.enums.PropertyExchangeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
import org.brewchain.cwv.game.enums.PropertyTypeEnum;
import org.brewchain.cwv.game.enums.TransHashTypeEnum;
import org.brewchain.cwv.game.helper.GameNoticeHelper;
import org.brewchain.cwv.game.helper.GameNoticeHelper.NoticeTradeTypeEnum;
import org.brewchain.cwv.game.helper.GameNoticeHelper.NoticeTypeEnum;
import org.brewchain.cwv.game.helper.PropertyHelper;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

import lombok.extern.slf4j.Slf4j;
import onight.tfw.ojpa.api.TransactionExecutor;

/**
 * 定时任务生成指纹信息随机数
 * @author Moon
 * @date 2018-05-15
 */
@Slf4j
public class RandomInitTask implements Runnable {
	//防止收益数据冲突
	public static HashSet<Integer> jobIncomeSet = new HashSet<>();
	
	public final static float RATE = 0.25f;
	public final static float ORDINARY_TO_DIVIDED = 0.4f;
	public final static float TYPICAL_TO_DIVIDED = 0.6f;
	
	public final static int DAY_PERIOD = 7 ; //7日
	public final static String INCOME_TIME = "income_time" ; //7日
	
	private PropertyHelper propertyHelper;
	
	
	public RandomInitTask(PropertyHelper propertyHelper) {
		super();
		this.propertyHelper = propertyHelper;
	}


	@Override
	public void run() {
		log.info("TransactionStatusTask start ....");
		//1 执行交易 卖出，撤销卖出 
			//买入，放入单独任务
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andCoinTypeEqualTo((byte) CoinEnum.CWB.getValue());
//		.andAccountIsNotNull();
		
		List<Object> list = propertyHelper.getDao().walletDao.selectByExample(example);
		StringBuffer sb = new StringBuffer();
		boolean go1 = true;
		boolean go2 = true;
		while(go1) {
			for(Object o : list) {
				try {
					Thread.currentThread().sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				CWVUserWallet wallet = (CWVUserWallet) o;
				try {
					RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
					
					String data1 = propertyHelper.getWltHelper().excuteContract("1", "fingerprintInfo",wallet.getAccount()+new Date().getTime());
					String contractAddress = propertyHelper.getDrawInvoker().getContractAddress(ContractTypeEnum.RANDOM_CONTRACT.getName());
					if(contractAddress == null) {
						return ;
					}
					ret = propertyHelper.getWltHelper().excuteContract(new BigDecimal(0), wallet.getAccount(), contractAddress,data1);
					
					if(ret.getRetCode()==1) {
						System.out.println(ret.getTxHash());
						sb.append("|"+ret.getTxHash());
					}
				} catch (Exception e) {
					continue ;
				}
				
			}
			go1 = false;
		}
		sb.append("=");
		while(go2) {
			for(Object o : list) {
				try {
					Thread.currentThread().sleep(3000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				CWVUserWallet wallet = (CWVUserWallet) o;
				try {
					RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
					
					String data2 = propertyHelper.getWltHelper().excuteContract("1", "computerRandomNumber");
					String contractAddress = propertyHelper.getDrawInvoker().getContractAddress(ContractTypeEnum.RANDOM_CONTRACT.getName());
					if(contractAddress == null) {
						return ;
					}
					ret = propertyHelper.getWltHelper().excuteContract(new BigDecimal(0), wallet.getAccount(), contractAddress,data2);
				
					if(ret.getRetCode()==1) {
						System.out.println(ret.getTxHash());
						sb.append("|"+ret.getTxHash());
					}
				} catch (Exception e) {
					continue ;
				}
				
			}
			go2 = false;
		}
		log.info("TransactionStatusTask ended ....");
	}
	
}





