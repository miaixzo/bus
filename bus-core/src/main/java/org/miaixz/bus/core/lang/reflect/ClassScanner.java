/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.lang.reflect;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.miaixz.bus.core.center.iterator.EnumerationIterator;
import org.miaixz.bus.core.io.file.FileType;
import org.miaixz.bus.core.io.resource.JarResource;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.url.UrlDecoder;
import org.miaixz.bus.core.xyz.*;

/**
 * 类扫描器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ClassScanner implements Serializable {

    private static final long serialVersionUID = -1L;

    /**
     * 包名
     */
    private final String packageName;
    /**
     * 包名，最后跟一个点，表示包名，避免在检查前缀时的歧义 如果包名指定为空，不跟点
     */
    private final String packageNameWithDot;
    /**
     * 包路径，用于文件中对路径操作
     */
    private final String packageDirName;
    /**
     * 包路径，用于jar中对路径操作，在Linux下与packageDirName一致
     */
    private final String packagePath;
    /**
     * 过滤器
     */
    private final Predicate<Class<?>> classPredicate;
    /**
     * 编码
     */
    private final java.nio.charset.Charset charset;
    /**
     * 扫描结果集
     */
    private final Set<Class<?>> classes = new HashSet<>();
    /**
     * 获取加载错误的类名列表
     */
    private final Set<String> classesOfLoadError = new HashSet<>();
    /**
     * 类加载器
     */
    private ClassLoader classLoader;
    /**
     * 是否初始化类
     */
    private boolean initialize;
    /**
     * 忽略loadClass时的错误
     */
    private boolean ignoreLoadError = false;

    /**
     * 构造，默认UTF-8编码
     */
    public ClassScanner() {
        this(null);
    }

    /**
     * 构造，默认UTF-8编码
     *
     * @param packageName 包名，所有包传入""或者null
     */
    public ClassScanner(final String packageName) {
        this(packageName, null);
    }

    /**
     * 构造，默认UTF-8编码
     *
     * @param packageName    包名，所有包传入""或者null
     * @param classPredicate 过滤器，无需传入null
     */
    public ClassScanner(final String packageName, final Predicate<Class<?>> classPredicate) {
        this(packageName, classPredicate, Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param packageName    包名，所有包传入""或者null
     * @param classPredicate 过滤器，无需传入null
     * @param charset        编码
     */
    public ClassScanner(String packageName, final Predicate<Class<?>> classPredicate,
            final java.nio.charset.Charset charset) {
        packageName = StringKit.toStringOrEmpty(packageName);
        this.packageName = packageName;
        this.packageNameWithDot = StringKit.addSuffixIfNot(packageName, Symbol.DOT);
        this.packageDirName = packageName.replace(Symbol.C_DOT, File.separatorChar);
        this.packagePath = packageName.replace(Symbol.C_DOT, Symbol.C_SLASH);
        this.classPredicate = classPredicate;
        this.charset = charset;
    }

    /**
     * 扫描指定包路径下所有包含指定注解的类，包括其他加载的jar或者类
     *
     * @param packageName     包路径
     * @param annotationClass 注解类
     * @return 类集合
     */
    public static Set<Class<?>> scanAllPackageByAnnotation(final String packageName,
            final Class<? extends Annotation> annotationClass) {
        return scanAllPackage(packageName, clazz -> clazz.isAnnotationPresent(annotationClass));
    }

    /**
     * 扫描指定包路径下所有包含指定注解的类 如果classpath下已经有类，不再扫描其他加载的jar或者类
     *
     * @param packageName     包路径
     * @param annotationClass 注解类
     * @return 类集合
     */
    public static Set<Class<?>> scanPackageByAnnotation(final String packageName,
            final Class<? extends Annotation> annotationClass) {
        return scanPackage(packageName, clazz -> clazz.isAnnotationPresent(annotationClass));
    }

    /**
     * 扫描指定包路径下所有指定类或接口的子类或实现类，不包括指定父类本身，包括其他加载的jar或者类
     *
     * @param packageName 包路径
     * @param superClass  父类或接口（不包括）
     * @return 类集合
     */
    public static Set<Class<?>> scanAllPackageBySuper(final String packageName, final Class<?> superClass) {
        return scanAllPackage(packageName, clazz -> superClass.isAssignableFrom(clazz) && !superClass.equals(clazz));
    }

    /**
     * 扫描指定包路径下所有指定类或接口的子类或实现类，不包括指定父类本身 如果classpath下已经有类，不再扫描其他加载的jar或者类
     *
     * @param packageName 包路径
     * @param superClass  父类或接口（不包括）
     * @return 类集合
     */
    public static Set<Class<?>> scanPackageBySuper(final String packageName, final Class<?> superClass) {
        return scanPackage(packageName, clazz -> superClass.isAssignableFrom(clazz) && !superClass.equals(clazz));
    }

    /**
     * 扫描该包路径下所有class文件，包括其他加载的jar或者类
     *
     * @return 类集合
     */
    public static Set<Class<?>> scanAllPackage() {
        return scanAllPackage(Normal.EMPTY, null);
    }

    /**
     * 扫描classpath下所有class文件，如果classpath下已经有类，不再扫描其他加载的jar或者类
     *
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage() {
        return scanPackage(Normal.EMPTY, null);
    }

    /**
     * 扫描该包路径下所有class文件
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage(final String packageName) {
        return scanPackage(packageName, null);
    }

    /**
     * 扫描包路径下和所有在classpath中加载的类，满足class过滤器条件的所有class文件， 如果包路径为 com.abs + A.class 但是输入 abs会产生classNotFoundException
     * 因为className 应该为 com.abs.A 现在却成为abs.A,此工具类对该异常进行忽略处理
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @param classFilter class过滤器，过滤掉不需要的class
     * @return 类集合
     */
    public static Set<Class<?>> scanAllPackage(final String packageName, final Predicate<Class<?>> classFilter) {
        return new ClassScanner(packageName, classFilter).scan(true);
    }

    /**
     * 扫描包路径下满足class过滤器条件的所有class文件， 如果包路径为 com.abs + A.class 但是输入 abs会产生classNotFoundException 因为className 应该为
     * com.abs.A 现在却成为abs.A,此工具类对该异常进行忽略处理
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @param classFilter class过滤器，过滤掉不需要的class
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage(final String packageName, final Predicate<Class<?>> classFilter) {
        return new ClassScanner(packageName, classFilter).scan();
    }

    /**
     * 扫描包路径下满足class过滤器条件的所有class文件 此方法首先扫描指定包名下的资源目录，如果未扫描到，则扫描整个classpath中所有加载的类
     *
     * @return 类集合
     */
    public Set<Class<?>> scan() {
        return scan(false);
    }

    /**
     * 扫描包路径下满足class过滤器条件的所有class文件
     *
     * @param forceScanJavaClassPaths 是否强制扫描其他位于classpath关联jar中的类
     * @return 类集合
     */
    public Set<Class<?>> scan(final boolean forceScanJavaClassPaths) {
        // 多次扫描时,清理上次扫描历史
        this.classes.clear();
        this.classesOfLoadError.clear();

        for (final URL url : ResourceKit.getResourceUrlIter(this.packagePath, this.classLoader)) {
            switch (url.getProtocol()) {
            case "file":
                scanFile(new File(UrlDecoder.decode(url.getFile(), this.charset)), null);
                break;
            case "jar":
                scanJar(new JarResource(url).getJarFile());
                break;
            }
        }

        // classpath下未找到，则扫描其他jar包下的类
        if (forceScanJavaClassPaths || CollKit.isEmpty(this.classes)) {
            scanJavaClassPaths();
        }

        return Collections.unmodifiableSet(this.classes);
    }

    /**
     * 忽略加载错误扫描后，可以获得之前扫描时加载错误的类名字集合
     *
     * @return 加载错误的类名字集合
     */
    public Set<String> getClassesOfLoadError() {
        return Collections.unmodifiableSet(this.classesOfLoadError);
    }

    /**
     * 设置是否忽略所有错误
     *
     * @param ignoreLoadError 是否忽略错误
     */
    public void setIgnoreLoadError(final boolean ignoreLoadError) {
        this.ignoreLoadError = ignoreLoadError;
    }

    /**
     * 设置是否在扫描到类时初始化类
     *
     * @param initialize 是否初始化类
     */
    public void setInitialize(final boolean initialize) {
        this.initialize = initialize;
    }

    /**
     * 设置自定义的类加载器
     *
     * @param classLoader 类加载器
     */
    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * 扫描Java指定的ClassPath路径
     */
    private void scanJavaClassPaths() {
        final String[] javaClassPaths = Keys.getJavaClassPaths();
        for (String classPath : javaClassPaths) {
            // bug修复，由于路径中空格和中文导致的Jar找不到
            classPath = UrlDecoder.decode(classPath, Charset.defaultCharset());

            scanFile(new File(classPath), null);
        }
    }

    /**
     * 扫描文件或目录中的类
     *
     * @param file    文件或目录
     * @param rootDir 包名对应classpath绝对路径
     */
    private void scanFile(final File file, final String rootDir) {
        if (file.isFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(FileType.CLASS)) {
                final String className = fileName//
                        // 8为classes长度，fileName.length() - 6为".class"的长度
                        .substring(rootDir.length(), fileName.length() - 6)//
                        .replace(File.separatorChar, Symbol.C_DOT);//
                // 加入满足条件的类
                addIfAccept(className);
            } else if (fileName.endsWith(FileType.JAR)) {
                try {
                    scanJar(new JarFile(file));
                } catch (final IOException e) {
                    throw new InternalException(e);
                }
            }
        } else if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (null != files) {
                for (final File subFile : files) {
                    scanFile(subFile, (null == rootDir) ? subPathBeforePackage(file) : rootDir);
                }
            }
        }
    }

    /**
     * 扫描jar包，扫描结束后关闭jar文件
     *
     * @param jar jar包
     */
    private void scanJar(final JarFile jar) {
        try {
            String name;
            for (final JarEntry entry : new EnumerationIterator<>(jar.entries())) {
                name = StringKit.removePrefix(entry.getName(), Symbol.SLASH);
                if (StringKit.isEmpty(packagePath) || name.startsWith(this.packagePath)) {
                    if (name.endsWith(FileType.CLASS) && !entry.isDirectory()) {
                        final String className = name//
                                .substring(0, name.length() - 6)//
                                .replace(Symbol.C_SLASH, Symbol.C_DOT);//
                        addIfAccept(loadClass(className));
                    }
                }
            }
        } finally {
            IoKit.closeQuietly(jar);
        }
    }

    /**
     * 加载类
     *
     * @param className 类名
     * @return 加载的类
     */
    protected Class<?> loadClass(final String className) {
        ClassLoader loader = this.classLoader;
        if (null == loader) {
            loader = ClassKit.getClassLoader();
            this.classLoader = loader;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, this.initialize, loader);
        } catch (final NoClassDefFoundError | ClassNotFoundException e) {
            // 由于依赖库导致的类无法加载，直接跳过此类
        } catch (final UnsupportedClassVersionError e) {
            // 版本导致的不兼容的类，跳过
        } catch (final Exception e) {
            classesOfLoadError.add(className);
        } catch (final Throwable e) {
            if (!this.ignoreLoadError) {
                throw ExceptionKit.wrapRuntime(e);
            } else {
                classesOfLoadError.add(className);
            }
        }
        return clazz;
    }

    /**
     * 通过过滤器，是否满足接受此类的条件
     *
     * @param className 类名
     */
    private void addIfAccept(final String className) {
        if (StringKit.isBlank(className)) {
            return;
        }
        final int classLen = className.length();
        final int packageLen = this.packageName.length();
        if (classLen == packageLen) {
            // 类名和包名长度一致，用户可能传入的包名是类名
            if (className.equals(this.packageName)) {
                addIfAccept(loadClass(className));
            }
        } else if (classLen > packageLen) {
            // 检查类名是否以指定包名为前缀，包名后加.
            if (".".equals(this.packageNameWithDot) || className.startsWith(this.packageNameWithDot)) {
                addIfAccept(loadClass(className));
            }
        }
    }

    /**
     * 通过过滤器，是否满足接受此类的条件
     *
     * @param clazz 类
     */
    private void addIfAccept(final Class<?> clazz) {
        if (null != clazz) {
            final Predicate<Class<?>> classFilter = this.classPredicate;
            if (classFilter == null || classFilter.test(clazz)) {
                this.classes.add(clazz);
            }
        }
    }

    /**
     * 截取文件绝对路径中包名之前的部分
     *
     * @param file 文件
     * @return 包名之前的部分
     */
    private String subPathBeforePackage(final File file) {
        String filePath = file.getAbsolutePath();
        if (StringKit.isNotEmpty(this.packageDirName)) {
            filePath = StringKit.subBefore(filePath, this.packageDirName, true);
        }
        return StringKit.addSuffixIfNot(filePath, File.separator);
    }

}
