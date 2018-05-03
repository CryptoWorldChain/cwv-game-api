package org.brewchain.cwv.game.helper;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.User.PRetWalletAccount;
import org.brewchain.cwv.service.game.User.PRetWalletAccount.WalletAccount;
import org.brewchain.cwv.service.game.User.PRetWalletRecord;
import org.brewchain.cwv.service.game.User.PSWalletAccount;
import org.brewchain.cwv.service.game.User.PSWalletRecord;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.beans.FramePacket;

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
	enum Coin{
		CWB(0),
		CWV(1),
		ETH(2);
		private int value;
		Coin(int value){
			this.value = value;		
		}
	}

	@ActorRequire
	Daos daos;
	
	@ActorRequire(scope = "global")
	UserHelper userHelper;
	
	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	
	
	
	/**
	 * 查询账户
	 * @param userId
	 * @param coinType
	 * @return
	 */
	
	public CWVUserWallet getUserAccount(Integer userId, Coin coin ){
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andUserIdEqualTo(userId)
		.andCoinTypeEqualTo((byte) coin.value);
		List<Object> list = this.daos.getWalletDao().selectByExample(example);
		return list == null || list.isEmpty()? null : (CWVUserWallet)list.get(0);
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



	/**
	 * 查询钱包账户
	 * @param pb
	 * @param ret
	 */
	public void walletAccount(FramePacket pack, PSWalletAccount pb, PRetWalletAccount.Builder ret) {
		
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andUserIdEqualTo(authUser.getUserId());
		
		//校验 账户类型
		if(!StringUtils.isEmpty(pb.getCoinType())) {//查询单个账户
			example.createCriteria().andCoinTypeEqualTo(Byte.parseByte(pb.getCoinType()));
		}else{
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			example.setOffset(page.getOffset());
			example.setLimit(page.getLimit());
			int count  = daos.walletDao.countByExample(example);
			page.setTotalCount(count);
			ret.setPage(page.getPageOut());
		}
		
		
		List<Object> list = daos.walletDao.selectByExample(example);
		if(list == null || list.isEmpty()) {
			ret.setRetCode(ReturnCodeMsgEnum.WAS_ERROR_ACCOUNT.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.WAS_ERROR_ACCOUNT.getRetMsg());
			return ;
		}

		for(Object o: list) {
			CWVUserWallet wallet = (CWVUserWallet) o;
			WalletAccount.Builder account = WalletAccount.newBuilder();
			account.setAccountId(wallet.getWalletId()+"");
			account.setAssert(wallet.getBalance().doubleValue());
			account.setCoinType(wallet.getCoinType().toString());
			account.setIcon(wallet.getCoinIcon());
			ret.addAccount(account);
		}
		
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		
	}
	

	public void walletRecord(PSWalletRecord pb, PRetWalletRecord.Builder ret) {
		
	}

	
	
	
	
	

}
