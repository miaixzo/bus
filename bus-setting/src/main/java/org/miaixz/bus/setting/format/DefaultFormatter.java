/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.setting.format;

import java.util.Objects;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.setting.Format;
import org.miaixz.bus.setting.metric.ini.IniComment;
import org.miaixz.bus.setting.metric.ini.IniElement;
import org.miaixz.bus.setting.metric.ini.IniProperty;
import org.miaixz.bus.setting.metric.ini.IniSection;

/**
 * The default line formatter for INI files. It uses a chain of responsibility pattern, attempting to parse a line as a
 * comment, a section, or a property, in that order.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DefaultFormatter implements Format {

    /**
     * Formatter for comment lines.
     */
    protected final ElementFormatter<IniComment> commentElementFormatter;
    /**
     * Formatter for section headers.
     */
    protected final ElementFormatter<IniSection> sectionElementFormatter;
    /**
     * Formatter for property lines (key-value pairs).
     */
    protected final ElementFormatter<IniProperty> propertyElementFormatter;
    /**
     * The most recently parsed section, used to associate properties with it.
     */
    protected IniSection lastSection;
    /**
     * The current physical line number being read.
     */
    private int lineNumber = 0;
    /**
     * The current effective line number, excluding empty lines.
     */
    private int effectiveLineNumber = 0;

    /**
     * Constructs a new DefaultFormatter with the specified element formatters.
     *
     * @param commentElementFormatter  The formatter for comments.
     * @param sectionElementFormatter  The formatter for sections.
     * @param propertyElementFormatter The formatter for properties.
     */
    public DefaultFormatter(ElementFormatter<IniComment> commentElementFormatter,
            ElementFormatter<IniSection> sectionElementFormatter,
            ElementFormatter<IniProperty> propertyElementFormatter) {
        this.commentElementFormatter = commentElementFormatter;
        this.sectionElementFormatter = sectionElementFormatter;
        this.propertyElementFormatter = propertyElementFormatter;
    }

    /**
     * Formats a raw line from an INI file into an {@link IniElement}. Empty lines are ignored and return null.
     *
     * @param raw The raw string data for the line.
     * @return The parsed {@link IniElement}, or null for an empty line.
     * @throws InternalException if the line cannot be parsed as a comment, section, or property.
     */
    @Override
    public IniElement formatLine(String raw) {
        Objects.requireNonNull(raw);
        lineNumber++;
        String line = raw.trim();
        if (line.isEmpty()) {
            return null;
        }

        IniElement element;
        int preEffectiveLineNumber = effectiveLineNumber + 1;

        if (commentElementFormatter.check(line)) {
            element = commentElementFormatter.format(line, preEffectiveLineNumber);
        } else if (sectionElementFormatter.check(line)) {
            IniSection section = sectionElementFormatter.format(line, preEffectiveLineNumber);
            lastSection = section;
            element = section;
        } else if (propertyElementFormatter.check(line)) {
            IniProperty property = propertyElementFormatter.format(line, preEffectiveLineNumber);
            if (null == lastSection) {
                throw new InternalException("Cannot find section for property on line " + lineNumber + ": " + line);
            }
            property.setSection(lastSection);
            lastSection.add(property);
            element = property;
        } else {
            throw new InternalException("No matching element type found for line " + lineNumber + ": " + line);
        }

        effectiveLineNumber = preEffectiveLineNumber;
        return element;
    }

    /**
     * Resets the formatter to its initial state, clearing the line counters and the last seen section. This should be
     * called before parsing a new file.
     */
    @Override
    public synchronized void init() {
        lineNumber = 0;
        effectiveLineNumber = 0;
        lastSection = null;
    }

}
