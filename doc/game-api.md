


-----------
# 发送验证码
## URL
	cwv/sms/pbaut.do
## HTTP请求方式
	POST
## 输入参数
	     

	{
	"country_code":"86", 
	"phone":"18512345678",
	"type":"1"
	}


	country_code 手机号所属国家代码
	phone        手机号
	type         1:注册验证
				 2:登陆验证(不可用)
				 3:重置密码验证
				 4:修改信息验证[需传递用户身份]


## 输出参数


	{
	"ret_code":"00", 
	"ret_msg":"success"
	}


	{
	"ret_code":"-1", 
	"ret_msg":"fails"
	}


	ret_code  状态码
	ret_msg   状态说明



-----------


# 获取图片验证码（Kaptcha）
## URL
	cwv/sms/pbmsg.do
## HTTP请求方式
	POST
## 输入参数
	     

	{
	"image_width":"135", 
	"image_height":"45",
	"char_length":"6"
	}


	image_width		图片宽度
	image_height		图片高度
	char_length		验证码个数


## 输出参数
-----------


# 短信验证码验证接口
## URL
	cwv/sms/pbver.do
## HTTP请求方式
	POST
## 输入参数
	     

	{
	"phone":"18512345678", 
	"code":"123456"
	}

	phone 	手机号
	code		验证码

## 输出参数


	{
	"ret_code":"00", 
	"ret_msg":"success"
	}


	{
	"ret_code":"-1", 
	"ret_msg":"fails"
	}


	ret_code  	状态码
				没有短信验证记录，请重新发起短信验证 02
				有多条短信验证记录,请重新发起短信验证！ 03
				验证码已过期，请重新发起短信验证！ 04
	ret_msg   状态说明
-----------


# 图片验证码验证接口
## URL
	cwv/sms/pbmsv.do
## HTTP请求方式
	POST
## 输入参数
	     

	{
	"code":"123456"
	}

	code		验证码

## 输出参数


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

# 获取国家列表
## URL
	cwv/nsd/pbcol.do
## HTTP请求方式
	POST
## 输入参数
	     

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

## 输出参数


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
