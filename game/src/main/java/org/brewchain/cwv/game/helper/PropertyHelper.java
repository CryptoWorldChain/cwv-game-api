package org.brewchain.cwv.game.helper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.ipojo.annotations.Provides;
import org.brewchain.cwv.dbgens.game.entity.CWVGameProperty;
import org.brewchain.cwv.dbgens.game.entity.CWVGamePropertyExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuction;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketAuctionExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketBid;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDraw;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketDrawExample;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchange;
import org.brewchain.cwv.dbgens.market.entity.CWVMarketExchangeExample;
import org.brewchain.cwv.game.dao.Daos;
import org.brewchain.cwv.game.enums.ReturnCodeMsgEnum;
import org.brewchain.cwv.game.util.DateUtil;
import org.brewchain.cwv.game.util.PageUtil;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBid;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBid.PropertyBid;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBidAuction;
import org.brewchain.cwv.service.game.Bid.PRetPropertyBidAuction.PropertyBidAuction;
import org.brewchain.cwv.service.game.Bid.PSAuctionProperty;
import org.brewchain.cwv.service.game.Bid.PSPropertyBid;
import org.brewchain.cwv.service.game.Bid.PSPropertyBidAuction;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord;
import org.brewchain.cwv.service.game.Draw.PRetPropertyDrawRecord.PropertyDraw;
import org.brewchain.cwv.service.game.Draw.PSCommonDraw;
import org.brewchain.cwv.service.game.Draw.PSPropertyDrawRecord;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PRetPropertyExchange.PropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSBuyProperty;
import org.brewchain.cwv.service.game.Exchange.PSPropertyExchange;
import org.brewchain.cwv.service.game.Exchange.PSSellProperty;
import org.brewchain.cwv.service.game.Game.PBGameProperty;
import org.brewchain.cwv.service.game.Game.PRetCommon;
import org.brewchain.cwv.service.game.Game.PRetCommon.Builder;
import org.brewchain.cwv.service.game.Game.PRetProperty;
import org.brewchain.cwv.service.game.Game.PRetRefGameProperty;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import onight.osgi.annotation.iPojoBean;
import onight.tfw.ntrans.api.ActorService;
import onight.tfw.ntrans.api.annotation.ActorRequire;
import onight.tfw.ojpa.api.TransactionExecutor;

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
	WalletHelper waletHelper;
	
	// 防止相互引用死循环
	@Override
	public String toString() {
		return "102service:";
	}

	public void getPropertyExchange(PSPropertyExchange pb, PRetPropertyExchange.Builder ret) {

		CWVMarketExchangeExample example = new CWVMarketExchangeExample();
		CWVMarketExchangeExample.Criteria criteria = example.createCriteria();
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		if (StringUtils.isNotEmpty(pb.getPropertyName())) {
			criteria.addCriterion(" property_id in ()");
		}
		StringBuffer sql = new StringBuffer();
		sql.append("");
		
		sql.append(" limit ").append(page.getOffset()).append(",").append(page.getLimit());
		
		
		List<Map<String,Object>> list = daos.provider.getCommonSqlMapper().executeSql(sql.toString());
		
		for(Map<String,Object> map :list){
			PropertyExchange.Builder exchange = PropertyExchange.newBuilder();
			exchange.setCreateUser(map.get("create_user").toString());
			exchange.setOwner(map.get("owner").toString());
			exchange.setPrice(Double.parseDouble(map.get("price").toString()));
			exchange.setStatus(Integer.parseInt(map.get("status").toString()));
			exchange.setTax(Double.parseDouble(map.get("tax").toString()));
			PRetProperty.Builder property = this.getRetProperty(map);
			exchange.setProperty(property);
			ret.addExchange(exchange);
		}
		
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PES_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PES_SUCCESS.getRetMsg());

	}
	
	/**
	 * 购买房产
	 * @param pb
	 * @param ret
	 */
	public void buyProperty(PSBuyProperty pb, Builder ret) {

		//校验
		if (StringUtils.isEmpty(pb.getExchangeId())) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_ID.getRetMsg());
			return;
		}
		
		if (pb.getAmount() <= 0) {
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_AMOUNT.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_AMOUNT.getRetMsg());
			return;
		}
		//查询交易状态
		CWVMarketExchange exchangeRecord = new CWVMarketExchange();
		exchangeRecord.setExchangeId(Integer.parseInt(pb.getExchangeId()));
		final CWVMarketExchange exchange = daos.exchangeDao.selectByPrimaryKey(exchangeRecord);		
		if(exchange.getStatus().intValue() != 0){
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_STATUS.getRetMsg());
			return;
		}
		
		if(new BigDecimal(pb.getAmount()).subtract(exchange.getSellPrice()).intValue() >=0){
			ret.setRetCode(ReturnCodeMsgEnum.BPS_ERROR_AMOUNT.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.BPS_ERROR_AMOUNT.getRetMsg());
			return;
		}
		
		//设置 交易成功数据 start
			//房产信息更新
		final CWVGameProperty property = new CWVGameProperty();
		property.setPropertyId(exchange.getPropertyId());
		property.setLastPrice(new BigDecimal(pb.getAmount()));
			//需要引入jar包
		property.setOwner(123);
		property.setPropertyStatus("3");
		
		//统一事务处理 暂时没有代码
			
		daos.exchangeDao.doInTransaction(new TransactionExecutor() {
			
		//资产处理 TODO
			//账户
			
			//房产记录
		
		
			@Override
			public Object doInTransaction() {

				//链上操作记录Log
				
				
				//更新交易
				daos.exchangeDao.updateByPrimaryKeySelective(exchange);
				//更新房产
				daos.gamePropertyDao.updateByPrimaryKeySelective(property);
				//个人交易记录
			
					//账户
					//房产记录
			
				return null;
			}
		});
			
		//设置 交易成功数据 end
		
		//设置返回数据
		ret.setRetCode(ReturnCodeMsgEnum.BPS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.BPS_SUCCESS.getRetMsg());
		
	}

	/**
	 * 卖出房产
	 * @param pb
	 * @param ret
	 */
	public void sellProperty(PSSellProperty pb, PRetCommon.Builder ret) {
		
		//校验
		if(StringUtils.isEmpty(pb.getPropetyId())) {//房产ID
			ret.setRetCode(ReturnCodeMsgEnum.SPS_VALIDATE_ID.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_VALIDATE_ID.getRetMsg());
			return ;
		}
		
		if(pb.getPrice() <= 0) {//出售价格
			ret.setRetCode(ReturnCodeMsgEnum.SPS_VALIDATE_PRICE.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_VALIDATE_PRICE.getRetMsg());
			return ;
		}
			//校验 手续费 todo
			
		if(false) {
			ret.setRetCode(ReturnCodeMsgEnum.SPS_VALIDATE_TAX.getRetCode());
			ret.setRetMsg(ReturnCodeMsgEnum.SPS_VALIDATE_TAX.getRetMsg());
			return ;
		}
		
		// 扣除手续费
		
		// 生成交易
		
		CWVMarketExchange exchange = new CWVMarketExchange();
		exchange.setPropertyId(Integer.parseInt(pb.getPropetyId()));
		exchange.setSellPrice(new BigDecimal(pb.getPrice()));
		exchange.setTax(new BigDecimal(pb.getTax()));
		//TODO 获取用户
		exchange.setUserId(1);
//		exchange.setCreateUser("1");
		exchange.setStatus((byte) 0);
		exchange.setCreateTime(new Date());
		exchange.setUpdateTime(new Date());
		
		daos.exchangeDao.insert(exchange);
		//daos.exchangeDao.doInTransaction(exec)
		//设置返回
		ret.setRetCode(ReturnCodeMsgEnum.SPS_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.SPS_SUCCESS.getRetMsg());
		
	}
	
	/**
	 * 获取竞拍房产列表
	 * @param pb
	 * @param ret
	 */
	public void getPropertyBid(PSPropertyBid pb, PRetPropertyBid.Builder ret) {
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		StringBuffer sql = new StringBuffer();
		sql.append("");
		//是否单条查询
		if(StringUtils.isEmpty(pb.getBidId()))
			sql.append(" limit ").append(page.getOffset()).append(",").append(page.getLimit());
		
		
		List<Map<String,Object>> list = daos.provider.getCommonSqlMapper().executeSql(sql.toString());
		
		for(Map<String,Object> map :list){
			PropertyBid.Builder bid = PropertyBid.newBuilder();
			bid.setBidId(map.get("bid_id").toString());
			bid.setAuctionEnd(map.get("auction_end").toString());
			bid.setAuctionStart(map.get("auction_start").toString());
			PRetProperty.Builder property = this.getRetProperty(map);
			bid.setProperty(property);
			ret.addBid(bid);
		}
		
		ret.setPage(page.getPageOut());
		ret.setRetCode(ReturnCodeMsgEnum.PBS_SUCCESS.getRetCode());
		ret.setRetMsg(ReturnCodeMsgEnum.PBS_SUCCESS.getRetMsg());
		
	}
	
	private PRetProperty.Builder getRetProperty(Map<String,Object> map){
		PRetProperty.Builder pProperty = PRetProperty.newBuilder();
		pProperty.setMapId(map.get("map_id").toString());
		pProperty.setPropertyId(map.get("property_id").toString());
		pProperty.setPropertyName(map.get("property_name").toString());
		pProperty.setPropertyStatus(map.get("property_name").toString());
		pProperty.setPropertyType(map.get("property_type").toString());
		
		if(pProperty.getPropertyStatus().equals("3") || pProperty.getPropertyStatus().equals("2")){
			pProperty.setOwner(map.get("owner").toString());
			pProperty.setPrice(map.get("price").toString());
		}else if(pProperty.getPropertyStatus().equals("0")
				|| pProperty.getPropertyStatus().equals("1")){
			pProperty.setPrice("0.000");
			pProperty.setOwner("No");
		}
		pProperty.setIncome(map.get("income").toString());
		pProperty.setPropertyTemplateId(map.get("property_template_id").toString());
		pProperty.setPropertyTemplate(map.get("property_template").toString());
		pProperty.setAppearanceType("1");
		return pProperty;
	}
	private PRetProperty.Builder getRetProperty(CWVGameProperty property){
		
		PRetProperty.Builder pProperty = PRetProperty.newBuilder();
		pProperty.setMapId(property.getGameMapId()+"");
		pProperty.setPropertyId(property.getPropertyId()+"");
		pProperty.setPropertyName(property.getPropertyName());
		pProperty.setPropertyStatus(property.getPropertyStatus());
		pProperty.setPropertyType(property.getPropertyType());
		
		if(pProperty.getPropertyStatus().equals("3") || pProperty.getPropertyStatus().equals("2")){
			pProperty.setOwner(pProperty.getOwner());
			pProperty.setPrice(pProperty.getPrice());
		}else if(pProperty.getPropertyStatus().equals("0")
				|| pProperty.getPropertyStatus().equals("1")){
			pProperty.setPrice("0.000");
			pProperty.setOwner("No");
		}
		pProperty.setIncome(pProperty.getIncome());
		pProperty.setPropertyTemplateId(property.getPropertyTemplateId()+"");
		pProperty.setPropertyTemplate(property.getPropertyTemplate());
		pProperty.setAppearanceType("1");
		
		return pProperty;
	
	}
	
	/**
	 * 地区房产列表
	 * @param pb
	 * @param ret
	 */
	public void gameMapProperty(PBGameProperty pb,PRetRefGameProperty.Builder ret){
		ret.setRetCode("01");
		ret.setRetMsg("SUCCESS");
		
		if(StringUtils.isEmpty(pb.getMapId())){
			ret.setRetCode("80");
			ret.setRetMsg("地图ID不能为空");
			return;
		}
		
		CWVGamePropertyExample propertyExample = new CWVGamePropertyExample();
		CWVGamePropertyExample.Criteria criteria = propertyExample.createCriteria();
		criteria.andIsDisplayEqualTo("1").andGameMapIdEqualTo(Integer.parseInt(pb.getMapId()));
		
		if(StringUtils.isNotBlank(pb.getPropertyName())){
			criteria.andPropertyNameEqualTo(pb.getPropertyName());
		}
		
		if(StringUtils.isNotBlank(pb.getPropertyStatus())){
			criteria.andPropertyStatusEqualTo(pb.getPropertyStatus());
		}
		
		if(StringUtils.isNotBlank(pb.getPropertyType())){
			criteria.andPropertyTypeEqualTo(pb.getPropertyType());
		}
		
		if(StringUtils.isNotBlank(pb.getIsPage())&&"1".equals(pb.getIsPage())){
			PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
			propertyExample.setLimit(page.getLimit());
			propertyExample.setOffset(page.getOffset());
			
			ret.setTotalCount(daos.gamePropertyDao.countByExample(propertyExample)+"");
		}
		List<Object> properties = daos.gamePropertyDao.selectByExample(propertyExample);
		for(Object coun : properties){
			CWVGameProperty property = (CWVGameProperty) coun;
			PRetProperty.Builder pProperty = this.getRetProperty(property);
			ret.addProperties(pProperty);
		}
		
	}

	/**
	 * 获取个人竞价记录
	 * @param pb
	 * @param ret
	 */
	public void getPropertyBidAuction(PSPropertyBidAuction pb,
			PRetPropertyBidAuction.Builder ret) {
		//区分查询内容
		PageUtil page = new PageUtil(pb.getPageIndex(), pb.getPageSize());
		
		CWVMarketAuctionExample example = new CWVMarketAuctionExample();
		
		CWVMarketAuctionExample.Criteria criteria = example.createCriteria();
		
		if(StringUtils.isNotEmpty(pb.getAuctionId())) {
			criteria.andAuctionIdEqualTo(Integer.parseInt(pb.getAuctionId()));
		}else{
			if(StringUtils.isNotEmpty(pb.getBidId())) {
				criteria.andBidIdEqualTo(Integer.parseInt(pb.getBidId()));
			}
		}
		
		List<Object> list  = daos.gamePropertyDao.selectByExample(example);
		
		for(Object o : list) {
			CWVMarketAuction auctionCWV = (CWVMarketAuction)o;
			PropertyBidAuction.Builder auction = PropertyBidAuction.newBuilder();
			auction.setAmount(auctionCWV.getBidPrice()+"");
			auction.setAuctionId(auctionCWV.getAuctionId()+"");
			auction.setBidId(auctionCWV.getBidId() + "");
			auction.setBidPrice(auctionCWV.getBidPrice()+"");
//			auction.setNickName(auctionCWV.getNickName());
			
			ret.addAuction(auction);
		}
		ret.setRetCode(ReturnCodeMsgEnum.PBA_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.PBA_SUCCESS.getRetMsg());
		
	}

	/**
	 * 发起竞价
	 * @param pb
	 * @param ret
	 */
	public void auctionProperty(PSAuctionProperty pb, Builder ret) {
		
		//1校验
			//1.1非空校验 
			
		if(StringUtils.isEmpty(pb.getBidId())) {//竞拍ID
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetMsg());
			return;
		}
			
		if(pb.getPrice() <= 0) {//竞拍价
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_PRICE.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_PRICE.getRetMsg());
			return;
		}
		
		//1.2 数据校验 
			// ID存在
		CWVMarketBid bid = new CWVMarketBid();
		bid.setBidId(Integer.parseInt(pb.getBidId()));
		bid = daos.bidDao.selectByPrimaryKey(bid);
		if(bid == null) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_VALIDATE_ID.getRetMsg());
			return;
		}
		
		
			//价格  竞价最高价对比  查询钱包账户
		if(bid.getBidAmount().doubleValue() >= pb.getPrice()) {
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_PRICE.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_PRICE.getRetMsg());
			return;
		}
		
		//查询
		
		if(waletHelper.CWBAccountBalance("", "") < pb.getPrice()){
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_ACCOUNT.getRetMsg());
			return;
		}
		
		//1.3查询竞拍时间以及竞拍状态
		if(bid.getStatus()!=0 || bid.getAuctionEnd().compareTo(new Date())<0){
			ret.setRetCode(ReturnCodeMsgEnum.APS_ERROR_STATUS.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.APS_ERROR_STATUS.getRetMsg());
			return;
		}
		
		//2扣除账户竞价资产  
			
			//2.1调取竞拍合约
		PRetCommon.Builder retContract =  waletHelper.invokeBidContract("", "", "");
			//2.2新增竞价记录，并更新竞价记录
		
		if(retContract.getRetCode().equals("1")) {
			CWVMarketAuctionExample example = new CWVMarketAuctionExample();
			example.createCriteria().andBidIdEqualTo(Integer.parseInt(pb.getBidId()))
			.andStatusEqualTo((byte) 1);
			
			CWVMarketAuction update = new CWVMarketAuction();
			update.setStatus((byte) 2);
			daos.auctionDao.updateByExampleSelective(update, example);
		}else{
			
		}
		
		CWVMarketAuction auctionList = new CWVMarketAuction();
		auctionList.setBidId(Integer.parseInt(pb.getBidId()));
//		auctionList.setBidPrice(new BigDecimal(pb.getPrice()));
		auctionList.setCreateTime(new Date());
		auctionList.setUserId(1);
		if(retContract.getRetCode().equals("1"))
			auctionList.setStatus((byte) 1);
		else
			auctionList.setStatus((byte)0);
		daos.auctionDao.insert(auctionList);
		
		if(!retContract.getRetCode().equals("1")) {
			ret.setRetCode(ReturnCodeMsgEnum.EXCEPTION.getRetCode())
			.setRetMsg(ReturnCodeMsgEnum.EXCEPTION.getRetMsg());
			return;
		}
		
		//3更新竞拍信息 最高价 拥有者 更新时间
		bid.setBidAmount(new BigDecimal(pb.getPrice()));
		bid.setOwner(1);//用户
		bid.setBiddersCount(11);
		daos.bidDao.updateByPrimaryKeySelective(bid);
		
		//4设置竞价成功
		ret.setRetCode(ReturnCodeMsgEnum.PBS_SUCCESS.getRetCode())
		.setRetMsg(ReturnCodeMsgEnum.PBS_SUCCESS.getRetMsg());
		
	}

	/**
	 * 查询抽奖记录
	 * @param pb
	 * @param ret
	 */
	public void getPropertyDrawRecord(PSPropertyDrawRecord pb,
			PRetPropertyDrawRecord.Builder ret) {
		
		CWVMarketDrawExample example = new CWVMarketDrawExample();
		example.createCriteria().andUserIdEqualTo(1);//
		List<Object> list  = daos.drawDao.selectByExample(example);
		for (Object o :list){
			CWVMarketDraw draw = (CWVMarketDraw) o;
			PropertyDraw.Builder drawRet = PropertyDraw.newBuilder();
			drawRet.setDrawId(draw.getDrawId()+"");
			drawRet.setDrawTime(DateUtil.getDayTime(draw.getCreateTime()));
			drawRet.setPropertyId(draw.getPropertyId()+"");
		}
		
	}

	/**
	 * 抽奖房产
	 * @param pb
	 * @param ret
	 */
	public void drawProperty(PSCommonDraw pb, Builder ret) {
		//校验 
			//查询抽奖机会
		
			//抽奖
		
		
	}
		

}
