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
package org.miaixz.bus.spring.banner;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiOutput;

/**
 * A text-based {@link Banner} implementation that displays a custom ASCII art banner along with Spring Boot and Bus
 * framework version information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TextBanner extends AbstractBanner {

    /**
     * Constructs a default {@code TextBanner} with no specific resource location.
     */
    public TextBanner() {
        super(null, null, null);
    }

    /**
     * Constructs a {@code TextBanner} with a specified resource class, location, and default banner text.
     *
     * @param resourceClass    The class to use for loading resources.
     * @param resourceLocation The location of the banner resource.
     * @param defaultBanner    The default banner text to use if the resource is not found.
     */
    public TextBanner(Class<?> resourceClass, String resourceLocation, String defaultBanner) {
        super(resourceClass, resourceLocation, defaultBanner);
    }

    /**
     * Generates the banner text, including custom ASCII art and version information.
     * <p>
     * The custom ASCII art is retrieved from {@code GeniusBuilder.BUS_BANNER} and colored green. Spring Boot and Bus
     * framework versions are also included and colored magenta.
     * </p>
     *
     * @param bannerText (Ignored in this implementation, as the banner is generated internally).
     * @return The formatted banner string.
     */
    @Override
    protected String printBanner(String bannerText) {
        StringBuilder builder = new StringBuilder();
        for (String line : GeniusBuilder.BUS_BANNER) {
            builder.append(AnsiOutput.toString(AnsiColor.BRIGHT_GREEN, line)).append("\n");
        }

        String springVersion = GeniusBuilder.SPRING_BOOT_BANNER
                + String.format("(v%s)", SpringBootVersion.getVersion());
        String busVersion = GeniusBuilder.BUS_BOOT_BANNER + String.format("(v%s)", Version.all());
        StringBuilder padding = new StringBuilder();
        while (padding.length() < 70 - (springVersion.length() + busVersion.length())) {
            padding.append(Symbol.SPACE);
        }

        builder.append(
                AnsiOutput.toString(
                        AnsiColor.BRIGHT_MAGENTA,
                        springVersion,
                        padding.toString(),
                        AnsiColor.BRIGHT_MAGENTA,
                        busVersion));
        return builder.toString();
    }

}
