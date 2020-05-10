/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.office.magic.family;

import java.util.Set;

/**
 * 默认的{@code DocumentFormat}注册表。
 * 应该足以满足我们的大部分需求.
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
public final class DefaultFormatRegistry {

    /**
     * Portable Document Format.
     *
     * <ul>
     *   <li>Extension: pdf
     *   <li>Media Type: application/pdf
     * </ul>
     */
    public static final DocumentFormat PDF = getInstance().getFormatByExtension("pdf");

    /**
     * Macromedia Flash.
     *
     * <ul>
     *   <li>Extension: swf
     *   <li>Media Type: application/x-shockwave-flash
     * </ul>
     */
    public static final DocumentFormat SWF = getInstance().getFormatByExtension("swf");

    /**
     * HTML.
     *
     * <ul>
     *   <li>Extension: html
     *   <li>Media Type: text/html
     * </ul>
     */
    public static final DocumentFormat HTML = getInstance().getFormatByExtension("html");

    /**
     * XHTML.
     *
     * <ul>
     *   <li>Extension: xhtml
     *   <li>Media Type: application/xhtml+xml
     * </ul>
     */
    public static final DocumentFormat XHTML = getInstance().getFormatByExtension("xhtml");

    /**
     * OpenDocument Text.
     *
     * <ul>
     *   <li>Extension: odt
     *   <li>Media Type: application/vnd.oasis.opendocument.text
     * </ul>
     */
    public static final DocumentFormat ODT = getInstance().getFormatByExtension("odt");

    /**
     * OpenDocument Text Template.
     *
     * <ul>
     *   <li>Extension: ott
     *   <li>Media Type: application/vnd.oasis.opendocument.text-template
     * </ul>
     */
    public static final DocumentFormat OTT = getInstance().getFormatByExtension("ott");

    /**
     * OpenDocument Text Flat XML.
     *
     * <ul>
     *   <li>Extension: fodt
     *   <li>Media Type: application/vnd.oasis.opendocument.text-flat-xml
     * </ul>
     */
    public static final DocumentFormat FODT = getInstance().getFormatByExtension("fodt");

    /**
     * OpenOffice.org 1.0 Text Document.
     *
     * <ul>
     *   <li>Extension: swx
     *   <li>Media Type: application/vnd.sun.xml.writer
     * </ul>
     */
    public static final DocumentFormat SXW = getInstance().getFormatByExtension("sxw");

    /**
     * Microsoft Word 97-2003.
     *
     * <ul>
     *   <li>Extension: doc
     *   <li>Media Type: application/msword
     * </ul>
     */
    public static final DocumentFormat DOC = getInstance().getFormatByExtension("doc");

    /**
     * Microsoft Word 2007-2013 XML.
     *
     * <ul>
     *   <li>Extension: docx
     *   <li>Media Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document
     * </ul>
     */
    public static final DocumentFormat DOCX = getInstance().getFormatByExtension("docx");

    /**
     * Rich Text Format.
     *
     * <ul>
     *   <li>Extension: rtf
     *   <li>Media Type: text/rtf"
     * </ul>
     */
    public static final DocumentFormat RTF = getInstance().getFormatByExtension("rtf");

    /**
     * WordPerfect.
     *
     * <ul>
     *   <li>Extension: wpd
     *   <li>Media Type: application/wordperfect
     * </ul>
     */
    public static final DocumentFormat WPD = getInstance().getFormatByExtension("wpd");

    /**
     * Plain Text.
     *
     * <ul>
     *   <li>Extension: txt
     *   <li>Media Type: text/plain
     * </ul>
     */
    public static final DocumentFormat TXT = getInstance().getFormatByExtension("txt");

    /**
     * OpenDocument Spreadsheet.
     *
     * <ul>
     *   <li>Extension: ods
     *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet
     * </ul>
     */
    public static final DocumentFormat ODS = getInstance().getFormatByExtension("ods");

    /**
     * OpenDocument Spreadsheet Template.
     *
     * <ul>
     *   <li>Extension: ots
     *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet-template
     * </ul>
     */
    public static final DocumentFormat OTS = getInstance().getFormatByExtension("ots");

    /**
     * OpenDocument Spreadsheet Flat XML.
     *
     * <ul>
     *   <li>Extension: fods
     *   <li>Media Type: application/vnd.oasis.opendocument.spreadsheet-flat-xml
     * </ul>
     */
    public static final DocumentFormat FODS = getInstance().getFormatByExtension("fods");

    /**
     * OpenOffice.org 1.0 Spreadsheet.
     *
     * <ul>
     *   <li>Extension: sxc
     *   <li>Media Type: application/vnd.sun.xml.calc
     * </ul>
     */
    public static final DocumentFormat SXC = getInstance().getFormatByExtension("sxc");

    /**
     * Microsoft Excel 97-2003.
     *
     * <ul>
     *   <li>Extension: xls
     *   <li>Media Type: application/vnd.ms-excel
     * </ul>
     */
    public static final DocumentFormat XLS = getInstance().getFormatByExtension("xls");

    /**
     * Microsoft Excel 2007-2013 XML.
     *
     * <ul>
     *   <li>Extension: xlsx
     *   <li>Media Type: application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
     * </ul>
     */
    public static final DocumentFormat XLSX = getInstance().getFormatByExtension("xlsx");

    /**
     * Comma Separated Values.
     *
     * <ul>
     *   <li>Extension: csv
     *   <li>Media Type: text/csv
     * </ul>
     */
    public static final DocumentFormat CSV = getInstance().getFormatByExtension("csv");

    /**
     * Tab Separated Values.
     *
     * <ul>
     *   <li>Extension: tsv
     *   <li>Media Type: text/tab-separated-values
     * </ul>
     */
    public static final DocumentFormat TSV = getInstance().getFormatByExtension("tsv");

    /**
     * OpenDocument Presentation.
     *
     * <ul>
     *   <li>Extension: odp
     *   <li>Media Type: application/vnd.oasis.opendocument.presentation
     * </ul>
     */
    public static final DocumentFormat ODP = getInstance().getFormatByExtension("odp");

    /**
     * OpenDocument Presentation Template.
     *
     * <ul>
     *   <li>Extension: otp
     *   <li>Media Type: application/vnd.oasis.opendocument.presentation-template
     * </ul>
     */
    public static final DocumentFormat OTP = getInstance().getFormatByExtension("otp");

    /**
     * OpenDocument Presentation Flat XML.
     *
     * <ul>
     *   <li>Extension: fodp
     *   <li>Media Type: application/vnd.oasis.opendocument.presentation-flat-xml
     * </ul>
     */
    public static final DocumentFormat FODP = getInstance().getFormatByExtension("fodp");

    /**
     * OpenOffice.org 1.0 Presentation.
     *
     * <ul>
     *   <li>Extension: sxi
     *   <li>Media Type: application/vnd.sun.xml.impress
     * </ul>
     */
    public static final DocumentFormat SXI = getInstance().getFormatByExtension("sxi");

    /**
     * Microsoft PowerPoint 97-2003.
     *
     * <ul>
     *   <li>Extension: ppt
     *   <li>Media Type: application/vnd.ms-powerpoint
     * </ul>
     */
    public static final DocumentFormat PPT = getInstance().getFormatByExtension("ppt");

    /**
     * Microsoft PowerPoint 2007-2013 XML.
     *
     * <ul>
     *   <li>Extension: pptx
     *   <li>Media Type: application/vnd.openxmlformats-officedocument.presentationml.presentation
     * </ul>
     */
    public static final DocumentFormat PPTX = getInstance().getFormatByExtension("pptx");

    /**
     * OpenDocument Drawing.
     *
     * <ul>
     *   <li>Extension: odg
     *   <li>Media Type: application/vnd.oasis.opendocument.graphics
     * </ul>
     */
    public static final DocumentFormat ODG = getInstance().getFormatByExtension("odg");

    /**
     * OpenDocument Drawing Template.
     *
     * <ul>
     *   <li>Extension: otg
     *   <li>Media Type: application/vnd.oasis.opendocument.graphics
     * </ul>
     */
    public static final DocumentFormat OTG = getInstance().getFormatByExtension("otg");

    /**
     * OpenDocument Drawing Flat XML.
     *
     * <ul>
     *   <li>Extension: fodg
     *   <li>Media Type: application/vnd.oasis.opendocument.graphics-flat-xml
     * </ul>
     */
    public static final DocumentFormat FODG = getInstance().getFormatByExtension("fodg");

    /**
     * Scalable Vector Graphics.
     *
     * <ul>
     *   <li>Extension: svg
     *   <li>Media Type: image/svg+xml
     * </ul>
     */
    public static final DocumentFormat SVG = getInstance().getFormatByExtension("svg");

    /**
     * Visio format.
     *
     * <ul>
     *   <li>Extension: vsd
     *   <li>Media Type: application/x-visio
     * </ul>
     */
    public static final DocumentFormat VSD = getInstance().getFormatByExtension("vsd");

    /**
     * New Visio format.
     *
     * <ul>
     *   <li>Extension: vsdx
     *   <li>Media Type: application/vnd-ms-visio.drawing
     * </ul>
     */
    public static final DocumentFormat VSDX = getInstance().getFormatByExtension("vsdx");

    /**
     * Portable Network Graphics.
     *
     * <ul>
     *   <li>Extension: png
     *   <li>Media Type: image/png
     * </ul>
     */
    public static final DocumentFormat PNG = getInstance().getFormatByExtension("png");

    /**
     * Joint Photographic Experts Group.
     *
     * <ul>
     *   <li>Extensions: jpg, jpeg
     *   <li>Media Type: image/jpg
     * </ul>
     */
    public static final DocumentFormat JPEG = getInstance().getFormatByExtension("jpg");

    /**
     * Tagged Image File Format.
     *
     * <ul>
     *   <li>Extensions: tif, tiff
     *   <li>Media Type: image/tif
     * </ul>
     */
    public static final DocumentFormat TIFF = getInstance().getFormatByExtension("tif");

    /**
     * Graphic Interchange Format.
     *
     * <ul>
     *   <li>Extension: gif
     *   <li>Media Type: image/gif
     * </ul>
     */
    public static final DocumentFormat GIF = getInstance().getFormatByExtension("gif");

    /**
     * Windows Bitmap.
     *
     * <ul>
     *   <li>Extension: bmp
     *   <li>Media Type: image/bmp
     * </ul>
     */
    public static final DocumentFormat BMP = getInstance().getFormatByExtension("bmp");

    /**
     * 获取类的默认实例.
     *
     * @return 默认DocumentFormatRegistry.
     */
    public static FormatRegistry getInstance() {
        return RegistryInstanceHolder.getInstance();
    }

    /**
     * 获取指定扩展名的文档格式.
     *
     * @param extension 将返回其文档格式的扩展名.
     * @return 如果指定的扩展不存在文档格式，则使用找到的文档格式，或者使用{@code null}.
     */
    public static DocumentFormat getFormatByExtension(final String extension) {
        return getInstance().getFormatByExtension(extension);
    }

    /**
     * 获取指定媒体类型的文档格式.
     *
     * @param mediaType 将返回其文档格式的媒体类型.
     * @return 如果指定的媒体类型不存在文档格式，则使用找到的文档格式，或者使用{@code null}.
     */
    public static DocumentFormat getFormatByMediaType(final String mediaType) {
        return getInstance().getFormatByMediaType(mediaType);
    }

    /**
     * 获取给定系列的所有{@link DocumentFormat}.
     *
     * @param family 将返回其文档格式的集合.
     * @return 包含指定系列的所有文档格式的集合.
     */
    public static Set<DocumentFormat> getOutputFormats(final FamilyType family) {
        return getInstance().getOutputFormats(family);
    }

}
