/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.office.ofd;

import org.miaixz.bus.core.io.file.PathResolve;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.IoKit;
import org.ofdrw.font.Font;
import org.ofdrw.layout.OFDDoc;
import org.ofdrw.layout.edit.Annotation;
import org.ofdrw.layout.element.Div;
import org.ofdrw.layout.element.Img;
import org.ofdrw.layout.element.Paragraph;
import org.ofdrw.reader.OFDReader;

import java.io.*;
import java.nio.file.Path;

/**
 * OFD文件生成器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class OfdWriter implements Serializable, Closeable {

    private static final long serialVersionUID = -1L;

    private final OFDDoc doc;

    /**
     * 构造
     *
     * @param file 生成的文件
     */
    public OfdWriter(final File file) {
        this(file.toPath());
    }

    /**
     * 构造
     *
     * @param file 生成的文件
     */
    public OfdWriter(final Path file) {
        try {
            if (PathResolve.exists(file, true)) {
                this.doc = new OFDDoc(new OFDReader(file), file);
            } else {
                this.doc = new OFDDoc(file);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 构造
     *
     * @param out 需要输出的流
     */
    public OfdWriter(final OutputStream out) {
        this.doc = new OFDDoc(out);
    }

    /**
     * 增加文本内容
     *
     * @param font  字体
     * @param texts 文本
     * @return this
     */
    public OfdWriter addText(final Font font, final String... texts) {
        final Paragraph paragraph = new Paragraph();
        if (null != font) {
            paragraph.setDefaultFont(font);
        }
        for (final String text : texts) {
            paragraph.add(text);
        }
        return add(paragraph);
    }

    /**
     * 追加图片
     *
     * @param picFile 图片文件
     * @param width   宽度
     * @param height  高度
     * @return this
     */
    public OfdWriter addPicture(final File picFile, final int width, final int height) {
        return addPicture(picFile.toPath(), width, height);
    }

    /**
     * 追加图片
     *
     * @param picFile 图片文件
     * @param width   宽度
     * @param height  高度
     * @return this
     */
    public OfdWriter addPicture(final Path picFile, final int width, final int height) {
        final Img img;
        try {
            img = new Img(width, height, picFile);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return add(img);
    }

    /**
     * 增加节点
     *
     * @param div 节点，可以是段落、Canvas、Img或者填充
     * @return this
     */
    public OfdWriter add(final Div div) {
        this.doc.add(div);
        return this;
    }

    /**
     * 增加注释，比如水印等
     *
     * @param page       页码
     * @param annotation 节点，可以是段落、Canvas、Img或者填充
     * @return this
     */
    public OfdWriter add(final int page, final Annotation annotation) {
        try {
            this.doc.addAnnotation(page, annotation);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return this;
    }

    @Override
    public void close() {
        IoKit.closeQuietly(this.doc);
    }

}
