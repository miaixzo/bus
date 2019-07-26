package org.aoju.bus.health;

import org.aoju.bus.core.consts.System;

/**
 * 代表Java Virutal Machine Specification的信息。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JvmSpecInfo {

    /**
     * 取得当前JVM spec.的名称（取自系统属性：<code>java.vm.specification.name</code>）。
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"Java Virtual Machine Specification"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     */
    public final String getName() {
        return SystemUtils.get(System.VM_SPECIFICATION_NAME, false);
    }

    /**
     * 取得当前JVM spec.的版本（取自系统属性：<code>java.vm.specification.version</code>）。
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"1.0"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     */
    public final String getVersion() {
        return SystemUtils.get(System.VM_SPECIFICATION_VERSION, false);
    }

    /**
     * 取得当前JVM spec.的厂商（取自系统属性：<code>java.vm.specification.vendor</code>）。
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"Sun Microsystems Inc."</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     */
    public final String getVendor() {
        return SystemUtils.get(System.VM_SPECIFICATION_VENDOR, false);
    }

    /**
     * 将Java Virutal Machine Specification的信息转换成字符串。
     *
     * @return JVM spec.的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();

        SystemUtils.append(builder, "JavaVM Spec. Name:    ", getName());
        SystemUtils.append(builder, "JavaVM Spec. Version: ", getVersion());
        SystemUtils.append(builder, "JavaVM Spec. Vendor:  ", getVendor());

        return builder.toString();
    }

}
