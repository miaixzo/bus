package org.aoju.bus.spring.core.proxy.mode;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum ProxyMode {
    // 只通过扫描到接口名或者类名上的注解后，来确定是否要代理
    BY_CLASS_ANNOTATION_ONLY,
    // 只通过扫描到接口或者类方法上的注解后，来确定是否要代理
    BY_METHOD_ANNOTATION_ONLY,
    // 上述两者都可以
    BY_CLASS_OR_METHOD_ANNOTATION
}