package org.brewchain.cwv.game.helper;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.auth.impl.UserHelper;
import org.brewchain.cwv.dbgens.auth.entity.CWVAuthUser;
import org.brewchain.cwv.dbgens.game.entity.CWVGameCity;
import org.brewchain.cwv.dbgens.game.entity.CWVGameDic;
import org.brewchain.cwv.dbgens.game.entity.CWVGameDicExample;
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
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncome;
import org.brewchain.cwv.dbgens.user.entity.CWVUserPropertyIncomeExample;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTradePwd;
import org.brewchain.cwv.dbgens.user.entity.CWVUserTransactionRecord;
import org.brewchain.cwv.dbgens.user.entity.CWVUserWallet;
import org.brewchain.cwv.game.chain.PropertyBidInvoker;
import org.brewchain.cwv.game.chain.PropertyDrawInvoker;
import org.brewchain.cwv.game.chain.PropertyExchangeInvoker;
import org.brewchain.cwv.game.chain.PropertyIncomeInvoker;
import org.brewchain.cwv.game.chain.ret.RetDraw;
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
import org.brewchain.cwv.service.game.Bid.PSCreatePropertyBid;
import org.brewchain.cwv.service.game.Bid.PSPropertyBid;
import org.brewchain.cwv.service.game.Bid.PSPropertyBidAuction;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDraw;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDraw.DrawProperty;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord.PropertyDraw;
import org.brewchain.cwv.service.game.Draw.PSCommonDraw;
import org.brewchain.cwv.service.game.Draw.PSPropertyDrawRecord;
import org.brewchain.cwv.service.game.Exchange.ExchangeProperty;
import org.brewchain.cwv.service.game.Exchange.ExchangeProperty.ExchangeInfo;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PRetSellProperty;
import org.brewchain.cwv.service.game.Exchange.PSBuyProperty;
import org.brewchain.cwv.service.game.Exchange.PSCommonExchange;
import org.brewchain.cwv.service.game.Exchange.PSPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSSellProperty;
import org.brewchain.cwv.service.game.Game.PBGameProperty;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;
import org.brewchain.cwv.service.game.Game.PRetGamePropertyCharge;
import org.brewchain.cwv.service.game.Game.PRetProperty;
import org.brewchain.cwv.service.game.Game.PRetRefGameProperty;
import org.brewchain.cwv.service.game.Game.PSCommon;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome.PropertyIncome;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome.PropertyInfo;
import org.brewchain.cwv.service.game.User.PRetPropertyIncome.PropertyInfo.SubTypeInfo;
import org.brewchain.cwv.service.game.User.PSPropertyIncome;
import org.brewchain.cwv.service.game.User.PSPropertyIncomeClaim;

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
@Instantiate(name="Property_Helper")
public class PropertyHelper implements ActorService {

	@ActorRequire(name="Daos", scope = "global")
	Daos dao;

	@ActorRequire(name="Wallet_Helper")
	WalletHelper walletHelper;

	@ActorRequire(name="User_Helper", scope = "global")
	UserHelper userHelper;

	@ActorRequire(name="Property_Exchange_Invoker")
	PropertyExchangeInvoker exchangeInvoker;

	@ActorRequire(name="Property_Bid_Invoker")
	PropertyBidInvoker bidInvoker;

	@ActorRequire(name="Property_Draw_Invoker")
	PropertyDrawInvoker drawInvoker;
	
	@ActorRequire(name="Property_Income_Invoker")
	PropertyIncomeInvoker incomeInvoker;

	@ActorRequire(name="Common_Helper")
	CommonHelper commonHelper;
	
	private HashSet statusForSale = new HashSet<String>() {
		{
			add("0"); // 未出售
			// add("1"); 出售中
			// add("2"); 竞拍中
			add("3"); // 已出售
		}
	};
	
	enum PropertyStatus{
		NOSALE("0"),// 未出售
		ONSALE("1"), //出售中
		BIDDING("2"); //竞拍中
		private String value;
		PropertyStatus(String value){
			this.value = value;
		}
	}

	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	public void getPropertyExchange(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, FramePacket pack) {

		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());

		setExchangeRet(pb, ret, page, null);
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());

	}

	private void setExchangeRet(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, PageUtil page, String userId) {
		// 设置查询条件
		CWVGamePropertyExample cwvPropertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria = cwvPropertyExample.createCriteria();
		cwvPropertyExample.setLimit(page.getLimit());
		cwvPropertyExample.setOffset(page.getOffset());
		
		if(StringUtils.isEmpty(userId)) {
			criteria.addCriterion("property_id in (select property_id from cwv_market_exchange where status='0' )");
		}else {
			criteria.andUserIdEqualTo(Integer.parseInt(userId));
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
			criteria.addCriterion("game_map_id in (select map_id from cwv_game_city where game_country_id='"+pb.getCountryId()+"')");
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
			if (pb.getPriceType().equals("0"))
				cwvPropertyExample.setOrderByClause(" income desc ");
			else if (pb.getPriceType().equals("1"))
				cwvPropertyExample.setOrderByClause(" income ");
		}
		int count = dao.gamePropertyDao.countByExample(cwvPropertyExample);
		page.setTotalCount(count);
		List<Object> list = dao.gamePropertyDao.selectByExample(cwvPropertyExample);

		for (Object o : list) {
			CWVGameProperty gameProperty = (CWVGameProperty) o;
			// 设置房产信息
			ExchangeProperty.Builder property = ExchangeProperty.newBuilder();
			
			CWVGameMap map = new CWVGameMap();
			map.setMapId(gameProperty.getGameMapId());
			map = dao.gameMapDao.selectByPrimaryKey(map);
			
			CWVGameCity city = new CWVGameCity();
			city.setCityId(map.getGameCityId());
			city = dao.gameCityDao.selectByPrimaryKey(city);
			
			property.setCountryId(city.getGameCountryId() + "");
			property.setMapId(map.getMapId() + "");
			property.setMapTemplate(map.getTemplate()+"");
			property.setPropertyTemplateId(gameProperty.getPropertyTemplateId());
			property.setPropertyTemplate(gameProperty.getPropertyTemplate());
			CWVAuthUser user = userHelper.getUserById(gameProperty.getUserId());
			property.setOwner(user.getNickName());
			property.setPropertyId(gameProperty.getPropertyId() + "");
			property.setPropertyName(gameProperty.getPropertyName());
			property.setPropertyType(Integer.parseInt(gameProperty.getPropertyType()));

			property.setPropertyStatus(Integer.parseInt(gameProperty.getPropertyStatus()));
			property.setIncomeRemark("收益说明");
			property.setIncome(gameProperty.getIncome().doubleValue());
			property.setImageUrl(gameProperty.getImageUrl());
			// 设置交易信息
			CWVMarketExchangeExample exchangeExample = new CWVMarketExchangeExample();
			exchangeExample.createCriteria().andPropertyIdEqualTo(gameProperty.getPropertyId())
			.andStatusNotEqualTo(Byte.parseByte(PropertyStatus.BIDDING.value));
			
			List<Object> listExchange = dao.exchangeDao.selectByExample(exchangeExample);
			for(Object ob : listExchange) {
				
				CWVMarketExchange exchange = (CWVMarketExchange) ob;
				ExchangeInfo.Builder exchangeRet = ExchangeInfo.newBuilder();
				exchangeRet.setExchangeId(exchange.getExchangeId() + "");
				exchangeRet.setPrice(exchange.getSellPrice().doubleValue());
				exchangeRet.setStatus(exchange.getStatus());
				property.setExchange(exchangeRet);
			}
			ret.addProperty(property);
		}
	}
	
	private void setBidRet(PSPropertyExchange pb, PRetPropertyExchange.Builder ret, PageUtil page, String userId) {
		// 设置查询条件
		CWVGamePropertyExample cwvPropertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria = cwvPropertyExample.createCriteria();
		cwvPropertyExample.setLimit(page.getLimit());
		cwvPropertyExample.setOffset(page.getOffset());
		
		// 竞拍中
		criteria.andPropertyStatusEqualTo(PropertyStatus.BIDDING.value);
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
			criteria.addCriterion("game_map_id in (select map_id from cwv_game_city where game_country_id='"+pb.getCountryId()+"')");
		}
	
		// // 城市
		// if (StringUtils.isNotEmpty(pb.getCityId())) {
		// criteria.andCountryId(Integer.parseInt(pb.getCityId()));
		// }
		
		CWVAuthUser authUser = userHelper.getUserById(Integer.parseInt(userId));
		
		// 用户
		criteria.addCriterion("property_id in (select property_id from cwv_market_auction where user_id='"+authUser.getUserId()+"' and status='1')");
		
		// 价格排序
		if (StringUtils.isNotEmpty(pb.getPriceType())) {
			if (pb.getPriceType().equals("0"))
				cwvPropertyExample.setOrderByClause(" last_price desc ");
			if (pb.getPriceType().equals("1"))
				cwvPropertyExample.setOrderByClause(" last_price ");
		}
	
		// 收益排序
		if (StringUtils.isNotEmpty(pb.getIncomeType())) {
			if (pb.getPriceType().equals("0"))
				cwvPropertyExample.setOrderByClause(" income desc ");
			else if (pb.getPriceType().equals("1"))
				cwvPropertyExample.setOrderByClause(" income ");
		}
		int count = dao.gamePropertyDao.countByExample(cwvPropertyExample);
		page.setTotalCount(count);
		List<Object> list = dao.gamePropertyDao.selectByExample(cwvPropertyExample);

		for (Object o : list) {
			CWVGameProperty gameProperty = (CWVGameProperty) o;
			// 设置房产信息
			ExchangeProperty.Builder property = ExchangeProperty.newBuilder();
			
			CWVGameMap map = new CWVGameMap();
			map.setMapId(gameProperty.getGameMapId());
			map = dao.gameMapDao.selectByPrimaryKey(map);
			
			CWVGameCity city = new CWVGameCity();
			city.setCityId(map.getGameCityId());
			city = dao.gameCityDao.selectByPrimaryKey(city);
			
			property.setCountryId(city.getGameCountryId() + "");
			property.setMapId(map.getMapId() + "");

			property.setMapTemplate(map.getTemplate()+"");
			property.setPropertyTemplateId(gameProperty.getPropertyTemplateId());
			property.setPropertyTemplate(gameProperty.getPropertyTemplate());

			property.setOwner(authUser.getNickName());
			property.setPropertyId(gameProperty.getPropertyId() + "");
			property.setPropertyName(gameProperty.getPropertyName());
			property.setPropertyType(Integer.parseInt(gameProperty.getPropertyType()));

			property.setPropertyStatus(Integer.parseInt(gameProperty.getPropertyStatus()));
			property.setIncomeRemark(property.getIncomeRemark());
			property.setIncome(gameProperty.getIncome().doubleValue());
			property.setImageUrl(gameProperty.getImageUrl());
			// 设置交易信息
			
			CWVMarketBidExample bidExample = new CWVMarketBidExample();
			
			bidExample.createCriteria().andGamePropertyIdEqualTo(gameProperty.getPropertyId());
			
			List<Object> listBid = dao.bidDao.selectByExample(bidExample);
			
			for(Object ob : listBid) {
				
				CWVMarketBid bid = (CWVMarketBid) ob;
				ExchangeInfo.Builder exchangeRet = ExchangeInfo.newBuilder();
				exchangeRet.setExchangeId(bid.getGamePropertyId() + "");
				exchangeRet.setPrice(bid.getLastPrice().doubleValue());
				exchangeRet.setStatus(bid.getStatus());
				property.setExchange(exchangeRet);
			}
			ret.addProperty(property);
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

			property.setExchange(exchange);
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

			ret.addProperty(property);

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

		if (exchange.getStatus().intValue() != 0) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetMsg());
			return;
		}

		// 获取当前用户
		final CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		if(exchange.getCreateUser().equals(authUser.getUserId())) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_USER.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_USER.getRetMsg());
			return;
		}
		
		// 交易密码
		CWVUserTradePwd userTradePwd = userHelper.getTradePwd(authUser.getUserId());
		if (userTradePwd == null) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_TRADE_PWD.getRetMsg());
			return;
		} else {
			if (!userTradePwd.getTradePassword().equals(userHelper.getPwdMd5(pb.getTradePwd(), authUser.getSalt()))) {
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
		property.setPropertyStatus(PropertyStatus.NOSALE.value);

		// 设置账户信息
		userWalletBuyer.setBalance(userWalletBuyer.getBalance().subtract(exchange.getSellPrice()));

		final CWVUserWallet userWalletSeller = walletHelper.getUserAccount(exchange.getCreateUser(), Coin.CWB);
		userWalletSeller
				.setBalance(userWalletSeller.getBalance().add(exchange.getSellPrice()).subtract(exchange.getTax()));

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

		dao.exchangeDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {

				// 链上操作记录Log

				// 更新交易
				dao.exchangeDao.updateByPrimaryKeySelective(exchange);
				// 更新房产
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);

				// 用户交易

				// 账户余额
				dao.walletDao.updateByPrimaryKeySelective(userWalletBuyer);
				dao.walletDao.updateByPrimaryKeySelective(userWalletSeller);

				// 账户交易记录
				dao.userTransactionRecordDao.insert(recordBuy);
				dao.userTransactionRecordDao.insert(recordSell);
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

		if (pb.getPrice() <= 0 ) {// 出售价格
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
		if (property == null || !property.getUserId().equals(user.getUserId())) {// 当前用户
			ret.setRetCode(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_ERROR_ID.getRetMsg());
			return;
		}

		if (!this.statusForSale.contains(property.getPropertyStatus())) {// 必须是未出售状态
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
		property.setPropertyStatus(PropertyStatus.ONSALE.value);// 出售中
		dao.exchangeDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {
				// 更新交易
				dao.exchangeDao.insert(exchange);
				// 更新房产
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);
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

		if (StringUtils.isNotEmpty(pb.getStatus())) {
			criteria.andStatusEqualTo(Byte.parseByte(pb.getStatus()));
		}
		cwvMarketBidExample.setOrderByClause("auction_start desc");
		int count = dao.bidDao.countByExample(cwvMarketBidExample);
		page.setTotalCount(count);
		List<Object> list = dao.bidDao.selectByExample(cwvMarketBidExample);

		for (Object o : list) {
			CWVMarketBid bid = (CWVMarketBid) o;
			CWVGameProperty gameProperty = getByPropertyId(bid.getGamePropertyId());
			
			// 设置房产信息
			BidProperty.Builder property = BidProperty.newBuilder();
			CWVGameMap gameMap = new CWVGameMap();
			gameMap.setMapId(gameProperty.getGameMapId());
			gameMap = dao.gameMapDao.selectByPrimaryKey(gameMap);
			CWVGameCity gameCity = new CWVGameCity();
			gameCity.setCityId(gameMap.getGameCityId());
			gameCity = dao.gameCityDao.selectByPrimaryKey(gameCity);
			
			property.setCountryId(gameCity.getGameCountryId() + "");
			property.setMapId(gameMap.getMapId() + "");
			property.setMapTemplate(gameMap.getTemplate() + "");
			property.setPropertyTemplateId(gameProperty.getPropertyTemplateId());
			property.setPropertyTemplate(gameProperty.getPropertyTemplate());
			if(gameProperty.getUserId() != null){
				CWVAuthUser user = userHelper.getUserById(gameProperty.getUserId());
				property.setOwner(user.getNickName());
			}
			
			property.setPropertyId(gameProperty.getPropertyId() + "");
			property.setPropertyName(gameProperty.getPropertyName());
			property.setPropertyType(Integer.parseInt(gameProperty.getPropertyType()));

			property.setPropertyStatus(Integer.parseInt(gameProperty.getPropertyStatus()));
			property.setIncomeRemark("收益说明");
			property.setImageUrl(gameProperty.getImageUrl());
			// 设置交易信息
			BidInfo.Builder bidRet = BidInfo.newBuilder();
			bidRet.setBidId(bid.getBidId() + "");
			bidRet.setAuctionStart(DateUtil.getDayTime(bid.getAuctionStart()));
			bidRet.setAuctionEnd(DateUtil.getDayTime(bid.getAuctionEnd()));
			bidRet.setPrice(bid.getLastPrice() + "");
			bidRet.setStatus(bid.getStatus() + "");
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
		CWVGameMap gameMap = new CWVGameMap();
		gameMap.setMapId(property.getGameMapId());
		gameMap = dao.gameMapDao.selectByPrimaryKey(gameMap);
		pProperty.setMapTemplate(gameMap.getTemplate() + "");
		pProperty.setPropertyId(property.getPropertyId() + "");
		pProperty.setPropertyName(property.getPropertyName());
		pProperty.setPropertyStatus(property.getPropertyStatus());
		pProperty.setPropertyType(property.getPropertyType());
		if(property.getUserId()!=null) {
			CWVAuthUser authUser = userHelper.getUserById(property.getUserId());
			pProperty.setOwner(authUser.getNickName());
		}
		pProperty.setPrice(property.getLastPrice()+"");
		pProperty.setIncome(property.getIncome()+"");
		pProperty.setPropertyTemplateId(property.getPropertyTemplateId() + "");
		pProperty.setPropertyTemplate(property.getPropertyTemplate());
		pProperty.setAppearanceType("1");
		pProperty.setUrl(property.getImageUrl());
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

			ret.setTotalCount(dao.gamePropertyDao.countByExample(propertyExample) + "");
		}
		List<Object> properties = dao.gamePropertyDao.selectByExample(propertyExample);
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
		if ((pb.getPrice() - bid.getBidStart().doubleValue()) % bid.getIncreaseLadder().doubleValue() != 0) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_PRICE_LADDER.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_PRICE_LADDER.getRetMsg());
			return;
		}

		// 2扣除账户竞价资产

		// 2.1调取竞拍合约
		PRetCommon.Builder retContract = bidInvoker.auctionProperty(cwbAccount.getAccount(),
				bid.getGamePropertyId() + "", pb.getPrice() + "");
		// 2.2新增竞价记录，并更新竞价记录

		// TODO 增加调取竞拍合约日志

		CWVMarketAuctionExample example = new CWVMarketAuctionExample();
		example.createCriteria().andBidIdEqualTo(Integer.parseInt(pb.getBidId())).andStatusEqualTo((byte) 1)
				.andUserIdEqualTo(user.getUserId());
		final List<Object> list = dao.auctionDao.selectByExample(example);
		// 账户
		final CWVUserWallet auctionAccount = walletHelper.getUserAccount(user.getUserId(), Coin.CWB);

		if (retContract.getRetCode().equals(ReturnCodeMsgEnum.APS_SUCCESS.getRetCode())) {
			dao.bidDao.doInTransaction(new TransactionExecutor() {

				@Override
				public Object doInTransaction() {

					CWVUserTransactionRecord recordAuction = new CWVUserTransactionRecord();
					// 设置交易记录
					recordAuction.setCreateTime(new Date());
					recordAuction.setCreateUser(user.getUserId());
					recordAuction.setDetail("竞拍房产");
					recordAuction.setUserId(user.getUserId());

					// 更新个人竞拍记录
					if (list != null && !list.isEmpty()) {// 更新历史竞价

						CWVMarketAuction old = (CWVMarketAuction) list.get(0);
						// 账户金额
						BigDecimal gainCost = old.getBidPrice().subtract(new BigDecimal(pb.getPrice()));
						auctionAccount.setBalance(auctionAccount.getBalance().add(gainCost));
						// 交易记录
						recordAuction.setGainCost(gainCost);

						old.setBidPrice(new BigDecimal(pb.getPrice()));
						dao.auctionDao.updateByPrimaryKey(old);

					} else {// 新增竞拍
						CWVMarketAuction newAuction = new CWVMarketAuction();
						newAuction.setBidId(bid.getBidId());
						newAuction.setBidPrice(new BigDecimal(pb.getPrice()));
						newAuction.setCreateTime(new Date());
						newAuction.setStatus((byte) 1);
						newAuction.setUserId(user.getUserId());
						dao.auctionDao.insert(newAuction);
						// 账户金额
						BigDecimal gainCost = newAuction.getBidPrice();
						auctionAccount.setBalance(auctionAccount.getBalance().subtract(gainCost));
						// 交易记录
						recordAuction.setGainCost(gainCost.negate());
						// 更新参与人数数
						bid.setBiddersCount(bid.getBiddersCount() + 1);
					}

					// 3更新竞拍信息 最高价 拥有者 更新时间

					bid.setBidAmount(new BigDecimal(pb.getPrice()));
					bid.setOwner(user.getUserId());
					bid.setLastUpdateTime(new Date());
					dao.bidDao.updateByPrimaryKeySelective(bid);
					// 更新账户金额
					dao.walletDao.updateByPrimaryKeySelective(auctionAccount);
					// 插入钱包操作记录
					dao.userTransactionRecordDao.insert(recordAuction);
					return null;
				}
			});

		} else {
			ret.setRetCode(ReturnCodeMsgEnum.APS_FALURE.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.APS_FALURE.getRetMsg());
			return;
		}

		// 4设置竞价成功
		ret.setRetCode(ReturnCodeMsgEnum.APS_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.APS_SUCCESS.getRetMsg());

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
	public void drawProperty(FramePacket pack, PSCommonDraw pb, PRetPropertyDraw.Builder ret) {
		// 校验
		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		// 查询抽奖机会
		CWVUserWallet walletCWB = walletHelper.getUserAccount(authUser.getUserId(), Coin.CWB);
		if (walletCWB.getDrawCount() < 1) {
			ret.setRetCode(ReturnCodeMsgEnum.PDS_ERROR_DRAW_COUNT.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.PDS_ERROR_DRAW_COUNT.getRetMsg());
			return;
		}
		// 抽奖
		// 调取抽奖合约
		final CWVUserWallet wallet = walletHelper.getUserAccount(authUser.getUserId(), Coin.CWB);
		RetDraw retDraw = drawInvoker.drawProperty(wallet.getAccount());
		if (!retDraw.getRetCode().equals(ReturnCodeMsgEnum.SUCCESS.getRetCode())) {
			ret.setRetCode(retDraw.getRetCode());
			ret.setRetMsg(retDraw.getRetMsg());
			return;
		}

		// 更新房产信息
		final CWVGameProperty gameProperty = getDrawProperty();
		//设置抽奖合约返回ID
//		gameProperty.setPropertyId(1);
		gameProperty.setUserId(authUser.getUserId());
		gameProperty.setPropertyStatus("0");
		// 更新抽奖次数
		wallet.setDrawCount(wallet.getDrawCount() - 1);
		dao.drawDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {
				// 更新房产信息
				dao.gamePropertyDao.updateByPrimaryKeySelective(gameProperty);
				// 更新抽奖次数
				dao.walletDao.updateByPrimaryKeySelective(wallet);

				return null;
			}
		});

		CWVGameProperty property = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);

		DrawProperty.Builder drawProperty = DrawProperty.newBuilder();

		drawProperty.setPropertyId(gameProperty.getPropertyId() + "");

		CWVGameMap map = new CWVGameMap();
		map.setMapId(property.getGameMapId());
		map = dao.gameMapDao.selectByPrimaryKey(map);

		CWVGameCity city = new CWVGameCity();
		city.setCityId(map.getGameCityId());
		city = dao.gameCityDao.selectByPrimaryKey(city);

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

		drawProperty.setCountryId(city.getGameCountryId() + "");
		drawProperty.setIncomeRemark("收益说明");
		drawProperty.setMapId(map.getMapId() + "");

		drawProperty.setMapTemplate(map.getTemplate() + "");
		if (authUser != null)
			drawProperty.setOwner(authUser.getNickName() + "");
		drawProperty.setPropertyId(property.getPropertyId() + "");
		drawProperty.setPropertyName(property.getPropertyName());
		drawProperty.setPropertyTemplate(property.getPropertyTemplate());
		drawProperty.setPropertyTemplateId(property.getPropertyTemplateId());
		drawProperty.setPropertyType(Integer.parseInt(property.getPropertyType()));
		drawProperty.setImageUrl(property.getImageUrl());

		ret.setRetCode(ReturnCodeMsgEnum.SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.SUCCESS.getRetMsg())
				.setProperty(drawProperty);

	}

	private CWVGameProperty getDrawProperty() {
		CWVGamePropertyExample example = new CWVGamePropertyExample();
		example.createCriteria().andUserIdIsNull()
		.andPropertyTypeEqualTo("2");
		Object o = dao.gamePropertyDao.selectOneByExample(example);
		return (CWVGameProperty) o;
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
		if("1".equals(pb.getMarketType()))
			setBidRet(pb, ret, page, authUser.getUserId() + "");
		else
			setExchangeRet(pb, ret, page, authUser.getUserId() + "");
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());

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

		BidProperty.Builder bidProperty = BidProperty.newBuilder();
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
		ret.setBidStart(bid.getBidStart().toString());
		ret.setRetCode(ReturnCodeMsgEnum.PBD_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.PBD_SUCCESS.getRetMsg());

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

		BidProperty.Builder bidProperty = BidProperty.newBuilder();
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
		ret.setProperty(bidProperty);
		ret.setBidPrice(bid.getBidAmount().doubleValue());

		ret.setRetCode(ReturnCodeMsgEnum.PBD_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.PBD_SUCCESS.getRetMsg());

	}

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
		}else if(org.brewchain.cwv.auth.util.DateUtil.compare(DateUtil.getDateTime(pb.getAuctionEnd()), new Date()) <=0){
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode()).setRetMsg("竞拍止期必须大于当前时间");
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
		final CWVMarketBid bid = new CWVMarketBid();

		bid.setGamePropertyId(Integer.parseInt(pb.getPropertyId()));
		bid.setAuctionEnd(DateUtil.getDateTime(pb.getAuctionEnd()));
		bid.setAuctionStart(DateUtil.getDateTime(pb.getAuctionEnd()));
		bid.setBidStart(new BigDecimal(pb.getBidStart()));
		bid.setIncreaseLadder(Long.parseLong(pb.getIncreaseLadder()));
		if(StringUtils.isEmpty(pb.getAnnounceTime())) {
			bid.setAnnounceTime(DateUtil.addMinute(bid.getAuctionEnd(), 60));
		}else{
			bid.setAnnounceTime(DateUtil.addMinute(bid.getAuctionEnd(), Integer.parseInt(pb.getAnnounceTime())));
		}
		
		bid.setBidAmount(new BigDecimal(pb.getBidStart()));
		bid.setBiddersCount(0);
		// 生成交易
		// 调取卖出房产合约
		PRetCommon.Builder exchangeRet = bidInvoker.createBid(bid);

		// 添加调取合约日志 TODO

		if (!exchangeRet.getRetCode().equals(ReturnCodeMsgEnum.SUCCESS.getRetCode())) {
			ret.setRetCode(exchangeRet.getRetCode());
			ret.setRetMsg(exchangeRet.getRetMsg());
			return;
		}
		if(org.brewchain.cwv.auth.util.DateUtil.compare(bid.getAuctionStart(), new Date()) > 0)
			bid.setStatus((byte)0);
		else 
			bid.setStatus((byte)1);
		
		bid.setCreateTime(new Date());
		bid.setCreateUser(user.getUserId() + "");

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
		property.setPropertyStatus("1");// 竞拍中

		dao.bidDao.doInTransaction(new TransactionExecutor() {

			@Override
			public Object doInTransaction() {
				// 更新交易
				dao.bidDao.insert(bid);
				// 更新房产
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);

				return null;
			}
		});

		// 设置返回
		ret.setRetCode(ReturnCodeMsgEnum.CPB_SUCCESS.getRetCode()).setRetMsg(ReturnCodeMsgEnum.CPB_SUCCESS.getRetMsg());

	}

	public void propertyIncome(FramePacket pack, PSPropertyIncome pb, PRetPropertyIncome.Builder ret) {

		CWVAuthUser authUser = userHelper.getCurrentUser(pack);
		CWVUserWallet userWallet = walletHelper.getUserAccount(authUser.getUserId(), Coin.CWB);
		CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
		CWVUserPropertyIncomeExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(authUser.getUserId());
//		criteria.andStatusEqualTo((byte) 0);// 新建收益
		criteria.andPropertyIdIsNull();//property_id为null为统计数据
		example.setLimit(1);
		example.setOrderByClause(" income_id desc ");
		if (StringUtils.isEmpty(pb.getType())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode());
			ret.setRetMsg("类型不能为空");
			return;
		}

		criteria.andTypeEqualTo(Byte.parseByte(pb.getType()));
		//根据类型查询房产
		List<Object> list = dao.incomeDao.selectByExample(example);
		//未领取收益
		
		
//		for (Object o : list) {
//			CWVUserPropertyIncome income = (CWVUserPropertyIncome) o;
//			incomeUnclaim = incomeUnclaim + income.getAmount().doubleValue();
//		}
//		
		// 房产信息
		PropertyInfo.Builder propertyInfo = PropertyInfo.newBuilder();
		// propertyInfo.setSubTypeInfo(index, value)
		propertyInfo.setDescription("");
		CWVGameDicExample dicExample = new CWVGameDicExample();
		if (pb.getType().equals("1")) {// 普通房产
			CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
			CWVGamePropertyExample.Criteria propertyCriteria = propertyExample.createCriteria();
			propertyCriteria.andUserIdEqualTo(authUser.getUserId()).andPropertyTypeEqualTo(pb.getType());
			List<Object> listProperty= dao.gamePropertyDao.selectByExample(propertyExample);
			double totalValue = 0;
			for(Object o : listProperty){
				CWVGameProperty gameProperty = (CWVGameProperty) o;
				totalValue = totalValue + (gameProperty.getLastPrice() == null? 0 : gameProperty.getLastPrice().doubleValue());
			}
			
			propertyInfo.setTotalValue(totalValue+"");// 房产总价值
			
		} else { //
			
			dicExample.createCriteria().andParentKeyEqualTo(pb.getType());
			List<Object> listDic = dao.dicDao.selectByExample(dicExample);
			for (Object o : listDic) {
				CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
				CWVGamePropertyExample.Criteria propertyCriteria = propertyExample.createCriteria();
				propertyCriteria.andUserIdEqualTo(authUser.getUserId()).andPropertyTypeEqualTo(pb.getType());
				CWVGameDic dic = (CWVGameDic) o;
				SubTypeInfo.Builder subType = SubTypeInfo.newBuilder();
				subType.setPropertySubType(dic.getKey());
				propertyCriteria.andPropertySubTypeEqualTo(Byte.parseByte(dic.getKey()));
				int countType = dao.gamePropertyDao.countByExample(propertyExample);
				subType.setCount(countType + "");
				propertyInfo.addSubTypeInfo(subType);
			}
		}


		PropertyIncome.Builder income =  PropertyIncome.newBuilder();
		if(list != null  && !list.isEmpty()) {
			CWVUserPropertyIncome propertyIncome = (CWVUserPropertyIncome) list.get(0);
			income.setAmount(propertyIncome.getAmount().toString());
			income.setIncomeId(propertyIncome.getIncomeId() + "");
		}
		
		income.setCoinType(Coin.CWB.getValue() + "");
		
		String time = commonHelper.getSysSettingValue(CommonHelper.INCOMETIME);
		ret.setNextIncomeTime(time);//派息日期
		switch (pb.getType()) {
		case "1":
			ret.setIncomeTotal(userWallet.getIncomeOrdinary()+"");
			break;
		case "2":
			ret.setIncomeTotal(userWallet.getIncomeTypical()+"");
			break;
		case "3":
			ret.setIncomeTotal(userWallet.getIncomeFunctional()+"");
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
		
		//校验
		if(StringUtils.isEmpty(pb.getIncomeId())) {
			ret.setRetCode(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetMsg());
			return ; 
		}
		
		CWVUserPropertyIncome propertyIncome = new CWVUserPropertyIncome();
		propertyIncome.setIncomeId(Integer.parseInt(pb.getIncomeId()));

		CWVAuthUser user = userHelper.getCurrentUser(pack);
		final CWVUserPropertyIncome propertyIncome2 = dao.incomeDao.selectByPrimaryKey(propertyIncome);
		if(propertyIncome2 == null || propertyIncome2.getUserId() != user.getUserId() ) {
			ret.setRetCode(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.PIC_ERROR_ID.getRetMsg());
			return ; 
		}
		if(propertyIncome2.getStatus() !=0) {
			ret.setRetCode(ReturnCodeMsgEnum.PIC_ERROR_STATUS.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.PIC_ERROR_STATUS.getRetMsg());
			return ; 
		}
		final CWVUserWallet wallet = walletHelper.getUserAccount(user.getUserId(), Coin.CWB);
		//更新信息
		propertyIncome2.setStatus((byte)1);
		
		// 调取合约领取收益
		PRetCommon.Builder retCommon = incomeInvoker.claimIncome(wallet.getAccount(), propertyIncome2.getType(), propertyIncome2.getAmount());
		if(!retCommon.getRetCode().equals(ReturnCodeMsgEnum.SUCCESS.getRetCode())) {
			ret.setRetCode(retCommon.getRetCode())
			.setRetMsg(retCommon.getRetMsg());
			return ;
		}
		dao.incomeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				
				//更新收益状态
				dao.incomeDao.updateByPrimaryKeySelective(propertyIncome2);
				
				//更新各个房产收益记录状态
				CWVUserPropertyIncomeExample example = new CWVUserPropertyIncomeExample();
				example.createCriteria().andUserIdEqualTo(propertyIncome2.getUserId())
				.andTypeEqualTo(propertyIncome2.getType()).andPropertyIdIsNotNull();
				CWVUserPropertyIncome income = new CWVUserPropertyIncome();
				income.setStatus((byte) 1);
				dao.incomeDao.updateByExampleSelective(income, example);
				//更新账户历史收益
				switch (propertyIncome2.getType()) {
				case 1:
					wallet.setIncomeOrdinary(wallet.getIncomeOrdinary().add(propertyIncome2.getAmount()));
					break;
				case 2:
					wallet.setIncomeTypical(wallet.getIncomeTypical().add(propertyIncome2.getAmount()));
					break;
				case 3:
					wallet.setIncomeFunctional(wallet.getIncomeFunctional().add(propertyIncome2.getAmount()));
					break;
				default:
					break;
				}
				
				wallet.setBalance(wallet.getBalance().add(propertyIncome2.getAmount()));
				dao.walletDao.updateByPrimaryKeySelective(wallet);
				return null;
			}
		});
		
		ret.setRetCode(ReturnCodeMsgEnum.PIC_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.PIC_SUCCESS.getRetMsg());
		
	}

	public void cancelExchange(FramePacket pack, PSCommonExchange pb, PRetCommon.Builder ret) {
		//校验
		if(StringUtils.isEmpty(pb.getExchangeId())) {
			ret.setRetCode(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.ERROR_VALIDATION.getRetMsg());
			return ;
		}
		
		//查询该交易
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

		if (exchange.getStatus().intValue() != 0) {//出手中
			ret.setRetCode(ReturnCodeMsgEnum.CPE_ERROR_STATUS.getRetCode())
					.setRetMsg(ReturnCodeMsgEnum.CPE_ERROR_STATUS.getRetMsg());
			return;
		}
		
		//调取合约撤销
		PRetCommon.Builder retCommon = exchangeInvoker.cancelExchange("","");
		if(!retCommon.getRetCode().equals(ReturnCodeMsgEnum.SUCCESS.getRetCode())) {
			ret.setRetCode(retCommon.getRetCode())
			.setRetMsg(retCommon.getRetMsg());
			return ;
		}
		//设置交易状态： 撤销		
		exchange.setStatus((byte) 2);
		
		//房产信息
		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(exchange.getPropertyId());
		final CWVGameProperty property = dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
		property.setPropertyStatus("0");
		dao.exchangeDao.doInTransaction(new TransactionExecutor() {
			
			@Override
			public Object doInTransaction() {
				//更新交易
				dao.exchangeDao.updateByPrimaryKeySelective(exchange);
				
				//更新房产
				dao.gamePropertyDao.updateByPrimaryKeySelective(property);
				
				return null;
			}
		});
		
		ret.setRetCode(ReturnCodeMsgEnum.CPE_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.CPE_SUCCESS.getRetMsg());
		
	}
	
	public CWVGameProperty getByPropertyId(Integer propertyId)  {
		CWVGameProperty gameProperty = new CWVGameProperty();
		gameProperty.setPropertyId(propertyId);
		return dao.gamePropertyDao.selectByPrimaryKey(gameProperty);
	}
	
	

}
