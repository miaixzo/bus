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
package org.miaixz.bus.setting.magic;

import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.setting.Setting;

/**
 * Represents a configuration profile, allowing for environment-specific settings. A profile defines a set of
 * configuration files that are activated under certain conditions. For example, you can define profiles like 'test',
 * 'develop', and 'production', where each profile loads configuration files from a corresponding directory (e.g.,
 * {@code /test/db.setting}, {@code /develop/db.setting}).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Profile implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852228167252L;

    /**
     * A cache for {@link Setting} instances, keyed by file name.
     */
    private final Map<String, Setting> settingMap = new ConcurrentHashMap<>();
    /**
     * The name of the current active profile.
     */
    private String profile;
    /**
     * The character set for reading settings files.
     */
    private Charset charset;
    /**
     * Whether to enable variable substitution in settings files.
     */
    private boolean useVar;

    /**
     * Default constructor. Uses the profile name "default", UTF-8 encoding, and disables variable substitution.
     */
    public Profile() {
        this("default");
    }

    /**
     * Constructs a profile with a specific name, UTF-8 encoding, and no variable substitution.
     *
     * @param profile The environment profile name.
     */
    public Profile(final String profile) {
        this(profile, Setting.DEFAULT_CHARSET, false);
    }

    /**
     * Constructs a profile with full configuration.
     *
     * @param profile The environment profile name.
     * @param charset The character set for reading files.
     * @param useVar  {@code true} to enable variable substitution.
     */
    public Profile(final String profile, final Charset charset, final boolean useVar) {
        this.profile = profile;
        this.charset = charset;
        this.useVar = useVar;
    }

    /**
     * Gets a {@link Setting} instance for a given file name under the current profile. The file is loaded from a path
     * constructed as {@code [profile_name]/[file_name]}. Instances are cached to avoid repeated file loading.
     *
     * @param name The name of the settings file. If no extension is provided, ".setting" is assumed.
     * @return The {@link Setting} instance for the current profile.
     */
    public Setting getSetting(final String name) {
        final String nameForProfile = fixNameForProfile(name);
        return settingMap.computeIfAbsent(nameForProfile, (key) -> new Setting(key, this.charset, this.useVar));
    }

    /**
     * Sets the active profile.
     *
     * @param profile The new profile name.
     * @return This {@code Profile} instance for chaining.
     */
    public Profile setProfile(final String profile) {
        this.profile = profile;
        return this;
    }

    /**
     * Sets the character set to be used for reading configuration files.
     *
     * @param charset The new character set.
     * @return This {@code Profile} instance for chaining.
     */
    public Profile setCharset(final Charset charset) {
        this.charset = charset;
        return this;
    }

    /**
     * Sets whether to enable variable substitution in configuration files.
     *
     * @param useVar {@code true} to enable variable substitution.
     * @return This {@code Profile} instance for chaining.
     */
    public Profile setUseVar(final boolean useVar) {
        this.useVar = useVar;
        return this;
    }

    /**
     * Clears the cache of all loaded {@link Setting} instances.
     *
     * @return This {@code Profile} instance for chaining.
     */
    public Profile clear() {
        this.settingMap.clear();
        return this;
    }

    /**
     * Constructs the full path for a setting file based on the current profile.
     *
     * @param name The base name of the file.
     * @return The profile-specific path (e.g., "dev/database.setting").
     */
    private String fixNameForProfile(final String name) {
        Assert.notBlank(name, "Setting name must be not blank !");
        final String actralProfile = StringKit.toStringOrEmpty(this.profile);
        if (!name.contains(Symbol.DOT)) {
            return StringKit.format("{}/{}.setting", actralProfile, name);
        }
        return StringKit.format("{}/{}", actralProfile, name);
    }

}
