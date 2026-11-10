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
 * Verify a complete Bus build and its locally installed Maven artifacts.
 *
 * Usage: node .github/scripts/artifact.cjs
 *
 * Run a full clean build and Maven install for VERSION first. Component JARs,
 * source JARs, target/classes, bus-all JARs and installed artifacts must belong
 * to that same build. The installed repository is HOME/.m2/repository; custom
 * Maven local-repository settings are not resolved.
 *
 * Checks source and class bytes, Java 21 bytecode compatibility, packaged
 * native resources, effective bus-all metadata coverage, installed JAR hashes
 * and resolved revision values in the installed parent/BOM POMs.
 *
 * All operations are read-only and offline. The script does not build, install
 * or run applications. A clean build is required because comparing bytecode
 * with JARs does not independently prove it came from the current Java source.
 * This is a packaging verifier, not a GraalVM schema or runtime validator.
 *
 * stdout receives one JSON report. Validation errors set exit code 1; success
 * exits with 0. Missing/corrupt/unsupported archives and unexpected I/O failures
 * throw directly rather than returning a partial success report.
 */
'use strict';
const fs = require('node:fs'), path = require('node:path'), crypto = require('node:crypto'), zlib = require('node:zlib');

// Resolve paths independently of the current directory and use VERSION as the
// artifact version. Names and timestamps alone do not establish build freshness.
const root = path.resolve(__dirname, '../..'), version = fs.readFileSync(path.join(root, 'VERSION'), 'utf8').trim();
const modules = fs.readdirSync(root).filter(m => m.startsWith('bus-') && fs.existsSync(path.join(root, m, 'pom.xml'))).sort();

// Keep all semantic mismatches, per-artifact evidence and source metadata rows.
// The rows later define the native coverage that bus-all must retain.
const errors = [], artifacts = [], sourceRows = [];

/**
 * Compute a content digest for an entire file or an uncompressed archive entry.
 *
 * @param {Buffer} b Bytes to compare.
 * @returns {string} Lowercase SHA-256 digest.
 */
const hash = b => crypto.createHash('sha256').update(b).digest('hex');

/**
 * Normalize metadata for comparison without changing source documents.
 *
 * Sort object keys and member declaration lists. Preserve parameterTypes and
 * proxy interface order, which are part of the registration's meaning.
 *
 * @param {*} x Metadata value.
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
 * Produce a comparison key for a type, condition, signature or resource.
 *
 * @param {*} x Value to compare.
 * @returns {string|undefined} Canonical JSON, or undefined for an absent value.
 */
const key = x => JSON.stringify(stable(x));

/**
 * Minimal random-access reader for ordinary ZIP/JAR archives.
 *
 * Index the central directory once and read entries on demand. Stored and
 * DEFLATE entries are supported using Node built-ins; ZIP64 is rejected.
 * This is not a general ZIP validator and does not independently verify CRCs
 * or archive authenticity. Each reader owns a descriptor the caller must close.
 */
class Zip {
    /**
     * Open an archive and index its central-directory records by entry path.
     *
     * @param {string} file Archive path.
     * @throws {Error} For missing/invalid archives or unsupported ZIP64 records.
     */
    constructor(file) {
        this.file = file;
        this.fd = fs.openSync(file, 'r');
        this.entries = new Map();
        // Search backwards for the end-of-central-directory (EOCD) signature.
        // Its fixed record is 22 bytes; the maximum ZIP comment is 65,535 bytes.
        const size = fs.fstatSync(this.fd).size, tail = this.read(Math.max(0, size - 65557), Math.min(size, 65557));
        let end = -1;
        for (let i = tail.length - 22; i >= 0; i--)
            if (tail.readUInt32LE(i) === 0x06054b50) {
                end = i;
                break;
            }
        if (end < 0)
            throw Error('Invalid ZIP ' + file);
        // EOCD fields hold the entry count, directory offset and byte length.
        // Sentinel counts/offsets require a ZIP64 reader instead of this parser.
        const n = tail.readUInt16LE(end + 10), offset = tail.readUInt32LE(end + 16), length = tail.readUInt32LE(end + 12);
        if (n === 65535 || offset === 0xffffffff)
            throw Error('ZIP64 archive requires a ZIP64-aware verifier: ' + file);

        // Central-directory records contain 46 fixed bytes, followed by a
        // filename, extra fields and comment. Retain compression details and
        // the local-header offset needed to retrieve each payload later.
        const central = this.read(offset, length);
        let at = 0;
        for (let i = 0; i < n; i++) {
            if (central.readUInt32LE(at) !== 0x02014b50)
                throw Error('Invalid ZIP directory');
            const nl = central.readUInt16LE(at + 28), el = central.readUInt16LE(at + 30), cl = central.readUInt16LE(at + 32), name = central.subarray(at + 46, at + 46 + nl).toString('utf8');
            // Duplicate paths may hide overwritten classes/resources. Record
            // the error even though the map retains the final matching record.
            if (this.entries.has(name))
                errors.push(file + ': duplicate ZIP entry ' + name);
            this.entries.set(name, {
                method: central.readUInt16LE(at + 10),
                compressed: central.readUInt32LE(at + 20),
                size: central.readUInt32LE(at + 24),
                offset: central.readUInt32LE(at + 42)
            });
            at += 46 + nl + el + cl;
        }
    }

    /**
     * Read exactly size bytes at an absolute archive offset.
     *
     * Synchronous reads can be short. Continue until the buffer is full, and
     * treat unexpected EOF as archive truncation rather than returning bad data.
     *
     * @param {number} offset Absolute byte offset.
     * @param {number} size Required byte count.
     * @returns {Buffer} Complete requested range.
     * @throws {Error} On premature EOF or an underlying file read failure.
     */
    read(offset, size) {
        const b = Buffer.alloc(size);
        let n = 0;
        while (n < size) {
            const got = fs.readSync(this.fd, b, n, size - n, offset + n);
            if (!got)
                throw Error('Truncated ZIP ' + this.file);
            n += got;
        }
        return b;
    }

    /**
     * Retrieve an uncompressed entry, or null when its path is absent.
     *
     * A local header has 30 fixed bytes. Its filename/extra-field lengths locate
     * the payload; compressed size comes from the central directory, including
     * entries that use a data descriptor rather than sizes in the local header.
     *
     * @param {string} name Forward-slash-separated entry path.
     * @returns {Buffer|null} Decoded bytes, or null if absent.
     * @throws {Error} For unsupported compression or a corrupt payload.
     */
    data(name) {
        const e = this.entries.get(name);
        if (!e)
            return null;
        const h = this.read(e.offset, 30), data = this.read(e.offset + 30 + h.readUInt16LE(26) + h.readUInt16LE(28), e.compressed);
        return e.method === 0 ? data : e.method === 8 ? zlib.inflateRawSync(data) : (() => {
            throw Error('Unsupported ZIP compression');
        })();
    }

    /**
     * Release this reader's descriptor after its final archive entry is read.
     *
     * @returns {void}
     */
    close() {
        fs.closeSync(this.fd);
    }
}

/**
 * Enumerate files recursively in deterministic filename order.
 *
 * Missing optional source/resource directories produce an empty inventory.
 * Required JARs are opened separately and fail immediately when absent.
 *
 * @param {string} dir Directory to enumerate.
 * @returns {string[]} File paths below the directory.
 */
function walk(dir) {
    if (!fs.existsSync(dir))
        return [];
    return fs.readdirSync(dir, { withFileTypes: true }).sort((a, b) => a.name.localeCompare(b.name)).flatMap(e => e.isDirectory() ? walk(path.join(dir, e.name)) : [path.join(dir, e.name)]);
}

// bus-all packages the Bus components, including transitive dependencies.
// bus-opencv is shipped separately alongside the aggregate.
const allFile = path.join(root, 'bus-all/target', `bus-all-${ version }.jar`), all = new Zip(allFile);
const allSources = new Zip(path.join(root, 'bus-all/target', `bus-all-${ version }-sources.jar`));
const aggregatedModules = new Set(modules.filter(m => ![
    'bus-all',
    'bus-bom',
    'bus-opencv',
    'bus-parent'
].includes(m)));

// These counters describe comparisons, not a new compilation. Report the
// observed class major version together with the fixed Java 21 ceiling.
let compiledClasses = 0, aggregatedClasses = 0, metadataFiles = 0, maxClassMajor = 0, sourceFiles = 0, aggregatedSourceFiles = 0;

// Verify each component independently before checking its aggregate coverage.
// Parent/BOM modules publish POMs and are validated separately below.
for (const module of modules) {
    if ([
            'bus-bom',
            'bus-parent'
        ].includes(module))
        continue;
    const file = path.join(root, module, 'target', `${ module }-${ version }.jar`), zip = new Zip(file);
    // Whole-file hashes catch installed copies from a different build even
    // when their Maven versions and filenames match current target artifacts.
    const installed = path.join(process.env.HOME, '.m2/repository/org/miaixz', module, version, `${ module }-${ version }.jar`);
    const bytes = fs.readFileSync(file);
    if (!fs.existsSync(installed) || hash(bytes) !== hash(fs.readFileSync(installed)))
        errors.push(module + ': installed JAR differs from this build');

    // Every current Java file must match its component source JAR entry. For
    // included modules, bus-all's source JAR must match too; module-info.java
    // is deliberately exempt from the aggregate-source requirement.
    const sourceJar = new Zip(path.join(root, module, 'target', `${ module }-${ version }-sources.jar`));
    const javaRoot = path.join(root, module, 'src/main/java');
    if (module !== 'bus-all')
        for (const source of walk(javaRoot).filter(f => f.endsWith('.java'))) {
            const name = path.relative(javaRoot, source).split(path.sep).join('/'), expected = hash(fs.readFileSync(source)), actual = sourceJar.data(name);
            if (!actual || hash(actual) !== expected)
                errors.push(module + ': source JAR differs: ' + name);
            sourceFiles++;
            if (aggregatedModules.has(module) && path.basename(name) !== 'module-info.java') {
                const entry = allSources.data(name);
                if (!entry || hash(entry) !== expected)
                    errors.push(module + ': bus-all source JAR differs: ' + name);
                aggregatedSourceFiles++;
            }
        }
    sourceJar.close();

    // Compare target/classes bytes with component and aggregate JAR entries.
    // The major-version field at class-file offset 6 must be <= 65 (Java 21).
    // module-info.class is excluded from class and aggregation comparisons.
    let classes = 0;
    const prefix = path.join(root, module, 'target/classes') + path.sep;
    for (const cls of walk(prefix).filter(f => f.endsWith('.class') && path.basename(f) !== 'module-info.class')) {
        const name = path.relative(prefix, cls).split(path.sep).join('/'), raw = fs.readFileSync(cls), expected = hash(raw), actual = zip.data(name);
        maxClassMajor = Math.max(maxClassMajor, raw.readUInt16BE(6));
        if (raw.readUInt16BE(6) > 65)
            errors.push(module + ': class exceeds Java 21 bytecode: ' + name);
        if (!actual || hash(actual) !== expected)
            errors.push(module + ': component JAR class differs: ' + name);
        classes++;
        compiledClasses++;
        if (aggregatedModules.has(module)) {
            const data = all.data(name);
            if (!data || hash(data) !== expected)
                errors.push(module + ': bus-all class differs: ' + name);
            aggregatedClasses++;
        }
    }

    // Indexes, JSON and properties must be packaged byte-for-byte as authored.
    // Collect JSON rows for semantic aggregate checks: shading may merge source
    // documents while preserving every required registration.
    const source = path.join(root, module, 'src/main/resources/META-INF/native-image');
    for (const f of walk(source)) {
        const name = 'META-INF/native-image/' + path.relative(source, f).split(path.sep).join('/'), actual = zip.data(name);
        if (!actual || hash(actual) !== hash(fs.readFileSync(f)))
            errors.push(module + ': component native metadata differs: ' + name);
        metadataFiles++;
        if (f.endsWith('/reachability-metadata.json')) {
            const d = JSON.parse(fs.readFileSync(f));
            for (const [section, rows] of Object.entries(d))
                if (Array.isArray(rows))
                    for (const row of rows)
                        sourceRows.push({
                            module,
                            section,
                            row
                        });
        }
    }

    // Retain per-module evidence in the report and close this component reader.
    artifacts.push({
        module,
        sha256: hash(bytes),
        classes,
        aggregated: aggregatedModules.has(module),
        bytes: bytes.length
    });
    zip.close();
}

// Inventory actual bus-all metadata. Type sections are indexed by section and
// declaration target; retain multiple candidate rows when conditions differ.
// Resource rows and initialization properties are checked independently.
const exported = new Map(), exportedResources = [], exportedProps = [];
let exportedFiles = 0;
for (const name of all.entries.keys()) {
    if (!name.includes('META-INF/native-image/'))
        continue;
    if (name.endsWith('/native-image.properties'))
        exportedProps.push(all.data(name).toString('utf8'));
    if (!name.endsWith('/reachability-metadata.json'))
        continue;
    exportedFiles++;
    const d = JSON.parse(all.data(name));
    for (const [section, rows] of Object.entries(d))
        if (Array.isArray(rows))
            for (const row of rows) {
                if ([
                        'reflection',
                        'serialization',
                        'jni'
                    ].includes(section)) {
                    const k = section + ':' + key(row.type || row.name), xs = exported.get(k) || [];
                    xs.push(row);
                    exported.set(k, xs);
                } else
                    exportedResources.push({
                        section,
                        row
                    });
            }
}

/**
 * Compare an exported member with the corresponding source requirement.
 *
 * Match names and ordered parameter types. Every boolean flag required as true
 * by the source must remain true; false flags do not narrow a broader export.
 * Other member attributes are outside this helper's comparison policy.
 *
 * @param {Object} a Candidate member exported in bus-all.
 * @param {Object} b Required member from source metadata.
 * @returns {boolean} Whether the compared member requirements are retained.
 */
function sameMember(a, b) {
    return a.name === b.name && key(a.parameterTypes || []) === key(b.parameterTypes || []) && Object.entries(b).every(([k, v]) => typeof v !== 'boolean' || !v || a[k] === true);
}

// Only modules listed for aggregation require bus-all coverage. An unconditional
// type registration covers a conditional source row; otherwise the condition
// must match exactly. Required members and true capability flags must survive.
// Resource sections require an exact normalized row, including conditions;
// coverage by a broader glob is not inferred by this verifier.
for (const {module, section, row} of sourceRows) {
    if (!aggregatedModules.has(module))
        continue;
    if ([
            'reflection',
            'serialization',
            'jni'
        ].includes(section)) {
        const candidates = (exported.get(section + ':' + key(row.type || row.name)) || []).filter(c => !c.condition || key(c.condition) === key(row.condition));
        if (!candidates.length) {
            errors.push('bus-all lost type ' + key(row.type || row.name));
            continue;
        }
        // Accept explicit members or broad allDeclared/allPublic flags under
        // the existing verification policy. This does not independently prove
        // member visibility or runtime GraalVM reflection behavior.
        for (const group of [
                'fields',
                'methods',
                'queriedMethods'
            ])
            for (const member of row[group] || []) {
                const covered = candidates.some(c => (group === 'fields' ? c.allDeclaredFields || c.allPublicFields : member.name === '<init>' ? c.allDeclaredConstructors || c.allPublicConstructors : c.allDeclaredMethods || c.allPublicMethods) || (c[group] || []).some(x => sameMember(x, member)));
                if (!covered)
                    errors.push('bus-all lost member ' + key(row.type || row.name) + '.' + member.name);
            }
        for (const [flag, value] of Object.entries(row))
            if (typeof value === 'boolean' && value && !candidates.some(c => c[flag]))
                errors.push('bus-all lost flag ' + key(row.type || row.name) + '.' + flag);
    } else if (!exportedResources.some(x => x.section === section && key(x.row) === key(row)))
        errors.push('bus-all lost resource ' + key(row));
}

// Check property arguments separately: join continuation lines, then locate
// initialization targets and -H: options in exported property text. Presence
// checking does not detect initialization-mode conflicts or option equivalence.
for (const module of aggregatedModules)
    for (const f of walk(path.join(root, module, 'src/main/resources/META-INF/native-image')).filter(f => f.endsWith('/native-image.properties'))) {
        const text = fs.readFileSync(f, 'utf8').replace(/\\\r?\n\s*/g, ' ');
        for (const match of text.matchAll(/--initialize-at-(build|run)-time=([^\s]+)/g))
            for (const type of match[2].split(','))
                if (!exportedProps.some(p => p.includes(type)))
                    errors.push('bus-all lost initialization ' + type);
        for (const option of text.matchAll(/(?:^|\s)(-H:[^\s]+)/g))
            if (!exportedProps.some(p => p.includes(option[1])))
                errors.push('bus-all lost native option ' + option[1]);
    }

// Parent/BOM artifacts have no JAR. Their installed publication POMs must not
// retain an unresolved CI-friendly revision placeholder.
for (const module of [
        'bus-bom',
        'bus-parent'
    ]) {
    const installed = path.join(process.env.HOME, '.m2/repository/org/miaixz', module, version, `${ module }-${ version }.pom`), text = fs.readFileSync(installed, 'utf8');
    if (text.includes('${revision}'))
        errors.push(module + ': published POM contains unresolved revision');
}

// Release aggregate readers after the last comparison. The report includes
// hashes/counts rather than dumping class, source or metadata file contents.
all.close();
allSources.close();

// Emit a single report and set a failing process status when mismatches exist.
// A JSON FAIL value alone would not fail a shell caller or CI step.
console.log(JSON.stringify({
    status: errors.length ? 'FAIL' : 'PASS',
    version,
    artifacts: artifacts.length,
    compiler_release: 21,
    max_class_major: maxClassMajor,
    compiled_classes_verified: compiledClasses,
    aggregated_classes_verified: aggregatedClasses,
    source_files_verified: sourceFiles,
    aggregated_source_files_verified: aggregatedSourceFiles,
    component_native_files_verified: metadataFiles,
    bus_all_native_metadata_files: exportedFiles,
    bus_all_sha256: hash(fs.readFileSync(allFile)),
    separate_artifacts: artifacts.filter(a => !a.aggregated && a.module !== 'bus-all').map(a => a.module),
    errors,
    artifact_details: artifacts
}, null, 2));
if (errors.length)
    process.exitCode = 1;
