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
package org.miaixz.bus.spring.undertow;

import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * Customizes the Undertow servlet web server factory, specifically for WebSocket configuration.
 * <p>
 * This class implements {@link WebServerFactoryCustomizer} to configure Undertow's {@link WebSocketDeploymentInfo},
 * ensuring that WebSocket buffers are properly set up.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Component
public class DeploymentInfoCustomizers implements WebServerFactoryCustomizer<UndertowServletWebServerFactory> {

    /**
     * Customizes the Undertow servlet web server factory.
     * <p>
     * This method adds a {@code DeploymentInfoCustomizer} to the factory, which configures the
     * {@link WebSocketDeploymentInfo} with a {@link DefaultByteBufferPool} for WebSocket buffers.
     * </p>
     *
     * @param factory The {@link UndertowServletWebServerFactory} to customize.
     */
    @Override
    public void customize(UndertowServletWebServerFactory factory) {
        factory.addDeploymentInfoCustomizers(deploymentInfo -> {
            WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo();
            webSocketDeploymentInfo.setBuffers(new DefaultByteBufferPool(false, 1024));
            deploymentInfo.addServletContextAttribute(
                    "io.undertow.websockets.jsr.WebSocketDeploymentInfo",
                    webSocketDeploymentInfo);
        });
    }

}
