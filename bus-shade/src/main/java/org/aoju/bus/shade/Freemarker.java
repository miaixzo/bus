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
package org.aoju.bus.shade;

import freemarker.template.Configuration;
import freemarker.template.Template;
import org.aoju.bus.core.consts.Normal;
import org.aoju.bus.shade.entity.TableEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 创建文件
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class Freemarker {

    public static Object createFile(TableEntity dataModel, String templateName, String filePath) {

        FileWriter out = null;
        try {
            // 通过FreeMarker的Confuguration读取相应的模板文件
            Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
            // 设置模板路径
            configuration.setClassForTemplateLoading(Freemarker.class, "/" + Normal.META_DATA_INF + "/template");
            // 设置默认字体
            configuration.setDefaultEncoding("utf-8");
            // 获取模板
            Template template = configuration.getTemplate(templateName);
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            } else {
                return "The file already exists:" + filePath;
            }

            //设置输出流
            out = new FileWriter(file);
            //模板输出静态文件
            template.process(dataModel, out);
            return "create a file:" + filePath;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "failed to create file:" + filePath;
    }

}
