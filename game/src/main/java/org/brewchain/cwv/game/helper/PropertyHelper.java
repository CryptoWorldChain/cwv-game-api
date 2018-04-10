package org.brewchain.cwv.game.helper;

import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSCommonExchange;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.otransio.api.IPacketSender;

@iPojoBean
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
/**
 * 用户service
 * 
 * @author Moon
 * @date 2018-03-30
 */
public class PropertyHelper implements ActorService {

	@ActorRequire
	Daos daos;

	@ActorRequire(name = "http", scope = "global")
	IPacketSender sender;

	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	public void getPropertyExchange(PSCommonExchange pb,
			PRetPropertyExchange.Builder ret) {
		
		
	}

	/**
	 * 购买房产（创建房产交易）
	 * @param pb
	 * @param ret
	 */
	public void buyPropertyExchange(PSCommonExchange pb, Builder ret) {
		// TODO Auto-generated method stub
		
	}

	
}
