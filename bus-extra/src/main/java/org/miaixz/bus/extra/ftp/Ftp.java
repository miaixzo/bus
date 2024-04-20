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
package org.miaixz.bus.extra.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.FileKit;
import org.miaixz.bus.core.toolkit.ObjectKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * FTP客户端封装
 * 此客户端基于Apache-Commons-Net
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ftp extends AbstractFtp {

    /**
     * 默认端口
     */
    public static final int DEFAULT_PORT = 21;

    private FTPClient client;
    private FtpMode mode;
    /**
     * 执行完操作是否返回当前目录
     */
    private boolean backToPwd;

    /**
     * 构造,匿名登录
     *
     * @param host 域名或IP
     */
    public Ftp(String host) {
        this(host, DEFAULT_PORT);
    }

    /**
     * 构造,匿名登录
     *
     * @param host 域名或IP
     * @param port 端口
     */
    public Ftp(String host, int port) {
        this(host, port, "anonymous", Normal.EMPTY);
    }

    /**
     * 构造
     *
     * @param host     域名或IP
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     */
    public Ftp(String host, int port, String user, String password) {
        this(host, port, user, password, org.miaixz.bus.core.lang.Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param host     域名或IP
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @param charset  编码
     */
    public Ftp(String host, int port, String user, String password, java.nio.charset.Charset charset) {
        this(host, port, user, password, charset, null, null);
    }

    /**
     * 构造
     *
     * @param host               域名或IP
     * @param port               端口
     * @param user               用户名
     * @param password           密码
     * @param charset            编码
     * @param serverLanguageCode 服务器语言 例如：zh
     * @param systemKey          服务器标识 例如：org.apache.commons.net.ftp.FTPClientConfig.SYST_NT
     */
    public Ftp(String host, int port, String user, String password, java.nio.charset.Charset charset, String serverLanguageCode, String systemKey) {
        this(host, port, user, password, charset, serverLanguageCode, systemKey, null);
    }

    /**
     * 构造
     *
     * @param host               域名或IP
     * @param port               端口
     * @param user               用户名
     * @param password           密码
     * @param charset            编码
     * @param serverLanguageCode 服务器语言
     * @param systemKey          系统关键字
     * @param mode               模式
     */
    public Ftp(String host, int port, String user, String password, Charset charset, String serverLanguageCode, String systemKey, FtpMode mode) {
        this(new FtpConfig(host, port, user, password, charset, serverLanguageCode, systemKey), mode);
    }

    /**
     * 构造
     *
     * @param config FTP配置
     * @param mode   模式
     */
    public Ftp(FtpConfig config, FtpMode mode) {
        super(config);
        this.mode = mode;
        this.init();
    }

    /**
     * 构造
     *
     * @param client 自定义实例化好的{@link FTPClient}
     */
    public Ftp(FTPClient client) {
        super(FtpConfig.create());
        this.client = client;
    }

    /**
     * 初始化连接
     *
     * @return this
     */
    public Ftp init() {
        return this.init(this.ftpConfig, this.mode);
    }

    /**
     * 初始化连接
     *
     * @param host     域名或IP
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @return this
     */
    public Ftp init(String host, int port, String user, String password) {
        return this.init(host, port, user, password, null);
    }

    /**
     * 初始化连接
     *
     * @param host     域名或IP
     * @param port     端口
     * @param user     用户名
     * @param password 密码
     * @param mode     模式
     * @return this
     */
    public Ftp init(String host, int port, String user, String password, FtpMode mode) {
        return init(new FtpConfig(host, port, user, password, this.ftpConfig.getCharset(), null, null), mode);
    }

    /**
     * 初始化连接
     *
     * @param config FTP配置
     * @param mode   模式
     * @return this
     */
    public Ftp init(FtpConfig config, FtpMode mode) {
        final FTPClient client = new FTPClient();
        final Charset charset = config.getCharset();
        if (null != charset) {
            client.setControlEncoding(charset.toString());
        }
        client.setConnectTimeout((int) config.getConnectionTimeout());
        final String systemKey = config.getSystemKey();
        if (StringKit.isNotBlank(systemKey)) {
            final FTPClientConfig conf = new FTPClientConfig(systemKey);
            final String serverLanguageCode = config.getServerLanguageCode();
            if (StringKit.isNotBlank(serverLanguageCode)) {
                conf.setServerLanguageCode(config.getServerLanguageCode());
            }
            client.configure(conf);
        }

        try {
            // 连接ftp服务器
            client.connect(config.getHost(), config.getPort());
            client.setSoTimeout((int) config.getSoTimeout());
            // 登录ftp服务器
            client.login(config.getUser(), config.getPassword());
        } catch (IOException e) {
            throw new InternalException(e);
        }
        final int replyCode = client.getReplyCode(); // 是否成功登录服务器
        if (false == FTPReply.isPositiveCompletion(replyCode)) {
            try {
                client.disconnect();
            } catch (IOException e) {
                // ignore
            }
            throw new InternalException("Login failed for user [{}], reply code is: [{}]", config.getUser(), replyCode);
        }
        this.client = client;
        if (null != mode) {
            setMode(mode);
        }
        return this;
    }

    /**
     * 设置FTP连接模式，可选主动和被动模式
     *
     * @param mode 模式枚举
     * @return this
     */
    public Ftp setMode(FtpMode mode) {
        this.mode = mode;
        switch (mode) {
            case Active:
                this.client.enterLocalActiveMode();
                break;
            case Passive:
                this.client.enterLocalPassiveMode();
                break;
        }
        return this;
    }

    /**
     * 是否执行完操作返回当前目录
     *
     * @return 执行完操作是否返回当前目录
     */
    public boolean isBackToPwd() {
        return this.backToPwd;
    }

    /**
     * 设置执行完操作是否返回当前目录
     *
     * @param backToPwd 执行完操作是否返回当前目录
     * @return this
     */
    public Ftp setBackToPwd(boolean backToPwd) {
        this.backToPwd = backToPwd;
        return this;
    }

    /**
     * 如果连接超时的话，重新进行连接 经测试，当连接超时时，client.isConnected()仍然返回ture，无法判断是否连接超时 因此，通过发送pwd命令的方式，检查连接是否超时
     *
     * @return this
     */
    @Override
    public Ftp reconnectIfTimeout() {
        String pwd = null;
        try {
            pwd = pwd();
        } catch (InternalException fex) {
            // ignore
        }

        if (null == pwd) {
            return this.init();
        }
        return this;
    }

    /**
     * 改变目录
     *
     * @param directory 目录
     * @return 是否成功
     */
    @Override
    public synchronized boolean cd(String directory) {
        if (StringKit.isBlank(directory)) {
            return true;
        }

        try {
            return client.changeWorkingDirectory(directory);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 远程当前目录
     *
     * @return 远程当前目录
     */
    @Override
    public String pwd() {
        try {
            return client.printWorkingDirectory();
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public List<String> ls(String path) {
        return ArrayKit.map(lsFiles(path), FTPFile::getName);
    }

    /**
     * 遍历某个目录下所有文件和目录，不会递归遍历
     * 此方法自动过滤"."和".."两种目录
     *
     * @param path   目录
     * @param filter 过滤器，null表示不过滤，默认去掉"."和".."两种目录
     * @return 文件或目录列表
     */
    public List<FTPFile> lsFiles(String path, Predicate<FTPFile> filter) {
        final FTPFile[] ftpFiles = lsFiles(path);
        if (ArrayKit.isEmpty(ftpFiles)) {
            return new ArrayList<>();
        }

        final List<FTPFile> result = new ArrayList<>(ftpFiles.length - 2 <= 0 ? ftpFiles.length : ftpFiles.length - 2);
        String fileName;
        for (FTPFile ftpFile : ftpFiles) {
            fileName = ftpFile.getName();
            if (false == StringKit.equals(Symbol.DOT, fileName)
                    && false == StringKit.equals(Symbol.DOT + Symbol.DOT, fileName)) {
                if (null == filter || filter.test(ftpFile)) {
                    result.add(ftpFile);
                }
            }
        }
        return result;
    }

    /**
     * 遍历某个目录下所有文件和目录，不会递归遍历
     *
     * @param path 目录，如果目录不存在，抛出异常
     * @return 文件或目录列表
     * @throws InternalException 路径不存在
     * @throws InternalException IO异常
     */
    public FTPFile[] lsFiles(String path) throws InternalException {
        String pwd = null;
        if (StringKit.isNotBlank(path)) {
            pwd = pwd();
            if (false == isDir(path)) {
                throw new InternalException("Change dir to [{}] error, maybe path not exist!", path);
            }
        }

        FTPFile[] ftpFiles;
        try {
            ftpFiles = this.client.listFiles();
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            cd(pwd);
        }

        return ftpFiles;
    }

    @Override
    public boolean mkdir(String dir) {
        try {
            return this.client.makeDirectory(dir);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 判断ftp服务器目录内是否还有子元素（目录或文件）
     *
     * @param path 文件路径
     * @return 是否存在
     */
    public boolean existFile(String path) {
        FTPFile[] ftpFileArr;
        try {
            ftpFileArr = client.listFiles(path);
        } catch (IOException e) {
            throw new InternalException(e);
        }
        return ArrayKit.isNotEmpty(ftpFileArr);
    }

    /**
     * 获取服务端目录状态。
     *
     * @param path 路径
     * @return 状态int，服务端不同，返回不同
     */
    public int stat(String path) {
        try {
            return this.client.stat(path);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    public boolean delFile(String path) {
        final String pwd = pwd();
        final String fileName = FileKit.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        if (false == cd(dir)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!");
        }

        boolean isSuccess;
        try {
            isSuccess = client.deleteFile(fileName);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            cd(pwd);
        }
        return isSuccess;
    }

    @Override
    public boolean delDir(String dirPath) {
        FTPFile[] dirs;
        try {
            dirs = client.listFiles(dirPath);
        } catch (IOException e) {
            throw new InternalException(e);
        }
        String name;
        String childPath;
        for (FTPFile ftpFile : dirs) {
            name = ftpFile.getName();
            childPath = StringKit.format("{}/{}", dirPath, name);
            if (ftpFile.isDirectory()) {
                // 上级和本级目录除外
                if (false == ObjectKit.equals(name, Symbol.DOT)
                        && false == ObjectKit.equals(name, Symbol.DOUBLE_DOT)) {
                    delDir(childPath);
                }
            } else {
                delFile(childPath);
            }
        }

        // 删除空目录
        try {
            return this.client.removeDirectory(dirPath);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 上传文件到指定目录,可选：
     *
     * <pre>
     * 1. dest为null或""上传到当前路径
     * 2. dest为相对路径则相对于当前路径的子路径
     * 3. dest为绝对路径则上传到此路径
     * </pre>
     *
     * @param dest 服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param file 文件
     * @return 是否上传成功
     */
    @Override
    public boolean upload(String dest, File file) {
        Assert.notNull(file, "file to upload is null !");
        return upload(dest, file.getName(), file);
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. dest为null或""上传到当前路径
     * 2. dest为相对路径则相对于当前路径的子路径
     * 3. dest为绝对路径则上传到此路径
     * </pre>
     *
     * @param file     文件
     * @param dest     服务端路径,可以为{@code null} 或者相对路径或绝对路径
     * @param fileName 自定义在服务端保存的文件名
     * @return 是否上传成功
     */
    public boolean upload(String dest, String fileName, File file) {
        try (InputStream in = FileKit.getInputStream(file)) {
            return upload(dest, fileName, in);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 上传文件到指定目录,可选：
     *
     * <pre>
     * 1. path为null或""上传到当前路径
     * 2. path为相对路径则相对于当前路径的子路径
     * 3. path为绝对路径则上传到此路径
     * </pre>
     *
     * @param dest       服务端路径,可以为{@code null} 或者相对路径或绝对路径
     * @param fileName   文件名
     * @param fileStream 文件流
     * @return 是否上传成功
     */
    public boolean upload(String dest, String fileName, InputStream fileStream) {
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
        } catch (IOException e) {
            throw new InternalException(e);
        }

        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }

        if (StringKit.isNotBlank(dest)) {
            mkDirs(dest);
            if (false == cd(dest)) {
                throw new InternalException("Change dir to [{}] error, maybe dir not exist!");
            }
        }

        try {
            return client.storeFile(fileName, fileStream);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (this.backToPwd) {
                cd(pwd);
            }
        }
    }

    /**
     * 下载文件
     *
     * @param path    文件路径，包含文件名
     * @param outFile 输出文件或目录，当为目录时，使用服务端的文件名
     */
    @Override
    public void download(String path, File outFile) {
        final String fileName = FileKit.getName(path);
        final String dir = StringKit.removeSuffix(path, fileName);
        download(dir, fileName, outFile);
    }

    /**
     * 下载文件
     *
     * @param path     文件所在路径（远程目录），不包含文件名
     * @param fileName 文件名
     * @param outFile  输出文件或目录，当为目录时使用服务端文件名
     */
    public void download(String path, String fileName, File outFile) {
        if (outFile.isDirectory()) {
            outFile = new File(outFile, fileName);
        }
        if (false == outFile.exists()) {
            FileKit.touch(outFile);
        }
        try (OutputStream out = FileKit.getOutputStream(outFile)) {
            download(path, fileName, out);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 下载文件到输出流
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param out      输出位置
     */
    public void download(String path, String fileName, OutputStream out) {
        download(path, fileName, out, null);
    }

    /**
     * 下载文件到输出流
     *
     * @param path     服务端的文件路径
     * @param fileName 服务端的文件名
     * @param out      输出流，下载的文件写出到这个流中
     * @param charset  文件名编码，通过此编码转换文件名编码为ISO8859-1
     */
    public void download(String path, String fileName, OutputStream out, java.nio.charset.Charset charset) {
        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }

        if (false == cd(path)) {
            throw new InternalException("Change dir to [{}] error, maybe dir not exist!");
        }

        if (null != charset) {
            fileName = new String(fileName.getBytes(charset), org.miaixz.bus.core.lang.Charset.ISO_8859_1);
        }
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            client.retrieveFile(fileName, out);
        } catch (IOException e) {
            throw new InternalException(e);
        } finally {
            if (backToPwd) {
                cd(pwd);
            }
        }
    }

    /**
     * 递归获取远程文件
     *
     * @param sourcePath 服务器目录
     * @param desPath    本地目录
     */
    @Override
    public void download(String sourcePath, String desPath) {
        FTPFile[] lsFiles = lsFiles(sourcePath);
        for (FTPFile ftpFile : lsFiles) {
            String sourcePathPathFile = sourcePath + Symbol.SLASH + ftpFile.getName();
            String destinationPathFile = desPath + Symbol.SLASH + ftpFile.getName();

            if (!ftpFile.isDirectory()) {
                // 本地不存在文件或者ftp上文件有变更则下载
                if (!FileKit.exists(destinationPathFile)
                        || (ftpFile.getTimestamp().getTimeInMillis() > FileKit.lastModifiedTime(destinationPathFile).getTime())) {
                    download(sourcePathPathFile, FileKit.file(destinationPathFile));
                }
            } else if (!(Symbol.DOT.equals(ftpFile.getName()) || Symbol.DOUBLE_DOT.equals(ftpFile.getName()))) {
                FileKit.mkdir(destinationPathFile);
                download(sourcePathPathFile, destinationPathFile);
            }
        }
    }

    /**
     * 获取FTPClient客户端对象
     *
     * @return {@link FTPClient}
     */
    public FTPClient getClient() {
        return this.client;
    }

    @Override
    public void close() throws IOException {
        if (null != this.client) {
            this.client.logout();
            if (this.client.isConnected()) {
                this.client.disconnect();
            }
            this.client = null;
        }
    }

}
