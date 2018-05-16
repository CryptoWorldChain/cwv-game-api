****
# 目录
* [发送验证码](#发送验证码)
* [获取图片验证码（Kaptcha）](#获取图片验证码（Kaptcha）)
* [短信验证码验证接口](#短信验证码验证接口)
* [图片验证码验证接口](#图片验证码验证接口)
* [获取国家列表](#获取国家列表)
* [用户注册](#用户注册)
* [用户登陆](#用户登陆)
* [重置登陆密码](#重置登陆密码)
* [修改登陆密码](#修改登陆密码)
* [设置交易密码](#设置交易密码)
* [设置用户昵称](#设置用户昵称)
* [设置用户头像](#设置用户头像)
* [校验access_token](#校验access_token)
* [刷新access_token](#刷新access_token)
* [用户注销](#用户注销)
* [获取游戏国家](#获取游戏国家)
* [获取游戏城市](#获取游戏城市)
* [获取游戏地图](#获取游戏地图)
* [获取游戏房产](#获取游戏房产)
* [获取公告消息](#获取公告消息)

-----------
## 发送验证码
### URL
	cwv/usr/pbsmc.do
### HTTP请求方式
	POST
### 输入参数
	     

	{
		"phone_code":"86", 
		"phone":"18512345678",
		"type":"1"
	}


	phone_code 手机号所属国家代码 86
	phone        手机号
	type         1:注册验证
			 2:交易密码
			 3:重置密码验证
			 4:设置登陆密码


### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|状态码<br/>01,发送成功！<br/>61,短信发送失败！ <br/>66,手机号错误！<br/>80,校验类错误信息<br/>99，未知异常|
ret_msg|string|状态说明|

	{
	"ret_code":"00", 
	"ret_msg":"success"
	}





-----------


## 获取图片验证码（Kaptcha）
### URL
	cwv/sms/pbmsg.do
### HTTP请求方式
	POST
### 输入参数
	     

	{
	"image_width":"135", 
	"image_height":"45",
	"char_length":"6"
	}


	image_width		图片宽度
	image_height		图片高度
	char_length		验证码个数


### 输出参数
-----------


## 短信验证码验证接口
### URL
	cwv/sms/pbver.do
### HTTP请求方式
	POST
### 输入参数
	     

	{
		"phone":"18512345678", 
		"code":"123456"
	}

	phone 	手机号
	code		验证码

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|状态码<br/>01,验证码验证成功！<br/>62没有短信验证记录，请重新发起短信验证！ <br/>63,有多条短信验证记录,请重新发起短信验证！<br/>64,验证码已过期，请重新发起短信验证！ <br/>65,短信验证码无效<br/>99，未知异常|
ret_msg|string|状态说明|

	{
	"ret_code":"01", 
	"ret_msg":"success"
	}
	
-----------


## 图片验证码验证接口
### URL
	cwv/sms/pbmsv.do
### HTTP请求方式
	POST
### 输入参数
	     

	{
	"code":"123456"
	}

	code		验证码

### 输出参数


	{
	"ret_code":"00", 
	"ret_msg":"success"
	}


	{
	"ret_code":"-1", 
	"ret_msg":"fails"
	}


	ret_code  	状态码
	ret_msg   	状态说明



-----------	  

## 获取国家列表
### URL
	cwv/nsd/pbcol.do
### HTTP请求方式
	POST
### 输入参数
	     

	{
	"short_name":"123456"，
	“page_index”：“1”，
	“page_size”：“10”，
	“is_page”：“1”
	}

	short_name		简称的模糊搜索
	page_index		页索引
	page_size		页大小
	is_page			是否分页 0:不分页 1:分页

### 输出参数


	{
	"ret_code":"00", 
	"ret_msg":"success"
	"countries": [
        {
            "domain_code": "AL1",
            "regin_code": "313",
            "short_name": "Albania",
            "full_name": "Albania",
            "desc_en": "Albania",
            "phone_code": "355"
        },
        {
            "domain_code": "AL2",
            "regin_code": "313",
            "short_name": "Albania",
            "full_name": "Albania",
            "desc_en": "Albania",
            "phone_code": "355"
        }
    ],
    "total_count": "11"
	}


	{
	"ret_code":"-1", 
	"ret_msg":"fails"
	}


	ret_code  	状态码
	ret_msg   	状态说明
	countries	国家信息集合
		domain_code		国际域名缩写
		regin_code		地区代码
		short_name		简称
		full_name		全称
		desc_en			英文描述
		phone_code		电话代码
	total_count	总条数 is_page值为0时，返回值为0
	
	
-----------	

## 用户注册
### 接口说明
	空
### URL
	/cwv/usr/pbreg.do
### HTTP请求方式
	POST
### 输入参数
     
|参数|类型|说明|示例|
|:----|:----|:----|:----|
|user_name|string|用户名(可选，默认为手机号)||
|nick_name|string|用户昵称(可选，默认为手机号)||
|phone_code|string|电话代码|86|
|phone|string|手机号|18512345678|
|password|string|用户密码||
|phone_verify_code|string|短信验证码|1234|
|reg_verify_code|string|注册验证码||
|country_code|number|国家编码||

	{
		"user_name":"test01",
		"phone_code":"86",
		"phone":"13161531208",
		"password":"123456",
		"phone_verify_code":"123456",
		"reg_verify_code":"123456",
		"country_code": "156"
	}
	
### 输出参数

|参数|类型|说明|示例|
|:----|:----|:----|:----|
|ret_code|string|注册结果状态码<br/>01.注册成功<br/>02.手机号已注册<br/>03.验证码错误<br/>04.短信验证码错误<br/>05.验证码已过期，请重新发起短信验证！<br/>80.校验类错误信息<br/>90.注册调用过于频繁<br/>99.未知异常|[01]|
|ret_msg|string|注册结果说明|成功|

	{
	    "ret_code": "01",
	    "ret_msg": "注册成功"
	}

-----------
     
## 用户登陆
### 接口说明
	本接口文档中所有access_token传入 更名为_smid
	
	用户登录成功后，返回access_token（_smid需要登陆的接口使用）,refresh_token（刷新_smid）,expires_in(_smid有效时间参考，具体以服务器为准)
	前端可以依据expires_in判断_smid(access_token)是否失效，失效时，可以使用refresh_token重新获取_smid(access_token)。
	需要校验登陆的请求，应在HTTP Header中增加Cookie节点， 格式为<_smid>=<token值>。

### URL
	/cwv/usr/pblin.do
### HTTP请求方式
	POST
### 输入参数

|参数|类型|说明|示例|
|:----|:----|:----|:----|
|phone|string|手机号|18512345678|
|password|string|登录密码||
|phone_verify_code|string|登录验证码|1234|

	{
		"phone":"13161531208",
		"password":"123456",
		"phone_verify_code":"123456"
	}

### 输出参数

|参数|类型|说明|示例|
|:----|:----|:----|:----|
|ret_code|string|登录结果状态码<br/>01.登录成功<br/>02.手机号或密码错误<br/>03.验证码错误<br/>99.未知异常|[01]|
|ret_msg|string|登录结果说明||
|token_type|string|token类型，默认为bearer||
|access_token|string|用户标识||
|expires_in|number|access_token的有效期，以秒为单位|3600|
|refresh_token|string|用于刷新access_token||
|user_info|Object|||
|uid|string|用户标识||
|nick_name|string|昵称||
|phone|string|手机号||
|image_url|string|用户头像||
|is_supper|string|是否超级玩家  0不是，1是||

	{
	    "ret_code": "01",
	    "ret_msg": "登录成功",
	    "token_type": "JWT",
	    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1MjM2QSIsInN1YiI6IntcInVpZFwiOjEsXCJ1dHlcIjpcImthZWxcIn0iLCJpYXQiOjE1MjI1OTkwNTgsImV4cCI6MTUyMjU5OTY1OH0.ql3MZht8KyJFpODLqcQSDEKN03_1Pgae44QjZatMwDg",
	    "expires_in": 600,
	    "refresh_token": "40288a8400002d14016281f9db410001",
	    "user_info": {
	        "uid": "1",
	        "nick_name": "kael",
	        "phone": "13161531208",
	        "image_url": "/cwv/user/pbghi.do",
	        "trade_pwd_set": "1",
	        "account_balance": "10000.00"
	         "is_supper": "0"
	    }
	}


## 重置登陆密码
### 接口说明
	空

### URL
	/cwv/usr/pbrsp.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
phone|string|手机号|18512345678
password|string|新登录密码|
phone_verify_code|String|手机验证码|1234

	{
		"phone":"13161531208",
		"password":"123456",
		"phone_verify_code":"123456"
	}

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.重置密码成功<br/>02.验证码无效<br/>03 手机号无效<br/>04.次数超限<br/>05.短信验证码错误<br/>80.校验类错误<br/>99.未知异常|[01]
ret_msg|string|返回消息|


	{
	    "ret_code": "01",
	    "ret_msg": "重置密码成功"
	}

## 修改登陆密码
### 接口说明
	空

### URL
	/cwv/usr/pbsps.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
password|string|新登录密码|
password_old|string|新登录密码|
phone_verify_code|string|短信验证码|

	{
		"password":"123456",
		"password_old":"123456",
		"phone_verify_code":"3456"
	}

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.修改密码成功<br/>02.旧密码错误<br/>03.新密码与原密码相同<br/>04.短信验证码无效<br/>05.次数超限<br/>80.校验类错误<br/>99.未知异常|[01]
ret_msg|string|返回消息|


	{
	    "ret_code": "01",
	    "ret_msg": "修改密码成功"
	}
		
## 设置交易密码
### 接口说明
	空

### URL
	/cwv/usr/pbstp.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
password|string|交易密码|
password_old|string|旧交易密码|
phone_verify_code|string|短信验证码|

	{
		"password":"123456",
		"password_old":"133456",
		"phone_verify_code":"3456"
	}

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.设置交易密码成功<br/>02.旧密码错误<br/>03.新密码与原密码相同<br/>04.短信验证码无效<br/>05.次数超限<br/>80.校验类错误<br/>99.未知异常|[01]
ret_msg|string|返回消息|


	{
	    "ret_code": "01",
	    "ret_msg": "设置交易密码成功"
	}	
	
## 设置用户昵称
### 接口说明
	空

### URL
	/cwv/usr/pbsnn.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
nick_name|string|昵称|

	{
		"password":"123456"
	}

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.设置昵称成功<br/>80.校验类错误信息<br/>99.未知异常|[01]
ret_msg|string|返回消息|


	{
	    "ret_code": "01",
	    "ret_msg": "设置昵称成功"
	}	
	
## 设置用户头像
### 接口说明
	空

### URL
	/cwv/usr/pbshi.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
filedata|string|图片数据|base64编码

	{
		"password":"123456"
	}

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.设置昵称成功<br/>80.校验类错误信息<br/>99.未知异常|[01]
ret_msg|string|返回消息|
image_url|string|用户头像地址|


	{
	    "ret_code": "01",
	    "ret_msg": "设置头像成功"
	}	
	

## 校验access_token
### 接口说明
	前端校验token方式：
		范围：需要登陆验证token的接口
		方式：请求头header中加入Cookie键值对：<_smid>=<token>
		举例：_smid=eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1MjM2QSIsInN1YiI6IntcInVpZFwiOjEsXCJ1dHlcIjpcImthZWxcIn0iLCJpYXQiOjE1MjI2NDMxODksImV4cCI6MTUyMjY0Mzc4OX0.WyRmJiNHpfUm75r89vw1N0BSEYViWkUAvVboTA65-Kc

### URL
	/cwv/tkn/pbats.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
access_token|string|用户标识|

	{
		"access_token":"eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1MjM2QSIsInN1YiI6IntcInVpZFwiOjEsXCJ1dHlcIjpcImthZWxcIn0iLCJpYXQiOjE1MjI1NTE5NTksImV4cCI6MTUyMjU1MjU1OX0.97YIW-IGIQ26Ekz4nChXli8qLc-C_VICXk3wCGJMHrU"
	}

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.校验通过<br/>02.access_token无效<br/>99.未知异常|[01]
ret_msg|string|返回消息|
image_url|string|用户头像地址|


	{
	    "ret_code": "01",
	    "ret_msg": "校验通过"
	}	

## 刷新access_token
### 接口说明
	当用户的access_token失效后(未失效时也可刷新)，应使用refresh_token重新获取access_token和refresh_token，并更新本地存储。
	如果重新获取仍失败(ret_code=02)，则应重新登录。
	一旦刷新access_token，原有的access_token和refresh_token立即失效。

### URL
	/cwv/tkn/pbrts.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
refresh_token|string|登录时获取的refresh_token|

	{
		"refresh_token": "40288a84000051f40162816a7bcc0002"
	}


### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.刷新token成功<br/>02.refresh_token无效<br/>99.未知异常|[01]
ret_msg|String|返回消息|
token_type|string|token类型|
access_token|string|用户标识|
expires_in|number|access_token的有效期，以秒为单位|3600
refresh_token|string|用于刷新access_token|

	{
	    "ret_code": "01",
	    "ret_msg": "刷新token成功",
	    "token_type": "JWT",
	    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI1MjM2QSIsInN1YiI6IntcInVpZFwiOjEsXCJ1dHlcIjpcImthZWxcIn0iLCJpYXQiOjE1MjI1OTQyMzYsImV4cCI6MTUyMjU5NDgzNn0.MCJbjfvdEVK7lYORx75qHsYzvkB-YH_80npGJeIAlZ8",
	    "expires_in": "600",
	    "refresh_token": "40288a84000051f4016281b048f60005"
	}
	
## 用户注销
### 接口说明
	注销成功后，access_token和refresh_token立即失效。

### URL
	/cwv/usr/pblout.do
### HTTP请求方式
	POST
### 输入参数
	空

### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.设置昵称成功<br/>80.校验类错误信息<br/>99.未知异常|[01]
ret_msg|string|返回消息|


	{
	    "ret_code": "01",
	    "ret_msg": "注销成功"
	}	

## 获取游戏国家
### 接口说明
	国家界面获取国家列表。

### URL
	/cwv/gga/pbgcs.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
shot_name|string|简称的模糊搜索|
page_index|string|页索引|
page_size|string|页大小|
is_page|string|是否分页，0：不分页，1分页|

	{
		"shot_name": "USA",
		"page_index": "1",
		"page_size": "10",
		"is_page": "1"
	}


### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.成功<br/>99.未知异常|[01]
ret_msg|String|返回消息|
total_count|string|总条数|
countries|array||
country_id|string|游戏国家编码||
country_name|string|国家名称|
map_number|string|地图数量||

	{
	    "ret_code": "01",
	    "ret_msg": "获取成功",
	    "total_count": "16",
	    "countries": [{
	    		"country_id": "1",
			    "country_name": "USA",
			    "map_number": "1780"
	    },
	    ...
	    ,
	    {
	    		"country_id": "2",
			    "country_name": "CHINA",
			    "map_number": "1920"
	    }]
	    
	}

## 获取游戏城市
### 接口说明
	城市界面获取城市列表。

### URL
	/cwv/gga/pbgcc.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
shot_name|string|简称的模糊搜索|
page_index|string|页索引|
page_size|string|页大小|
is_page|string|是否分页，0：不分页，1分页|
country_id|string|国家编码|

	{
		"shot_name": "New",
		"page_index": "1",
		"page_size": "10",
		"is_page": "1",
		"country_id": "1"
	}


### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.成功<br/>99.未知异常|[01]
ret_msg|String|返回消息|
total_count|string|总条数|
cities|array||
country_id|string|游戏国家编码|
city_id|string|游戏城市编码|
city_name|string|城市名称|
map_number|string|地图数量||

	{
	    "ret_code": "01",
	    "ret_msg": "获取成功",
	    "total_count": "16",
	    "countries": [{
	    		"country_id": "1",
	    		"city_id": "1",
			   "country_name": "New York",
			   "map_number": "80"
	    },
	    ...
	    ,
	    {
	    		"country_id": "1",
	    		"city_id": "2",
			   "country_name": "New XXX",
			   "map_number": "90"
	    }]
	    
	}
	
## 获取游戏地图
### 接口说明
	地图界面获取地图列表。

### URL
	/cwv/gga/pbgcm.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
shot_name|string|简称的模糊搜索|
page_index|string|页索引|
page_size|string|页大小|
is_page|string|是否分页，0：不分页，1分页|
city_id|string|城市编码|

	{
		"shot_name": "高层",
		"page_index": "1",
		"page_size": "10",
		"is_page": "1",
		"city_id": "1"
	}


### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.成功<br/>99.未知异常|[01]
ret_msg|String|返回消息|
total_count|string|总条数|
maps|array||
city_id|string|游戏城市编码|
map_id|string|地图编号|
map_name|string|地图名称|
property_count|string|房产总数|
property_sell_count|string|房产已售出数量|
average_price|string|已售出的房产平均价|
tamplate|string|模型||
		
	{
	    "ret_code": "01",
	    "ret_msg": "获取成功",
	    "total_count": "16",
	    "countries": [{
	    		"city_id": "1",
	    		"map_id": "1",
			   "map_name": "高层住宅2031",
			   "property_count": "100",
			   "property_sell_count": "80",
			   "average_price": "5000000",
			   "tamplate": "1"
	    },
	    ...
	    ,
	    {
	    		"city_id": "1",
	    		"map_id": "2",
			   "map_name": "高层住宅2032",
			   "property_count": "100",
			   "property_sell_count": "80",
			   "average_price": "5000000",
			   "tamplate": "1"
	    }]
	    
	}

## 获取游戏房产
### 接口说明
	房产界面获取房产列表。

### URL
	/cwv/gga/pbgmp.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
page_index|string|页索引|
page_size|string|页大小|
is_page|string|是否分页，0：不分页，1分页|
map_id|string|所属地图|
property_name|string|房产名称的模糊搜索|
property_type|string|房产类型，1：价值房产，2：功能型房产，3：标志性房产|
property_status|string|房产状态，0：未出售，1：竞拍中，2：出售中，3：已出售|

	{
		"page_index": "1",
		"page_size": "10",
		"is_page": "1",
		"map_id": "1",
		"property_name": "帝国",
		"property_type": "1",
		"property_status": "1"
	}


### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.成功<br/>99.未知异常|[01]
ret_msg|String|返回消息|
total_count|string|总条数|
properties|array||
map_id|string|地图编码|
property_id|string|房产编号|
property_name|string|房产名称|
property_type|string|房产类型|
property_status|string|房产状态|
appearance_type|string|外观||
	
	{
	    "ret_code": "01",
	    "ret_msg": "获取成功",
	    "total_count": "16",
	    "countries": [{
	    		"map_id": "1",
	    		"property_id": "1",
			   "property_name": "帝国大厦1",
			   "property_type": "1",
			   "property_status": "1",
			   "appearance_type": "1"
	    },
	    ...
	    ,
	    {
	    		"map_id": "1",
	    		"property_id": "2",
			   "property_name": "帝国大厦2",
			   "property_type": "1",
			   "property_status": "1",
			   "appearance_type": "1"
	    }]
	    
	}
	
## 获取公告消息
### 接口说明
	公告栏展示公告消息。

### URL
	/cwv/gns/pbgno.do
### HTTP请求方式
	POST
### 输入参数

参数|类型|说明|示例
:----|:----|:----|:----
page_index|string|页索引|
page_size|string|页大小|
page_num|string|页数|
user_id|string|用户id（暂时可不传）|
notice_type|string|公告类型（目前只支持announcement类型）|announcement

	{
		"page_index": "1",
		"page_size": "10",
		"page_num": "1",
		"user_id": "1",
		"notice_type": "announcement"
	}


### 输出参数

参数|类型|说明|示例
:----|:----|:----|:----
ret_code|string|返回状态码<br/>01.成功<br/>99.未知异常|[01]
ret_msg|String|返回消息|
notices|array||
notice_type|string|消息类型|
notice_content|string|消息内容||
start_time|string|轮播开始时间||
end_time|string|轮播结束时间||
cycle_period|number|循环周期||
count|number|循环次数||
	
	{
	    "ret_code": "01",
	    "ret_msg": "SUCCESS",
	    "notices": [
	        {
	            "notice_type": "announcement",
	            "notice_content": "llalalalallalal",
	            "start_time": "2018-04-07",
	            "end_time": "2018-04-07",
	            "cycle_period": 5000,
	            "count": 1
	        }
	    ]
	}
