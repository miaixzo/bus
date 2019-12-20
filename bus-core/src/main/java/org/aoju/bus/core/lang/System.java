/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.lang;

/**
 * 系统常量
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class System {

    /**
     * Java 运行时环境版本
     */
    public static final String VERSION = "java.version";

    /**
     * Java 运行时环境供应商
     */
    public static final String VENDOR = "java.vendor";

    /**
     * Java 供应商的 URL
     */
    public static final String VENDOR_URL = "java.vendor.url";

    /**
     * Java 安装目录
     */
    public static final String HOME = "java.home";

    /**
     * Java 虚拟机规范版本
     */
    public static final String VM_SPECIFICATION_VERSION = "java.vm.specification.version";

    /**
     * Java 虚拟机规范供应商
     */
    public static final String VM_SPECIFICATION_VENDOR = "java.vm.specification.vendor";

    /**
     * Java 虚拟机规范名称
     */
    public static final String VM_SPECIFICATION_NAME = "java.vm.specification.name";

    /**
     * Java 虚拟机实现版本
     */
    public static final String VM_VERSION = "java.vm.version";

    /**
     * Java 虚拟机实现供应商
     */
    public static final String VM_VENDOR = "java.vm.vendor";

    /**
     * Java 虚拟机实现名称
     */
    public static final String VM_NAME = "java.vm.name";
    /**
     * Java 虚拟机实现信息
     */
    public static final String VM_INFO = " java.vm.info";

    /**
     * Java 运行时环境规范版本
     */
    public static final String SPECIFICATION_VERSION = "java.specification.version";

    /**
     * Java 运行时环境规范供应商
     */
    public static final String SPECIFICATION_VENDOR = "java.specification.vendor";

    /**
     * Java 运行时环境规范名称
     */
    public static final String SPECIFICATION_NAME = "java.specification.name";

    /**
     * Java 类格式版本号
     */
    public static final String CLASS_VERSION = "java.class.version";

    /**
     * 类路径
     */
    public static final String CLASS_PATH = "java.class.path";

    /**
     * 加载库时搜索的路径列表
     */
    public static final String LIBRARY_PATH = "java.library.path";

    /**
     * 默认的临时文件路径
     */
    public static final String IO_TMPDIR = "java.io.tmpdir";

    /**
     * 要使用的 JIT 编译器的名称
     */
    public static final String COMPILER = "java.compiler";

    /**
     * 一个或多个扩展目录的路径
     */
    public static final String EXT_DIRS = "java.ext.dirs";

    /**
     * 操作系统的名称
     */
    public static final String OS_NAME = "os.name";

    /**
     * 操作系统的架构
     */
    public static final String OS_ARCH = "os.arch";

    /**
     * 操作系统的版本
     */
    public static final String OS_VERSION = "os.version";

    /**
     * 文件分隔符
     * UNIX /
     */
    public static final String FILE_SEPARATOR = "file.separator";

    /**
     * 路径分隔符
     * Unix :
     */
    public static final String PATH_SEPARATOR = "path.separator";

    /**
     * 行分隔符
     * Unix /n
     */
    public static final String LINE_SEPARATOR = "line.separator";

    /**
     * 用户的账户名称
     */
    public static final String USER_NAME = "user.name";

    /**
     * 用户的主目录
     */
    public static final String USER_HOME = "user.home";

    /**
     * 用户的当前工作目录
     */
    public static final String USER_DIR = "user.dir";
    /**
     * 用户的当前语言
     */
    public static final String USER_LANGUAGE = "user.language";
    /**
     * 用户的当前地区国家
     */
    public static final String USER_COUNTRY = "user.country";
    /**
     * 用户的当前区域
     */
    public static final String USER_REGION = "user.region";

    /**
     * 运行环境名称
     */
    public static final String RUNTIME_NAME = " java.runtime.name";
    /**
     * 运行环境版本
     */
    public static final String RUNTIME_VERSION = "java.runtime.version";

    public static final String ENDORSED_DIRS = "java.endorsed.dirs";
    public static final String SUN_CLASS_PATH = "sun.boot.class.path";
    public static final String SUN_DATA_MODEL = "sun.arch.data.model";

    /**
     * 获取指定配置信息
     *
     * @param key key
     * @return 结果
     */
    public static String getProperty(final String key) {
        return System.getProperty(key);
    }

}
