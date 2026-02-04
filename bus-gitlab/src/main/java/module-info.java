/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
/**
 * bus.gitlab
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
module bus.gitlab {

    requires java.logging;

    requires jakarta.ws.rs;
    requires jakarta.servlet;
    requires jakarta.annotation;
    requires jersey.client;
    requires jersey.media.multipart;
    requires jersey.common;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports org.miaixz.bus.gitlab;
    exports org.miaixz.bus.gitlab.hooks.system;
    exports org.miaixz.bus.gitlab.hooks.web;
    exports org.miaixz.bus.gitlab.models;
    exports org.miaixz.bus.gitlab.services;
    exports org.miaixz.bus.gitlab.support;

}
