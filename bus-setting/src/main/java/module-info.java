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
/**
 * Module: {@code bus.setting}
 *
 * <p>
 * Provides a consistent API for loading and accessing application settings.
 *
 * <p>
 * Includes configuration formats, typed value access, resource-backed settings, and readers for properties, YAML,
 * TOML, and INI documents.
 *
 * @author Kimi Liu
 */
module bus.setting {

    requires bus.core;
    requires bus.logger;

    requires static org.yaml.snakeyaml;

    exports org.miaixz.bus.setting;
    exports org.miaixz.bus.setting.format;
    exports org.miaixz.bus.setting.magic;
    exports org.miaixz.bus.setting.nimble.ini;
    exports org.miaixz.bus.setting.nimble.props;
    exports org.miaixz.bus.setting.nimble.setting;
    exports org.miaixz.bus.setting.nimble.toml;
    exports org.miaixz.bus.setting.nimble.yaml;

}
