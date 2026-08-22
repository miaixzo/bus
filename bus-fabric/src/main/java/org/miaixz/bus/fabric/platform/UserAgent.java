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
package org.miaixz.bus.fabric.platform;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Parsed User-Agent classification.
 *
 * @author Kimi Liu
 */
public class UserAgent {

    /**
     * Non-blank User-Agent text from which this classification was derived.
     */
    private final String value;

    /**
     * First matching browser classifier or the shared unknown classifier.
     */
    private final Browser browser;

    /**
     * First matching device classifier or the shared unknown classifier.
     */
    private final Device device;

    /**
     * First matching operating-system family or the shared unknown family.
     */
    private final OsFamily osFamily;

    /**
     * First matching rendering-engine classifier or the shared unknown classifier.
     */
    private final EngineClassifier engine;

    /**
     * Optional first capture extracted by the browser's version rule.
     */
    private final String browserVersion;

    /**
     * Optional first capture extracted by the engine's version rule.
     */
    private final String engineVersion;

    /**
     * Optional first capture extracted by the operating system's version rule.
     */
    private final String osVersion;

    /**
     * Derived mobile classification after the macOS exclusion is applied.
     */
    private final boolean mobile;

    /**
     * Creates a fully parsed User-Agent snapshot.
     *
     * @param value          original non-blank User-Agent text
     * @param browser        selected browser classifier
     * @param device         selected device classifier
     * @param osFamily       selected operating-system family
     * @param engine         selected rendering-engine classifier
     * @param browserVersion extracted browser version, or {@code null}
     * @param engineVersion  extracted engine version, or {@code null}
     * @param osVersion      extracted operating-system version, or {@code null}
     * @param mobile         derived mobile classification
     */
    public UserAgent(final String value, final Browser browser, final Device device, final OsFamily osFamily,
            final EngineClassifier engine, final String browserVersion, final String engineVersion,
            final String osVersion, final boolean mobile) {
        this.value = value;
        this.browser = browser;
        this.device = device;
        this.osFamily = osFamily;
        this.engine = engine;
        this.browserVersion = browserVersion;
        this.engineVersion = engineVersion;
        this.osVersion = osVersion;
        this.mobile = mobile;
    }

    /**
     * Classifies a non-blank User-Agent value and extracts available component versions.
     *
     * @param text User-Agent text to classify
     * @return parsed snapshot, or {@code null} when the input is null, empty, or whitespace-only
     */
    public static UserAgent parse(final String text) {
        if (StringKit.isBlank(text)) {
            return null;
        }
        final Browser browser = Browser.parse(text);
        final EngineClassifier engine = EngineClassifier.parse(text);
        final OsFamily osFamily = OsFamily.parse(text);
        final Device device = Device.parse(text);
        final boolean mobile = (device.mobile() || browser.mobile()) && !osFamily.macOS();
        return new UserAgent(text, browser, device, osFamily, engine, browser.version(text), engine.version(text),
                osFamily.version(text), mobile);
    }

    /**
     * Returns the original value.
     *
     * @return original non-blank User-Agent text
     */
    public String value() {
        return value;
    }

    /**
     * Returns the browser.
     *
     * @return selected browser classifier, possibly the shared unknown classifier
     */
    public Browser browser() {
        return browser;
    }

    /**
     * Returns the device.
     *
     * @return selected device classifier, possibly the shared unknown classifier
     */
    public Device device() {
        return device;
    }

    /**
     * Returns the operating-system family.
     *
     * @return selected operating-system family, possibly the shared unknown family
     */
    public OsFamily osFamily() {
        return osFamily;
    }

    /**
     * Returns the engine.
     *
     * @return selected rendering-engine classifier, possibly the shared unknown classifier
     */
    public EngineClassifier engine() {
        return engine;
    }

    /**
     * Returns the browser version.
     *
     * @return extracted browser version, or {@code null} when unavailable
     */
    public String browserVersion() {
        return browserVersion;
    }

    /**
     * Returns the browser version through the compatibility alias.
     *
     * @return same value as {@link #browserVersion()}
     */
    public String version() {
        return browserVersion;
    }

    /**
     * Returns the engine version.
     *
     * @return extracted rendering-engine version, or {@code null} when unavailable
     */
    public String engineVersion() {
        return engineVersion;
    }

    /**
     * Returns the operating-system version.
     *
     * @return extracted operating-system version, or {@code null} when unavailable
     */
    public String osVersion() {
        return osVersion;
    }

    /**
     * Returns whether this User-Agent is mobile.
     *
     * @return {@code true} when the device or browser is mobile and the operating system is not classified as macOS
     */
    public boolean mobile() {
        return mobile;
    }

    /**
     * Returns the original User-Agent value.
     *
     * @return User-Agent value
     */
    @Override
    public String toString() {
        return value;
    }

}
