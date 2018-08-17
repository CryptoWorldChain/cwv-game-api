package org.brewchain.cwv.game.test;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.TransactionTypeEnum;
import org.brewchain.cwv.game.helper.CommonHelper;
import org.brewchain.test.service.Test.PWLTTestCommand;
import org.brewchain.test.service.Test.PWLTTestModule;
import org.brewchain.test.service.Test.ReqCreateCryptoToken;
import org.brewchain.wallet.service.Wallet.AccountCryptoToken;
import org.brewchain.wallet.service.Wallet.CryptoTokenData;
import org.brewchain.wallet.service.Wallet.MultiTransactionImpl;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

import com.google.protobuf.ByteString;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.async.CompleteHandler;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.PacketHelper;
import onight.tfw.otransio.api.beans.FramePacket;

@NActorProvider
@Slf4j
@Data
public class CreateCryptoTokenTestService extends SessionModules<ReqCreateCryptoToken> {
	
	@ActorRequire(name="Wlt_Helper", scope = "global")
	WltHelper wltHelper;
	
	@ActorRequire(name="Daos")
	Daos dao;
	

	@ActorRequire(name = "Common_Helper")
	CommonHelper commonHelper;
	
	@Override
	public String[] getCmds() {
		return new String[] { PWLTTestCommand.NCT.name() };
	}

	@Override
	public String getModule() {
		return PWLTTestModule.TTS.name();
	}
	
	
	public void onPBPacket(final FramePacket pack, final ReqCreateCryptoToken pb, final CompleteHandler handler) {
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andCryptoTokenIsNull();
		int total = (int) pb.getTotal();
		int offset = 0;
		int limit = 10000;
		example.setLimit(limit);
		while(dao.gamePropertyDao.countByExample(example)>0) {
			
			try {
				List<Object> list = dao.gamePropertyDao.selectByExample(example);
				RespCreateTransaction.Builder retCreate = createProcess(pb.getAddress(),pb.getSymbol(),total,list);
				if(retCreate.getRetCode() == 1) {
					
					try {
						Thread.currentThread().sleep(20000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					RespGetTxByHash.Builder respGetTxByHash = wltHelper.getTxInfo(retCreate.getTxHash());
					if(respGetTxByHash.getRetCode() == -1) {//失败
						ret.setRetMsg(ret.getRetMsg()+"\n error :"+retCreate.getTxHash());
						break ;
					}
					int totalTime = 0;
					while(totalTime<=20 * 60 * 1000 && StringUtils.isEmpty(wltHelper.getTxInfo(retCreate.getTxHash()).getTransaction().getStatus())){
						Thread.currentThread().sleep(20000);
						totalTime += 20000;
					}
					
					RespGetTxByHash.Builder respTx = wltHelper.getTxInfo(retCreate.getTxHash());
					
					String status = respTx.getTransaction().getStatus();
					if(StringUtils.isEmpty(status)) {
						ret.setRetMsg(ret.getRetMsg()+"\n no status exception :"+retCreate.getTxHash());
						break;
					}
					if(status.equals("error")) {
						continue;
					}
					ret.setRetMsg(ret.getRetMsg()+"\n status success :"+retCreate.getTxHash());
					
					commonHelper.txManageAdd(TransactionTypeEnum.CREATE_PROPERTY_TOKEN.getKey(), retCreate.getTxHash());
					RespCreateTransaction.Builder retUpdate = updatePropertyToken(total,respGetTxByHash.getTransaction(),list);
					ret.setRetMsg(ret.getRetMsg()+retUpdate.getRetMsg());
				}
			} catch (Exception e) {
				ret.setRetMsg(ret.getRetMsg()+"==>error createProcess "+offset+","+limit);
			}
			
		}
		
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
	
	private RespCreateTransaction.Builder createProcess(String address,String symbol, long total, List<Object> list) {
		CryptoTokenData.Builder builder = CryptoTokenData.newBuilder();
		builder
		.setSymbol(symbol)
		.setTotal(total)
		;
		for(Object o : list) {
			CWVGameProperty property = (CWVGameProperty) o;
			builder.addCode(property.getPropertyId()+"")
			.addName(property.getPropertyName());
		}
		
		return wltHelper.createCryptoTokenTx(address, builder);
		
	}
	
	
	private RespCreateTransaction.Builder updatePropertyToken(long total,MultiTransactionImpl transaction, List<Object> list) {
		 RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		
		long timestamp = transaction.getTxBody().getTimestamp();
		String address = transaction.getTxBody().getInputs(0).getAddress();
		for(int i=1;i<=list.size(); i++) {
			try {
				CWVGameProperty property = (CWVGameProperty) list.get(i-1);
				AccountCryptoToken.Builder oAccountCryptoToken = AccountCryptoToken.newBuilder();
				oAccountCryptoToken.setCode(property.getPropertyId()+"");
				oAccountCryptoToken.setExtData(ByteString.copyFrom("".getBytes()));
				oAccountCryptoToken.setIndex(i);
				oAccountCryptoToken.setName(property.getPropertyName());
				oAccountCryptoToken.setNonce(0);
				oAccountCryptoToken.setOwner(ByteString.copyFrom(wltHelper.getEncAPI().hexDec(address)));
				oAccountCryptoToken.setOwnertime(timestamp);
				oAccountCryptoToken.setTotal(total);
				oAccountCryptoToken.setTimestamp(timestamp);
				oAccountCryptoToken.clearHash();
				String hash = wltHelper.getEncAPI().hexEnc(wltHelper.getEncAPI().sha256Encode(oAccountCryptoToken.build().toByteArray()));
				property.setCryptoToken(hash);
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);
			} catch (Exception e) {
				System.out.println(e.getStackTrace());
			}
			
		}
		
		return ret.setRetCode(1).setRetMsg("\n cryptoToken update success :"+transaction.getTxHash());
		
	}

	public void onPBPacketBack(final FramePacket pack, final ReqCreateCryptoToken pb, final CompleteHandler handler) {
		RespCreateTransaction.Builder ret = RespCreateTransaction.newBuilder();
		CryptoTokenData.Builder builder = CryptoTokenData.newBuilder();
		builder
		.setSymbol(pb.getSymbol())
		.setTotal(pb.getTotal())
		;
		String[] names = pb.getName().split(",");
		String[] codes = pb.getCode().split(",");
		if(names.length!=codes.length) {
			ret.setRetCode(-1);
			return;
		}
		for(int i=0;i<names.length; i++ ){
			builder.addCode(codes[i])
			.addName(names[i]);
		}
		ret = wltHelper.createCryptoTokenTx(pb.getAddress(), builder);
		pack.getExtHead().buildFor(pack.getHttpServerletResponse());
		// 返回给客户端
		handler.onFinished(PacketHelper.toPBReturn(pack, ret.build()));
	}
}
