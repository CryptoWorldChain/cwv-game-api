package org.brewchain.cwv.game.helper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCity;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCountry;
import org.brewchain.cwv.dbgens.game.entity.CWVGameMap;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBidExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradePwd;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.game.chain.PropertyBidInvoker;
import org.brewchain.cwv.game.chain.PropertyExchangeInvoker;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.helper.WalletHelper.Coin;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Bid.BidProperty;
import org.brewchain.cwv.service.game.Bid.PRetBidPropertyDetail;
import org.brewchain.cwv.service.game.Bid.PRetBidPropertyNotice;
import org.brewchain.cwv.service.game.Bid.PRetBidPropertyNotice.AuctionRank;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBid;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBid.BidInfo;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBidAuction;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBidAuction.PropertyBidAuction;
import org.brewchain.cwv.service.game.Bid.PSAuctionProperty;
import org.brewchain.cwv.service.game.Bid.PSCommonBid;
import org.brewchain.cwv.service.game.Bid.PSPropertyBid;
import org.brewchain.cwv.service.game.Bid.PSPropertyBidAuction;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord.PropertyDraw;
import org.brewchain.cwv.service.game.Draw.PSCommonDraw;
import org.brewchain.cwv.service.game.Draw.PSPropertyDrawRecord;
import org.brewchain.cwv.service.game.Exchange.ExchangeProperty;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange.ExchangeInfo;
import org.brewchain.cwv.service.game.Exchange.PRetSellProperty;
import org.brewchain.cwv.service.game.Exchange.PSBuyProperty;
import org.brewchain.cwv.service.game.Exchange.PSPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSSellProperty;
import org.brewchain.cwv.service.game.Game.PBGameProperty;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;
import org.brewchain.cwv.service.game.Game.PRetGamePropertyCharge;
import org.brewchain.cwv.service.game.Game.PRetProperty;
import org.brewchain.cwv.service.game.Game.PRetRefGameProperty;
import org.brewchain.cwv.service.game.Game.PSCommon;

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
public class PropertyHelper implements ActorService {

	@ActorRequire
	Daos daos;

	@ActorRequire
	WalletHelper walletHelper;

	@ActorRequire(scope = "global")
	UserHelper userHelper;

	@ActorRequire
	PropertyExchangeInvoker exchangeInvoker;

	@ActorRequire
	PropertyBidInvoker bidInvoker;

	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	public void getPropertyExchange(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, FramePacket pack) {

		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		setExchangeRet(pb, ret, page, null );
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());

	}
	
	
	private void setExchangeRet(PSPropertyExchange pb, PRetPropertyExchange.Builder ret,PageUtil page, String userId) {
		// 设置查询条件
				CWVMarketExchangeExample cwvMarketExchangeExample = new CWVMarketExchangeExample();
				CWVMarketExchangeExample.Criteria criteria = cwvMarketExchangeExample.createCriteria();
				cwvMarketExchangeExample.setLimit(page.getLimit());
				cwvMarketExchangeExample.setOffset(page.getOffset());
				// 房产类型
				if (StringUtils.isNotEmpty(pb.getPropertyType())) {
					criteria.andPropertyTypeEqualTo(pb.getPropertyType());
				}

				// 房产类型
				if (StringUtils.isNotEmpty(userId)) {
					criteria.andCreateUserEqualTo(Integer.parseInt(userId));
				}
				
				// 房产名称
				if (StringUtils.isNotEmpty(pb.getPropertyName())) {
					criteria.andPropertyNameLike("%"+pb.getPropertyName()+"%");
				}

				// 国家
				if (StringUtils.isNotEmpty(pb.getCountryId())) {
					criteria.andCountryIdEqualTo(Integer.parseInt(pb.getCountryId()));
				}

//				// 城市
//				if (StringUtils.isNotEmpty(pb.getCityId())) {
//					criteria.andCountryId(Integer.parseInt(pb.getCityId()));
//				}

				// 价格排序
				if (StringUtils.isNotEmpty(pb.getPriceType())) {
					if (pb.getPriceType().equals("0"))
						cwvMarketExchangeExample.setOrderByClause(" last_price desc ");
					if (pb.getPriceType().equals("1"))
						cwvMarketExchangeExample.setOrderByClause(" last_price ");
				}

				// 收益排序
				if (StringUtils.isNotEmpty(pb.getIncomeType())) {
					if (pb.getPriceType().equals("0"))
						cwvMarketExchangeExample.setOrderByClause(" income desc ");
					else if (pb.getPriceType().equals("1"))
						cwvMarketExchangeExample.setOrderByClause(" income ");
				}
				int count = daos.exchangeDao.countByExample(cwvMarketExchangeExample);
				page.setTotalCount(count);
				List<Object> list = daos.exchangeDao.selectByExample(cwvMarketExchangeExample);
				
				for (Object o : list) {
					CWVMarketExchange exchange = (CWVMarketExchange) o;
					// 设置房产信息
					ExchangeProperty.Builder property = ExchangeProperty.newBuilder();
					property.setCountryId(exchange.getCountryId()+"");
					property.setMapId(exchange.getMapId()+"");
					property.setPropertyTemplateId(exchange.getPropertyTemplateId());
					property.setPropertyTemplate(exchange.getPropertyTemplate());
					property.setOwner(exchange.getNickName());
					property.setPropertyId(exchange.getPropertyId()+"");
					property.setPropertyName(exchange.getPropertyName());
					property.setPropertyType(Integer.parseInt(exchange.getPropertyType()));

					property.setPropertyStatus(Integer.parseInt(exchange.getPropertyStatus()));
					property.setIncomeRemark(exchange.getIncomeRemark());
					property.setIncome(exchange.getIncome().doubleValue());
					// 设置交易信息
					ExchangeInfo.Builder exchangeRet = ExchangeInfo.newBuilder();
					exchangeRet.setExchangeId(exchange.getPropertyId()+"");
					exchangeRet.setPrice(exchange.getSellPrice().doubleValue());
					exchangeRet.setStatus(exchange.getStatus());
					exchangeRet.setProperty(property);

					ret.addExchange(exchangeRet);

				}
	}

	public void getPropertyExchangeBack(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, FramePacket pack) {

		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		// 设置查询条件
		StringBuffer sb = new StringBuffer();

		propertyExchangeSql(sb, pb, null);

		sb.append(" limit ").append(page.getOffset()).append(",").append(page.getLimit());

		setExchangeRet(sb.toString(), ret, page);

		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());

	}

	private void setExchangeRet(String sql, PRetPropertyExchange.Builder ret, PageUtil pageUtil) {
		List<Map<String, Object>> list = this.executeSqlRet(sql, pageUtil);
		for (Map<String, Object> map : list) {
			// 设置房产信息
			ExchangeProperty.Builder property = ExchangeProperty.newBuilder();
			// string country_id = 1;//所属国家
			// string map_id = 2;//所属地图
			// string property_template_id = 3;//房产模板Id
			// string property_template = 4;//房产模板
			// string owner = 5;//拥有者
			// string property_name = 6; //房产名称
			// string property_id = 7;//房产编码
			// int32 property_type = 8;//房产类型
			// int32 property_status = 9;//房产状态
			// string income_remark = 10;//房产说明
			// //string appearance_type = 11;//外观
			// double income = 12;//收益

			property.setCountryId(map.get("country_id").toString());
			property.setMapId(map.get("map_id").toString());
			property.setPropertyTemplateId(map.get("property_template_id").toString());
			property.setPropertyTemplate(map.get("property_template").toString());
			property.setOwner(map.get("owner").toString());
			property.setPropertyId(map.get("property_id").toString());
			property.setPropertyName(map.get("property_name").toString());
			property.setPropertyType(Integer.parseInt(map.get("property_type").toString()));

			property.setPropertyStatus(Integer.parseInt(map.get("status").toString()));
			property.setIncomeRemark(map.get("income_remark").toString());
			property.setIncome(Double.parseDouble(map.get("income").toString()));
			// 设置交易信息
			ExchangeInfo.Builder exchange = ExchangeInfo.newBuilder();
			exchange.setExchangeId(map.get("exchange_id").toString());
			exchange.setPrice(Double.parseDouble(map.get("exchange_price").toString()));
			exchange.setStatus(Integer.parseInt(map.get("exchange_status").toString()));
			exchange.setProperty(property);

			//
			// property.setCountryId("1");
			// property.setMapId("1");
			// property.setPropertyTemplateId("1");
			// property.setPropertyTemplate("1");
			// property.setOwner("kael");
			// property.setPropertyId("1");
			// property.setPropertyName("矿场007");
			// property.setPropertyType(1);
			//
			// property.setPropertyStatus(1);
			// property.setIncomeRemark("矿场可以带来收益");
			// property.setIncome(123132.77);
			// //设置交易信息
			// ExchangeInfo.Builder exchange = ExchangeInfo.newBuilder();
			// exchange.setExchangeId("1");
			// exchange.setPrice(44321.22);
			// exchange.setStatus(0);
			// exchange.setProperty(property);

			ret.addExchange(exchange);

		}
	}

	private void setBidRet(String sql, PRetPropertyBid.Builder ret, PageUtil pageUtil) {
		List<Map<String, Object>> list = this.executeSqlRet(sql, pageUtil);
		for (Map<String, Object> map : list) {
			// 设置房产信息
			BidProperty.Builder property = BidProperty.newBuilder();
			// string country_id = 1;//所属国家
			// string map_id = 2;//所属地图
			// string property_template_id = 3;//房产模板Id
			// string property_template = 4;//房产模板
			// string owner = 5;//拥有者
			// string property_name = 6; //房产名称
			// string property_id = 7;//房产编码
			// int32 property_type = 8;//房产类型
			// int32 property_status = 9;//房产状态
			// string income_remark = 10;//房产说明
			// //string appearance_type = 11;//外观
			// double income = 12;//收益

			property.setCountryId(map.get("country_id").toString());
			property.setMapId(map.get("map_id").toString());
			property.setPropertyTemplateId(map.get("property_template_id").toString());
			property.setPropertyTemplate(map.get("property_template").toString());
			property.setOwner(map.get("owner").toString());
			property.setPropertyId(map.get("property_id").toString());
			property.setPropertyName(map.get("property_name").toString());
			property.setPropertyType(Integer.parseInt(map.get("property_type").toString()));

			property.setPropertyStatus(Integer.parseInt(map.get("status").toString()));
			property.setIncomeRemark(map.get("income_remark").toString());
			// 设置交易信息
			BidInfo.Builder bid = BidInfo.newBuilder();
			bid.setBidId(map.get("bid_id").toString());
			bid.setAuctionStart(map.get("bid_auction_start").toString());
			bid.setPrice(map.get("bid_price").toString());
			bid.setProperty(property);

			//
			// property.setCountryId("1");
			// property.setMapId("1");
			// property.setPropertyTemplateId("1");
			// property.setPropertyTemplate("1");
			// property.setOwner("kael");
			// property.setPropertyId("1");
			// property.setPropertyName("矿场007");
			// property.setPropertyType(1);
			//
			// property.setPropertyStatus(1);
			// property.setIncomeRemark("矿场可以带来收益");
			// property.setIncome(123132.77);
			// //设置交易信息
			// ExchangeInfo.Builder exchange = ExchangeInfo.newBuilder();
			// exchange.setExchangeId("1");
			// exchange.setPrice(44321.22);
			// exchange.setStatus(0);
			// exchange.setProperty(property);

			ret.addBid(bid);

		}
	}

	private List<Map<String, Object>> executeSqlRet(String sql, PageUtil pageUtil) {
		String sqlCount = sql.substring(sql.indexOf("from"));
		sqlCount = "select count(1) as count " + sqlCount;
		List<Map<String, Object>> listCount = daos.provider.getCommonSqlMapper().executeSql(sqlCount);
		String count = listCount.get(0).get("count").toString();
		pageUtil.setTotalCount(Integer.parseInt(count));

		return daos.provider.getCommonSqlMapper().executeSql(sql);
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

		if (StringUtils.isNotEmpty(pb.getPropertyStatus())) {
			sb.append(" (select * from cwv_market_bid where status='").append(pb.getPropertyStatus()).append("') ");
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
		final CWVMarketExchange exchange = daos.exchangeDao.selectByPrimaryKey(exchangeRecord);
		if (exchange == null) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_ID.getRetMsg());
			return;
		}
		
		if (exchange.getStatus().intValue() != 0) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetMsg());
			return;
		}
		
		// 获取当前用户
		final CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		// 交易密码
		CWVUserTradePwd userTradePwd = userHelper.getTradePwd(authUser.getUserId());
		if (userTradePwd == null) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetMsg());
			return;
		}else{
			if(!userTradePwd.getTradePassword().equals(userHelper.getPwdMd5(pb.getTradePwd(), authUser.getSalt()))) {
				ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetCode())
				.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetMsg());
				return;
			}
		}
		
		// 查询余额

		final CWVUserWallet userWalletBuyer = walletHelper.getUserAccount(authUser.getUserId(), Coin.CWB);

		if (userWalletBuyer.getBalance().subtract(exchange.getSellPrice()).intValue() < 0) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_BALANCE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_BALANCE.getRetMsg());
			return;
		}

		// 调取合约，发起购买

		PRetCommon.Builder exchangeRet = exchangeInvoker.buyProperty(userWalletBuyer.getAccount(),
				exchange.getPropertyId().toString());
		// 添加调取合约日志 TODO

		if (!exchangeRet.getRetCode().equals(ReturnCodeMsgEnum.SUCCESS.getRetCode())) {
			ret.setRetCode(exchangeRet.getRetCode());
			ret.setRetMsg(exchangeRet.getRetMsg());
			return;
		}

		// 设置 交易成功数据 start


		// 设置房产交易
		exchange.setStatus((byte) 1);
		exchange.setUserId(authUser.getUserId());
		
		// 房产信息更新
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setLastPrice(exchange.getSellPrice());
		property.setLastPriceTime(new Date());
		property.setUserId(exchange.getUserId());
		property.setPropertyStatus("3");

		// 设置账户信息
		userWalletBuyer.setBalance(userWalletBuyer.getBalance().subtract(exchange.getSellPrice()));

		final CWVUserWallet userWalletSeller = walletHelper.getUserAccount(exchange.getCreateUser(), Coin.CWB);
		userWalletSeller.setBalance(userWalletSeller.getBalance().add(exchange.getSellPrice()).subtract(exchange.getTax()));
		
		
		// 设置交易记录
		final CWVUserTransactionRecord recordBuy = new CWVUserTransactionRecord();
		recordBuy.setCreateTime(new Date());
		recordBuy.setCreateUser(exchange.getUserId());
		recordBuy.setDetail("买入房产");
		recordBuy.setGainCost(exchange.getSellPrice().negate());
		recordBuy.setUserId(exchange.getUserId());
		
		final CWVUserTransactionRecord recordSell = new CWVUserTransactionRecord();
		recordSell.setCreateTime(new Date());
		recordSell.setCreateUser(exchange.getUserId());
		recordSell.setDetail("卖出房产");
		recordSell.setGainCost(exchange.getSellPrice().subtract(exchange.getTax()));
		recordSell.setUserId(exchange.getCreateUser());
		// 统一事务处理 暂时没有代码

		daos.exchangeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {

				// 链上操作记录Log

				// 更新交易
				daos.exchangeDao.updateByPrimaryKeySelective(exchange);
				// 更新房产
				daos.gamePropertyDao.updateByPrimaryKeySelective(property);
				
				// 用户交易

					// 账户余额
				daos.walletDao.updateByPrimaryKeySelective(userWalletBuyer);
				daos.walletDao.updateByPrimaryKeySelective(userWalletSeller);
					
					// 账户交易记录
				daos.userTransactionRecordDao.insert(recordBuy);
				daos.userTransactionRecordDao.insert(recordSell);
				return null;
			}

		});

		// 设置 交易成功数据 end

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
	public void sellProperty(FramePacket pack, PSSellProperty pb, PRetSellProperty.Builder ret) {

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
		}else{
			if(!userTradePwd.getTradePassword().equals(userHelper.getPwdMd5(pb.getTradePwd(), user.getSalt()))) {
				ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_TRADEPWD.getRetCode())
				.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_TRADEPWD.getRetMsg());
				return;
			}
		}
		// 查询该用户房产
		 CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(Integer.parseInt(pb.getPropetyId()));
		final CWVGameProperty property = daos.gamePropertyDao.selectByPrimaryKey(gameProperty);
		if (property == null || !property.getUserId().equals(user.getUserId())) {//当前用户
			ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetMsg());
			return;
		}
		
		
		if(!property.getPropertyStatus().equals("0")) {//必须是未出售状态
			ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_STATUS.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_STATUS.getRetMsg());
			return;
		}
		// 扣除手续费
		double chargeRate = getChargeRate();
		double charge = pb.getPrice() * chargeRate;

		// 生成交易
		// 调取卖出房产合约
		CWVUserWallet account = walletHelper.getUserAccount(user.getUserId(), Coin.CWB);
		PRetCommon.Builder exchangeRet = exchangeInvoker.sellProperty(account.getAccount(), pb.getPropetyId(),
				pb.getPrice(), charge);

		// 添加调取合约日志 TODO

		if (!exchangeRet.getRetCode().equals(ReturnCodeMsgEnum.SUCCESS.getRetCode())) {
			ret.setRetCode(exchangeRet.getRetCode());
			ret.setRetMsg(exchangeRet.getRetMsg());
			return;
		}

		final CWVMarketExchange exchange = new CWVMarketExchange();
		exchange.setPropertyId(Integer.parseInt(pb.getPropetyId()));
		exchange.setSellPrice(new BigDecimal(pb.getPrice()));
		exchange.setTax(new BigDecimal(charge));
		exchange.setUserId(user.getUserId());
		exchange.setCreateUser(user.getUserId()); //卖出
		exchange.setStatus((byte) 0);
		exchange.setCreateTime(new Date());
		exchange.setUpdateTime(new Date());
		
		//房产
		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = daos.gameMapDao.selectByPrimaryKey(map);
		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = daos.gameCityDao.selectByPrimaryKey(city);
		
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

		property.setPropertyStatus("2");//出售中
		daos.exchangeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				//更新交易
				daos.exchangeDao.insert(exchange);
				//更新房产
				daos.gamePropertyDao.updateByPrimaryKeySelective(property);
				return null;
			}
		});
		
		// 设置返回
		ret.setRetCode(ReturnCodeMsgEnum.SPS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SPS_SUCCESS.getRetMsg());

	}

	/**
	 * 获取竞拍房产列表
	 * 
	 * @param pb
	 * @param ret
	 */
	public void getPropertyBidBack(PSPropertyBid pb, PRetPropertyBid.Builder ret) {
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		// 设置查询条件
		StringBuffer sb = new StringBuffer();

		propertyBidSql(sb, pb, null);

		sb.append(" limit ").append(page.getOffset()).append(",").append(page.getLimit());

		setBidRet(sb.toString(), ret, page);

		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PBS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PBS_SUCCESS.getRetMsg());

		// property.setCountryId("1");
		// property.setMapId("1");
		// property.setPropertyTemplateId("1");
		// property.setPropertyTemplate("1");
		// property.setPropertyId("1");
		// property.setPropertyName("矿场007");
		// property.setPropertyType(1);
		//
		// property.setPropertyStatus(1);
		// property.setIncomeRemark("矿场可以带来收益");
		// //设置交易信息
		// BidInfo.Builder bid = BidInfo.newBuilder();
		//
		// bid.setAuctionStart("2018-04-20");
		// bid.setBidId("1");
		// bid.setPrice("34243.00");
		// bid.setStatus("1");
		// bid.setProperty(property);

		// ret.addBid(bid);

		// }

		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PBS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PBS_SUCCESS.getRetMsg());

	}

	/**
	 * 获取竞拍房产列表
	 * 
	 * @param pb
	 * @param ret
	 */
	public void getPropertyBid(PSPropertyBid pb, PRetPropertyBid.Builder ret) {
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		// 设置查询条件
		CWVMarketBidExample cwvMarketBidExample = new CWVMarketBidExample();
		CWVMarketBidExample.Criteria criteria = cwvMarketBidExample.createCriteria();

		if (StringUtils.isNotEmpty(pb.getPropertyStatus())) {
			criteria.andPropertyStatusEqualTo(pb.getPropertyStatus());
		}

		List<Object> list = daos.bidDao.selectByExample(cwvMarketBidExample);
		
		for (Object o : list) {
			CWVMarketBid bid = (CWVMarketBid) o;
			// 设置房产信息
			BidProperty.Builder property = BidProperty.newBuilder();
			property.setCountryId(o.toString());
			property.setMapId(bid.getMapId()+"");
			property.setPropertyTemplateId(bid.getPropertyTemplateId());
			property.setPropertyTemplate(bid.getPropertyTemplate());
			property.setOwner(bid.getNickName());
			property.setPropertyId(bid.getGamePropertyId()+"");
			property.setPropertyName(bid.getPropertyName());
			property.setPropertyType(Integer.parseInt(bid.getPropertyType()));

			property.setPropertyStatus(Integer.parseInt(bid.getPropertyStatus()));
			property.setIncomeRemark(bid.getIncomeRemark());
			// 设置交易信息
			BidInfo.Builder bidRet = BidInfo.newBuilder();
			bidRet.setBidId(bid.getBidId()+"");
			bidRet.setAuctionStart(DateUtil.getDayTime(bid.getAuctionStart()));
			bidRet.setProperty(property);
			ret.addBid(bidRet);

		}

		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PBS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PBS_SUCCESS.getRetMsg());

	}

	
	private PRetProperty.Builder getRetProperty(Map<String, Object> map) {
		PRetProperty.Builder pProperty = PRetProperty.newBuilder();
		pProperty.setMapId(map.get("map_id").toString());
		pProperty.setPropertyId(map.get("property_id").toString());
		pProperty.setPropertyName(map.get("property_name").toString());
		pProperty.setPropertyStatus(map.get("property_name").toString());
		pProperty.setPropertyType(map.get("property_type").toString());

		if (pProperty.getPropertyStatus().equals("3") || pProperty.getPropertyStatus().equals("2")) {
			pProperty.setOwner(map.get("owner").toString());
			pProperty.setPrice(map.get("price").toString());
		} else if (pProperty.getPropertyStatus().equals("0") || pProperty.getPropertyStatus().equals("1")) {
			pProperty.setPrice("0.000");
			pProperty.setOwner("No");
		}
		pProperty.setIncome(map.get("income").toString());
		pProperty.setPropertyTemplateId(map.get("property_template_id").toString());
		pProperty.setPropertyTemplate(map.get("property_template").toString());
		pProperty.setAppearanceType("1");
		return pProperty;
	}

	private PRetProperty.Builder getRetProperty(CWVGameProperty property) {

		PRetProperty.Builder pProperty = PRetProperty.newBuilder();
		pProperty.setMapId(property.getGameMapId() + "");
		pProperty.setPropertyId(property.getPropertyId() + "");
		pProperty.setPropertyName(property.getPropertyName());
		pProperty.setPropertyStatus(property.getPropertyStatus());
		pProperty.setPropertyType(property.getPropertyType());

		if (pProperty.getPropertyStatus().equals("3") || pProperty.getPropertyStatus().equals("2")) {
			pProperty.setOwner(pProperty.getOwner());
			pProperty.setPrice(pProperty.getPrice());
		} else if (pProperty.getPropertyStatus().equals("0") || pProperty.getPropertyStatus().equals("1")) {
			pProperty.setPrice("0.000");
			pProperty.setOwner("No");
		}
		pProperty.setIncome(pProperty.getIncome());
		pProperty.setPropertyTemplateId(property.getPropertyTemplateId() + "");
		pProperty.setPropertyTemplate(property.getPropertyTemplate());
		pProperty.setAppearanceType("1");

		return pProperty;

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

			ret.setTotalCount(daos.gamePropertyDao.countByExample(propertyExample) + "");
		}
		List<Object> properties = daos.gamePropertyDao.selectByExample(propertyExample);
		for (Object coun : properties) {
			CWVGameProperty property = (CWVGameProperty) coun;
			PRetProperty.Builder pProperty = this.getRetProperty(property);
			ret.addProperties(pProperty);
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

		List<Object> list = daos.gamePropertyDao.selectByExample(example);

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
	public void auctionProperty(FramePacket pack, final PSAuctionProperty pb, Builder ret) {

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
		final CWVMarketBid bid = daos.bidDao.selectByPrimaryKey(bidNew);
		if (bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetMsg());
			return;
		}
		
		if(bid.getStatus().intValue() == 0) {//未开始
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_STATUS_0.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_STATUS_0.getRetMsg());
			return;
		}else if(bid.getStatus().intValue() == 2) { //已结束
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_STATUS_2.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_STATUS_2.getRetMsg());
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
		CWVUserWallet cwbAccount = walletHelper.getUserAccount(user.getUserId(), Coin.CWB);
		if (cwbAccount.getBalance().compareTo(new BigDecimal(pb.getPrice())) < 0) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetMsg());
			return;
		}
		
		// 价格 阶梯
		if ((pb.getPrice()-bid.getBidStart().doubleValue())%bid.getIncreaseLadder().doubleValue() !=0  ) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_PRICE_LADDER.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_PRICE_LADDER.getRetMsg());
			return;
		}


		// 2扣除账户竞价资产

		// 2.1调取竞拍合约
		PRetCommon.Builder retContract = bidInvoker.auctionProperty(cwbAccount.getAccount(),
				bid.getGamePropertyId() + "", pb.getPrice() + "");
		// 2.2新增竞价记录，并更新竞价记录
		
		//TODO 增加调取竞拍合约日志

		CWVMarketAuctionExample example = new CWVMarketAuctionExample();
		example.createCriteria().andBidIdEqualTo(Integer.parseInt(pb.getBidId())).andStatusEqualTo((byte) 1)
				.andUserIdEqualTo(user.getUserId());
		final List<Object> list = daos.auctionDao.selectByExample(example);
		//账户
		final CWVUserWallet auctionAccount = walletHelper.getUserAccount(user.getUserId(), Coin.CWB);
		
		if (retContract.getRetCode().equals(ReturnCodeMsgEnum.APS_SUCCESS.getRetCode())) {
			daos.bidDao.doInTransaction(new TransactionExecutor() {
				
				@Override
				public Object doInTransaction() {
					// 更新个人竞拍记录
					if (list != null && !list.isEmpty()) {// 更新历史竞拍
						
						CWVMarketAuction old = (CWVMarketAuction) list.get(0);
						//账户金额 
						auctionAccount.setBalance(auctionAccount.getBalance().add(old.getBidPrice()).subtract(new BigDecimal(pb.getPrice())));
						
						old.setBidPrice(new BigDecimal(pb.getPrice()));
						daos.auctionDao.updateByPrimaryKey(old);
						
						
					} else {// 新增竞拍
						CWVMarketAuction newAuction = new CWVMarketAuction();
						newAuction.setBidId(bid.getBidId());
						newAuction.setBidPrice(new BigDecimal(pb.getPrice()));
						newAuction.setCreateTime(new Date());
						newAuction.setStatus((byte) 1);
						newAuction.setUserId(user.getUserId());
						daos.auctionDao.insert(newAuction);
						//账户金额 
						auctionAccount.setBalance(auctionAccount.getBalance().subtract(newAuction.getBidPrice()));
						
					}

					// 3更新竞拍信息 最高价 拥有者 更新时间

					bid.setBidAmount(new BigDecimal(pb.getPrice()));
					bid.setOwner(user.getUserId());
					bid.setLastUpdateTime(new Date());
					daos.bidDao.updateByPrimaryKeySelective(bid);
					//更新账户金额
					daos.walletDao.updateByPrimaryKeySelective(auctionAccount);
					return null;
				}
			});
			

		} else {
			ret.setRetCode(ReturnCodeMsgEnum.APS_FALURE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_FALURE.getRetMsg());
			return;
		}

		// 4设置竞价成功
		ret.setRetCode(ReturnCodeMsgEnum.APS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.PBS_SUCCESS.getRetMsg());

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
		List<Object> list = daos.drawDao.selectByExample(example);
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
	public void drawProperty(PSCommonDraw pb, Builder ret) {
		// 校验
		// 查询抽奖机会

		// 抽奖

	}

	public void getUserPropertyExchangeBack(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, FramePacket pack) {
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		// 设置查询条件
		StringBuffer sb = new StringBuffer();

		CWVAuthUser authUser = userHelper.getCurrentUser(pack);

		propertyExchangeSql(sb, pb, authUser.getUserId().toString());

		sb.append(" limit ").append(page.getOffset()).append(",").append(page.getLimit());

		setExchangeRet(sb.toString(), ret, page);

		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());
	}
	
	public void getUserPropertyExchange(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, FramePacket pack) {

		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);

		setExchangeRet(pb, ret, page, authUser.getUserId()+"" );
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());

	}

	public void bidDetail(PSCommonBid pb, PRetBidPropertyDetail.Builder ret) {
		//校验
		if(StringUtils.isEmpty(pb.getBidId())){
			ret.setRetCode(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetMsg());
			return ;
		}
		
		//查询
		CWVMarketBid bid = new CWVMarketBid();
		bid.setBidId(Integer.parseInt(pb.getBidId()));
		bid = daos.bidDao.selectByPrimaryKey(bid);
		if(bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.PBD_ERROR_ID.getRetMsg());
			return ;
		}else{
			if(!bid.getStatus().equals("1")) {
				ret.setRetCode(ReturnCodeMsgEnum.PBD_ERROR_STATUS.getRetCode())
				.setRetMsg(ReturnCodeMsgEnum.PBD_ERROR_STATUS.getRetMsg());
				return ;
			}
		}
		
		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(bid.getGamePropertyId());
		property = daos.gamePropertyDao.selectByPrimaryKey(property);
		
		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = daos.gameMapDao.selectByPrimaryKey(map);
		
		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = daos.gameCityDao.selectByPrimaryKey(city);
		
		CWVAuthUser user = userHelper.getUserById(property.getUserId());
		//设置返回信息
		
//		string ret_code = 21; //返回状态码
//		string ret_msg = 22; //提示信息
//		string country_id = 1;//所属国家
//		string map_id = 2;//所属地图
//	    string property_template_id = 3;//房产模板Id
//	    string property_template = 4;//房产模板
//	    //string owner = 5;//拥有者
//	    string property_name = 6; //房产名称
//	    string property_id = 7;//房产编码
//	    int32 property_type = 8;//房产类型
//	    int32 property_status = 9;//房产状态
//		string income_remark = 10;//房产说明

		BidProperty.Builder bidProperty = BidProperty.newBuilder();
		bidProperty.setCountryId(city.getGameCountryId()+"");
		bidProperty.setIncomeRemark("收益说明");
		bidProperty.setMapId(map.getMapId() + "");
		bidProperty.setOwner(user.getNickName());
		bidProperty.setPropertyId(property.getPropertyId()+"");
		bidProperty.setPropertyName(property.getPropertyName());
		bidProperty.setPropertyStatus(Integer.parseInt(property.getPropertyStatus()));
		bidProperty.setPropertyTemplate(property.getPropertyTemplate());
		bidProperty.setPropertyTemplateId(property.getPropertyTemplateId());
		bidProperty.setPropertyType(Integer.parseInt(property.getPropertyType()));
		
		
//	    string max_price = 12;//房产编码
//	    string bidders_count = 13;//当前参与人数
//	    string auction_start = 14;//开始时间
//	    string auction_end = 15;//结束时间
//	    string announce_time = 16;//公布时间
//	    string bid_start = 17;//最低喊价
		
		ret.setMaxPrice(bid.getBidAmount().toString());
		ret.setBiddersCount(bid.getBiddersCount()+"");
		ret.setAuctionStart(DateUtil.getDayTime(bid.getAuctionStart()));
		ret.setAuctionEnd(DateUtil.getDayTime(bid.getAuctionEnd()));
		ret.setAnnounceTime(DateUtil.getDayTime(bid.getAnnounceTime()));
		ret.setBidStart(bid.getBidStart().toString());
		ret.setRetCode(ReturnCodeMsgEnum.PBD_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.PBD_SUCCESS.getRetMsg());
		
	}

	public void getPropertyCharge(PSCommon pb, PRetGamePropertyCharge.Builder ret) {
		// TODO 是否查询链以及配置化

		ret.setChargeRate(getChargeRate());
		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg());
	}

	private double getChargeRate() {
		// TODO Auto-generated method stub
		return 0.1;
	}

	public void bidNotice(PSCommonBid pb, PRetBidPropertyNotice.Builder ret) {
		//校验
		if(StringUtils.isEmpty(pb.getBidId())){
			ret.setRetCode(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetMsg());
			return ;
		}
		
		//查询
		CWVMarketBid bid = new CWVMarketBid();
		bid.setBidId(Integer.parseInt(pb.getBidId()));
		bid = daos.bidDao.selectByPrimaryKey(bid);
		if(bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.PBN_ERROR_ID.getRetMsg());
			return ;
		}else{
			if(!bid.getStatus().equals("1")) {
				ret.setRetCode(ReturnCodeMsgEnum.PBN_ERROR_STATUS.getRetCode())
				.setRetMsg(ReturnCodeMsgEnum.PBN_ERROR_STATUS.getRetMsg());
				return ;
			}
		}
		
		
		CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(bid.getGamePropertyId());
		property = daos.gamePropertyDao.selectByPrimaryKey(property);
		
		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = daos.gameMapDao.selectByPrimaryKey(map);
		
		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = daos.gameCityDao.selectByPrimaryKey(city);
		
		CWVAuthUser user = userHelper.getUserById(property.getUserId());
		
		CWVMarketAuctionExample auctionExample = new CWVMarketAuctionExample();
		auctionExample.createCriteria().andBidIdEqualTo(bid.getBidId());
		auctionExample.setOrderByClause(" bid_price desc ");
		List<Object> listAuction = daos.auctionDao.selectByExample(auctionExample);
		if(listAuction !=null && !listAuction.isEmpty()) {
			for (Object object : listAuction) {
				CWVMarketAuction auction = (CWVMarketAuction) object;
				CWVAuthUser authUser = userHelper.getUserById(auction.getAuctionId());
				AuctionRank.Builder rank = AuctionRank.newBuilder();
				rank.setBidAmount(auction.getBidPrice().toString());
				rank.setNickName(authUser.getNickName());
				ret.addAuctionRank(rank);
			}
		}
		//设置返回信息
		
//				string ret_code = 21; //返回状态码
//				string ret_msg = 22; //提示信息
//		string country_id = 1;//所属国家
//		string map_id = 2;//所属地图
//	    string property_template_id = 3;//房产模板Id
//	    string property_template = 4;//房产模板
//	    string owner = 5;//拥有者
//	    string property_name = 6; //房产名称
//	    string property_id = 7;//房产编码
//		string income_remark = 10;//房产说明

		BidProperty.Builder bidProperty = BidProperty.newBuilder();
		bidProperty.setCountryId(city.getGameCountryId()+"");
		bidProperty.setMapId(map.getMapId() + "");
		bidProperty.setPropertyTemplate(property.getPropertyTemplate());
		bidProperty.setPropertyTemplateId(property.getPropertyTemplateId());
		bidProperty.setOwner(user.getNickName());
		bidProperty.setPropertyName(property.getPropertyName());
		bidProperty.setPropertyId(property.getPropertyId()+"");
		bidProperty.setIncomeRemark("收益说明");
		
//			    string max_price = 12;//房产编码
//			    string bidders_count = 13;//当前参与人数
//			    string auction_start = 14;//开始时间
//			    string auction_end = 15;//结束时间
//			    string announce_time = 16;//公布时间
//			    string bid_start = 17;//最低喊价
		
		ret.setBidPrice(bid.getBidAmount().doubleValue());
		
		ret.setRetCode(ReturnCodeMsgEnum.PBD_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.PBD_SUCCESS.getRetMsg());
				
	}

}
