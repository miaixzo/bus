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
package org.miaixz.bus.cortex.registry;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cortex.Assets;
import org.miaixz.bus.cortex.Type;
import org.miaixz.bus.cortex.registry.api.ApiAssets;
import org.miaixz.bus.cortex.registry.mcp.McpAssets;
import org.miaixz.bus.cortex.registry.prompt.PromptAssets;

/**
 * Asset subtype factory and shallow-copy helper.
 * <p>
 * Subtype metadata is owned by each subtype's {@code Meta}; this helper copies only base asset fields.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RegistryAssets {

    /**
     * Creates the registry assets utility holder.
     */
    private RegistryAssets() {

    }

    /**
     * Resolves asset type from the field, subtype, or metadata.
     *
     * @param asset asset
     * @return resolved type
     */
    public static Type typeOf(Assets asset) {
        if (asset == null) {
            return Type.API;
        }
        if (asset.getType() != null) {
            return Type.requireKnownKey(asset.getType());
        }
        if (asset instanceof PromptAssets) {
            return Type.PROMPT;
        }
        if (asset instanceof McpAssets) {
            return Type.MCP;
        }
        if (asset instanceof ApiAssets) {
            return Type.API;
        }
        Map<String, Object> cortex = MetadataCodec
                .object(MetadataCodec.root(asset.getMetadata()).get(MetadataCodec.ROOT));
        if (cortex == null) {
            return Type.API;
        }
        String type = MetadataCodec.string(cortex.get(MetadataCodec.TYPE));
        if (StringKit.isEmpty(type)) {
            type = MetadataCodec.string(cortex.get(MetadataCodec.LEGACY_SPECIES));
        }
        if (StringKit.isEmpty(type)) {
            return Type.API;
        }
        return Type.requireKnown(type);
    }

    /**
     * Copies an asset while preserving its effective subtype.
     *
     * @param asset source asset
     * @return typed copy
     */
    public static Assets copy(Assets asset) {
        return asset == null ? null : copyAs(asset, typeOf(asset));
    }

    /**
     * Copies an asset and writes its subtype metadata view back to metadata.
     *
     * @param asset source asset
     * @return normalized typed copy
     */
    public static Assets normalize(Assets asset) {
        Assets copy = copy(asset);
        normalizeMeta(copy);
        return copy;
    }

    /**
     * Copies an asset as an API definition.
     *
     * @param asset source asset
     * @return API asset copy
     */
    public static ApiAssets api(Assets asset) {
        return (ApiAssets) copyAs(asset, Type.API);
    }

    /**
     * Copies an asset as an MCP definition.
     *
     * @param asset source asset
     * @return MCP asset copy
     */
    public static McpAssets mcp(Assets asset) {
        return (McpAssets) copyAs(asset, Type.MCP);
    }

    /**
     * Copies an asset as a prompt definition.
     *
     * @param asset source asset
     * @return prompt asset copy
     */
    public static PromptAssets prompt(Assets asset) {
        return (PromptAssets) copyAs(asset, Type.PROMPT);
    }

    /**
     * Writes the subtype metadata view back to the asset metadata string.
     *
     * @param asset target asset
     */
    public static void normalizeMeta(Assets asset) {
        if (asset instanceof ApiAssets api) {
            api.normalizeMeta();
        } else if (asset instanceof McpAssets mcp) {
            mcp.normalizeMeta();
        } else if (asset instanceof PromptAssets prompt) {
            prompt.normalizeMeta();
        }
    }

    /**
     * Copies one asset into a requested subtype.
     *
     * @param source source asset
     * @param type   requested type
     * @return typed copy
     */
    public static Assets copyAs(Assets source, Type type) {
        if (source == null) {
            return null;
        }
        Assets copy = create(type, source);
        copyBase(copy, source);
        return copy;
    }

    /**
     * Creates an empty asset subtype for the requested type or source instance.
     *
     * @param type   requested registry type
     * @param source source asset
     * @return empty asset subtype
     */
    private static Assets create(Type type, Assets source) {
        if (Type.MCP.is(type) || source instanceof McpAssets) {
            return new McpAssets();
        }
        if (Type.PROMPT.is(type) || source instanceof PromptAssets) {
            return new PromptAssets();
        }
        if (Type.API.is(type) || source instanceof ApiAssets) {
            return new ApiAssets();
        }
        return new Assets();
    }

    /**
     * Copies base asset fields from one asset to another.
     *
     * @param target target asset
     * @param source source asset
     */
    private static void copyBase(Assets target, Assets source) {
        target.setId(source.getId());
        target.setNamespace_id(source.getNamespace_id());
        target.setApp_id(source.getApp_id());
        target.setType(source.getType());
        target.setStatus(source.getStatus());
        target.setCreator(source.getCreator());
        target.setCreated(source.getCreated());
        target.setModifier(source.getModifier());
        target.setModified(source.getModified());
        target.setName(source.getName());
        target.setIcon(source.getIcon());
        target.setHost(source.getHost());
        target.setPort(source.getPort());
        target.setPath(source.getPath());
        target.setUrl(source.getUrl());
        target.setMethod(source.getMethod());
        target.setProtocol(source.getProtocol());
        target.setStream(source.getStream());
        target.setVerb(source.getVerb());
        target.setPolicy(source.getPolicy());
        target.setSign(source.getSign());
        target.setScope(source.getScope());
        target.setRetries(source.getRetries());
        target.setTimeout(source.getTimeout());
        target.setThrottle(source.getThrottle());
        target.setBalance(source.getBalance());
        target.setWeight(source.getWeight());
        target.setCommand(source.getCommand());
        target.setEnv(source.getEnv());
        target.setSort(source.getSort());
        target.setMock(source.getMock());
        target.setResult(source.getResult());
        target.setVersion(source.getVersion());
        target.metadata(source.getMetadata());
        target.setDescription(source.getDescription());
        target.setTtl(source.getTtl());
        target.setLabels(source.getLabels() == null ? null : new LinkedHashMap<>(source.getLabels()));
    }

}
