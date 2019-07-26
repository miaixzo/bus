package org.aoju.bus.health;

import org.aoju.bus.core.consts.System;

/**
 * 代表Java Virtual Machine Implementation的信息。
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class JvmInfo {

    /**
     * 取得当前JVM impl.的名称（取自系统属性：<code>java.vm.name</code>）。
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"Java HotSpot(TM) Client VM"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     */
    public final String getName() {
        return SystemUtils.get(System.VM_NAME, false);
    }

    /**
     * 取得当前JVM impl.的版本（取自系统属性：<code>java.vm.version</code>）。
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"1.4.2-b28"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     */
    public final String getVersion() {
        return SystemUtils.get(System.VM_VERSION, false);
    }

    /**
     * 取得当前JVM impl.的厂商（取自系统属性：<code>java.vm.vendor</code>）。
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"Sun Microsystems Inc."</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     */
    public final String getVendor() {
        return SystemUtils.get(System.VM_VENDOR, false);
    }

    /**
     * 取得当前JVM impl.的信息（取自系统属性：<code>java.vm.info</code>）。
     *
     * <p>
     * 例如Sun JDK 1.4.2：<code>"mixed mode"</code>
     * </p>
     *
     * @return 属性值，如果不能取得（因为Java安全限制）或值不存在，则返回<code>null</code>。
     */
    public final String getInfo() {
        return SystemUtils.get(System.VM_INFO, false);
    }

    /**
     * 将Java Virutal Machine Implementation的信息转换成字符串。
     *
     * @return JVM impl.的字符串表示
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();

        SystemUtils.append(builder, "JavaVM Name:    ", getName());
        SystemUtils.append(builder, "JavaVM Version: ", getVersion());
        SystemUtils.append(builder, "JavaVM Vendor:  ", getVendor());
        SystemUtils.append(builder, "JavaVM Info:    ", getInfo());

        return builder.toString();
    }

}
