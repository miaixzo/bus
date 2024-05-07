#### 项目说明

核心功能及工具类,包括常量、线程、类加载器、反射、集合、日期等常用工具

## 运行环境

要求java 17+

## 快速开始

- 引入依赖

```xml
<dependency>
    <groupId>org.miaixz</groupId>
    <artifactId>bus-core</artifactId>
    <version>x.x.x</version>
</dependency>
```

#### org.miaixz.bus.core.loader 资源加载器，充分拓展ClassLoader#getResources(name)的能力，实现递归加载，支持普通风格 / 包名风格 / ANT风格 / 正则风格路径的资源加载同时支持自定义过滤器，通常作为框架的基础类库

###### 由于ClassLoader#getResources(name)方法提供的资源加载能力非常有限，不支持递归和搜索的特性，但是开发一个框架往往需要按照约定或配置去加载一些当前项目的资源或者扫描指定包目录下的类，能提供非常便利的API以满足需求

## **功能特性**

* 纯Java的API，无第三方依赖
* 支持多种路径风格的资源加载
* 完全惰性加载，避免性能浪费
* 可自定义资源过滤器实现精确加载
* 底层支持file 和 jar 两个URL协议，满足绝大部分项目需求
* 传递URLStreamHandler，支持自定义的URLStreamHandler

## **示例代码**

```java
// 从当前的classpath中加载org/miaixz/bus/core/loader目录的资源，但不递归加载子目录
Loaders.std().load("org/miaixz/bus/core/loader");
```

```java
// 从当前的classpath中加载io目录的资源，而且递归加载子目录
Loaders.std().load("org", true);
```

```java
// 从当前的classpath中加载io以及所有递归子目录并且名称以Loader.class结尾的资源
Loaders.std().load("org", true, (name, url) -> name.endsWith("Loader.class"));
```

```java
// 上面的API默认采用的时当前线程的上下文类加载器，如果上下文类加载器不存在时则采用ClassLoader.getSystemClassLoader();
// 同时也可以采用下面的方式指定类加载器
Loaders.std(otherClassLoader);
```

```java
// 当加载某个package下的类资源时也要用路径的方式来写包名的话其实不太直观，这时可以采用pkg资源加载器
Loaders.pkg().load("org.miaixz.bus.core.loader"); // 不递归
Loaders.pkg().load("org.miaixz.bus.core.loader", true); // 递归
// 递归加载io.loadkit包下名称以Loader.class结尾的资源
Loaders.pkg().load("org.miaixz.bus.core.loader", (name, url) -> name.endsWith("Loader.class")); 
```

```java
// package资源加载器实际上是一个委派加载器，只是内部将包名转换成路径方式然后委派给实际的资源加载器
// 缺省情况下采用的是Loaders.std()资源加载器，也可以通过指定ClassLoader和delegate，实现更灵活的资源加载方式
Loaders.pkg(otherClassLoader);
Loaders.pkg(Loaders.std(otherClassLoader));
```

```java
// 在package资源加载器中要加载org.miaixz.bus.core.loader包下的名称Loader.class结尾的资源是需要自定义过滤器，
// 即便使用了Lambda表达式，但是采用ANT表达式就更简洁
// * 任意个字符，但不包括子目录
// ** 任意个字符，而且包括子目录
// ? 单个字符，可以使用多个表达多个字符，例如：load??? 可匹配loadkit 但不匹配loader
Loaders.ant().load("org/miaixz/bus/core/loader/*Loader.class");
```

```java
// 上面的ANT风格路径的资源加载器加载io.loadkit包的资源时，需要用 “/” 来分隔目录，用来加载包资源不太直观
// 这时可以采用delegate模式用package资源加载器去包装一个ANT资源加载器，
// 让资源加载同时拥有package 和 ant 两个加载器的解析能力
Loaders.pkg(Loaders.ant()).load("org.miaixz.bus.core.loader.*"); // 加载 org.miaixz.bus.core.loader.*
```

```java
// 有ANT表达式的资源加载器，自然也会有正则表达式的资源加载器。
Loaders.regex().load("org/miaixz/bus/core/loader/\\w+Loader.class"); // 加载 org.miaixz.bus.core.loader 包下名称以Loader.class 结尾的资源
```

```java
// Loader#load(); 方法只接收一个Filter参数，当过滤逻辑比较多而且不好写在一个过滤器，当然这样的类也是违背了"单一职责原则"的
Filters.and(Filter...filters); // AND 连接的混合过滤器
Filters.or(Filter...filters); // OR 连接的混合过滤器
// 来将多个子过滤器混合成一个过滤器，或者：
Filters.all(Filter...filters); // AND 连接的混合过滤器的另一种表达方式
Filters.any(Filter...filters); // OR 连接的混合过滤器的另一种表达方式
```

## ImageKit使用

ImageKit-图片合并功能使用起来相当简单，主要的类只用一个对象，指定背景图片和输出格式，然后加入各种素材元素，设置元素的位置、大小和效果（如圆角、颜色、透明度等），调用merge()
方法即可merge方法直接返回BufferedImage对象，getInputStream()获得流，方便上传oss等后续操作，或者调用out()方法保存到本地，调试的时候比较方便。

## 完整示例

```java
  public void demo()throws Exception{
        // 背景图
        String bgImageUrl="http://xxx.com/image/bg.jpg";
        // 二维码
        String qrCodeUrl="http://xxx.com/image/qrCode.png";
        // 商品图
        String itemUrl="http://xxx.com/image/item.jpg";
        // 水印图
        BufferedImage waterMark=ImageIO.read(new URL("https://xxx.com/image/mark.jpg"));
        // 头像
        BufferedImage avatar=ImageIO.read(new URL("https://xxx.com/image/avatar.jpg"));
        String title="# 最爱的家居";                                       //标题文本
        String content="“如果没有那个桌子，可能就没有那个水壶”";  //内容文本

        // 背景图（整个图片的宽高和相关计算依赖于背景图，所以背景图的大小是个基准）
        Image image=ImageKit.merge(bgImageUrl,FileType.TYPE_JPG);

        // 加图片元素（居中绘制，圆角，半透明）
        image.addImageElement(itemUrl,0,300)
        .setCenter(true)
        .setRoundCorner(60)
        .setAlpha(.8f);

        // 加文本元素
        image.addTextElement(title,60,100,960)
        .setColor(Color.red);
        // 合成图片
        image.merge();
        // 输出文件
        image.out("E://123.jpg");


        // 商品图（设置坐标、宽高和缩放模式，若按宽度缩放，则高度按比例自动计算）
        image.addImageElement(itemUrl,0,160,837,0,Scale.Mode.WIDTH)
        .setRoundCorner(46)     //设置圆角
        .setCenter(true);       //居中绘制，会忽略x坐标参数，改为自动计算

        // 标题（默认字体为阿里普惠、黑色，也可以自己指定Font对象）
        image.addTextElement(title,55,150,1400);

        // 内容（设置文本自动换行，需要指定最大宽度（超出则换行）、最大行数（超出则丢弃）、行高）
        image.addTextElement(content,"微软雅黑",40,150,1480)
        .setAutoBreakLine(837,2,60);

        // 头像（圆角设置一定的大小，可以把头像变成圆的）
        image.addImageElement(avatar,200,1200).setRoundCorner(200);

        // 水印（设置透明度，0.0~1.0）
        image.addImageElement(waterMark,630,1200).setAlpha(.8f);

        // 二维码（强制按指定宽度、高度缩放）
        image.addImageElement(qrCodeUrl,138,1707,186,186,Scale.Mode.OPTIONAL);

        // 元素对象也可以直接new，然后手动加入待绘制列表
        TextElement textPrice=new TextElement("￥1290",60,230,1300);
        // 红色
        textPrice.setColor(Color.red);
        // 删除线
        textPrice.setStrikeThrough(true);
        // 加入待绘制集合
        image.addElement(textPrice);

        // 执行图片合并
        image.merge();
        // 获取流（并上传oss等）
        InputStream is=image.getInputStream();
        // 输出文件
        image.out("E://topic.png");
        }
```

## 元素支持的特性

具体`ImageElement`和`TextElement`对象支持的特性如下表：

| 元素类型        | 特性    | 相关方法                                 |
| ---------      | ---------------------- | ----------------------------------------- |
| `ImageElement` | 图片     | `setImage()`,`setImgUrl()`              |
| `ImageElement` | 位置     | `setX()`,`setY()`                       |
| `ImageElement` | 缩放     | `setWidth()`,`setHeight()`,`ZoomMode`   |
| `ImageElement` | 圆角     | `setRoundCorner()`                      |
| `ImageElement` | 居中绘制 | `setCenter()`                           |
| `ImageElement` | 透明度   | `setAlpha()`                            |
| ----------------- |  |  |
| `TextElement`  | 文本     | `setText()`,`setY()`                    |
| `TextElement`  | 位置     | `setX()`,`setY()`                       |
| `TextElement`  | 居中绘制 | `setCenter()`                           |
| `TextElement`  | 透明度   | `setAlpha()`                            |
| `TextElement`  | 颜色     | `setColor()`                            |
| `TextElement`  | 字体     | `setFontName()`                         |
| `TextElement`  | 字号     | `setFontName()`                         |
| `TextElement`  | 删除线   | `setStrikeThrough()`                    |
| `TextElement`  | 自动换行 | `setAutoBreakLine()`                    |

注意：合成图片若包含文字的话，开发机和服务器要先安装相应的字体，否则看不出效果，默认使用的字体为“阿里普惠”
