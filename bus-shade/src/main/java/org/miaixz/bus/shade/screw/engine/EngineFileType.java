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
package org.miaixz.bus.shade.screw.engine;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * Enumeration of supported file types for document generation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */

public enum EngineFileType implements Serializable {

    /**
     * HTML file type.
     */
    HTML(".html", "html", "HTML File"),
    /**
     * Microsoft Word (.doc) file type.
     */
    WORD(".doc", "word", "WORD File"),
    /**
     * Markdown file type.
     */
    MD(".md", "md", "Markdown File");

    /**
     * The file suffix (e.g., ".html").
     */
    @Getter
    @Setter
    private String fileSuffix;
    /**
     * The prefix for the template file name.
     */
    @Getter
    @Setter
    private String templateNamePrefix;
    /**
     * A description of the file type.
     */
    @Getter
    @Setter
    private String desc;

    /**
     * Constructs an {@code EngineFileType} enum constant.
     *
     * @param type         The file suffix.
     * @param templateFile The prefix for the template file name.
     * @param desc         A description of the file type.
     */
    EngineFileType(String type, String templateFile, String desc) {
        this.fileSuffix = type;
        this.templateNamePrefix = templateFile;
        this.desc = desc;
    }

}
