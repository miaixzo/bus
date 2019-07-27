package org.aoju.bus.spring.core.proxy;

import org.aoju.bus.spring.core.proxy.mode.ProxyMode;
import org.aoju.bus.spring.core.proxy.mode.ScanMode;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * <p>Title: 默认扫描代理</p>
 * <p>Description: </p>
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class DefaultAutoScanProxy extends AbstractAutoScanProxy {

    private static final long serialVersionUID = 9073263289068871774L;

    public DefaultAutoScanProxy() {
        super();
    }

    public DefaultAutoScanProxy(String scanPackages) {
        super(scanPackages);
    }

    public DefaultAutoScanProxy(String[] scanPackages) {
        super(scanPackages);
    }

    public DefaultAutoScanProxy(ProxyMode proxyMode, ScanMode scanMode) {
        super(proxyMode, scanMode);
    }

    public DefaultAutoScanProxy(String scanPackages, ProxyMode proxyMode, ScanMode scanMode) {
        super(scanPackages, proxyMode, scanMode);
    }

    public DefaultAutoScanProxy(String[] scanPackages, ProxyMode proxyMode, ScanMode scanMode) {
        super(scanPackages, proxyMode, scanMode);
    }

    public DefaultAutoScanProxy(ProxyMode proxyMode, ScanMode scanMode, boolean exposeProxy) {
        super(proxyMode, scanMode, exposeProxy);
    }

    public DefaultAutoScanProxy(String scanPackages, ProxyMode proxyMode, ScanMode scanMode, boolean exposeProxy) {
        super(scanPackages, proxyMode, scanMode, exposeProxy);
    }

    public DefaultAutoScanProxy(String[] scanPackages, ProxyMode proxyMode, ScanMode scanMode, boolean exposeProxy) {
        super(scanPackages, proxyMode, scanMode, exposeProxy);
    }

    @Override
    protected Class<? extends MethodInterceptor>[] getCommonInterceptors() {
        return null;
    }

    @Override
    protected String[] getCommonInterceptorNames() {
        return null;
    }

    @Override
    protected MethodInterceptor[] getAdditionalInterceptors(Class<?> targetClass) {
        return null;
    }

    @Override
    protected Class<? extends Annotation>[] getClassAnnotations() {
        return null;
    }

    @Override
    protected Class<? extends Annotation>[] getMethodAnnotations() {
        return null;
    }

    @Override
    protected void classAnnotationScanned(Class<?> targetClass, Class<? extends Annotation> classAnnotation) {

    }

    @Override
    protected void methodAnnotationScanned(Class<?> targetClass, Method method, Class<? extends Annotation> methodAnnotation) {

    }

}