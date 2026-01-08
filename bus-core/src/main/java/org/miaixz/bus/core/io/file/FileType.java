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
package org.miaixz.bus.core.io.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * File type determination utility class.
 *
 * <p>
 * This tool attempts to guess the file type based on the first few bytes of the file. It may not be accurate for text
 * and zip files, but is generally accurate for video and image types.
 *
 * <p>
 * It's important to note that Office 2007 formats like XLSX and DOCX are all identified as ZIP files because the new
 * versions use the OpenXML format, which essentially packages XML files into a ZIP archive.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileType {

    /**
     * Java source file extension.
     */
    public static final String JAVA = ".java";
    /**
     * Java compiled class file extension.
     */
    public static final String CLASS = ".class";
    /**
     * Java Archive (JAR) file extension.
     */
    public static final String JAR = ".jar";
    /**
     * Extension form for paths within a JAR file.
     */
    public static final String JAR_PATH_EXT = ".jar!";
    /**
     * Microsoft Excel document extension (legacy).
     */
    public static final String TYPE_XLS = ".xls";
    /**
     * Microsoft Excel document extension (OpenXML).
     */
    public static final String TYPE_XLSX = ".xlsx";
    /**
     * Microsoft Word document extension (legacy).
     */
    public static final String TYPE_DOC = ".doc";
    /**
     * Microsoft Word document extension (OpenXML).
     */
    public static final String TYPE_DOCX = ".docx";
    /**
     * Microsoft PowerPoint document extension (legacy).
     */
    public static final String TYPE_PPT = ".ppt";
    /**
     * Microsoft PowerPoint document extension (OpenXML).
     */
    public static final String TYPE_PPTX = ".pptx";
    /**
     * Microsoft PowerPoint Slideshow extension (legacy).
     */
    public static final String TYPE_PPS = ".pps";
    /**
     * Microsoft PowerPoint Slideshow extension (OpenXML).
     */
    public static final String TYPE_PPSX = ".ppsx";
    /**
     * XML format extension.
     */
    public static final String TYPE_XML = ".xml";

    /**
     * PSD format, Photoshop's proprietary format.
     */
    public static final String TYPE_PSD = "psd";
    /**
     * GIF format.
     */
    public static final String TYPE_GIF = "gif";
    /**
     * JPG format.
     */
    public static final String TYPE_JPG = "jpg";
    /**
     * JPEG format.
     */
    public static final String TYPE_JPEG = "jpeg";
    /**
     * BMP format.
     */
    public static final String TYPE_BMP = "bmp";
    /**
     * PNG format.
     */
    public static final String TYPE_PNG = "png";

    /**
     * CSV format.
     */
    public static final String TYPE_CSV = "csv";

    /**
     * PDF format.
     */
    public static final String TYPE_PDF = "pdf";

    /**
     * DCM format.
     */
    public static final String TYPE_DCM = "dcm";

    /**
     * SVG format.
     */
    public static final String TYPE_SVG = "svg";

    /**
     * TXT format.
     */
    public static final String TYPE_TXT = "txt";

    /**
     * A map storing file type mappings, where the key is the file's hexadecimal header (magic number) or file
     * extension, and the value is the corresponding file type extension. This map is initialized with common image,
     * document, compressed, video, and audio file types.
     */
    public static final Map<String, String> FILE_TYPE = new ConcurrentSkipListMap<>() {

        private static final long serialVersionUID = 1L;

        /**
         * Image formats.
         */
        {
            put(".jpe", "image/jpeg");
            put(".jpeg", "image/jpeg");
            put(".jpg", "image/jpeg");
            put(".bmp", "image/bmp");
            put(".png", "image/png");
            put(".gif", "image/gif");
            put(".jfif", "image/pjpeg");
            put(".dib", "image/bmp");
            put(".pnz", "image/png");
            put(".art", "image/x-jg");
            put(".cmx", "image/x-cmx");
            put(".ico", "image/x-icon");
            put(".ppm", "image/x-portable-pixmap");
            put(".mac", "image/x-macpaint");
            put(".pbm", "image/x-portable-bitmap");
            put(".pgm", "image/x-portable-graymap");
            put(".pnm", "image/x-portable-anymap");
            put(".pnt", "image/x-macpaint");
            put(".pntg", "image/x-macpaint");
            put(".qti", "image/x-quicktime");
            put(".qtif", "image/x-quicktime");
            put(".rgb", "image/x-rgb");
            put(".xwd", "image/x-xwindowdump");
            put(".ras", "image/x-cmu-raster");
            put(".xbm", "image/x-xbitmap");
            put(".xpm", "image/x-xpixmap");
            put(".cod", "image/cis-cod");
            put(".ief", "image/ief");
            put(".pct", "image/pict");
            put(".pic", "image/pict");
            put(".pict", "image/pict");
            put(".rf", "image/vnd.rn-realflash");
            put(".wbmp", "image/vnd.wap.wbmp");
            put(".wdp", "image/vnd.ms-photo");
            put(".tif", "image/tiff");
            put(".tiff", "image/tiff");
        }

        /**
         * Document formats.
         */
        {
            // txt
            put(".txt", "text/plain");
            // css
            put(".css", "text/css");
            // html
            put(".htm", "text/html");
            put(".html", "text/html");
            put(".shtml", "text/html");
            // xml
            put(".wsdl", "text/xml");
            put(".xml", "text/xml");
            // pdf
            put(".pdf", "application/pdf");
            // ppt
            put(".ppt", "application/vnd.ms-powerpoint");
            put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
            // doc
            put(".doc", "application/msword");
            put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            // excel
            put(".xlm", "application/vnd.ms-excel");
            put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            // not
            put(".one", "application/onenote");
            // access
            put(".accdb", "application/msaccess");
            // visio
            put(".vsd", "application/vnd.visio");
            /** Less common document types */
            put(".323", "text/h323");
            put(".rqy", "text/x-ms-rqy");
            put(".rtx", "text/richtext");
            put(".rc", "text/plain");
            put(".XOML", "text/plain");
            put(".sln", "text/plain");
            put(".rgs", "text/plain");
            put(".pkgdef", "text/plain");
            put(".pkgundef", "text/plain");
            put(".sol", "text/plain");
            put(".sor", "text/plain");
            put(".srf", "text/plain");
            put(".xdr", "text/plain");
            put(".rc2", "text/plain");
            put(".rct", "text/plain");
            put(".s", "text/plain");
            put(".asm", "text/plain");
            put(".c", "text/plain");
            put(".cc", "text/plain");
            put(".cd", "text/plain");
            put(".def", "text/plain");
            put(".cxx", "text/plain");
            put(".cnf", "text/plain");
            put(".cpp", "text/plain");
            put(".cs", "text/plain");
            put(".csdproj", "text/plain");
            put(".csproj", "text/plain");
            put(".dbproj", "text/plain");
            put(".bas", "text/plain");
            put(".dsw", "text/plain");
            put(".inc", "text/plain");
            put(".hxx", "text/plain");
            put(".i", "text/plain");
            put(".idl", "text/plain");
            put(".inl", "text/plain");
            put(".lst", "text/plain");
            put(".jsxbin", "text/plain");
            put(".mak", "text/plain");
            put(".map", "text/plain");
            put(".h", "text/plain");
            put(".hpp", "text/plain");
            put(".ipproj", "text/plain");
            put(".mk", "text/plain");
            put(".odh", "text/plain");
            put(".odl", "text/plain");
            put(".tsv", "text/tab-separated-values");
            put(".uls", "text/iuls");
            put(".user", "text/plain");
            put(".tlh", "text/plain");
            put(".tli", "text/plain");
            put(".vb", "text/plain");
            put(".vbdproj", "text/plain");
            put(".vbproj", "text/plain");
            put(".vcs", "text/plain");
            put(".vddproj", "text/plain");
            put(".vdp", "text/plain");
            put(".vdproj", "text/plain");
            put(".vspscc", "text/plain");
            put(".vsscc", "text/plain");
            put(".vssscc", "text/plain");
            put(".hxt", "text/html");
            put(".vssettings", "text/xml");
            put(".vstemplate", "text/xml");
            put(".vml", "text/xml");
            put(".vsct", "text/xml");
            put(".vsixlangpack", "text/xml");
            put(".vsixmanifest", "text/xml");
            put(".exe.config", "text/xml");
            put(".disco", "text/xml");
            put(".dll.config", "text/xml");
            put(".AddIn", "text/xml");
            put(".dtd", "text/xml");
            put(".dtsConfig", "text/xml");
            put(".mno", "text/xml");
            put(".xrm-ms", "text/xml");
            put(".xsd", "text/xml");
            put(".xsf", "text/xml");
            put(".xsl", "text/xml");
            put(".xslt", "text/xml");
            put(".SSISDeploymentManifest", "text/xml");
            put(".iqy", "text/x-ms-iqy");
            put(".contact", "text/x-ms-contact");
            put(".etx", "text/x-setext");
            put(".hdml", "text/x-hdml");
            put(".htc", "text/x-component");
            put(".group", "text/x-ms-group");
            put(".vcf", "text/x-vcard");
            put(".odc", "text/x-ms-odc");
            put(".qht", "text/x-html-insertion");
            put(".qhtm", "text/x-html-insertion");
            put(".wml", "text/vnd.wap.wml");
            put(".wmls", "text/vnd.wap.wmlscript");
            put(".vbs", "text/vbscript");
            put(".jsx", "text/jscript");
            put(".sct", "text/scriptlet");
            put(".csv", "text/csv");
            put(".323", "text/h323");
            put(".dlm", "text/dlm");
            put(".htt", "text/webviewhtml");
            put(".wsc", "text/scriptlet");
            put(".sgml", "text/sgml");
            // ppt
            put(".pot", "application/vnd.ms-powerpoint");
            put(".ppa", "application/vnd.ms-powerpoint");
            put(".pwz", "application/vnd.ms-powerpoint");
            put(".pps", "application/vnd.ms-powerpoint");
            put(".sldm", "application/vnd.ms-powerpoint.slide.macroEnabled.12");
            put(".ppam", "application/vnd.ms-powerpoint.addin.macroEnabled.12");
            put(".potm", "application/vnd.ms-powerpoint.template.macroEnabled.12");
            put(".ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
            put(".pptm", "application/vnd.ms-powerpoint.presentation.macroEnabled.12");
            put(".potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
            put(".ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
            // doc
            put(".wbk", "application/msword");
            put(".wiz", "application/msword");
            put(".dot", "application/msword");
            put(".docm", "application/vnd.ms-word.document.macroEnabled.12");
            put(".dotm", "application/vnd.ms-word.template.macroEnabled.12");
            put(".dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
            // excel
            put(".xla", "application/vnd.ms-excel");
            put(".xlc", "application/vnd.ms-excel");
            put(".xld", "application/vnd.ms-excel");
            put(".xlk", "application/vnd.ms-excel");
            put(".xll", "application/vnd.ms-excel");
            put(".xls", "application/vnd.ms-excel");
            put(".xlt", "application/vnd.ms-excel");
            put(".xlw", "application/vnd.ms-excel");
            put(".slk", "application/vnd.ms-excel");
            put(".xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
            put(".xlsm", "application/vnd.ms-excel.sheet.macroEnabled.12");
            put(".xltm", "application/vnd.ms-excel.template.macroEnabled.12");
            put(".xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
            put(".xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
            // access
            put(".accde", "application/msaccess");
            put(".accdt", "application/msaccess");
            put(".adp", "application/msaccess");
            put(".mda", "application/msaccess");
            put(".mde", "application/msaccess");
            put(".accda", "application/msaccess.addin");
            put(".accdc", "application/msaccess.cab");
            put(".accdr", "application/msaccess.runtime");
            put(".accdw", "application/msaccess.webapplication");
            put(".accft", "application/msaccess.ftemplate");
            put(".ade", "application/msaccess");
            // visio
            put(".thmx", "application/vnd.ms-officetheme");
            put(".vdx", "application/vnd.ms-visio.viewer");
            put(".vss", "application/vnd.visio");
            put(".vst", "application/vnd.visio");
            put(".vsw", "application/vnd.visio");
            put(".vsx", "application/vnd.visio");
            put(".vtx", "application/vnd.visio");
            // note type text
            put(".onea", "application/onenote");
            put(".onepkg", "application/onenote");
            put(".onetmp", "application/onenote");
            put(".onetoc", "application/onenote");
            put(".onetoc2", "application/onenote");
            // other
            put(".pko", "application/vnd.ms-pki.pko");
            put(".cat", "application/vnd.ms-pki.seccat");
            put(".sst", "application/vnd.ms-pki.certstore");
            put(".stl", "application/vnd.ms-pki.stl");
            put(".mpf", "application/vnd.ms-mediapackage");
            put(".mpp", "application/vnd.ms-project");
            put(".wpl", "application/vnd.ms-wpl");
            put(".wks", "application/vnd.ms-works");
            put(".wps", "application/vnd.ms-works");
            put(".wcm", "application/vnd.ms-works");
            put(".wdb", "application/vnd.ms-works");
            put(".calx", "application/vnd.ms-office.calx");
            put(".xps", "application/vnd.ms-xpsdocument");
            put(".odp", "application/vnd.oasis.opendocument.presentation");
            put(".odt", "application/vnd.oasis.opendocument.text");
            put(".rm", "application/vnd.rn-realmedia");
            put(".rmp", "application/vnd.rn-rn_music_package");
            put(".sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
            put(".air", "application/vnd.adobe.air-application-installer-package+zip");
            put(".wmlsc", "application/vnd.wap.wmlscriptc");
            put(".wmlc", "application/vnd.wap.wmlc");
            put(".m13", "application/x-msmediaview");
            put(".m14", "application/x-msmediaview");
            put(".wmf", "application/x-msmetafile");
            put(".wri", "application/x-mswrite");
            put(".mdb", "application/x-msaccess");
            put(".wmd", "application/x-ms-wmd");
            put(".wmz", "application/x-ms-wmz");
        }

        /**
         * Compressed archive formats.
         */
        {
            put(".7z", "application/x-7z-compressed");
            put(".z", "application/x-compress");
            put(".zip", "application/x-zip-compressed");
            put(".tgz", "application/x-compressed");
            put(".jar", "application/java-archive");
            put(".rar", "application/octet-stream");
            put(".war", "application/octet-stream");
            put(".tar", "application/x-tar");
        }

        /**
         * Video formats.
         */
        {
            put(".flv", "video/x-flv");
            put(".3gp", "video/3gpp");
            put(".avi", "video/x-msvideo");
            put(".3g2", "video/3gpp2");
            put(".3gp2", "video/3gpp2");
            put(".3gpp", "video/3gpp");
            put(".asf", "video/x-ms-asf");
            put(".asr", "video/x-ms-asf");
            put(".asx", "video/x-ms-asf");
            put(".dif", "video/x-dv");
            put(".mod", "video/mpeg");
            put(".mov", "video/quicktime");
            put(".movie", "video/x-sgi-movie");
            put(".mp2", "video/mpeg");
            put(".mp2v", "video/mpeg");
            put(".dv", "video/x-dv");
            put(".IVF", "video/x-ivf");
            put(".lsf", "video/x-la-asf");
            put(".lsx", "video/x-la-asf");
            put(".m1v", "video/mpeg");
            put(".m2t", "video/vnd.dlna.mpeg-tts");
            put(".m2ts", "video/vnd.dlna.mpeg-tts");
            put(".m2v", "video/mpeg");
            put(".m4v", "video/x-m4v");
            put(".mp4", "video/mp4");
            put(".mp4v", "video/mp4");
            put(".mpa", "video/mpeg");
            put(".mpe", "video/mpeg");
            put(".mpeg", "video/mpeg");
            put(".wm", "video/x-ms-wm");
            put(".mpg", "video/mpeg");
            put(".mpv2", "video/mpeg");
            put(".mqv", "video/quicktime");
            put(".nsc", "video/x-ms-asf");
            put(".qt", "video/quicktime");
            put(".ts", "video/vnd.dlna.mpeg-tts");
            put(".vbk", "video/mpeg");
            put(".wmp", "video/x-ms-wmp");
            put(".wmv", "video/x-ms-wmv");
            put(".wmx", "video/x-ms-wmx");
            put(".wvx", "video/x-ms-wvx");
            put(".mts", "video/vnd.dlna.mpeg-tts");
            put(".tts", "video/vnd.dlna.mpeg-tts");
        }

        /**
         * Audio formats.
         */
        {
            put(".mp3", "audio/mpeg");
            put(".wma", "audio/x-ms-wma");
            put(".aa", "audio/audible");
            put(".AAC", "audio/aac");
            put(".aax", "audio/vnd.audible.aax");
            put(".ac3", "audio/ac3");
            put(".ADT", "audio/vnd.dlna.adts");
            put(".ADTS", "audio/aac");
            put(".aif", "audio/x-aiff");
            put(".aifc", "audio/aiff");
            put(".aiff", "audio/aiff");
            put(".cdda", "audio/aiff");
            put(".au", "audio/basic");
            put(".m3u", "audio/x-mpegurl");
            put(".m3u8", "audio/x-mpegurl");
            put(".m4a", "audio/m4a");
            put(".m4b", "audio/m4b");
            put(".m4p", "audio/m4p");
            put(".m4r", "audio/x-m4r");
            put(".caf", "audio/x-caf");
            put(".gsm", "audio/x-gsm");
            put(".mid", "audio/mid");
            put(".midi", "audio/mid");
            put(".pls", "audio/scpls");
            put(".ra", "audio/x-pn-realaudio");
            put(".ram", "audio/x-pn-realaudio");
            put(".rmi", "audio/mid");
            put(".rpm", "audio/x-pn-realaudio-plugin");
            put(".sd2", "audio/x-sd2");
            put(".smd", "audio/x-smd");
            put(".smx", "audio/x-smd");
            put(".smz", "audio/x-smd");
            put(".snd", "audio/basic");
            put(".wav", "audio/wav");
            put(".wave", "audio/wav");
            put(".wax", "audio/x-ms-wax");
        }

        /**
         * Other file types, including XML, binary streams, and various application-specific formats.
         */
        {
            // xml type files
            put(".asa", "application/xml");
            put(".asax", "application/xml");
            put(".ascx", "application/xml");
            put(".ashx", "application/xml");
            put(".asmx", "application/xml");
            put(".aspx", "application/xml");
            put(".config", "application/xml");
            put(".coverage", "application/xml");
            put(".datasource", "application/xml");
            put(".dgml", "application/xml");
            put(".generictest", "application/xml");
            put(".hxa", "application/xml");
            put(".hxc", "application/xml");
            put(".hxe", "application/xml");
            put(".hxf", "application/xml");
            put(".hxk", "application/xml");
            put(".svc", "application/xml");
            put(".rdlc", "application/xml");
            put(".resx", "application/xml");
            put(".ruleset", "application/xml");
            put(".settings", "application/xml");
            put(".snippet", "application/xml");
            put(".testrunconfig", "application/xml");
            put(".testsettings", "application/xml");
            put(".xss", "application/xml");
            put(".xsc", "application/xml");
            put(".hxv", "application/xml");
            put(".loadtest", "application/xml");
            put(".trx", "application/xml");
            put(".psess", "application/xml");
            put(".mtx", "application/xml");
            put(".master", "application/xml");
            put(".orderedtest", "application/xml");
            put(".sitemap", "application/xml");
            put(".skin", "application/xml");
            put(".vscontent", "application/xml");
            put(".vsmdi", "application/xml");
            put(".webtest", "application/xml");
            put(".wiq", "application/xml");
            put(".xmta", "application/xml");
            put(".filters", "Application/xml");
            put(".vcproj", "Application/xml");
            put(".vcxproj", "Application/xml");
            // file stream types
            put(".thn", "application/octet-stream");
            put(".toc", "application/octet-stream");
            put(".ttf", "application/octet-stream");
            put(".u32", "application/octet-stream");
            put(".xsn", "application/octet-stream");
            put(".xtp", "application/octet-stream");
            put(".aaf", "application/octet-stream");
            put(".aca", "application/octet-stream");
            put(".afm", "application/octet-stream");
            put(".asd", "application/octet-stream");
            put(".asi", "application/octet-stream");
            put(".cab", "application/octet-stream");
            put(".bin", "application/octet-stream");
            put(".chm", "application/octet-stream");
            put(".cur", "application/octet-stream");
            put(".dat", "application/octet-stream");
            put(".deploy", "application/octet-stream");
            put(".dwp", "application/octet-stream");
            put(".dsp", "application/octet-stream");
            put(".emz", "application/octet-stream");
            put(".eot", "application/octet-stream");
            put(".exe", "application/octet-stream");
            put(".hxd", "application/octet-stream");
            put(".hxh", "application/octet-stream");
            put(".hxi", "application/octet-stream");
            put(".hxq", "application/octet-stream");
            put(".hxr", "application/octet-stream");
            put(".hxs", "application/octet-stream");
            put(".hxw", "application/octet-stream");
            put(".ics", "application/octet-stream");
            put(".hhk", "application/octet-stream");
            put(".hhp", "application/octet-stream");
            put(".inf", "application/octet-stream");
            put(".fla", "application/octet-stream");
            put(".java", "application/octet-stream");
            put(".jpb", "application/octet-stream");
            put(".mdp", "application/octet-stream");
            put(".mix", "application/octet-stream");
            put(".msi", "application/octet-stream");
            put(".mso", "application/octet-stream");
            put(".ocx", "application/octet-stream");
            put(".pcx", "application/octet-stream");
            put(".pcz", "application/octet-stream");
            put(".pfb", "application/octet-stream");
            put(".pfm", "application/octet-stream");
            put(".lzh", "application/octet-stream");
            put(".lpk", "application/octet-stream");
            put(".qxd", "application/octet-stream");
            put(".prm", "application/octet-stream");
            put(".prx", "application/octet-stream");
            put(".psd", "application/octet-stream");
            put(".psm", "application/octet-stream");
            put(".psp", "application/octet-stream");
            put(".sea", "application/octet-stream");
            put(".smi", "application/octet-stream");
            put(".snp", "application/octet-stream");
            put(".acx", "application/internet-property-stream");
            put(".ai", "application/postscript");
            put(".atom", "application/atom+xml");
            put(".axs", "application/olescript");
            put(".ustar", "application/x-ustar");
            put(".bcpio", "application/x-bcpio");
            put(".xhtml", "application/xhtml+xml");
            put(".crl", "application/pkix-crl");
            put(".amc", "application/x-mpeg");
            put(".cdf", "application/x-cdf");
            put(".cer", "application/x-x509-ca-cert");
            put(".class", "application/x-java-applet");
            put(".clp", "application/x-msclip");
            put(".application", "application/x-ms-application");
            put(".adobebridge", "application/x-bridge-url");
            put(".cpio", "application/x-cpio");
            put(".crd", "application/x-mscardfile");
            put(".crt", "application/x-x509-ca-cert");
            put(".der", "application/x-x509-ca-cert");
            put(".csh", "application/x-csh");
            put(".dcr", "application/x-director");
            put(".dir", "application/x-director");
            put(".dll", "application/x-msdownload");
            put(".dvi", "application/x-dvi");
            put(".dwf", "drawing/x-dwf");
            put(".dxr", "application/x-director");
            put(".flr", "x-world/x-vrml");
            put(".gtar", "application/x-gtar");
            put(".gz", "application/x-gzip");
            put(".hdf", "application/x-hdf");
            put(".hhc", "application/x-oleobject");
            put(".mmf", "application/x-smaf");
            put(".mny", "application/x-msmoney");
            put(".ms", "application/x-troff-ms");
            put(".mvb", "application/x-msmediaview");
            put(".mvc", "application/x-miva-compiled");
            put(".mxp", "application/x-mmxp");
            put(".nc", "application/x-netcdf");
            put(".pcast", "application/x-podcast");
            put(".ins", "application/x-internet-signup");
            put(".jnlp", "application/x-java-jnlp-file");
            put(".js", "application/x-javascript");
            put(".latex", "application/x-latex");
            put(".lit", "application/x-ms-reader");
            put(".manifest", "application/x-ms-manifest");
            put(".man", "application/x-troff-man");
            put(".me", "application/x-troff-me");
            put(".mfp", "application/x-shockwave-flash");
            put(".pfx", "application/x-pkcs12");
            put(".p7r", "application/x-pkcs7-certreqresp");
            put(".p12", "application/x-pkcs12");
            put(".p7b", "application/x-pkcs7-certificates");
            put(".pma", "application/x-perfmon");
            put(".pmc", "application/x-perfmon");
            put(".pml", "application/x-perfmon");
            put(".pmr", "application/x-perfmon");
            put(".pmw", "application/x-perfmon");
            put(".iii", "application/x-iphone");
            put(".ipa", "application/x-itunes-ipa");
            put(".ipg", "application/x-itunes-ipg");
            put(".ipsw", "application/x-itunes-ipsw");
            put(".isp", "application/x-internet-signup");
            put(".ite", "application/x-itunes-ite");
            put(".itlp", "application/x-itunes-itlp");
            put(".itms", "application/x-itunes-itms");
            put(".itpc", "application/x-itunes-itpc");
            put(".eps", "application/postscript");
            put(".etl", "application/etl");
            put(".evy", "application/envoy");
            put(".fdf", "application/vnd.fdf");
            put(".fif", "application/fractals");
            put(".fsscript", "application/fsharp-script");
            put(".fsx", "application/fsharp-script");
            put(".hlp", "application/winhlp");
            put(".hqx", "application/mac-binhex40");
            put(".hta", "application/hta");
            put(".jck", "application/liquidmotion");
            put(".jcz", "application/liquidmotion");
            put(".library-ms", "application/windows-library+xml");
            put(".mht", "message/rfc822");
            put(".mhtml", "message/rfc822");
            put(".nws", "message/rfc822");
            put(".eml", "message/rfc822");
            put(".oda", "application/oda");
            put(".ods", "application/oleobject");
            put(".osdx", "application/opensearchdescription+xml");
            put(".p10", "application/pkcs10");
            put(".p7c", "application/pkcs7-mime");
            put(".p7m", "application/pkcs7-mime");
            put(".p7s", "application/pkcs7-signature");
            put(".prf", "application/pics-rules");
            put(".ps", "application/postscript");
            put(".psc1", "application/PowerShell");
            put(".pub", "application/x-mspublisher");
            put(".qtl", "application/x-quicktimeplayer");
            put(".rat", "application/rat-file");
            put(".roff", "application/x-troff");
            put(".rtf", "application/rtf");
            put(".safariextz", "application/x-safari-safariextz");
            put(".scd", "application/x-msschedule");
            put(".sdp", "application/sdp");
            put(".searchConnector-ms", "application/windows-search-connector+xml");
            put(".setpay", "application/set-payment-initiation");
            put(".setreg", "application/set-registration-initiation");
            put(".sgimb", "application/x-sgimb");
            put(".sh", "application/x-sh");
            put(".shar", "application/x-shar");
            put(".sit", "application/x-stuffit");
            put(".slupkg-ms", "application/x-ms-license");
            put(".spc", "application/x-pkcs7-certificates");
            put(".spl", "application/futuresplash");
            put(".src", "application/x-wais-source");
            put(".ssm", "application/streamingmedia");
            put(".sv4cpio", "application/x-sv4cpio");
            put(".sv4crc", "application/x-sv4crc");
            put(".swf", "application/x-shockwave-flash");
            put(".t", "application/x-troff");
            put(".tcl", "application/x-tcl");
            put(".tex", "application/x-tex");
            put(".texi", "application/x-texinfo");
            put(".texinfo", "application/x-texinfo");
            put(".tr", "application/x-troff");
            put(".trm", "application/x-msterminal");
            put(".vsi", "application/ms-vsi");
            put(".vsix", "application/vsix");
            put(".vsto", "application/x-ms-vsto");
            put(".webarchive", "application/x-safari-webarchive");
            put(".WLMP", "application/wlmoviemaker");
            put(".wlpginstall", "application/x-wlpg-detect");
            put(".wlpginstall3", "application/x-wlpg3-detect");
            put(".x", "application/directx");
            put(".xaml", "application/xaml+xml");
            put(".xht", "application/xhtml+xml");
            put(".xap", "application/x-silverlight-app");
            put(".xbap", "application/x-ms-xbap");
            put(".xaf", "x-world/x-vrml");
            put(".xof", "x-world/x-vrml");
            put(".wrl", "x-world/x-vrml");
            put(".wrz", "x-world/x-vrml");
        }

        /**
         * File magic numbers (hexadecimal headers) for type detection.
         */
        {
            // JPEG (jpg)
            put("ffd8ffe", "jpg");
            // PNG (png)
            put("89504e470d0a1a0a0000", "png");
            // GIF (gif)
            put("47494638396126026f01", "gif");
            // TIFF (tif)
            put("49492a00227105008037", "tif");
            // Bitmap (bmp)
            put("424d", "bmp");
            // CAD (dwg)
            put("41433130313500000000", "dwg");
            put("3c21444f435459504520", "html");
            put("3c21646f637479706520", "htm");
            put("48544d4c207b0d0a0942", "css");
            put("696b2e71623d696b2e71", "js");
            // Rich Text Format (rtf)
            put("7b5c727466315c616e73", "rtf");
            // Photoshop (psd)
            put("38425053000100000000", "psd");
            // Email [Outlook Express 6] (eml)
            put("46726f6d3a203d3f6762", "eml");
            // MS Excel Note: Word, MSI, and Excel have the same file header
            put("d0cf11e0a1b11ae10000", "doc");
            // Visio Drawing
            put("d0cf11e0a1b11ae10000", "vsd");
            // MS Access (mdb)
            put("5374616E64617264204A", "mdb");
            put("252150532D41646F6265", "ps");
            // Adobe Acrobat (pdf)
            put("255044462d312e", "pdf");
            // rmvb/rm are the same
            put("2e524d46000000120001", "rmvb");
            // flv and f4v are the same
            put("464c5601050000000900", "flv");
            put("0000001C66747970", "mp4");
            put("00000020667479706", "mp4");
            put("00000018667479706D70", "mp4");
            put("49443303000000002176", "mp3");
            put("000001ba210001000180", "mpg");
            put("3026b2758e66cf11a6d9", "wmv");
            put("52494646e27807005741", "wav");
            put("52494646d07d60074156", "avi");
            put("4d546864000000060001", "mid");
            put("526172211a0700cf9073", "rar");
            put("235468697320636f6e66", "ini");
            put("504B03040a0000000000", "jar");
            put("504B0304140008000800", "jar");
            put("504B0304140006000800", "docx");
            put("504B0304140006000800", "xlsx");
            put("D0CF11E0A1B11AE10", "xls");
            put("504B0304", "zip");
            put("4d5a9000030000000400", "exe");
            put("3c25402070616765206c", "jsp");
            put("4d616e69666573742d56", "mf");
            put("3c3f786d6c2076657273", "xml");
            put("494e5345525420494e54", "sql");
            put("7061636b616765207765", "java");
            put("406563686f206f66660d", "bat");
            put("1f8b0800000000000000", "gz");
            put("6c6f67346a2e726f6f74", "properties");
            put("cafebabe0000002e0041", "class");
            put("49545346030000006000", "chm");
            put("04000000010000001300", "mxp");
            // WPS text wps, spreadsheet et, presentation dps are all the same
            put("d0cf11e0a1b11ae10000", "wps");
            put("6431303a637265617465", "torrent");
            // Quicktime (mov)
            put("6D6F6F76", "mov");
            // WordPerfect (wpd)
            put("FF575043", "wpd");
            // Outlook Express (dbx)
            put("CFAD12FEC5FD746F", "dbx");
            // Outlook (pst)
            put("2142444E", "pst");
            // Quicken (qdf)
            put("AC9EBD8F", "qdf");
            // Windows Password (pwl)
            put("E3828596", "pwl");
            // Real Audio (ram)
            put("2E7261FD", "ram");
            put("52494646", "webp");

            // 2-byte signatures
            // https://github.com/sindresorhus/file-type/blob/main/core.js#L90
            put("424D", "bmp"); // Bitmap (bmp)
            put("0B77", "ac3");
            put("7801", "dmg");
            put("4D5A", "exe");
            put("1FA0", "Z");
            put("1F9D", "Z");

            // 3-byte signatures
            // https://github.com/sindresorhus/file-type/blob/main/core.js#L149
            put("474946", "gif"); // GIF (gif)
            put("FFd8FF", "jpg"); // JPEG (jpg)
            put("4949BC", "jxr"); // jxr
            put("1F8B08", "gz"); // gzip
            put("425A68", "bz2"); // bz2

            // check string
            put(HexKit.encodeString("MP+"), "mpc");
            put(HexKit.encodeString("FLIF"), "flif");
            put(HexKit.encodeString("8BPS"), "psd");// Photoshop (psd)
            put(HexKit.encodeString("MPCK"), "mpc");
            put(HexKit.encodeString("FORM"), "aif");// Musepack, SV8
            put(HexKit.encodeString("icns"), "icns");
        }
    };

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FileType() {

    }

    /**
     * Adds a file type mapping. If a mapping for the given hexadecimal head already exists, it will be overwritten.
     *
     * @param fileStreamHexHead The hexadecimal string representing the file stream's head (magic number).
     * @param extName           The file extension name (e.g., "jpg", "pdf").
     * @return The previously associated file extension for the given hexadecimal head, or {@code null} if none was
     *         present.
     */
    public static String putFileType(final String fileStreamHexHead, final String extName) {
        return FILE_TYPE.put(fileStreamHexHead, extName);
    }

    /**
     * Removes a file type mapping.
     *
     * @param fileStreamHexHead The hexadecimal string representing the file stream's head (magic number).
     * @return The removed file extension name, or {@code null} if no mapping was found for the given hexadecimal head.
     */
    public static String removeFileType(final String fileStreamHexHead) {
        return FILE_TYPE.remove(fileStreamHexHead);
    }

    /**
     * Determines the file type based on the hexadecimal header of a file stream.
     *
     * @param fileStreamHexHead The hexadecimal string representing the file stream's head.
     * @return The file type extension (e.g., "jpg", "pdf"), or {@code null} if the type cannot be determined.
     */
    public static String getType(final String fileStreamHexHead) {
        if (StringKit.isBlank(fileStreamHexHead)) {
            return null;
        }
        for (final Entry<String, String> fileTypeEntry : FILE_TYPE.entrySet()) {
            if (StringKit.startWithIgnoreCase(fileStreamHexHead, fileTypeEntry.getKey())) {
                return fileTypeEntry.getValue();
            }
        }
        final byte[] bytes = (HexKit.decode(fileStreamHexHead));
        return FileMagicNumber.getMagicNumber(bytes).getExtension();
    }

    /**
     * Determines the file type based on the hexadecimal header of an input stream.
     *
     * @param in           The input stream.
     * @param fileHeadSize The number of bytes to read from the stream's head for type determination.
     * @return The file type extension, or {@code null} if the type cannot be determined.
     * @throws InternalException If an I/O error occurs while reading the stream.
     */
    public static String getType(final InputStream in, final int fileHeadSize) throws InternalException {
        return getType((IoKit.readHex(in, fileHeadSize, false)));
    }

    /**
     * Determines the file type based on the hexadecimal header of an input stream. Note that this method reads a
     * portion of the stream's header, which may affect subsequent reads. If the stream needs to be reused, it should
     * support {@link InputStream#reset()}.
     *
     * @param in      The {@link InputStream}.
     * @param isExact If {@code false}, uses the first 64 bytes for matching; if {@code true}, uses the first 8192 bytes
     *                for more precise matching.
     * @return The file type extension, or {@code null} if the input stream is {@code null} or the type cannot be
     *         determined.
     * @throws InternalException If an I/O error occurs while reading the stream.
     */
    public static String getType(final InputStream in, final boolean isExact) throws InternalException {
        if (null == in) {
            return null;
        }
        return isExact ? getType(readHex8192Upper(in)) : getType(readHex64Upper(in));
    }

    /**
     * Determines the file type based on the hexadecimal header of an input stream, using the first 64 bytes. Note that
     * this method reads a portion of the stream's header, which may affect subsequent reads. If the stream needs to be
     * reused, it should support {@link InputStream#reset()}.
     *
     * @param in The {@link InputStream}.
     * @return The file type extension, or {@code null} if the type cannot be determined.
     * @throws InternalException If an I/O error occurs while reading the stream.
     */
    public static String getType(final InputStream in) throws InternalException {
        return getType(in, false);
    }

    /**
     * Determines the file type based on the hexadecimal header of an input stream and the file's extension as a
     * fallback. Note that this method reads a portion of the stream's header, which may affect subsequent reads. If the
     * stream needs to be reused, it should support {@link InputStream#reset()}.
     *
     * <pre>
     *     1. If the type cannot be recognized by the header, it defaults to recognition by extension.
     *     2. For files like XLS, DOC, MSI, whose headers are indistinguishable, the extension is used.
     *     3. For ZIP files that could be DOCX, XLSX, PPTX, JAR, WAR, OFD, etc., the extension is used for differentiation.
     * </pre>
     *
     * @param in       The {@link InputStream}.
     * @param filename The file name, used as a fallback for type determination.
     * @return The file type extension, or {@code null} if the type cannot be determined.
     * @throws InternalException If an I/O error occurs while reading the stream.
     */
    public static String getType(final InputStream in, final String filename) throws InternalException {
        return getType(in, filename, false);
    }

    /**
     * Determines the file type based on the hexadecimal header of an input stream and the file's extension as a
     * fallback. Note that this method reads a portion of the stream's header, which may affect subsequent reads. If the
     * stream needs to be reused, it should support {@link InputStream#reset()}.
     *
     * <pre>
     *     1. If the type cannot be recognized by the header, it defaults to recognition by extension.
     *     2. For files like XLS, DOC, MSI, whose headers are indistinguishable, the extension is used.
     *     3. For ZIP files that could be DOCX, XLSX, PPTX, JAR, WAR, OFD, etc., the extension is used for differentiation.
     * </pre>
     *
     * @param in       The {@link InputStream}.
     * @param filename The file name, used as a fallback for type determination.
     * @param isExact  If {@code false}, uses the first 64 bytes for matching; if {@code true}, uses the first 8192
     *                 bytes for more precise matching.
     * @return The file type extension, or {@code null} if the type cannot be determined.
     * @throws InternalException If an I/O error occurs while reading the stream.
     */
    public static String getType(final InputStream in, final String filename, final boolean isExact)
            throws InternalException {
        String typeName = getType(in, isExact);
        if (null == typeName) {
            // If type recognition by header fails, use extension as a fallback
            typeName = FileName.extName(filename);
        } else if ("zip".equals(typeName)) {
            // ZIP files can be DOCX, XLSX, PPTX, JAR, WAR, OFD, etc.; use extension for differentiation
            final String extName = FileName.extName(filename);
            if ("docx".equalsIgnoreCase(extName)) {
                typeName = "docx";
            } else if ("xlsx".equalsIgnoreCase(extName)) {
                typeName = "xlsx";
            } else if ("pptx".equalsIgnoreCase(extName)) {
                typeName = "pptx";
            } else if ("jar".equalsIgnoreCase(extName)) {
                typeName = "jar";
            } else if ("war".equalsIgnoreCase(extName)) {
                typeName = "war";
            } else if ("ofd".equalsIgnoreCase(extName)) {
                typeName = "ofd";
            } else if ("apk".equalsIgnoreCase(extName)) {
                typeName = "apk";
            }
        } else if ("jar".equals(typeName)) {
            // WPS-edited .xlsx files have the same header as .jar files; differentiate by extension
            final String extName = FileName.extName(filename);
            if ("xlsx".equalsIgnoreCase(extName)) {
                typeName = "xlsx";
            } else if ("docx".equalsIgnoreCase(extName)) {
                typeName = "docx";
            } else if ("pptx".equalsIgnoreCase(extName)) {
                typeName = "pptx";
            } else if ("zip".equalsIgnoreCase(extName)) {
                typeName = "zip";
            } else if ("apk".equalsIgnoreCase(extName)) {
                typeName = "apk";
            }
        }
        return typeName;
    }

    /**
     * Determines the file type based on the hexadecimal header of a {@link File} and its extension as a fallback.
     *
     * <pre>
     *     1. If the type cannot be recognized by the header, it defaults to recognition by extension.
     *     2. For files like XLS, DOC, MSI, whose headers are indistinguishable, the extension is used.
     *     3. For ZIP files that could be JAR, WAR, etc., the extension is used for differentiation.
     * </pre>
     *
     * @param file    The {@link File} object.
     * @param isExact If {@code false}, uses the first 64 bytes for matching; if {@code true}, uses the first 8192 bytes
     *                for more precise matching.
     * @return The file type extension, or {@code null} if the type cannot be determined.
     * @throws InternalException        If an I/O error occurs while reading the file.
     * @throws IllegalArgumentException If the provided {@code file} is not a regular file.
     */
    public static String getType(final File file, final boolean isExact) throws InternalException {
        if (!FileKit.isFile(file)) {
            throw new IllegalArgumentException("Not a regular file!");
        }
        InputStream in = null;
        try {
            in = IoKit.toStream(file);
            return getType(in, file.getName(), isExact);
        } finally {
            IoKit.closeQuietly(in);
        }
    }

    /**
     * Determines the file type based on the hexadecimal header of a {@link File} and its extension as a fallback, using
     * the first 64 bytes for header matching.
     *
     * <pre>
     *     1. If the type cannot be recognized by the header, it defaults to recognition by extension.
     *     2. For files like XLS, DOC, MSI, whose headers are indistinguishable, the extension is used.
     *     3. For ZIP files that could be JAR, WAR, etc., the extension is used for differentiation.
     * </pre>
     *
     * @param file The {@link File} object.
     * @return The file type extension, or {@code null} if the type cannot be determined.
     * @throws InternalException        If an I/O error occurs while reading the file.
     * @throws IllegalArgumentException If the provided {@code file} is not a regular file.
     */
    public static String getType(final File file) throws InternalException {
        return getType(file, false);
    }

    /**
     * Determines the file type by path, reading the file's header.
     *
     * @param path    The file path (absolute or relative to ClassPath).
     * @param isExact If {@code false}, uses the first 64 bytes for matching; if {@code true}, uses the first 8192 bytes
     *                for more precise matching.
     * @return The file type extension.
     * @throws InternalException If an I/O error occurs while reading the file.
     */
    public static String getTypeByPath(final String path, final boolean isExact) throws InternalException {
        return getType(FileKit.file(path), isExact);
    }

    /**
     * Determines the file type by path, reading the file's header using the first 64 bytes for matching.
     *
     * @param path The file path (absolute or relative to ClassPath).
     * @return The file type extension.
     * @throws InternalException If an I/O error occurs while reading the file.
     */
    public static String getTypeByPath(final String path) throws InternalException {
        return getTypeByPath(path, false);
    }

    /**
     * Reads the first 8192 bytes from an input stream and converts them to an uppercase hexadecimal string.
     *
     * @param in The {@link InputStream}.
     * @return The hexadecimal string representation of the first 8192 bytes (or fewer if the stream is shorter).
     * @throws InternalException If an I/O error occurs while reading the stream.
     */
    private static String readHex8192Upper(final InputStream in) throws InternalException {
        try {
            return IoKit.readHex(in, Math.min(8192, in.available()), false);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Reads the first 64 bytes from an input stream and converts them to an uppercase hexadecimal string.
     *
     * @param in The {@link InputStream}.
     * @return The hexadecimal string representation of the first 64 bytes (or fewer if the stream is shorter).
     * @throws InternalException If an I/O error occurs while reading the stream.
     */
    private static String readHex64Upper(final InputStream in) throws InternalException {
        return IoKit.readHex(in, 64, false);
    }

}
