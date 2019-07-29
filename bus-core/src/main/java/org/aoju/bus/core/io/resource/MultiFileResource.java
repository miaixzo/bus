package org.aoju.bus.core.io.resource;

import java.io.File;
import java.util.Collection;

/**
 * 多文件组合资源<br>
 * 此资源为一个利用游标自循环资源，只有调用{@link #next()} 方法才会获取下一个资源，使用完毕后调用{@link #reset()}方法重置游标
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class MultiFileResource extends MultiResource {

    /**
     * 构造
     *
     * @param files
     */
    public MultiFileResource(Collection<File> files) {
        super();
        add(files);
    }

    /**
     * 构造
     *
     * @param files
     */
    public MultiFileResource(File... files) {
        super();
        add(files);
    }

    /**
     * 增加文件资源
     *
     * @param files 文件资源
     * @return this
     */
    public MultiFileResource add(File... files) {
        for (File file : files) {
            this.add(new FileResource(file));
        }
        return this;
    }

    /**
     * 增加文件资源
     *
     * @param files 文件资源
     * @return this
     */
    public MultiFileResource add(Collection<File> files) {
        for (File file : files) {
            this.add(new FileResource(file));
        }
        return this;
    }

    @Override
    public MultiFileResource add(Resource resource) {
        return (MultiFileResource) super.add(resource);
    }

}
