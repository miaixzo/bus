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

| 完成    | 提供商 | 描述信息               |
|-------|-----|--------------------|
| [ √ ] | 阿里  | 短信,邮件，语音已完成        |
| [ × ] | 腾讯  | 短信已完成,微信、企业微信、尚未完成 |
| [ × ] | 华为  | 短信已完成              |
| [ × ] | 京东  | 短信已完成              |
| [ × ] | 七牛  | 短信已完成              |
| [ × ] | 网易  | 短信已完成              |
| [ × ] | 云片  | 短信已完成              |
| [ × ] | 又拍  | 短信已完成              |
| [ × ] | 百度  | 短信已完成              |
| [ × ] | 钉钉  | 推送完成               |
