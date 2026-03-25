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
package org.miaixz.bus.core.center.date.culture.festival;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In-memory festival registry for storing and retrieving festival data.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FestivalRegistry {

    /**
     * Valid encoding characters.
     */
    public static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTU_VWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Full festival data string. Format: @[1] rule type[1] content[3] day offset(-31 to 31)[1] start year[3] name[n]
     * <h3>Content</h3>
     * <ul>
     * <li>0.SOLAR_DAY: month(1-12, &gt;12 rolls to next Jan)[1] day(1-31)[1] delay days(-31 to 31)[1]</li>
     * <li>1.SOLAR_WEEK: month(1-12, &gt;12 rolls to next Jan)[1] occurrence(-6 to -1, 1 to 6)[1] weekday(0-6)[1]</li>
     * <li>2.LUNAR_DAY: month(-12 to -1, 1-12, &gt;12 rolls to next Jan)[1] day(1-30)[1] delay days(-31 to 31)[1]</li>
     * <li>3.TERM_DAY: term index(0-23)[1] reserved[1] day offset(-31 to 31)[1]</li>
     * <li>4.TERM_HS: term index(0-23)[1] heaven stem index(0-9)[1] day offset(-31 to 31)[1]</li>
     * <li>5.TERM_EB: term index(0-23)[1] earth branch index(0-11)[1] day offset(-31 to 31)[1]</li>
     * </ul>
     */
    public static String DATA = "";

    /**
     * Regex pattern for matching festival data entries.
     */
    public static final String REGEX = "(@[0-9A-Za-z_]{8})(%s)";

    /**
     * Removes a festival by name.
     *
     * @param name festival name
     */
    public static void remove(String name) {
        DATA = DATA.replaceAll(String.format(REGEX, name), "");
    }

    /**
     * Saves or updates a festival entry in the data string.
     *
     * @param name festival name
     * @param data encoded festival data with name
     */
    protected static void saveOrUpdate(String name, String data) {
        String o = String.format(REGEX, name);
        Matcher matcher = Pattern.compile(o).matcher(DATA);
        if (matcher.find()) {
            DATA = DATA.replaceAll(o, data);
        } else {
            DATA += data;
        }
    }

    /**
     * Adds or updates a festival by name.
     *
     * @param name     festival name
     * @param festival festival instance
     */
    public static void update(String name, Festival festival) {
        saveOrUpdate(
                name,
                festival.getData()
                        + (null == festival.getName() || festival.getName().isEmpty() ? name : festival.getName()));
    }

    /**
     * Adds or updates a festival by name with raw encoded data.
     *
     * @param name festival name
     * @param data encoded festival data
     */
    public static void updateData(String name, String data) {
        Festival.validate(data);
        saveOrUpdate(name, data);
    }

}
