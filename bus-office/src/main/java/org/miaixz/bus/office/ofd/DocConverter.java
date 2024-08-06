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

import java.io.IOException;
import java.nio.file.Path;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.ofdrw.converter.export.*;
import org.ofdrw.converter.ofdconverter.ImageConverter;
import org.ofdrw.converter.ofdconverter.PDFConverter;
import org.ofdrw.converter.ofdconverter.TextConverter;

/**
 * 基于{@code ofdrw-converter}文档转换，提供：
 * <ul>
 * <li>OFD PDF 相互转换</li>
 * <li>OFD TEXT 相互转换</li>
 * <li>OFD 图片 相互转换</li>
 * </ul>
 * 具体见:https://toscode.gitee.com/ofdrw/ofdrw/blob/master/ofdrw-converter/doc/CONVERTER.md
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DocConverter {

    /**
     * PDF转为ODF
     *
     * @param src    PDF文件路径
     * @param target OFD文件路径
     * @param pages  页码,（从0起）
     */
    public static void pdfToOfd(final Path src, final Path target, final int... pages) {
        try (final org.ofdrw.converter.ofdconverter.DocConverter converter = new PDFConverter(target)) {
            converter.convert(src, pages);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 纯文本转为ODF
     *
     * @param src      纯文件路径
     * @param target   OFD文件路径
     * @param fontSize 字体大小
     */
    public static void textToOfd(final Path src, final Path target, final double fontSize) {
        try (final TextConverter converter = new TextConverter(target)) {
            converter.setFontSize(fontSize);
            converter.convert(src);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 多个图片转为ODF
     *
     * @param target OFD文件路径
     * @param images 图片列表
     */
    public static void imgToOfd(final Path target, final Path... images) {
        try (final org.ofdrw.converter.ofdconverter.DocConverter converter = new ImageConverter(target)) {
            for (final Path image : images) {
                converter.convert(image);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * OFD转图片
     *
     * @param src       ODF路径
     * @param targetDir 生成图片存放目录
     * @param imgType   生成图片的格式，如 JPG、PNG、GIF、BMP、SVG
     * @param ppm       转换图片质量，每毫米像素数量(Pixels per millimeter)
     */
    public static void odfToImage(final Path src, final Path targetDir, final String imgType, final double ppm) {
        if ("svg".equalsIgnoreCase(imgType)) {
            odfToSvg(src, targetDir, ppm);
        }
        try (final ImageExporter exporter = new ImageExporter(src, targetDir, imgType, ppm)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * OFD转HTML
     *
     * @param src        ODF路径
     * @param targetPath 生成HTML路径
     */
    public static void odfToHtml(final Path src, final Path targetPath) {
        try (final HTMLExporter exporter = new HTMLExporter(src, targetPath)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * OFD转文本
     *
     * @param src        ODF路径
     * @param targetPath 生成文本路径
     */
    public static void odfToText(final Path src, final Path targetPath) {
        try (final TextExporter exporter = new TextExporter(src, targetPath)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * OFD转PDF
     *
     * @param src        ODF路径
     * @param targetPath 生成PDF路径
     */
    public static void odfToPdf(final Path src, final Path targetPath) {
        try (final OFDExporter exporter = new PDFExporterPDFBox(src, targetPath)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        } catch (final Exception e) {
            // 当用户未引入PDF-BOX时,尝试iText
            try (final OFDExporter exporter = new PDFExporterIText(src, targetPath)) {
                exporter.export();
            } catch (final IOException e2) {
                throw new InternalException(e);
            }
        }
    }

    /**
     * OFD转SVG
     *
     * @param src       ODF路径
     * @param targetDir 生成SVG存放目录
     * @param ppm       转换图片质量，每毫米像素数量(Pixels per millimeter)
     */
    private static void odfToSvg(final Path src, final Path targetDir, final double ppm) {
        try (final SVGExporter exporter = new SVGExporter(src, targetDir, ppm)) {
            exporter.export();
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

}
