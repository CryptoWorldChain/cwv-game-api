package org.brewchain.cwv.game.helper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecordExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletTopup;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PSCommon;
import org.brewchain.cwv.service.game.User.PRetAccountTopup;
import org.brewchain.cwv.service.game.User.PRetWalletAccount;
import org.brewchain.cwv.service.game.User.PRetWalletAccount.WalletAccount;
import org.brewchain.cwv.service.game.User.PRetWalletAccountBalance.Builder;
import org.brewchain.cwv.service.game.User.PRetWalletInfo;
import org.brewchain.cwv.service.game.User.PRetWalletRecord;
import org.brewchain.cwv.service.game.User.PRetWalletRecord.WalletRecord;
import org.brewchain.cwv.service.game.User.PSAccountTopup;
import org.brewchain.cwv.service.game.User.PSWalletAccount;
import org.brewchain.cwv.service.game.User.PSWalletAccountBalance;
import org.brewchain.cwv.service.game.User.PSWalletRecord;

import freemarker.core.ReturnInstruction.Return;
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
@Instantiate(name="Wallet_Helper")
public class WalletHelper implements ActorService {
	enum Coin{
		CWB(0),
		CWV(1),
		ETH(2);
		private int value;
		Coin(int value){
			this.value = value;		
		}
		public int getValue() {
			return value;
		}
		
	}

	@ActorRequire(name="Daos")
	Daos dao;
	
	@ActorRequire(name="User_Helper", scope = "global")
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
		List<Object> list = this.dao.getWalletDao().selectByExample(example);
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
			int count  = dao.walletDao.countByExample(example);
			page.setTotalCount(count);
			ret.setPage(page.getPageOut());
		}
		
		
		List<Object> list = dao.walletDao.selectByExample(example);
		if(list != null && !list.isEmpty()) {
			for(Object o: list) {
				CWVUserWallet wallet = (CWVUserWallet) o;
				WalletAccount.Builder account = WalletAccount.newBuilder();
				account.setAccountId(wallet.getWalletId()+"");
				account.setAssert(wallet.getBalance().doubleValue());
				//实时查询行情 TODO
				account.setMarketPrice(0.002);
				account.setCoinType(wallet.getCoinType().toString());
				account.setIcon(wallet.getCoinIcon());
				ret.addAccount(account);
			}
		}

		
		
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		
	}
	

	public void walletRecord(FramePacket pack, PSWalletRecord pb, PRetWalletRecord.Builder ret) {
		//
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		PageUtil pageUtil = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		CWVUserTransactionRecordExample example = new CWVUserTransactionRecordExample();
		example.createCriteria().andUserIdEqualTo(authUser.getUserId());
		int count = dao.userTransactionRecordDao.countByExample(example);
		pageUtil.setTotalCount(count);
		List<Object> list = dao.userTransactionRecordDao.selectByExample(example);
		for(Object o: list) {
			CWVUserTransactionRecord record = (CWVUserTransactionRecord) o;
//			string record_id = 1;//记录ID
//			string create_time = 2;//时间
//			string detail = 3; //明细
//			string amount = 4; //金额 
			WalletRecord.Builder walletRecord = WalletRecord.newBuilder();
			walletRecord.setRecordId(record.getRecordId()+"");
			walletRecord.setAmount(record.getGainCost().toString());
			walletRecord.setCreateTime(DateUtil.getDayTime(record.getCreateTime()));
			walletRecord.setDetail(record.getDetail());
			ret.addRecord(walletRecord);
		}
		ret.setPage(pageUtil.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		
	}



	/**
	 * 查询余额接口
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void walletAccountBalance(FramePacket pack, PSWalletAccountBalance pb, Builder ret) {
		
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		CWVUserWallet account =  getAccountByUserId(authUser.getUserId()+"",pb.getCoinType());
		if(account == null ) {
			ret.setRetCode(ReturnCodeMsgEnum.WAB_ERROR_TYPE.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.WAB_ERROR_TYPE.getRetMsg());
			return ;
		}

		ret.setBalance(account.getBalance().doubleValue());
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		
	}
	
	/**
	 * 根据用户ID和账户类型查询单个账户
	 * @param userId
	 * @param coinType
	 * @return
	 */
	public CWVUserWallet getAccountByUserId(String userId, String coinType){
		CWVUserWalletExample example = new CWVUserWalletExample();
		CWVUserWalletExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(Integer.parseInt(userId));
		
		//校验 账户类型
		if(StringUtils.isEmpty(coinType)) {//默认CWB
			criteria.andCoinTypeEqualTo((byte) Coin.CWB.value);
		}else{
			criteria.andCoinTypeEqualTo((byte) Integer.parseInt(coinType));
		}
		
		List<Object> list = dao.walletDao.selectByExample(example);
		
		return list == null || list.isEmpty() ? null : (CWVUserWallet) list.get(0);
	}




	public void accountTopup(FramePacket pack, PSAccountTopup pb, PRetAccountTopup.Builder ret) {
		//校验
		if(StringUtils.isEmpty(pb.getAmount())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("充值金额不能为空");
			return ;
		}
		CWVUserWalletTopup topup = new CWVUserWalletTopup();
		//充值操作
		if(StringUtils.isEmpty(pb.getCoinType())) {
			topup.setCoinType((byte) Coin.CWB.value);
		}else {
			topup.setCoinType(Byte.parseByte(pb.getCoinType()));
		}
		
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		CWVUserWallet wallet = getUserAccount(user.getUserId(), Coin.CWB);
		
		topup.setAddress(wallet.getAccount());
		topup.setAmount(new BigDecimal(pb.getAmount()));
		topup.setUserId(user.getUserId());
		topup.setStatus((byte) 0);
		topup.setCreateTime(new Date());
		dao.topupDao.insert(topup);
		
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setAmount(topup.getAmount().doubleValue());
		
	}


	public void walletInfo(FramePacket pack, PSCommon pb,
			PRetWalletInfo.Builder ret) {
		// TODO Auto-generated method stub
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andUserIdEqualTo(authUser.getUserId());
		
		double total = 0;
		List<Object> list = dao.walletDao.selectByExample(example);
		if(list != null && !list.isEmpty()) {
			for(Object o: list) {
				CWVUserWallet wallet = (CWVUserWallet) o;
				total = total + wallet.getBalance().doubleValue() * 0.002;
				if(wallet.getCoinType().intValue() == Coin.CWB.value) {
					ret.setCwbTopup(wallet.getTopupBalance().toString());//充值CWB
					ret.setCwbAmount(wallet.getBalance().doubleValue());
					ret.setDrawCount(wallet.getDrawCount());
				}
				
			}
		}
		
		ret.setTotalValueCNY(total);
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
	}




	public void gameShare(FramePacket pack, org.brewchain.cwv.service.game.User.PRetGameShare.Builder ret) {
		
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		
		//查询分享链接 TODO
		
		
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg())
		.setUrl("/share_user="+authUser.getUserId());
	}
	
}