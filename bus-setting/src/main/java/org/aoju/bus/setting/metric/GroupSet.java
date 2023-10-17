/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.setting.metric;

import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * 分组化的Set集合类
 * 在配置文件中可以用中括号分隔不同的分组,每个分组会放在独立的Set中,用group区别
 * 无分组的集合和`[]`分组集合会合并成员,重名的分组也会合并成员
 * 分组配置文件如下：
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class GroupSet extends HashMap<String, LinkedHashSet<String>> {

    /**
     * 注释符号(当有此符号在行首,表示此行为注释)
     */
    private static final String COMMENT_FLAG_PRE = Symbol.SHAPE;
    /**
     * 分组行识别的环绕标记
     */
    private static final char[] GROUP_SURROUND = {Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT};

    /**
     * 本设置对象的字符集
     */
    private java.nio.charset.Charset charset;
    /**
     * 设定文件的URL
     */
    private URL groupedSetUrl;

    /**
     * 基本构造
     * 需自定义初始化配置文件
     *
     * @param charset 字符集
     */
    public GroupSet(java.nio.charset.Charset charset) {
        this.charset = charset;
    }

    /**
     * 构造,使用相对于Class文件根目录的相对路径
     *
     * @param pathBaseClassLoader 相对路径(相对于当前项目的classes路径)
     * @param charset             字符集
     */
    public GroupSet(String pathBaseClassLoader, java.nio.charset.Charset charset) {
        if (null == pathBaseClassLoader) {
            pathBaseClassLoader = Normal.EMPTY;
        }

        final URL url = UriKit.getURL(pathBaseClassLoader);
        if (null == url) {
            throw new RuntimeException(StringKit.format("Can not find GroupSet file : [{}]", pathBaseClassLoader));
        }
        this.init(url, charset);
    }

    /**
     * 构造
     *
     * @param configFile 配置文件对象
     * @param charset    字符集
     */
    public GroupSet(File configFile, java.nio.charset.Charset charset) {
        if (null == configFile) {
            throw new RuntimeException("Null GroupSet file!");
        }
        final URL url = UriKit.getURL(configFile);
        if (null == url) {
            throw new RuntimeException(StringKit.format("Can not find GroupSet file : [{}]", configFile.getAbsolutePath()));
        }
        this.init(url, charset);
    }

    /**
     * 构造,相对于classes读取文件
     *
     * @param path    相对路径
     * @param clazz   基准类
     * @param charset 字符集
     */
    public GroupSet(String path, Class<?> clazz, java.nio.charset.Charset charset) {
        final URL url = UriKit.getURL(path, clazz);
        if (null == url) {
            throw new RuntimeException(StringKit.format("Can not find GroupSet file : [{}]", path));
        }
        this.init(url, charset);
    }

    /**
     * 构造
     *
     * @param url     设定文件的URL
     * @param charset 字符集
     */
    public GroupSet(URL url, java.nio.charset.Charset charset) {
        if (null == url) {
            throw new RuntimeException("Null url define!");
        }
        this.init(url, charset);
    }

    /**
     * 构造
     *
     * @param pathBaseClassLoader 相对路径(相对于当前项目的classes路径)
     */
    public GroupSet(String pathBaseClassLoader) {
        this(pathBaseClassLoader, Charset.UTF_8);
    }

    /**
     * 初始化设定文件
     *
     * @param groupedSetUrl 设定文件的URL
     * @param charset       字符集
     * @return 成功初始化与否
     */
    public boolean init(URL groupedSetUrl, java.nio.charset.Charset charset) {
        if (null == groupedSetUrl) {
            throw new RuntimeException("Null GroupSet url or charset define!");
        }
        this.charset = charset;
        this.groupedSetUrl = groupedSetUrl;

        return this.load(groupedSetUrl);
    }

    /**
     * 加载设置文件
     *
     * @param groupedSetUrl 配置文件URL
     * @return 加载是否成功
     */
    synchronized public boolean load(URL groupedSetUrl) {
        if (null == groupedSetUrl) {
            throw new RuntimeException("Null GroupSet url define!");
        }
        // log.debug("Load GroupSet file [{}]", groupedSetUrl.getPath());
        InputStream settingStream = null;
        try {
            settingStream = groupedSetUrl.openStream();
            load(settingStream);
        } catch (IOException e) {
            // log.error(e, "Load GroupSet error!");
            return false;
        } finally {
            IoKit.close(settingStream);
        }
        return true;
    }

    /**
     * 重新加载配置文件
     */
    public void reload() {
        this.load(groupedSetUrl);
    }

    /**
     * 加载设置文件  此方法不会关闭流对象
     *
     * @param settingStream 文件流
     * @return 加载成功与否
     * @throws IOException IO异常
     */
    public boolean load(InputStream settingStream) throws IOException {
        super.clear();
        BufferedReader reader = null;
        try {
            reader = IoKit.getReader(settingStream, charset);
            // 分组
            String group;
            LinkedHashSet<String> valueSet = null;

            while (true) {
                String line = reader.readLine();
                if (null == line) {
                    break;
                }
                line = line.trim();
                // 跳过注释行和空行
                if (StringKit.isBlank(line) || line.startsWith(COMMENT_FLAG_PRE)) {
                    // 空行和注释忽略
                    continue;
                } else if (line.startsWith(Symbol.BACKSLASH + COMMENT_FLAG_PRE)) {
                    // 对于值中出现开头为#的字符串,需要转义处理,在此做反转义
                    line = line.substring(1);
                }

                // 记录分组名
                if (line.charAt(0) == GROUP_SURROUND[0] && line.charAt(line.length() - 1) == GROUP_SURROUND[1]) {
                    // 开始新的分组取值,当出现重名分组时候,合并分组值
                    group = line.substring(1, line.length() - 1).trim();
                    valueSet = super.get(group);
                    if (null == valueSet) {
                        valueSet = new LinkedHashSet<>();
                    }
                    super.put(group, valueSet);
                    continue;
                }

                // 添加值
                if (null == valueSet) {
                    // 当出现无分组值的时候,会导致valueSet为空,此时group为""
                    valueSet = new LinkedHashSet<>();
                    super.put(Normal.EMPTY, valueSet);
                }
                valueSet.add(line);
            }
        } finally {
            IoKit.close(reader);
        }
        return true;
    }

    /**
     * @return 获得设定文件的路径
     */
    public String getPath() {
        return groupedSetUrl.getPath();
    }

    /**
     * @return 获得所有分组名
     */
    public Set<String> getGroups() {
        return super.keySet();
    }

    /**
     * 获得对应分组的所有值
     *
     * @param group 分组名
     * @return 分组的值集合
     */
    public LinkedHashSet<String> getValues(String group) {
        if (null == group) {
            group = Normal.EMPTY;
        }
        return super.get(group);
    }

    /**
     * 是否在给定分组的集合中包含指定值
     * 如果给定分组对应集合不存在,则返回false
     *
     * @param group       分组名
     * @param value       测试的值
     * @param otherValues 其他值
     * @return 是否包含
     */
    public boolean contains(String group, String value, String... otherValues) {
        if (ArrayKit.isNotEmpty(otherValues)) {
            // 需要测试多个值的情况
            final List<String> valueList = new ArrayList<>(Arrays.asList(otherValues));
            valueList.add(value);
            return contains(group, valueList);
        } else {
            // 测试单个值
            final LinkedHashSet<String> valueSet = getValues(group);
            if (CollKit.isEmpty(valueSet)) {
                return false;
            }

            return valueSet.contains(value);
        }
    }

    /**
     * 是否在给定分组的集合中全部包含指定值集合
     * 如果给定分组对应集合不存在,则返回false
     *
     * @param group  分组名
     * @param values 测试的值集合
     * @return 是否包含
     */
    public boolean contains(String group, Collection<String> values) {
        final LinkedHashSet<String> valueSet = getValues(group);
        if (CollKit.isEmpty(values) || CollKit.isEmpty(valueSet)) {
            return false;
        }

        return valueSet.containsAll(values);
    }

}
