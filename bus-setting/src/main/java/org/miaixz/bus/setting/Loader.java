/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.setting;

import org.miaixz.bus.core.io.LineReader;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.PatternKit;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.format.*;
import org.miaixz.bus.setting.magic.GroupedMap;
import org.miaixz.bus.setting.metric.ini.*;

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
public class Loader {

    /**
     * 注释符号（当有此符号在行首，表示此行为注释）
     */
    private static char COMMENT_FLAG_PRE = Symbol.C_SHAPE;
    /**
     * 赋值分隔符（用于分隔键值对）
     */
    private static char assignFlag = Symbol.C_EQUAL;

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
    private GroupedMap groupedMap;

    /**
     * 变量名称的正则
     */
    private String varRegex = "\\$\\{(.*?)\\}";

    /**
     * ini line data formatter factory
     */
    private Factory formatterFactory;

    private Supplier<ElementFormatter<IniComment>> commentElementFormatterSupplier = CommentFormatter::new;
    private Supplier<ElementFormatter<IniSection>> sectionElementFormatterSupplier = SectionFormatter::new;
    private Supplier<ElementFormatter<IniProperty>> propertyElementFormatterSupplier = PropertyFormatter::new;

    public Loader() {
        this.formatterFactory = DefaultFormatter::new;
    }

    public Loader(Factory formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    /**
     * 构造
     *
     * @param groupedMap GroupedMap
     */
    public Loader(final GroupedMap groupedMap) {
        this(groupedMap, Charset.UTF_8, false);
    }

    /**
     * 构造
     *
     * @param groupedMap    GroupedMap
     * @param charset       编码
     * @param isUseVariable 是否使用变量
     */
    public Loader(final GroupedMap groupedMap, final java.nio.charset.Charset charset, final boolean isUseVariable) {
        this.groupedMap = groupedMap;
        this.charset = charset;
        this.isUseVariable = isUseVariable;
    }

    /**
     * 加载设置文件
     *
     * @param resource 配置文件URL
     * @return 加载是否成功
     */
    public boolean load(final Resource resource) {
        if (resource == null) {
            throw new NullPointerException("Null setting url define!");
        }
        Logger.debug("Load setting file [{}]", resource);
        InputStream settingStream = null;
        try {
            settingStream = resource.getStream();
            load(settingStream);
        } catch (final Exception e) {
            Logger.error(e, "Load setting error!");
            return false;
        } finally {
            IoKit.closeQuietly(settingStream);
        }
        return true;
    }

    /**
     * 加载设置文件。 此方法不会关闭流对象
     *
     * @param settingStream 文件流
     * @throws IOException IO异常
     */
    synchronized public void load(final InputStream settingStream) throws IOException {
        this.groupedMap.clear();
        LineReader reader = null;
        try {
            reader = new LineReader(settingStream, this.charset);
            // 分组
            String group = null;

            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = StringKit.trim(line);
                // 跳过注释行和空行
                if (StringKit.isBlank(line) || StringKit.startWith(line, COMMENT_FLAG_PRE)) {
                    continue;
                }

                // 记录分组名
                if (StringKit.isWrap(line, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT)) {
                    group = StringKit.trim(line.substring(1, line.length() - 1));
                    continue;
                }

                final String[] keyValue = CharsBacker.split(line, String.valueOf(this.assignFlag), 2, true, false)
                        .toArray(new String[0]);
                // 跳过不符合键值规范的行
                if (keyValue.length < 2) {
                    continue;
                }

                String value = keyValue[1];
                // 替换值中的所有变量变量（变量必须是此行之前定义的变量，否则无法找到）
                if (this.isUseVariable) {
                    value = replaceVar(group, value);
                }
                this.groupedMap.put(group, StringKit.trim(keyValue[0]), value);
            }
        } finally {
            IoKit.closeQuietly(reader);
        }
    }

    /**
     * 设置变量的正则
     * 正则只能有一个group表示变量本身，剩余为字符 例如 \$\{(name)\}表示${name}变量名为name的一个变量表示
     *
     * @param regex 正则
     */
    public void setVarRegex(final String regex) {
        this.varRegex = regex;
    }

    /**
     * 赋值分隔符（用于分隔键值对）
     *
     * @param assignFlag 正则
     */
    public void setAssignFlag(final char assignFlag) {
        this.assignFlag = assignFlag;
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置
     * 持久化会不会保留之前的分组
     *
     * @param absolutePath 设置文件的绝对路径
     */
    public void store(final String absolutePath) {
        store(FileKit.touch(absolutePath));
    }

    /**
     * 持久化当前设置，会覆盖掉之前的设置
     * 持久化会不会保留之前的分组
     *
     * @param file 设置文件
     */
    public void store(final File file) {
        Assert.notNull(file, "File to store must be not null !");
        Logger.debug("Store Setting to [{}]...", file.getAbsolutePath());
        PrintWriter writer = null;
        try {
            writer = FileKit.getPrintWriter(file, charset, false);
            store(writer);
        } finally {
            IoKit.closeQuietly(writer);
        }
    }

    /**
     * 存储到Writer
     *
     * @param writer Writer
     */
    synchronized private void store(final PrintWriter writer) {
        for (final Entry<String, LinkedHashMap<String, String>> groupEntry : this.groupedMap.entrySet()) {
            writer.println(StringKit.format("{}{}{}", Symbol.C_BRACKET_LEFT, groupEntry.getKey(), Symbol.C_BRACKET_RIGHT));
            for (final Entry<String, String> entry : groupEntry.getValue().entrySet()) {
                writer.println(StringKit.format("{} {} {}", entry.getKey(), this.assignFlag, entry.getValue()));
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
    private String replaceVar(final String group, String value) {
        // 找到所有变量标识
        final Set<String> vars = PatternKit.findAll(varRegex, value, 0, new HashSet<>());
        String key;
        for (final String var : vars) {
            key = PatternKit.get(varRegex, var, 1);
            if (StringKit.isNotBlank(key)) {
                // 本分组中查找变量名对应的值
                String varValue = this.groupedMap.get(group, key);
                // 跨分组查找
                if (null == varValue) {
                    final List<String> groupAndKey = CharsBacker.split(key, Symbol.DOT, 2, true, false);
                    if (groupAndKey.size() > 1) {
                        varValue = this.groupedMap.get(groupAndKey.get(0), groupAndKey.get(1));
                    }
                }
                // 系统参数和环境变量中查找
                if (null == varValue) {
                    varValue = Keys.get(key);
                }

                if (null != varValue) {
                    // 替换标识
                    value = value.replace(var, varValue);
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
