/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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

import java.io.IOException;
import java.io.Serial;
import java.nio.file.*;

import org.miaixz.bus.core.io.file.visitor.CopyVisitor;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.copier.SrcToDestCopier;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ObjectKit;

/**
 * 文件复制封装
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PathCopier extends SrcToDestCopier<Path, PathCopier> {

    @Serial
    private static final long serialVersionUID = 2852288723311L;

    private final CopyOption[] options;

    /**
     * 构造
     *
     * @param src     源文件或目录，不能为{@code null}且必须存在
     * @param target  目标文件或目录
     * @param options 移动参数
     */
    public PathCopier(final Path src, final Path target, final CopyOption[] options) {
        Assert.notNull(target, "Src path must be not null !");
        if (!PathResolve.exists(src, false)) {
            throw new IllegalArgumentException("Src path is not exist!");
        }
        this.src = src;
        this.target = Assert.notNull(target, "Target path must be not null !");
        this.options = ObjectKit.defaultIfNull(options, new CopyOption[] {});
    }

    /**
     * 创建文件或目录拷贝器
     *
     * @param src        源文件或目录
     * @param target     目标文件或目录
     * @param isOverride 是否覆盖目标文件
     * @return {@code PathCopier}
     */
    public static PathCopier of(final Path src, final Path target, final boolean isOverride) {
        return of(src, target,
                isOverride ? new CopyOption[] { StandardCopyOption.REPLACE_EXISTING } : new CopyOption[] {});
    }

    /**
     * 创建文件或目录拷贝器
     *
     * @param src     源文件或目录
     * @param target  目标文件或目录
     * @param options 拷贝参数
     * @return {@code PathCopier}
     */
    public static PathCopier of(final Path src, final Path target, final CopyOption[] options) {
        return new PathCopier(src, target, options);
    }

    /**
     * 拷贝目录下的所有文件或目录到目标目录中，此方法不支持文件对文件的拷贝。
     * <ul>
     * <li>源文件为目录，目标也为目录或不存在，则拷贝目录下所有文件和目录到目标目录下</li>
     * <li>源文件为文件，目标为目录或不存在，则拷贝文件到目标目录下</li>
     * </ul>
     *
     * @param src     源文件路径，如果为目录只在目标中创建新目录
     * @param target  目标目录，如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return Path
     * @throws InternalException IO异常
     */
    private static Path copyContent(final Path src, final Path target, final CopyOption... options)
            throws InternalException {
        try {
            Files.walkFileTree(src, new CopyVisitor(src, target, options));
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return target;
    }

    /**
     * 通过JDK7+的 {@link Files#copy(Path, Path, CopyOption...)} 方法拷贝文件 此方法不支持递归拷贝目录，如果src传入是目录，只会在目标目录中创建空目录
     *
     * @param src     源文件路径，如果为目录只在目标中创建新目录
     * @param target  目标文件或目录，如果为目录使用与源文件相同的文件名
     * @param options {@link StandardCopyOption}
     * @return Path
     * @throws InternalException IO异常
     */
    private static Path copyFile(final Path src, final Path target, final CopyOption... options)
            throws InternalException {
        Assert.notNull(src, "Source file is null !");
        Assert.notNull(target, "Target file or directory is null !");

        final Path targetPath = PathResolve.isDirectory(target) ? target.resolve(src.getFileName()) : target;
        // 创建级联父目录
        PathResolve.mkParentDirs(targetPath);
        try {
            return Files.copy(src, targetPath, options);
        } catch (final IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * 复制src到target中
     * <ul>
     * <li>src路径和target路径相同时，不执行操作</li>
     * <li>src为文件，target为已存在目录，则拷贝到目录下，文件名不变。</li>
     * <li>src为文件，target为不存在路径，则目标以文件对待（自动创建父级目录），相当于拷贝后重命名，比如：/dest/aaa，如果aaa不存在，则aaa被当作文件名</li>
     * <li>src为文件，target是一个已存在的文件，则当{@link CopyOption}设为覆盖时会被覆盖，默认不覆盖，抛出{@link FileAlreadyExistsException}</li>
     * <li>src为目录，target为已存在目录，整个src目录连同其目录拷贝到目标目录中</li>
     * <li>src为目录，target为不存在路径，则自动创建目标为新目录，并只拷贝src内容到目标目录中，相当于重命名目录。</li>
     * <li>src为目录，target为文件，抛出{@link IllegalArgumentException}</li>
     * </ul>
     *
     * @return 目标Path
     * @throws InternalException IO异常
     */
    @Override
    public Path copy() throws InternalException {
        if (PathResolve.isDirectory(src)) {
            if (PathResolve.exists(target, false)) {
                if (PathResolve.isDirectory(target)) {
                    return copyContent(src, target.resolve(src.getFileName()), options);
                } else {
                    // src目录，target文件，无法拷贝
                    throw new IllegalArgumentException("Can not copier directory to a file!");
                }
            } else {
                // 目标不存在，按照重命名对待
                return copyContent(src, target, options);
            }
        }
        return copyFile(src, target, options);
    }

    /**
     * 复制src的内容到target中
     * <ul>
     * <li>src路径和target路径相同时，不执行操作</li>
     * <li>src为文件，target为已存在目录，则拷贝到目录下，文件名不变。</li>
     * <li>src为文件，target为不存在路径，则目标以文件对待（自动创建父级目录），相当于拷贝后重命名，比如：/dest/aaa，如果aaa不存在，则aaa被当作文件名</li>
     * <li>src为文件，target是一个已存在的文件，则当{@link CopyOption}设为覆盖时会被覆盖，默认不覆盖，抛出{@link FileAlreadyExistsException}</li>
     * <li>src为目录，target为已存在目录，整个src目录下的内容拷贝到目标目录中</li>
     * <li>src为目录，target为不存在路径，则自动创建目标为新目录，整个src目录下的内容拷贝到目标目录中，相当于重命名目录。</li>
     * <li>src为目录，target为文件，抛出IO异常</li>
     * </ul>
     *
     * @return 目标Path
     * @throws InternalException IO异常
     */
    public Path copyContent() throws InternalException {
        if (PathResolve.isDirectory(src, false)) {
            return copyContent(src, target, options);
        }
        return copyFile(src, target, options);
    }

}
