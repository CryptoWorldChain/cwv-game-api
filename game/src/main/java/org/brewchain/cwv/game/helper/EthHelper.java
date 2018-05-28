//package org.brewchain.cwv.game.helper;
//
//import java.math.BigInteger;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.felix.ipojo.annotations.Provides;
//
//import lombok.Data;
//import lombok.val;
//import lombok.extern.slf4j.Slf4j;
//import onight.osgi.annotation.iPojoBean;
//import onight.tfw.ntrans.api.ActorService;
//import onight.tfw.ntrans.api.annotation.ActorRequire;
//import onight.tfw.otransio.api.IPacketSender;
//import onight.tfw.otransio.api.PacketHelper;
//import onight.tfw.otransio.api.beans.FramePacket;
//import onight.tfw.outils.serialize.JsonSerializer;
//
///**
// * ethereum
// * 
// * @author jack
// *
// */
//@iPojoBean
//@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
//@Slf4j
//@Data
//public class EthHelper implements ActorService {
//
//	@ActorRequire(name = "http", scope = "global")
//	IPacketSender sender;
//
//	@ActorRequire
//	BaseHelper baseHelper;
//
//	private final static String ETH_TEST_URL = "http://api.blockcypher.com/v1/beth/test";
//	private final static String ETH_MAIN_URL = "http://api.blockcypher.com/v1/eth/main";
//	private final static String ETH_BASE_URL = ETH_TEST_URL;
//	private final static String ETH_TOKEN = "token=f1812d52c7984dcc8a9f0775001cac09";
//	
//	//TODO 
//	private final static long gasPrice = 21000l;
//	private final static long gasLimit = 21000l;
//
//	/**
//	 * 创建账户
//	 * 
//	 * @return
//	 * @throws Exception
//	 */
//	public PRetWallet.Builder createEhtAccount() throws Exception {
//		PRetWallet.Builder wallet = PRetWallet.newBuilder();
//		wallet.setErrCode("");
//		wallet.setMsg("");
//		wallet.setRequestNo("");
//		ECKey key = new ECKey(Utils.getRandom());
//
//		Addrs.Builder addrs = Addrs.newBuilder();
//		addrs.setAlias("eth");
//		addrs.setHexAddr(Hex.toHexString(key.getAddress()));
//		addrs.setPki(Hex.toHexString(key.getPrivKeyBytes()));
//		addrs.setRpmdHash(Hex.toHexString(key.getPubKey()));
//
//		wallet.setAddrs(addrs);
//
//		return wallet;
//	}
//
//	/**
//	 * 查询钱包balance
//	 * 
//	 * @param addr
//	 *            地址
//	 * @return
//	 * @throws Exception
//	 */
//	public Map<String, Object> checkWalletETH(String addr) throws Exception {
//		String url = ETH_BASE_URL + "/addrs/" + addr + "/balance";
//		log.debug("request the eth url is : " + url);
//		FramePacket fp = PacketHelper.buildUrlForGet(url);
//
//		val yearMeasureRet = sender.send(fp, 30000);
//		Map<String, Object> jsonRet = null;
//		try {
//			jsonRet = JsonSerializer.getInstance().deserialize(new String(yearMeasureRet.getBody()), Map.class);
//		} catch (Exception e) {
//			throw new Exception(e);
//		}
//		jsonRet.put("errCode", "000000");
//		jsonRet.put("msg", "successful");
//
//		Map<String, Object> assetMap = new HashMap<String, Object>();
//		double holdCount = (double)(Long.valueOf(jsonRet.get("balance").toString()));
//		assetMap.put("holdCount", holdCount);
//		jsonRet.put("asset", assetMap);
//
//		return jsonRet;
//	}
//
//	/**
//	 * 提现
//	 * 
//	 * @param addr
//	 *            提现地址
//	 * @param amount
//	 *            金额
//	 * @return
//	 * @throws Exception
//	 */
//	public Map<String, Object> withdrawETH(String addr, double amount) throws Exception {
//		TXTpsAccountDetail plateformAccount = baseHelper.getUserAccountDetail("ETH", null);
//		long value = (long)amount;
//		return walletTrade(plateformAccount.getAddress(), plateformAccount.getPriKey(), addr, value, gasPrice, gasLimit);
//	}
//
//	/**
//	 * 充值
//	 * 
//	 * @param accountDetail
//	 *            账户
//	 * @param amount
//	 *            金额
//	 * @return
//	 * @throws Exception
//	 */
//	public Map<String, Object> rechargeETH(TXTpsAccountDetail accountDetail, double amount) throws Exception {
//		TXTpsAccountDetail plateformAccount = baseHelper.getUserAccountDetail("ETH", null);
//		long value = (long)amount;
//		return walletTrade(accountDetail.getAddress(), accountDetail.getPriKey(), plateformAccount.getAddress(), value, gasPrice, gasLimit);
//	}
//
//	/**
//	 * 钱包交易
//	 * 
//	 * @param inputs
//	 *            交易输入
//	 * @param outputs
//	 *            交易输出
//	 * @return 接口返回信息
//	 * @throws Exception
//	 */
//	public Map<String, Object> walletTrade(String inputAddr, String inputPriKey, String outputAddr, long amount, long gasPrice, long gasLimit) throws Exception {
//		String tx = getTransactionInfo(inputAddr, inputPriKey, gasPrice, gasLimit, outputAddr, amount);
//		
//		String url = ETH_TEST_URL + "/txs/push?" + ETH_TOKEN;
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("tx", tx);
//		String sendJson = JsonSerializer.formatToString(params);
//		System.out.println("thedddd" + sendJson);
////		log.debug("tx send json is ", sendJson + "test");
//		
//		FramePacket fposttx = PacketHelper.buildUrlFromJson(sendJson, "POST", url);
//		val txretReg = sender.send(fposttx, 30000);
//		Map<String, Object> jsonRet = null;
//		try {
//			jsonRet = JsonSerializer.getInstance().deserialize(new String(txretReg.getBody()), Map.class);
//		} catch (Exception e) {
//			throw new Exception(e);
//		}
//		if(jsonRet.get("error") != null) {
//			jsonRet.put("errCode", "2");
//			jsonRet.put("msg",jsonRet.get("error"));
//		}else {
//			jsonRet.put("errCode", "000000");
//			jsonRet.put("msg", "successful");
//		}
//
//		Map<String, Object> assetMap = new HashMap<String, Object>();
//		assetMap.put("holdCount", jsonRet.get("balance"));
//		jsonRet.put("asset", assetMap);
//
//		return jsonRet;
//	}
//
//	/**
//	 * 获取交易的hash
//	 * 
//	 * @param inputAddr	发送方地址
//	 * @param inputAddrPriKey	发送方私钥
//	 * @param gasPrice		price
//	 * @param gasLimit		limit
//	 * @param outputAddr	接收方地址
//	 * @param amount		金额
//	 * @return
//	 * @throws Exception
//	 */
//	public String getTransactionInfo(String inputAddr, String inputAddrPriKey, long gasPrice, long gasLimit, String outputAddr,
//			long amount) throws Exception {
//		Map<String, Object> walletDetail = checkWalletETH(inputAddr);
////		byte[] onceByte =BigIntegers.asUnsignedByteArray(BigInteger.ZERO);
//		long o = 0l;
//		if(walletDetail != null && walletDetail.size() > 0 && walletDetail.get("nonce") != null) {
//			o = Long.valueOf(walletDetail.get("nonce").toString());
//			BigInteger once = new BigInteger(walletDetail.get("nonce").toString());
//			log.debug("the onceByte of address[" + inputAddr + "] is : " + once.intValue());
////			onceByte = BigIntegers.asUnsignedByteArray(once); 
//		}
//		Transaction tx = new Transaction(
//				ByteUtil.longToBytesNoLeadZeroes(o), 
//				ByteUtil.longToBytesNoLeadZeroes(gasPrice),
//				ByteUtil.longToBytesNoLeadZeroes(gasLimit), 
//				Hex.decode(outputAddr),
//				ByteUtil.longToBytesNoLeadZeroes(amount),
//				Hex.decode(""),
//				null
//			);
//
//		byte[] privateKey = Hex.decode(inputAddrPriKey);
//		ECKey fromPrivate = ECKey.fromPrivate(privateKey);
//		
//		tx.sign(fromPrivate);
//		
//		return Hex.toHexString(tx.getEncoded());
//
//	}
//}
