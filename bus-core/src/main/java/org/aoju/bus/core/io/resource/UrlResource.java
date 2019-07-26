package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.aoju.bus.core.utils.URLUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * URL资源访问类
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class UrlResource implements Resource {

    protected URL url;
    protected String name;

    /**
     * 构造
     *
     * @param url URL
     */
    public UrlResource(URL url) {
        this(url, null);
    }

    /**
     * 构造
     *
     * @param url  URL，允许为空
     * @param name 资源名称
     */
    public UrlResource(URL url, String name) {
        this.url = url;
        this.name = ObjectUtils.defaultIfNull(name, (null != url) ? FileUtils.getName(url.getPath()) : null);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public InputStream getStream() {
        if (null == this.url) {
            throw new CommonException("Resource [{}] not exist!", this.url);
        }
        return URLUtils.getStream(url);
    }

    /**
     * 获得Reader
     *
     * @param charset 编码
     * @return {@link BufferedReader}
     * @since 3.0.1
     */
    public BufferedReader getReader(Charset charset) {
        return URLUtils.getReader(this.url, charset);
    }

    @Override
    public String readStr(Charset charset) throws CommonException {
        BufferedReader reader = null;
        try {
            reader = getReader(charset);
            return IoUtils.read(reader);
        } finally {
            IoUtils.close(reader);
        }
    }

    @Override
    public String readUtf8Str() throws CommonException {
        return readStr(org.aoju.bus.core.consts.Charset.UTF_8);
    }

    @Override
    public byte[] readBytes() throws CommonException {
        InputStream in = null;
        try {
            in = getStream();
            return IoUtils.readBytes(in);
        } finally {
            IoUtils.close(in);
        }
    }

    /**
     * 获得File
     *
     * @return {@link File}
     */
    public File getFile() {
        return FileUtils.file(this.url);
    }

    /**
     * 返回路径
     *
     * @return 返回URL路径
     */
    @Override
    public String toString() {
        return (null == this.url) ? "null" : this.url.toString();
    }

}
