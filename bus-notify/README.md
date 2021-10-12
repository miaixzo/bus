基于Spring boot的通知服务支持，支持多通道下的负载均衡 目前支持类型：阿里云短信、百度云短信、华为云短信、京东云短信、网易云信短信、腾讯云短信、七牛云短信、云片网短信、又拍云短信

![](https://img.shields.io/maven-central/v/net.guerlab.sms/guerlab-sms-server-starter.svg)
[![Build Status](https://travis-ci.org/guerlab-net/guerlab-sms.svg?branch=master)](https://travis-ci.org/guerlab-net/guerlab-sms)
![](https://img.shields.io/badge/LICENSE-LGPL--3.0-brightgreen.svg)

## Maven配置

```xml

<dependency>
    <groupId>org.aoju</groupId>
    <artifactId>bus-notify</artifactId>
    <version>6.2.9</version>
</dependency>
```

## 支持通道

| 完成 |提供商 | 描述信息 |
|------| ------ | ------- |
|[ √ ]|阿里云|短信,邮件，语音已完成| 
|[ × ]|腾讯云|无|
|[ × ]|华为云|无|
|[ × ]|京东云|无|
|[ × ]|七牛云|无|
|[ × ]|网易云信|云信推送、短信已完成|
|[ × ]|云片网|无|
|[ × ]|又拍云|无|
|[ × ]|百度云|无|
|[ × ]|钉钉|推送完成|