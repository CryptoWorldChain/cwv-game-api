package org.brewchain.cwv.game.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.bcvm.utils.ByteUtil;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.auth.impl.WltHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCity;
import org.brewchain.cwv.dbgens.game.entity.CWVGameDic;
import org.brewchain.cwv.dbgens.game.entity.CWVGameDicExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMap;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyGame;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyGameExample;
import org.brewchain.cwv.dbgens.game.entity.CWVGameTxManage;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuy;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeBuyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncomeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradePwd;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWalletExample;
import org.brewchain.cwv.game.chain.PropertyBidInvoker;
import org.brewchain.cwv.game.chain.PropertyDrawInvoker;
import org.brewchain.cwv.game.chain.PropertyExchangeInvoker;
import org.brewchain.cwv.game.chain.PropertyIncomeInvoker;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ChainTransStatusEnum;
import org.brewchain.cwv.game.enums.CoinEnum;
import org.brewchain.cwv.game.enums.CryptoTokenEnum;
import org.brewchain.cwv.game.enums.MarketTypeEnum;
import org.brewchain.cwv.game.enums.PropertyBidStatusEnum;
import org.brewchain.cwv.game.enums.PropertyExchangeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyIncomeStatusEnum;
import org.brewchain.cwv.game.enums.PropertyStatusEnum;
import org.brewchain.cwv.game.enums.PropertyTypeEnum;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.enums.TransactionTypeEnum;
import org.brewchain.cwv.game.enums.UpAndDownEnum;
import org.brewchain.cwv.game.job.PropertyJobHandle;
import org.brewchain.cwv.game.util.Arith;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Bid.PRetBidPropertyDetail;
import org.brewchain.cwv.service.game.Bid.PRetBidPropertyNotice;
import org.brewchain.cwv.service.game.Bid.PRetBidPropertyNotice.AuctionRank;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBid;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBid.PropertyBid;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBidAuction;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBidAuction.PropertyBidAuction;
import org.brewchain.cwv.service.game.Bid.PSAuctionProperty;
import org.brewchain.cwv.service.game.Bid.PSCommonBid;
import org.brewchain.cwv.service.game.Bid.PSCreatePropertyBid;
import org.brewchain.cwv.service.game.Bid.PSPropertyBid;
import org.brewchain.cwv.service.game.Bid.PSPropertyBidAuction;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDraw;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord.PropertyDraw;
import org.brewchain.cwv.service.game.Draw.PSCommonDraw;
import org.brewchain.cwv.service.game.Draw.PSPropertyDrawRecord;
import org.brewchain.cwv.service.game.Draw.PSPropertyDrawTx;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange.PropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSBuyProperty;
import org.brewchain.cwv.service.game.Exchange.PSCommonExchange;
import org.brewchain.cwv.service.game.Exchange.PSPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSSellProperty;
import org.brewchain.cwv.service.game.Game.BidInfo;
import org.brewchain.cwv.service.game.Game.ExchangeInfo;
import org.brewchain.cwv.service.game.Game.GameDetail;
import org.brewchain.cwv.service.game.Game.MapPropertyDetail;
import org.brewchain.cwv.service.game.Game.MapPropertyDetail.PropertyState;
import org.brewchain.cwv.service.game.Game.PBGameProperty;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;
import org.brewchain.cwv.service.game.Game.PRetGamePropertyCharge;
import org.brewchain.cwv.service.game.Game.PRetRefGameProperty;
import org.brewchain.cwv.service.game.Game.PSCommon;
import org.brewchain.cwv.service.game.Game.PSMapPropertyDetail;
import org.brewchain.cwv.service.game.Game.PSPropertyGame;
import org.brewchain.cwv.service.game.Game.PSPropertyGameDetail;
import org.brewchain.cwv.service.game.Game.Property;
import org.brewchain.cwv.service.game.Game.PropertyGameInfo;
import org.brewchain.cwv.service.game.Game.PropertyGameInfo.GameInfo;
import org.brewchain.cwv.service.game.Game.PropertyGameType;
import org.brewchain.cwv.service.game.Game.PropertyGameType.GameType;
import org.brewchain.cwv.service.game.Game.RetCodeMsg;
import org.brewchain.cwv.service.game.Game.RetData;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome.PropertyIncome;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome.PropertyInfo;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome.PropertyInfo.SubTypeInfo;
import org.brewchain.cwv.service.game.User.PSPropertyIncome;
import org.brewchain.cwv.service.game.User.PSPropertyIncomeClaim;
import org.brewchain.wallet.service.Wallet.AccountCryptoTokenImpl;
import org.brewchain.wallet.service.Wallet.AccountCryptoValueImpl;
import org.brewchain.wallet.service.Wallet.RespCreateContractTransaction;
import org.brewchain.wallet.service.Wallet.RespCreateTransaction;
import org.brewchain.wallet.service.Wallet.RespGetAccount;
import org.brewchain.wallet.service.Wallet.RespGetTxByHash;

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
 * 用户service
 * 
 * @author Moon
 * @date 2018-03-30
 */
@Instantiate(name = "Property_Helper")
public class PropertyHelper implements ActorService {

	@ActorRequire(name = "Daos", scope = "global")
	Daos dao;

	@ActorRequire(name = "Wallet_Helper")
	WalletHelper walletHelper;

	@ActorRequire(name = "User_Helper", scope = "global")
	UserHelper userHelper;

	@ActorRequire(name = "Property_Exchange_Invoker")
	PropertyExchangeInvoker exchangeInvoker;

	@ActorRequire(name = "Property_Bid_Invoker")
	PropertyBidInvoker bidInvoker;

	@ActorRequire(name = "Property_Draw_Invoker")
	PropertyDrawInvoker drawInvoker;

	@ActorRequire(name = "Property_Income_Invoker")
	PropertyIncomeInvoker incomeInvoker;

	@ActorRequire(name = "Common_Helper")
	CommonHelper commonHelper;

	@ActorRequire(name = "Game_Notice_Helper")
	GameNoticeHelper gameNoticeHelper;

	@ActorRequire(name = "Wlt_Helper", scope = "global")
	WltHelper wltHelper;

	private HashSet statusForSale = new HashSet<String>() {
		{
			add("0"); // 未出售
			// add("1"); 出售中
			// add("2"); 竞拍中
			add("3"); // 已出售
		}
	};


	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	public void getPropertyExchange(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, FramePacket pack) throws DecoderException {

		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		if (StringUtils.isNoneBlank(pb.getExchangeId())) {
			setExchangeSingleRet(pb, ret, pack);
		} else
			setExchangeRet(pb, ret, page, pack);
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());

	}
	/**
	 * 查询房产交易列表
	 * @param pb
	 * @param ret
	 * @param page
	 * @param pack
	 * @throws DecoderException 
	 */
	private void setExchangeRet(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, PageUtil page,
			FramePacket pack) throws DecoderException {
		// 设置查询条件
		CWVGamePropertyExample cwvPropertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria = cwvPropertyExample.createCriteria();
		cwvPropertyExample.setLimit(page.getLimit());
		cwvPropertyExample.setOffset(page.getOffset());
		page.setTotalCount(0);
		criteria.andPropertyStatusNotEqualTo(PropertyStatusEnum.NOOWNER.getValue());
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		if (pb.getUserOnly() == 1 && StringUtils.isEmpty(pb.getExchangeStatus()) ) {
			CWVUserWalletExample wltExample = new CWVUserWalletExample();
			wltExample.createCriteria().andUserIdEqualTo(user.getUserId()).andCoinTypeEqualTo((byte) 0);
			List<Object> wltList = dao.walletDao.selectByExample(wltExample);
			if (wltList.isEmpty()) {
				log.debug("未获取到账户信息");
				return;
			}
			CWVUserWallet wallet = (CWVUserWallet) wltList.get(0);
			RespGetAccount.Builder accInfo = wltHelper.getAccountInfo(wallet.getAccount());

			// 遍历判断账户是否有此房产，并获取cryptoToken
			if (accInfo.getRetCode() != 1) {
				log.error("未找到账户相关信息");
				return;
			}
			List<AccountCryptoValueImpl> cryptosList = accInfo.getAccount().getCryptosList();
			if (cryptosList.isEmpty()) {
				log.error("未找到账户的erc721信息");
				return;
			}
			List<AccountCryptoTokenImpl> tokens = new ArrayList<>();
			// StringBuffer sb = new StringBuffer();
			List<Integer> ids = new ArrayList<>();
			for (int i = 0; i < cryptosList.size(); i++) {
				if (!cryptosList.get(i).getSymbol().equals(CryptoTokenEnum.CYT_HOUSE.getValue())) {
					continue;// 如果不是房产类型的erc721，跳出继续搜索
				}
				tokens = cryptosList.get(i).getTokensList();
				if (tokens.isEmpty()) {
					log.debug("该账户无房产");
					return;
				}
				for (int j = 0; j < tokens.size(); j++) {
					ids.add(ByteUtil.byteArrayToInt(Hex.decodeHex(tokens.get(j).getCode().toCharArray())));
					// sb.append(tokens.get(j).getCode());
					// if(j<tokens.size()-1){
					// sb.append(",");
					// }
				}
			}

			// criteria.andUserIdEqualTo(user.getUserId());
			if (ids.size() > 0)
				criteria.andPropertyIdIn(ids);
			else
				return;
			if (StringUtils.isNotBlank(pb.getExchangeStatus())) {
				criteria.andPropertyStatusEqualTo(pb.getExchangeStatus());
			}
			
		} else if(pb.getUserOnly() == 1){//个人出售中房产
		
			criteria.andUserIdEqualTo(user.getUserId());
			if(StringUtils.isNotEmpty(pb.getExchangeStatus()))
				criteria.addCriterion(" property_id in (select property_id from cwv_market_exchange where status ='"+pb.getExchangeStatus()+"')");
			
		} else {//房产交易列表
			
			criteria.andUserIdNotEqualTo(user.getUserId());
			if(StringUtils.isNotEmpty(pb.getExchangeStatus()))
				criteria.addCriterion(" property_id in (select property_id from cwv_market_exchange where status ='"+pb.getExchangeStatus()+"')");
			
		}
		criteria.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey())
		.andCryptoTokenIsNotNull();
		// 房产类型
		if (StringUtils.isNotEmpty(pb.getPropertyType())) {
			criteria.andPropertyTypeEqualTo(pb.getPropertyType());
		}

		// 房产名称
		if (StringUtils.isNotEmpty(pb.getPropertyName())) {
			criteria.andPropertyNameLike("%" + pb.getPropertyName() + "%");
		}

		// 国家
		if (StringUtils.isNotEmpty(pb.getCountryId())) {
			criteria.addCriterion(
					"game_map_id in (select map_id from cwv_game_map where game_city_id in (select city_id from cwv_game_city where game_country_id='"
							+ pb.getCountryId() + "') )");
		}

		// // 城市
		// if (StringUtils.isNotEmpty(pb.getCityId())) {
		// criteria.andCountryId(Integer.parseInt(pb.getCityId()));
		// }

		// 价格排序
		if (StringUtils.isNotEmpty(pb.getPriceType())) {
			if (pb.getPriceType().equals("0"))
				cwvPropertyExample.setOrderByClause(" last_price desc ");
			if (pb.getPriceType().equals("1"))
				cwvPropertyExample.setOrderByClause(" last_price ");
		}

		// 收益排序
		if (StringUtils.isNotEmpty(pb.getIncomeType())) {
			if (pb.getIncomeType().equals("0"))
				cwvPropertyExample.setOrderByClause(" income desc ");
			else if (pb.getIncomeType().equals("1"))
				cwvPropertyExample.setOrderByClause(" income ");
		}
		int count = dao.gamePropertyDao.countByExample(cwvPropertyExample);
		page.setTotalCount(count);
		List<Object> list = dao.gamePropertyDao.selectByExample(cwvPropertyExample);

		for (Object o : list) {
			CWVGameProperty gameProperty = (CWVGameProperty) o;
			// 设置房产信息
			Property.Builder property = Property.newBuilder();

			propertyCopy(property, gameProperty);

			
			PropertyExchange.Builder propertyExchange = PropertyExchange.newBuilder();
			propertyExchange.setProperty(property);
			// 设置交易信息
			CWVMarketExchangeExample exchangeExample = new CWVMarketExchangeExample();
			CWVMarketExchangeExample.Criteria criteria2 = exchangeExample.createCriteria()
					.andPropertyIdEqualTo(gameProperty.getPropertyId())
					.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey());

			if (pb.getUserOnly() == 1) {
				if (StringUtils.isEmpty(pb.getExchangeStatus())) {// 查询普通房产
					ret.addPropertyExchange(propertyExchange);
					continue;
				} else {
					criteria2.andStatusEqualTo(Byte.parseByte(pb.getExchangeStatus()));
				}
			} else {
				criteria2.andStatusEqualTo(Byte.parseByte(pb.getExchangeStatus()));
			}

			List<Object> listExchange = dao.exchangeDao.selectByExample(exchangeExample);
			for (Object ob : listExchange) {

				CWVMarketExchange exchange = (CWVMarketExchange) ob;
				ExchangeInfo.Builder exchangeRet = ExchangeInfo.newBuilder();
				exchangeInfoSet(exchangeRet, exchange);
				propertyExchange.setExchange(exchangeRet);
			}

			ret.addPropertyExchange(propertyExchange);
		}
	}

	private void setExchangeSingleRet(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, FramePacket pack) {
		// 设置查询条件
		CWVMarketExchange exchange = new CWVMarketExchange();
		exchange.setExchangeId(Integer.parseInt(pb.getExchangeId()));

		CWVAuthUser user = userHelper.getCurrentUser(pack);
		if (pb.getUserOnly() == 1) {
			exchange.setCreateUser(user.getUserId());
		}

		exchange = dao.exchangeDao.selectByPrimaryKey(exchange);
		if (exchange == null) {
			return;
		}

		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(exchange.getPropertyId());
		gameProperty = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
		// 设置房产信息
		Property.Builder property = Property.newBuilder();

		propertyCopy(property, gameProperty);
		// 设置priceLine
		CWVMarketExchangeExample exchangeExample2 = new CWVMarketExchangeExample();
		exchangeExample2.createCriteria().andPropertyIdEqualTo(gameProperty.getPropertyId())
				.andStatusEqualTo(PropertyExchangeStatusEnum.SOLD.getValue());
		exchangeExample2.setLimit(10);
		List<Object> listExchange2 = dao.exchangeDao.selectByExample(exchangeExample2);
		StringBuffer exchangePrice = new StringBuffer();
		if (listExchange2 != null && !listExchange2.isEmpty()) {
			for (int i = 0; i < listExchange2.size(); i++) {
				CWVMarketExchange echange2 = (CWVMarketExchange) listExchange2.get(i);
				exchangePrice.append(",").append(echange2.getSellPrice());
			}
		}
		property.setPriceLine(exchangePrice.toString());

		PropertyExchange.Builder propertyExchange = PropertyExchange.newBuilder();
		propertyExchange.setProperty(property);
		// 设置交易信息
		ExchangeInfo.Builder exchangeRet = ExchangeInfo.newBuilder();
		exchangeInfoSet(exchangeRet, exchange);
		propertyExchange.setExchange(exchangeRet);
		ret.addPropertyExchange(propertyExchange);
	}

	private void exchangeInfoSet(ExchangeInfo.Builder exchangeRet, CWVMarketExchange exchange) {
		exchangeRet.setExchangeId(exchange.getExchangeId() + "");
		exchangeRet.setPrice(exchange.getSellPrice() + "");
		exchangeRet.setChainStatus(exchange.getChainStatus()+"");
		exchangeRet.setStatus(exchange.getStatus() + "");
	}

	private void setBidRet(PSPropertyBid pb, PRetPropertyBid.Builder ret, PageUtil page, FramePacket pack) {
		// 设置查询条件
		CWVGamePropertyExample cwvPropertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria = cwvPropertyExample.createCriteria();
		cwvPropertyExample.setLimit(page.getLimit());
		cwvPropertyExample.setOffset(page.getOffset());

		CWVAuthUser authUser = userHelper.getCurrentUser(pack);

		// // 城市
		// if (StringUtils.isNotEmpty(pb.getCityId())) {
		// criteria.andCountryId(Integer.parseInt(pb.getCityId()));
		// }
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		if (pb.getUserOnly() == 1) {
			// 用户
			criteria.addCriterion("property_id in (select game_property_id from cwv_market_bid where status='"
					+ PropertyBidStatusEnum.BIDDING.getValue()
					+ "' and bid_id in (select bid_id from cwv_market_auction  where user_id='" + user.getUserId()
					+ "' and status='1' ))");

		} else {

			if (!StringUtils.isEmpty(pb.getStatus())) {
				criteria.addCriterion("property_id in (select game_property_id from cwv_market_bid where status='"
						+ pb.getStatus() + "' )");
			}
		}
		
		// 房产类型
		if (StringUtils.isNotEmpty(pb.getPropertyType())) {
			criteria.andPropertyTypeEqualTo(pb.getPropertyType());
		}

		// 房产名称
		if (StringUtils.isNotEmpty(pb.getPropertyName())) {
			criteria.andPropertyNameLike("%" + pb.getPropertyName() + "%");
		}

		// 国家
		if (StringUtils.isNotEmpty(pb.getCountryId())) {
			criteria.addCriterion(
					"game_map_id in (select map_id from cwv_game_map where game_city_id in (select city_id from cwv_game_city where game_country_id='"
							+ pb.getCountryId() + "') )");
		}

		// // 城市
		// if (StringUtils.isNotEmpty(pb.getCityId())) {
		// criteria.andCountryId(Integer.parseInt(pb.getCityId()));
		// }

		// 价格排序
		if (StringUtils.isNotEmpty(pb.getPriceType())) {
			if (pb.getPriceType().equals("0"))
				cwvPropertyExample.setOrderByClause(" last_price desc ");
			if (pb.getPriceType().equals("1"))
				cwvPropertyExample.setOrderByClause(" last_price ");
		}

		// 收益排序
		if (StringUtils.isNotEmpty(pb.getIncomeType())) {
			if (pb.getIncomeType().equals("0"))
				cwvPropertyExample.setOrderByClause(" income desc ");
			else if (pb.getIncomeType().equals("1"))
				cwvPropertyExample.setOrderByClause(" income ");
		}
		// 收益排序
		int count = dao.gamePropertyDao.countByExample(cwvPropertyExample);
		page.setTotalCount(count);
		List<Object> list = dao.gamePropertyDao.selectByExample(cwvPropertyExample);

		for (Object o : list) {
			CWVGameProperty gameProperty = (CWVGameProperty) o;
			// 设置房产信息
			Property.Builder property = Property.newBuilder();

			propertyCopy(property, gameProperty);

			PropertyBid.Builder propertyBid = PropertyBid.newBuilder();
			propertyBid.setProperty(property);
			// 设置交易信息
			CWVMarketBidExample bidExample = new CWVMarketBidExample();
			CWVMarketBidExample.Criteria criteria2 = bidExample.createCriteria();
			criteria2.andGamePropertyIdEqualTo(gameProperty.getPropertyId())
					.andStatusNotEqualTo(PropertyBidStatusEnum.NOBID.getValue());
			// 判断是否查询本用户信息
			if (pb.getUserOnly() == 1) {
				criteria2.andStatusEqualTo(PropertyBidStatusEnum.BIDDING.getValue());
			}
			List<Object> listBid = dao.bidDao.selectByExample(bidExample);
			for (Object ob : listBid) {

				CWVMarketBid bid = (CWVMarketBid) ob;
				BidInfo.Builder bidRet = BidInfo.newBuilder();
				bidInfoSet(bidRet, bid);
				// 设置用户竞价
				if (pb.getUserOnly() == 1) {
					CWVMarketAuction auctionUser = getUserAuction(bid.getBidId(), authUser.getUserId());
					bidRet.setUserPrice(auctionUser == null ? "0" : auctionUser.getBidPrice() + "");

				}
				propertyBid.setBid(bidRet);
			}

			ret.addPropertyBid(propertyBid);

		}

	}

	private void setBidSingleRet(PSPropertyBid pb, PRetPropertyBid.Builder ret, FramePacket pack) {
		// 设置查询条件
		CWVMarketBid bid = new CWVMarketBid();
		bid.setBidId(Integer.parseInt(pb.getBidId()));

		CWVAuthUser user = userHelper.getCurrentUser(pack);
		if (pb.getUserOnly() == 1)
			bid.setCreateUser(user.getUserId() + "");
		bid = dao.bidDao.selectByPrimaryKey(bid);
		if (bid == null) {
			return;
		}

		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(bid.getGamePropertyId());
		gameProperty = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);

		// 设置房产信息
		Property.Builder property = Property.newBuilder();

		propertyCopy(property, gameProperty);

		PropertyBid.Builder propertyBid = PropertyBid.newBuilder();
		propertyBid.setProperty(property);

		BidInfo.Builder bidRet = BidInfo.newBuilder();
		bidInfoSet(bidRet, bid);
		// 设置用户竞价
		if (pb.getUserOnly() == 1) {
			CWVMarketAuction auctionUser = getUserAuction(bid.getBidId(), user.getUserId());
			bidRet.setUserPrice(auctionUser == null ? "0" : auctionUser.getBidPrice() + "");

		}
		propertyBid.setBid(bidRet);

		ret.addPropertyBid(propertyBid);

	}

	private List<Map<String, Object>> executeSqlRet(String sql, PageUtil pageUtil) {
		String sqlCount = sql.substring(sql.indexOf("from"));
		sqlCount = "select count(1) as count " + sqlCount;
		List<Map<String, Object>> listCount = dao.provider.getCommonSqlMapper().executeSql(sqlCount);
		String count = listCount.get(0).get("count").toString();
		pageUtil.setTotalCount(Integer.parseInt(count));

		return dao.provider.getCommonSqlMapper().executeSql(sql);
	}

	private String propertyExchangeSql(StringBuffer sb, PSPropertyExchange pb, String userId) {

		sb.append(" select co.country_id,m.map_id,p.property_template_id,p.property_template, ")
				.append(" u.nick_name as owner,p.property_name,p.property_id,p.property_type,p.property_status,")
				.append(" '房产说明' as income_remark , p.income , e.exchange_id, e.price as exchange_price, e.status as exchange_status ")// 查询字典

				.append(" from ");

		if (StringUtils.isNotEmpty(userId)) {
			sb.append(" (select * from cwv_market_exchange where user_id='").append(userId)
					.append("' and create_user = '").append(userId).append("') ");
		} else {
			sb.append(" cwv_market_exchange ");
		}

		sb.append(" e, (SELECT * FROM cwv_game_property WHERE 1=1 ");

		// 房产类型
		if (StringUtils.isNotEmpty(pb.getPropertyType())) {
			sb.append(" AND property_type = '" + pb.getPropertyType() + "'");
		}

		// 房产名称
		if (StringUtils.isNotEmpty(pb.getPropertyName())) {
			sb.append(" AND property_name LIKE '%" + pb.getPropertyName() + "%'");
		}

		sb.append(" and property_status = '2' ) p LEFT JOIN cwv_auth_user u on p.user_id = u.user_id,  ");
		// 国家
		if (StringUtils.isNotEmpty(pb.getCountryId())) {
			sb.append(" (select * from cwv_game_country where 1=1 AND country_id = '" + pb.getCountryId() + "' ) co, ");
		} else {
			sb.append(" cwv_game_country co,");
		}

		sb.append("");
		// 城市
		if (StringUtils.isNotEmpty(pb.getCityId())) {
			sb.append("  (select * from cwv_game_city where 1=1 AND city_id = '" + pb.getCityId() + "' ) ci,");

		} else {
			sb.append(" cwv_game_city ci, ");
		}

		sb.append(" cwv_game_map m ").append(
				" where p.property_id = e.property_id and p.game_map_id = m.map_id and m.game_city_id = ci.city_id and ci.game_country_id = co.country_id ");

		// 价格排序
		if (StringUtils.isNotEmpty(pb.getPriceType())) {
			if (pb.getPriceType().equals("0"))
				sb.append(" order by p.last_price desc ");
			if (pb.getPriceType().equals("1"))
				sb.append(" order by p.last_price ");
		}

		// 收益排序
		if (StringUtils.isNotEmpty(pb.getIncomeType())) {
			if (sb.indexOf("order by") > 0) {
				if (pb.getPriceType().equals("0"))
					sb.append(" , p.income desc ");
				else if (pb.getPriceType().equals("1"))
					sb.append(" , p.income ");
			} else {
				if (pb.getPriceType().equals("0"))
					sb.append(" order by p.income desc ");
				else if (pb.getPriceType().equals("1"))
					sb.append(" order by p.income ");
			}

		}

		return sb.toString();
	}

	/**
	 * 
	 * @param sb
	 * @param pb
	 * @param userId
	 * @return
	 */
	private String propertyBidSql(StringBuffer sb, PSPropertyBid pb, String userId) {

		sb.append(" select co.country_id,m.map_id,p.property_template_id,p.property_template, ")
				.append(" u.nick_name as owner,p.property_name,p.property_id,p.property_type,p.property_status,")
				.append(" '房产说明' as income_remark , e.bid_id,e.auction_start as bid_auction_start,e.price as bid_price ")// 查询字典

				.append(" from ");

		if (StringUtils.isNotEmpty(pb.getStatus())) {
			sb.append(" (select * from cwv_market_bid where status='").append(pb.getStatus()).append("') ");
		} else {
			sb.append(" cwv_market_bid ");
		}

		sb.append(" e, (SELECT * FROM cwv_game_property WHERE 1=1 ");

		// // 房产类型
		// if (StringUtils.isNotEmpty(pb.getPropertyType())) {
		// sb.append(" AND property_type = '"+pb.getPropertyType()+"'");
		// }
		//
		// //房产名称
		// if (StringUtils.isNotEmpty(pb.getPropertyName())) {
		// sb.append(" AND property_name LIKE '%"+pb.getPropertyName()+"%'");
		// }
		//
		// sb.append(" and property_status = '2' ) p LEFT JOIN cwv_auth_user u
		// on p.user_id = u.user_id, ");
		// // 国家
		// if (StringUtils.isNotEmpty(pb.getCountryId())) {
		// sb.append(" (select * from cwv_game_country where 1=1 AND country_id
		// = '"+pb.getCountryId()+"' ) co, ");
		// }else{
		// sb.append(" cwv_game_country co,");
		// }
		//
		// sb.append("");
		// // 城市
		// if (StringUtils.isNotEmpty(pb.getCityId())) {
		// sb.append(" (select * from cwv_game_city where 1=1 AND city_id =
		// '"+pb.getCityId()+"' ) ci,");
		//
		// }else {
		// sb.append(" cwv_game_city ci, ");
		// }
		//
		sb.append(" cwv_game_map m ").append(
				" where p.property_id = e.property_id and p.game_map_id = m.map_id and m.game_city_id = ci.city_id and ci.game_country_id = co.country_id ");

		// // 价格排序
		// if( StringUtils.isNotEmpty(pb.getPriceType()) ) {
		// if(pb.getPriceType().equals("0"))
		// sb.append(" order by p.last_price desc ");
		// if(pb.getPriceType().equals("1"))
		// sb.append(" order by p.last_price ");
		// }
		//
		// // 收益排序
		// if (StringUtils.isNotEmpty(pb.getIncomeType())) {
		// if(sb.indexOf("order by")>0) {
		// if(pb.getPriceType().equals("0"))
		// sb.append(" , p.income desc ");
		// else if(pb.getPriceType().equals("1"))
		// sb.append(" , p.income ");
		// }else {
		// if(pb.getPriceType().equals("0"))
		// sb.append(" order by p.income desc ");
		// else if(pb.getPriceType().equals("1"))
		// sb.append(" order by p.income ");
		// }
		//
		// }

		return sb.toString();
	}

	/**
	 * 购买房产
	 * 
	 * @param pb
	 * @param ret
	 */
	public void buyProperty(FramePacket pack, PSBuyProperty pb, Builder ret) {

		// 校验
		if (StringUtils.isEmpty(pb.getExchangeId())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("交易ID不能为空");
			return;
		}

		if (StringUtils.isEmpty(pb.getTradePwd())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("交易密码不能为空");
			return;
		}

		// 查询交易状态
		CWVMarketExchange exchangeRecord = new CWVMarketExchange();
		exchangeRecord.setExchangeId(Integer.parseInt(pb.getExchangeId()));
		final CWVMarketExchange exchange = dao.exchangeDao.selectByPrimaryKey(exchangeRecord);
		if (exchange == null) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_ID.getRetMsg());
			return;
		}

		if (exchange.getStatus().byteValue() == PropertyExchangeStatusEnum.ONSALE.getValue()
				&& exchange.getChainStatus().equals(ChainTransStatusEnum.DONE.getKey())) {
			// 不做处理
		} else {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetMsg());
			return;
		}

		// 获取当前用户
		final CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		if (exchange.getCreateUser().equals(authUser.getUserId())) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_USER.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_USER.getRetMsg());
			return;
		}

		// 交易密码
		CWVUserTradePwd userTradePwd = userHelper.getTradePwd(authUser.getUserId());
		if (userTradePwd == null) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_VALIDATE_PWD_SET.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_VALIDATE_PWD_SET.getRetMsg());
			return;
		} else {
			if (!userTradePwd.getTradePassword().equals(userHelper.getPwdMd5(pb.getTradePwd(), authUser.getSalt()))) {
				ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetMsg());
				return;
			}
		}

		// 查询余额

		final CWVUserWallet userWalletBuyer = walletHelper.getUserAccount(authUser.getUserId(), CoinEnum.CWB);
		if (userWalletBuyer.getBalance().subtract(exchange.getSellPrice()).intValue() < 0) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_BALANCE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_BALANCE.getRetMsg());
			return;
		}

		// 调取合约，发起购买

		// 创建申请记录
		CWVMarketExchangeBuyExample buyExample = new CWVMarketExchangeBuyExample();
		// 是否存在申请记录
		buyExample.createCriteria().andExchangeIdEqualTo(exchange.getExchangeId());

		Object o = dao.exchangeBuyDao.selectOneByExample(buyExample);

		if (o != null) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_EXCHANGE_EXISTS.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_EXCHANGE_EXISTS.getRetMsg());
			return;
		} 
		// 创建申请记录
		final CWVMarketExchangeBuy buy = new CWVMarketExchangeBuy();
		buy.setExchangeId(exchange.getExchangeId());
		buy.setAmount(exchange.getSellPrice());
		buy.setBuyerAddress(userWalletBuyer.getAccount());
		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(exchange.getPropertyId());
		final CWVGameProperty property = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
		buy.setPropertyToken(property.getCryptoToken());
		CWVUserWallet seller = walletHelper.getUserAccount(exchange.getCreateUser(), CoinEnum.CWB);
		buy.setSellerAddress(seller.getAccount());
		
		dao.exchangeBuyDao.insert(buy);

		RespCreateTransaction.Builder exchangeRet = wltHelper.createTx(buy.getAmount(), PropertyJobHandle.MARKET_EXCHANGE_AGENT,
				buy.getBuyerAddress());
		// 添加调取合约日志 TODO

		
//		txManage.getDescription()
		
		if (exchangeRet.getRetCode() != 1) {
			ret.setRetCode("99");
			ret.setRetMsg(exchangeRet.getRetMsg());
			dao.exchangeBuyDao.deleteByPrimaryKey(buy);
			return;
		}

		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.EXCHANGE_BUY_AMOUNT.getKey(),exchangeRet.getTxHash());
				
		// 设置 交易成功数据 start
		buy.setChainStatus(ChainTransStatusEnum.START.getKey());
		buy.setChainTransHash(exchangeRet.getTxHash());
		
		
		// 设置房产交易
		exchange.setStatus(PropertyExchangeStatusEnum.SOLD.getValue());
		exchange.setUserId(authUser.getUserId());

		exchange.setChainStatus(ChainTransStatusEnum.START.getKey());
		// exchange.setChainContract();
		exchange.setChainTransHash(exchangeRet.getTxHash());

		//设置房产状态
		property.setChainStatus(ChainTransStatusEnum.START.getKey());
		property.setChainTransHash(exchangeRet.getTxHash());
		
		// 设置账户信息
//		userWalletBuyer.setBalance(userWalletBuyer.getBalance().subtract(exchange.getSellPrice()));

		// 设置交易记录
		final CWVUserTransactionRecord recordBuy = new CWVUserTransactionRecord();
		recordBuy.setCreateTime(new Date());
		recordBuy.setCreateUser(exchange.getUserId());
		recordBuy.setDetail("买入房产");
		recordBuy.setGainCost(exchange.getSellPrice().negate());
		recordBuy.setUserId(exchange.getUserId());
		recordBuy.setMarketId(exchange.getExchangeId());
		recordBuy.setType(MarketTypeEnum.EXCHANGE.getValue());

		// 统一事务处理 暂时没有代码

		dao.exchangeDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				// 申请记录更新交易信息
				dao.exchangeBuyDao.updateByPrimaryKeySelective(buy);
				// 更新交易
				dao.exchangeDao.updateByPrimaryKeySelective(exchange);
				
				//更新房产
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);
				
				// 用户交易
				// 账户余额
				userWalletBuyer.setUpdateTime(new Date());
				dao.walletDao.updateByPrimaryKeySelective(userWalletBuyer);
				// 账户交易记录
				dao.userTransactionRecordDao.insert(recordBuy);

				// 转到定时任务处理 start
				// 更新房产
				// dao.gamePropertyDao.updateByPrimaryKeySelective(property);
				// userWalletSeller.setUpdateTime(new Date());
				// dao.walletDao.updateByPrimaryKeySelective(userWalletSeller);
				// dao.userTransactionRecordDao.insert(recordSell);

				// 转到定时任务处理 end

				return null;
			}

		});

		// 设置 交易成功数据 end
		Property.Builder propertyBuilder = Property.newBuilder();
		CWVGameProperty propertyUpdated = new CWVGameProperty();
		propertyUpdated.setPropertyId(exchange.getPropertyId());
		propertyUpdated = dao.gamePropertyDao.selectByPrimaryKey(propertyUpdated);
		propertyCopy(propertyBuilder, propertyUpdated);
		ret.setProperty(propertyBuilder);
		ExchangeInfo.Builder exchangeInfo = ExchangeInfo.newBuilder();
		CWVMarketExchange exchangeNew = dao.exchangeDao.selectByPrimaryKey(exchange);
		exchangeInfoSet(exchangeInfo, exchangeNew);
		ret.setExchange(exchangeInfo);
		// 设置返回数据
		ret.setRetCode(ReturnCodeMsgEnum.BPS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.BPS_SUCCESS.getRetMsg());

	}

	/**
	 * 卖出房产
	 * 
	 * @param pb
	 * @param ret
	 */
	public void sellProperty(FramePacket pack, PSSellProperty pb, PRetCommon.Builder ret) {

		// 校验
		if (StringUtils.isEmpty(pb.getPropetyId())) {// 房产ID
			ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetMsg());
			return;
		}

		if (pb.getPrice() <= 0) {// 出售价格
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			ret.setRetMsg("出售价格错误");
			return;
		}

		if (StringUtils.isEmpty(pb.getTradePwd())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("交易密码不能为空");
			return;
		}
		// 获取当前用户
		final CWVAuthUser user = userHelper.getCurrentUser(pack);
		// 交易密码
		CWVUserTradePwd userTradePwd = userHelper.getTradePwd(user.getUserId());
		if (userTradePwd == null) {
			ret.setRetCode(ReturnCodeMsgEnum.SPS_VALIDATE_PWD_SET.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.SPS_VALIDATE_PWD_SET.getRetMsg());
			return;
		} else {
			if (!userTradePwd.getTradePassword().equals(userHelper.getPwdMd5(pb.getTradePwd(), user.getSalt()))) {
				ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_TRADEPWD.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_TRADEPWD.getRetMsg());
				return;
			}
		}
		// 查询该用户房产
		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(Integer.parseInt(pb.getPropetyId()));
		final CWVGameProperty property = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
		if (property == null || !property.getUserId().equals(user.getUserId()) || StringUtils.isEmpty(property.getCryptoToken())) {// 当前用户
			ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetMsg());
			return;
		}
		//TODO 加入 chainStatus
		if (!this.statusForSale.contains(property.getPropertyStatus()) || property.getChainStatus()!=ChainTransStatusEnum.DONE.getKey()) {// 必须是未出售状态
			ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_STATUS.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_STATUS.getRetMsg());
			return;
		}
		// 扣除手续费
		double chargeRate = Double.parseDouble(PropertyJobHandle.EXCHANGE_CHARGE);
		double charge = Arith.mul(pb.getPrice(), chargeRate) ;

		// 生成交易
		// 调取卖出房产合约
		CWVUserWallet account = walletHelper.getUserAccount(user.getUserId(), CoinEnum.CWB);
		RespCreateTransaction.Builder exchangeRet = exchangeInvoker.sellProperty(account.getAccount(),
				property.getCryptoToken(), 0);

		// 添加调取合约日志 TODO

		if (exchangeRet.getRetCode() != 1) {
			ret.setRetCode("99");
			ret.setRetMsg(exchangeRet.getRetMsg());
			return;
		}

		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.EXCHANGE_SELL.getKey(),exchangeRet.getTxHash());
				
		final CWVMarketExchange exchange = new CWVMarketExchange();
		// 设置合约交易信息
//		exchange.setChainContract("");
		exchange.setChainStatus(ChainTransStatusEnum.START.getKey());
		exchange.setChainTransHash(exchangeRet.getTxHash());

		exchange.setPropertyId(Integer.parseInt(pb.getPropetyId()));
		exchange.setSellPrice(new BigDecimal(pb.getPrice()));
		exchange.setTax(new BigDecimal(charge).setScale(0, RoundingMode.HALF_UP));
		exchange.setUserId(user.getUserId());
		exchange.setCreateUser(user.getUserId()); // 卖出
		exchange.setStatus((byte) 0);
		exchange.setCreateTime(new Date());
		exchange.setUpdateTime(new Date());

		// 房产
		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = dao.gameMapDao.selectByPrimaryKey(map);
		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = dao.gameCityDao.selectByPrimaryKey(city);

		exchange.setCountryId(city.getGameCountryId());
		exchange.setMapId(map.getMapId());
		exchange.setPropertyName(property.getPropertyName());
		exchange.setPropertyStatus(property.getPropertyStatus());
		exchange.setPropertyTemplate(property.getPropertyTemplate());
		exchange.setPropertyTemplateId(property.getPropertyTemplateId());
		exchange.setPropertyType(property.getPropertyType());
		exchange.setNickName(user.getNickName());
		exchange.setIncome(property.getIncome());
		exchange.setIncomeRemark("收益说明123");
		exchange.setLastPrice(property.getLastPrice());
		// exchange.setImageUrl(property.getImageUrl());
		exchange.setChainStatus(ChainTransStatusEnum.START.getKey());
		exchange.setChainContract("");
		exchange.setChainTransHash(exchangeRet.getTxHash());
		
		//更新房产
		Property.Builder propertyBuilder = Property.newBuilder();
		property.setChainStatus(ChainTransStatusEnum.START.getKey());
		property.setChainTransHash(exchangeRet.getTxHash());
		dao.exchangeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				
				dao.exchangeDao.insert(exchange);
				
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);
				return null;
			}
		});
		
		propertyCopy(propertyBuilder, property);
		ret.setProperty(propertyBuilder);
		ExchangeInfo.Builder exchangeInfo = ExchangeInfo.newBuilder();
		CWVMarketExchangeExample exchangeExample = new CWVMarketExchangeExample();
		exchangeExample.createCriteria().andPropertyIdEqualTo(exchange.getPropertyId())
				.andStatusEqualTo(PropertyExchangeStatusEnum.ONSALE.getValue()).andUserIdEqualTo(exchange.getUserId());
		CWVMarketExchange exchangeNew = (CWVMarketExchange) dao.exchangeDao.selectOneByExample(exchangeExample);
		exchangeInfoSet(exchangeInfo, exchangeNew);
		ret.setExchange(exchangeInfo);
		// 设置返回
		ret.setRetCode(ReturnCodeMsgEnum.SPS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SPS_SUCCESS.getRetMsg());

	}

	/**
	 * 获取竞拍房产列表
	 * 
	 * @param pb
	 * @param ret
	 */
	public void getPropertyBid(FramePacket pack, PSPropertyBid pb, PRetPropertyBid.Builder ret) {
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		if (StringUtils.isNotBlank(pb.getBidId())) {
			setBidSingleRet(pb, ret, pack);
		} else
			setBidRet(pb, ret, page, pack);
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PBS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PBS_SUCCESS.getRetMsg());

	}

	private void bidInfoSet(BidInfo.Builder bidRet, CWVMarketBid bid) {

		bidRet.setBidId(bid.getBidId() + "");
		bidRet.setAuctionStart(DateUtil.getDayTime(bid.getAuctionStart()));
		bidRet.setAuctionEnd(DateUtil.getDayTime(bid.getAuctionEnd()));
		// 查询竞价最高者
		CWVMarketAuction auctionMax = getMaxAuction(bid.getBidId());
		bidRet.setPrice(bid.getLastPrice() + "");
		if (auctionMax != null) {
			CWVAuthUser userMax = userHelper.getUserById(auctionMax.getUserId());
			bidRet.setMaxPriceUser(userMax.getNickName());
			bidRet.setPrice(auctionMax.getBidPrice() + "");
		} else {
			bidRet.setPrice("0");
		}

		bidRet.setStatus(bid.getStatus() + "");
	}

	private void bidInfoSetToExchange(ExchangeInfo.Builder bidRet, CWVMarketBid bid) {

		bidRet.setBidId(bid.getBidId() + "");
		bidRet.setAuctionStart(DateUtil.getDayTime(bid.getAuctionStart()));
		bidRet.setAuctionEnd(DateUtil.getDayTime(bid.getAuctionEnd()));
		// 查询竞价最高者
		CWVMarketAuction auctionMax = getMaxAuction(bid.getBidId());
		bidRet.setPrice(bid.getLastPrice() + "");
		if (auctionMax != null) {
			CWVAuthUser userMax = userHelper.getUserById(auctionMax.getUserId());
			bidRet.setMaxPriceUser(userMax.getNickName());
			bidRet.setPrice(auctionMax.getBidPrice() + "");
		} else {
			bidRet.setPrice("0");
		}

		bidRet.setStatus(bid.getStatus() + "");
	}

	/**
	 * 地区房产列表
	 * 
	 * @param pb
	 * @param ret
	 */
	public void gameMapProperty(PBGameProperty pb, PRetRefGameProperty.Builder ret) {
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");

		if (StringUtils.isEmpty(pb.getMapId())) {
			ret.setRetCode("80");
			ret.setRetMsg("地图ID不能为空");
			return;
		}

		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria = propertyExample.createCriteria();
		criteria.andIsDisplayEqualTo("1").andGameMapIdEqualTo(Integer.parseInt(pb.getMapId()));

		if (StringUtils.isNotBlank(pb.getPropertyName())) {
			criteria.andPropertyNameEqualTo(pb.getPropertyName());
		}

		if (StringUtils.isNotBlank(pb.getPropertyStatus())) {
			criteria.andPropertyStatusEqualTo(pb.getPropertyStatus());
		}

		if (StringUtils.isNotBlank(pb.getPropertyType())) {
			criteria.andPropertyTypeEqualTo(pb.getPropertyType());
		}

		if (StringUtils.isNotBlank(pb.getIsPage()) && "1".equals(pb.getIsPage())) {
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			propertyExample.setLimit(page.getLimit());
			propertyExample.setOffset(page.getOffset());

			ret.setTotalCount(dao.gamePropertyDao.countByExample(propertyExample) + "");
		}

		List<Object> properties = dao.gamePropertyDao.selectByExample(propertyExample);
		for (Object coun : properties) {
			CWVGameProperty gameProperty = (CWVGameProperty) coun;
			Property.Builder property = Property.newBuilder();
			propertyCopy(property, gameProperty);
			ret.addProperties(property);
		}

	}

	/**
	 * 获取个人竞价记录
	 * 
	 * @param pb
	 * @param ret
	 */
	public void getPropertyBidAuction(PSPropertyBidAuction pb, PRetPropertyBidAuction.Builder ret) {
		// 区分查询内容
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		CWVMarketAuctionExample example = new CWVMarketAuctionExample();

		CWVMarketAuctionExample.Criteria criteria = example.createCriteria();

		if (StringUtils.isNotEmpty(pb.getAuctionId())) {
			criteria.andAuctionIdEqualTo(Integer.parseInt(pb.getAuctionId()));
		} else {
			if (StringUtils.isNotEmpty(pb.getBidId())) {
				criteria.andBidIdEqualTo(Integer.parseInt(pb.getBidId()));
			}
		}

		List<Object> list = dao.gamePropertyDao.selectByExample(example);

		for (Object o : list) {
			CWVMarketAuction auctionCWV = (CWVMarketAuction) o;
			PropertyBidAuction.Builder auction = PropertyBidAuction.newBuilder();
			auction.setAmount(auctionCWV.getBidPrice() + "");
			auction.setAuctionId(auctionCWV.getAuctionId() + "");
			auction.setBidId(auctionCWV.getBidId() + "");
			auction.setBidPrice(auctionCWV.getBidPrice() + "");
			// auction.setNickName(auctionCWV.getNickName());

			ret.addAuction(auction);
		}
		ret.setRetCode(ReturnCodeMsgEnum.PBA_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.PBA_SUCCESS.getRetMsg());

	}

	/**
	 * 发起竞价
	 * 
	 * @param pb
	 * @param ret
	 */
	public void auctionProperty(FramePacket pack, final PSAuctionProperty pb, final Builder ret) {

		// 1校验
		// 1.1非空校验

		if (StringUtils.isEmpty(pb.getBidId())) {// 竞拍ID
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetMsg());
			return;
		}

		if (pb.getPrice() <= 0) {// 竞拍价
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_PRICE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_PRICE.getRetMsg());
			return;
		}

		// 1.2 数据校验
		// ID存在
		CWVMarketBid bidNew = new CWVMarketBid();
		bidNew.setBidId(Integer.parseInt(pb.getBidId()));
		final CWVMarketBid bid = dao.bidDao.selectByPrimaryKey(bidNew);
		if (bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetMsg());
			return;
		}

		if (bid.getStatus().intValue() == 0) {// 未开始
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_STATUS_0.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_STATUS_0.getRetMsg());
			return;
		} else if (bid.getStatus().intValue() == 2) { // 已结束
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_STATUS_2.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_STATUS_2.getRetMsg());
			return;
		} else if (bid.getStatus().intValue() == 1
				&& !bid.getChainStatus().equals(ChainTransStatusEnum.DONE.getKey())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_CHAIN_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.ERROR_CHAIN_STATUS.getRetMsg());
			return;
		} else if (bid.getAuctionEnd().compareTo(new Date()) < 0) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_TIME.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_TIME.getRetMsg());
			return;
		}

		// 价格 竞价最高价对比 查询钱包账户
		if (bid.getBidAmount().doubleValue() >= pb.getPrice()) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_PRICE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_PRICE.getRetMsg());
			return;
		}

		// 查询
		final CWVAuthUser user = userHelper.getCurrentUser(pack);
		CWVUserWallet cwbAccount = walletHelper.getUserAccount(user.getUserId(), CoinEnum.CWB);
		if (cwbAccount.getBalance().compareTo(new BigDecimal(pb.getPrice())) < 0) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetMsg());
			return;
		}

		// 价格 阶梯
		if ((pb.getPrice() - bid.getBidStart().doubleValue()) % bid.getIncreaseLadder().doubleValue() != 0) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_PRICE_LADDER.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_PRICE_LADDER.getRetMsg());
			return;
		}

		// 2扣除账户竞价资产

		// 2.1调取竞拍合约
		final RespCreateTransaction.Builder retContract = bidInvoker.auctionProperty(cwbAccount.getAccount(),
				bid.getChainContract(), pb.getPrice());

		if(retContract.getRetCode()!=1) {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode())
			.setRetMsg(retContract.getRetMsg());
			return;
		}
		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.BID_AUCTION.getKey(),retContract.getTxHash());
				
		// 2.2新增竞价记录，并更新竞价记录
		CWVMarketAuctionExample example = new CWVMarketAuctionExample();
		example.createCriteria().andBidIdEqualTo(Integer.parseInt(pb.getBidId())).andUserIdEqualTo(user.getUserId())// 当前用户
				.andStatusEqualTo((byte) 1)// 有效
				.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey());// 合约交易已完成
		example.setOrderByClause(" auction_id desc ");
		final CWVMarketAuction old = (CWVMarketAuction) dao.auctionDao.selectOneByExample(example);
		// 竞拍账户
		final CWVUserWallet auctionAccount = walletHelper.getUserAccount(user.getUserId(), CoinEnum.CWB);

		
		
		if (retContract.getRetCode() == 1) {
			dao.bidDao.doInTransaction(new TransactionExecutor() {

				@Override
				public Object doInTransaction() {

					CWVUserTransactionRecord recordAuction = new CWVUserTransactionRecord();
					// 设置交易记录
					recordAuction.setCreateTime(new Date());
					recordAuction.setCreateUser(user.getUserId());
					recordAuction.setDetail("竞拍房产");
					recordAuction.setUserId(user.getUserId());

					CWVMarketAuction newAuction = new CWVMarketAuction();
					newAuction.setBidId(bid.getBidId());
					newAuction.setCreateTime(new Date());
					newAuction.setStatus((byte) 1);
					newAuction.setUserId(user.getUserId());
					newAuction.setChainStatus(ChainTransStatusEnum.START.getKey());
					newAuction.setChainContract(bid.getChainContract());
					newAuction.setChainTransHash(retContract.getTxHash());
					// 更新个人竞拍记录
					if (old != null) {// 存在历史竞价
						
						
						// 账户金额
						BigDecimal gainCost = new BigDecimal(pb.getPrice()).subtract(old.getBidPrice());
						BigDecimal balance = auctionAccount.getBalance().subtract(gainCost);
						if(balance.longValue()<0) {
							ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetCode())
							.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetMsg());
							return null;
						}
						auctionAccount.setBalance(balance);
						// 交易记录
						recordAuction.setGainCost(gainCost.negate());
						newAuction.setLastBidPrice(old.getBidPrice());
						newAuction.setBidPrice(new BigDecimal(pb.getPrice()));
						// 更新参与人数数
					} else {
						
						
						newAuction.setBidPrice(new BigDecimal(pb.getPrice()));
						BigDecimal gainCost = newAuction.getBidPrice();
						BigDecimal balance = auctionAccount.getBalance().subtract(gainCost);
						if(balance.longValue()<0) {
							ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetCode())
							.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetMsg());
							return null;
						}
						newAuction.setLastBidPrice(new BigDecimal(0));
						// 账户金额 ：查询余额时会同步链上余额，此处不做业务处理
//						auctionAccount.setBalance(balance);
						// 交易记录
						recordAuction.setGainCost(gainCost.negate());
						// 更新参与人数
						bid.setBiddersCount(bid.getBiddersCount() + 1);
					}

					dao.auctionDao.insertSelective(newAuction);
					// 3更新竞拍信息 最高价 拥有者 更新时间

					bid.setBidAmount(new BigDecimal(pb.getPrice()));
					bid.setOwner(user.getUserId());
					bid.setLastUpdateTime(new Date());
					dao.bidDao.updateByPrimaryKeySelective(bid);
					// 更新账户金额
//					auctionAccount.setUpdateTime(new Date());
//					dao.walletDao.updateByPrimaryKeySelective(auctionAccount);
					// 插入钱包操作记录
					recordAuction.setMarketId(bid.getBidId());
					recordAuction.setType(MarketTypeEnum.BID.getValue());
					dao.userTransactionRecordDao.insert(recordAuction);
					return null;
				}
			});

		} else {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode()).setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			return;
		}
		BidInfo.Builder bidRet = BidInfo.newBuilder();
		bidRet.setMaxPriceUser(user.getNickName());
		bidRet.setUserPrice(bid.getBidAmount() + "");
		bidRet.setPrice(bid.getBidAmount() + "");

		// 设置房产信息
		// Property.Builder property = Property.newBuilder();
		// propertyCopy(property, gameProperty);

		// 设置交易信息
		// 设置本人出价
		CWVMarketAuction auctionUser = getUserAuction(bid.getBidId(), user.getUserId());
		bidRet.setUserPrice(auctionUser == null ? "0" : auctionUser.getBidPrice() + "");
		ret.setBid(bidRet);
		// 4设置竞价成功
		ret.setRetCode(ReturnCodeMsgEnum.APS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.APS_SUCCESS.getRetMsg());

	}
	

	/**
	 * 抽奖房产
	 * 
	 * @param pb
	 * @param ret
	 */
	public void drawProperty(FramePacket pack, PSCommonDraw pb, PRetPropertyDraw.Builder ret) {
		// 校验
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		// 查询抽奖机会
		CWVUserWallet walletCWB = walletHelper.getUserAccount(authUser.getUserId(), CoinEnum.CWB);
		if (walletCWB.getDrawCount() < 1) {
			ret.setRetCode(ReturnCodeMsgEnum.PDS_ERROR_DRAW_COUNT.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PDS_ERROR_DRAW_COUNT.getRetMsg());
			return;
		}
		// 抽奖
		// 调取抽奖合约
		final CWVUserWallet wallet = walletHelper.getUserAccount(authUser.getUserId(), CoinEnum.CWB);
//		RetDraw retDraw = drawInvoker.drawProperty(wallet.getAccount());
//		if (!retDraw.getRetCode().equals(ReturnCodeMsgEnum.SUCCESS.getRetCode())) {
//			ret.setRetCode(retDraw.getRetCode());
//			ret.setRetMsg(retDraw.getRetMsg());
//			return;
//		}

		// 更新房产信息
		final CWVGameProperty gameProperty = getDrawProperty();
		if(gameProperty == null ){
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg("暂无房产可抽奖");
			return;
		}
		
		final CWVMarketDraw draw = new CWVMarketDraw();
		draw.setUserId(authUser.getUserId());
		draw.setUserAddress(wallet.getAccount());;
		draw.setCreateTime(new Date());
		draw.setPropertyId(gameProperty.getPropertyId());
		draw.setPropertyToken(gameProperty.getCryptoToken());
		//transfer property 
		RespCreateTransaction.Builder retTrans = drawInvoker.propertyTransfer(draw);
		
		if(retTrans.getRetCode() != 1) {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode());
			ret.setRetMsg(retTrans.getRetMsg());
			return ;
		}
		
		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.DRAW_GROUP.getKey(),retTrans.getTxHash());
				
		draw.setChainTransHash(retTrans.getTxHash());
		draw.setChainStatus(ChainTransStatusEnum.START.getKey());
		
		// 无token字段，临时使用
		dao.getDrawDao().insert(draw);
				
//		wltHelper.excuteContract("", "", "");
		//设置抽奖合约返回ID
		gameProperty.setPropertyId(1);
		gameProperty.setUserId(authUser.getUserId());
		gameProperty.setPropertyStatus("0");
		gameProperty.setLastPrice(new BigDecimal("1000"));
		gameProperty.setChainStatus(ChainTransStatusEnum.START.getKey());
		gameProperty.setChainTransHash(retTrans.getTxHash());
		
		// 更新抽奖次数
		wallet.setDrawCount(wallet.getDrawCount() - 1);
		
		dao.walletDao.updateByPrimaryKeySelective(wallet);

		Property.Builder property = Property.newBuilder();
		propertyCopy(property, gameProperty);
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg())
		.setProperty(property)		;

	}

	/**
	 * 查询抽奖记录
	 * 
	 * @param pb
	 * @param ret
	 */
	public void getPropertyDrawRecord(PSPropertyDrawRecord pb, PRetPropertyDrawRecord.Builder ret) {

		CWVMarketDrawExample example = new CWVMarketDrawExample();
		example.createCriteria().andUserIdEqualTo(1);//
		List<Object> list = dao.drawDao.selectByExample(example);
		for (Object o : list) {
			CWVMarketDraw draw = (CWVMarketDraw) o;
			PropertyDraw.Builder drawRet = PropertyDraw.newBuilder();
			drawRet.setDrawId(draw.getDrawId() + "");
			drawRet.setDrawTime(DateUtil.getDayTime(draw.getCreateTime()));
			drawRet.setPropertyId(draw.getPropertyId() + "");
		}

	}

	/**
	 * 抽奖房产
	 * 
	 * @param pb
	 * @param ret
	 */
	public void drawPropertyBack(FramePacket pack, PSCommonDraw pb, PRetPropertyDraw.Builder ret) {
		// 校验
		final CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		// 查询抽奖机会
		final CWVUserWallet walletCWB = walletHelper.getUserAccount(authUser.getUserId(), CoinEnum.CWB);
		if (walletCWB.getDrawCount() < 1) {
			ret.setRetCode(ReturnCodeMsgEnum.PDS_ERROR_DRAW_COUNT.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PDS_ERROR_DRAW_COUNT.getRetMsg());
			return;
		}
		// 抽奖
		// 调取抽奖合约
		String superUserId = commonHelper.getSysSettingValue("super_user");
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andUserIdEqualTo(Integer.parseInt(superUserId)).andGameMapIdIsNotNull()
				.andPropertyTypeEqualTo("2").addCriterion(
						"property_id not in ( select property_id from cwv_market_draw where chain_status != '-1' or chain_status is null and property_id is not null)");

		int count = dao.gamePropertyDao.countByExample(example);
		if (count == 0) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("暂无房产可抽奖");
			return;
		}

		RespCreateTransaction.Builder retStr = drawInvoker.drawProperty(walletCWB.getAccount(), count);

		if (retStr.getRetCode() != 1) {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode()).setRetMsg(retStr.getRetMsg());
			return;
		}

		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.DRAW_RANDOM.getKey(),retStr.getTxHash());
				
		// 更新抽奖次数
		walletCWB.setDrawCount(walletCWB.getDrawCount() - 1);

		//定时任务处理
		
		CWVUserWallet wallet = walletHelper.getUserAccount(authUser.getUserId(), CoinEnum.CWB);

		final CWVMarketDraw draw = new CWVMarketDraw();
		draw.setUserId(authUser.getUserId());
		draw.setUserAddress(wallet.getAccount());;
		draw.setCreateTime(new Date());
		draw.setChainTransHashRandom(retStr.getTxHash());
		draw.setChainStatusRandom(ChainTransStatusEnum.START.getKey());
		// 无token字段，临时使用
		dao.getDrawDao().insert(draw);

		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg())
		.setTxHash(retStr.getTxHash());

	}

	private void propertyCopy(Property.Builder property, CWVGameProperty gameProperty) {
		try {
			CWVGameMap map = new CWVGameMap();
			map.setMapId(gameProperty.getGameMapId());
			map = dao.gameMapDao.selectByPrimaryKey(map);
			if (map != null) {
				CWVGameCity city = new CWVGameCity();
				city.setCityId(map.getGameCityId());
				city = dao.gameCityDao.selectByPrimaryKey(city);
				property.setCountryId(city.getGameCountryId() + "");
				property.setMapId(map.getMapId() + "");
				property.setMapTemplate(map.getTemplate() + "");
			}

			property.setPropertyTemplateId(gameProperty.getPropertyTemplateId());
			property.setPropertyTemplate(gameProperty.getPropertyTemplate());
			if (gameProperty.getUserId() != null) {
				CWVAuthUser user = userHelper.getUserById(gameProperty.getUserId());
				property.setOwner(user.getNickName());
			}
			property.setPropertyId(gameProperty.getPropertyId() + "");
			property.setPropertyName(gameProperty.getPropertyName());
			property.setPropertyType(Integer.parseInt(gameProperty.getPropertyType()));

			property.setChainStatus(gameProperty.getChainStatus()+"");
			property.setPropertyStatus(Integer.parseInt(gameProperty.getPropertyStatus()));
			property.setIncomeRemark("收益说明");
			property.setIncome(gameProperty.getIncome().doubleValue());
			property.setImageUrl(gameProperty.getImageUrl());
			property.setPrice(gameProperty.getLastPrice() + "");
			property.setLongitude(gameProperty.getLongitude() + "");
			property.setLatitude(gameProperty.getLatitude() + "");
			property.setIsDisplay(Integer.parseInt(gameProperty.getIsDisplay()));
			
			// 设置priceLine
			StringBuffer exchangePrice = new StringBuffer();
			//普通房产抽奖
			if(PropertyTypeEnum.ORDINARY.getValue().equals(""+property.getPropertyType())) {
				
				exchangePrice.append("1000");
			}else{
				CWVMarketBidExample bidExample = new CWVMarketBidExample();
				bidExample.createCriteria().andGamePropertyIdEqualTo(gameProperty.getPropertyId());
				
				Object bidO = dao.bidDao.selectOneByExample(bidExample);
				
				if(bidO != null)
					exchangePrice.append(((CWVMarketBid) bidO).getBidAmount()).append(",");
			}
			CWVMarketExchangeExample exchangeExample2 = new CWVMarketExchangeExample();
			exchangeExample2.createCriteria().andPropertyIdEqualTo(gameProperty.getPropertyId())
					.andStatusEqualTo(PropertyExchangeStatusEnum.SOLD.getValue())
					.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey());
				
			exchangeExample2.setLimit(9);
			List<Object> listExchange2 = dao.exchangeDao.selectByExample(exchangeExample2);
			
			if (listExchange2 != null && !listExchange2.isEmpty()) {
				for (int i = 0; i < listExchange2.size(); i++) {
					CWVMarketExchange echange2 = (CWVMarketExchange) listExchange2.get(i);
					
					exchangePrice.append(",").append(echange2.getSellPrice());
				}
			}
			property.setPriceLine(exchangePrice.toString());

			
		} catch (Exception e) {
			log.error("propertyCopy exception:", e);
		}
	}

	private CWVGameProperty getDrawProperty() {
		String superUserId = commonHelper.getSysSettingValue("super_user");

		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andUserIdEqualTo(Integer.parseInt(superUserId)).andGameMapIdIsNotNull()
				.andPropertyTypeEqualTo(PropertyTypeEnum.ORDINARY.getValue())
				.andCryptoTokenIsNotNull()
				.addCriterion("property_id not in ( select property_id from cwv_market_draw where chain_status='0' )");

		int count = dao.gamePropertyDao.countByExample(example);
		int offset = (int) (Math.random() * count);
		example.setOffset(offset);
		Object o = dao.gamePropertyDao.selectOneByExample(example);
		return (CWVGameProperty) o;
	}

	public void bidDetail(PSCommonBid pb, PRetBidPropertyDetail.Builder ret) {
		// 校验
		if (StringUtils.isEmpty(pb.getBidId())) {
			ret.setRetCode(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetMsg());
			return;
		}

		// 查询
		CWVMarketBid bid = new CWVMarketBid();
		bid.setBidId(Integer.parseInt(pb.getBidId()));
		bid = dao.bidDao.selectByPrimaryKey(bid);
		if (bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetMsg());
			return;
		} else {
			if (!(bid.getStatus().intValue() == 1)) {
				ret.setRetCode(ReturnCodeMsgEnum.PBD_ERROR_STATUS.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.PBD_ERROR_STATUS.getRetMsg());
				return;
			}
		}

		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(bid.getGamePropertyId());
		property = dao.gamePropertyDao.selectByPrimaryKey(property);

		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = dao.gameMapDao.selectByPrimaryKey(map);

		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = dao.gameCityDao.selectByPrimaryKey(city);

		CWVAuthUser user = userHelper.getUserById(property.getUserId());
		// 设置返回信息

		// string ret_code = 21; //返回状态码
		// string ret_msg = 22; //提示信息
		// string country_id = 1;//所属国家
		// string map_id = 2;//所属地图
		// string property_template_id = 3;//房产模板Id
		// string property_template = 4;//房产模板
		// //string owner = 5;//拥有者
		// string property_name = 6; //房产名称
		// string property_id = 7;//房产编码
		// int32 property_type = 8;//房产类型
		// int32 property_status = 9;//房产状态
		// string income_remark = 10;//房产说明

		Property.Builder bidProperty = Property.newBuilder();
		bidProperty.setCountryId(city.getGameCountryId() + "");
		bidProperty.setIncomeRemark("收益说明");
		bidProperty.setMapId(map.getMapId() + "");
		bidProperty.setMapTemplate(map.getTemplate() + "");
		if (user != null)
			bidProperty.setOwner(user.getNickName() + "");
		bidProperty.setPropertyId(property.getPropertyId() + "");
		bidProperty.setPropertyName(property.getPropertyName());
		bidProperty.setPropertyStatus(Integer.parseInt(property.getPropertyStatus()));
		bidProperty.setPropertyTemplate(property.getPropertyTemplate());
		bidProperty.setPropertyTemplateId(property.getPropertyTemplateId());
		bidProperty.setPropertyType(Integer.parseInt(property.getPropertyType()));
		bidProperty.setImageUrl(property.getImageUrl());

		// string max_price = 12;//房产编码
		// string bidders_count = 13;//当前参与人数
		// string auction_start = 14;//开始时间
		// string auction_end = 15;//结束时间
		// string announce_time = 16;//公布时间
		// string bid_start = 17;//最低喊价
		ret.setProperty(bidProperty);
		ret.setMaxPrice(bid.getBidAmount().toString());
		ret.setBiddersCount(bid.getBiddersCount() + "");
		ret.setAuctionStart(DateUtil.getDayTime(bid.getAuctionStart()));
		ret.setAuctionEnd(DateUtil.getDayTime(bid.getAuctionEnd()));
		ret.setAnnounceTime(DateUtil.getDayTime(bid.getAnnounceTime()));
		// 查询竞价最高者
		CWVMarketAuction auctionMax = getMaxAuction(bid.getBidId());
		if (auctionMax != null) {
			CWVAuthUser userMax = userHelper.getUserById(auctionMax.getUserId());
			ret.setMaxPriceUser(userMax.getNickName());
			ret.setMaxPrice(auctionMax.getBidPrice() + "");
		} else {
			ret.setMaxPrice("0");
		}
		// 查询竞价最高者
		CWVMarketAuction auctionUser = getUserAuction(bid.getBidId(), user.getUserId());
		ret.setUserPrice(auctionUser == null ? "0" : auctionUser.getBidPrice() + "");

		ret.setBidStart(bid.getBidStart().toString());
		ret.setRetCode(ReturnCodeMsgEnum.PBD_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.PBD_SUCCESS.getRetMsg());

	}

	public CWVMarketAuction getMaxAuction(Integer bidId) {
		CWVMarketAuctionExample auctionExample = new CWVMarketAuctionExample();
		auctionExample.createCriteria().andBidIdEqualTo(bidId);
		auctionExample.setOrderByClause("bid_price desc");
		Object o = dao.auctionDao.selectOneByExample(auctionExample);

		return o == null ? null : (CWVMarketAuction) o;
	}

	public CWVMarketAuction getUserAuction(Integer bidId, Integer userId) {
		CWVMarketAuctionExample auctionExample = new CWVMarketAuctionExample();
		auctionExample.createCriteria().andBidIdEqualTo(bidId).andUserIdEqualTo(userId);
		Object o = dao.auctionDao.selectOneByExample(auctionExample);
		return o == null ? null : (CWVMarketAuction) o;
	}

	public void getPropertyCharge(PSCommon pb, PRetGamePropertyCharge.Builder ret) {
		// TODO 是否查询链以及配置化

		ret.setChargeRate(Double.parseDouble(PropertyJobHandle.EXCHANGE_CHARGE));
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
	}

	public void bidNotice(PSCommonBid pb, PRetBidPropertyNotice.Builder ret) {
		// 校验
		if (StringUtils.isEmpty(pb.getBidId())) {
			ret.setRetCode(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetMsg());
			return;
		}

		// 查询
		CWVMarketBid bid = new CWVMarketBid();
		bid.setBidId(Integer.parseInt(pb.getBidId()));
		bid = dao.bidDao.selectByPrimaryKey(bid);
		if (bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetMsg());
			return;
		} else {
			if (!(bid.getStatus().intValue() == 2)) {
				ret.setRetCode(ReturnCodeMsgEnum.PBN_ERROR_STATUS.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.PBN_ERROR_STATUS.getRetMsg());
				return;
			}
		}

		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(bid.getGamePropertyId());
		property = dao.gamePropertyDao.selectByPrimaryKey(property);

		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = dao.gameMapDao.selectByPrimaryKey(map);

		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = dao.gameCityDao.selectByPrimaryKey(city);

		CWVAuthUser user = userHelper.getUserById(property.getUserId());

		CWVMarketAuctionExample auctionExample = new CWVMarketAuctionExample();
		auctionExample.createCriteria().andBidIdEqualTo(bid.getBidId());
		auctionExample.setOrderByClause(" bid_price desc ");
		List<Object> listAuction = dao.auctionDao.selectByExample(auctionExample);
		if (listAuction != null && !listAuction.isEmpty()) {
			for (Object object : listAuction) {
				CWVMarketAuction auction = (CWVMarketAuction) object;
				CWVAuthUser authUser = userHelper.getUserById(auction.getUserId());
				AuctionRank.Builder rank = AuctionRank.newBuilder();
				rank.setBidAmount(auction.getBidPrice().toString());
				rank.setNickName(authUser.getNickName());
				ret.addAuctionRank(rank);
			}
		}
		// 设置返回信息

		// string ret_code = 21; //返回状态码
		// string ret_msg = 22; //提示信息
		// string country_id = 1;//所属国家
		// string map_id = 2;//所属地图
		// string property_template_id = 3;//房产模板Id
		// string property_template = 4;//房产模板
		// string owner = 5;//拥有者
		// string property_name = 6; //房产名称
		// string property_id = 7;//房产编码
		// string income_remark = 10;//房产说明

		Property.Builder bidProperty = Property.newBuilder();
		bidProperty.setCountryId(city.getGameCountryId() + "");
		bidProperty.setMapId(map.getMapId() + "");
		bidProperty.setMapTemplate(map.getTemplate() + "");
		bidProperty.setPropertyTemplate(property.getPropertyTemplate());
		bidProperty.setPropertyTemplateId(property.getPropertyTemplateId());
		if (user != null)
			bidProperty.setOwner(user.getNickName());
		bidProperty.setPropertyName(property.getPropertyName());
		bidProperty.setPropertyId(property.getPropertyId() + "");
		bidProperty.setIncomeRemark("收益说明");
		bidProperty.setImageUrl(property.getImageUrl());
		// string max_price = 12;//房产编码
		// string bidders_count = 13;//当前参与人数
		// string auction_start = 14;//开始时间
		// string auction_end = 15;//结束时间
		// string announce_time = 16;//公布时间
		// string bid_start = 17;//最低喊价
		// 查询竞价最高者
		CWVAuthUser owner = userHelper.getUserById(bid.getOwner());
		ret.setMaxPriceUser(owner.getNickName());
		// 查询竞价最高者
		CWVMarketAuction auctionUser = getUserAuction(bid.getBidId(), user.getUserId());
		ret.setUserPrice(auctionUser == null ? "0" : auctionUser.getBidPrice() + "");

		ret.setProperty(bidProperty);
		ret.setBidPrice(bid.getBidAmount().doubleValue());

		ret.setRetCode(ReturnCodeMsgEnum.PBD_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.PBD_SUCCESS.getRetMsg());

	}

	/**
	 * 撤销发起的竞拍
	 * 
	 * @param pack
	 * @param id
	 * @param ret
	 */
	public void cancelPropertyBid(FramePacket pack, String id, PRetCommon.Builder ret) {
		//
		if (StringUtils.isEmpty(id)) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetMsg());
			return;
		}

		CWVMarketBid marketBid = new CWVMarketBid();
		marketBid.setBidId(Integer.parseInt(id));
		final CWVMarketBid bid = dao.getBidDao().selectByPrimaryKey(marketBid);
		if (bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.CPC_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPC_ERROR_ID.getRetMsg());
			return;
		}

		if (!bid.getStatus().equals(PropertyBidStatusEnum.PREVIEW.getValue())) {
			ret.setRetCode(ReturnCodeMsgEnum.CPC_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPC_ERROR_STATUS.getRetMsg());
			return;
		}

		String superUserId = commonHelper.getSysSettingValue("super_user");

		if (!bid.getCreateUser().equals(superUserId)) {
			ret.setRetCode(ReturnCodeMsgEnum.CPC_ERROR_PROPERTY.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPC_ERROR_PROPERTY.getRetMsg());
			return;
		}

		CWVAuthUser user = userHelper.getCurrentUser(pack);
		if (user.getUserId().intValue() != Integer.parseInt(superUserId)) {
			ret.setRetCode(ReturnCodeMsgEnum.CPC_ERROR_USER.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPC_ERROR_USER.getRetMsg());
			return;
		}

		// 调取撤销合约
		RespCreateTransaction.Builder cancelBidRet = bidInvoker.cancelBid(bid);
		if(cancelBidRet.getRetCode()!=1) {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode())
			.setRetMsg(cancelBidRet.getRetMsg());
			return;
		}
		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.BID_CREATE_CANCEL.getKey(),cancelBidRet.getTxHash());
				
		bid.setStatus(PropertyBidStatusEnum.CANCEL.getValue());
		// 设置合约交易信息
		bid.setChainStatus(ChainTransStatusEnum.START.getKey());
		bid.setChainContract("");
		bid.setChainTransHash(cancelBidRet.getTxHash());

		dao.getBidDao().updateByPrimaryKeySelective(bid);

	}

	/**
	 * 创建竞拍
	 * @param pack
	 * @param pb
	 * @param ret
	 * @throws ParseException
	 */
	public void createPropertyBid(FramePacket pack, PSCreatePropertyBid pb, PRetCommon.Builder ret)
			throws ParseException {
		if (StringUtils.isEmpty(pb.getPropertyId())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("房产ID不能为空");
			return;
		}

		if (StringUtils.isEmpty(pb.getIncreaseLadder())) {

			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("竞价必须是最小单位竞价的倍数");
			return;
		}

		if (pb.getBidStart() <= 0) {

			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("竞拍起价不能为空");
			return;
		}

		if (StringUtils.isEmpty(pb.getAuctionStart())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("竞拍起期不能为空");
			return;
		}

		if (StringUtils.isEmpty(pb.getAuctionEnd())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("竞拍止期不能为空");
			return;
		} else if (org.brewchain.cwv.auth.util.DateUtil.compare(DateUtil.getDateTime(pb.getAuctionEnd()),
				new Date()) <= 0) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("竞拍止期必须大于当前时间");
			return;
		}

		if (org.brewchain.cwv.auth.util.DateUtil.compare(DateUtil.getDateTime(pb.getAuctionEnd()),
				DateUtil.getDateTime(pb.getAuctionStart())) <= 0) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("竞拍止期必须大于竞拍起期");
			return;
		}

		final CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(Integer.parseInt(pb.getPropertyId()));
		final CWVGameProperty property = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);

		if (property == null) {
			ret.setRetCode(ReturnCodeMsgEnum.CPB_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPB_ERROR_ID.getRetMsg());
			return;
		}
		if (!property.getPropertyStatus().equals(PropertyStatusEnum.NOSALE.getValue())) {
			ret.setRetCode(ReturnCodeMsgEnum.CPB_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPB_ERROR_STATUS.getRetMsg());
			return;
		}

		String superUserId = commonHelper.getSysSettingValue("super_user");

		if (property.getUserId() != Integer.parseInt(superUserId)) {
			ret.setRetCode(ReturnCodeMsgEnum.CPB_ERROR_PROPERTY.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPB_ERROR_PROPERTY.getRetMsg());
			return;
		}
		CWVAuthUser user = userHelper.getCurrentUser(pack);
		if (user.getUserId().intValue() != Integer.parseInt(superUserId)) {
			ret.setRetCode(ReturnCodeMsgEnum.CPB_ERROR_USER.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPB_ERROR_USER.getRetMsg());
			return;
		}

		// 交易密码
		CWVUserTradePwd userTradePwd = userHelper.getTradePwd(user.getUserId());
		if (userTradePwd == null) {
			ret.setRetCode(ReturnCodeMsgEnum.CPB_VALIDATE_PWD_SET.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPB_VALIDATE_PWD_SET.getRetMsg());
			return;
		} else {
			if (!userTradePwd.getTradePassword().equals(userHelper.getPwdMd5(pb.getTradePwd(), user.getSalt()))) {
				ret.setRetCode(ReturnCodeMsgEnum.CPB_ERROR_TRADE_PWD.getRetCode())
						.setRetMsg(ReturnCodeMsgEnum.CPB_ERROR_TRADE_PWD.getRetMsg());
				return;
			}
		}

		final CWVMarketBid bid = new CWVMarketBid();

		bid.setGamePropertyId(Integer.parseInt(pb.getPropertyId()));
		bid.setAuctionEnd(DateUtil.getDateTime(pb.getAuctionEnd()));
		bid.setAuctionStart(DateUtil.getDateTime(pb.getAuctionStart()));
		bid.setBidStart(new BigDecimal(pb.getBidStart()));
		bid.setIncreaseLadder(Long.parseLong(pb.getIncreaseLadder()));
		if (StringUtils.isEmpty(pb.getAnnounceTime())) {
			bid.setAnnounceTime(DateUtil.addMinute(bid.getAuctionEnd(), 60));
		} else {
			bid.setAnnounceTime(DateUtil.addMinute(bid.getAuctionEnd(), Integer.parseInt(pb.getAnnounceTime())));
		}

		bid.setBidAmount(new BigDecimal(0));
		bid.setBiddersCount(0);
		// 生成交易
		// 调取卖出房产合约
		CWVUserWallet wallet = walletHelper.getUserAccount(user.getUserId(), CoinEnum.CWB);
		
		RespCreateContractTransaction.Builder exchangeRet = bidInvoker.createBid(bid, wallet.getAccount(), property.getCryptoToken());
		if(exchangeRet.getRetCode()!=1) {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode())
			.setRetMsg(exchangeRet.getRetMsg());
			return;
		}
		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.BID_CREATE.getKey(),exchangeRet.getTxHash());
		
		// 添加调取合约日志 TODO

		if (exchangeRet.getRetCode() != 1) {
			ret.setRetCode("99");
			ret.setRetMsg(exchangeRet.getRetMsg());
			return;
		}
		if (org.brewchain.cwv.auth.util.DateUtil.compare(bid.getAuctionStart(), new Date()) > 0)
			bid.setStatus((byte) 0);
		else
			bid.setStatus((byte) 1);

		bid.setCreateTime(new Date());
		bid.setCreateUser(user.getUserId() + "");
		// 设置合约交易信息
		bid.setChainContract(exchangeRet.getContractAddress());
		bid.setChainStatus(ChainTransStatusEnum.START.getKey());
		bid.setChainTransHash(exchangeRet.getTxHash());

		// 房产
		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = dao.gameMapDao.selectByPrimaryKey(map);
		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = dao.gameCityDao.selectByPrimaryKey(city);

		bid.setCountryId(city.getGameCountryId());
		bid.setMapId(map.getMapId());
		bid.setPropertyName(property.getPropertyName());
		bid.setPropertyStatus(property.getPropertyStatus());
		bid.setPropertyTemplate(property.getPropertyTemplate());
		bid.setPropertyTemplateId(property.getPropertyTemplateId());
		bid.setPropertyType(property.getPropertyType());
		bid.setNickName(user.getNickName());
		bid.setIncomeRemark("收益说明123");
		bid.setLastPrice(property.getLastPrice());
		bid.setImageUrl(property.getImageUrl());

		dao.bidDao.insert(bid);

		// 设置返回
		ret.setRetCode(ReturnCodeMsgEnum.CPB_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.CPB_SUCCESS.getRetMsg());

	}

	public void propertyIncome(FramePacket pack, PSPropertyIncome pb, PRetPropertyIncome.Builder ret) {

		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		CWVUserWallet userWallet = walletHelper.getUserAccount(authUser.getUserId(), CoinEnum.CWB);
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		CWVUserPropertyIncomeExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(authUser.getUserId()).andMasterEqualTo(1);
		// criteria.andStatusEqualTo((byte) 0);// 新建收益
		criteria.andPropertyIdIsNull();// property_id为null为统计数据
		criteria.andChainStatusEqualTo(ChainTransStatusEnum.DONE.getKey());
		example.setOrderByClause(" income_id desc ");
		if (StringUtils.isEmpty(pb.getType())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			ret.setRetMsg("类型不能为空");
			return;
		}

		criteria.andTypeEqualTo(Byte.parseByte(pb.getType()));
		
		// 根据类型查询房产
		List<Object> list = dao.incomeDao.selectByExample(example);
		// 未领取收益

		// for (Object o : list) {
		// CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
		// incomeUnclaim = incomeUnclaim + income.getAmount().doubleValue();
		// }
		//
		// 房产信息
		PropertyInfo.Builder propertyInfo = PropertyInfo.newBuilder();
		// propertyInfo.setSubTypeInfo(index, value)
		propertyInfo.setDescription("");
		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria propertyCriteria = propertyExample.createCriteria();
		propertyCriteria.andUserIdEqualTo(authUser.getUserId()).andPropertyTypeEqualTo(pb.getType());
		Integer count = dao.gamePropertyDao.countByExample(propertyExample);

		propertyInfo.setTotalValue(count.toString());// 房产总价值

		PropertyIncome.Builder income = PropertyIncome.newBuilder();
		if (list != null && !list.isEmpty()) {
			CWVUserPropertyIncome propertyIncome = (CWVUserPropertyIncome) list.get(0);
			income.setAmount(propertyIncome.getAmount().toString());
			income.setIncomeId(propertyIncome.getIncomeId() + "");
			income.setStatus(propertyIncome.getStatus() + "");
		}

		income.setCoinType(CoinEnum.CWB.getValue() + "");

		String time = commonHelper.getSysSettingValue(CommonHelper.INCOMETIME);
		ret.setNextIncomeTime(time);// 派息日期
		switch (pb.getType()) {
		case "2":
			ret.setIncomeTotal(userWallet.getIncomeOrdinary() + "");
			break;
		case "1":
			ret.setIncomeTotal(userWallet.getIncomeTypical() + "");
			break;
		case "3":
			ret.setIncomeTotal(userWallet.getIncomeFunctional() + "");
			break;
		default:
			break;
		}
		ret.setIncome(income);
		ret.setPropertyInfo(propertyInfo);
		ret.setPropertyType(pb.getType());
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());

	}

	public void propertyIncomeClaim(FramePacket pack, PSPropertyIncomeClaim pb, PRetCommon.Builder ret) {

		// 校验
		if (StringUtils.isBlank(pb.getIncomeId())) {
			ret.setRetCode(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetMsg());
			return;
		}

		CWVUserPropertyIncome propertyIncome = new CWVUserPropertyIncome();
		propertyIncome.setIncomeId(Integer.parseInt(pb.getIncomeId()));

		CWVAuthUser user = userHelper.getCurrentUser(pack);
		final CWVUserPropertyIncome propertyIncome2 = dao.incomeDao.selectByPrimaryKey(propertyIncome);
		if (propertyIncome2 == null || propertyIncome2.getUserId() != user.getUserId()) {
			ret.setRetCode(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetMsg());
			return;
		}
		if (propertyIncome2.getStatus() != 0) {// 0 未领取
			ret.setRetCode(ReturnCodeMsgEnum.PIC_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PIC_ERROR_STATUS.getRetMsg());
			return;
		}
		final CWVUserWallet wallet = walletHelper.getUserAccount(user.getUserId(), CoinEnum.CWB);
		// 更新信息
		propertyIncome2.setStatus((byte) 1);// 已领取

		// 调取合约领取收益
		RespCreateTransaction.Builder retData = incomeInvoker.claimIncome(wallet,
				propertyIncome2.getAmount());

		// 获取房产收益超级账户
		if (retData.getRetCode() != 1) {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode()).setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			return;
		}
		//加入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.INCOME_CLAIM.getKey(), retData.getTxHash());
		propertyIncome2.setStatus(PropertyIncomeStatusEnum.CLAIMED.getValue());
		propertyIncome2.setChainStatusClaim(ChainTransStatusEnum.START.getKey());
		propertyIncome2.setChainTransHashClaim(retData.getTxHash());
		dao.incomeDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				// 更新收益状态
				dao.incomeDao.updateByPrimaryKeySelective(propertyIncome2);

				return null;
			}
		});

		ret.setRetCode(ReturnCodeMsgEnum.PIC_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.PIC_SUCCESS.getRetMsg());

	}

	public void cancelExchange(FramePacket pack, PSCommonExchange pb, PRetCommon.Builder ret) {
		// 校验
		if (StringUtils.isEmpty(pb.getExchangeId())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetMsg());
			return;
		}

		// 查询该交易
		// 查询交易状态
		final CWVMarketExchange exchangeRecord = new CWVMarketExchange();
		exchangeRecord.setExchangeId(Integer.parseInt(pb.getExchangeId()));
		// 获取当前用户
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		final CWVMarketExchange exchange = dao.exchangeDao.selectByPrimaryKey(exchangeRecord);
		if (exchange == null || !authUser.getUserId().equals(exchange.getUserId())) {
			ret.setRetCode(ReturnCodeMsgEnum.CPE_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPE_ERROR_ID.getRetMsg());
			return;
		}

		if (exchange.getStatus().intValue() != 0 || exchange.getChainStatus()!=1) {// 出手中
			ret.setRetCode(ReturnCodeMsgEnum.CPE_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPE_ERROR_STATUS.getRetMsg());
			return;
		}

		// 调取合约撤销
		RespCreateTransaction.Builder retCommon = exchangeInvoker.cancelExchange(exchange);
		if (retCommon.getRetCode() != 1) {
			ret.setRetCode("99").setRetMsg(retCommon.getRetMsg());
			return;
		}
		
		//插入交易管理
		commonHelper.txManageAdd(TransactionTypeEnum.EXCHANGE_SELL_CANCEL.getKey(),retCommon.getTxHash());
		
		// 设置交易状态： 撤销
		exchange.setStatus(PropertyExchangeStatusEnum.CANCEL.getValue());
		exchange.setChainStatus(ChainTransStatusEnum.START.getKey());
		exchange.setChainContract("");
		exchange.setChainTransHash(retCommon.getTxHash());
		
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setChainStatus(ChainTransStatusEnum.START.getKey());
		property.setChainTransHash(retCommon.getTxHash());
		
		dao.exchangeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);
				// 更新交易
				dao.exchangeDao.updateByPrimaryKeySelective(exchange);
				return null;
			}
		});
		

		ret.setRetCode(ReturnCodeMsgEnum.CPE_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.CPE_SUCCESS.getRetMsg());

	}

	public CWVGameProperty getByPropertyId(Integer propertyId) {
		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(propertyId);
		return dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
	}

	public void getPropertyGame(PSPropertyGame pb, PRetCommon.Builder ret, RetCodeMsg.Builder builder) {
		CWVGamePropertyGameExample example = new CWVGamePropertyGameExample();
		PageUtil pageUtil = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		CWVGamePropertyGameExample.Criteria criteria = example.createCriteria();
		if (StringUtils.isNotBlank(pb.getGameType())) {
			criteria.andGameTypeEqualTo(pb.getGameType());
		}

		if (StringUtils.isNotBlank(pb.getGameStatus())) {
			criteria.andGameStatusEqualTo(pb.getGameStatus());
		}

		if (StringUtils.isNotBlank(pb.getHotOrder()) && "1".equals(pb.getHotOrder())) {
			example.setOrderByClause(" players_count desc");
		}

		if (StringUtils.isNotBlank(pb.getIncomeOrder()) && "1".equals(pb.getIncomeOrder())) {
			example.setOrderByClause(" income desc");
		}

		if (StringUtils.isNotBlank(pb.getName())) {
			criteria.andNameLike("%" + pb.getName() + "%");
		}

		example.setLimit(pageUtil.getLimit());
		example.setOffset(pageUtil.getOffset());

		pageUtil.setTotalCount(dao.gamePropertyGameDao.countByExample(example));
		List<Object> list = dao.gamePropertyGameDao.selectByExample(example);
		RetData.Builder data = RetData.newBuilder();
		for (Object o : list) {
			CWVGamePropertyGame game = (CWVGamePropertyGame) o;
			CWVGameProperty gameProperty = new CWVGameProperty();
			gameProperty.setPropertyId(game.getPropertyId());
			gameProperty = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
			Property.Builder property = Property.newBuilder();
			propertyCopy(property, gameProperty);
			GameInfo.Builder gameInfo = GameInfo.newBuilder();
			gameInfo.setName(game.getName()).setGameId(game.getGameId() + "").setStatus(game.getStatus() + "")
					.setType(game.getType() + "").setGameStatus(game.getGameStatus()).setGameType(game.getGameType())
					.setGameUrl(game.getGameUrl()).setPlayers(game.getPlayersCount() + "");
			PropertyGameInfo.Builder propetyGameInfo = PropertyGameInfo.newBuilder();
			propetyGameInfo.setProperty(property);
			propetyGameInfo.setGameInfo(gameInfo);
			data.addPropertyGameInfo(propetyGameInfo);
		}

		builder.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		data.setPage(pageUtil.getPageOut());
		ret.setData(data);
	}

	public void getPropertyGameDetail(PSPropertyGameDetail pb, PRetCommon.Builder ret, RetCodeMsg.Builder builder) {
		if (StringUtils.isEmpty(pb.getGameId())) {

			builder.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			builder.setRetMsg("游戏ID不能为空");
			return;
		}

		CWVGamePropertyGame game = new CWVGamePropertyGame();
		game.setGameId(Integer.parseInt(pb.getGameId()));
		game = dao.gamePropertyGameDao.selectByPrimaryKey(game);
		if (game == null) {

			builder.setRetCode(ReturnCodeMsgEnum.PGD_ERROR_ID.getRetCode());
			builder.setRetMsg(ReturnCodeMsgEnum.PGD_ERROR_ID.getRetMsg());
			return;
		}
		Property.Builder property = Property.newBuilder();
		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(game.getPropertyId());
		gameProperty = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
		propertyCopy(property, gameProperty);

		RetData.Builder data = RetData.newBuilder();
		data.setProperty(property);

		GameDetail.Builder gameDetail = GameDetail.newBuilder();
		gameDetail.setDevelopers(game.getDevelopersCount() + "").setGameId(game.getGameId() + "")
				.setName(game.getName()).setImages(game.getImages()).setInstructions(game.getInstructions())
				.setPlayers(game.getPlayersCount() + "").setStatus(game.getStatus() + "").setType(game.getType() + "")
				.setGameStatus(game.getGameStatus()).setGameType(game.getGameType()).setGameUrl(game.getGameUrl());
		data.setGameDetail(gameDetail);

		builder.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode());
		builder.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setData(data);

	}

	public void getMapPropertyDetail(PSMapPropertyDetail pb, Builder ret,
			org.brewchain.cwv.service.game.Game.RetCodeMsg.Builder builder) {
		if (StringUtils.isEmpty(pb.getMapId())) {

			builder.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			builder.setRetMsg("地图ID不能为空");
			return;
		}

		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		PageUtil pageUtil = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		propertyExample.setLimit(pageUtil.getLimit());
		propertyExample.setOffset(pageUtil.getOffset());
		propertyExample.createCriteria().andGameMapIdEqualTo(Integer.parseInt(pb.getMapId()));
		pageUtil.setTotalCount(dao.gamePropertyDao.countByExample(propertyExample));
		List<Object> list = dao.gamePropertyDao.selectByExample(propertyExample);
		
		// 统计价值
		// 房产数量
		// 剩余房产数量
		propertyExample.setOrderByClause(" price_increase desc ");
		RetData.Builder data = RetData.newBuilder();
		MapPropertyDetail.Builder mapPropertyDetail = MapPropertyDetail.newBuilder();
		mapPropertyDetail.setAveragePrice("");
		mapPropertyDetail.setTotalValue("");
		mapPropertyDetail.setRemainCount("");
		for (Object o : list) {
			CWVGameProperty gameProperty = (CWVGameProperty) o;

			PropertyState.Builder propertyState = PropertyState.newBuilder();
			propertyState.setPropertyId(gameProperty.getPropertyId() + "")
					.setPropertyName(gameProperty.getPropertyName()).setPropertyStatus(Integer.parseInt(gameProperty.getPropertyStatus()))
					.setPrice(gameProperty.getLastPrice() + "");

			CWVMarketExchangeExample exchangeExample = new CWVMarketExchangeExample();
			exchangeExample.createCriteria().andPropertyIdEqualTo(gameProperty.getPropertyId())
					.andStatusEqualTo(PropertyExchangeStatusEnum.SOLD.getValue());
			exchangeExample.setOrderByClause("create_time desc");
			exchangeExample.setOffset(1);// 取倒数第二个成交价比较房产价格
			Object ob = dao.exchangeDao.selectOneByExample(exchangeExample);

			if (ob != null) {
				CWVMarketExchange exchange = (CWVMarketExchange) ob;
				propertyState.setUpDown(gameProperty.getLastPrice().compareTo(exchange.getSellPrice()));
			} else {
				propertyState.setUpDown(UpAndDownEnum.NOCHANGE.getValue());
			}

			mapPropertyDetail.addPropertyState(propertyState);
		}
		mapPropertyDetail.setPage(pageUtil.getPageOut());
		data.setMapPropertyDetail(mapPropertyDetail);

		builder.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode());
		builder.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setData(data);

	}

	/**
	 * 游戏类型分组对应的三条数据列表
	 * 
	 * @param pb
	 * @param ret
	 * @param builder
	 */
	public void getPropertyGameTypeList(PSCommon pb, Builder ret,
			org.brewchain.cwv.service.game.Game.RetCodeMsg.Builder builder) {
		List<Object> list = commonHelper.getDicEntities("game_type");
		RetData.Builder data = RetData.newBuilder();
		for (Object o : list) {
			CWVGameDic dic = (CWVGameDic) o;
			PropertyGameType.Builder propertyGameType = PropertyGameType.newBuilder();
			propertyGameType.setType(dic.getDicKey());
			CWVGamePropertyGameExample gameExample = new CWVGamePropertyGameExample();
			gameExample.createCriteria().andGameStatusEqualTo(dic.getDicKey());
			gameExample.setLimit(3);
			List<Object> listGame = dao.gamePropertyGameDao.selectByExample(gameExample);
			for (Object g : listGame) {
				CWVGamePropertyGame game = (CWVGamePropertyGame) g;
				GameType.Builder gameType = GameType.newBuilder();
				GameDetail.Builder gameDetail = GameDetail.newBuilder();
				gameDetail.setDevelopers(game.getDevelopersCount() + "").setGameId(game.getGameId() + "")
						.setName(game.getName()).setImages(game.getImages()).setInstructions(game.getInstructions())
						.setPlayers(game.getPlayersCount() + "").setStatus(game.getStatus() + "")
						.setType(game.getType() + "").setGameStatus(game.getGameStatus())
						.setGameType(game.getGameType()).setGameUrl(game.getGameUrl());
				gameType.setGameDetail(gameDetail);

				CWVGameProperty property = new CWVGameProperty();
				property.setPropertyId(game.getPropertyId());
				property = dao.gamePropertyDao.selectByPrimaryKey(property);

				if (property == null) {
					continue;
				} else if (property.getUserId() != null) {
					CWVAuthUser user = userHelper.getUserById(property.getUserId());
					if (user != null)
						gameType.setOwner(user.getNickName());
				}
				gameType.setIncome(property.getIncome() + "");
				propertyGameType.addGameType(gameType);
			}
			data.addPropertyGameType(propertyGameType);
		}

		builder.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode());
		builder.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
		ret.setData(data);
	}

	
	/**
	 * 查询抽奖结果
	 * @param pb
	 * @param ret
	 * @param builder
	 */
	public void drawTxInfo(PSPropertyDrawTx pb, Builder ret,
			org.brewchain.cwv.service.game.Game.RetCodeMsg.Builder builder) {
		RetData.Builder data = RetData.newBuilder();
		
		if(StringUtils.isEmpty(pb.getTxHash())) {
			builder.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			builder.setRetMsg("交易hash不能为空");
			ret.setData(data);
			return;
		}
		
		RespGetTxByHash.Builder respGetTxByHash = wltHelper.getTxInfo(pb.getTxHash());
		if(respGetTxByHash.getRetCode() == -1) {//失败
			builder.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			builder.setRetMsg("交易hash不能为空");
			ret.setData(data);
			return;
		}
		String status = respGetTxByHash.getTransaction().getStatus();
		
		if( status == null || status.equals("") ) {
			builder.setRetCode(ReturnCodeMsgEnum.DTI_PROCESS.getRetCode());
			builder.setRetMsg(ReturnCodeMsgEnum.DTI_PROCESS.getRetMsg());
			ret.setData(data);
			return;
		}else if(status.equals(ChainTransStatusEnum.DONE.getValue())){
			
			CWVMarketDrawExample example = new CWVMarketDrawExample();
			example.createCriteria().andChainTransHashRandomEqualTo(pb.getTxHash());
			Object o = dao.getDrawDao().selectOneByExample(example);
			CWVMarketDraw draw = (CWVMarketDraw) o;
			if(draw.getPropertyId()!=null){
				CWVGameProperty gameProperty = new CWVGameProperty();
				gameProperty.setPropertyId(draw.getPropertyId());
				gameProperty = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
				
				Property.Builder property = Property.newBuilder();
				propertyCopy(property, gameProperty);
				data.setProperty(property);
			}
			
			builder.setRetCode(ReturnCodeMsgEnum.DTI_DONE.getRetCode());
			builder.setRetMsg(ReturnCodeMsgEnum.DTI_DONE.getRetMsg());
			ret.setData(data);
		}else if(status.equals(ChainTransStatusEnum.ERROR.getValue())){
			builder.setRetCode(ReturnCodeMsgEnum.DTI_ERROR.getRetCode());
			builder.setRetMsg(ReturnCodeMsgEnum.DTI_ERROR.getRetMsg());
			ret.setData(data);
		}
		
	}


}
