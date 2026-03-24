/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A plugin that rejects connections from IP addresses on a blacklist.
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 21+
 */
public final class BlackListPlugin<T> extends AbstractPlugin<T> {

    /**
     * A queue of {@link BlackListRule} instances that define which IP addresses are blacklisted.
     */
    private ConcurrentLinkedQueue<BlackListRule> ipBlackList = new ConcurrentLinkedQueue<>();

    @Override
    public AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        InetSocketAddress inetSocketAddress = null;
        try {
            inetSocketAddress = (InetSocketAddress) channel.getRemoteAddress();
        } catch (IOException e) {
            Logger.error("Failed to get remote address for channel.", e);
        }
        if (inetSocketAddress == null) {
            return channel;
        }
        for (BlackListRule rule : ipBlackList) {
            if (!rule.access(inetSocketAddress)) {
                Logger.warn(
                        "Connection from blacklisted IP: " + inetSocketAddress.getAddress().getHostAddress()
                                + " rejected.");
                return null;
            }
        }
        return channel;
    }

    /**
     * Adds a blacklist rule to this plugin.
     *
     * @param rule the {@link BlackListRule} to add
     */
    public void addRule(BlackListRule rule) {
        ipBlackList.add(rule);
    }

    /**
     * Removes a blacklist rule from this plugin.
     *
     * @param rule the {@link BlackListRule} to remove
     */
    public void removeRule(BlackListRule rule) {
        ipBlackList.remove(rule);
    }

    /**
     * Defines a rule for blacklisting IP addresses.
     */
    public interface BlackListRule {

        /**
         * Determines whether a connection from the given address should be allowed.
         *
         * @param address the {@link InetSocketAddress} of the remote client
         * @return {@code true} if the connection is allowed, {@code false} if it should be rejected
         */
        boolean access(InetSocketAddress address);
    }

}
