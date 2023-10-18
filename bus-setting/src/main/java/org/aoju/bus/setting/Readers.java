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
package org.aoju.bus.setting;

import org.aoju.bus.core.io.reader.LineReader;
import org.aoju.bus.core.io.resource.Resource;
import org.aoju.bus.core.lang.Assert;
import org.aoju.bus.core.lang.Charset;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.PatternKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.setting.format.*;
import org.aoju.bus.setting.magic.*;
import org.aoju.bus.setting.metric.GroupMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

/**
 * Setting文件加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Readers {

    /**
     * 注释符号(当有此符号在行首,表示此行为注释)
     */
    private static final char COMMENT_FLAG_PRE = Symbol.C_SHAPE;
    /**
     * 赋值分隔符(用于分隔键值对)
     */
    private static final char ASSIGN_FLAG = Symbol.C_EQUAL;
    /**
     * 变量名称的正则
     */
    private String reg_var = "\\$\\{(.*?)\\}";

    /**
     * 本设置对象的字符集
     */
    private java.nio.charset.Charset charset;
    /**
     * 是否使用变量
     */
    private boolean isUseVariable;
    /**
     * GroupedMap
     */
    private GroupMap groupMap;

    /**
     * ini line data formatter factory
     */
    private Factory formatterFactory;

    private Supplier<ElementFormatter<IniComment>> commentElementFormatterSupplier = CommentFormatter::new;
    private Supplier<ElementFormatter<IniSection>> sectionElementFormatterSupplier = SectionFormatter::new;
    private Supplier<ElementFormatter<IniProperty>> propertyElementFormatterSupplier = PropertyFormatter::new;

    public Readers() {
        this.formatterFactory = DefaultFormatter::new;
    }

    public Readers(Factory formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    /**
     * 构造
     *
     * @param groupMap GroupedMap
     */
    public Readers(GroupMap groupMap) {
        this(groupMap, Charset.UTF_8, false);
    }

    /**
     * 构造
     *
     * @param groupMap      GroupedMap
     * @param charset       编码
     * @param isUseVariable 是否使用变量
     */
    public Readers(GroupMap groupMap, java.nio.charset.Charset charset, boolean isUseVariable) {
        this.groupMap = groupMap;
        this.charset = charset;
        this.isUseVariable = isUseVariable;
    }

    /**
     * 加载设置文件
     *
     * @param resource 配置文件URL
     * @return 加载是否成功
     */
    public boolean load(Resource resource) {
        if (null == resource) {
            throw new NullPointerException("Null setting url define!");
        }
        Logger.debug("Load setting file [{}]", resource);
        InputStream settingStream;
        try {
            settingStream = resource.getStream();
            load(settingStream);
        } catch (Exception e) {
            Logger.error(e, "Load setting error!");
            return false;
        }
        return true;
    }

    /**
     * 加载设置文件  此方法不会关闭流对象
     *
     * @param inputStream 文件流
     * @return 加载成功与否
     * @throws IOException IO异常
     */
    public boolean load(final InputStream inputStream) throws IOException {
        this.groupMap.clear();
        LineReader reader = null;
        try {
            reader = new LineReader(inputStream, this.charset);
            // 分组
            String group = null;

            String line;
            while (true) {
                line = reader.readLine();
                if (null == line) {
                    break;
                }
                line = line.trim();
                // 跳过注释行和空行
                if (StringKit.isBlank(line) || StringKit.startWith(line, COMMENT_FLAG_PRE)) {
                    continue;
                }

                // 记录分组名
                if (StringKit.isSurround(line, Symbol.BRACKET_LEFT, Symbol.BRACKET_RIGHT)) {
                    group = line.substring(1, line.length() - 1).trim();
                    continue;
                }

                final String[] keyValue = StringKit.splitToArray(line, ASSIGN_FLAG, 2);
                // 跳过不符合键值规范的行
                if (keyValue.length < 2) {
                    continue;
                }

                String value = keyValue[1].trim();
                // 替换值中的所有变量变量(变量必须是此行之前定义的变量,否则无法找到)
                if (this.isUseVariable) {
                    value = replaceVar(group, value);
                }
                this.groupMap.put(group, keyValue[0].trim(), value);
            }
        } finally {
            IoKit.close(reader);
        }
        return true;
    }

    /**
     * 设置变量的正则
     * 正则只能有一个group表示变量本身,剩余为字符 例如 \$\{(name)\}表示${name}变量名为name的一个变量表示
     *
     * @param regex 正则
     */
    public void setVarRegex(String regex) {
        this.reg_var = regex;
    }


    /**
     * 持久化当前设置，会覆盖掉之前的设置
     * 持久化会不会保留之前的分组
     *
     * @param absolutePath 设置文件的绝对路径
     */
    public void store(String absolutePath) {
        store(FileKit.touch(absolutePath));
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置
     * 持久化会不会保留之前的分组
     *
     * @param file 设置文件
     */
    public void store(File file) {
        Assert.notNull(file, "File to store must be not null !");
        Logger.debug("Store Setting to [{}]...", file.getAbsolutePath());
        PrintWriter writer = null;
        try {
            writer = FileKit.getPrintWriter(file, charset, false);
            store(writer);
        } finally {
            IoKit.close(writer);
        }
    }

    /**
     * 存储到Writer
     *
     * @param writer Writer
     */
    private void store(PrintWriter writer) {
        for (Entry<String, LinkedHashMap<String, String>> groupEntry : this.groupMap.entrySet()) {
            writer.println(StringKit.format("{}{}{}", Symbol.BRACKET_LEFT, groupEntry.getKey(), Symbol.BRACKET_RIGHT));
            for (Entry<String, String> entry : groupEntry.getValue().entrySet()) {
                writer.println(StringKit.format("{} {} {}", entry.getKey(), ASSIGN_FLAG, entry.getValue()));
            }
        }
    }

    /**
     * 替换给定值中的变量标识
     *
     * @param group 所在分组
     * @param value 值
     * @return 替换后的字符串
     */
    private String replaceVar(String group, String value) {
        // 找到所有变量标识
        final Set<String> vars = PatternKit.findAll(reg_var, value, 0, new HashSet<>());
        String key;
        for (String var : vars) {
            key = PatternKit.get(reg_var, var, 1);
            if (StringKit.isNotBlank(key)) {
                // 查找变量名对应的值
                String varValue = this.groupMap.get(group, key);
                if (null != varValue) {
                    // 替换标识
                    value = value.replace(var, varValue);
                } else {
                    // 跨分组查找
                    final List<String> groupAndKey = StringKit.split(key, Symbol.C_DOT, 2);
                    if (groupAndKey.size() > 1) {
                        varValue = this.groupMap.get(groupAndKey.get(0), groupAndKey.get(1));
                        if (null != varValue) {
                            // 替换标识
                            value = value.replace(var, varValue);
                        }
                    }
                }
            }
        }
        return value;
    }

    /**
     * get a default formatter by factory
     *
     * @return {@link Format}
     */
    protected Format getFormatter() {
        return formatterFactory.apply(
                commentElementFormatterSupplier.get(),
                sectionElementFormatterSupplier.get(),
                propertyElementFormatterSupplier.get()
        );
    }

    /**
     * read ini data from an inputStream
     *
     * @param in an ini data inputStream
     * @return ini bean
     * @throws IOException io exception
     * @see #read(java.io.Reader)
     */
    public IniSetting read(InputStream in) throws IOException {
        return read(new InputStreamReader(in));
    }

    /**
     * read ini file to bean
     *
     * @param file ini file
     * @return ini bean
     * @throws IOException io exception
     * @see #read(java.io.Reader)
     */
    public IniSetting read(File file) throws IOException {
        try (java.io.Reader reader = new FileReader(file)) {
            return read(reader);
        }
    }

    /**
     * read ini file to bean
     *
     * @param path ini path(file)
     * @return ini bean
     * @throws IOException io exception
     * @see #read(java.io.Reader)
     */
    public IniSetting read(Path path) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path)) {
            return read(reader);
        }
    }

    /**
     * to buffered and read
     *
     * @param reader ini data reader
     * @return the object
     * @throws IOException io exception
     */
    public IniSetting read(Reader reader) throws IOException {
        BufferedReader bufReader;
        if (reader instanceof BufferedReader) {
            bufReader = (BufferedReader) reader;
        } else {
            bufReader = new BufferedReader(reader);
        }
        return bufferedRead(bufReader);
    }

    /**
     * format reader to ini bean
     *
     * @param reader reader
     * @return {@link IniSetting} bean
     * @throws IOException io exception
     * @see #defaultFormat(java.io.Reader, int)
     */
    protected IniSetting defaultFormat(java.io.Reader reader) throws IOException {
        return defaultFormat(reader, Normal._16);
    }

    /**
     * format reader to ini bean
     *
     * @param reader          reader
     * @param builderCapacity {@link StringBuilder} init param
     * @return {@link IniSetting} bean
     * @throws IOException io exception
     */
    protected IniSetting defaultFormat(java.io.Reader reader, int builderCapacity) throws IOException {
        Format format = getFormatter();
        List<IniElement> iniElements = new ArrayList<>();
        // new line split
        String newLineSplit = System.getProperty("line.separator", Symbol.LF);
        StringBuilder line = new StringBuilder(builderCapacity);

        int ch;
        while ((ch = reader.read()) != -1) {
            line.append((char) ch);
            String nowStr = line.toString();
            // if new line
            if (nowStr.endsWith(newLineSplit)) {
                // format and add
                IniElement element = format.formatLine(nowStr);
                if (null != element) {
                    iniElements.add(element);
                }
                // init stringBuilder
                line.delete(0, line.length());
            }
        }
        // the end of files, format again
        if (line.length() > 0) {
            // format and add
            iniElements.add(format.formatLine(line.toString()));
        }

        return new IniSetting(iniElements);
    }

    /**
     * read buffered reader and parse to ini.
     *
     * @param reader BufferedReader
     * @return Ini
     * @throws IOException io exception
     */
    private IniSetting bufferedRead(BufferedReader reader) throws IOException {
        return defaultFormat(reader);
    }

    public Supplier<ElementFormatter<IniComment>> getCommentElementFormatterSupplier() {
        return commentElementFormatterSupplier;
    }

    public void setCommentElementFormatterSupplier(Supplier<ElementFormatter<IniComment>> commentElementFormatterSupplier) {
        this.commentElementFormatterSupplier = commentElementFormatterSupplier;
    }

    public Supplier<ElementFormatter<IniSection>> getSectionElementFormatterSupplier() {
        return sectionElementFormatterSupplier;
    }

    public void setSectionElementFormatterSupplier(Supplier<ElementFormatter<IniSection>> sectionElementFormatterSupplier) {
        this.sectionElementFormatterSupplier = sectionElementFormatterSupplier;
    }

    public Supplier<ElementFormatter<IniProperty>> getPropertyElementFormatterSupplier() {
        return propertyElementFormatterSupplier;
    }

    public void setPropertyElementFormatterSupplier(Supplier<ElementFormatter<IniProperty>> propertyElementFormatterSupplier) {
        this.propertyElementFormatterSupplier = propertyElementFormatterSupplier;
    }

}
