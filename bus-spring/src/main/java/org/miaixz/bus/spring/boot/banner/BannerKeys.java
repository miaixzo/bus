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
package org.miaixz.bus.spring.boot.banner;

/**
 * Constants used exclusively by Bus banner rendering.
 *
 * @author Kimi Liu
 */
public final class BannerKeys {

    /**
     * Default Bus ASCII banner lines.
     */
    public static final String[] BUS_BANNER = {
            " ███╗   ███╗██╗ █████╗ ██╗██╗  ██╗███████╗    ██████╗ ██████╗  ██████╗  ",
            " ████╗ ████║██║██╔══██╗██║╚██╗██╔╝╚══███╔╝   ██╔═══██╗██╔══██╗██╔════╝  ",
            " ██╔████╔██║██║███████║██║ ╚███╔╝   ███╔╝    ██║   ██║██████╔╝██║  ███╗ ",
            " ██║╚██╔╝██║██║██╔══██║██║ ██╔██╗  ███╔╝     ██║   ██║██╔══██╗██║   ██║ ",
            " ██║ ╚═╝ ██║██║██║  ██║██║██╔╝ ██╗███████╗██╗╚██████╔╝██║  ██║╚██████╔╝ ",
            " ╚═╝     ╚═╝╚═╝╚═╝  ╚═╝╚═╝╚═╝  ╚═╝╚══════╝╚═╝ ╚═════╝ ╚═╝  ╚═╝ ╚═════╝  " };

    /**
     * Short Bus Boot banner title.
     */
    public static final String BUS_BOOT_BANNER = " :: Bus Boot :: ";
    /**
     * Short Spring Boot banner title.
     */
    public static final String SPRING_BOOT_BANNER = " :: Spring Boot :: ";
    /**
     * Conventional Spring banner resource name.
     */
    public static final String SPRING_BANNER_TEXT = "banner.txt";
    /**
     * Spring property selecting a banner resource.
     */
    public static final String SPRING_BANNER_LOCATION = "spring.banner.location";
    /**
     * Bus property controlling banner output.
     */
    public static final String BUS_BANNER_ENABLED = "bus.banner.enabled";

    /**
     * Prevents instantiation of this constants holder.
     */
    private BannerKeys() {
        // No initialization required.
    }

}
