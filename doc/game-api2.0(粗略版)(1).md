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
* [钱包信息](## 钱包信息)
* [钱包充值](## 钱包充值)
* [抽奖房产](## 抽奖房产)
* [分享游戏](## 分享游戏)
* [游戏说明](## 游戏说明)

以下接口暂未处理
	* [历史收益查询](## 历史收益查询)
	* [领取收益](## 领取收益)
	* [个人收益房产列表查询](## 个人收益房产列表查询)
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
     
	{
		int32 price_type = 1; // 价格排序
		int32 income_type = 2; // 收益排序
		string country_id = 3; // 国家
		string city_id = 4; // 城市
		int32 property_type = 5; //房产类型 
		string property_name = 6;//房产名称
		int32 page_index = 7;//页索引
		int32 page_size = 8;//页大小
	}
	
#### 输出参数

	{
		string ret_code = 1; //返回状态码
		string ret_msg = 2; //提示信息
		repeated PropertyExchange exchange = 13; //交易数组
		message PropertyExchange{
			string exchange_id = 3; //交易ID
			double price = 8; // 价格
			int32 status = 10; // 交易状态 0发起，1成功，2撤销
			PRetProperty property = 13;//房产信息
			message PRetProperty{
				string country_id = 1;//所属国家
				string map_id = 2;//所属地图
				string property_template_id = 3;//房产模板Id
				string property_template = 4;//房产模板
				string owner = 5;//拥有者
				string property_name = 6; //房产名称
				string property_id = 7;//房产编码
				int32 property_type = 8;//房产类型
				int32 property_status = 9;//房产状态
				string income_remark = 10;//房产说明
				//string appearance_type = 11;//外观
				double income = 12;//收益
			}
		}
		
		PageOut page = 15; //分页对象
		message PageOut{
			string page_index = 1;//页码
			string page_size = 2;//数量
			string total_count = 3;//总量
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
	{
		string exchange_id = 1;//交易ID
		double amount = 2;//金额
		string trade_pwd = 3;//交易密码	
	}
### 输出参数

	{
		string ret_code = 1; //错误代码
		string ret_msg = 2; //提示信息
	}

-----------

## 卖出房产
### 接口说明
	
### URL
	/cwv/ges/pbsps.do
### HTTP请求方式
	POST
### 输入参数
	{
		string propety_id = 1;//房产ID
		double price = 2; //出售价格
		string trade_pwd = 3; //交易密码
		
	}
### 输出参数

	{
		string ret_code = 1; //错误代码
		string ret_msg = 2; //提示信息
		string exchange_id = 3; //交易接口
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

	{
		string ret_code = 1; //错误代码
		string ret_msg = 2; //提示信息
		double charge = 3; //手续费比例
	}

-----------

## 个人房产交易
### 接口说明
	
### URL
	/cwv/gea/pbupe.do
### HTTP请求方式
	POST
### 输入参数
	{
		int32 price_type = 1; // 价格排序
		int32 income_type = 2; // 收益排序
		string country_id = 3; // 国家
		string city_id = 4; // 城市
		int32 property_type = 5; //房产类型 
		string property_name = 6;//房产名称
		int32 page_index = 7;//页索引
		int32 page_size = 8;//页大小
	}
### 输出参数

	{
		string ret_code = 1; //返回状态码
		string ret_msg = 2; //提示信息
		repeated PropertyExchange exchange = 13;//交易数组
		message PropertyExchange{
			string exchange_id = 3; //交易ID
			double price = 8; // 价格
			int32 status = 10; // 交易状态 0发起，1成功，2撤销
			PRetProperty property = 13;//房产信息
			{
				string country_id = 1;//所属国家
				string map_id = 2;//所属地图
				string property_template_id = 3;//房产模板Id
				string property_template = 4;//房产模板
				string owner = 5;//拥有者
				string property_name = 6; //房产名称
				string property_id = 7;//房产编码
				int32 property_type = 8;//房产类型
				int32 property_status = 9;//房产状态
				string income_remark = 10;//房产说明
				//string appearance_type = 11;//外观
				double income = 12;//收益
			}
		}
		
		
		PageOut page = 15; //分页对象
		message PageOut{
			string page_index = 1;//页码
			string page_size = 2;//数量
			string total_count = 3;//总量
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
	{
		string property_status = 1;//房产状态 0发起竞拍 1竞拍中 2竞拍完成
		string bid_id = 2; //竞拍ID 查询单条时传入
		int32 page_index = 3;//页索引
		int32 page_size = 4;//页大小
	}
### 输出参数

	{
		string ret_code = 1; //返回状态码
		string ret_msg = 2; //提示信息
		repeated BidInfo bid = 3;//竞拍信息数组
		message BidInfo{//竞拍信息定义
			string bid_id = 1; //交易ID
			string auction_start = 2;//开始时间
			string price = 4;
			{
				string country_id = 1;//所属国家
				string map_id = 2;//所属地图
				string property_template_id = 3;//房产模板Id
				string property_template = 4;//房产模板
				string owner = 5;//拥有者
				string property_name = 6; //房产名称
				string property_id = 7;//房产编码
				int32 property_type = 8;//房产类型
				int32 property_status = 9;//房产状态
				string income_remark = 10;//房产说明
			}
				
		}
		
		PageOut page = 15; //分页对象
		message PageOut{
			string page_index = 1;//页码
			string page_size = 2;//数量
			string total_count = 3;//总量
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
	{
		string bid_id = 2; //竞拍ID
		double price  = 3; //价格
	}
### 输出参数

	{
		string ret_code = 1; //错误代码
		string ret_msg = 2; //提示信息
	}
-----------

## 预约竞拍详情
### 接口说明
	
### URL
	/cwv/user/pblin.do
### HTTP请求方式
	POST
### 输入参数
	{
		string exchange_id = 1;//交易ID
		double amount = 2;//金额
		
	}
### 输出参数

	{
		string ret_code = 1; //错误代码
		string ret_msg = 2; //提示信息
	}
-----------

## 竞拍详情
### 接口说明
	
### URL
	/cwv/gba/pbpbd.do
### HTTP请求方式
	POST
### 输入参数
	{
		string bid_id = 1;//竞拍ID
		
	}
### 输出参数

	{
		string country_id = 1;//所属国家
		string map_id = 2;//所属地图
		string property_template_id = 3;//房产模板Id
		string property_template = 4;//房产模板
		//string owner = 5;//拥有者
		string property_name = 6; //房产名称
		string property_id = 7;//房产编码
		int32 property_type = 8;//房产类型
		int32 property_status = 9;//房产状态
		string income_remark = 10;//房产说明
		
		string max_price = 12;//房产编码
		string bidders_count = 13;//当前参与人数
		string auction_start = 14;//开始时间
		string auction_end = 15;//结束时间
		string announce_time = 16;//公布时间
		string bid_start = 17;//最低喊价
	}

-----------

## 公示详情
### 接口说明
	
### URL
	/cwv/gba/pbpba.do
### HTTP请求方式
	POST
### 输入参数
	{
		string bid_id = 1;//竞拍ID
		
	}
### 输出参数
	{
		string country_id = 1;//所属国家
		string map_id = 2;//所属地图
		string property_template_id = 3;//房产模板Id
		string property_template = 4;//房产模板
		string owner = 5;//拥有者
		string property_name = 6; //房产名称
		string property_id = 7;//房产编码
		//int32 property_type = 8;//房产类型
		double price = 8;//房产类型
		//int32 property_status = 9;//房产状态
		string income_remark = 10;//房产说明
		repeated AuctionRank auctionRank = 11;//竞价排名数组
		message AuctionRank
		{
			string nick_name = 1;//昵称
			string bid_amount = 2;//竞拍额度
		}
		
	}
-----------

## 账户余额
### 接口说明
	
### URL
	/cwv/gua/pbwab.do
### HTTP请求方式
	POST
### 输入参数
	{ 
		string coin_type = 1;//币种类型
	}
### 输出参数

	{ 
		double balance = 1;//账户余额
	}

-----------

## 账户列表
### 接口说明
	
### URL
	/cwv/gua/pbwas.do
### HTTP请求方式
	POST
### 输入参数
	{
		int32 page_index = 4;//页索引
		int32 page_size = 5;//页大小
	}
### 输出参数

	{
		string ret_code = 1; //返回状态码
		string ret_msg = 2; //提示信息
		repeated WalletAccount account = 13;//钱包账户数组
		message WalletAccount{//钱包账户
			string account_id = 3; //账户ID
			string coin_type = 7; // 币种类型
			double market_price = 8; // 价格CNY
			double assert = 9;// 资产
			string icon = 11;//缩略图
		}
		
		
		PageOut page = 15; //分页对象
		message PageOut{
			string page_index = 1;//页码
			string page_size = 2;//数量
			string total_count = 3;//总量
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
	{
		string ret_code = 1; //返回状态码
		string ret_msg = 2; //提示信息
		double total_value_CNY = 3; //总额
		double cwb_amount = 4;//CWB数
		string cwb_topup = 5;//CWB充值
	}

    
-----------

## 钱包充值
### 接口说明
	
### URL
	/cwv/gua/pbwtu.do
### HTTP请求方式
	POST
### 输入参数
	无
### 输出参数
	{
		string ret_code = 1; //返回状态码
		string ret_msg = 2; //提示信息
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
	{
		string ret_code = 1; //错误编码
		string ret_msg = 2; //提示信息
		DrawProperty property = 3; //获得房产
		message DrawProperty
		{
			string country_id = 1;//所属国家
			string map_id = 2;//所属地图
		    string property_template_id = 3;//房产模板Id
		    string property_template = 4;//房产模板
		    string owner = 5;//拥有者
		    string property_name = 6; //房产名称
		    string property_id = 7;//房产编码
		    int32 property_type = 8;//房产类型
		    //int32 property_status = 9;//房产状态
		    double price = 9;//房产状态
			string income_remark = 10;//房产说明
		    //string appearance_type = 11;//外观
		    //double income = 12;//收益
		}
	
		PageOut page = 15; //分页对象
		message PageOut{
			string page_index = 1;//页码
			string page_size = 2;//数量
			string total_count = 3;//总量
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
	{
		string url = 1;	//分享链接
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
	{
		string title = 1;//标题
		string content = 2;//内容
	}