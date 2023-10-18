/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.shade.safety.boot;

import org.aoju.bus.shade.safety.Launcher;
import org.springframework.boot.loader.WarLauncher;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Spring-Boot Jar 启动器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BootWarLauncher extends WarLauncher {

    private final Launcher launcher;

    public BootWarLauncher(String... args) throws Exception {
        this.launcher = new Launcher(args);
    }

    public static void main(String[] args) throws Exception {
        new BootWarLauncher(args).launch();
    }

    public void launch() throws Exception {
        launch(launcher.args);
    }

    @Override
    protected void launch(String[] args, String launchClass, ClassLoader classLoader) throws Exception {
        URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
        URL[] urls = urlClassLoader.getURLs();
        ClassLoader cl = new BootClassLoader(urls, this.getClass().getClassLoader(), launcher.decryptorProvider, launcher.encryptorProvider, launcher.key);
        Thread.currentThread().setContextClassLoader(cl);
        createMainMethodRunner(launchClass, args, classLoader).run();
    }

}
