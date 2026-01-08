/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import java.io.InputStream;
import java.io.PrintStream;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

/**
 * Abstract base class for {@link Banner} implementations.
 * <p>
 * This class provides common functionality for loading banner text from resources and handling ANSI escape codes.
 * Subclasses must implement the {@link #printBanner(String)} method to define how the banner text is processed.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractBanner implements Banner {

    /**
     * The resource class used to load the banner resource.
     */
    protected Class<?> resourceClass;
    /**
     * The location of the banner resource (e.g., "/banner.txt").
     */
    protected String resourceLocation;
    /**
     * The default banner text to use if the resource cannot be loaded.
     */
    protected String defaultBanner;
    /**
     * The final banner text, after loading and processing.
     */
    protected String banner;

    /**
     * Constructs an {@code AbstractBanner} with the specified resource class, location, and default banner text.
     *
     * @param resourceClass    The class to use for loading resources.
     * @param resourceLocation The location of the banner resource.
     * @param defaultBanner    The default banner text.
     */
    public AbstractBanner(Class<?> resourceClass, String resourceLocation, String defaultBanner) {
        this.resourceClass = resourceClass;
        this.resourceLocation = resourceLocation;
        this.defaultBanner = defaultBanner;
    }

    /**
     * Initializes the banner by attempting to load it from the specified resource location. If loading fails, it falls
     * back to the default banner text.
     */
    protected void initialize() {
        InputStream inputStream = null;
        String bannerText = null;
        try {
            if (null != resourceLocation) {
                inputStream = resourceClass.getResourceAsStream(resourceLocation);
                if (inputStream != null) {
                    bannerText = IoKit.readUtf8(IoKit.toBuffered(inputStream));
                }
            }
        } catch (Exception e) {
            // Log the exception if necessary, but don't rethrow to allow startup to continue
        } finally {
            banner = printBanner(bannerText);

            if (null != inputStream) {
                IoKit.close(inputStream);
            }
        }
    }

    /**
     * Returns the processed banner text.
     *
     * @return The banner text, potentially including ANSI escape codes.
     */
    public String getBanner() {
        return banner;
    }

    /**
     * Returns the banner text with ANSI escape codes removed, resulting in plain text.
     *
     * @return The plain text banner.
     */
    public String getPlainBanner() {
        if (null != banner) {
            // Regular expression to remove ANSI escape codes
            banner = banner.replaceAll("\\u001b\\[[;\\d]*m", Normal.EMPTY);
        }
        return banner;
    }

    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.println();
        out.println(printBanner(null));
        out.println();
    }

    /**
     * Abstract method to be implemented by subclasses to define how the banner text is processed.
     *
     * @param bannerText The loaded banner text, or {@code null} if not loaded from a resource.
     * @return The final banner string to be displayed.
     */
    protected abstract String printBanner(String bannerText);

}
