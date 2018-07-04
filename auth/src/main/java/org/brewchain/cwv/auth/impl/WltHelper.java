package org.brewchain.cwv.auth.impl;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.bcvm.CodeBuild;
import org.brewchain.bcvm.call.CallTransaction;
import org.brewchain.cwv.auth.dao.Dao;
import org.brewchain.cwv.auth.enums.ContractTypeEnum;
import org.brewchain.cwv.auth.util.DESedeCoder;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddress;
import org.brewchain.cwv.dbgens.game.entity.CWVGameContractAddressExample;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSetting;
import org.brewchain.cwv.dbgens.sys.entity.CWVSysSettingExample;
import org.brewchain.wallet.service.Wallet.AccountCryptoTokenImpl;
import org.brewchain.wallet.service.Wallet.AccountCryptoValueImpl;
import org.brewchain.wallet.service.Wallet.AccountTokenValueImpl;
import org.brewchain.wallet.service.Wallet.AccountValueImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionBodyImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionInputImpl;
import org.brewchain.wallet.service.Wallet.MultiTransactionNodeImpl;
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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.fc.brewchain.bcapi.EncAPI;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;

import lombok.Data;
import lombok.val;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.IPacketSender;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;
import onight.tfw.outils.bean.JsonPBFormat;
import onight.tfw.outils.serialize.JsonSerializer;

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
	
	private final String WLT_NAD = "wlt_url_nad";//创建地址
	private final String WLT_QAD = "wlt_url_qad";//查询地址
	private final String WLT_NCR = "wlt_url_ncr";//创建合约
	private final String WLT_NTS = "wlt_url_nts";//创建交易
	private final String WLT_QTS = "wlt_url_qts";//查询交易
	private final String WLT_DCR = "wlt_url_dcr";//执行合约
	
	@ActorRequire(name="http",scope="global")
	IPacketSender sender;
	
	@ActorRequire(name = "bc_encoder", scope = "global")
	EncAPI encAPI;
	
	@ActorRequire(name="Dao",scope="global")
	Dao daos;
	
	ObjectMapper mapper = new ObjectMapper();
	
	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}
	
	public <T> FramePacket send(T builder,String urlCode){
		String priKey = getWltUrl("sys_des_key");
		String encMssg = DESedeCoder.encrypt(new JsonPBFormat().printToString((Message) builder),priKey);
		Map<String,String> reqParam = new HashMap<>();
		reqParam.put("data", encMssg);
		reqParam.put("busi", "cwv");
		FramePacket fposttx = PacketHelper.buildUrlFromJson(JsonSerializer.getInstance().formatToString(reqParam),"POST", getWltUrl(urlCode));
		return fposttx;
	}

	/**
	 * 创建账户
	 * @param coinType 币种类型
	 * @param seed 助记词
	 * @return 账户地址
	 * @throws Exception 
	 */
	public RetNewAddress.Builder createAccount(String seed){
		ReqNewAddress.Builder reqNewAddress = ReqNewAddress.newBuilder();
		reqNewAddress.setSeed(seed);
		reqNewAddress.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqNewAddress.build(),WLT_NAD);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqNewAddress.build()),"POST", getWltUrl(WLT_NAD));
//		
		val retReg = sender.send(fposttx, 30000);
		RetNewAddress.Builder retNewAddress = RetNewAddress.newBuilder();
		
		Map<String,Object> map = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		if(map.get("retCode")!=null && map.get("retCode").toString().equals("1")){
			if(map.get("address")!=null){
				retNewAddress.setAddress(map.get("address").toString());
				retNewAddress.setRetCode(Integer.parseInt(map.get("retCode").toString()));
			}else{
				retNewAddress.setRetCode(-1);
				retNewAddress.setMsg("为获取到地址信息");
				log.debug("调用创建账户接口成功，返回结果没有地址信息");
			}
		}else{
			Object msg = map.get("msg");
			if(msg==null){
				msg="创建账户失败";
			}
			retNewAddress.setMsg(msg.toString());
			retNewAddress.setRetCode(-1);
			log.debug("调用创建账户接口发生错误");
		}
		
		return retNewAddress;
	}
	
	/**
	 * 获取账户信息
	 * @param address
	 * @param addressType
	 * @return RespGetAccount账户模型
	 * @throws Exception 
	 */
	public RespGetAccount.Builder getAccountInfo(String address) {
//		RespGetAccount.Builder respGetAccount = RespGetAccount.newBuilder();
		ReqGetAccount.Builder reqGetAccount = ReqGetAccount.newBuilder();
		reqGetAccount.setAddress(address);
		reqGetAccount.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqGetAccount.build(),WLT_QAD);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqGetAccount.build()), "POST", getWltUrl(WLT_QAD));
		val retReg = sender.send(fposttx, 30000);
	
//		Map<String,Object> map = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		JsonNode retNode = null;
		ObjectMapper mapper = new ObjectMapper();
		try{
			retNode = mapper.readTree(retReg.getBody());
		} catch (Exception e){
			log.error("parse query address error : " + e.getMessage());
		}
		RespGetAccount.Builder account = RespGetAccount.newBuilder();
		if(retNode != null && retNode.has("retCode") && retNode.get("retCode").asInt() == 1){
			account = parseJson2AccountValueImpl(retNode);
			return account;
		}
		return account.setRetCode(-1);
	}
	
	/**
	 * 查询交易
	 * @param hexTxHash 交易hash
	 * @return
	 * @throws Exception 
	 */
	public RespGetTxByHash.Builder getTxInfo(String hexTxHash) {
		RespGetTxByHash.Builder ret = RespGetTxByHash.newBuilder();
		ReqGetTxByHash.Builder reqGetTxByHash = ReqGetTxByHash.newBuilder();
		reqGetTxByHash.setHexTxHash(hexTxHash);
		reqGetTxByHash.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqGetTxByHash.build(),WLT_QTS);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqGetTxByHash.build()),"POST", getWltUrl(WLT_QTS));
		val retReg = sender.send(fposttx, 30000);
		Map<String,Object> map = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		JsonNode retNode = null;
		try{
			retNode = mapper.readTree(retReg.getBody());
		} catch(Exception e){
			log.error("parse query transaction error : " + e.getMessage());
		}
		
		if(retNode != null && retNode.has("retCode") && retNode.get("retCode").asInt() == 1){
			ret = parseJson2RespGetTxByHash(retNode);
		}else{
			ret.setRetCode(-1);
		}
		return ret;
	}
	
	/**
	 * 创建合约
	 * @param address 合约发起发地址
	 * @param amount 金额
	 * @param type 合约类型
	 * @return RespCreateContractTransaction.Builder 包含合约地址和交易hash
	 * @throws Exception 
	 */
	public RespCreateContractTransaction.Builder createContract(String address,BigDecimal amount,String type) {
		RespCreateContractTransaction.Builder ret = RespCreateContractTransaction.newBuilder();
		//TODO 拼装合约内容待确认，根据合约类型，拼装合约模板，创建合约 ReqCreateContractTransaction.data
		excuteContract();
		CodeBuild.Result codeBuild = buildContract(type);
		if(codeBuild==null||StringUtils.isNotBlank(codeBuild.error)){
			ret.setRetMsg("创建合约失败，请重新操作");
			ret.setRetCode(-1);
			log.debug("合约编译失败");
			return ret;
		}
		String data = JsonSerializer.getInstance().formatToString(codeBuild);
		return createContract(address, amount, codeBuild.data,type);
	}

	
	/**
	 * 创建合约
	 * @param address 合约发起发地址
	 * @param amount 金额
	 * @param type 合约类型
	 * @return RespCreateContractTransaction.Builder 包含合约地址和交易hash
	 * @throws Exception 
	 */
	public RespCreateContractTransaction.Builder createContract(String address,BigDecimal amount,String data,String type) {
		RespCreateContractTransaction.Builder ret = RespCreateContractTransaction.newBuilder();
		ReqCreateContractTransaction.Builder reqCreateContractTransaction = ReqCreateContractTransaction.newBuilder();
		MultiTransactionInputImpl.Builder multiTransactionInputImpl = MultiTransactionInputImpl.newBuilder();
		
		reqCreateContractTransaction.setData(data);
		
		//获取账户nonce
		RespGetAccount.Builder accountMap = getAccountInfo(address);
		if(accountMap==null||accountMap.getRetCode()==-1){
			ret.setRetMsg("查询账户发生错误");
			ret.setRetCode(-1);
			log.debug("查询账户发生错误");
			return ret;
		}
		//TODO 需要校验账户是否有此721
		
		AccountValueImpl account = accountMap.getAccount();
		multiTransactionInputImpl.setNonce(account.getNonce());//交易次数 *
		multiTransactionInputImpl.setAddress(address);//发起方地址 *
		multiTransactionInputImpl.setAmount(amount.longValue());//交易金额 *
		
		reqCreateContractTransaction.setInput(multiTransactionInputImpl);//合约发起方 *
		reqCreateContractTransaction.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqCreateContractTransaction.build(),WLT_NCR);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqCreateContractTransaction.build()),"POST", getWltUrl(WLT_NCR));
		val retReg = sender.send(fposttx, 30000);
		Map<String,Object> map = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		if(map.get("retCode")!=null&&map.get("retCode").toString().equals("1")){
			ret.setTxHash(map.get("txHash").toString());
			ret.setContractAddress(map.get("contractAddress").toString());
			ret.setRetCode(1);
			
			CWVGameContractAddress contractAddress = new CWVGameContractAddress();
			contractAddress.setContractAddress(map.get("contractAddress").toString());
			contractAddress.setContractState("0");
			contractAddress.setContractType(type);
			int cou = daos.contractAddressDao.insert(contractAddress);
			if(cou!=1){
				ret.setRetCode(-1);
				ret.setRetMsg("合约创建成功，保存合约失败");
			}
			
		}else{
			Object msg = map.get("retMsg");
			if(msg==null){
				msg="调用创建合约接口发生错误";
			}
			ret.setRetMsg(msg.toString());
			ret.setRetCode(-1);
			log.debug("调用创建合约接口发生错误");
		}
		//---------------------------------------------------------------------
//		JsonNode retNode = null;
//		try {
//			retNode = mapper.readTree(retReg.getBody());
//		} catch (IOException e) {
//			log.error("parse ret error : " + new String(retReg.getBody()));
//		}
//		
//		if(retNode != null && retNode.has("retCode") && retNode.get("retCode").asInt() == 0){
//			ret = RespCreateContractTransaction.newBuilder();
//			ret.setContractAddress(retNode.has("contractAddress") ? retNode.get("contractAddress").asText() : "");
//			ret.setRetCode(1);
//			ret.setRetMsg(retNode.has("retMsg") ? retNode.get("retMsg").asText() : "");
//			ret.setTxHash(retNode.has("txHash") ? retNode.get("txHash").asText() : "");
//		}
		
		return ret;
	}
	
	/**
	 * 创建交易
	 * @param amount 交易金额
	 * @param outputAddress 接收方账户地址
	 * @param inputAddress 发送方账户地址
	 * @return RespCreateTransaction.Builder 包含交易hash
	 * @throws Exception 
	 */
	public RespCreateTransaction.Builder createTx(BigDecimal amount,String outputAddress,String inputAddress) {
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		Map<String,Object> ret = new HashMap<>();
		//创建交易请求
		ReqCreateMultiTransaction.Builder reqCreateMultiTransaction = ReqCreateMultiTransaction.newBuilder();
		MultiTransactionImpl.Builder transaction = MultiTransactionImpl.newBuilder();
		//交易内容体详情
		MultiTransactionBodyImpl.Builder txBody = MultiTransactionBodyImpl.newBuilder();
		//获取发起发账户nonce
		
		RespGetAccount.Builder accountMap = getAccountInfo(inputAddress);
		if(accountMap==null){
			respCreateTransaction.setRetMsg("查询账户发生错误");
			respCreateTransaction.setRetCode(-1);
			log.debug("查询账户发生错误");
			return respCreateTransaction;
		}
		AccountValueImpl account = accountMap.getAccount();
		if(account.getBalance()<amount.longValue()){
			respCreateTransaction.setRetCode(-1);
			respCreateTransaction.setRetMsg("账户余额不足");
			return respCreateTransaction;
		}
		//发起方详情
		MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
		input.setAmount(amount.longValue());//交易金额 *
		input.setAddress(inputAddress);//发起方地址 *
		input.setNonce(account.getNonce());//交易次数 *
		//接收方详情
		MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
		output.setAmount(amount.longValue());//交易金额 *
		output.setAddress(outputAddress);//接收方地址 *
		
		txBody.addInputs(input);//发起方 *
		txBody.addOutputs(output);//接收方 *
		
		transaction.setTxBody(txBody);//交易内容体 *
		
		reqCreateMultiTransaction.setTransaction(transaction);//交易内容 *
		reqCreateMultiTransaction.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqCreateMultiTransaction.build(),WLT_NTS);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqCreateMultiTransaction.build()),"POST", getWltUrl(WLT_NTS));
		val retReg = sender.send(fposttx, 30000);
		ret = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		if(ret.get("retCode")!=null&&ret.get("retCode").toString().equals("1")){
			respCreateTransaction.setTxHash(ret.get("txHash").toString()).setRetCode(1);
		}else{
			if(ret.get("retMsg")!=null){
				respCreateTransaction.setRetMsg(ret.get("retMsg").toString());
			}else{
				respCreateTransaction.setRetMsg("创建交易失败");
			}
			respCreateTransaction.setRetCode(-1);
		}
		
		return respCreateTransaction;
	}
	
	/**
	 * 创建交易
	 * @param amount 交易金额
	 * @param outputAddress 接收方账户地址
	 * @param accountInfo 发送方账户信息
	 * @return RespCreateTransaction.Builder 包含交易hash
	 * @throws Exception 
	 */
	public RespCreateTransaction.Builder createTx(BigDecimal amount,String outputAddress,RespGetAccount.Builder accountMap) {
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		Map<String,Object> ret = new HashMap<>();
		//创建交易请求
		ReqCreateMultiTransaction.Builder reqCreateMultiTransaction = ReqCreateMultiTransaction.newBuilder();
		MultiTransactionImpl.Builder transaction = MultiTransactionImpl.newBuilder();
		//交易内容体详情
		MultiTransactionBodyImpl.Builder txBody = MultiTransactionBodyImpl.newBuilder();
		//获取发起发账户nonce
		
		if(accountMap==null){
			respCreateTransaction.setRetMsg("查询账户发生错误");
			respCreateTransaction.setRetCode(-1);
			log.debug("查询账户发生错误");
			return respCreateTransaction;
		}
		AccountValueImpl account = accountMap.getAccount();
		if(account.getBalance()<amount.longValue()){
			respCreateTransaction.setRetCode(-1);
			respCreateTransaction.setRetMsg("账户余额不足");
			return respCreateTransaction;
		}
		//发起方详情
		MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
		input.setAmount(amount.longValue());//交易金额 *
		input.setAddress(accountMap.getAddress());//发起方地址 *
		input.setNonce(account.getNonce());//交易次数 *
		//接收方详情
		MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
		output.setAmount(amount.longValue());//交易金额 *
		output.setAddress(outputAddress);//接收方地址 *
		
		txBody.addInputs(input);//发起方 *
		txBody.addOutputs(output);//接收方 *
		
		transaction.setTxBody(txBody);//交易内容体 *
		
		reqCreateMultiTransaction.setTransaction(transaction);//交易内容 *
		reqCreateMultiTransaction.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqCreateMultiTransaction.build(),WLT_NTS);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqCreateMultiTransaction.build()),"POST", getWltUrl(WLT_NTS));
		val retReg = sender.send(fposttx, 30000);
		ret = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		if(ret.get("retCode")!=null&&ret.get("retCode").toString().equals("1")){
			respCreateTransaction.setTxHash(ret.get("txHash").toString()).setRetCode(1);
		}else{
			if(ret.get("retMsg")!=null){
				respCreateTransaction.setRetMsg(ret.get("retMsg").toString());
			}else{
				respCreateTransaction.setRetMsg("创建交易失败");
			}
			respCreateTransaction.setRetCode(-1);
		}
		
		return respCreateTransaction;
	}
	
	/**
	 * 创建交易 721
	 * @param amount 交易金额
	 * @param outputAddress 接收方账户地址
	 * @param inputAddress 发送方账户地址
	 * @param symbol ERC721 token标记 
	 * @param cryptoToken ERC721 token名字 
	 * @return RespCreateTransaction.Builder 包含交易hash
	 * @throws Exception 
	 */
	public RespCreateTransaction.Builder createTx(BigDecimal amount,String outputAddress,String inputAddress,String symbol,String cryptoToken) {
		
		//返回参数
		RespCreateTransaction.Builder respCreateTransaction = RespCreateTransaction.newBuilder();
		Map<String,Object> ret = new HashMap<>();
		//创建交易请求
		ReqCreateMultiTransaction.Builder reqCreateMultiTransaction = ReqCreateMultiTransaction.newBuilder();
		MultiTransactionImpl.Builder transaction = MultiTransactionImpl.newBuilder();
		//交易内容体详情
		MultiTransactionBodyImpl.Builder txBody = MultiTransactionBodyImpl.newBuilder();
		//获取发起发账户nonce
		
		RespGetAccount.Builder accountMap = getAccountInfo(inputAddress);
		if(accountMap==null){
			respCreateTransaction.setRetMsg("查询账户发生错误");
			respCreateTransaction.setRetCode(-1);
			log.debug("查询账户发生错误");
			return respCreateTransaction;
		}
		AccountValueImpl account = accountMap.getAccount();
		if(account.getBalance()<amount.longValue()){
			respCreateTransaction.setRetCode(-1);
			respCreateTransaction.setRetMsg("账户余额不足");
			return respCreateTransaction;
		}
		//发起方详情
		MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
		input.setAmount(amount.longValue());//交易金额 *
		input.setAddress(inputAddress);//发起方地址 *
		input.setNonce(account.getNonce());//交易次数 *
		input.setCryptoToken(cryptoToken);
		input.setSymbol(symbol);
		//接收方详情
		MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
		output.setAmount(amount.longValue());//交易金额 *
		output.setAddress(outputAddress);//接收方地址 *
		output.setCryptoToken(cryptoToken);
		output.setSymbol(symbol);
		
		txBody.addInputs(input);//发起方 *
		txBody.addOutputs(output);//接收方 *
		
		transaction.setTxBody(txBody);//交易内容体 *
		
		reqCreateMultiTransaction.setTransaction(transaction);//交易内容 *
		
		reqCreateMultiTransaction.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqCreateMultiTransaction.build(),WLT_NTS);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqCreateMultiTransaction.build()),"POST", getWltUrl(WLT_NTS));
		val retReg = sender.send(fposttx, 30000);
		ret = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		if(ret.get("retCode")!=null&&ret.get("retCode").toString().equals("1")){
			respCreateTransaction.setTxHash(ret.get("txHash").toString()).setRetCode(1);
		}else{
			if(ret.get("retMsg")!=null){
				respCreateTransaction.setRetMsg(ret.get("retMsg").toString());
			}else{
				respCreateTransaction.setRetMsg("创建交易失败");
			}
			respCreateTransaction.setRetCode(-1);
		}
		
		return respCreateTransaction;
	}
	
	/**
	 * 执行合约
	 * @param amount 交易金额
	 * @param outputAddress 发送方账户地址
	 * @param type 合约类型
	 * @return 
	 * @throws Exception 
	 */
	public RespCreateTransaction.Builder excuteContract(BigDecimal amount,String outputAddress,String contractAddress,String type){
		//返回参数
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		//执行合约请求
		ReqDoContractTransaction.Builder reqDoContractTransaction = ReqDoContractTransaction.newBuilder();
		MultiTransactionImpl.Builder transaction = MultiTransactionImpl.newBuilder();
		//交易内容体详情
		MultiTransactionBodyImpl.Builder txBody = MultiTransactionBodyImpl.newBuilder();
		//获取发起发账户nonce
		RespGetAccount.Builder accountMap = getAccountInfo(outputAddress);
		if(accountMap==null){
			ret.setRetMsg("查询账户发生错误");
			ret.setRetCode(-1);
			log.debug("查询账户发生错误");
			return ret;
		}
		AccountValueImpl account = accountMap.getAccount();
		if(account.getBalance()<amount.longValue()){
			ret.setRetCode(-1);
			ret.setRetMsg("账户余额不足");
			return ret;
		}
		CWVGameContractAddressExample caExample = new CWVGameContractAddressExample();
		caExample.createCriteria().andContractTypeEqualTo(contractAddress).andContractStateEqualTo("0");
		List<Object> listContract = daos.contractAddressDao.selectByExample(caExample);
//		if(listContract.isEmpty()){
//			
//		}
		
//		excuteContract();
		
		//发起方详情
		MultiTransactionInputImpl.Builder input = MultiTransactionInputImpl.newBuilder();
		input.setAmount(amount.longValue());//交易金额 *
		input.setAddress(outputAddress);//发起方地址 *
		input.setNonce(account.getNonce());//交易次数 *
		//接收方详情
		MultiTransactionOutputImpl.Builder output = MultiTransactionOutputImpl.newBuilder();
		output.setAmount(amount.longValue());//交易金额 *
		output.setAddress("");//接收方地址 *
		
		txBody.addInputs(input);//发起方 *
		txBody.addOutputs(output);//接收方 *
		
		transaction.setTxBody(txBody);//交易内容体 *
		
		reqDoContractTransaction.setTransaction(transaction);//交易内容 *
		reqDoContractTransaction.setTimestamp(new Date().getTime());
		
		FramePacket fposttx = send(reqDoContractTransaction.build(),WLT_DCR);
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(new JsonPBFormat().printToString(reqDoContractTransaction.build()),"POST", getWltUrl(WLT_DCR));
		val retReg = sender.send(fposttx, 30000);
		
		Map<String,Object> map = JsonSerializer.getInstance().deserialize(new String(retReg.getBody()), Map.class);
		if(map.get("retCode")!=null && map.get("retCode").toString().equals("1")){
			if(map.get("txHash")!=null){
				ret.setTxHash(map.get("txHash").toString());
				ret.setRetCode(1);
			}else{
				ret.setRetCode(-1);
				ret.setRetMsg("未获取到交易hash信息");
				log.debug("调用执行合约接口成功，返回结果没有交易hash信息");
			}
		}else{
			Object msg = map.get("retMsg");
			if(msg==null){
				msg="调用执行合约接口发生错误";
			}
			ret.setRetMsg(msg.toString());	
			ret.setRetCode(-1);
			log.debug("调用执行合约接口发生错误");
		}
		
		return ret;
	}
	
	public String getWltUrl(String busiType){
		CWVSysSettingExample settingExample = new CWVSysSettingExample();
		settingExample.createCriteria().andNameEqualTo(busiType);
		List<Object> datas = daos.getSettingDao().selectByExample(settingExample);
		if(datas.isEmpty()){
			throw new IllegalArgumentException("系统无法找到查询该业务参数");
		}
		return ((CWVSysSetting)datas.get(0)).getValue();
	}
	
	/**
	 * @param retNode
	 * @return
	 */
	private RespGetAccount.Builder parseJson2AccountValueImpl(JsonNode retNode){
		RespGetAccount.Builder ret = RespGetAccount.newBuilder();
		ret.setRetCode(retNode.get("retCode").asInt());
		if(retNode.has("address")){
			ret.setAddress(retNode.get("address").asText());
		}
		
		if(retNode.has("account")){
			JsonNode accountNode = retNode.get("account");
			AccountValueImpl.Builder account = AccountValueImpl.newBuilder();
			account.setNonce(accountNode.has("nonce") ? accountNode.get("nonce").asInt() : 0);
			account.setBalance(accountNode.has("balance") ? accountNode.get("balance").asLong() : 0L);
			account.setPubKey(accountNode.has("pubKey") ? accountNode.get("pubKey").asText() : "");
			account.setMax(accountNode.has("max") ? accountNode.get("max").asLong() : 0L);
			account.setAcceptMax(accountNode.has("acceptMax") ? accountNode.get("acceptMax").asLong() : 0L);
			account.setAcceptLimit(accountNode.has("acceptLimit") ? accountNode.get("acceptLimit").asInt() : 0);
			account.setCode(accountNode.has("code") ? accountNode.get("code").asText() : "");
			if(accountNode.has("address")){
				ArrayNode addrs = (ArrayNode) accountNode.get("address");
				if(addrs != null && addrs.size() > 0){
					for (JsonNode addrObj : addrs){
						account.addAddress(addrObj.asText());
					}
				}
			}
			
			if(accountNode.has("tokens")){
				ArrayNode tokens = (ArrayNode) accountNode.get("tokens");
				if(tokens != null && tokens.size() > 0){
					for(JsonNode token : tokens){
						AccountTokenValueImpl.Builder tokenValue = AccountTokenValueImpl.newBuilder();
						tokenValue.setBalance(token.has("balance") ? token.get("balance").asLong() : 0L);
						tokenValue.setToken(token.has("token") ? token.get("token").asText() : "");
					}
				}
			}
			
			if(accountNode.has("cryptos")){
				ArrayNode cryptos = (ArrayNode) accountNode.get("cryptos");
				if(cryptos != null && cryptos.size() > 0){
					for(JsonNode crypto : cryptos){
						AccountCryptoValueImpl.Builder cryptoValue = AccountCryptoValueImpl.newBuilder();
						cryptoValue.setSymbol(crypto.has("symbol") ? crypto.get("symbol").asText() : "");
						if(crypto.has("tokens")){
							ArrayNode tokens = (ArrayNode) crypto.get("tokens");
							for(JsonNode token : tokens){
								AccountCryptoTokenImpl.Builder cryptoToken = AccountCryptoTokenImpl.newBuilder();
								cryptoToken.setHash(token.has("hash") ? token.get("hash").asText() : "");
								cryptoToken.setTimestamp(token.has("timestamp") ? token.get("timestamp").asLong() : 0L);
								cryptoToken.setIndex(token.has("index") ? token.get("index").asInt() : 0);
								cryptoToken.setTotal(token.has("total") ? token.get("total").asInt() : 0);
								cryptoToken.setCode(token.has("code") ? token.get("code").asText() : "");
								cryptoToken.setName(token.has("name") ? token.get("name").asText() : "");
								cryptoToken.setNonce(token.has("nonce") ? token.get("nonce").asInt() : 0);
								cryptoToken.setOwnertime(token.has("ownertime") ? token.get("ownertime").asLong() : 0L);
								
								cryptoValue.addTokens(cryptoToken);
							}
						}
						
						account.addCryptos(cryptoValue);
					}
				}
			}
			
			ret.setAccount(account);
		}
		
		return ret;
	}
	
	/**
	 * @param retNode
	 * @return
	 */
	private RespGetTxByHash.Builder parseJson2RespGetTxByHash(JsonNode retNode){
		RespGetTxByHash.Builder ret = RespGetTxByHash.newBuilder();
		MultiTransactionImpl.Builder transaction = MultiTransactionImpl.newBuilder();
		if(retNode.has("transaction")) {
			JsonNode tNode = retNode.get("transaction");
			transaction.setTxBody(getTxBodyFromTransaction(tNode.get("txBody")));
			if(tNode.has("node"))
				transaction.setNode(getMultiTransactionNode(retNode.get("node")));
			transaction.setStatus(tNode.has("status") ? tNode.get("status").asText() : "");
			transaction.setTxHash(tNode.has("txHash") ? tNode.get("txHash").asText() : "");
			
			ret.setTransaction(transaction);
		}
		ret.setRetCode(retNode.get("retCode").asInt());
		
		return ret;
	}
	
	/**
	 * 签名前，body中的内容需要补充完整
	 * 
	 * @param node = txBody
	 * @return
	 */
	private MultiTransactionBodyImpl getTxBodyFromTransaction(JsonNode node){
		MultiTransactionBodyImpl.Builder body = MultiTransactionBodyImpl.newBuilder();
		
		body.setData(node.has("data") ? node.get("data").asText() : "");
		body.setExdata(node.has("exdata") ? node.get("exdata").asText() : "");
		
		if(node.has("deledate")){
			ArrayNode array = (ArrayNode) node.get("delegate");
			for (int i = 0; i < array.size(); i++){
				body.addDelegate(array.get(i).asText());
			}
		}
		
		if(node.has("inputs")){
			ArrayNode inputs = (ArrayNode) node.get("inputs");
			for (JsonNode input : inputs){
				body.addInputs(getInput(input));
			}
		}
		
		if(node.has("outputs")){
			ArrayNode outputs = (ArrayNode) node.get("outputs");
			for(JsonNode output : outputs){
				body.addOutputs(getOutput(output));
			}
		}
		
		body.setTimestamp(node.has("timestamp") ? node.get("timestamp").asLong() : 0l);
		
		return body.build();
	}
	
	/**
	 * @param retNode
	 * @return
	 */
	private MultiTransactionNodeImpl.Builder getMultiTransactionNode(JsonNode retNode){
		MultiTransactionNodeImpl.Builder node = MultiTransactionNodeImpl.newBuilder();
		node.setBcuid(retNode.has("bcuid") ? retNode.get("bcuid").asText() : "");
		node.setIp(retNode.has("ip") ? retNode.get("ip").asText() : "");
		node.setNode(retNode.has("node") ? retNode.get("node").asText() : "");
		return node;
	}
	
	/**
	 * @param input
	 * @return
	 */
	private MultiTransactionInputImpl.Builder getInput(JsonNode input){
		MultiTransactionInputImpl.Builder inputB = MultiTransactionInputImpl.newBuilder();
		inputB.setAddress(input.has("address") ? input.get("address").asText() : "");
		inputB.setAmount(input.has("amount") ? input.get("amount").asLong() : 0l);
		inputB.setCryptoToken(input.has("cryptoToken") ? input.get("cryptoToken").asText() : "");
		inputB.setFee(input.has("fee") ? input.get("fee").asInt() : 0);
		inputB.setFeeLimit(input.has("feeLimit") ? input.get("feeLimit").asInt() : 0);
		inputB.setNonce(input.has("nonce") ? input.get("nonce").asInt() : 0);
		inputB.setPubKey(input.has("pubKey") ? input.get("pubKey").asText() : "");
		inputB.setSymbol(input.has("symbol") ? input.get("symbol").asText() : "");
		inputB.setToken(input.has("token") ? input.get("token").asText() : "");
		
		return inputB;
	}
	
	/**
	 * @param output
	 * @return
	 */
	private MultiTransactionOutputImpl.Builder getOutput(JsonNode output){
		MultiTransactionOutputImpl.Builder outputB = MultiTransactionOutputImpl.newBuilder();
		outputB.setAddress(output.has("address") ? output.get("address").asText() : "");
		outputB.setAmount(output.has("amount") ? output.get("amount").asLong() : 0l);
		outputB.setCryptoToken(output.has("cryptoToken") ? output.get("cryptoToken").asText() : "");
		outputB.setSymbol(output.has("symbol") ? output.get("symbol").asText() : "");
		return outputB;
	}
	
	public CodeBuild.Result buildContract() {
		try {
			File file = new File("F:\\finchain\\GIT\\cwv\\cwv-contract-sol\\contracts\\RandomApple.sol");
			FileReader reader = new FileReader(file);// 获取该文件的输入流  
	        char[] bb = new char[1024];// 用来保存每次读取到的字符  
	        
	        String code = "";// 用来将每次读取到的字符拼接，当然使用StringBuffer类更好  
	        int n;// 每次读取到的字符长度  
	        while ((n = reader.read(bb)) != -1) {  
	            code += new String(bb, 0, n);  
	        }  
	        reader.close();// 关闭输入流，释放连接  
	        //System.out.println(code);
	        
	        CodeBuild.Build  cvm = CodeBuild.newBuild(CodeBuild.Type.SOLIDITY);
	        
	        CodeBuild.Result ret = cvm.build(code);
	        if(StringUtils.isBlank(ret.error)) {
	        		System.out.println("data="+ret.data);
	        		System.out.println("exdata="+ret.exdata);
	        }else {
	        		System.out.println("error="+ret.error);
	        }
	        return ret;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public CodeBuild.Result buildContract(String busi) {
		
		String contractFile = null;
		if(ContractTypeEnum.EXCHANGE_CONTRACT.getName().equals(busi)){
			contractFile=ContractTypeEnum.EXCHANGE_CONTRACT.getValue();
		}else if(ContractTypeEnum.AUCTION_CONTRACT.getName().equals(busi)){
			contractFile=ContractTypeEnum.AUCTION_CONTRACT.getValue();
		}else if(ContractTypeEnum.RANDOM_CONTRACT.getName().equals(busi)){
			contractFile=ContractTypeEnum.RANDOM_CONTRACT.getValue();
		}
		if(StringUtils.isBlank(contractFile)){
			throw new IllegalArgumentException("创建合约参数有误");
		}
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream(contractFile);// 获取该文件的输入流 
	        byte[] buffer = new byte[8192];// 用来保存每次读取到的字符 
	        String code = "";// 用来将每次读取到的字符拼接，当然使用StringBuffer类更好  
	        int n;// 每次读取到的字符长度  
	        while ((n = in.read(buffer)) != -1) {  
	            code += new String(buffer, 0, n);  
	        }  
	        in.close();
	        
	        CodeBuild.Build  cvm = CodeBuild.newBuild(CodeBuild.Type.SOLIDITY);
	        
	        CodeBuild.Result ret = cvm.build(code);
	        return ret;
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void excuteContract(){
		log.info("Calling the contract function 'inc'");
//		RespGetAccount.Builder  acc = getAccountInfo(contractAddress);
//		acc.get
		
		CodeBuild.Result res = buildContract("2");
        CallTransaction.Contract contract = new CallTransaction.Contract(res.data);
        CallTransaction.Function inc = contract.getByName("getCurrentTimes");
//        CallTransaction.Function inc2 = contract.getByName("get");
        byte[] functionCallBytes = inc.encode();
//        byte[] functionCallBytes2 = inc2.encode();
        
        String str = encAPI.hexEnc(functionCallBytes);
//        String str2 = encAPI.hexEnc(functionCallBytes2);
        System.out.println(str);
//        System.out.println(str2);
        
	}
}
