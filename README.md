> 注：数据接口已不可用，项目不再维护 -- 2021.10.21

# 前言

因为有一些小说在软件上搜不到，就萌生了自己动手开发一个小说软件的念头

关于数据接口：网上搜索了一下，其实是有免费提供的，但提供的不完全，有一些想要的数据部分并没有提供到，因此我是使用Python + Flask搭建的爬虫项目

项目采用的是MVP架构，其中最核心的部分就在于阅读视图（ReadView）,需要实现翻页，缓存，自动加载等功能，弄了挺久~

# 第三方库

OkHttp + Retrofit 实现网络请求

RxJava 实现 异步回调

Glide 实现图片加载

Basequickadapter RV万能适配器 

&& 其他...

# 界面展示

|Column1|Column2|Column3|Column4|
|---|---|---|---|
|![pic1](http://static.jqwong.cn/image/1.jpg)|![pic2](http://static.jqwong.cn/image/2.png)|![pic3](http://static.jqwong.cn/image/3.png)|![pic4](http://static.jqwong.cn/image/4.png)|
|![pic5](http://static.jqwong.cn/image/5.jpg)|![pic6](http://static.jqwong.cn/image/6.jpg)|![pic7](http://static.jqwong.cn/image/7.jpg)|![pic8](http://static.jqwong.cn/image/8.jpg)|


# 反馈

### 2021.02.16

最近，数据接口访问都报500，今天研究了一天，终于恢复正常了

HTTP 500，都是服务器的问题，我们要去看错误日志，关于UWSGI的LOG

最后查出来的原因是：访问速度过快，导致了频繁访问。

我们只要将捕捉错误就可以，有错误就延时0.1s
```
import time
@app.route("route")
def def():
    flag = True
    while(flag):
        try:
            ...
            if(...):
                flag = False
        except requests.exceptions.ConnectionError:
            time.sleep(0.1)
    return ...
```
延时，也导致了速度没有原来那样飞快了~，但也避免了500


### 2021.02.06

接口数据突然访问地飞快，返回数据在1s~2s左右

怎么回事...我好像什么都没做


### 2020.12.31

出现机型：XiaoMi Mix2s -- Android 8

1.  Loading 动画同步，没有延时
    
2.  PageIndicator 无显示
    
3.  ReadActvitiy InfoDialog 没有覆盖全屏
