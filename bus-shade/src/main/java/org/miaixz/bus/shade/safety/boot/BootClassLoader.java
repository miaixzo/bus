/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.safety.boot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.springframework.boot.loader.launch.LaunchedClassLoader;

import org.miaixz.bus.errors.ComponentFailure;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.shade.safety.Builder;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;

/**
 * A custom {@link LaunchedClassLoader} that supports decryption of resources within a Spring Boot JAR.
 * <p>
 * Spring Boot 3.2+ reads nested jars directly from the outer archive instead of going through the {@code jar:} URL
 * handler, and class path scanning walks those nested directory URLs, so decrypting URL connections on the fly is not
 * enough. This loader therefore materializes a plaintext view of the encrypted archive into a temporary directory:
 * {@code BOOT-INF/classes/**} entries are decrypted into a classes directory and encrypted nested jars are decrypted
 * into standalone jar files. Resources are served as ordinary {@code file:} / {@code jar:file:} URLs, which keeps
 * Spring's class path scanning working.
 *
 * @author Kimi Liu
 */
public class BootClassLoader extends LaunchedClassLoader {

    /**
     * Reports archive definition failures while preserving ClassNotFoundException and loader fallback.
     */
    private static final ComponentFailure FAILURES = new ComponentFailure("bus-shade", "archive-loader");

    /**
     * Prefix of the temporary directory holding the plaintext view.
     */
    private static final String TEMP_PREFIX = "bus-shade-";

    static {
        // Registers this class loader as parallel-capable, allowing concurrent loading of classes.
        ClassLoader.registerAsParallelCapable();
    }

    /**
     * The decryptor used for encrypted entries.
     */
    private final DecryptorProvider decryptorProvider;

    /**
     * The cryptographic key used for decryption.
     */
    private final Key key;

    /**
     * The outer archive, or {@code null} when running from an exploded directory.
     */
    private final File archive;

    /**
     * Root of the materialized plaintext view.
     */
    private final File root;

    /**
     * Directory holding decrypted {@code BOOT-INF/classes} entries.
     */
    private final File classes;

    /**
     * Directory holding decrypted nested jars.
     */
    private final File libraries;

    /**
     * Opened plaintext nested jars, keyed by entry name.
     */
    private final Map<String, JarFile> opened = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@code BootClassLoader}.
     *
     * @param urls              The URLs from which to load classes and resources.
     * @param parent            The parent class loader.
     * @param decryptorProvider The provider responsible for decrypting resources.
     * @param encryptorProvider The provider responsible for encrypting resources (retained for API compatibility).
     * @param key               The cryptographic key used for decryption.
     * @throws Exception If an error occurs during initialization.
     */
    public BootClassLoader(URL[] urls, ClassLoader parent, DecryptorProvider decryptorProvider,
            EncryptorProvider encryptorProvider, Key key) throws Exception {
        super(true, urls, parent);
        this.decryptorProvider = decryptorProvider;
        this.key = key;
        this.archive = locateArchive();
        this.root = null == this.archive ? null : Files.createTempDirectory(TEMP_PREFIX).toFile();
        this.classes = null == this.root ? null : new File(this.root, "classes");
        this.libraries = null == this.root ? null : new File(this.root, "lib");
        if (null != this.root) {
            this.classes.mkdirs();
            this.libraries.mkdirs();
            this.root.deleteOnExit();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> delete(this.root), "bus-shade-cleanup"));
            materialize();
        }
        Logger.debug(
                false,
                "Shade",
                "Boot class loader initialized: archive={}, libraries={}",
                null == this.archive ? "exploded" : this.archive.getName(),
                null == this.libraries ? 0 : this.libraries.list().length);
    }


    /**
     * Recursively deletes a file or directory, ignoring failures during shutdown.
     *
     * @param file file or directory to delete
     */
    private static void delete(final File file) {
        try {
            File[] children = file.listFiles();
            if (null != children) {
                for (File child : children) {
                    delete(child);
                }
            }
            if (!file.delete() && file.exists()) {
                Logger.debug(false, "Shade", "Boot plaintext cleanup skipped: path={}", file.getName());
            }
        } catch (RuntimeException e) {
            Logger.debug(false, "Shade", "Boot plaintext cleanup failed: path={}", file.getName());
        }
    }

    /**
     * Locates the executable archive that contains this class loader.
     *
     * @return the archive file, or {@code null} when running from an exploded directory
     */
    private static File locateArchive() {
        try {
            var source = BootClassLoader.class.getProtectionDomain().getCodeSource();
            if (null == source) {
                return null;
            }
            File file = new File(source.getLocation().toURI());
            return file.isFile() ? file : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decrypts the encrypted archive into the temporary plaintext view.
     */
    private void materialize() {
        try (ZipFile zip = new ZipFile(this.archive)) {
            Set<String> encrypted = readIndex(zip);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (entry.isDirectory()) {
                    continue;
                }
                if (name.startsWith(Builder.BOOT_INF_CLASSES)) {
                    String relative = name.substring(Builder.BOOT_INF_CLASSES.length());
                    if (relative.isEmpty()) {
                        continue;
                    }
                    byte[] bytes = read(zip, entry, encrypted.contains(name));
                    if (null != bytes) {
                        write(new File(this.classes, relative), bytes);
                    }
                } else if (name.startsWith(Builder.BOOT_INF_LIB) && name.endsWith(".jar")) {
                    byte[] raw = read(zip, entry, false);
                    if (null == raw) {
                        continue;
                    }
                    byte[] bytes = encrypted.contains(name) ? decryptLibrary(raw) : raw;
                    write(new File(this.libraries, name.substring(Builder.BOOT_INF_LIB.length())), bytes);
                }
            }
        } catch (IOException e) {
            FAILURES.failed(org.miaixz.bus.errors.ErrorType.INVALID_DATA, e);
            Logger.warn(false, "Shade", e, "Boot archive materialization failed");
        }
    }

    /**
     * Reads the encryptor index describing which entries are encrypted.
     *
     * @param zip the outer archive
     * @return the encrypted entry names
     */
    private static Set<String> readIndex(final ZipFile zip) {
        Set<String> names = new LinkedHashSet<>();
        ZipEntry index = zip.getEntry(Builder.BOOT_INF_CLASSES + Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX);
        if (null == index) {
            return names;
        }
        try (InputStream in = zip.getInputStream(index)) {
            for (String line : new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).split("\\R")) {
                String name = line.trim();
                if (name.isEmpty()) {
                    continue;
                }
                // Class and resource entries are recorded relative to BOOT-INF/classes, while nested jars keep
                // their full archive path.
                if (!name.startsWith(Builder.BOOT_INF_CLASSES) && !name.startsWith(Builder.BOOT_INF_LIB)
                        && !name.startsWith(Builder.XJAR_SRC_DIR)) {
                    name = Builder.BOOT_INF_CLASSES + name;
                }
                names.add(name);
            }
        } catch (IOException e) {
            Logger.warn(false, "Shade", e, "Boot encryptor index read failed");
        }
        return names;
    }

    /**
     * Reads an entry from the encrypted archive, decrypting it when required.
     *
     * @param zip      the outer archive
     * @param entry    the entry to read
     * @param decrypt  whether the entry content is encrypted
     * @return the entry bytes, or {@code null} when the entry cannot be read
     */
    private byte[] read(final ZipFile zip, final ZipEntry entry, final boolean decrypt) {
        try (InputStream in = zip.getInputStream(entry)) {
            byte[] bytes = in.readAllBytes();
            if (!decrypt) {
                return bytes;
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream(bytes.length);
            this.decryptorProvider.decrypt(this.key, new ByteArrayInputStream(bytes), out);
            return out.toByteArray();
        } catch (IOException e) {
            Logger.warn(false, "Shade", e, "Boot archive entry read failed: entry={}", entry.getName());
            return null;
        }
    }

    /**
     * Decrypts an encrypted nested jar entry by entry using only the JDK zip API.
     *
     * @param raw the encrypted nested jar bytes
     * @return the plaintext nested jar bytes, or {@code null} when decryption fails
     */
    private byte[] decryptLibrary(final byte[] raw) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length);
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(raw));
                java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(out)) {
            ZipEntry entry;
            while (null != (entry = zis.getNextEntry())) {
                String name = entry.getName();
                if (name.endsWith(Builder.XJAR_INF_DIR + Builder.XJAR_INF_IDX)) {
                    continue;
                }
                JarEntry target = new JarEntry(name);
                target.setTime(entry.getTime());
                jos.putNextEntry(target);
                if (entry.isDirectory()) {
                    jos.closeEntry();
                    continue;
                }
                byte[] data = zis.readAllBytes();
                if (name.equals(Builder.META_INF_MANIFEST) || name.startsWith(Builder.XJAR_SRC_DIR)) {
                    jos.write(data);
                } else {
                    // The cipher stream closes its delegate, so decrypt from an isolated entry buffer.
                    this.decryptorProvider.decrypt(this.key, new ByteArrayInputStream(data), jos);
                }
                jos.closeEntry();
            }
        } catch (IOException e) {
            FAILURES.failed(org.miaixz.bus.errors.ErrorType.INVALID_DATA, e);
            Logger.warn(false, "Shade", e, "Boot library decrypt failed");
            return null;
        }
        return out.toByteArray();
    }

    /**
     * Writes the given bytes to a file, creating parent directories as needed.
     *
     * @param file  target file
     * @param bytes content
     */
    private static void write(final File file, final byte[] bytes) {
        File parent = file.getParentFile();
        if (null != parent && !parent.exists() && !parent.mkdirs() && !parent.exists()) {
            Logger.warn(false, "Shade", "Boot plaintext directory creation failed: path={}", parent.getName());
            return;
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(bytes);
        } catch (IOException e) {
            Logger.warn(false, "Shade", e, "Boot plaintext entry write failed: path={}", file.getName());
        }
    }

    /**
     * Finds the class with the specified binary name, optionally decrypting it from the plaintext view.
     *
     * @param name The binary name of the class.
     * @return The resulting {@link Class} object.
     * @throws ClassNotFoundException If the class could not be found or loaded.
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassFormatError | ClassNotFoundException e) {
            byte[] bytes = classBytes(name);
            if (null == bytes) {
                throw new ClassNotFoundException(name, e);
            }
            return defineClass(name, bytes, 0, bytes.length);
        }
    }

    /**
     * Finds a resource, preferring the plaintext view over the encrypted archive.
     *
     * @param name resource name
     * @return the resource URL, or {@code null} when not present
     */
    @Override
    public URL findResource(String name) {
        URL url = plainResource(name);
        if (null != url) {
            return url;
        }
        return super.findResource(name);
    }

    /**
     * Enumerates resources, preferring the plaintext view over the encrypted archive.
     *
     * @param name resource name
     * @return the resource URLs
     * @throws IOException if the enumeration fails
     */
    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        List<URL> urls = new ArrayList<>(plainResources(name));
        Enumeration<URL> base = super.findResources(name);
        while (base.hasMoreElements()) {
            URL next = base.nextElement();
            if (!isArchiveUrl(next)) {
                urls.add(next);
            }
        }
        return Collections.enumeration(urls);
    }

    /**
     * Resolves a resource from the plaintext view.
     *
     * @param name resource name
     * @return the resource URL, or {@code null} when not present
     */
    private URL plainResource(final String name) {
        List<URL> urls = plainResources(name);
        return urls.isEmpty() ? null : urls.get(0);
    }

    /**
     * Resolves all matching resources from the plaintext view.
     *
     * @param name resource name
     * @return the resource URLs
     */
    private List<URL> plainResources(final String name) {
        List<URL> urls = new ArrayList<>();
        if (null == this.classes) {
            return urls;
        }
        File file = new File(this.classes, name);
        if (file.exists()) {
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                Logger.warn(false, "Shade", e, "Boot plaintext resource URL failed: resource={}", name);
            }
        }
        for (JarFile jar : libraryJars()) {
            if (null == jar.getJarEntry(name)) {
                continue;
            }
            try {
                urls.add(new URL("jar:" + jarFileUrl(jar) + "!/" + name));
            } catch (MalformedURLException e) {
                Logger.warn(false, "Shade", e, "Boot library resource URL failed: resource={}", name);
            }
        }
        return urls;
    }

    /**
     * Returns a file URL for a plaintext nested jar.
     *
     * @param jar opened jar
     * @return the jar file URL
     * @throws MalformedURLException if the URL cannot be created
     */
    private URL jarFileUrl(final JarFile jar) throws MalformedURLException {
        return new File(jar.getName()).toURI().toURL();
    }

    /**
     * Resolves a class file from the plaintext view.
     *
     * @param name binary class name
     * @return the class bytes, or {@code null} when not present
     */
    private byte[] classBytes(final String name) {
        String path = name.replace('.', '/') + ".class";
        if (null != this.classes) {
            File file = new File(this.classes, path);
            if (file.isFile()) {
                try {
                    return Files.readAllBytes(file.toPath());
                } catch (IOException e) {
                    Logger.warn(false, "Shade", e, "Boot plaintext class read failed: className={}", name);
                }
            }
        }
        for (JarFile jar : libraryJars()) {
            JarEntry entry = jar.getJarEntry(path);
            if (null == entry) {
                continue;
            }
            try (InputStream in = jar.getInputStream(entry)) {
                return in.readAllBytes();
            } catch (IOException e) {
                Logger.warn(false, "Shade", e, "Boot library class read failed: className={}", name);
            }
        }
        return null;
    }

    /**
     * Returns the opened plaintext nested jars.
     *
     * @return the opened jars
     */
    private List<JarFile> libraryJars() {
        if (null == this.libraries) {
            return List.of();
        }
        List<JarFile> jars = new ArrayList<>();
        File[] files = this.libraries.listFiles((dir, name) -> name.endsWith(".jar"));
        if (null == files) {
            return jars;
        }
        for (File file : files) {
            JarFile jar = this.opened.computeIfAbsent(file.getName(), name -> {
                try {
                    return new JarFile(file);
                } catch (IOException e) {
                    Logger.warn(false, "Shade", e, "Boot plaintext library open failed: library={}", name);
                    return null;
                }
            });
            if (null != jar) {
                jars.add(jar);
            }
        }
        return jars;
    }

    /**
     * Indicates whether a URL points into the encrypted archive.
     *
     * @param url resource URL
     * @return {@code true} when the URL belongs to the encrypted archive
     */
    private boolean isArchiveUrl(final URL url) {
        return null != this.archive && url.toString().contains(this.archive.getName());
    }

}
