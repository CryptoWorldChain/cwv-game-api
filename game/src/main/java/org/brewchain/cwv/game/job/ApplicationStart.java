package org.brewchain.cwv.game.job;

import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.service.game.Game.PTPSModule;

import com.google.protobuf.Message;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.oapi.scala.commons.SessionModules;
import onight.osgi.annotation.NActorProvider;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;

@NActorProvider
@Provides(specifications = { ActorService.class }, strategy = "SINGLETON")
@Slf4j
@Data
public class ApplicationStart extends SessionModules<Message> {

	@Override
	public String[] getCmds() {
		return new String[] { "___" };
	}

	@Override
	public String getModule() {
		return PTPSModule.GGA.name();
	}

	@ActorRequire(name="Daos")
	Daos dao;

	@Validate
	public void startup() {
		try {
//			new Thread(new PropertyBidTask()).start();
//			final Timer timer = new Timer();
//			timer.schedule(new TimerTask() {
//				@Override
//				public void run() {
//					// copy db is db is not exists
//
//					// load block
//					blockChainHelper.onStart();
//
//					// get node
//					// Network oNetwork = dao.getPzp().networkByID("raft");
//					// KeyConstant.nodeName = oNetwork.root().name();
//					KeyConstant.nodeName = "测试节点01";
//				}
//			}, 1000 * 20);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}


