package org.brewchain.cwv.game.helper;

import java.math.BigDecimal;

import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.wallet.service.Wallet.MultiTransactionBodyImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionInputImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionOutputImpl;
import org.brewchain.wallet.service.Wallet.ReqCreateContractTransaction;
import org.brewchain.wallet.service.Wallet.ReqCreateMultiTransaction;
import org.brewchain.wallet.service.Wallet.ReqDoContractTransaction;
import org.brewchain.wallet.service.Wallet.ReqGetAccount;
import org.brewchain.wallet.service.Wallet.ReqGetTxByHash;
import org.brewchain.wallet.service.Wallet.ReqNewAddress;
import org.brewchain.wallet.service.Wallet.RespCreateContractTransaction;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetAccount;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;
import org.brewchain.wallet.service.Wallet.RetNewAddress;

import com.google.protobuf.InvalidProtocolBufferException;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

/**
 * 钱包账户操作
 * 
 * @author Murphy
 * @date 2018-05-26
 */

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
@Instantiate(name="Wlt_Helper")
public class WltHelper implements ActorService {
	
	private final String WLT_NAT = "http://127.0.0.1:8000/cwv/wlt/pbnat.do";//创建地址
	private final String WLT_QAD = "http://127.0.0.1:8000/cwv/wlt/pbqad.do";//查询地址
	private final String WLT_NCR = "http://127.0.0.1:8000/cwv/wlt/pbncr.do";//创建合约
	private final String WLT_NTS = "http://127.0.0.1:8000/cwv/wlt/pbnts.do";//创建交易
	private final String WLT_QTS = "http://127.0.0.1:8000/cwv/wlt/pbqts.do";//查询交易
	private final String WLT_DCR = "http://127.0.0.1:8000/cwv/wlt/pbdcr.do";//执行合约
	
	@ActorRequire(name="http",scope="global")
	IPacketSender sender;
	
	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	/**
	 * 创建账户
	 * @param coinType 币种类型
	 * @param seed 助记词
	 * @return 账户地址
	 */
	public String createAccount(String coinType,String seed){
		try {
			ReqNewAddress.Builder reqNewAddress = ReqNewAddress.newBuilder();
			reqNewAddress.setType(coinType);
			reqNewAddress.setSeed(seed);
			
			FramePacket fposttx = PacketHelper.buildUrlFromPB(reqNewAddress.build(),"POST", WLT_NAT);
			val retReg = sender.send(fposttx, 30000);
		
			RetNewAddress.Builder retNewAddress = RetNewAddress.newBuilder().mergeFrom(retReg.getBody());
			if(retNewAddress!=null&&retNewAddress.getRetCode()==1){
				return retNewAddress.getAddress();
			}
		} catch (InvalidProtocolBufferException e) {
			log.debug("create acount error.");
			e.printStackTrace();
		};
		return null;
	}
	
	/**
	 * 获取账户信息
	 * @param address
	 * @param addressType
	 * @return RespGetAccount账户模型
	 */
	public RespGetAccount.Builder getAccountInfo(String address ,String addressType){
		RespGetAccount.Builder respGetAccount = RespGetAccount.newBuilder();
		try {
			ReqGetAccount.Builder reqGetAccount = ReqGetAccount.newBuilder();
			reqGetAccount.setAddress(address);
			reqGetAccount.setType(addressType);
			
			FramePacket fposttx = PacketHelper.buildUrlFromPB(reqGetAccount.build(),"POST", WLT_QAD);
			val retReg = sender.send(fposttx, 30000);
		
			respGetAccount = RespGetAccount.newBuilder().mergeFrom(retReg.getBody());
			
		} catch (InvalidProtocolBufferException e) {
			log.debug("get acount info error.");
			e.printStackTrace();
		};
		return respGetAccount;
	}
	
	/**
	 * 查询交易
	 * @param hexTxHash 交易hash
	 * @return
	 */
	public RespGetTxByHash.Builder getAccountInfo(String hexTxHash){
		RespGetTxByHash.Builder respGetTxByHash = RespGetTxByHash.newBuilder();
		try {
			ReqGetTxByHash.Builder reqGetTxByHash = ReqGetTxByHash.newBuilder();
			reqGetTxByHash.setHexTxHash(hexTxHash);
			
			FramePacket fposttx = PacketHelper.buildUrlFromPB(reqGetTxByHash.build(),"POST", WLT_QTS);
			val retReg = sender.send(fposttx, 30000);
		
			respGetTxByHash = RespGetTxByHash.newBuilder().mergeFrom(retReg.getBody());
			
		} catch (InvalidProtocolBufferException e) {
			log.debug("get acount info error.");
//			e.printStackTrace();
		};
		return respGetTxByHash;
	}
	
	/**
	 * 创建合约
	 * @param address 合约发起发地址
	 * @param amount 金额
	 * @param type 合约类型
	 * @return RespCreateContractTransaction.Builder 包含合约地址和交易hash
	 */
	public RespCreateContractTransaction.Builder createContract(String address,BigDecimal amount,String type){
		RespCreateContractTransaction.Builder respCreateContractTransaction = RespCreateContractTransaction.newBuilder();
		try {
			ReqCreateContractTransaction.Builder reqCreateContractTransaction = ReqCreateContractTransaction.newBuilder();
			MultiTransactionInputImpl.Builder multiTransactionInputImpl = MultiTransactionInputImpl.newBuilder();
			//TODO 拼装合约内容待确认，根据合约类型，拼装合约模板，创建合约 ReqCreateContractTransaction.data
			reqCreateContractTransaction.setData("");
			
			//获取账户nonce
			RespGetAccount.Builder respGetAccount = getAccountInfo(address, "CWC");
			//TODO 需要校验账户是否有此721
			
			
			multiTransactionInputImpl.setNonce(respGetAccount.getAccount().getNonce());//交易次数 *
			multiTransactionInputImpl.setAddress(address);//发起方地址 *
			multiTransactionInputImpl.setAmount(amount.longValue());//交易金额 *
			
			reqCreateContractTransaction.setInput(multiTransactionInputImpl);//合约发起方 *
			
			FramePacket fposttx = PacketHelper.buildUrlFromPB(reqCreateContractTransaction.build(),"POST", WLT_NCR);
			val retReg = sender.send(fposttx, 30000);
			respCreateContractTransaction = RespCreateContractTransaction.newBuilder().mergeFrom(retReg.getBody());
		} catch (InvalidProtocolBufferException e) {
//			e.printStackTrace();
			respCreateContractTransaction.setRetCode(-1);
			respCreateContractTransaction.setRetMsg(e.getMessage());
		}
		return respCreateContractTransaction;
	}
	
	/**
	 * 创建交易
	 * @param amount 交易金额
	 * @param outputAddress 发送方账户地址
	 * @param inputAddress 接收方账户地址
	 * @return RespCreateTransaction.Builder 包含交易hash
	 */
	public RespCreateTransaction.Builder createTx(BigDecimal amount,String outputAddress,String inputAddress){
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		try {
			//创建交易请求
			ReqCreateMultiTransaction.Builder reqCreateMultiTransaction = ReqCreateMultiTransaction.newBuilder();
			MultiTransactionImpl.Builder transaction = MultiTransactionImpl.newBuilder();
			//交易内容体详情
			MultiTransactionBodyImpl.Builder txBody = MultiTransactionBodyImpl.newBuilder();
			//获取发起发账户nonce
			RespGetAccount.Builder respGetAccount = getAccountInfo(inputAddress, "CWC");
			if(respGetAccount.getAccount().getBalance()<amount.longValue()){
				respCreateTransaction.setRetCode(-1);
				respCreateTransaction.setRetMsg("账户余额不足");
				return respCreateTransaction;
			}
			//发起方详情
			MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
			input.setAmount(amount.longValue());//交易金额 *
			input.setAddress(inputAddress);//发起方地址 *
			input.setNonce(respGetAccount.getAccount().getNonce());//交易次数 *
			//接收方详情
			MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
			output.setAmount(amount.longValue());//交易金额 *
			output.setAddress(outputAddress);//接收方地址 *
			
			txBody.addInputs(input);//发起方 *
			txBody.addOutputs(output);//接收方 *
			
			transaction.setTxBody(txBody);//交易内容体 *
			
			reqCreateMultiTransaction.setTransaction(transaction);//交易内容 *
			
			FramePacket fposttx = PacketHelper.buildUrlFromPB(reqCreateMultiTransaction.build(),"POST", WLT_NTS);
			val retReg = sender.send(fposttx, 30000);
			
			respCreateTransaction = RespCreateTransaction.newBuilder().mergeFrom(retReg.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			respCreateTransaction.setRetCode(-1);
			respCreateTransaction.setRetMsg(e.getMessage());
		}
		return respCreateTransaction;
	}
	
	/**
	 * 执行合约
	 * @param amount 交易金额
	 * @param outputAddress 发送方账户地址
	 * @param contractAddress 合约地址
	 * @return 
	 */
	public RespCreateTransaction.Builder excuteContract(BigDecimal amount,String outputAddress,String contractAddress){
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		try {
			//执行合约请求
			ReqDoContractTransaction.Builder reqDoContractTransaction = ReqDoContractTransaction.newBuilder();
			MultiTransactionImpl.Builder transaction = MultiTransactionImpl.newBuilder();
			//交易内容体详情
			MultiTransactionBodyImpl.Builder txBody = MultiTransactionBodyImpl.newBuilder();
			//获取发起发账户nonce
			RespGetAccount.Builder respGetAccount = getAccountInfo(contractAddress, "CWC");
			if(respGetAccount.getAccount().getBalance()<amount.longValue()){
				respCreateTransaction.setRetCode(-1);
				respCreateTransaction.setRetMsg("账户余额不足");
				return respCreateTransaction;
			}
			//发起方详情
			MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
			input.setAmount(amount.longValue());//交易金额 *
			input.setAddress(contractAddress);//发起方地址 *
			input.setNonce(respGetAccount.getAccount().getNonce());//交易次数 *
			//接收方详情
			MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
			output.setAmount(amount.longValue());//交易金额 *
			output.setAddress(outputAddress);//接收方地址 *
			
			txBody.addInputs(input);//发起方 *
			txBody.addOutputs(output);//接收方 *
			
			transaction.setTxBody(txBody);//交易内容体 *
			
			reqDoContractTransaction.setTransaction(transaction);//交易内容 *
			
			FramePacket fposttx = PacketHelper.buildUrlFromPB(reqDoContractTransaction.build(),"POST", WLT_DCR);
			val retReg = sender.send(fposttx, 30000);
			
			respCreateTransaction = RespCreateTransaction.newBuilder().mergeFrom(retReg.getBody());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
			respCreateTransaction.setRetCode(-1);
			respCreateTransaction.setRetMsg(e.getMessage());
		}
		return respCreateTransaction;
	}
}
