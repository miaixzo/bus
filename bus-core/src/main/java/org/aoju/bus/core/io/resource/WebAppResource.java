package org.aoju.bus.core.io.resource;

import org.aoju.bus.core.utils.FileUtils;

import java.io.File;

/**
 * Web root资源访问对象
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class WebAppResource extends FileResource {

    /**
     * 构造
     *
     * @param path 相对于Web root的路径
     */
    public WebAppResource(String path) {
        super(new File(FileUtils.getWebRoot(), path));
    }

}
