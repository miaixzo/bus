/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.lang;

import org.aoju.bus.core.collection.EnumerationIterator;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.toolkit.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描器
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since Java 17+
 */
public class Scaner {

    /**
     * 包名
     */
    private final String packageName;
    /**
     * 包名,最后跟一个点,表示包名,避免在检查前缀时的歧义
     */
    private final String packageNameWithDot;
    /**
     * 包路径,用于文件中对路径操作
     */
    private final String packageDirName;
    /**
     * 包路径,用于jar中对路径操作,在Linux下与packageDirName一致
     */
    private final String packagePath;
    /**
     * 过滤器
     */
    private final Filter<Class<?>> classFilter;
    /**
     * 编码
     */
    private final java.nio.charset.Charset charset;
    private final Set<Class<?>> classes = new HashSet<>();
    /**
     * 是否初始化类
     */
    private boolean initialize;

    /**
     * 构造,默认UTF-8编码
     */
    public Scaner() {
        this(null);
    }

    /**
     * 构造,默认UTF-8编码
     *
     * @param packageName 包名,所有包传入""或者null
     */
    public Scaner(String packageName) {
        this(packageName, null);
    }

    /**
     * 构造,默认UTF-8编码
     *
     * @param packageName 包名,所有包传入""或者null
     * @param classFilter 过滤器,无需传入null
     */
    public Scaner(String packageName, Filter<Class<?>> classFilter) {
        this(packageName, classFilter, Charset.UTF_8);
    }

    /**
     * 构造
     *
     * @param packageName 包名,所有包传入""或者null
     * @param classFilter 过滤器,无需传入null
     * @param charset     编码
     */
    public Scaner(String packageName, Filter<Class<?>> classFilter, java.nio.charset.Charset charset) {
        packageName = StringKit.nullToEmpty(packageName);
        this.packageName = packageName;
        this.packageNameWithDot = StringKit.addSuffixIfNot(packageName, Symbol.DOT);
        this.packageDirName = packageName.replace(Symbol.C_DOT, File.separatorChar);
        this.packagePath = packageName.replace(Symbol.C_DOT, Symbol.C_SLASH);
        this.classFilter = classFilter;
        this.charset = charset;
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
     * 扫描包路径下和所有在classpath中加载的类，满足class过滤器条件的所有class文件，<br>
     * 如果包路径为 com.abs + A.class 但是输入 abs会产生classNotFoundException<br>
     * 因为className 应该为 com.abs.A 现在却成为abs.A,此工具类对该异常进行忽略处理<br>
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @param classFilter class过滤器，过滤掉不需要的class
     * @return 类集合
     */
    public static Set<Class<?>> scanAllPackage(String packageName, Filter<Class<?>> classFilter) {
        return new Scaner(packageName, classFilter).scan(true);
    }

    /**
     * 扫描该包路径下所有class文件
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
    public static Set<Class<?>> scanPackage(String packageName) {
        return scanPackage(packageName, null);
    }

    /**
     * 扫面包路径下满足class过滤器条件的所有class文件,
     * 如果包路径为 com.abs + A.class 但是输入 abs会产生classNotFoundException
     * 因为className 应该为 com.abs.A 现在却成为abs.A,此工具类对该异常进行忽略处理
     *
     * @param packageName 包路径 com | com. | com.abs | com.abs.
     * @param classFilter class过滤器,过滤掉不需要的class
     * @return 类集合
     */
    public static Set<Class<?>> scanPackage(String packageName, Filter<Class<?>> classFilter) {
        return new Scaner(packageName, classFilter).scan();
    }

    /**
     * 扫描指定包路径下所有包含指定注解的类，包括其他加载的jar或者类
     *
     * @param packageName     包路径
     * @param annotationClass 注解类
     * @return 类集合
     */
    public static Set<Class<?>> scanAllPackageByAnnotation(String packageName, Class<? extends Annotation> annotationClass) {
        return scanAllPackage(packageName, clazz -> clazz.isAnnotationPresent(annotationClass));
    }

    /**
     * 扫描指定包路径下所有包含指定注解的类
     *
     * @param packageName     包路径
     * @param annotationClass 注解类
     * @return 类集合
     */
    public static Set<Class<?>> scanPackageByAnnotation(String packageName, final Class<? extends Annotation> annotationClass) {
        return scanPackage(packageName, clazz -> clazz.isAnnotationPresent(annotationClass));
    }

    /**
     * 扫描指定包路径下所有指定类或接口的子类或实现类，不包括指定父类本身，包括其他加载的jar或者类
     *
     * @param packageName 包路径
     * @param superClass  父类或接口（不包括）
     * @return 类集合
     */
    public static Set<Class<?>> scanAllPackageBySuper(String packageName, Class<?> superClass) {
        return scanAllPackage(packageName, clazz -> superClass.isAssignableFrom(clazz) && !superClass.equals(clazz));
    }

    /**
     * 扫描指定包路径下所有指定类或接口的子类或实现类
     *
     * @param packageName 包路径
     * @param superClass  父类或接口
     * @return 类集合
     */
    public static Set<Class<?>> scanPackageBySuper(String packageName, final Class<?> superClass) {
        return scanPackage(packageName, clazz -> superClass.isAssignableFrom(clazz) && !superClass.equals(clazz));
    }

    /**
     * 扫描包路径下满足class过滤器条件的所有class文件
     * 此方法首先扫描指定包名下的资源目录，如果未扫描到，则扫描整个classpath中所有加载的类
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
    public Set<Class<?>> scan(boolean forceScanJavaClassPaths) {
        for (URL url : FileKit.getResourceIter(this.packagePath)) {
            switch (url.getProtocol()) {
                case "file":
                    scanFile(new File(UriKit.decode(url.getFile(), this.charset.name())), null);
                    break;
                case "jar":
                    scanJar(UriKit.getJarFile(url));
                    break;
            }
        }

        if (forceScanJavaClassPaths || CollKit.isEmpty(this.classes)) {
            scanJavaClassPaths();
        }

        return Collections.unmodifiableSet(this.classes);
    }

    /**
     * 扫描Java指定的ClassPath路径
     *
     * @return 扫描到的类
     */
    private void scanJavaClassPaths() {
        final String[] javaClassPaths = ClassKit.getJavaClassPaths();
        for (String classPath : javaClassPaths) {
            // bug修复,由于路径中空格和中文导致的Jar找不到
            classPath = UriKit.decode(classPath, Charset.systemCharsetName());

            scanFile(new File(classPath), null);
        }
    }

    /**
     * 扫描文件或目录中的类
     *
     * @param file    文件或目录
     * @param rootDir 包名对应classpath绝对路径
     */
    private void scanFile(File file, String rootDir) {
        if (file.isFile()) {
            final String fileName = file.getAbsolutePath();
            if (fileName.endsWith(FileType.CLASS)) {
                final String className = fileName
                        // 8为classes长度,fileName.length() - 6为".class"的长度
                        .substring(rootDir.length(), fileName.length() - 6)
                        .replace(File.separatorChar, Symbol.C_DOT);
                //加入满足条件的类
                addIfAccept(className);
            } else if (fileName.endsWith(FileType.JAR)) {
                try {
                    scanJar(new JarFile(file));
                } catch (IOException e) {
                    throw new InstrumentException(e);
                }
            }
        } else if (file.isDirectory()) {
            final File[] files = file.listFiles();
            if (null != files) {
                for (File subFile : files) {
                    scanFile(subFile, (null == rootDir) ? subPathBeforePackage(file) : rootDir);
                }
            }
        }
    }

    /**
     * 扫描jar包
     *
     * @param jar jar包
     */
    private void scanJar(JarFile jar) {
        String name;
        for (JarEntry entry : new EnumerationIterator<>(jar.entries())) {
            name = StringKit.removePrefix(entry.getName(), Symbol.SLASH);
            if (StringKit.isEmpty(packagePath) || name.startsWith(this.packagePath)) {
                if (name.endsWith(FileType.CLASS) && false == entry.isDirectory()) {
                    final String className = name
                            .substring(0, name.length() - 6)
                            .replace(Symbol.C_SLASH, Symbol.C_DOT);
                    addIfAccept(loadClass(className));
                }
            }
        }
    }

    /**
     * 设置是否在扫描到类时初始化类
     *
     * @param initialize 是否初始化类
     */
    public void setInitialize(boolean initialize) {
        this.initialize = initialize;
    }

    /**
     * 加载类
     *
     * @param className 类名
     * @return 加载的类
     */
    private Class<?> loadClass(String className) {
        Class<?> clazz = null;
        try {
            clazz = Class.forName(className, this.initialize, ClassKit.getClassLoader());
        } catch (NoClassDefFoundError e) {
            // 由于依赖库导致的类无法加载,直接跳过此类
        } catch (UnsupportedClassVersionError | ClassNotFoundException ee) {
            // 版本导致的不兼容的类,跳过
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return clazz;
    }

    /**
     * 通过过滤器,是否满足接受此类的条件
     *
     * @param className 类
     * @return 是否接受
     */
    private void addIfAccept(String className) {
        if (StringKit.isBlank(className)) {
            return;
        }
        int classLen = className.length();
        int packageLen = this.packageName.length();
        if (classLen == packageLen) {
            //类名和包名长度一致,用户可能传入的包名是类名
            if (className.equals(this.packageName)) {
                addIfAccept(loadClass(className));
            }
        } else if (classLen > packageLen) {
            //检查类名是否以指定包名为前缀,包名后加.
            if (className.startsWith(this.packageNameWithDot)) {
                addIfAccept(loadClass(className));
            }
        }
    }

    /**
     * 通过过滤器,是否满足接受此类的条件
     *
     * @param clazz 类
     * @return 是否接受
     */
    private void addIfAccept(Class<?> clazz) {
        if (null != clazz) {
            Filter<Class<?>> classFilter = this.classFilter;
            if (null == classFilter || classFilter.accept(clazz)) {
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
    private String subPathBeforePackage(File file) {
        String filePath = file.getAbsolutePath();
        if (StringKit.isNotEmpty(this.packageDirName)) {
            filePath = StringKit.subBefore(filePath, this.packageDirName, true);
        }
        return StringKit.addSuffixIfNot(filePath, File.separator);
    }

}
