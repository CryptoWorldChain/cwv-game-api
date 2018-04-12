package org.brewchain.cwv.game.helper;

import java.util.List;

import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Wallet.PRetWalletAccount;
import org.brewchain.cwv.service.game.Wallet.PRetWalletAccount.WalletAccount;
import org.brewchain.cwv.service.game.Wallet.PRetWalletRecord;
import org.brewchain.cwv.service.game.Wallet.PSWalletAccount;
import org.brewchain.cwv.service.game.Wallet.PSWalletRecord;

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
 * 钱包账户操作
 * 
 * @author Moon
 * @date 2018-03-30
 */
public class WalletHelper implements ActorService {

	@ActorRequire
	Daos daos;
	
	private static String CONTRACT_ADDRESS_BID = "";
	private static String CONTRACT_ADDRESS_DRAW = "";
	

	@ActorRequire(scope = "global")
	UserHelper userHelper;
	
	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	
	
	/**
	 * 创建CWB账户
	 * @param pwd
	 * @return retCode:retMsg 1:账户地址;2:异常信息
	 */
	
	public PRetCommon.Builder CWBAccountCreate(String pwd){
		
		
		
		return null;
	}
	
	
	/**
	 * CWB账户充值
	 * @param pwd
	 * @return retCode:retMsg 1:账户地址;2:异常信息
	 */
	
	public PRetCommon.Builder CWBAccountTopup(String from, String to, Double amount, String sign){
		
		
		
		return null;
	}
	
	
	/**
	 * CWB账户充值
	 * @param pwd
	 * @return retCode:retMsg 1:账户地址;2:异常信息
	 */
	
	public double CWBAccountBalance(String account, String pwd ){
		
		
		
		return 10000000;
	}
	/**
	 * CWB账户交易
	 * @param pwd
	 * @return retCode:retMsg 1:账户地址;2:异常信息
	 */
	
	public PRetCommon.Builder CWBAccountTransfer(String from, String to, Double amount, String sign){
		
		
		
		return null;
	}


	public PRetCommon.Builder invokeContract(String address,String sendData,String account,String sign){
		
		return null;
	}
	
	public PRetCommon.Builder invokeBidContract(String sendData,String account,String sign){
		
		return invokeContract(CONTRACT_ADDRESS_BID, sendData, account, sign);
	}
	public PRetCommon.Builder invokeDrawContract(String sendData,String account,String sign){
		
		return invokeContract(CONTRACT_ADDRESS_DRAW, sendData, account, sign);
	}



	/**
	 * 查询钱包账户
	 * @param pb
	 * @param ret
	 */
	public void walletAccount(PSWalletAccount pb, PRetWalletAccount.Builder ret) {
		
		CWVAuthUser authUser = userHelper.getUserByPhone("");
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andUserIdEqualTo(1);
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		example.setOffset(page.getOffset());
		example.setLimit(page.getLimit());
		int count  = daos.walletDao.countByExample(example);
		page.setTotalCount(count);
		
		List<Object> list = daos.walletDao.selectByExample(example);
		for(Object o: list) {
			CWVUserWallet wallet = (CWVUserWallet) o;
			WalletAccount.Builder account = WalletAccount.newBuilder();
			account.setAccountId(wallet.getWalletId()+"");
			account.setAssert(wallet.getBalance().doubleValue());
//			account.setCoinType(wallet.getCoinType());
//			account.setIcon(wallet.getIcon());
			ret.addAccount(account);
		}
		
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		
	}



	public void walletRecord(PSWalletRecord pb, PRetWalletRecord.Builder ret) {
		
	}

	
	
	
	
	

}
