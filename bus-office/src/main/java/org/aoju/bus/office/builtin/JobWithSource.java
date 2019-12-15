package org.aoju.bus.office.builtin;

import java.io.File;
import java.io.OutputStream;

/**
 * 具有指定转换源的转换作业.
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface JobWithSource {

    /**
     * 将当前转换配置为将结果写入指定的目标.
     *
     * @param target 将写入转换结果的文件。现有文件将被覆盖.
     *               如果文件被JVM或任何其他应用程序锁定或不可写，则会抛出异常.
     * @return 当前转换规范.
     */
    OptionalTarget to(File target);

    /**
     * 将当前转换配置为将结果写入指定的{@link OutputStream}.
     * 在写入转换之后，流将被关闭.
     *
     * @param target 写入转换结果的输出流.
     * @return 当前转换规范.
     */
    RequiredTarget to(OutputStream target);

    /**
     * 将当前转换配置为将结果写入指定的{@link OutputStream}.
     * 在写入转换之后，流将被关闭.
     *
     * @param target      写入转换结果的输出流.
     * @param closeStream 确定写入结果后输出流是否关闭.
     * @return 当前转换规范.
     */
    RequiredTarget to(OutputStream target, boolean closeStream);

}
