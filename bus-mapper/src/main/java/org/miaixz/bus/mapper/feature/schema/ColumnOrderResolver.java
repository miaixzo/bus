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
package org.miaixz.bus.mapper.feature.schema;

import java.lang.reflect.Field;
import java.util.*;

import org.miaixz.bus.core.xyz.FieldKit;
import org.miaixz.bus.mapper.annotation.ColumnOrder;
import org.miaixz.bus.mapper.parsing.ColumnMeta;
import org.miaixz.bus.mapper.parsing.TableMeta;

/**
 * Resolves schema-only column ordering without mutating cached mapper metadata.
 *
 * @author Kimi Liu
 */
public class ColumnOrderResolver {

    /**
     * Resolves primary-key-first declaration order, optional audit tails, and explicit ancestor and descendant rules.
     *
     * @param table            final mapped table metadata
     * @param auditColumnsLast whether conventional audit properties default to the end
     * @return an independent, ordered list of the original column objects
     * @throws IllegalArgumentException when an explicit rule is invalid
     */
    public List<ColumnMeta> resolve(TableMeta table, boolean auditColumnsLast) {
        Map<String, ColumnMeta> columns = new LinkedHashMap<>();
        for (ColumnMeta column : table.columns()) {
            if (columns.putIfAbsent(column.property(), column) != null) {
                throw invalid(table, table.entityClass(), "ambiguous mapped property '" + column.property() + "'");
            }
        }
        List<Class<?>> hierarchy = new ArrayList<>();
        for (Class<?> current = table.entityClass(); current != null
                && current != Object.class; current = current.getSuperclass()) {
            if (current == table.entityClass() || !table.isExcludeSuperClass(current)) {
                hierarchy.add(current);
            }
        }
        Collections.reverse(hierarchy);
        Map<Field, List<ColumnMeta>> fields = new HashMap<>();
        for (ColumnMeta column : columns.values()) {
            if (column.fieldMeta() != null && column.fieldMeta().getField() != null) {
                fields.computeIfAbsent(column.fieldMeta().getField(), key -> new ArrayList<>()).add(column);
            }
        }
        List<ColumnMeta> declared = new ArrayList<>(columns.size());
        for (Class<?> source : hierarchy) {
            for (Field field : FieldKit.getDeclaredFields(source, null)) {
                List<ColumnMeta> mapped = fields.get(field);
                if (mapped != null) {
                    declared.addAll(mapped);
                }
            }
        }
        // Retain custom columns without a declaring field after the source-backed properties.
        Set<String> ordered = new LinkedHashSet<>();
        declared.stream().filter(ColumnMeta::id).map(ColumnMeta::property).forEach(ordered::add);
        columns.values().stream().filter(ColumnMeta::id).map(ColumnMeta::property).forEach(ordered::add);
        declared.stream().map(ColumnMeta::property).forEach(ordered::add);
        ordered.addAll(columns.keySet());
        List<String> base = new ArrayList<>(ordered);
        List<String> first = new ArrayList<>();
        List<String> last = new ArrayList<>();
        if (auditColumnsLast) {
            List.of("status", "creator", "created", "modifier", "modified").stream().filter(columns::containsKey)
                    .forEach(last::add);
        }
        Class<?> completeSource = null;
        // Keep origins so errors in surviving inherited rules identify their declaration, not the child entity.
        Map<String, Class<?>> sources = new HashMap<>();
        for (Class<?> source : hierarchy) {
            ColumnOrder order = source.getDeclaredAnnotation(ColumnOrder.class);
            if (order == null) {
                continue;
            }
            if (order.value().length > 0 && (order.first().length > 0 || order.last().length > 0)) {
                throw invalid(table, source, "value cannot be combined with first or last");
            }
            Set<String> mentioned = new HashSet<>();
            validateDeclaration(table, source, order.value(), mentioned);
            validateDeclaration(table, source, order.first(), mentioned);
            validateDeclaration(table, source, order.last(), mentioned);
            if (order.value().length > 0) {
                base = new ArrayList<>(List.of(order.value()));
                first.clear();
                last.clear();
                completeSource = source;
                sources.clear();
            } else {
                first.removeAll(mentioned);
                if (order.last().length > 0) {
                    last.clear();
                } else {
                    last.removeAll(mentioned);
                }
                first.addAll(List.of(order.first()));
                last.addAll(List.of(order.last()));
            }
            mentioned.forEach(name -> sources.put(name, source));
        }
        // Validate mappings only after replacement: superseded ancestor rules are no longer effective.
        if (completeSource != null) {
            for (String name : base) {
                validateMapped(table, completeSource, name, columns);
            }
        }
        for (String name : first) {
            validateMapped(table, sources.get(name), name, columns);
        }
        for (String name : last) {
            validateMapped(table, sources.getOrDefault(name, table.entityClass()), name, columns);
        }
        if (completeSource != null && base.size() != columns.size()) {
            Set<String> missing = new LinkedHashSet<>(columns.keySet());
            missing.removeAll(base);
            throw invalid(table, completeSource, "value is missing mapped properties " + missing);
        }
        List<ColumnMeta> result = new ArrayList<>(columns.size());
        first.forEach(name -> result.add(columns.get(name)));
        Set<String> positioned = new HashSet<>(first);
        positioned.addAll(last);
        base.stream().filter(name -> !positioned.contains(name)).forEach(name -> result.add(columns.get(name)));
        last.forEach(name -> result.add(columns.get(name)));
        return result;
    }

    /**
     * Validates list syntax and duplicate names at the same declaration level, even when later replaced.
     */
    private void validateDeclaration(TableMeta table, Class<?> source, String[] names, Set<String> mentioned) {
        for (String name : names) {
            if (name == null || name.isBlank()) {
                throw invalid(table, source, "property names must not be blank");
            }
            if (!mentioned.add(name)) {
                throw invalid(table, source, "duplicate property '" + name + "'");
            }
        }
    }

    /**
     * Validates a surviving rule against the final mapped properties.
     */
    private void validateMapped(TableMeta table, Class<?> source, String name, Map<String, ColumnMeta> columns) {
        if (!columns.containsKey(name)) {
            throw invalid(table, source, "property '" + name + "' is not mapped");
        }
    }

    /**
     * Builds a configuration error identifying both the effective entity and the rule source.
     */
    private IllegalArgumentException invalid(TableMeta table, Class<?> source, String reason) {
        return new IllegalArgumentException("Invalid @ColumnOrder for " + table.entityClass().getName()
                + " declared in " + source.getName() + ": " + reason);
    }

}
