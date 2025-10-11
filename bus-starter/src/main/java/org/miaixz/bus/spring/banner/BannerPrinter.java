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
package org.miaixz.bus.spring.banner;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.ResourceKit;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.SpringBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;

import java.io.BufferedReader;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Handles Spring Boot Banner printing, ensuring a single banner is printed. Prioritizes an existing {@link Banner}
 * bean, then {@code banner.txt}, and finally a default {@link TextBanner}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BannerPrinter {

    /**
     * Prints the application banner.
     * <p>
     * This method disables the default Spring Boot banner printing and checks for an existing {@link Banner} bean, a
     * {@code banner.txt} file, or falls back to a {@link TextBanner}.
     * </p>
     *
     * @param app The {@link SpringApplication} instance.
     * @param env The Spring {@link ConfigurableEnvironment}.
     */
    public void printBanner(SpringApplication app, ConfigurableEnvironment env) {
        // Check for an already configured Banner
        Banner banner = (Banner) FieldKit.getFieldValue(app, "banner");
        if (banner != null) {
            return;
        }

        // Load banner.txt if it exists
        String location = env.getProperty(GeniusBuilder.SPRING_BANNER_LOCATION, GeniusBuilder.SPRING_BANNER_TXT);
        if (FileKit.exists(location)) {
            BufferedReader reader = ResourceKit.getReader(location);
            String text = reader.lines().map(line -> SpringBuilder.replacePlaceholders(line, env))
                    .collect(Collectors.joining("\n"));
            banner = (e, s, p) -> p.print(text);
            app.setBanner(banner);
            return;
        }

        // Fallback to default TextBanner
        banner = new TextBanner();
        app.setBanner(banner);
    }

    /**
     * Replaces environment variable placeholders (e.g., {@code ${spring.application.name}}) in the banner text.
     *
     * @param text The text containing placeholders.
     * @param env  The Spring {@link ConfigurableEnvironment}.
     * @return The text with placeholders replaced by their corresponding environment values.
     */
    private String replacePlaceholders(String text, ConfigurableEnvironment env) {
        Properties props = new Properties();
        env.getPropertySources().forEach(source -> {
            if (source instanceof EnumerablePropertySource eps) {
                for (String name : eps.getPropertyNames()) {
                    props.put(name, String.valueOf(eps.getProperty(name)));
                }
            }
        });
        String result = text;
        for (String key : props.stringPropertyNames()) {
            result = result
                    .replace(Symbol.DOLLAR + Symbol.BRACE_LEFT + key + Symbol.BRACE_RIGHT, props.getProperty(key));
        }
        return result;
    }

}
