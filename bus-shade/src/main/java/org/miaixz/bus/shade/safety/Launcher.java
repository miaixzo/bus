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
package org.miaixz.bus.shade.safety;

import java.io.*;
import java.net.URI;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.shade.safety.algorithm.Key;
import org.miaixz.bus.shade.safety.provider.DecryptorProvider;
import org.miaixz.bus.shade.safety.provider.EncryptorProvider;
import org.miaixz.bus.shade.safety.provider.JdkDecryptorProvider;
import org.miaixz.bus.shade.safety.provider.JdkEncryptorProvider;

/**
 * Launcher for Spring-Boot applications with enhanced security features, including JAR encryption and decryption. This
 * class handles the initialization of encryption/decryption providers and key management based on command-line
 * arguments and manifest attributes.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Launcher {

    /**
     * Command-line arguments passed to the application.
     */
    public final String[] args;
    /**
     * Provider for decryption operations.
     */
    public final DecryptorProvider decryptorProvider;
    /**
     * Provider for encryption operations.
     */
    public final EncryptorProvider encryptorProvider;
    /**
     * The encryption/decryption key used by the providers.
     */
    public final Key key;

    /**
     * Constructs a new {@code Launcher} instance, initializing encryption/decryption parameters. It parses command-line
     * arguments, manifest attributes, and an optional key file to determine the encryption algorithm, key size, IV
     * size, and password.
     *
     * @param args Command-line arguments for the application.
     * @throws Exception If an error occurs during initialization, such as file not found or algorithm not available.
     */
    public Launcher(String... args) throws Exception {
        this.args = args;
        String algorithm = Builder.ALGORITHM;
        int keysize = Builder.DEFAULT_KEYSIZE;
        int ivsize = Builder.DEFAULT_IVSIZE;
        String password = null;
        String keypath = null;
        for (String arg : args) {
            if (arg.toLowerCase().startsWith(Builder.XJAR_ALGORITHM)) {
                algorithm = arg.substring(Builder.XJAR_ALGORITHM.length());
            }
            if (arg.toLowerCase().startsWith(Builder.XJAR_KEYSIZE)) {
                keysize = Integer.valueOf(arg.substring(Builder.XJAR_KEYSIZE.length()));
            }
            if (arg.toLowerCase().startsWith(Builder.XJAR_IVSIZE)) {
                ivsize = Integer.valueOf(arg.substring(Builder.XJAR_IVSIZE.length()));
            }
            if (arg.toLowerCase().startsWith(Builder.XJAR_PASSWORD)) {
                password = arg.substring(Builder.XJAR_PASSWORD.length());
            }
            if (arg.toLowerCase().startsWith(Builder.XJAR_KEYFILE)) {
                keypath = arg.substring(Builder.XJAR_KEYFILE.length());
            }
        }

        ProtectionDomain domain = this.getClass().getProtectionDomain();
        CodeSource source = domain.getCodeSource();
        URI location = (null == source ? null : source.getLocation().toURI());
        String filepath = (null == location ? null : location.getSchemeSpecificPart());
        if (null != filepath) {
            File file = new File(filepath);
            JarFile jar = new JarFile(file, false);
            Manifest manifest = jar.getManifest();
            Attributes attributes = manifest.getMainAttributes();
            if (null != attributes.getValue(Builder.XJAR_ALGORITHM_KEY)) {
                algorithm = attributes.getValue(Builder.XJAR_ALGORITHM_KEY);
            }
            if (null != attributes.getValue(Builder.XJAR_KEYSIZE_KEY)) {
                keysize = Integer.valueOf(attributes.getValue(Builder.XJAR_KEYSIZE_KEY));
            }
            if (null != attributes.getValue(Builder.XJAR_IVSIZE_KEY)) {
                ivsize = Integer.valueOf(attributes.getValue(Builder.XJAR_IVSIZE_KEY));
            }
            if (null != attributes.getValue(Builder.XJAR_PASSWORD_KEY)) {
                password = attributes.getValue(Builder.XJAR_PASSWORD_KEY);
            }
        }

        Properties key = null;
        File keyfile = null;
        if (null != keypath) {
            String path = Builder.absolutize(keypath);
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                keyfile = file;
                try (InputStream in = new FileInputStream(file)) {
                    key = new Properties();
                    key.load(in);
                }
            } else {
                throw new FileNotFoundException("could not find key file at path: " + file.getCanonicalPath());
            }
        } else {
            String path = Builder.absolutize("xjar.key");
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                keyfile = file;
                try (InputStream in = new FileInputStream(file)) {
                    key = new Properties();
                    key.load(in);
                }
            }
        }

        String hold = null;
        if (null != key) {
            Set<String> names = key.stringPropertyNames();
            for (String name : names) {
                switch (name.toLowerCase()) {
                    case Builder.XJAR_KEY_ALGORITHM:
                        algorithm = key.getProperty(name);
                        break;

                    case Builder.XJAR_KEY_KEYSIZE:
                        keysize = Integer.valueOf(key.getProperty(name));
                        break;

                    case Builder.XJAR_KEY_IVSIZE:
                        ivsize = Integer.valueOf(key.getProperty(name));
                        break;

                    case Builder.XJAR_KEY_PASSWORD:
                        password = key.getProperty(name);
                        break;

                    case Builder.XJAR_KEY_HOLD:
                        hold = key.getProperty(name);
                    default:
                        break;
                }
            }
        }

        if (null == hold || !Arrays.asList("true", Symbol.ONE, "yes", "y").contains(hold.trim().toLowerCase())) {
            if (null != keyfile && keyfile.exists() && !keyfile.delete() && keyfile.exists()) {
                throw new IOException("could not delete key file : " + keyfile.getCanonicalPath());
            }
        }

        if (null == password && null != System.console()) {
            Console console = System.console();
            char[] chars = console.readPassword("password:");
            password = new String(chars);
        }
        if (null == password) {
            Scanner scanner = new Scanner(System.in);
            password = scanner.nextLine();
        }
        this.decryptorProvider = new JdkDecryptorProvider(algorithm);
        this.encryptorProvider = new JdkEncryptorProvider(algorithm);
        this.key = Builder.key(algorithm, keysize, ivsize, password);
    }

}
