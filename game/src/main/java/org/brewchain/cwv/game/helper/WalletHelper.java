package org.brewchain.cwv.game.helper;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.user.entity.CWVUserRechargeAddress;
import org.brewchain.cwv.dbgens.user.entity.CWVUserRechargeAddressExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserSendRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserSendRecordExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecordExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletTopup;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletTopupExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Exchange.ReqCreateTx;
import org.brewchain.cwv.service.game.Exchange.ReqGetTxRecord;
import org.brewchain.cwv.service.game.Exchange.ResCreateTx;
import org.brewchain.cwv.service.game.Exchange.ResGetTxRecord;
import org.brewchain.cwv.service.game.Exchange.TxRecord;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PSCommon;
import org.brewchain.cwv.service.game.Game.RetCodeMsg;
import org.brewchain.cwv.service.game.Game.RetData;
import org.brewchain.cwv.service.game.Game.RetData.AccountInfo;
import org.brewchain.cwv.service.game.Game.WalletAccount;
import org.brewchain.cwv.service.game.User.PRetAccountTopup;
import org.brewchain.cwv.service.game.User.PRetAccountTopupRecord;
import org.brewchain.cwv.service.game.User.PRetAccountTopupRecord.TopupRecord;
import org.brewchain.cwv.service.game.User.PRetWalletAccount;
import org.brewchain.cwv.service.game.User.PRetWalletAccountBalance.Builder;
import org.brewchain.cwv.service.game.User.PRetWalletRecord;
import org.brewchain.cwv.service.game.User.PRetWalletRecord.WalletRecord;
import org.brewchain.cwv.service.game.User.PSAccountTopup;
import org.brewchain.cwv.service.game.User.PSRecharge;
import org.brewchain.cwv.service.game.User.PSWalletAccount;
import org.brewchain.cwv.service.game.User.PSWalletAccountBalance;
import org.brewchain.cwv.service.game.User.PSWalletRecord;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetAccount;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.ojpa.api.TransactionExecutor;
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
	

	@ActorRequire(name="Daos")
	Daos dao;
	
	@ActorRequire(name="User_Helper", scope = "global")
	UserHelper userHelper;
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
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
	
	public CWVUserWallet getUserAccount(Integer userId, CoinEnum coin ){
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andUserIdEqualTo(userId)
		.andCoinTypeEqualTo((byte) coin.getValue());
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
		RespGetAccount.Builder accInfo = wltHelper.getAccountInfo(account.getAccount());
		if(accInfo.getRetCode()==1){
			if(account.getBalance().longValue() != accInfo.getAccount().getBalance()) {
				account.setBalance(new BigDecimal(accInfo.getAccount().getBalance()));
				dao.walletDao.updateByPrimaryKeySelective(account);
			}
			
		}
		ret.setAddress(account.getAccount());
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
			criteria.andCoinTypeEqualTo((byte) CoinEnum.CWB.getValue());
		}else{
			criteria.andCoinTypeEqualTo((byte) Integer.parseInt(coinType));
		}
		
		List<Object> list = dao.walletDao.selectByExample(example);
		if(list.isEmpty()){
			log.debug("账户信息错误");
			return null;
		}
		CWVUserWallet wallet = (CWVUserWallet) list.get(0);
		RespGetAccount.Builder accountInfo = wltHelper.getAccountInfo(wallet.getAccount());
		if(accountInfo.getRetCode()==1){
			wallet.setBalance(new BigDecimal(accountInfo.getAccount().getBalance()));
		}
		return wallet;
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
			topup.setCoinType((byte) CoinEnum.CWB.getValue());
		}else {
			topup.setCoinType(Byte.parseByte(pb.getCoinType()));
		}
		
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		CWVUserWallet wallet = getUserAccount(user.getUserId(), CoinEnum.CWB);
		
		topup.setAddress(wallet.getAccount());
		topup.setAmount(new BigDecimal(pb.getAmount()));
		topup.setUserId(user.getUserId());
//		topup.setStatus((byte) 0);
		topup.setStatus((byte) 1);
		topup.setCreateTime(new Date());
		dao.topupDao.insert(topup);
		int countHistory = wallet.getBalance().intValue()/1000;
		
		wallet.setBalance(wallet.getBalance().add(new BigDecimal(pb.getAmount())));
		int countNew = wallet.getBalance().intValue()/1000;
		
		wallet.setDrawCount(wallet.getDrawCount()+(countNew-countHistory));
		wallet.setTopupBalance(wallet.getTopupBalance().add(new BigDecimal(pb.getAmount())));
		dao.walletDao.updateByPrimaryKeySelective(wallet);
		
		CWVUserRechargeAddressExample example = new CWVUserRechargeAddressExample();
		
		int count = dao.rechargeAddressDao.countByExample(example);
		int offset = (int) (Math.random() * count);
		example.setOffset(offset);
		example.setLimit(1);
		CWVUserRechargeAddress address = (CWVUserRechargeAddress) dao.rechargeAddressDao.selectOneByExample(example);
		ret.setAddress(address.getRechargeAddress());
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setAmount(topup.getAmount().doubleValue());
		
	}


	public void walletInfo(FramePacket pack, PSCommon pb,
			PRetCommon.Builder ret, RetCodeMsg.Builder builder) {
		// TODO Auto-generated method stub
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andUserIdEqualTo(authUser.getUserId());
		
		double total = 0;
		List<Object> list = dao.walletDao.selectByExample(example);
		AccountInfo.Builder accountInfo = AccountInfo.newBuilder();
		RetData.Builder data = RetData.newBuilder();
		if(list != null && !list.isEmpty()) {
			for(Object o: list) {
				CWVUserWallet wallet = (CWVUserWallet) o;
				if(wallet.getCoinType().intValue() == CoinEnum.CWB.getValue()) {
					
					RespGetAccount.Builder accInfo = wltHelper.getAccountInfo(wallet.getAccount());
					if(accInfo.getRetCode()==1){
						if(wallet.getBalance().longValue() != accInfo.getAccount().getBalance()) {
							wallet.setBalance(new BigDecimal(accInfo.getAccount().getBalance()));
							dao.walletDao.updateByPrimaryKeySelective(wallet);
						}
						
					}
					
					accountInfo.setCwcTopup(wallet.getTopupBalance().toString());//充值CWB
					accountInfo.setCwcAmount(wallet.getBalance().doubleValue());
					accountInfo.setDrawCount(wallet.getDrawCount());
					
				}
				total = total + wallet.getBalance().doubleValue() * 0.002;
				
				WalletAccount.Builder account = WalletAccount.newBuilder();
				account.setAccountId(wallet.getWalletId()+"");
				account.setAssert(wallet.getBalance().doubleValue());
				//实时查询行情 TODO
				account.setMarketPrice(0.002);
				account.setCoinType(wallet.getCoinType().toString());
				account.setIcon(wallet.getCoinIcon());
				data.addAccount(account);
			}
		}
		accountInfo.setTotalValueCNY(total);
		data.setAccountInfo(accountInfo);
		builder.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setData(data);
	}




	public void gameShare(FramePacket pack, org.brewchain.cwv.service.game.User.PRetGameShare.Builder ret) {
		
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		
		//查询分享链接 TODO
		
		
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg())
		.setUrl("/share_user="+authUser.getUserId());
	}




	public void accountTopupRecord(FramePacket pack, PSCommon pb,PRetAccountTopupRecord.Builder ret) {
		// TODO Auto-generated method stub
		CWVUserWalletTopupExample example = new CWVUserWalletTopupExample();
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		example.createCriteria().andUserIdEqualTo(user.getUserId());
		example.setOrderByClause("create_time desc");
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		page.setTotalCount(dao.topupDao.countByExample(example));
		List<Object> list = dao.topupDao.selectByExample(example);
		for(Object o : list){
			CWVUserWalletTopup topup = (CWVUserWalletTopup) o;
			TopupRecord.Builder tr = TopupRecord.newBuilder();
			tr.setAmount(topup.getAmount().doubleValue())
			.setCoinType(topup.getCoinType()+"")
			.setStatus(topup.getStatus()+"")
			.setTopupTime(DateUtil.getDayTime(topup.getCreateTime()));
			ret.addTopup(tr);
		}
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		
	}




	public void accountTopupConfirm(FramePacket pack, PSAccountTopup pb,
			PRetCommon.Builder ret) {
		//校验入参
		if(StringUtils.isEmpty(pb.getTopupId())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("充值ID不能为空");
			return ; 
		}
		
		//校验用户
		
		CWVUserWalletTopup topup = new CWVUserWalletTopup();
		topup.setTopupId(Integer.parseInt(pb.getTopupId()));
		
	}
	//add by murphy
	/**
	 * 客服充值
	 * @param pack
	 * @param pb
	 * @param ret
	 */
	public void recharge(FramePacket pack, PSRecharge pb, PRetAccountTopup.Builder ret) {
		//校验
		if(StringUtils.isEmpty(pb.getAmount())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("充值金额不能为空");
			return ;
		}
		final CWVUserWalletTopup topup = new CWVUserWalletTopup();
		
		final CWVUserWallet wallet = getAccountByAddress(pb.getOutputAddress());
		if(wallet==null){
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("账户信息错误");
			return ;
		}
		topup.setAddress(wallet.getAccount());
		topup.setAmount(new BigDecimal(pb.getAmount()));
		topup.setUserId(wallet.getUserId());
		topup.setStatus((byte) 0);
		topup.setCreateTime(new Date());
		
		int countHistory = wallet.getTopupBalance().intValue()/1000;
		
		wallet.setTopupBalance(wallet.getTopupBalance().add(new BigDecimal(pb.getAmount())));
		int countNew = wallet.getTopupBalance().intValue()/1000;
		
		wallet.setDrawCount(wallet.getDrawCount()+(countNew-countHistory));
		wallet.setBalance(wallet.getBalance().add(new BigDecimal(pb.getAmount())));
		
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setAmount(topup.getAmount().doubleValue());
		
		RespCreateTransaction.Builder mapData = wltHelper.createTx(new BigDecimal(pb.getAmount()), pb.getOutputAddress(), pb.getInputAddress());
		if(mapData.getRetCode()!=1){
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode())
			.setRetMsg("交易失败");
			return;
		}
		
		topup.setTxHash(mapData.getTxHash());
		dao.topupDao.doInTransaction(new TransactionExecutor() {
			@Override
			public Object doInTransaction() {
				dao.topupDao.insert(topup);
				//dao.walletDao.updateByPrimaryKeySelective(wallet);//需要定时任务执行
				return null;
			}
		});
	}
	
	public CWVUserWallet getAccountByAddress(String address){
		CWVUserWalletExample example = new CWVUserWalletExample();
		example.createCriteria().andAccountEqualTo(address);
		List<Object> list = dao.getWalletDao().selectByExample(example);
		if(list.isEmpty()){
			return null;
		}else{
			return (CWVUserWallet) list.get(0);
		}
	}
	
	public void createTx(FramePacket pack, ReqCreateTx pb, ResCreateTx.Builder ret){
		if(StringUtils.isBlank(pb.getAmount())){
			ret.setRetCode("02");
			ret.setRetMsg("金额不能为空");
			return;
		}
		if(new BigDecimal(pb.getAmount()).compareTo(new BigDecimal(0))<=0){
			ret.setRetCode("02");
			ret.setRetMsg("金额不能为空");
			return;
		}
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		CWVUserWallet wallet = getUserAccount(user.getUserId(), CoinEnum.CWB);
		RespGetAccount.Builder accountMap = wltHelper.getAccountInfo(wallet.getAccount());
		RespCreateTransaction.Builder res = wltHelper.createTx(new BigDecimal(pb.getAmount()), pb.getAddress(), accountMap);
		if(res!=null&&res.getRetCode()==1){
			CWVUserSendRecord sendRecord = new CWVUserSendRecord();
			sendRecord.setCreateTime(new Date());
			sendRecord.setStatus((byte)0);
			sendRecord.setUserId(user.getUserId());
			sendRecord.setAmount(new BigDecimal(pb.getAmount()));
			sendRecord.setCoinType((byte)0);
			sendRecord.setTxHash(res.getTxHash());
			sendRecord.setInputAddress(wallet.getAccount());
			sendRecord.setOutAddress(pb.getAddress());
			dao.sendRecordDao.insert(sendRecord);
			ret.setRetCode("01").setRetMsg(res.getRetMsg());
			ret.setTxHash(res.getTxHash());
			
		}else{
			ret.setRetCode("02").setRetMsg(res.getRetMsg());
		}
	}
	
	public void getTxRecord(FramePacket pack, ReqGetTxRecord pb, ResGetTxRecord.Builder ret){
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		
		CWVUserSendRecordExample sendRecordExample = new CWVUserSendRecordExample();
		
		sendRecordExample.createCriteria().andStatusEqualTo((byte)1).andUserIdEqualTo(user.getUserId());
		sendRecordExample.setLimit(page.getLimit());
		sendRecordExample.setOffset(page.getOffset());
		
//		if(StringUtils.isNoneBlank(pb.getRecordType())){
//			ret.setRetCode("02").setRetMsg("输入参数中未找到交易类型");
//			return;
//		}
//		if(pb.getRecordType().equals("2")){
//			criteria.andUserIdEqualTo(user.getUserId());
//			criteria.andStatusEqualTo((byte)1);
//		}else if(pb.getRecordType().equals("1")){
//			criteria.andoutEqualTo(user.getUserId());
//			criteria.andStatusEqualTo((byte)1);
//		}
		List<Object> recordList = dao.sendRecordDao.selectByExample(sendRecordExample);
		for(int i = 0;i<recordList.size();i++){
			CWVUserSendRecord sr = (CWVUserSendRecord) recordList.get(i); 
			TxRecord.Builder tr = TxRecord.newBuilder();
			tr.setRecordId(sr.getRecordId());
			tr.setUserId(sr.getUserId());
			tr.setCoinType(sr.getCoinType());
			tr.setInputAddress(sr.getInputAddress());
			tr.setOutAddress(sr.getOutAddress());
			tr.setAmount(sr.getAmount().longValue());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			tr.setCreateTime(sdf.format(sr.getCreateTime()));
			ret.addRecord(tr);
		}
		ret.setRetCode("01");
		
	}
	
}
