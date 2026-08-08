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
package org.miaixz.bus.mapper.feature.affix;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.mapper.Args;
import org.miaixz.bus.mapper.Context;
import org.miaixz.bus.mapper.handler.ScopedProviderHandler;

/**
 * Integrates affix-based table-name rewriting into the Mapper execution chain.
 * <p>
 * Rules are resolved for the active datasource, then shared and default scopes. Query and update SQL is delegated to
 * {@link AffixSqlRewriter} using values from {@link AffixValueProvider} and exclusions from {@link AffixRuleConfig}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class AffixRewriteHandler extends ScopedProviderHandler<Object, AffixRuleConfig, AffixValueProvider> {

    /**
     * Creates an affix rewrite handler that resolves its rules from runtime properties or context.
     */
    public AffixRewriteHandler() {
        super();
    }

    /**
     * Creates an affix rewrite handler with an explicit default rule configuration.
     *
     * @param config default affix rule configuration
     */
    public AffixRewriteHandler(AffixRuleConfig config) {
        super(config);
    }

    /**
     * Returns the flattened property scope used to configure affix rewriting.
     *
     * @return the {@code affix} property scope
     */
    @Override
    protected String scope() {
        return Args.AFFIX_KEY;
    }

    /**
     * Captures thread-local affix rules from the current Mapper context.
     *
     * @return context-specific affix rules, or {@code null} when none are configured
     */
    @Override
    protected AffixRuleConfig capture() {
        Context.MapperConfig contextConfig = Context.getMapperConfig();
        return contextConfig != null ? contextConfig.getAffix() : null;
    }

    /**
     * Returns the handler order used to rewrite table names before later SQL handlers execute.
     *
     * @return an order immediately after the minimum handler order
     */
    @Override
    public int getOrder() {
        return MIN_VALUE + 1;
    }

    /**
     * Rewrites physical table references before a Mapper query is executed.
     *
     * @param result        invocation result placeholder supplied by the interceptor
     * @param executor      MyBatis executor handling the query
     * @param ms            mapped statement being executed
     * @param parameter     query parameter object
     * @param rowBounds     query row bounds
     * @param resultHandler optional MyBatis result handler
     * @param boundSql      bound SQL produced for the current query
     */
    @Override
    public void query(
            Object result,
            Executor executor,
            MappedStatement ms,
            Object parameter,
            RowBounds rowBounds,
            ResultHandler resultHandler,
            BoundSql boundSql) {
        AffixRuleConfig currentConfig = current();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.trace(true, "Mapper", "Affix rule config not found, skipping: method={}", ms.getId());
            return;
        }
        processSqlInMappedStatement(ms, boundSql, parameter, currentConfig);
    }

    /**
     * Rewrites physical table references before a Mapper insert, update, or delete is executed.
     *
     * @param executor  MyBatis executor handling the statement
     * @param ms        mapped statement being executed
     * @param parameter statement parameter object
     */
    @Override
    public void update(Executor executor, MappedStatement ms, Object parameter) {
        AffixRuleConfig currentConfig = current();
        if (currentConfig == null || currentConfig.getProvider() == null) {
            Logger.trace(true, "Mapper", "Affix rule config not found, skipping: method={}", ms.getId());
            return;
        }
        processSqlInMappedStatement(ms, null, parameter, currentConfig);
    }

    /**
     * Returns the provider contract accepted by this handler.
     *
     * @return affix value provider type
     */
    @Override
    protected Class<AffixValueProvider> type() {
        return AffixValueProvider.class;
    }

    /**
     * Resolves effective affix rules for a datasource scope.
     *
     * @param datasourceKey effective datasource key
     * @param properties    flattened Mapper properties
     * @param provider      optional dynamic affix value provider
     * @return resolved affix rules, or {@code null} when the feature is not configured
     */
    @Override
    protected AffixRuleConfig resolve(String datasourceKey, Properties properties, AffixValueProvider provider) {
        return resolveConfig(datasourceKey, properties, provider);
    }

    /**
     * Resolves datasource, shared, and default affix settings into executable rules.
     * <p>
     * Datasource values take precedence over shared values, which take precedence over default values. A supplied
     * provider controls prefix and suffix values while property-based ignore lists remain effective.
     *
     * @param datasourceKey effective datasource key
     * @param properties    flattened Mapper properties
     * @param provider      optional dynamic affix value provider
     * @return resolved affix rules, or {@code null} when neither values nor a provider are configured
     */
    public static AffixRuleConfig resolveConfig(
            String datasourceKey,
            Properties properties,
            AffixValueProvider provider) {
        if (properties == null && provider == null) {
            return null;
        }
        if (properties == null) {
            return AffixRuleConfig.builder().provider(provider).prefixIgnore(Collections.emptyList())
                    .suffixIgnore(Collections.emptyList()).build();
        }

        String key = StringKit.isNotEmpty(datasourceKey) ? datasourceKey : Normal.DEFAULT;
        String datasourceAffixScope = key + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT;
        String sharedAffixScope = Args.SHARED_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT;
        String defaultAffixScope = Normal.DEFAULT + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT;
        String prefixValue = properties.getProperty(
                datasourceAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                properties.getProperty(
                        sharedAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                        properties.getProperty(
                                defaultAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                                Normal.EMPTY)));
        String suffixValue = properties.getProperty(
                datasourceAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                properties.getProperty(
                        sharedAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                        properties.getProperty(
                                defaultAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_VALUE,
                                Normal.EMPTY)));
        String prefixIgnore = properties.getProperty(
                datasourceAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                properties.getProperty(
                        sharedAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        properties.getProperty(
                                defaultAffixScope + Args.PREFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                                Normal.EMPTY)));
        String suffixIgnore = properties.getProperty(
                datasourceAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                properties.getProperty(
                        sharedAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                        properties.getProperty(
                                defaultAffixScope + Args.SUFFIX_KEY + Symbol.DOT + Args.PROP_IGNORE,
                                Normal.EMPTY)));

        List<String> prefixIgnoreTables = StringKit.isNotEmpty(prefixIgnore)
                ? Arrays.stream(prefixIgnore.split(Symbol.COMMA)).map(String::trim).filter(ObjectKit::isNotEmpty)
                        .collect(Collectors.toList())
                : Collections.emptyList();
        List<String> suffixIgnoreTables = StringKit.isNotEmpty(suffixIgnore)
                ? Arrays.stream(suffixIgnore.split(Symbol.COMMA)).map(String::trim).filter(ObjectKit::isNotEmpty)
                        .collect(Collectors.toList())
                : Collections.emptyList();
        if (provider == null && StringKit.isEmpty(prefixValue) && StringKit.isEmpty(suffixValue)) {
            return null;
        }

        AffixValueProvider resolvedProvider = provider;
        if (resolvedProvider == null) {
            String configuredPrefix = prefixValue;
            String configuredSuffix = suffixValue;
            resolvedProvider = new AffixValueProvider() {

                @Override
                public String getPrefix() {
                    return configuredPrefix;
                }

                @Override
                public String getSuffix() {
                    return configuredSuffix;
                }
            };
        }
        return AffixRuleConfig.builder().provider(resolvedProvider).prefixIgnore(prefixIgnoreTables)
                .suffixIgnore(suffixIgnoreTables).build();
    }

    /**
     * Resolves whether affix rewriting is enabled for a datasource scope.
     *
     * @param datasourceKey effective datasource key
     * @param properties    flattened Mapper properties
     * @return {@code true} when affix rewriting is enabled
     */
    @Override
    protected boolean enabled(String datasourceKey, Properties properties) {
        if (properties == null) {
            return true;
        }
        String key = StringKit.isNotEmpty(datasourceKey) ? datasourceKey : getDatasourceKey();
        String datasourceEnabledKey = key + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PROP_ENABLED;
        String sharedEnabledKey = Args.SHARED_KEY + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PROP_ENABLED;
        String defaultEnabledKey = Normal.DEFAULT + Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT + Args.PROP_ENABLED;
        return Boolean.parseBoolean(
                properties.getProperty(
                        datasourceEnabledKey,
                        properties.getProperty(sharedEnabledKey, properties.getProperty(defaultEnabledKey, "true"))));
    }

    /**
     * Tests whether flattened Mapper properties contain an affix configuration scope.
     *
     * @param properties flattened Mapper properties
     * @return {@code true} when at least one {@code affix.*} property is present
     */
    @Override
    protected boolean hasScopeConfiguration(Properties properties) {
        if (properties == null) {
            return false;
        }
        String affixMarker = Symbol.DOT + Args.AFFIX_KEY + Symbol.DOT;
        return properties.stringPropertyNames().stream().anyMatch(key -> key.contains(affixMarker));
    }

    /**
     * Applies the current affix rules to the SQL associated with a mapped statement.
     *
     * @param ms        mapped statement whose SQL is being processed
     * @param boundSql  current bound SQL, or {@code null} when it must be resolved from the statement
     * @param parameter statement parameter object
     * @param config    effective affix rule configuration
     */
    private void processSqlInMappedStatement(
            MappedStatement ms,
            BoundSql boundSql,
            Object parameter,
            AffixRuleConfig config) {
        String prefix = config.getProvider().getPrefix();
        String suffix = config.getProvider().getSuffix();
        if (StringKit.isEmpty(prefix) && StringKit.isEmpty(suffix)) {
            return;
        }

        try {
            String originalSql = currentSql(ms, parameter, boundSql);
            String actualSql = new AffixSqlRewriter(prefix, config.getPrefixIgnore(), suffix, config.getSuffixIgnore())
                    .apply(originalSql);
            if (!originalSql.equals(actualSql)) {
                if (boundSql != null && !setBoundSql(boundSql, actualSql)) {
                    Logger.warn(
                            false,
                            "Mapper",
                            "Affix SQL update failed: method={}, reason=boundSqlImmutable",
                            ms.getId());
                }
                putSqlRewrite(ms, actualSql);
                Logger.debug(
                        false,
                        "Mapper",
                        "Applied affix rules: prefix={}, suffix={}, method={}",
                        prefix,
                        suffix,
                        ms.getId());
            }
        } catch (Exception e) {
            Logger.warn(
                    false,
                    "Mapper",
                    e,
                    "Affix SQL rewrite failed: method={}, exception={}",
                    ms.getId(),
                    e.getClass().getSimpleName());
        }
    }

    /**
     * Resolves the SQL text used as the next affix rewrite input.
     *
     * @param ms        mapped statement whose SQL is being processed
     * @param parameter statement parameter object
     * @param boundSql  current bound SQL, or {@code null} when unavailable
     * @return current SQL text, including any rewrite produced by an earlier handler
     */
    private String currentSql(MappedStatement ms, Object parameter, BoundSql boundSql) {
        if (boundSql != null) {
            return boundSql.getSql();
        }
        String rewrittenSql = getSqlRewrite(ms);
        return rewrittenSql != null ? rewrittenSql : getFreshBoundSql(ms, parameter).getSql();
    }

}
