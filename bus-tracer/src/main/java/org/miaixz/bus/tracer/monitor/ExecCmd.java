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
package org.miaixz.bus.tracer.monitor;

import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.FileKit;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class ExecCmd {

    /**
     * 执行外部程序,并获取标准输出
     *
     * @param cmd      命令
     * @param encoding 编码
     * @return 执行结果
     */
    public static String execute(String[] cmd, String... encoding) {
        BufferedReader bufferedReader;
        InputStreamReader inputStreamReader;
        try {
            Process p = Runtime.getRuntime().exec(cmd);

            /* "标准输出流"就在当前方法中读取 */
            BufferedInputStream bis = new BufferedInputStream(p.getInputStream());

            if (null != encoding && encoding.length != 0) {
                inputStreamReader = new InputStreamReader(bis, encoding[0]);
            } else {
                inputStreamReader = new InputStreamReader(bis, Charset.DEFAULT_UTF_8);
            }
            bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line;

            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
                sb.append(Symbol.LF);
            }

            bufferedReader.close();
            inputStreamReader.close();
            p.destroy();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return 本机器IP
     */
    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        return Http.HOST_IPV4;
    }

    /**
     * @return 本机器名称
     */
    public static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
        }
        return "未知";
    }

    /**
     * @return java版本信息
     */
    public static String version() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"java", "-version"});
            InputStreamReader inputStreamReader = new InputStreamReader(p.getErrorStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while (null != (line = bufferedReader.readLine())) {
                sb.append(line);
                sb.append(Symbol.LF);
            }
            bufferedReader.close();
            p.destroy();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Normal.EMPTY;
    }

    /**
     * @return 项目根目录
     */
    public static String getRootPath() {
        return rootPath(Normal.EMPTY);
    }

    /**
     * 自定义追加路径
     *
     * @param path 路径
     * @return 项目根目录
     */
    public static String getRootPath(String path) {
        return rootPath(Symbol.SLASH + path);
    }

    /**
     * 导出堆快照
     *
     * @return 快照信息
     */
    public static String exJmap() {
        String id = getPid();
        String path = ExecCmd.getRootPath("dump/" + id + "_heap.hprof");
        File file = new File(ExecCmd.getRootPath("dump/"));
        if (!file.exists()) {
            file.mkdirs();
        }
        ExecCmd.execute(new String[]{"jmap", "-dump:format=b,file=" + path, id});
        return path;
    }

    /**
     * 导出线程快照
     *
     * @return 快照信息
     */
    public static String exJstack() {
        String id = getPid();
        String path = ExecCmd.getRootPath("dump/" + id + "_thread.txt");
        String s = ExecCmd.execute(new String[]{"jstack", id});
        File file = new File(path);
        FileKit.writeString(s, file, Charset.UTF_8);
        return path;
    }

    /**
     * 获取当前应用进程id
     *
     * @return pid
     */
    public static String getPid() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split(Symbol.AT)[0];
        return pid;
    }

    /**
     * 该进程的线程信息
     * X轴为时间，Y轴为值的变化
     *
     * @return the StackEntity
     */
    public static StackEntity jstack() {
        String id = getPid();
        String s = ExecCmd.execute(new String[]{"jstack", id});
        int total = appearNumber(s, "nid=");
        String prefix = "java.lang.Thread.State: ";
        int RUNNABLE = appearNumber(s, prefix + "RUNNABLE");
        int TIMED_WAITING = appearNumber(s, prefix + "TIMED_WAITING");
        int WAITING = appearNumber(s, prefix + "WAITING");
        return new StackEntity(id, total, RUNNABLE, TIMED_WAITING, WAITING);
    }

    /**
     * 匹配字符出现次数
     *
     * @param srcText  文本内容
     * @param findText 查找内容
     * @return 次数
     */
    public static int appearNumber(String srcText, String findText) {
        int count = 0;
        Pattern p = Pattern.compile(findText);
        Matcher m = p.matcher(srcText);
        while (m.find()) {
            count++;
        }
        return count;
    }

    /**
     * 类加载信息
     * X轴为时间，Y轴为值的变化
     *
     * @return the list
     * @throws IOException 异常
     */
    public static List<Map<String, String>> jstatClazz() throws IOException {
        String id = getPid();
        List<Map<String, String>> jstatClass = jstat(new String[]{"jstat", "-class", id});
        List<Map<String, String>> jstatCompiler = jstat(new String[]{"jstat", "-compiler", id});
        jstatClass.addAll(jstatCompiler);
        return jstatClass;
    }

    /**
     * 堆内存信息
     * X轴为时间，Y轴为值的变化
     *
     * @return the list
     * @throws IOException 异常
     */
    public static List<Map<String, String>> jstatGc() throws IOException {
        return jstat(new String[]{"jstat", "-gc", getPid()});
    }

    /**
     * 堆内存百分比
     * 实时监控
     *
     * @return the list
     * @throws IOException 异常
     */
    public static List<Map<String, String>> jstatGcu() throws IOException {
        return jstat(new String[]{"jstat", "-gcutil", getPid()});
    }

    /**
     * @param path 路径
     * @return 项目路径
     */
    private static String rootPath(String path) {
        String rootPath = Normal.EMPTY;

        //获取项目的根路径
        String classPath = System.getProperty(Keys.USER_DIR);

        //windows下
        if (Symbol.BACKSLASH.equals(File.separator)) {
            rootPath = classPath + path;
            rootPath = rootPath.replaceAll(Symbol.SLASH, "\\\\");
            if (rootPath.startsWith(Symbol.BACKSLASH)) {
                rootPath = rootPath.substring(1);
            }
        }
        //linux下
        if (Symbol.SLASH.equals(File.separator)) {
            rootPath = classPath + path;
            rootPath = rootPath.replaceAll("\\\\", Symbol.SLASH);
        }
        return rootPath;
    }

    /**
     * jstat 模板方法
     *
     * @param strings 命令
     * @return 集合
     * @throws IOException 异常
     */
    private static List<Map<String, String>> jstat(String[] strings) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();
        String s = ExecCmd.execute(strings);
        assert null != s;
        BufferedReader reader = new BufferedReader(new StringReader(s));
        String[] keys = ArrayKit.ofArray(reader.readLine().split("\\s+|\t"));
        String[] values = ArrayKit.ofArray(reader.readLine().split("\\s+|\t"));
        // 特殊情况
        if (strings[1].equals("-compiler")) {
            for (int i = 0; i < 4; i++) {
                Map<String, String> map = new HashMap();
                map.put(keys[i], values[i]);
                list.add(map);
            }
            return list;
        }
        // 正常流程
        for (int i = 0; i < keys.length; i++) {
            Map<String, String> map = new HashMap();
            map.put(keys[i], values[i]);
            list.add(map);
        }
        return list;
    }

}
