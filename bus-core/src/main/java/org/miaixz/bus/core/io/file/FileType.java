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
package org.miaixz.bus.core.io.file;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.HexKit;
import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 文件类型判断工具类
 *
 * <p>此工具根据文件的前几位bytes猜测文件类型，对于文本、zip判断不准确，对于视频、图片类型判断准确</p>
 *
 * <p>需要注意的是，xlsx、docx等Office2007格式，全部识别为zip，因为新版采用了OpenXML格式，这些格式本质上是XML文件打包为zip</p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FileType {

    private static final Map<String, String> FILE_TYPE_MAP = new ConcurrentSkipListMap<>();

    /**
     * 增加文件类型映射
     * 如果已经存在将覆盖之前的映射
     *
     * @param fileStreamHexHead 文件流头部Hex信息
     * @param extName           文件扩展名
     * @return 之前已经存在的文件扩展名
     */
    public static String putFileType(final String fileStreamHexHead, final String extName) {
        return FILE_TYPE_MAP.put(fileStreamHexHead, extName);
    }

    /**
     * 移除文件类型映射
     *
     * @param fileStreamHexHead 文件流头部Hex信息
     * @return 移除的文件扩展名
     */
    public static String removeFileType(final String fileStreamHexHead) {
        return FILE_TYPE_MAP.remove(fileStreamHexHead);
    }

    /**
     * 根据文件流的头部信息获得文件类型
     *
     * @param fileStreamHexHead 文件流头部16进制字符串
     * @return 文件类型，未找到为{@code null}
     */
    public static String getType(final String fileStreamHexHead) {
        for (final Entry<String, String> fileTypeEntry : FILE_TYPE_MAP.entrySet()) {
            if (StringKit.startWithIgnoreCase(fileStreamHexHead, fileTypeEntry.getKey())) {
                return fileTypeEntry.getValue();
            }
        }
        final byte[] bytes = (HexKit.decode(fileStreamHexHead));
        return FileMagicNumber.getMagicNumber(bytes).getExtension();
    }

    /**
     * 根据文件流的头部信息获得文件类型
     *
     * @param in           文件流
     * @param fileHeadSize 自定义读取文件头部的大小
     * @return 文件类型，未找到为{@code null}
     * @throws InternalException IO异常
     */
    public static String getType(final InputStream in, final int fileHeadSize) throws InternalException {
        return getType((IoKit.readHex(in, fileHeadSize, false)));
    }

    /**
     * 根据文件流的头部信息获得文件类型
     * 注意此方法会读取头部一些bytes，造成此流接下来读取时缺少部分bytes
     * 因此如果想复用此流，流需支持{@link InputStream#reset()}方法。
     *
     * @param in      {@link InputStream}
     * @param isExact 是否精确匹配，如果为false，使用前64个bytes匹配，如果为true，使用前8192bytes匹配
     * @return 类型，文件的扩展名，in为{@code null}或未找到为{@code null}
     * @throws InternalException 读取流引起的异常
     */
    public static String getType(final InputStream in, final boolean isExact) throws InternalException {
        if (null == in) {
            return null;
        }
        return isExact
                ? getType(readHex8192Upper(in))
                : getType(readHex64Upper(in));
    }

    /**
     * 根据文件流的头部信息获得文件类型
     * 注意此方法会读取头部64个bytes，造成此流接下来读取时缺少部分bytes
     * 因此如果想复用此流，流需支持{@link InputStream#reset()}方法。
     *
     * @param in {@link InputStream}
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws InternalException 读取流引起的异常
     */
    public static String getType(final InputStream in) throws InternalException {
        return getType(in, false);
    }

    /**
     * 根据文件流的头部信息获得文件类型
     * 注意此方法会读取头部64个bytes，造成此流接下来读取时缺少部分bytes
     * 因此如果想复用此流，流需支持{@link InputStream#reset()}方法。
     *
     * <pre>
     *     1、无法识别类型默认按照扩展名识别
     *     2、xls、doc、msi头信息无法区分，按照扩展名区分
     *     3、zip可能为docx、xlsx、pptx、jar、war、ofd头信息无法区分，按照扩展名区分
     * </pre>
     *
     * @param in       {@link InputStream}
     * @param filename 文件名
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws InternalException 读取流引起的异常
     */
    public static String getType(final InputStream in, final String filename) throws InternalException {
        return getType(in, filename, false);
    }

    /**
     * 根据文件流的头部信息获得文件类型
     * 注意此方法会读取头部一些bytes，造成此流接下来读取时缺少部分bytes
     * 因此如果想复用此流，流需支持{@link InputStream#reset()}方法。
     *
     * <pre>
     *     1、无法识别类型默认按照扩展名识别
     *     2、xls、doc、msi头信息无法区分，按照扩展名区分
     *     3、zip可能为docx、xlsx、pptx、jar、war、ofd头信息无法区分，按照扩展名区分
     * </pre>
     *
     * @param in       {@link InputStream}
     * @param filename 文件名
     * @param isExact  是否精确匹配，如果为false，使用前64个bytes匹配，如果为true，使用前8192bytes匹配
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws InternalException 读取流引起的异常
     */
    public static String getType(final InputStream in, final String filename, final boolean isExact) throws InternalException {
        String typeName = getType(in, isExact);
        if (null == typeName) {
            // 未成功识别类型，扩展名辅助识别
            typeName = FileName.extName(filename);
        } else if ("zip".equals(typeName)) {
            // zip可能为docx、xlsx、pptx、jar、war、ofd等格式，扩展名辅助判断
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
            // wps编辑过的.xlsx文件与.jar的开头相同,通过扩展名判断
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
     * 根据文件流的头部信息获得文件类型
     *
     * <pre>
     *     1、无法识别类型默认按照扩展名识别
     *     2、xls、doc、msi头信息无法区分，按照扩展名区分
     *     3、zip可能为jar、war头信息无法区分，按照扩展名区分
     * </pre>
     *
     * @param file    文件 {@link File}
     * @param isExact 是否精确匹配，如果为false，使用前64个bytes匹配，如果为true，使用前8192bytes匹配
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws InternalException 读取文件引起的异常
     */
    public static String getType(final File file, final boolean isExact) throws InternalException {
        if (false == FileKit.isFile(file)) {
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
     * 根据文件流的头部信息获得文件类型
     *
     * <pre>
     *     1、无法识别类型默认按照扩展名识别
     *     2、xls、doc、msi头信息无法区分，按照扩展名区分
     *     3、zip可能为jar、war头信息无法区分，按照扩展名区分
     * </pre>
     *
     * @param file 文件 {@link File}
     * @return 类型，文件的扩展名，未找到为{@code null}
     * @throws InternalException 读取文件引起的异常
     */
    public static String getType(final File file) throws InternalException {
        return getType(file, false);
    }

    /**
     * 通过路径获得文件类型
     *
     * @param path    路径，绝对路径或相对ClassPath的路径
     * @param isExact 是否精确匹配，如果为false，使用前64个bytes匹配，如果为true，使用前8192bytes匹配
     * @return 类型
     * @throws InternalException 读取文件引起的异常
     */
    public static String getTypeByPath(final String path, final boolean isExact) throws InternalException {
        return getType(FileKit.file(path), isExact);
    }

    /**
     * 通过路径获得文件类型
     *
     * @param path 路径，绝对路径或相对ClassPath的路径
     * @return 类型
     * @throws InternalException 读取文件引起的异常
     */
    public static String getTypeByPath(final String path) throws InternalException {
        return getTypeByPath(path, false);
    }

    /**
     * 从流中读取前8192个byte并转换为16进制，字母部分使用大写
     *
     * @param in {@link InputStream}
     * @return 16进制字符串
     * @throws InternalException IO异常
     */
    private static String readHex8192Upper(final InputStream in) throws InternalException {
        try {
            return IoKit.readHex(in, Math.min(8192, in.available()), false);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 从流中读取前64个byte并转换为16进制，字母部分使用大写
     *
     * @param in {@link InputStream}
     * @return 16进制字符串
     * @throws InternalException IO异常
     */
    private static String readHex64Upper(final InputStream in) throws InternalException {
        return IoKit.readHex(in, 64, false);
    }

}
