#### 项目说明

基于Spring boot的通知服务支持，支持多通道下的负载均衡 目前支持类型：阿里云短信、百度云短信、华为云短信、京东云短信、网易云信短信、腾讯云短信、七牛云短信、云片网短信、又拍云短信

![](https://img.shields.io/maven-central/v/net.guerlab.sms/guerlab-sms-server-starter.svg)
[![Build Status](https://travis-ci.org/guerlab-net/guerlab-sms.svg?branch=master)](https://travis-ci.org/guerlab-net/guerlab-sms)
![](https://img.shields.io/badge/LICENSE-LGPL--3.0-brightgreen.svg)

## Maven配置

```xml

<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-notify</artifactId>
    <version>x.x.x</version>
</dependency>
```

## 支持通道

| 完成    | 提供商                                                          | 描述信息               |
|-------|--------------------------------------------------------------|--------------------|
| [ √ ] | [阿里云](https://www.aliyun.com/product/sms)                    | 短信/邮件/语音           |
| [ √ ] | [百度云](https://cloud.baidu.com/product/sms.html)              | 短信                 |
| [ √ ] | [容联云](https://www.yuntongxun.com/sms/note-inform)            | 短信                 |
| [ √ ] | [天翼云](https://www.ctyun.cn/products/10020341)                | 短信                 |
| [ √ ] | [腾讯云](https://cloud.tencent.com/product/sms)                 | 短信                 |
| [ √ ] | [华为云](https://www.huaweicloud.com/product/msgsms.html)       | 短信                 |
| [ √ ] | [京东云](https://www.jdcloud.com/cn/products/text-message)      | 短信                 |
| [ √ ] | [七牛云](https://www.qiniu.com/products/sms)                    | 短信                 |
| [ √ ] | [网易云](https://netease.im/sms)                                | 短信                 |
| [ √ ] | [又拍云](https://www.upyun.com/products/sms)                    | 短信                 |
| [ √ ] | [亿美软通](https://www.emay.cn/article949.html)                  | 短信                 |
| [ √ ] | [助通短信](https://www.ztinfo.cn/products/sms)                   | 短信                 |
| [ √ ] | [合一短信](https://unisms.apistd.com/)                           | 短信                 |
| [ √ ] | [云片](https://www.yunpian.com/product/domestic-sms)           | 短信                 |
| [ √ ] | [微信](https://mp.weixin.qq.com/)                              | 小程序/企业微信/模版消息/微信客服 |
| [ √ ] | [钉钉](https://open.dingtalk.com/document/orgapp/api-overview) | 推送                 |
| [ √ ] | [JPush](https://docs.jiguang.cn/jpush)                       | 推送                 |
