package org.aoju.bus.spring.core.proxy.mode;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public enum ScanMode {
    // 只执行扫描到接口名或者类名上的注解后的处理
    FOR_CLASS_ANNOTATION_ONLY,
    // 只执行扫描到接口或者类方法上的注解后的处理
    FOR_METHOD_ANNOTATION_ONLY,
    // 上述两者都执行
    FOR_CLASS_OR_METHOD_ANNOTATION
}