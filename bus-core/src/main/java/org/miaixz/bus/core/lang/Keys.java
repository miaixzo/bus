/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.lang;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import org.miaixz.bus.core.convert.Convert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.BooleanKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 系统属性名称常量池 封装了包括Java运行时环境信息、Java虚拟机信息、Java类信息、OS信息、用户信息等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Keys {

    /**
     * 框架本体
     */
    public static final String BUS = "bus";

    /**
     * 框架名称
     */
    public static final String NAME = "bus.name";

    /**
     * 框架版本
     */
    public static final String VERSION = "bus.version";

    /**
     * 操作系统名称
     */
    public static final String OS_NAME = "os.name";

    /**
     * 操作系统架构
     */
    public static final String OS_ARCH = "os.arch";

    /**
     * 操作系统版本
     */
    public static final String OS_VERSION = "os.version";
    /**
     * 用户账户名称
     */
    public static final String USER_NAME = "user.name";

    /**
     * 用户的主目录
     */
    public static final String USER_HOME = "user.home";

    /**
     * 当前工作目录
     */
    public static final String USER_DIR = "user.dir";
    /**
     * 文件编码
     */
    public static final String FILE_ENCODING = "file.encoding";
    /**
     * 当前语言
     */
    public static final String USER_LANGUAGE = "user.language";
    /**
     * 当前地区
     */
    public static final String USER_COUNTRY = "user.country";
    /**
     * 当前区域
     */
    public static final String USER_REGION = "user.region";
    /**
     * 文件路径分隔符 在Unix和Linux下 是{@code '/'}; 在Windows下是 {@code '\'}
     */
    public static final String FILE_SEPARATOR = "file.separator";
    /**
     * 多个PATH之间的分隔符 在Unix和Linux下 是{@code ':'}; 在Windows下是 {@code ';'}
     */
    public static final String PATH_SEPARATOR = "path.separator";
    /**
     * 行分隔符 Unix /n
     */
    public static final String LINE_SEPARATOR = "line.separator";

    /**
     * Java 运行时环境版本
     */
    public static final String JAVA_VERSION = "java.version";

    /**
     * Java 运行时环境供应商
     */
    public static final String JAVA_VENDOR = "java.vendor";

    /**
     * Java 供应商的 URL
     */
    public static final String JAVA_VENDOR_URL = "java.vendor.url";

    /**
     * Java 安装目录
     */
    public static final String JAVA_HOME = "java.home";

    /**
     * Java 虚拟机规范版本
     */
    public static final String JAVA_VM_SPECIFICATION_VERSION = "java.vm.specification.version";

    /**
     * Java 虚拟机规范供应商
     */
    public static final String JAVA_VM_SPECIFICATION_VENDOR = "java.vm.specification.vendor";

    /**
     * Java 虚拟机规范名称
     */
    public static final String JAVA_VM_SPECIFICATION_NAME = "java.vm.specification.name";

    /**
     * Java 虚拟机实现版本
     */
    public static final String JAVA_VM_VERSION = "java.vm.version";

    /**
     * Java 虚拟机实现供应商
     */
    public static final String JAVA_VM_VENDOR = "java.vm.vendor";

    /**
     * Java 虚拟机实现名称
     */
    public static final String JAVA_VM_NAME = "java.vm.name";
    /**
     * Java 虚拟机实现信息
     */
    public static final String JAVA_VM_INFO = " java.vm.info";

    /**
     * Java 运行时环境规范版本
     */
    public static final String JAVA_SPECIFICATION_VERSION = "java.specification.version";

    /**
     * Java 运行时环境规范供应商
     */
    public static final String JAVA_SPECIFICATION_VENDOR = "java.specification.vendor";

    /**
     * Java 运行时环境规范名称
     */
    public static final String JAVA_SPECIFICATION_NAME = "java.specification.name";

    /**
     * Java 类格式版本号
     */
    public static final String JAVA_CLASS_VERSION = "java.class.version";

    /**
     * 类路径
     */
    public static final String JAVA_CLASS_PATH = "java.class.path";

    /**
     * 加载库时搜索的路径列表
     */
    public static final String JAVA_LIBRARY_PATH = "java.library.path";

    /**
     * 默认的临时文件路径
     */
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    /**
     * 要使用的 JIT 编译器的名称
     */
    public static final String JAVA_COMPILER = "java.compiler";

    /**
     * 一个或多个扩展目录的路径
     */
    public static final String JAVA_EXT_DIRS = "java.ext.dirs";
    /**
     * 运行环境名称
     */
    public static final String JAVA_RUNTIME_NAME = " java.runtime.name";
    /**
     * 运行环境版本
     */
    public static final String JAVA_RUNTIME_VERSION = "java.runtime.version";
    /**
     * 扩展jdk的系统库目录
     */
    public static final String JAVA_ENDORSED_DIRS = "java.endorsed.dirs";
    /**
     * Transient注解的类名
     */
    public static final String JAVA_BEANS_TRANSIENT = "java.beans.Transient";
    /**
     * BootstrapClassLoader加载的jar包路径
     */
    public static final String SUN_BOOT_CLASS_PATH = "sun.boot.class.path";
    /**
     * JVM 系统位数 32/64
     */
    public static final String SUN_ARCH_DATA_MODEL = "sun.arch.data.model";
    /**
     * 自定义系统属性：是否解析日期字符串采用严格模式
     */
    public static final String DATE_LENIENT = "bus.date.lenient";
    /**
     * JDK版本
     */
    public static final int JVM_VERSION;
    /**
     * 是否大于等于JDK17
     */
    public static final boolean IS_AT_LEAST_JDK17;
    /**
     * 是否Android环境
     */
    public static final boolean IS_ANDROID;
    /**
     * 是否OPENJ9环境
     */
    public static final boolean IS_OPENJ9;

    /**
     * 是否GraalVM Native Image环境
     */
    public static final boolean IS_GRAALVM_NATIVE;

    static {
        // JVM版本
        JVM_VERSION = _getJvmVersion();
        IS_AT_LEAST_JDK17 = JVM_VERSION >= 17;

        // JVM名称
        final String jvmName = _getJvmName();
        IS_ANDROID = jvmName.equals("Dalvik");
        IS_OPENJ9 = jvmName.contains("OpenJ9");
        // GraalVM
        IS_GRAALVM_NATIVE = null != System.getProperty("org.graalvm.nativeimage.imagecode");
    }

    /**
     * 取得系统属性，如果因为Java安全的限制而失败，则将错误打在Log中，然后返回 defaultValue
     *
     * @param name         属性名
     * @param defaultValue 默认值
     * @return 属性值或defaultValue
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String get(final String name, final String defaultValue) {
        return ObjectKit.defaultIfNull(get(name), defaultValue);
    }

    /**
     * 获得System属性
     *
     * @param key 键
     * @return 属性值
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String get(final String key) {
        return get(key, false);
    }

    /**
     * 获得System属性，忽略无权限问题
     *
     * @param key 键
     * @return 属性值
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String getQuietly(final String key) {
        return get(key, true);
    }

    /**
     * 取得系统属性，如果因为Java安全的限制而失败，则将错误打在日志中，然后返回 {@code null}
     *
     * @param name  属性名
     * @param quiet 安静模式，不将出错信息打在{@code System.err}中
     * @return 属性值或{@code null}
     * @see System#getProperty(String)
     * @see System#getenv(String)
     */
    public static String get(final String name, final boolean quiet) {
        String value = null;
        try {
            value = System.getProperty(name);
        } catch (final SecurityException e) {
            if (!quiet) {
                Console.error("Caught a SecurityException reading the system property '{}'; "
                        + "the Keys property value will default to null.", name);
            }
        }

        if (null == value) {
            try {
                value = System.getenv(name);
            } catch (final SecurityException e) {
                if (!quiet) {
                    Console.error("Caught a SecurityException reading the system env '{}'; "
                            + "the Keys env value will default to null.", name);
                }
            }
        }

        return value;
    }

    /**
     * 获得boolean类型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static boolean getBoolean(final String key, final boolean defaultValue) {
        final String value = get(key);
        if (value == null) {
            return defaultValue;
        }

        return BooleanKit.toBoolean(value);
    }

    /**
     * 获得int类型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static int getInt(final String key, final int defaultValue) {
        return Convert.toInt(get(key), defaultValue);
    }

    /**
     * 获得long类型值
     *
     * @param key          键
     * @param defaultValue 默认值
     * @return 值
     */
    public static long getLong(final String key, final long defaultValue) {
        return Convert.toLong(get(key), defaultValue);
    }

    /**
     * @return 属性列表
     */
    public static Properties getProps() {
        return System.getProperties();
    }

    /**
     * 设置系统属性，value为{@code null}表示移除此属性
     *
     * @param key   属性名
     * @param value 属性值，{@code null}表示移除此属性
     */
    public static void set(final String key, final String value) {
        if (null == value) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    /**
     * 获得Java ClassPath路径，不包括 jre
     *
     * @return Java ClassPath路径，不包括 jre
     */
    public static String[] getJavaClassPaths() {
        return get(JAVA_CLASS_PATH).split(get(PATH_SEPARATOR));
    }

    /**
     * 获取用户路径（绝对路径）
     *
     * @return 用户路径
     */
    public static String getUserHomePath() {
        return get(USER_HOME);
    }

    /**
     * 获取临时文件路径（绝对路径）
     *
     * @return 临时文件路径
     */
    public static String getTmpDirPath() {
        return get(JAVA_IO_TMPDIR);
    }

    /**
     * 获取JVM名称
     *
     * @return JVM名称
     */
    static String _getJvmName() {
        return getQuietly(JAVA_VM_NAME);
    }

    /**
     * 根据{@code java.specification.version}属性值，获取版本号 默认8
     *
     * @return 版本号
     */
    public static int _getJvmVersion() {
        int jvmVersion = 8;

        String javaSpecVer = getQuietly(JAVA_SPECIFICATION_VERSION);
        if (StringKit.isNotBlank(javaSpecVer)) {
            if (javaSpecVer.startsWith("1.")) {
                javaSpecVer = javaSpecVer.substring(2);
            }
            if (javaSpecVer.indexOf('.') == -1) {
                jvmVersion = Integer.parseInt(javaSpecVer);
            }
        }

        return jvmVersion;
    }

    /**
     * 获取指定容器环境的对象的属性 如获取DNS属性，则URI为类似：dns:miaixz.org
     *
     * @param uri     URI字符串，格式为[scheme:][name]/[domain]
     * @param attrIds 需要获取的属性ID名称
     * @return {@link Attributes}
     */
    public static Attributes getAttributes(final String uri, final String... attrIds) {
        try {
            return createInitialDirContext(null).getAttributes(uri, attrIds);
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建{@link InitialDirContext}
     *
     * @param environment 环境参数，{@code null}表示无参数
     * @return {@link InitialDirContext}
     */
    static InitialDirContext createInitialDirContext(final Map<String, String> environment) {
        try {
            if (MapKit.isEmpty(environment)) {
                return new InitialDirContext();
            }
            return new InitialDirContext(Convert.convert(Hashtable.class, environment));
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 创建{@link InitialContext}
     *
     * @param environment 环境参数，{@code null}表示无参数
     * @return {@link InitialContext}
     */
    static InitialContext createInitialContext(final Map<String, String> environment) {
        try {
            if (MapKit.isEmpty(environment)) {
                return new InitialContext();
            }
            return new InitialContext(Convert.convert(Hashtable.class, environment));
        } catch (final NamingException e) {
            throw new InternalException(e);
        }
    }

}
