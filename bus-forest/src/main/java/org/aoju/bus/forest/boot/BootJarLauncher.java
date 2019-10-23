/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.forest.boot;

import org.aoju.bus.forest.Launcher;
import org.springframework.boot.loader.JarLauncher;

import java.net.URL;

/**
 * Spring-Boot Jar 启动器
 *
 * @author Kimi Liu
 * @version 5.0.8
 * @since JDK 1.8+
 */
public class BootJarLauncher extends JarLauncher {

    private final Launcher launcher;

    public BootJarLauncher(String... args) throws Exception {
        this.launcher = new Launcher(args);
    }

    public static void main(String[] args) throws Exception {
        new BootJarLauncher(args).launch();
    }

    public void launch() throws Exception {
        launch(launcher.args);
    }

    @Override
    protected ClassLoader createClassLoader(URL[] urls) throws Exception {
        return new BootClassLoader(urls, this.getClass().getClassLoader(), launcher.decryptorProvider, launcher.encryptorProvider, launcher.key);
    }

}
