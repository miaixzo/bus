package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.utils.FileUtils;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.core.utils.URLUtils;

import java.io.File;

/**
 * 文件资源访问对象
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class FileResource extends UrlResource {

    /**
     * 构造
     *
     * @param file 文件
     */
    public FileResource(File file) {
        this(file, file.getName());
    }

    /**
     * 构造
     *
     * @param file     文件
     * @param fileName 文件名，如果为null获取文件本身的文件名
     */
    public FileResource(File file, String fileName) {
        super(URLUtils.getURL(file), StringUtils.isBlank(fileName) ? file.getName() : fileName);
    }

    /**
     * 构造
     *
     * @param path 文件绝对路径或相对ClassPath路径，但是这个路径不能指向一个jar包中的文件
     */
    public FileResource(String path) {
        this(FileUtils.file(path));
    }

}
