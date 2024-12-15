#### 项目说明

本人项目中部分内容或方法来自来自justauth，如你所见，它仅仅是一个**第三方授权登录**的**工具类库**，它可以让我们脱离繁琐的第三方登录SDK，让登录变得
**So easy!**

## 特点

废话不多说，就俩字：

1. **全**：已集成十多家第三方平台(国内外常用的基本都已包含)，仍然还在持续扩展中！
2. **简**：API就是奔着最简单去设计的(见后面`快速开始`)，尽量让您用起来没有障碍感！

## 快速开始

- 引入依赖

```xml

<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-oauth</artifactId>
    <version>x.x.x</version>
</dependency>
```

- 调用api

```java
// 创建授权request
Provider provider = new GiteeProvider(Context.builder()
                .clientId("clientId")
                .clientSecret("clientSecret")
                .redirectUri("redirectUri")
                .build());
// 生成授权页面
        provider.

authorize("state");
// 授权登录后会返回code(auth_code(仅限支付宝))、state，1.8.0版本后，可以用Callback类作为回调接口的参数
// 注：默认保存state的时效为3分钟，3分钟内未使用则会自动清除过期的state
        provider.

login(callback);
```

### 获取授权链接

```
String authorizeUrl = shooting.authorize("state");
```

获取到`authorizeUrl`后，可以手动实现redirect到`authorizeUrl`上

**注：`state`建议必传！`state`在`OAuth`的流程中的主要作用就是保证请求完整性，防止**CSRF**风险，此处传的`state`将在回调时传回

### 登录(获取用户信息)

```
provider.login(callback);
```

授权登录后会返回code(auth_code(仅限支付宝)、authorization_code(仅限华为))、state，1.8.0版本后，用`AuthCallback`类作为回调接口的入参

**注：第三方平台中配置的授权回调地址，以本文为例，在创建授权应用时的回调地址应为：`[host]/callback/gitee`**

### 刷新token

注：`refresh`功能，并不是每个平台都支持

```
provider.refresh(AccToken.builder().refreshToken(token).build());
```

### 取消授权

注：`revoke`功能，并不是每个平台都支持

```
provider.revoke(AccToken.builder().accessToken(token).build());
```

#### API列表

|      平台       |                                                                         API                                                                         |                                                                                 SDK                                                                                 |
|:-------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|     gitee     |         [GiteeProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/GiteeProvider.java)         |                                            <a href="https://gitee.com/api/v5/oauth_doc#list_1" target="_blank">参考文档</a>                                             |
|    github     |        [GithubProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/GithubProvider.java)        |                          <a href="https://developer.github.com/apps/building-oauth-apps/authorizing-oauth-apps/" target="_blank">参考文档</a>                           |
|     weibo     |         [WeiboProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/WeiboProvider.java)         |                        <a href="https://open.weibo.com/wiki/%E6%8E%88%E6%9D%83%E6%9C%BA%E5%88%B6%E8%AF%B4%E6%98%8E" target="_blank">参考文档</a>                        |
|   dingtalk    |      [DingTalkProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/DingTalkProvider.java)      |                                     <a href="https://open-doc.dingtalk.com/microapp/serverapi2/kymkv6" target="_blank">参考文档</a>                                     |
|     baidu     |         [BaiduProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/BaiduProvider.java)         |                                    <a href="http://developer.baidu.com/wiki/index.php?title=docs/oauth" target="_blank">参考文档</a>                                    |
|    coding     |        [CodingProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/CodingProvider.java)        |                                                 <a href="https://open.coding.net/open-api" target="_blank">参考文档</a>                                                 |
| tencentCloud  |  [TencentCloudProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/TencentCloudProvider.java)  |                                     <a href="https://dev.tencent.com/help/doc/faq/b4e5b7aee786/oauth" target="_blank">参考文档</a>                                      |
|    oschina    |       [OschinaProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/OschinaProvider.java)       |                                      <a href="https://www.oschina.net/openapi/docs/oauth2_authorize" target="_blank">参考文档</a>                                       |
|    alipay     |        [AlipayProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/AlipayProvider.java)        |      <a href="https://alipay.open.taobao.com/docs/doc.htm?spm=a219a.7629140.0.0.336d4b70GUKXOl&treeId=193&articleId=105809&docType=1" target="_blank">参考文档</a>      |
|      qq       |            [QqProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/QqProvider.java)            |                  <a href="https://wiki.connect.qq.com/%E4%BD%BF%E7%94%A8authorization_code%E8%8E%B7%E5%8F%96access_token" target="_blank">参考文档</a>                  |
|    wechat     |        [WeChatProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/WeChatProvider.java)        | <a href="https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=&lang=zh_CN" target="_blank">参考文档</a> |
|    taobao     |        [TaobaoProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/TaobaoProvider.java)        |            <a href="https://open.taobao.com/doc.htm?spm=a219a.7386797.0.0.4e00669acnkQy6&source=search&docId=105590&docType=1" target="_blank">参考文档</a>             |
|    google     |        [GoogleProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/GoogleProvider.java)        |                                  <a href="https://developers.google.com/identity/protocols/OpenIDConnect" target="_blank">参考文档</a>                                  |
|   facebook    |      [FacebookProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/FacebookProvider.java)      |                         <a href="https://developers.facebook.com/docs/facebook-login/manually-build-a-login-flow" target="_blank">参考文档</a>                          |
|    douyin     |        [DouyinProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/DouyinProvider.java)        |                                           <a href="https://www.douyin.com/platform/doc/m-2-1-1" target="_blank">参考文档</a>                                            |
|   linkedin    |      [LinkedinProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/LinkedinProvider.java)      |         <a href="https://docs.microsoft.com/zh-cn/linkedin/shared/authentication/authorization-code-flow?context=linkedin/context" target="_blank">参考文档</a>         |
|   microsoft   |     [MicrosoftProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/MicrosoftProvider.java)     |                                             <a href="https://docs.microsoft.com/zh-cn/graph/" target="_blank">参考文档</a>                                              |
|      mi       |            [MiProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/MiProvider.java)            |                                          <a href="https://dev.mi.com/console/doc/detail?pId=711" target="_blank">参考文档</a>                                           |
|    toutiao    |       [ToutiaoProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/ToutiaoProvider.java)       |                                         <a href="https://open.mp.toutiao.com/#/resource?_k=y7mfgk" target="_blank">参考文档</a>                                         |
|  teambition   |    [TeambitionProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/TeambitionProvider.java)    |                                                   <a href="https://docs.teambition.com/" target="_blank">参考文档</a>                                                   |
|    renren     |        [RenrenProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/RenrenProvider.java)        |                                               <a href="http://open.renren.com/wiki/OAuth2.0" target="_blank">参考文档</a>                                               |
|   pinterest   |     [PinterestProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/PinterestProvider.java)     |                                        <a href="https://developers.pinterest.com/docs/api/overview" target="_blank">参考文档</a>                                        |
| stackoverflow | [StackOverflowProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/StackOverflowProvider.java) |                                        <a href="https://api.stackexchange.com/docs/authentication" target="_blank">参考文档</a>                                         |
|    huawei     |        [HuaweiProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/HuaweiProvider.java)        |                                  <a href="https://developer.huawei.com/consumer/cn/devservice/doc/30101" target="_blank">参考文档</a>                                   |
|    微信开放平台     |      [WeChatOPProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/WeChatOPProvider.java)      | <a href="https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1419316505&token=&lang=zh_CN" target="_blank">参考文档</a> |
|    微信公众平台     |      [WeChatMpProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/WeChatMpProvider.java)      |                  <a href="https://developers.weixin.qq.com/doc/offiaccount/OA_Web_Apps/Wechat_webpage_authorization.html" target="_blank">参考文档</a>                  |
|     企业微信      |      [WeChatEEProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/WeChatEEProvider.java)      |                                    <a href="https://open.work.weixin.qq.com/api/doc#90000/90135/90664" target="_blank">参考文档</a>                                     |
|    kujiale    |       [KujialeProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/KujialeProvider.java)       |                                       <a href="https://open.kujiale.com/open/apps/2/docs?doc_id=95" target="_blank">参考文档</a>                                        |
|    gitlab     |        [GitlabProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/GitlabProvider.java)        |                                            <a href="https://docs.gitlab.com/ee/api/oauth2.html" target="_blank">参考文档</a>                                            |
|    meituan    |       [MeituanProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/MeituanProvider.java)       |                                        <a href="http://open.waimai.meituan.com/openapi_docs/oauth/" target="_blank">参考文档</a>                                        |
|     eleme     |         [ElemeProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/ElemeProvider.java)         |                                        <a href="https://open.shop.ele.me/openapi/documents/khd001" target="_blank">参考文档</a>                                         |
|     csdn      |          [CsdnProvider](https://github.com/839128/bus/tree/master/bus-oauth/src/main/java/org/miaixz/bus/oauth/provider/CsdnProvider.java)          |                                                                                  无                                                                                  |

_请知悉：经咨询CSDN官方客服得知，CSDN的授权开放平台已经下线。如果以前申请过的应用，可以继续使用，但是不再支持申请新的应用,
本项目中的CSDN登录只能针对少部分用户使用了

# 关于OAuth

请先查阅以下资料：

- [The OAuth 2.0 Authorization Framework](https://tools.ietf.org/html/rfc6749)
- [OAuth 2.0](https://oauth.net/2/)

## OAuth 2的授权流程

### 参与的角色

- `Resource Owner` 资源所有者，即代表授权客户端访问本身资源信息的用户(User)，也就是应用场景中的“**开发者A**”
- `Resource Server` 资源服务器，托管受保护的**用户账号信息**，比如Github
- `Authorization Server` 授权服务器，**验证用户身份**然后为客户端派发资源访问令牌，比如Github
- `Resource Server`和`Authorization Server` 可以是同一台服务器，也可以是不同的服务器，视具体的授权平台而有所差异
- `Client` 客户端，即代表意图访问受限资源的**第三方应用**

### 授权流程

```
     +--------+                               +---------------+
     |        |--(A)- Authorization Request ->|   Resource    |
     |        |                               |     Owner     |
     |        |<-(B)-- Authorization Grant ---|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(C)-- Authorization Grant -->| Authorization |
     | Client |                               |     Server    |
     |        |<-(D)----- Access Token -------|               |
     |        |                               +---------------+
     |        |
     |        |                               +---------------+
     |        |--(E)----- Access Token ------>|    Resource   |
     |        |                               |     Server    |
     |        |<-(F)--- Protected Resource ---|               |
     +--------+                               +---------------+
```

上面的流程图取自[The OAuth 2.0 Authorization Framework#1.2](https://tools.ietf.org/html/rfc6749#section-1.2)

- (A)  用户打开**客户端**以后，**客户端**要求**用户**给予授权。
- (B)  **用户**同意给予**客户端**授权。
- (C)  **客户端**使用上一步获得的授权，向**认证服务器**申请令牌。
- (D)  **认证服务器**对**客户端**进行认证以后，确认无误，同意发放令牌
- (E)  **客户端**使用令牌，向**资源服务器**申请获取资源。
- (F)  **资源服务器**确认令牌无误，同意向**客户端**开放资源。

### 授权许可 `Authorization Grant`

- Authorization Code
    - 结合普通服务器端应用使用(**web**端常用的授权方式)
- Implicit
    - 结合移动应用或 Web App 使用
- Resource Owner Password Credentials
    - 适用于受信任客户端应用，例如同个组织的内部或外部应用
- Client Credentials
    - 适用于客户端调用主服务API型应用(比如百度API Store)

## 致谢

- [JustAuth](https://github.com/justauth/JustAuth): 第三方登录授权 SDK
