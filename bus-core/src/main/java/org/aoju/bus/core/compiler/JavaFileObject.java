package org.aoju.bus.core.compiler;

import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.core.toolkit.ZipKit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * {@link javax.tools.JavaFileObject} 相关工具类封装
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class JavaFileObject {

    /**
     * 获取指定文件下的所有待编译的java文件，并以{@link javax.tools.JavaFileObject}形式返回
     *
     * @param file 文件或目录，文件支持.java、.jar和.zip文件
     * @return 所有待编译的 {@link javax.tools.JavaFileObject}
     */
    public static List<javax.tools.JavaFileObject> getJavaFileObjects(File file) {
        final List<javax.tools.JavaFileObject> result = new ArrayList<>();
        final String fileName = file.getName();

        if (isJavaFile(fileName)) {
            result.add(new JavaSourceFileObject(file.toURI()));
        } else if (isJarOrZipFile(fileName)) {
            result.addAll(getJavaFileObjectByZipOrJarFile(file));
        }
        return result;
    }

    /**
     * 是否是jar 或 zip 文件
     *
     * @param fileName 文件名
     * @return 是否是jar 或 zip 文件
     */
    public static boolean isJarOrZipFile(String fileName) {
        return FileKit.isType(fileName, "jar", "zip");
    }

    /**
     * 是否是java文件
     *
     * @param fileName 文件名
     * @return 是否是.java文件
     */
    public static boolean isJavaFile(String fileName) {
        return FileKit.isType(fileName, "java");
    }

    /**
     * 通过zip包或jar包创建Java文件对象
     *
     * @param file 压缩文件
     * @return Java文件对象
     */
    private static List<javax.tools.JavaFileObject> getJavaFileObjectByZipOrJarFile(File file) {
        final List<javax.tools.JavaFileObject> collection = new ArrayList<>();
        final ZipFile zipFile = ZipKit.zipFile(file, null);
        ZipKit.get(zipFile, (zipEntry) -> {
            final String name = zipEntry.getName();
            if (isJavaFile(name)) {
                collection.add(new JavaSourceFileObject(name, ZipKit.get(zipFile, zipEntry)));
            }
        });
        return collection;
    }

}
