#!/usr/bin/env node
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
 * Audit native-image source metadata across all Bus Maven modules.
 *
 * Usage: node .github/scripts/native.cjs
 *
 * Requirements: Node.js and a modular JDK with java and jimage available.
 * The repository root is resolved from this file, independently of the current
 * directory. No Maven build or network access is required.
 *
 * Checks declaration ownership, selected resource paths, duplicates, baseline
 * directories, tested-version lists and third-party artifact index placement.
 * The two shared ImageIO SPI rules are reported as intentional exceptions.
 *
 * This read-only audit neither modifies files nor resolves Maven versions.
 * It is not a complete GraalVM schema validator or a native build/runtime test.
 * stdout receives one JSON report. Validation failures set exit code 1; success
 * exits with 0. Missing tools and unexpected I/O errors throw directly.
 */
'use strict';
const fs = require('node:fs');
const path = require('node:path');
const cp = require('node:child_process');

// Discover only bus- directories with a POM. Sorting makes module traversal and
// report ordering reproducible.
const root = path.resolve(__dirname, '../..');
const modules = fs.readdirSync(root).filter(m => m.startsWith('bus-') && fs.existsSync(path.join(root, m, 'pom.xml'))).sort();

// Collect all errors. files holds metadata paths; layouts holds bus-preset
// payload paths; indexes is checked after traversal; seen maps normalized rows
// to every source location so duplicates can be checked across components.
const errors = [], files = [], layouts = [], indexes = [], seen = new Map();
const count = {
    reflection: 0,
    serialization: 0,
    resources: 0,
    initialization: 0
};

// java prints properties on stderr. Inspect that installation's runtime image
// to recognize JDK classes outside java.*, including javax.imageio and com.sun.
// Treating every javax.* class as a platform type would hide third-party types.
const java = cp.spawnSync('java', [
    '-XshowSettings:properties',
    '-version'
], { encoding: 'utf8' });
const javaHome = /java.home\s*=\s*(.+)/.exec(java.stderr)?.[1].trim();
if (!javaHome)
    throw new Error('A JDK with jimage is required to distinguish platform types from third-party classes.');
const jdk = new Set(cp.execFileSync(path.join(javaHome, 'bin/jimage'), [
    'list',
    path.join(javaHome, 'lib/modules')
], {
    encoding: 'utf8',
    maxBuffer: 20000000
}).split('\n').map(s => s.trim()).filter(s => s.endsWith('.class')).map(s => s.slice(0, -6).replaceAll('/', '.')));

// Primitive names have no corresponding class files in the runtime image.
const primitives = new Set([
    'boolean',
    'byte',
    'char',
    'short',
    'int',
    'long',
    'float',
    'double',
    'void'
]);

/**
 * Extract the component suffix from an org.miaixz.bus.<component> type name.
 *
 * @param {string} t Declared type name, not a method parameter or condition.
 * @returns {string|false|undefined} Component suffix or a falsy value.
 */
const owner = t => typeof t === 'string' && /^org\.miaixz\.bus\.([^.]+)/.exec(t)?.[1];

/**
 * Recognize platform declarations, including primitive and reference arrays.
 *
 * java.*, sun.* and jdk.* remain platform namespaces even when a class is absent
 * from the current JDK image. This fallback determines ownership, not whether
 * the class exists or is compatible with the current native-image toolchain.
 *
 * @param {string} t Type name with optional array suffixes.
 * @returns {boolean} Whether the type is a platform declaration.
 */
const platform = t => {
    const base = t.replace(/\[\]/g, '');
    return primitives.has(base) || jdk.has(base) || /^(java\.|sun\.|jdk\.)/.test(base);
};

/**
 * Normalize metadata for duplicate comparisons without modifying its source.
 *
 * Sort object keys and fields/methods/queriedMethods lists. Preserve all other
 * array order: method parameters and proxy interface order carry meaning.
 *
 * @param {*} x Metadata value to normalize.
 * @param {string} [key] Parent property name identifying member lists.
 * @returns {*} Normalized copy.
 */
function stable(x, key) {
    if (Array.isArray(x)) {
        const a = x.map(v => stable(v));
        return [
            'fields',
            'methods',
            'queriedMethods'
        ].includes(key) ? a.sort((a, b) => JSON.stringify(a).localeCompare(JSON.stringify(b))) : a;
    }
    return x && typeof x === 'object' ? Object.fromEntries(Object.keys(x).sort().map(k => [
        k,
        stable(x[k], k)
    ])) : x;
}

/**
 * Serialize a normalized metadata row for duplicate detection.
 *
 * @param {*} x Metadata row.
 * @returns {string} Stable JSON comparison key.
 */
const stringify = x => JSON.stringify(stable(x));

/**
 * Extract type targets from ordinary, proxy and lambda declaration forms.
 *
 * Supports type/name strings, proxy interfaces and lambda declaring classes.
 * Parameter types and typeReached conditions do not determine row ownership.
 *
 * @param {Object} r Reflection, JNI or serialization row.
 * @returns {string[]} Declaration targets, or an empty list.
 */
function declarations(r) {
    const t = r.type || r.name;
    return typeof t === 'string' ? [t] : t?.proxy || (t?.lambda ? [t.lambda.declaringClass] : []);
}

/**
 * Inspect one module's metadata tree and collect errors, rows and indexes.
 *
 * Missing trees are skipped here and checked by the final completeness pass.
 * JSON parsing errors are collected so remaining files can still be audited.
 * Version-index checks are deferred until all files have been discovered.
 *
 * @param {string} dir Directory to visit.
 * @param {string} module Maven module directory name including bus-.
 * @returns {void}
 */
function walk(dir, module) {
    if (!fs.existsSync(dir))
        return;
    for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
        const file = path.join(dir, e.name), relative = path.relative(root, file);
        if (e.isDirectory()) {
            walk(file, module);
            continue;
        }
        // Ordinary components may initialize their own types or JDK types.
        // Third-party initialization belongs in bus-preset. Count files here,
        // rather than counting individual initialization arguments.
        if (e.name === 'native-image.properties') {
            if (module === 'bus-preset')
                layouts.push(relative);
            count.initialization++;
            const text = fs.readFileSync(file, 'utf8');
            if (module !== 'bus-preset')
                for (const match of text.matchAll(/--initialize-at-(?:build|run)-time=([^\s\\]+)/g))
                    for (const type of match[1].split(','))
                        if (owner(type) !== module.slice(4) && !platform(type))
                            errors.push(`${ relative }: foreign initialization ${ type }`);
        }

        // Ancillary non-JSON files are outside this audit. Parse JSON before
        // deciding whether it is an index or a supported metadata document.
        if (!e.name.endsWith('.json'))
            continue;
        let doc;
        try {
            doc = JSON.parse(fs.readFileSync(file, 'utf8'));
        } catch (err) {
            errors.push(`${ relative }: ${ err.message }`);
            continue;
        }
        // Indexes describe versions rather than registrations. Other JSON
        // files must use the repository's unified reachability metadata format.
        if (e.name === 'index.json') {
            indexes.push({
                file,
                doc
            });
            continue;
        }
        if (e.name !== 'reachability-metadata.json') {
            errors.push(`${ relative }: unexpected legacy metadata file`);
            continue;
        }
        // Require the component's own org.miaixz resource directory. Lattice
        // types are application-specific and must not leak into published Bus
        // metadata, including within signatures or conditions.
        files.push(relative);
        if (module === 'bus-preset')
            layouts.push(relative);
        if (module !== 'bus-preset' && !relative.includes(`native-image/org.miaixz/${ module }/`))
            errors.push(`${ relative }: component metadata is outside its owner directory`);
        if (JSON.stringify(doc).includes('org.miaixz.lattice.'))
            errors.push(`${ relative }: application-specific metadata`);
        for (const [section, rows] of Object.entries(doc)) {
            if (!Array.isArray(rows))
                continue;
            count[section] = (count[section] || 0) + rows.length;
            for (const row of rows) {
                // Duplicate keys include the section and entire normalized row.
                // Rows with different conditions remain distinct registrations.
                const key = `${ section }:${ stringify(row) }`, locations = seen.get(key) || [];
                locations.push(relative);
                seen.set(key, locations);
                if ([
                        'reflection',
                        'serialization',
                        'jni'
                    ].includes(section)) {
                    // A composite proxy involving this component's interface
                    // remains component-owned. Pure third-party declarations
                    // belong in bus-preset; Bus-owned declarations do not.
                    const types = declarations(row), ownTypes = types.filter(owner);
                    if (module === 'bus-preset') {
                        if (ownTypes.length)
                            errors.push(`${ relative }: Bus-owned declaration ${ types.join(', ') }`);
                    } else if (ownTypes.length) {
                        if (!ownTypes.some(t => owner(t) === module.slice(4)))
                            errors.push(`${ relative }: cross-component declaration ${ types.join(', ') }`);
                    } else if (types.some(t => !platform(t)))
                        errors.push(`${ relative }: third-party declaration ${ types.join(', ') }`);
                }
                // Keep Bus SPI files in their owning component. Also reject
                // selected common third-party resource prefixes. This targeted
                // check does not claim to infer ownership of every resource.
                if (section === 'resources') {
                    const glob = row.glob || '';
                    const spiOwner = /META-INF\/services\/org\.miaixz\.bus\.([^.]+)/.exec(glob)?.[1];
                    if (spiOwner && (module === 'bus-preset' || `bus-${ spiOwner }` !== module))
                        errors.push(`${ relative }: misplaced Bus SPI ${ glob }`);
                    if (module !== 'bus-preset' && /^(org\/apache|com\/google|io\/netty|org\/springframework)\//.test(glob))
                        errors.push(`${ relative }: third-party resource ${ glob }`);
                }
            }
        }
    }
}

// Collect all files before performing checks that require a repository-wide view.
for (const m of modules)
    walk(path.join(root, m, 'src/main/resources/META-INF/native-image'), m);

// Only these two exact ImageIO SPI rows may appear once in bus-image and once
// in bus-office: either component can be used independently. Every other exact
// duplicate, including a repetition within one file, is an audit error.
const intentional = [];
for (const [key, locations] of seen) {
    if (locations.length < 2)
        continue;
    const row = JSON.parse(key.slice(key.indexOf(':') + 1));
    const allowed = [
        'META-INF/services/javax.imageio.spi.ImageReaderSpi',
        'META-INF/services/javax.imageio.spi.ImageWriterSpi'
    ].includes(row.glob) && locations.length === 2 && locations.some(f => f.startsWith('bus-image/')) && locations.some(f => f.startsWith('bus-office/'));
    if (allowed)
        intentional.push({
            glob: row.glob,
            components: [
                'bus-image',
                'bus-office'
            ],
            reason: 'Both components can be consumed independently and each performs ImageIO SPI discovery.'
        });
    else
        errors.push(`Duplicate ${ key.slice(0, 180) } in ${ locations.join(', ') }`);
}

// Validate local index structure and version coverage without contacting Maven.
// Official release existence must be verified separately when changing versions.
for (const {file, doc} of indexes) {
    const dir = path.dirname(file), versions = new Set();
    if (!Array.isArray(doc) || !doc.length) {
        errors.push(`${ file }: empty index`);
        continue;
    }
    // The baseline must exist and appear in a unique, numerically sorted
    // tested-versions list. Official suffixes such as .Final remain intact.
    for (const entry of doc) {
        const version = entry['metadata-version'];
        versions.add(version);
        if (!fs.existsSync(path.join(dir, version || 'missing')))
            errors.push(`${ file }: missing baseline ${ version }`);
        const tested = entry['tested-versions'];
        if (!Array.isArray(tested) || !tested.includes(version) || new Set(tested).size !== tested.length)
            errors.push(`${ file }: invalid tested-versions`);
        else if (JSON.stringify(tested) !== JSON.stringify([...tested].sort(new Intl.Collator('en', { numeric: true }).compare)))
            errors.push(`${ file }: unsorted tested-versions`);
    }
    // Reject nonempty version directories lacking a corresponding index entry.
    for (const e of fs.readdirSync(dir, { withFileTypes: true }))
        if (e.isDirectory() && fs.readdirSync(path.join(dir, e.name)).length && !versions.has(e.name))
            errors.push(`${ file }: unindexed version ${ e.name }`);
}

// Enforce group/artifact/version/file for each third-party payload, with an
// index.json at its artifact root, including initialization-only libraries.
const nativeRoot = path.join(root, 'bus-preset/src/main/resources/META-INF/native-image');
for (const relative of layouts) {
    const parts = path.relative(nativeRoot, path.join(root, relative)).split(path.sep);
    if (parts.length !== 4 || !fs.existsSync(path.join(nativeRoot, parts[0], parts[1], 'index.json')))
        errors.push(`${ relative }: missing artifact index or invalid layout`);
}

// POMs, the aggregate module and the third-party container are exempt.
// The remaining Bus components must provide their own metadata document.
for (const module of modules.filter(m => ![
        'bus-all',
        'bus-bom',
        'bus-parent',
        'bus-preset'
    ].includes(m)))
    if (!files.some(f => f.startsWith(module + '/')))
        errors.push(`${ module }: missing component metadata`);

// Emit all findings together. A failing status must also fail the CI process;
// printing a JSON error alone would otherwise leave a successful exit status.
console.log(JSON.stringify({
    status: errors.length ? 'FAIL' : 'PASS',
    components: modules.length,
    metadata_files: files.length,
    indexes: indexes.length,
    entries: count,
    intentional_shared_spi: intentional,
    errors
}, null, 2));
if (errors.length)
    process.exitCode = 1;
