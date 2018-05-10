****
## 目录
* [房产交易列表](## 房产交易列表)
* [购买房产](## 购买房产)
* [卖出房产](## 卖出房产)
* [交易手续费](## 交易手续费)
* [个人房产交易](## 个人房产交易)
* [房产竞拍列表](## 房产竞拍列表)
* [竞拍房产](## 竞拍房产)
* [竞拍详情](## 竞拍详情)
* [公示详情](## 公示详情)
* [账户余额](## 账户余额)
* [账户列表](## 账户列表)
* [账户交易记录](## 账户交易记录)
* [钱包信息](## 钱包信息)
* [钱包充值](## 钱包充值)
* [抽奖房产](## 抽奖房产)
* [分享游戏](## 分享游戏)
* [游戏说明](## 游戏说明)
* [收益查询](## 收益查询)
* [收益领取](## 收益领取)
以下接口暂未处理
	* [撤销房产交易](## 撤销房产交易)
	* [个人竞拍记录](## 个人竞拍记录)
	* [查询抽奖记录](## 查询抽奖记录)


-----------
## 房产交易列表
### 接口说明
	空
### URL
	/cwv/ges/pbpes.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
price_type|string|价格排序 0降序 1升序|[0]
income_type|string|收益排序 0降序 1升序|
country_id|string|国家|
city_id|string|城市|
property_type|string|房产类型 <br/>1：价值房产<br/>2：功能型房产<br/>3：标志性房产|
property_name|string|房产名称|
page_index|string|页码|
page_size|string|数量|
    
	{
		"price_type":"1",
		"income_type":"0",
		"country_id":"1",
		"city_id":"1",
		"property_type":"1",
		"property_name":"Statue",
		"page_index":"1",
		"page_size":"10"
	}
	
#### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
exchange|object|交易对象数组|
exchange_id|string|交易ID|
price|string|卖出价格|
status|string|交易状态  0发起，1成功，2撤销|
property|object|交易对象中的房产对象|
country_id|string|所属国家|
map_id|string|所属地图|
property_template_id|string|房产模板ID|
property_template|string|房产模板|
owner|string|拥有者|
property_name|string|房产名称|
property_id|string|房产ID|
property_type|string|房产类型|
property_status|string|房产状态|
income_remark|string|收益说明|
income|string|收益|
image_url|string|房产图片地址|
page|object|分页对象|
page_index|string|页码|
page_size|string|数量|
total_count|string|总量|

	{
	    "ret_code": "01",
	    "ret_msg": "查询成功",
	    "exchange": [
	        {
	            "exchange_id": "1",
	            "price": 21312332.09,
	            "status": 0,
	            "property": {
	                "country_id": "1",
	                "map_id": "1",
	                "property_template_id": "51000",
	                "property_template": "51",
	                "owner": "kael",
	                "property_name": "the Statue of Liberty ",
	                "property_id": "1",
	                "property_type": 1,
	                "property_status": 0,
	                "income_remark": "收益说明123",
	                "income": 1010010,
	                "image_url": "wwww"
	            }
	        }
	    ],
	    "page": {
	        "page_index": "0",
	        "page_size": "10",
	        "total_count": "1"
	    }
	}

-----------

## 购买房产
### 接口说明
	
### URL
	/cwv/ges/pbbps.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
exchange_id|string|交易ID|
trade_pwd|string|交易密码|

	{
		"exchange_id":"1",
		"trade_pwd":"123456"
	}
	
### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.购买成功<br/>02.交易ID错误<br/>03.CWB不足<br/>04.该交易密码错误<br/>05.未设置交易密码<br/>06.该交易被成交或撤销<br/>99.未知异常|[01]
ret_msg|string|返回消息|
	
	{
	    "ret_code": "01",
	    "ret_msg": "购买成功"
	}

-----------

## 卖出房产
### 接口说明
	
### URL
	/cwv/ges/pbsps.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
propety_id|string|房产ID|
price|string|价格|
trade_pwd|string|交易密码|

	
	{
		"propety_id":"1",
		"price":"21312332.09",
		"trade_pwd":"123456"
	}
	
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.卖出成功<br/>02.房产ID错误<br/>03.房产状态错误<br/>04.房产价格式错误<br/>05.交易密码错误<br/>06.未设置交易密码<br/>99.未知异常|[01]
ret_msg|string|返回消息|

	{
	    "ret_code": "01",
	    "ret_msg": "卖出成功"
	}

-----------

## 交易手续费
### 接口说明
	
### URL
	/cwv/gga/pbgpc.do
### HTTP请求方式
	POST
### 输入参数
	无
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
charge_rate|string|手续费比例|
	
	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "charge_rate": 0.1
	}

-----------

## 个人房产交易
### 接口说明
	
### URL
	/cwv/gea/pbupe.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
price_type|string|价格排序 0降序 1升序|[0]
income_type|string|收益排序 0降序 1升序|
country_id|string|国家|
city_id|string|城市|
property_type|string|房产类型 <br/>1：价值房产<br/>2：功能型房产<br/>3：标志性房产|
property_name|string|房产名称|
page_index|string|页码|
page_size|string|数量|
    
	{
		"price_type":"1",
		"income_type":"0",
		"country_id":"1",
		"city_id":"1",
		"property_type":"1",
		"property_name":"Statue",
		"page_index":"1",
		"page_size":"10"
	}
	
#### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
exchange|object|交易对象数组|
exchange_id|string|交易ID|
price|string|卖出价格|
status|string|交易状态  0发起，1成功，2撤销|
property|object|交易对象中的房产对象|
country_id|string|所属国家|
map_id|string|所属地图|
property_template_id|string|房产模板ID|
property_template|string|房产模板|
owner|string|拥有者|
property_name|string|房产名称|
property_id|string|房产ID|
property_type|string|房产类型|
property_status|string|房产状态|
income_remark|string|收益说明|
income|string|收益|
image_url|string|房产图片地址|
page|object|分页对象|
page_index|string|页码|
page_size|string|数量|
total_count|string|总量|

	{
	    "ret_code": "01",
	    "ret_msg": "查询成功",
	    "exchange": [
	        {
	            "exchange_id": "1",
	            "price": 21312332.09,
	            "status": 0,
	            "property": {
	                "country_id": "1",
	                "map_id": "1",
	                "property_template_id": "51000",
	                "property_template": "51",
	                "owner": "kael",
	                "property_name": "the Statue of Liberty ",
	                "property_id": "1",
	                "property_type": 1,
	                "property_status": 0,
	                "income_remark": "收益说明123",
	                "income": 1010010,
	                "image_url": "wwww"
	            }
	        }
	    ],
	    "page": {
	        "page_index": "0",
	        "page_size": "10",
	        "total_count": "1"
	    }
	}

-----------

## 房产竞拍列表
### 接口说明
	
### URL
	/cwv/gba/pbpb.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
status|string|竞拍状态 0发起竞拍 1竞拍中 2竞拍完成|[0]
page_index|string|页码|
page_size|string|数量|

	{
		"status":"0",
		"page_index":"0",
		"page_size":"0",
	}
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
bid|object|竞拍对象数组|
bid_id|string|竞拍ID|
price|string|竞拍最终价格|
status|string|竞拍状态  0发起，1竞拍中 ，2完成|
property|object|竞拍对象中的房产对象|
country_id|string|所属国家|
map_id|string|所属地图|
property_template_id|string|房产模板ID|
property_template|string|房产模板|
owner|string|拥有者|
property_name|string|房产名称|
property_id|string|房产ID|
property_type|string|房产类型|
property_status|string|房产状态|
income_remark|string|收益说明|
image_url|string|房产图片地址|
page|object|分页对象|
page_index|string|页码|
page_size|string|数量|
total_count|string|总量|
	
	{
	    "ret_code": "01",
	    "ret_msg": "查询成功",
	    "bid": [
	        {
	            "bid_id": "2",
	            "auction_start": "2018-05-25 10:00:00",
	            "price": "null",
	            "status": "0",
	            "property": {
	                "country_id": "1",
	                "map_id": "1",
	                "property_template_id": "14007",
	                "property_template": "14",
	                "owner": "kael",
	                "property_name": "NYK 50 apartment",
	                "property_id": "51",
	                "property_type": 2,
	                "property_status": 1,
	                "income_remark": "收益说明123",
	                "image_url": "wwww"
	            }
	        }
	    ],
	    "page": {
	        "page_index": "1",
	        "page_size": "10",
	        "total_count": "1"
	    }
	}
-----------

## 竞拍房产 
### 接口说明
	
### URL
	/cwv/gba/pbaps.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
bid_id|string|竞拍ID|
price|string|价格|

	{
		"bid_id":"2",
		"price":"14000.0",	
	}
	
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|

	{
	    "ret_code": "01",
	    "ret_msg": "竞价成功"
	}
-----------

## 竞拍详情
### 接口说明
	
### URL
	/cwv/gba/pbpbd.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
bid_id|string|竞拍ID|

	{
		"bid_id":"2"
	}
		
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
property|object|竞拍对象中的房产对象|
country_id|string|所属国家|
map_id|string|所属地图|
property_template_id|string|房产模板ID|
property_template|string|房产模板|
owner|string|拥有者|
property_name|string|房产名称|
property_id|string|房产ID|
property_type|string|房产类型|
property_status|string|房产状态|
income_remark|string|收益说明|
image_url|string|房产图片地址|
bid|object|竞拍对象数组|
max_price|string|竞拍价格|
bidders_count|string|参与人数|
auction_start|string|开始时间|
auction_end|string|结束时间|
announce_time|string|公布时间|
bid_start|string|起拍价格|
	
	{
	    "ret_code": "01",
	    "ret_msg": "查询成功",
	    "property": {
	        "country_id": "1",
	        "map_id": "1",
	        "property_template_id": "14007",
	        "property_template": "14",
	        "property_name": "NYK 50 apartment",
	        "property_id": "51",
	        "property_type": 2,
	        "property_status": 1,
	        "income_remark": "收益说明"
	    },
	    "max_price": "14000.0000",
	    "bidders_count": "2",
	    "auction_start": "2018-05-25 10:00:00",
	    "auction_end": "2018-05-25 10:00:00",
	    "announce_time": "2018-05-26 10:00:00",
	    "bid_start": "11000.0000"
	}

-----------

## 公示详情
### 接口说明
	
### URL
	/cwv/gba/pbpbn.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
bid_id|string|竞拍ID|

	{
		"bid_id":"2"
	}
	
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
property|object|竞拍对象中的房产对象|
country_id|string|所属国家|
map_id|string|所属地图|
property_template_id|string|房产模板ID|
property_template|string|房产模板|
owner|string|拥有者|
property_name|string|房产名称|
property_id|string|房产ID|
property_status|string|房产状态|
income_remark|string|收益说明|
image_url|string|房产图片地址|
bid_price|string|竞拍价格|
auctionRank|object|竞拍人员对象数组|
nick_name|string|昵称|
bid_amount|string|竞拍额度|

	{
	    "ret_code": "01",
	    "ret_msg": "查询成功",
	    "property": {
	        "country_id": "1",
	        "map_id": "1",
	        "property_template_id": "14007",
	        "property_template": "14",
	        "property_name": "NYK 50 apartment",
	        "property_id": "51",
	        "property_type": 0,
	        "property_status": 0,
	        "income_remark": "收益说明",
	        "image_url": "asdasdad"
	    },
	    "bid_price": 14000,
	    "auctionRank": [
	        {
	            "nick_name": "18811879",
	            "bid_amount": "14000.0000"
	        },
	        {
	            "nick_name": "kael",
	            "bid_amount": "13000.0000"
	        }
	    ]
	}
-----------

## 账户余额
### 接口说明
	
### URL
	/cwv/gua/pbwab.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
coin_type|string|账户类型 0.CWB 1.CWV 2.ETH 默认为0 |[0]

	{ 
		"coin_type": "0"
	}
	
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
balance|string|余额|
	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "balance": 57543296.643
	}

-----------

## 账户交易记录
### 接口说明
	
### URL
	/cwv/gua/pbwrs.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
page_index|string|页码|[1]
page_size|string|数量|[10]

	{
		"page_index":"1",
		"page_size":"10"
	}
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
record|object|账户交易记录数组|
record_id|string|账户交易记录ID|
create_time|string|创建时间|
detail|string|交易明细|
amount|string|收支金额 正数：收入  负数：支出|
page|object|分页对象|
page_index|string|页码|
page_size|string|数量|
total_count|string|总量|
	
	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "record": [
	        {
	            "record_id": "5",
	            "create_time": "2018-05-04 08:36:39",
	            "detail": "买入房产",
	            "amount": "-21312332.0900"
	        },
	        {
	            "record_id": "9",
	            "create_time": "2018-05-07 11:31:12",
	            "detail": "竞拍房产",
	            "amount": "-3000.0000"
	        }
	    ],
	    "page": {
	        "page_index": "1",
	        "page_size": "10",
	        "total_count": "2"
	    }
	}


-----------

## 账户列表
### 接口说明
	
### URL
	/cwv/gua/pbwas.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
page_index|string|页码|[1]
page_size|string|数量|[10]

	{
		"page_index":"1",
		"page_size":"10"
	}
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
account|object|账户对象数组|
account_id|string|账户ID|
coin_type|string|币种类型|
market_price|string|价格CNY|
assert|string|资产|
icon|string|币种缩略图|
page|object|分页对象|
page_index|string|页码|
page_size|string|数量|
total_count|string|总量|
	
	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "account": [
	        {
	            "account_id": "1",
	            "coin_type": "0",
	            "market_price": 0,
	            "assert": 57543296.643,
	            "icon": "1"
	        }
	    ],
	    "page": {
	        "page_index": "0",
	        "page_size": "10",
	        "total_count": "1"
	    }
	}


-----------

## 钱包信息
### 接口说明
	
### URL
	/cwv/gua/pbwis.do
### HTTP请求方式
	POST
### 输入参数
	无
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
total_value_CNY|string|总额|
cwb_amount|string|CWB数量|
cwb_topup|string|充值数量|
draw_count|string|抽奖次数|

	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "total_value_CNY": 19872052.007459998,
	    "cwb_amount": 9936026003.73,
	    "cwb_topup": "10000000000.0000",
	    "draw_count": 111111
	}
    
-----------

## 钱包充值
### 接口说明
	
### URL
	/cwv/gua/pbwtu.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
coin_type|string|币种类型|[01]
amount|string|充值金额|

	{
		"amount":"1000",
		"coin_type":"0"
	}

### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
amount|string|总额|

	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "amount": 1000
	}
	
-----------

## 抽奖房产
### 接口说明
	
### URL
	/cwv/gda/pbdps.do
### HTTP请求方式
	POST
### 输入参数
	无
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>02.抽奖次数不足<br/>99.未知异常|[01]
ret_msg|string|返回消息|
property|object|房产对象|
country_id|string|所属国家|
map_id|string|所属地图|
property_template_id|string|房产模板ID|
property_template|string|房产模板|
owner|string|拥有者|
property_name|string|房产名称|
property_id|string|房产ID|
property_status|string|房产状态|
price|string|价格|
income_remark|string|收益说明|
image_url|string|房产图片地址|
	
	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "property": {
	        "country_id": "1",
	        "map_id": "1",
	        "property_template_id": "51000",
	        "property_template": "51",
	        "owner": "18811879",
	        "property_name": "the Statue of Liberty ",
	        "property_id": "1",
	        "property_type": 1,
	        "price": 0,
	        "income_remark": "收益说明",
	        "image_url": "asdadad"
	    }
	}
	
## 分享游戏
### 接口说明
	
### URL
	/cwv/gua/pbgss.do
### HTTP请求方式
	POST
### 输入参数
	无
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
url|string|分享链接|

	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "url": "/share_user=18"
	}
	
## 游戏说明
### 接口说明
	
### URL
	/cwv/gga/pbgis.do
### HTTP请求方式
	POST
### 输入参数
	无
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
introdcution|object|说明对象数组|
title|string|标题|
content|string|内容|
	
	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "introdcution": [
	        {
	            "title": "title1",
	            "content": "content1"
	        },
	        {
	            "title": "title2",
	            "content": "content2"
	        },
	        {
	            "title": "title3",
	            "content": "content3"
	        }
	    ]
	}
	
## 收益查询
### 接口说明
	
### URL
	/cwv/gua/pbpis.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
type|string|房产类型|

	{
		"type":"1"
	}
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.查询成功<br/>99.未知异常|[01]
ret_msg|string|返回消息|
income_total|string|普通房产价值（普通房产才有）|
next_income_time|string|下次派息时间|
property_type|string|房产类型 1普通房产 2标志性房产 3功能性房产|
income|object|收益对象|
income_id|string|收益ID|
coin_type|string|币种类型 0 CWB, 1 CWV, 2 ETH|
amount|string|收益金额|
propertyInfo|object|房产信息对象|
total_value|string|房产总价值（普通房产才有）|
	
	{
	    "ret_code": "01",
	    "ret_msg": "成功",
	    "income_total": "null",
	    "next_income_time": "2018-06-01",
	    "property_type": "1",
	    "income": {
	        "income_id": "1",
	        "coin_type": "0",
	        "amount": "10000.0000"
	    },
	    "propertyInfo": {
	        "total_value": "1000000.0"
	    }
	}
	
## 收益领取
### 接口说明
	
### URL
	/cwv/gua/pbpic.do
### HTTP请求方式
	POST
### 输入参数
参数|类型|说明|示例
:----|:----|:----|:----
income_id|string|收益ID|

	{
		"income_id":"1"
	}
	
### 输出参数
参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.领取成功<br/>02.收益ID错误<br/>03.收益已领取，无法重复领取<br/>99.未知异常|[01]
ret_msg|string|返回消息|
	
	{
	    "ret_code": "01",
	    "ret_msg": "领取成功"
	}