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
package org.miaixz.bus.fabric.protocol.http.agent;

/**
 * Parsed User-Agent classification.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class UserAgent {

    /**
     * Original User-Agent text.
     */
    private final String value;

    /**
     * Browser classifier.
     */
    private final Browser browser;

    /**
     * Device classifier.
     */
    private final Device device;

    /**
     * Client OS classifier.
     */
    private final ClientOs clientOs;

    /**
     * Engine classifier.
     */
    private final Engine engine;

    /**
     * Browser version.
     */
    private final String browserVersion;

    /**
     * Engine version.
     */
    private final String engineVersion;

    /**
     * Client OS version.
     */
    private final String clientOsVersion;

    /**
     * Mobile flag.
     */
    private final boolean mobile;

    /**
     * Creates a parsed User-Agent.
     *
     * @param value           raw value
     * @param browser         browser
     * @param device          device
     * @param clientOs        client OS
     * @param engine          engine
     * @param browserVersion  browser version
     * @param engineVersion   engine version
     * @param clientOsVersion client OS version
     * @param mobile          mobile flag
     */
    private UserAgent(final String value, final Browser browser, final Device device, final ClientOs clientOs,
            final Engine engine, final String browserVersion, final String engineVersion, final String clientOsVersion,
            final boolean mobile) {
        this.value = value;
        this.browser = browser;
        this.device = device;
        this.clientOs = clientOs;
        this.engine = engine;
        this.browserVersion = browserVersion;
        this.engineVersion = engineVersion;
        this.clientOsVersion = clientOsVersion;
        this.mobile = mobile;
    }

    /**
     * Parses a User-Agent value.
     *
     * @param text User-Agent text
     * @return parsed User-Agent, or null when blank
     */
    public static UserAgent parse(final String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        final Browser browser = Browser.parse(text);
        final Engine engine = Engine.parse(text);
        final ClientOs clientOs = ClientOs.parse(text);
        final Device device = Device.parse(text);
        final boolean mobile = (device.mobile() || browser.mobile()) && !clientOs.macOS();
        return new UserAgent(text, browser, device, clientOs, engine, browser.version(text), engine.version(text),
                clientOs.version(text), mobile);
    }

    /**
     * Returns the original value.
     *
     * @return value
     */
    public String value() {
        return value;
    }

    /**
     * Returns the browser.
     *
     * @return browser
     */
    public Browser browser() {
        return browser;
    }

    /**
     * Returns the device.
     *
     * @return device
     */
    public Device device() {
        return device;
    }

    /**
     * Returns the client OS.
     *
     * @return client OS
     */
    public ClientOs clientOs() {
        return clientOs;
    }

    /**
     * Returns the engine.
     *
     * @return engine
     */
    public Engine engine() {
        return engine;
    }

    /**
     * Returns the browser version.
     *
     * @return browser version
     */
    public String browserVersion() {
        return browserVersion;
    }

    /**
     * Returns the browser version.
     *
     * @return browser version
     */
    public String version() {
        return browserVersion;
    }

    /**
     * Returns the engine version.
     *
     * @return engine version
     */
    public String engineVersion() {
        return engineVersion;
    }

    /**
     * Returns the client OS version.
     *
     * @return client OS version
     */
    public String clientOsVersion() {
        return clientOsVersion;
    }

    /**
     * Returns whether this User-Agent is mobile.
     *
     * @return true when mobile
     */
    public boolean mobile() {
        return mobile;
    }

    @Override
    public String toString() {
        return value;
    }

}
