#### org.aoju.bus.core.loader 资源加载器，充分拓展ClassLoader#getResources(name)的能力，实现递归加载，支持普通风格 / 包名风格 / ANT风格 / 正则风格路径的资源加载同时支持自定义过滤器，通常作为框架的基础类库。

###### 由于ClassLoader#getResources(name)方法提供的资源加载能力非常有限，不支持递归和搜索的特性，但是开发一个框架往往需要按照约定或配置去加载一些当前项目的资源或者扫描指定包目录下的类，能提供非常便利的API以满足需求。

## **功能特性**
* 纯JDK的API，无第三方依赖。
* 支持多种路径风格的资源加载。
* 完全惰性加载，避免性能浪费。
* 可自定义资源过滤器实现精确加载。
* 底层支持file 和 jar 两个URL协议，满足绝大部分项目需求。
* 传递URLStreamHandler，支持自定义的URLStreamHandler。

## **示例代码**
```java
// 从当前的classpath中加载io/loadkit目录的资源，但不递归加载子目录。
Loaders.std().load("io/loadkit");
```

```java
// 从当前的classpath中加载io目录的资源，而且递归加载子目录。
Loaders.std().load("io", true);
```

```java
// 从当前的classpath中加载io以及所有递归子目录并且名称以Loader.class结尾的资源。
Loaders.std().load("io", true, (name, url) -> name.endsWith("Loader.class"));
```

```java
// 上面的API默认采用的时当前线程的上下文类加载器，如果上下文类加载器不存在时则采用ClassLoader.getSystemClassLoader();
// 同时也可以采用下面的方式指定类加载器。
Loaders.std(otherClassLoader);
```

```java
// 当加载某个package下的类资源时也要用路径的方式来写包名的话其实不太直观，这时可以采用pkg资源加载器。
Loaders.pkg().load("io.loadkit"); // 不递归
Loaders.pkg().load("io.loadkit", true); // 递归
// 递归加载io.loadkit包下名称以Loader.class结尾的资源
Loaders.pkg().load("io.loadkit", (name, url) -> name.endsWith("Loader.class")); 
```

```java
// package资源加载器实际上是一个委派加载器，只是内部将包名转换成路径方式然后委派给实际的资源加载器。
// 缺省情况下采用的是Loaders.std()资源加载器，也可以通过指定ClassLoader和delegate，实现更灵活的资源加载方式。
Loaders.pkg(otherClassLoader);
Loaders.pkg(Loaders.std(otherClassLoader));
```

```java
// 在package资源加载器中要加载io.loadkit包下的名称Loader.class结尾的资源是需要自定义过滤器，
// 即便使用了Lambda表达式，但是采用ANT表达式就更简洁。
// * 任意个字符，但不包括子目录
// ** 任意个字符，而且包括子目录
// ? 单个字符，可以使用多个表达多个字符，例如：load??? 可匹配loadkit 但不匹配loader
Loaders.ant().load("io/loadkit/*Loader.class");
```

```java
// 上面的ANT风格路径的资源加载器加载io.loadkit包的资源时，需要用 “/” 来分隔目录，用来加载包资源不太直观。
// 这时可以采用delegate模式用package资源加载器去包装一个ANT资源加载器，
// 让资源加载同时拥有package 和 ant 两个加载器的解析能力。
Loaders.pkg(Loaders.ant()).load("io.loadkit.*"); // 加载 io.loadkit.*
```

```java
// 有ANT表达式的资源加载器，自然也会有正则表达式的资源加载器。
Loaders.regex().load("io/loadkit/\\w+Loader.class"); // 加载 io.loadkit包下名称以Loader.class 结尾的资源。
```

```java
// Loader#load(); 方法只接收一个Filter参数，当过滤逻辑比较多而且不好写在一个过滤器，当然这样的类也是违背了"单一职责原则"的。
Filters.and(Filter...filters); // AND 连接的混合过滤器
Filters.or(Filter...filters); // OR 连接的混合过滤器
// 来将多个子过滤器混合成一个过滤器，或者：
Filters.all(Filter...filters); // AND 连接的混合过滤器的另一种表达方式
Filters.any(Filter...filters); // OR 连接的混合过滤器的另一种表达方式
```
