<p align="center">
	<a target="_blank" href="https://travis-ci.org/aoju/bus">
		<img src="https://travis-ci.org/aoju/bus.svg?branch=master">
	</a>
    <a target="_blank" href="http://maven.apache.org">
		<img src="https://img.shields.io/badge/Maven-3.5.0-yellowgreen.svg">
	</a>
	<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
		<img src="https://img.shields.io/badge/JDK-1.8+-green.svg">
	</a>
	<a target="_blank" href="https://spring.io/projects/spring-boot">
		<img src="https://img.shields.io/badge/Spring Boot-2.1.6-brightgreen.svg">
	</a>
	<a target="_blank" href="https://www.mysql.com">
		<img src="https://img.shields.io/badge/Mysql-5.7-blue.svg">
	</a>
	<a target="_blank" href="https://swagger.io">
		<img src="https://img.shields.io/badge/swagger-2.9.2-brightgreen.svg">
	</a>
	<a target="_blank" href="http://dubbo.apache.org">
		<img src="https://img.shields.io/badge/dubbo-2.6.6-yellow.svg">
	</a>
	<a target="_blank" href="http://poi.apache.org">
		<img src="https://img.shields.io/badge/poi-3.1.7-blue.svg">
	</a>
	<a target="_blank" href="http://github.com/aoju/bus.git">
		<img src="https://img.shields.io/badge/version-3.0.0-brightgreen.svg">
	</a>
	<a target="_blank" href="https://www.mit-license.org">
		<img src="https://img.shields.io/badge/license-MIT-green.svg">
	</a>
</p>

<p align="center">
	-- QQ群①：<a href="https://shang.qq.com/wpa/qunwpa?idkey=17fadd02891457034c6536c984f0d7db29b73ea14c9b86bba39ce18ed7a90e18">839128</a> --
	-- QQ群②：<a href="https://shang.qq.com/wpa/qunwpa?idkey=c207666cbc107d03d368bde8fc15605bb883ebc482e28d440de149e3e2217460">839120</a> --
</p>

---

## 项目说明
Bus 是一个微服务套件、基础框架，它基于Java8编写，参考、借鉴了大量已有框架、组件的设计，可以作为后端服务的开发基础中间件。代码简洁，架构清晰，非常适合学习使用。

很开心地告诉大家这套组件上手和学习难度非常小。如果是以学会使用为目的，只要你会Java语言即可。之前做项目的时候，往往会遇到各种各样的问题，这些问题有可能是会遇到很多次，不善于总结沉淀，这是很多人的一个通病，包括我自己也是。

于是我就萌生了把这些问题沉淀成组件的想法，分享自己成长路线,当然也参考了部分开源项目，资料，文章进行整合的一个提供基础功能的项目。
本项目旨在实现基础能力，不设计具体业务，希望能帮助到大家，也让大家见证我的勤奋与努力，一起进步。

欢迎大家来 这里 踩踩,生命有限！少写重复代码！给颗星奖励下呗~

目标期望能努力打造一套从 基础框架 - 分布式微服务架构 - 持续集成 - 自动化部署 -
系统监测的解决方案。

## 组件信息
|服务名 | 父级依赖 | 模块说明|
|----|----|----        |
|bus-all|无|为微服务提供统一的pom管理，以及通用组件| 
|bus-boot|无|springboot starter| 
|bus-base|无|基础功能及base相关功能|
|bus-cache|无|缓存服务及工具等|
|bus-core|无|核心功能及工具类等|
|bus-cron|无|定时器及定时任务等功能|
|bus-crypto|无|加密解密|
|bus-extra|无|扩展功能及文件操作|
|bus-fonts|无|PDF操作及输出字体信息|
|bus-health|无|应用服务器健康信息|
|bus-http|无|HTTP功能封装|
|bus-limiter|无|请求限流|
|bus-logger|无|日志信息及功能|
|bus-mapper|无|数据操作,mybatis|
|bus-pager|无|数据分页,mybatis|
|bus-poi|无|Excel处理|
|bus-sensitive|无|敏感数据脱敏|
|bus-setting|无|设置工具类， 用于支持设置/配置|
|bus-socket|无|基础NIO/AIO通讯|
|bus-spring|无|spring相关配置|
|bus-storage|无|存储公用工具类,qiniu,alioss等|
|bus-swagger|无|API调用及测试|
|bus-validate|无|参数校验|


## 功能概述


#### 功能概述
1. Java基础工具类，对文件、流、加密解密、转码、正则、线程、XML等JDK方法进行封装，组成各种Utils工具类；
   以及结合springboot封装常用工具按需加载例如mybatis、xss、i18n、sensitive、validate等框架

2. 详细说明请参考每个模块下README介绍
