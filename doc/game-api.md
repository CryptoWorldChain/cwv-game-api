****
## 目录
* 1.发送验证码
* 2.发送验证码



-----------
# 1.发送验证码
## URL
	cwv/sms/pbaut.do
## HTTP请求方式
	POST
## 输入参数
	     
```
{
	"country_code":"86", 
	"phone":"18512345678",
	"type":"1"
}
```
```
	country_code 手机号所属国家代码
	phone        手机号
	type         1:注册验证
				 2:登陆验证(不可用)
				 3:重置密码验证
				 4:修改信息验证[需传递用户身份]

```
## 输出参数

```
{
	"ret_code":"00", 
	"ret_msg":"success"
}
```
```
{
	"ret_code":"-1", 
	"ret_msg":"fails"
}
```
```
	ret_code  状态码
	ret_msg   状态说明
```


-----------


# 发送验证码
## URL
	cwv/sms/pbaut.do
## HTTP请求方式
	POST
## 输入参数
	     
```
{
	"country_code":"86", 
	"phone":"18512345678",
	"type":"1"
}
```
```
	country_code 手机号所属国家代码
	phone        手机号
	type         1:注册验证
				 2:登陆验证(不可用)
				 3:重置密码验证
				 4:修改信息验证[需传递用户身份]

```
## 输出参数

```
{
	"ret_code":"00", 
	"ret_msg":"success"
}
```
```
{
	"ret_code":"-1", 
	"ret_msg":"fails"
}
```
```
	ret_code  状态码
	ret_msg   状态说明
```
	      
