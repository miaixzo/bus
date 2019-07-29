注解介绍
@EnableXssFilter

添加对 XSS 攻击转义的支持

@EnableI18n

I18N 国际化支持

@EnableCorsFilter

全局跨域支持

@EnableOnceFilter

继承 HttpServletRequestWrapper 实现BodyCacheHttpServletRequestWrapper，解决 request.getInputStream() 一次读取后失效痛点

@EnableTimeZone

使 spring.jackson.date-format 属性支持 JDK8 日期格式化