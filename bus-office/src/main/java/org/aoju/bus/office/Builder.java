/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.office;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.ArrayKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Platform;
import org.aoju.bus.office.magic.Lo;
import org.aoju.bus.office.magic.UnoUrl;
import org.aoju.bus.office.magic.family.FamilyType;
import org.aoju.bus.office.process.*;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ExcelNumberFormat;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

/**
 * 为office提供辅助功能.
 *
 * @author Kimi Liu
 * @version 6.1.5
 * @since JDK 1.8+
 */
public final class Builder {

    public static final String UNKNOWN_SERVICE = "com.sun.frame.XModel";
    public static final String WRITER_SERVICE = "com.sun.star.text.GenericTextDocument";
    public static final String BASE_SERVICE = "com.sun.star.sdb.OfficeDatabaseDocument";
    public static final String CALC_SERVICE = "com.sun.star.sheet.SpreadsheetDocument";
    public static final String DRAW_SERVICE = "com.sun.star.drawing.DrawingDocument";
    public static final String IMPRESS_SERVICE = "com.sun.star.presentation.PresentationDocument";
    public static final String MATH_SERVICE = "com.sun.star.formula.FormulaProperties";
    public static final String NODE_PRODUCT = "/org.openoffice.Setup/Product";
    public static final String NODE_L10N = "/org.openoffice.Setup/L10N";
    public static final String[] NODE_PATHS = {NODE_PRODUCT, NODE_L10N};

    public static final String SUCCESS = Symbol.ZERO;
    public static final String FAILURE = "-1";

    /**
     * 连接到office的默认端口号.
     */
    public static final int DEFAULT_PORT_NUMBER = 8102;
    /**
     * 连接到office的默认管道名称.
     */
    public static final String DEFAULT_PIPE_NAME = "office";
    /**
     * 连接到office时的默认超时.
     * 默认2分钟
     */
    public static final long DEFAULT_CONNECT_TIMEOUT = 120000L;
    /**
     * 每次尝试连接之间的默认延迟.
     * 默认: 0.25秒
     */
    public static final long DEFAULT_RETRY_INTERVAL = 250L;
    /**
     * 执行进程调用时的默认超时(启动/终止).
     * 默认:2分钟
     */
    public static final long DEFAULT_PROCESS_TIMEOUT = 120000L;
    /**
     * 执行进程调用时每次尝试之间的默认延迟(启动/终止).
     * 默认:0.25 秒
     */
    public static final long DEFAULT_PROCESS_RETRY_INTERVAL = 250L;
    /**
     * office进程在重新启动之前可以执行的默认最大任务数.
     */
    public static final int DEFAULT_MAX_TASKS_PER_PROCESS = 200;
    /**
     * 有关OpenGL使用的默认行为.
     */
    public static final boolean DEFAULT_DISABLE_OPENGL = false;
    /**
     * 执行进程调用时每次尝试之间的最小延迟值(开始/终止).
     * 默认:没有延迟
     */
    public static final long MIN_PROCESS_RETRY_INTERVAL = 0L;
    /**
     * 执行进程调用时每次尝试之间的最大延迟(开始/终止).
     * 默认:10秒
     */
    public static final long MAX_PROCESS_RETRY_INTERVAL = 10000L;
    /**
     * 转换队列中任务的默认最大生存时间.
     * 默认:30秒
     */
    public static final long DEFAULT_TASK_QUEUE_TIMEOUT = 30000L;
    /**
     * 启动具有相同URL的office进程时的默认行为已经存在.
     */
    public static final boolean DEFAULT_KILL_EXISTING_PROCESS = true;
    /**
     * 处理任务时的默认超时.
     * 默认:2分钟
     */
    public static final long DEFAULT_TASK_EXECUTION_TIMEOUT = 120000L;
    public static final String ERROR_MESSAGE_STORE = "Could not store document: ";
    public static final String FILTER_DATA = "FilterData";
    public static final String FILTER_DATA_PREFIX_PARAM = "fd";
    public static final String LOAD_PROPERTIES_PREFIX_PARAM = "l";
    public static final String STORE_PROPERTIES_PREFIX_PARAM = "s";
    public static final long PID_NOT_FOUND = -2;
    public static final long PID_UNKNOWN = -1;
    public static final boolean DEFAULT_CLOSE_STREAM = true;
    /**
     * 默认office 信息
     */
    private static final String EXECUTABLE_DEFAULT = "program/soffice.bin";
    /**
     * 早期 office 信息
     */
    private static final String EXECUTABLE_MAC = "program/soffice";
    /**
     * MacOS office 信息
     */
    private static final String EXECUTABLE_MAC_41 = "MacOS/soffice";
    /**
     * Windows office 信息
     */
    private static final String EXECUTABLE_WINDOWS = "program/soffice.exe";

    private static final File INSTANCE;

    static {
        if (StringKit.isNotBlank(System.getProperty("office.home"))) {
            INSTANCE = new File(System.getProperty("office.home"));
        } else if (Platform.isWindows()) {
            final String programFiles64 = System.getenv("ProgramFiles");
            final String programFiles32 = System.getenv("ProgramFiles(x86)");
            INSTANCE = findOfficeHome(
                    EXECUTABLE_WINDOWS,
                    programFiles64 + File.separator + "LibreOffice",
                    programFiles64 + File.separator + "LibreOffice 5",
                    programFiles32 + File.separator + "LibreOffice 5",
                    programFiles32 + File.separator + "OpenOffice 4",
                    programFiles64 + File.separator + "LibreOffice 4",
                    programFiles32 + File.separator + "LibreOffice 4",
                    programFiles64 + File.separator + "LibreOffice 3",
                    programFiles32 + File.separator + "LibreOffice 3",
                    programFiles32 + File.separator + "OpenOffice.org 3");

        } else if (Platform.isMac()) {
            File homeDir = findOfficeHome(
                    EXECUTABLE_MAC_41,
                    "/Applications/LibreOffice.app/Contents",
                    "/Applications/OpenOffice.app/Contents",
                    "/Applications/OpenOffice.org.app/Contents");
            if (homeDir == null) {
                homeDir = findOfficeHome(
                        EXECUTABLE_MAC,
                        "/Applications/LibreOffice.app/Contents",
                        "/Applications/OpenOffice.app/Contents",
                        "/Applications/OpenOffice.org.app/Contents");
            }
            INSTANCE = homeDir;
        } else {
            INSTANCE = findOfficeHome(
                    EXECUTABLE_DEFAULT,
                    "/usr/lib64/libreoffice",
                    "/usr/lib/libreoffice",
                    "/usr/local/lib64/libreoffice",
                    "/usr/local/lib/libreoffice",
                    "/opt/libreoffice",
                    "/usr/lib64/openoffice",
                    "/usr/lib64/openoffice.org3",
                    "/usr/lib64/openoffice.org",
                    "/usr/lib/openoffice",
                    "/usr/lib/openoffice.org3",
                    "/usr/lib/openoffice.org",
                    "/opt/openoffice4",
                    "/opt/openoffice.org3");
        }
    }

    /**
     * 找到最好的进程管理器，它将用于检索进程PID并通过PID杀死进程.
     *
     * @return 最好的进程管理器根据当前的操作系统.
     */
    public static ProcessManager findBestProcessManager() {
        if (Platform.isMac()) {
            return MacProcessManager.getDefault();
        } else if (Platform.isFreeBSD()) {
            return FreeBSDProcessManager.getDefault();
        } else if (Platform.isAIX() || Platform.isSolaris()) {
            return UnixProcessManager.getDefault();
        } else if (Platform.isWindows()) {
            final WindowsProcessManager windowsProcessManager = WindowsProcessManager.getDefault();
            return windowsProcessManager.isUsable()
                    ? windowsProcessManager
                    : PureJavaProcessManager.getDefault();
        } else {
            return PureJavaProcessManager.getDefault();
        }
    }

    /**
     * 从端口号数组和管道名称数组构建{@link UnoUrl}数组.
     *
     * @param portNumbers 用于创建office url的端口号可能为空.
     * @param pipeNames   用于创建office url的管道名称可能为空.
     * @return office URL的数组。如果两个参数都为空，则返回一个带有单个office URL的数组，使用默认端口号8102.
     */
    public static UnoUrl[] buildOfficeUrls(final int[] portNumbers, final String[] pipeNames) {
        if (portNumbers == null && pipeNames == null) {
            return new UnoUrl[]{new UnoUrl(DEFAULT_PORT_NUMBER)};
        }

        final List<UnoUrl> unoUrls =
                new ArrayList<>(ArrayKit.getLength(portNumbers) + ArrayKit.getLength(pipeNames));
        Optional.ofNullable(pipeNames)
                .map(Stream::of)
                .ifPresent(stream -> stream.map(UnoUrl::new).forEach(unoUrls::add));
        if (portNumbers != null) {
            Arrays.stream(portNumbers).forEach(portNumber -> unoUrls.add(new UnoUrl(portNumber)));
        }
        return unoUrls.toArray(new UnoUrl[0]);
    }

    /**
     * 获取自动检测的默认office主目录.
     *
     * @return {@code File}实例，它是第一次检测到的office安装所在的目录.
     */
    public static File getDefaultOfficeHome() {
        return INSTANCE;
    }

    /**
     * 获取指定文档的{@link FamilyType}.
     *
     * @param document 文档类型.
     * @return 指定文档的{@link FamilyType}.
     * @throws InstrumentException 如果无法检索文档族.
     */
    public static FamilyType getDocumentFamily(final XComponent document) throws InstrumentException {
        final XServiceInfo serviceInfo = Lo.qi(XServiceInfo.class, document);
        if (serviceInfo.supportsService("com.sun.star.text.GenericTextDocument")) {
            return FamilyType.TEXT;
        } else if (serviceInfo.supportsService("com.sun.star.sheet.SpreadsheetDocument")) {
            return FamilyType.SPREADSHEET;
        } else if (serviceInfo.supportsService("com.sun.star.presentation.PresentationDocument")) {
            return FamilyType.PRESENTATION;
        } else if (serviceInfo.supportsService("com.sun.star.drawing.DrawingDocument")) {
            return FamilyType.DRAWING;
        }

        throw new InstrumentException("Document of unknown family: " + serviceInfo.getImplementationName());
    }

    /**
     * 获取office安装中的office可执行文件.
     *
     * @param officeHome office安装的根(主)目录.
     * @return 可执行文件的实例.
     */
    public static File getOfficeExecutable(final File officeHome) {
        if (Platform.isMac()) {
            File executableFile = new File(officeHome, EXECUTABLE_MAC_41);
            if (!executableFile.isFile()) {
                executableFile = new File(officeHome, EXECUTABLE_MAC);
            }
            return executableFile;
        }

        if (Platform.isWindows()) {
            return new File(officeHome, EXECUTABLE_WINDOWS);
        }

        return new File(officeHome, EXECUTABLE_DEFAULT);
    }

    /**
     * 使用指定的名称和值创建{@code PropertyValue}.
     *
     * @param name  属性名.
     * @param value 属性值.
     * @return 创建的{@code PropertyValue}.
     */
    public static PropertyValue property(final String name, final Object value) {

        final PropertyValue prop = new PropertyValue();
        prop.Name = name;
        prop.Value = value;
        return prop;
    }

    /**
     * 将常规java映射转换为{@code PropertyValue}数组，可用作带有UNO接口类型的参数.
     *
     * @param properties 要转换的map.
     * @return {@code PropertyValue}的数组.
     */
    public static PropertyValue[] toUnoProperties(final Map<String, Object> properties) {
        final List<PropertyValue> propertyValues = new ArrayList<>(properties.size());
        for (final Map.Entry<String, Object> entry : properties.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                final Map<String, Object> subProperties = (Map<String, Object>) value;
                value = toUnoProperties(subProperties);
            }
            propertyValues.add(property(entry.getKey(), value));
        }
        return propertyValues.toArray(new PropertyValue[0]);
    }

    /**
     * 按照office的要求，从指定文件构造URL.
     *
     * @param file 将为其构造URL的文件.
     * @return 有效的office网址.
     */
    public static String toUrl(final File file) {
        final String path = file.toURI().getRawPath();
        final String url = path.startsWith(Symbol.FORWARDSLASH) ? Normal.FILE_URL_PREFIX + path : "file://" + path;
        return url.endsWith(Symbol.SLASH) ? url.substring(0, url.length() - 1) : url;
    }

    /**
     * 验证指定的文件实例是否是有效的office主目录.
     *
     * @param officeHome 验证office主目录.
     * @throws IllegalStateException 如果指定的目录不是有效的office主目录.
     */
    public static void validateOfficeHome(final File officeHome) {
        if (officeHome == null) {
            throw new IllegalStateException("officeHome not set and could not be auto-detected");
        }

        if (!officeHome.isDirectory()) {
            throw new IllegalStateException(
                    "officeHome doesn't exist or is not a directory: " + officeHome);
        }

        if (!getOfficeExecutable(officeHome).isFile()) {
            throw new IllegalStateException(
                    "Invalid officeHome: it doesn't contain soffice.bin: " + officeHome);
        }
    }

    /**
     * 验证指定的文件实例是有效的office模板配置文件目录.
     *
     * @param templateProfileDir 要验证的目录.
     * @throws IllegalStateException 如果指定的目录不是有效的office模板配置文件目录.
     */
    public static void validateOfficeTemplateProfileDirectory(final File templateProfileDir) {
        if (templateProfileDir == null || new File(templateProfileDir, "user").isDirectory()) {
            return;
        }

        throw new IllegalStateException(
                "templateProfileDir doesn't appear to contain a user profile : " + templateProfileDir);
    }

    /**
     * 验证指定的文件实例是否是有效的office工作目录.
     *
     * @param workingDir 要验证的目录.
     * @throws IllegalStateException 如果指定的目录不是有效的office工作目录.
     */
    public static void validateOfficeWorkingDirectory(final File workingDir) {
        if (!workingDir.isDirectory()) {
            throw new IllegalStateException(
                    "workingDir doesn't exist or is not a directory: " + workingDir);
        }

        if (!workingDir.canWrite()) {
            throw new IllegalStateException("workingDir '" + workingDir + "' cannot be written to");
        }
    }

    /**
     * 某些特殊的自定义日期格式
     */
    private static final int[] customFormats = new int[]{28, 30, 31, 32, 33, 55, 56, 57, 58};

    public static boolean isDateFormat(Cell cell) {
        return isDateFormat(cell, null);
    }

    /**
     * 判断是否日期格式
     *
     * @param cell        单元格
     * @param cfEvaluator {@link ConditionalFormattingEvaluator}
     * @return 是否日期格式
     */
    public static boolean isDateFormat(Cell cell, ConditionalFormattingEvaluator cfEvaluator) {
        final ExcelNumberFormat nf = ExcelNumberFormat.from(cell, cfEvaluator);
        return isDateFormat(nf.getIdx(), nf.getFormat());
    }

    /**
     * 判断日期格式
     *
     * @param formatIndex  格式索引，一般用于内建格式
     * @param formatString 格式字符串
     * @return 是否为日期格式
     */
    public static boolean isDateFormat(int formatIndex, String formatString) {
        if (ArrayKit.contains(customFormats, formatIndex)) {
            return true;
        }
        // 自定义格式判断
        if (StringKit.isNotEmpty(formatString) &&
                StringKit.containsAny(formatString, "周", "星期", "aa")) {
            // aa  -> 周一
            // aaa -> 星期一
            return true;
        }
        return org.apache.poi.ss.usermodel.DateUtil.isADateFormat(formatIndex, formatString);
    }

    private static File findOfficeHome(final String executablePath, final String... homePaths) {
        return Stream.of(homePaths)
                .map(File::new)
                .filter(homeDir -> new File(homeDir, executablePath).isFile())
                .findFirst()
                .orElse(null);
    }

}
