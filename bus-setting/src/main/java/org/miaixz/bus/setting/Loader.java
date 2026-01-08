/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.setting;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

import org.miaixz.bus.core.io.LineReader;
import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.NotFoundException;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.PatternKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.setting.format.*;
import org.miaixz.bus.setting.magic.GroupedMap;
import org.miaixz.bus.setting.metric.ini.*;

/**
 * A loader for {@code .setting} files. This class handles the parsing of INI-style configuration files, including
 * support for sections (groups), variable substitution, and custom line formatters.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Loader {

    /**
     * The character that indicates the beginning of a comment line.
     */
    private static final char COMMENT_FLAG_PRE = Symbol.C_HASH;
    /**
     * The character used to separate keys from values.
     */
    private char assignFlag = Symbol.C_EQUAL;
    /**
     * The character set used to read the settings file.
     */
    private java.nio.charset.Charset charset;
    /**
     * Whether to enable variable substitution.
     */
    private boolean isUseVariable;
    /**
     * The regular expression for identifying variables.
     */
    private String varRegex = "\\$\\{(.*?)\\}";
    /**
     * An editor for modifying values as they are being loaded.
     */
    private ValueEditor valueEditor;
    /**
     * A factory for creating the line formatter.
     */
    private Factory formatterFactory;
    /**
     * A supplier for the comment formatter.
     */
    private Supplier<ElementFormatter<IniComment>> commentElementFormatterSupplier = CommentFormatter::new;
    /**
     * A supplier for the section formatter.
     */
    private Supplier<ElementFormatter<IniSection>> sectionElementFormatterSupplier = SectionFormatter::new;
    /**
     * A supplier for the property formatter.
     */
    private Supplier<ElementFormatter<IniProperty>> propertyElementFormatterSupplier = PropertyFormatter::new;

    /**
     * Constructs a new Loader with a default formatter factory.
     */
    public Loader() {
        this.formatterFactory = DefaultFormatter::new;
    }

    /**
     * Constructs a new Loader with a custom formatter factory.
     * 
     * @param formatterFactory The factory to create the line formatter.
     */
    public Loader(Factory formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    /**
     * Constructs a new Loader with specified settings.
     *
     * @param charset       The character set to use.
     * @param isUseVariable {@code true} to enable variable substitution.
     */
    public Loader(final java.nio.charset.Charset charset, final boolean isUseVariable) {
        this.charset = charset;
        this.isUseVariable = isUseVariable;
    }

    /**
     * Loads a settings file from the given resource.
     *
     * @param resource The resource representing the configuration file.
     * @return The loaded settings as a {@link GroupedMap}.
     * @throws NotFoundException if the resource cannot be found or read.
     */
    public GroupedMap load(final Resource resource) throws NotFoundException {
        Assert.notNull(resource, "Null setting resource define!");

        GroupedMap groupedMap;
        try (InputStream settingStream = resource.getStream()) {
            groupedMap = load(settingStream);
        } catch (final Exception e) {
            if (e instanceof NotFoundException) {
                throw (NotFoundException) e;
            }
            throw new NotFoundException(e);
        }
        return groupedMap;
    }

    /**
     * Loads settings from an {@link InputStream}. This method does not close the stream.
     *
     * @param inputStream The input stream to read from.
     * @return The loaded settings as a {@link GroupedMap}.
     * @throws IOException if an I/O error occurs.
     */
    public synchronized GroupedMap load(final InputStream inputStream) throws IOException {
        final GroupedMap groupedMap = new GroupedMap();
        try (LineReader reader = new LineReader(inputStream, this.charset)) {
            String group = null;
            String line;
            while ((line = reader.readLine()) != null) {
                line = StringKit.trim(line);
                if (StringKit.isBlank(line) || StringKit.startWith(line, COMMENT_FLAG_PRE)) {
                    continue;
                }
                if (StringKit.isWrap(line, Symbol.C_BRACKET_LEFT, Symbol.C_BRACKET_RIGHT)) {
                    group = StringKit.trim(line.substring(1, line.length() - 1));
                    continue;
                }

                final String[] keyValue = CharsBacker.split(line, String.valueOf(this.assignFlag), 2, true, false)
                        .toArray(new String[0]);
                if (keyValue.length < 2) {
                    continue;
                }

                final String key = StringKit.trim(keyValue[0]);
                String value = keyValue[1];
                if (null != this.valueEditor) {
                    value = this.valueEditor.edit(group, key, value);
                }
                if (this.isUseVariable) {
                    value = replaceVar(groupedMap, group, value);
                }
                groupedMap.put(group, key, value);
            }
        }
        return groupedMap;
    }

    /**
     * Stores the current settings to a file at the specified absolute path, overwriting its content.
     *
     * @param groupedMap   The map of grouped settings to store.
     * @param absolutePath The absolute path to the destination file.
     */
    public void store(final GroupedMap groupedMap, final String absolutePath) {
        store(groupedMap, FileKit.touch(absolutePath));
    }

    /**
     * Stores the current settings to a file, overwriting its content.
     *
     * @param groupedMap The map of grouped settings to store.
     * @param file       The destination file.
     */
    public void store(final GroupedMap groupedMap, final File file) {
        Assert.notNull(file, "File to store must be not null !");
        Logger.debug("Store Setting to [{}]...", file.getAbsolutePath());
        try (PrintWriter writer = FileKit.getPrintWriter(file, charset, false)) {
            store(groupedMap, writer);
        }
    }

    /**
     * Sets the regular expression for identifying variables (e.g., {@code "\\$\\{(.*?)\\}"}).
     *
     * @param regex The regular expression.
     * @return this {@code Loader} instance for chaining.
     */
    public Loader setVarRegex(final String regex) {
        this.varRegex = regex;
        return this;
    }

    /**
     * Sets the character used to separate keys from values.
     *
     * @param assignFlag The assignment character.
     * @return this {@code Loader} instance for chaining.
     */
    public Loader setAssignFlag(final char assignFlag) {
        this.assignFlag = assignFlag;
        return this;
    }

    /**
     * Sets a custom value editor, which can be used to modify values (e.g., for decryption) as they are being loaded.
     * This is called before variable substitution.
     *
     * @param valueEditor The value editor function.
     * @return this {@code Loader} instance for chaining.
     */
    public Loader setValueEditor(final ValueEditor valueEditor) {
        this.valueEditor = valueEditor;
        return this;
    }

    /**
     * Stores the {@link GroupedMap} content to the given {@link PrintWriter}.
     *
     * @param groupedMap The map of grouped settings.
     * @param writer     The writer to use.
     */
    private synchronized void store(final GroupedMap groupedMap, final PrintWriter writer) {
        for (final Entry<String, LinkedHashMap<String, String>> groupEntry : groupedMap.entrySet()) {
            writer.println(
                    StringKit.format("{}{}{}", Symbol.C_BRACKET_LEFT, groupEntry.getKey(), Symbol.C_BRACKET_RIGHT));
            for (final Entry<String, String> entry : groupEntry.getValue().entrySet()) {
                writer.println(StringKit.format("{} {} {}", entry.getKey(), this.assignFlag, entry.getValue()));
            }
        }
    }

    /**
     * Replaces variable placeholders (e.g., {@code ${var}}) in a given string with their resolved values.
     *
     * @param groupedMap The map to look up variable values.
     * @param group      The current group context.
     * @param value      The string containing placeholders.
     * @return The string with variables replaced.
     */
    private String replaceVar(final GroupedMap groupedMap, final String group, String value) {
        final Set<String> vars = PatternKit.findAll(varRegex, value, 0, new HashSet<>());
        for (final String var : vars) {
            String key = PatternKit.get(varRegex, var, 1);
            if (StringKit.isNotBlank(key)) {
                String varValue = groupedMap.get(group, key); // Look in current group.
                if (null == varValue) { // Look in other groups.
                    final List<String> groupAndKey = CharsBacker.split(key, Symbol.DOT, 2, true, false);
                    if (groupAndKey.size() > 1) {
                        varValue = groupedMap.get(groupAndKey.get(0), groupAndKey.get(1));
                    }
                }
                if (null == varValue) { // Look in system properties and environment variables.
                    varValue = Keys.get(key);
                }

                if (null != varValue) {
                    value = value.replace(var, varValue);
                }
            }
        }
        return value;
    }

    /**
     * Gets a default formatter using the configured factory and suppliers.
     *
     * @return A new {@link Format} instance.
     */
    protected Format getFormatter() {
        return formatterFactory.apply(
                commentElementFormatterSupplier.get(),
                sectionElementFormatterSupplier.get(),
                propertyElementFormatterSupplier.get());
    }

    /**
     * Reads INI data from an {@link InputStream}.
     *
     * @param in an INI data input stream.
     * @return The parsed INI data as an {@link IniSetting}.
     * @throws IOException if an I/O error occurs.
     */
    public IniSetting read(InputStream in) throws IOException {
        return read(new InputStreamReader(in));
    }

    /**
     * Reads an INI file.
     *
     * @param file The INI file.
     * @return The parsed INI data as an {@link IniSetting}.
     * @throws IOException if an I/O error occurs.
     */
    public IniSetting read(File file) throws IOException {
        try (java.io.Reader reader = new FileReader(file)) {
            return read(reader);
        }
    }

    /**
     * Reads an INI file from a {@link Path}.
     *
     * @param path The path to the INI file.
     * @return The parsed INI data as an {@link IniSetting}.
     * @throws IOException if an I/O error occurs.
     */
    public IniSetting read(Path path) throws IOException {
        try (java.io.Reader reader = Files.newBufferedReader(path)) {
            return read(reader);
        }
    }

    /**
     * Reads and parses INI data from a {@link Reader}.
     *
     * @param reader The reader containing the INI data.
     * @return The parsed INI data as an {@link IniSetting}.
     * @throws IOException if an I/O error occurs.
     */
    public IniSetting read(Reader reader) throws IOException {
        final BufferedReader bufReader = (reader instanceof BufferedReader) ? (BufferedReader) reader
                : new BufferedReader(reader);
        return bufferedRead(bufReader);
    }

    /**
     * Parses the content from a reader line by line using the default format.
     *
     * @param reader The reader to parse.
     * @return The parsed INI data as an {@link IniSetting}.
     * @throws IOException if an I/O error occurs.
     */
    protected IniSetting defaultFormat(java.io.Reader reader) throws IOException {
        return defaultFormat(reader, Normal._16);
    }

    /**
     * Parses the content from a reader line by line.
     *
     * @param reader          The reader to parse.
     * @param builderCapacity The initial capacity for the line buffer.
     * @return The parsed INI data as an {@link IniSetting}.
     * @throws IOException if an I/O error occurs.
     */
    protected IniSetting defaultFormat(java.io.Reader reader, int builderCapacity) throws IOException {
        Format format = getFormatter();
        List<IniElement> iniElements = new ArrayList<>();
        String newLineSplit = System.getProperty(Keys.LINE_SEPARATOR, Symbol.LF);
        StringBuilder line = new StringBuilder(builderCapacity);

        int ch;
        while ((ch = reader.read()) != -1) {
            line.append((char) ch);
            String nowStr = line.toString();
            if (nowStr.endsWith(newLineSplit)) {
                IniElement element = format.formatLine(nowStr);
                if (null != element) {
                    iniElements.add(element);
                }
                line.setLength(0);
            }
        }
        if (!line.isEmpty()) {
            iniElements.add(format.formatLine(line.toString()));
        }

        return new IniSetting(iniElements);
    }

    /**
     * Reads from a {@link BufferedReader} and parses the content into an {@link IniSetting}.
     *
     * @param reader The buffered reader.
     * @return The parsed {@link IniSetting}.
     * @throws IOException if an I/O error occurs.
     */
    private IniSetting bufferedRead(BufferedReader reader) throws IOException {
        return defaultFormat(reader);
    }

    /**
     * @return The supplier for the comment formatter.
     */
    public Supplier<ElementFormatter<IniComment>> getCommentElementFormatterSupplier() {
        return commentElementFormatterSupplier;
    }

    /**
     * Sets the supplier for the comment formatter.
     * 
     * @param commentElementFormatterSupplier The new supplier.
     */
    public void setCommentElementFormatterSupplier(
            Supplier<ElementFormatter<IniComment>> commentElementFormatterSupplier) {
        this.commentElementFormatterSupplier = commentElementFormatterSupplier;
    }

    /**
     * @return The supplier for the section formatter.
     */
    public Supplier<ElementFormatter<IniSection>> getSectionElementFormatterSupplier() {
        return sectionElementFormatterSupplier;
    }

    /**
     * Sets the supplier for the section formatter.
     * 
     * @param sectionElementFormatterSupplier The new supplier.
     */
    public void setSectionElementFormatterSupplier(
            Supplier<ElementFormatter<IniSection>> sectionElementFormatterSupplier) {
        this.sectionElementFormatterSupplier = sectionElementFormatterSupplier;
    }

    /**
     * @return The supplier for the property formatter.
     */
    public Supplier<ElementFormatter<IniProperty>> getPropertyElementFormatterSupplier() {
        return propertyElementFormatterSupplier;
    }

    /**
     * Sets the supplier for the property formatter.
     * 
     * @param propertyElementFormatterSupplier The new supplier.
     */
    public void setPropertyElementFormatterSupplier(
            Supplier<ElementFormatter<IniProperty>> propertyElementFormatterSupplier) {
        this.propertyElementFormatterSupplier = propertyElementFormatterSupplier;
    }

    /**
     * A functional interface for editing a property value during the loading process. This can be used for tasks like
     * decrypting encrypted values.
     */
    @FunctionalInterface
    public interface ValueEditor {

        /**
         * Edits the given value.
         *
         * @param group The group the property belongs to.
         * @param key   The property key.
         * @param value The original property value.
         * @return The edited value.
         */
        String edit(String group, String key, String value);
    }

}
