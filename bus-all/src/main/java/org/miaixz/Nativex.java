/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * GraalVM Native Image Configuration Consolidator
 *
 * An enterprise-grade Java implementation for cross-platform consolidation of GraalVM Native Image configuration
 * artifacts. This sophisticated utility dynamically discovers and merges all native-image configuration files from
 * multiple bus-* modules into a unified bus-all distribution package.
 *
 * Core Capabilities: - Dynamic discovery and consolidation of all configuration file types - Intelligent artifact
 * merging with advanced deduplication algorithms - Adaptive JSON structure preservation and validation - Cross-platform
 * compatibility (Windows, macOS, Linux) - Automatic project metadata detection and version management - Extensible
 * architecture supporting arbitrary configuration types
 *
 * Architecture: This consolidator follows a modular design pattern with clear separation of concerns: - Discovery
 * Layer: Dynamic project and artifact detection - Processing Layer: Configuration parsing and merging logic -
 * Validation Layer: Structure preservation and integrity checks - Output Layer: Standardized file generation and
 * metadata management
 *
 * Usage Examples:
 * 
 * <pre>
 * // Maven integration (recommended):
 * {@code <java classname="org.miaixz.Nativex"
 *           classpath="${project.build.outputDirectory}"
 *           fork="true"
 *           failonerror="false">
 *     <arg value="${project.version}" />
 * </java>}
 *
 * // Direct execution:
 * {@code
 * java - cp < classpath > org.miaixz.Nativex[version]
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 * @see <a href="https://graalvm.org/latest/reference/native-image/BuildConfiguration/">GraalVM Native Image
 *      Configuration</a>
 */
public class Nativex {

    /**
     * Relative path to project root directory where bus modules are located
     */
    private static final String PROJECT_ROOT_DIRECTORY = "..";

    /**
     * Standard Maven POM file name
     */
    private static final String MAVEN_POM_FILE = "pom.xml";

    /**
     * Base output directory for consolidated native-image configurations
     */
    private final String outputBaseDirectory;

    /**
     * Fully qualified module identifier for metadata
     */
    private final String moduleIdentifier;

    /**
     * Constructs a new Nativex with automatic project metadata detection.
     *
     * @throws IOException if project metadata cannot be detected
     */
    public Nativex() throws IOException {
        Metadata metadata = detectProjectMetadata();
        this.moduleIdentifier = metadata.getGroupId() + ":" + metadata.getArtifactId();
        this.outputBaseDirectory = "target/classes/META-INF/native-image/" + this.moduleIdentifier.replace(":", "/");
    }

    /**
     * Main entry point for the configuration consolidation process.
     *
     * @param args command line arguments. First optional argument is the target version. If no version is specified, it
     *             will be auto-detected from pom.xml.
     * @throws Exception if configuration consolidation fails
     */
    public static void main(String[] args) throws Exception {
        String targetVersion = args.length > 0 ? args[0] : detectTargetVersion();
        if (targetVersion == null) {
            System.err.println("Error: Cannot determine project version");
            System.exit(1);
        }

        System.out.println("GraalVM Native Image Configuration Consolidator");

        Nativex nativex = new Nativex();
        nativex.execute(targetVersion);

        System.out.println("Configuration consolidation completed successfully!");
    }

    /**
     * Executes the complete configuration consolidation workflow.
     *
     * @param targetVersion the target version to process, or null to process all versions
     * @throws IOException if file operations fail
     */
    public void execute(String targetVersion) throws IOException {
        Set<String> allVersions = discoverAvailableVersions();
        System.out.println("Discovered versions: " + String.join(", ", allVersions));

        Set<String> versionsToProcess = determineVersionsToProcess(allVersions, targetVersion);

        for (String version : versionsToProcess) {
            System.out.println("=== Processing version " + version + " ===");
            processVersion(version);
        }

        generateTopIndex(versionsToProcess);
    }

    /**
     * Discovers all available versions by scanning for version directories across all modules.
     *
     * @return a sorted set of version strings (highest version first)
     * @throws IOException if file system access fails
     */
    private Set<String> discoverAvailableVersions() throws IOException {
        Set<String> versions = new LinkedHashSet<>();
        Path projectRoot = Paths.get(PROJECT_ROOT_DIRECTORY);

        try (java.util.stream.Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(Files::isDirectory).filter(path -> path.toString().contains("native-image")).forEach(path -> {
                String name = path.getFileName().toString();
                if (name.matches("^\\d+(\\.\\d+)*$")) {
                    versions.add(name);
                }
            });
        }

        return versions.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Processes a specific version by consolidating all configuration files for that version.
     *
     * @param version the version to process
     * @throws IOException if file operations fail
     */
    private void processVersion(String version) throws IOException {
        String outputDir = outputBaseDirectory + "/" + version;
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        System.out.println("Consolidating native-image configurations for version " + version + "...");

        Set<String> configurationTypes = discoverConfigurationFileTypes(version);
        System.out.println("Discovered configuration types: " + String.join(", ", configurationTypes));

        for (String configType : configurationTypes) {
            List<Path> configFiles = locateConfigurationFiles(version, configType);

            if (configFiles.isEmpty()) {
                System.out.println("No " + configType + " files discovered");
                continue;
            }

            String consolidatedContent = consolidateConfigurationFiles(configFiles, configType);
            Files.writeString(outputPath.resolve(configType), consolidatedContent);

            System.out.println("Consolidated " + configFiles.size() + " " + configType + " files");
        }

        generateVersionIndex(outputPath.resolve("index.json"), configurationTypes);

        // Generate consolidation statistics
        displayConsolidationStatistics(version, outputPath, configurationTypes);
    }

    /**
     * Discovers all configuration file types for a specific version.
     *
     * @param version the version to scan for configuration files
     * @return a sorted set of configuration file names (alphabetical order)
     * @throws IOException if file system access fails
     */
    private Set<String> discoverConfigurationFileTypes(String version) throws IOException {
        Set<String> configTypes = new LinkedHashSet<>();
        Path projectRoot = Paths.get(PROJECT_ROOT_DIRECTORY);

        try (java.util.stream.Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().contains("native-image"))
                    .filter(path -> path.toString().contains(version)).filter(path -> !shouldExcludeModule(path))
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(path -> !path.getFileName().toString().equals("index.json"))
                    .forEach(path -> configTypes.add(path.getFileName().toString()));
        }

        return configTypes.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Locates all configuration files of a specific type for a given version.
     *
     * @param version        the version to search for
     * @param configFileName the configuration file name to locate
     * @return a list of paths to the discovered configuration files
     * @throws IOException if file system access fails
     */
    private List<Path> locateConfigurationFiles(String version, String configFileName) throws IOException {
        List<Path> files = new ArrayList<>();
        Path projectRoot = Paths.get(PROJECT_ROOT_DIRECTORY);

        try (java.util.stream.Stream<Path> paths = Files.walk(projectRoot)) {
            paths.filter(Files::isRegularFile).filter(path -> path.toString().contains("native-image"))
                    .filter(path -> path.toString().contains(version))
                    .filter(path -> path.getFileName().toString().equals(configFileName))
                    .filter(path -> !shouldExcludeModule(path)).forEach(files::add);
        }

        return files;
    }

    /**
     * Determines if a module path should be excluded from processing. Excludes the current module and aggregated
     * modules to prevent circular dependencies.
     *
     * @param path the path to evaluate
     * @return true if the path should be excluded, false otherwise
     */
    private boolean shouldExcludeModule(Path path) {
        String pathStr = path.toString().replace(File.separatorChar, '/');
        return pathStr.contains("bus-all") || pathStr.contains("bus-bom");
    }

    /**
     * Consolidates configuration files using appropriate merging strategy.
     *
     * @param files      list of configuration files to consolidate
     * @param configType the type/name of the configuration file
     * @return consolidated configuration content as a string
     */
    private String consolidateConfigurationFiles(List<Path> files, String configType) {
        if (configType.contains("resource-config")) {
            return consolidateResourceConfigurations(files);
        } else {
            // Default to array merging for most JSON configuration files
            return consolidateJsonArrayConfigurations(files);
        }
    }

    /**
     * Consolidates JSON array configuration files with deduplication and structure preservation. Handles
     * reflect-config.json, proxy-config.json, and other array-based configurations.
     *
     * @param files list of JSON configuration files to consolidate
     * @return consolidated JSON array as a string
     */
    private String consolidateJsonArrayConfigurations(List<Path> files) {
        List<String> uniqueObjects = new ArrayList<>();
        Set<String> processedKeys = new HashSet<>();

        for (Path file : files) {
            try {
                String content = Files.readString(file).trim();

                // Remove outer array brackets if present
                if (content.startsWith("[") && content.endsWith("]")) {
                    content = content.substring(1, content.length() - 1).trim();
                }

                if (!content.isEmpty()) {
                    List<String> objects = extractJsonObjects(content);
                    for (String obj : objects) {
                        obj = obj.trim();
                        if (obj.startsWith("{") && obj.endsWith("}")) {
                            String key = generateUniqueObjectKey(obj);
                            if (!processedKeys.contains(key)) {
                                uniqueObjects.add(obj);
                                processedKeys.add(key);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not process " + file + ": " + e.getMessage());
            }
        }

        return formatJsonArray(uniqueObjects);
    }

    /**
     * Extracts individual JSON objects from a JSON string content.
     *
     * @param content the JSON content containing multiple objects
     * @return list of individual JSON object strings
     */
    private List<String> extractJsonObjects(String content) {
        List<String> objects = new ArrayList<>();
        StringBuilder currentObject = new StringBuilder();
        boolean inObject = false;
        int braceDepth = 0;
        boolean inString = false;
        int index = 0;

        while (index < content.length()) {
            char character = content.charAt(index);

            // Handle string boundary detection
            if (character == '"' && (index == 0 || content.charAt(index - 1) != '\\')) {
                inString = !inString;
            }

            // Detect object start
            if (character == '{' && !inString && !inObject) {
                inObject = true;
                braceDepth = 0;
                currentObject.setLength(0);
            }

            if (inObject) {
                currentObject.append(character);
                if (!inString) {
                    if (character == '{') {
                        braceDepth++;
                    } else if (character == '}') {
                        braceDepth--;
                    }
                }

                // Object completion detection
                if (braceDepth == 0 && character == '}') {
                    String objectStr = currentObject.toString().trim();
                    if (!objectStr.isEmpty()) {
                        objects.add(objectStr);
                    }
                    inObject = false;
                }
            }
            index++;
        }

        return objects;
    }

    /**
     * Generates a unique key for a JSON object for deduplication purposes. Uses normalized JSON content to ensure
     * consistent comparison.
     *
     * @param jsonStr the JSON object string
     * @return a unique key string for the JSON object
     */
    private String generateUniqueObjectKey(String jsonStr) {
        String normalizedJson = normalizeJsonForComparison(jsonStr);
        return "hash:" + normalizedJson.hashCode();
    }

    /**
     * Normalizes JSON content for consistent comparison by removing formatting differences.
     *
     * @param jsonStr the JSON string to normalize
     * @return normalized JSON string in lowercase
     */
    private String normalizeJsonForComparison(String jsonStr) {
        StringBuilder normalized = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < jsonStr.length(); i++) {
            char character = jsonStr.charAt(i);

            if (escaped) {
                normalized.append(character);
                escaped = false;
                continue;
            }

            if (character == '\\') {
                normalized.append(character);
                escaped = true;
                continue;
            }

            if (character == '"') {
                inString = !inString;
                normalized.append(character);
                continue;
            }

            if (inString) {
                normalized.append(character);
            } else {
                // Skip whitespace outside of strings
                if (!Character.isWhitespace(character)) {
                    normalized.append(character);
                }
            }
        }

        return normalized.toString().toLowerCase();
    }

    /**
     * Formats a list of JSON objects into a properly formatted JSON array string.
     *
     * @param objects list of JSON object strings
     * @return formatted JSON array string
     */
    private String formatJsonArray(List<String> objects) {
        StringBuilder jsonArray = new StringBuilder();
        jsonArray.append("[\n");

        for (int i = 0; i < objects.size(); i++) {
            jsonArray.append("  ").append(objects.get(i));
            if (i < objects.size() - 1) {
                jsonArray.append(",");
            }
            jsonArray.append("\n");
        }

        jsonArray.append("]");
        return jsonArray.toString();
    }

    /**
     * Project metadata holder class for Java 8 compatibility.
     */
    private static class Metadata {

        private final String groupId;
        private final String artifactId;
        private final String version;

        public Metadata(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }

        public String getGroupId() {
            return groupId;
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }
    }

    /**
     * Detects project metadata from the Maven POM file.
     *
     * @return Metadata containing groupId, artifactId, and version
     * @throws IOException if metadata cannot be detected
     */
    private static Metadata detectProjectMetadata() throws IOException {
        Path pomPath = Paths.get(MAVEN_POM_FILE);
        if (!Files.exists(pomPath)) {
            throw new IOException("Maven POM file not found: " + pomPath);
        }

        String content = Files.readString(pomPath);

        Pattern groupIdPattern = Pattern.compile("<groupId>([^<]+)</groupId>");
        Pattern artifactIdPattern = Pattern.compile("<artifactId>bus-all</artifactId>\\s*<version>([^<]+)</version>");

        Matcher groupIdMatcher = groupIdPattern.matcher(content);
        Matcher artifactIdMatcher = artifactIdPattern.matcher(content);

        if (!groupIdMatcher.find() || !artifactIdMatcher.find()) {
            throw new IOException("Could not extract project metadata from POM file");
        }

        return new Metadata(groupIdMatcher.group(1).trim(), "bus-all", artifactIdMatcher.group(1).trim());
    }

    /**
     * Detects the target version from the Maven POM file.
     *
     * @return the detected version string, or null if detection fails
     */
    private static String detectTargetVersion() {
        try {
            Metadata metadata = detectProjectMetadata();
            return metadata.getVersion();
        } catch (IOException e) {
            System.err.println("Warning: Could not detect project version: " + e.getMessage());
            return null;
        }
    }

    /**
     * Determines which versions should be processed based on target version and available versions.
     *
     * @param allVersions   all discovered versions
     * @param targetVersion specific target version, or null for all versions
     * @return set of versions to process
     */
    private Set<String> determineVersionsToProcess(Set<String> allVersions, String targetVersion) {
        if (allVersions.contains(targetVersion)) {
            return Set.of(targetVersion);
        } else if (targetVersion != null) {
            System.out.println("Warning: Version " + targetVersion + " not found, processing all available versions");
            return allVersions;
        } else {
            return allVersions;
        }
    }

    /**
     * Consolidates resource configuration files with adaptive structure preservation. Sorts resource patterns by Java
     * package alphabetical order.
     *
     * @param files list of resource-config.json files to consolidate
     * @return consolidated resource configuration as a string
     */
    private String consolidateResourceConfigurations(List<Path> files) {
        Set<String> allIncludes = new LinkedHashSet<>();
        Set<String> allBundles = new LinkedHashSet<>();
        boolean foundExistingIncludes = false;

        // Analyze all files for structure and content
        for (Path file : files) {
            try {
                String content = Files.readString(file);

                if (content.contains("\"includes\"")) {
                    foundExistingIncludes = true;
                    extractResourcePatterns(content, allIncludes);
                }

                if (content.contains("\"bundles\"")) {
                    extractBundles(content, allBundles);
                }
            } catch (IOException e) {
                System.err.println("Warning: Could not analyze " + file + ": " + e.getMessage());
            }
        }

        if (foundExistingIncludes) {
            return mergeExistingResourceConfigurations(allIncludes, allBundles);
        } else {
            return createNewResourceConfiguration(allIncludes, allBundles);
        }
    }

    /**
     * Extracts resource patterns from configuration file content. Only extracts patterns from the "includes" array, not
     * from "bundles".
     *
     * @param content     the configuration file content
     * @param allIncludes set to store extracted patterns
     */
    private void extractResourcePatterns(String content, Set<String> allIncludes) {
        // Find the "includes" array
        int includesStart = content.indexOf("\"includes\"");
        if (includesStart == -1)
            return;

        int arrayStart = content.indexOf("[", includesStart);
        if (arrayStart == -1)
            return;

        int arrayEnd = findMatchingBracket(content, arrayStart);
        if (arrayEnd == -1)
            return;

        String includesArray = content.substring(arrayStart, arrayEnd + 1);

        // Extract pattern values from the includes array
        Pattern patternRegex = Pattern.compile("\"pattern\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = patternRegex.matcher(includesArray);

        while (matcher.find()) {
            String pattern = matcher.group(1);
            if (!pattern.isEmpty()) {
                allIncludes.add(pattern);
            }
        }
    }

    /**
     * Finds the matching closing bracket for an opening bracket.
     *
     * @param content the content to search
     * @param openPos position of the opening bracket
     * @return position of the matching closing bracket, or -1 if not found
     */
    private int findMatchingBracket(String content, int openPos) {
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = openPos; i < content.length(); i++) {
            char c = content.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString) {
                if (c == '[') {
                    depth++;
                } else if (c == ']') {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Extracts bundle objects from configuration file content.
     *
     * @param content    the configuration file content
     * @param allBundles set to store extracted bundle objects
     */
    private void extractBundles(String content, Set<String> allBundles) {
        // Find the "bundles" array
        int bundlesStart = content.indexOf("\"bundles\"");
        if (bundlesStart == -1)
            return;

        int arrayStart = content.indexOf("[", bundlesStart);
        if (arrayStart == -1)
            return;

        int arrayEnd = findMatchingBracket(content, arrayStart);
        if (arrayEnd == -1)
            return;

        String bundlesArray = content.substring(arrayStart + 1, arrayEnd).trim();
        if (bundlesArray.isEmpty())
            return;

        // Extract complete bundle objects (keep original indentation for now)
        List<String> bundleObjects = extractJsonObjects(bundlesArray);
        allBundles.addAll(bundleObjects);
    }

    /**
     * Merges existing resource-config.json files while preserving original structure. Currently simplified to use new
     * creation approach.
     *
     * @param allIncludes all discovered resource patterns
     * @param allBundles  all discovered bundle objects
     * @return merged resource configuration
     */
    private String mergeExistingResourceConfigurations(Set<String> allIncludes, Set<String> allBundles) {
        // Simplified approach: use new creation for now
        return createNewResourceConfiguration(allIncludes, allBundles);
    }

    /**
     * Creates a new resource-config.json file with standard structure.
     *
     * @param allIncludes resource patterns to include
     * @param allBundles  bundle objects to include
     * @return formatted resource configuration
     */
    private String createNewResourceConfiguration(Set<String> allIncludes, Set<String> allBundles) {
        List<String> sortedIncludes = new ArrayList<>(allIncludes);
        sortedIncludes.sort(this::compareByJavaPackage);

        StringBuilder configBuilder = new StringBuilder();
        configBuilder.append("{\n");
        configBuilder.append("  \"resources\": {\n");
        configBuilder.append("    \"includes\": [\n");

        for (int i = 0; i < sortedIncludes.size(); i++) {
            if (i > 0) {
                configBuilder.append(",\n");
            }
            configBuilder.append("      {\n");
            configBuilder.append("        \"pattern\": \"").append(sortedIncludes.get(i)).append("\"\n");
            configBuilder.append("      }");
        }

        configBuilder.append("\n    ]\n");
        configBuilder.append("  }");

        if (!allBundles.isEmpty()) {
            configBuilder.append(",\n  \"bundles\": [\n");
            int i = 0;
            for (String bundle : allBundles) {
                if (i > 0) {
                    configBuilder.append(",\n");
                }
                // Format bundle with standard 2-space indentation
                // Bundle object starts at 4 spaces (2 levels: bundles array + object)
                configBuilder.append(formatBundleObject(bundle));
                i++;
            }
            configBuilder.append("\n  ]");
        }

        configBuilder.append("\n}");
        return configBuilder.toString();
    }

    /**
     * Formats a bundle object with proper 2-space indentation. Ensures consistent formatting matching the rest of the
     * file.
     *
     * @param bundle the bundle JSON object to format
     * @return properly formatted bundle string
     */
    private String formatBundleObject(String bundle) {
        // Parse the bundle to extract name and locales
        String name = extractBundleName(bundle);
        List<String> locales = extractBundleLocales(bundle);

        StringBuilder result = new StringBuilder();
        result.append("    {\n"); // 4 spaces - bundle object start
        result.append("      \"name\": \"").append(name).append("\",\n"); // 6 spaces
        result.append("      \"locales\": [\n"); // 6 spaces

        for (int i = 0; i < locales.size(); i++) {
            result.append("        \"").append(locales.get(i)).append("\""); // 8 spaces
            if (i < locales.size() - 1) {
                result.append(",");
            }
            result.append("\n");
        }

        result.append("      ]\n"); // 6 spaces
        result.append("    }"); // 4 spaces - bundle object end

        return result.toString();
    }

    /**
     * Extracts the bundle name from a bundle JSON object.
     *
     * @param bundle the bundle JSON string
     * @return the bundle name
     */
    private String extractBundleName(String bundle) {
        Pattern pattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(bundle);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "unknown";
    }

    /**
     * Extracts the locales array from a bundle JSON object.
     *
     * @param bundle the bundle JSON string
     * @return list of locale strings
     */
    private List<String> extractBundleLocales(String bundle) {
        List<String> locales = new ArrayList<>();

        // Find the locales array
        int localesStart = bundle.indexOf("\"locales\"");
        if (localesStart == -1)
            return locales;

        int arrayStart = bundle.indexOf("[", localesStart);
        if (arrayStart == -1)
            return locales;

        int arrayEnd = findMatchingBracket(bundle, arrayStart);
        if (arrayEnd == -1)
            return locales;

        String localesArray = bundle.substring(arrayStart + 1, arrayEnd);

        // Extract all quoted strings from the array
        Pattern pattern = Pattern.compile("\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(localesArray);

        while (matcher.find()) {
            locales.add(matcher.group(1));
        }

        return locales;
    }

    /**
     * Compares two resource paths by Java package alphabetical order.
     *
     * @param path1 the first resource path
     * @param path2 the second resource path
     * @return negative if path1 < path2, zero if equal, positive if path1 > path2
     */
    private int compareByJavaPackage(String path1, String path2) {
        String pkg1 = extractJavaPackageName(path1);
        String pkg2 = extractJavaPackageName(path2);

        int packageComparison = pkg1.compareTo(pkg2);
        if (packageComparison != 0) {
            return packageComparison;
        }

        return path1.compareTo(path2);
    }

    /**
     * Extracts Java package name from a resource path for sorting purposes.
     *
     * @param path the resource path
     * @return extracted package name
     */
    private String extractJavaPackageName(String path) {
        path = path.replaceFirst("^/", "");

        if (path.startsWith("META-INF/")) {
            return "META-INF";
        } else if (path.contains("/")) {
            String[] segments = path.split("/");
            if (segments.length >= 2) {
                return segments[0] + "." + segments[1];
            } else if (segments.length == 1) {
                return segments[0];
            }
        }

        return path;
    }

    /**
     * Generates a version index file listing all configuration file types.
     *
     * @param outputPath         the path where the index file should be written
     * @param configurationTypes set of configuration file types
     * @throws IOException if file writing fails
     */
    private void generateVersionIndex(Path outputPath, Set<String> configurationTypes) throws IOException {
        StringBuilder indexBuilder = new StringBuilder();
        indexBuilder.append("[\n");

        List<String> sortedTypes = configurationTypes.stream().sorted().toList();

        for (int i = 0; i < sortedTypes.size(); i++) {
            indexBuilder.append("  \"").append(sortedTypes.get(i)).append("\"");
            if (i < sortedTypes.size() - 1) {
                indexBuilder.append(",");
            }
            indexBuilder.append("\n");
        }
        indexBuilder.append("]");

        Files.writeString(outputPath, indexBuilder.toString());
    }

    /**
     * Generates the master metadata index file for the consolidated module.
     *
     * @param processedVersions set of versions that were actually processed
     * @throws IOException if file operations fail
     */
    private void generateTopIndex(Set<String> processedVersions) throws IOException {
        Set<String> allTestedVersions = collectAllTestedVersions(processedVersions);

        // metadata-version should be the latest actually processed version directory
        String latestVersion = processedVersions.stream().max(Comparator.naturalOrder()).orElse("unknown");

        // Generate default-for pattern: major.minor.* (e.g., "8.5.*" becomes "8\\.5\\.[0-9]+")
        String[] versionParts = latestVersion.split("\\.");
        String defaultForPattern;
        if (versionParts.length >= 2) {
            defaultForPattern = versionParts[0] + "\\\\." + versionParts[1] + "\\\\.[0-9]+";
        } else {
            defaultForPattern = latestVersion.replace(".", "\\\\.") + "\\\\.[0-9]+";
        }

        StringBuilder metadataBuilder = new StringBuilder();
        metadataBuilder.append("[\n");
        metadataBuilder.append("  {\n");
        metadataBuilder.append("    \"latest\": true,\n");
        metadataBuilder.append("    \"override\": true,\n");
        metadataBuilder.append("    \"module\": \"").append(moduleIdentifier).append("\",\n");
        metadataBuilder.append("    \"default-for\": \"").append(defaultForPattern).append("\",\n");
        metadataBuilder.append("    \"metadata-version\": \"").append(latestVersion).append("\",\n");
        metadataBuilder.append("    \"tested-versions\": [\n");

        List<String> sortedVersions = allTestedVersions.stream().sorted(Comparator.naturalOrder()).toList();

        for (int i = 0; i < sortedVersions.size(); i++) {
            metadataBuilder.append("      \"").append(sortedVersions.get(i)).append("\"");
            if (i < sortedVersions.size() - 1) {
                metadataBuilder.append(",");
            }
            metadataBuilder.append("\n");
        }

        metadataBuilder.append("    ]\n");
        metadataBuilder.append("  }\n");
        metadataBuilder.append("]");

        Path metadataPath = Paths.get(outputBaseDirectory, "index.json");
        Files.createDirectories(metadataPath.getParent());
        Files.writeString(metadataPath, metadataBuilder.toString());

        System.out.println("\nLatest version: " + latestVersion);
        System.out.println("All tested versions: " + String.join(", ", sortedVersions));
    }

    /**
     * Collects all tested versions from module index files.
     *
     * @param processedVersions set of versions that were processed
     * @return comprehensive set of all tested versions
     */
    private Set<String> collectAllTestedVersions(Set<String> processedVersions) {
        Set<String> allTestedVersions = new LinkedHashSet<>(processedVersions);

        try {
            Path projectRoot = Paths.get(PROJECT_ROOT_DIRECTORY);
            try (java.util.stream.Stream<Path> paths = Files.walk(projectRoot)) {
                paths.filter(Files::isRegularFile).filter(path -> path.toString().contains("native-image"))
                        .filter(path -> path.getFileName().toString().equals("index.json"))
                        .filter(path -> !shouldExcludeModule(path)).forEach(path -> {
                            try {
                                String content = Files.readString(path);
                                extractTestedVersions(content, allTestedVersions);
                            } catch (IOException e) {
                                System.err.println("Warning: Could not read " + path + ": " + e.getMessage());
                            }
                        });
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not scan for index.json files: " + e.getMessage());
        }

        return allTestedVersions;
    }

    /**
     * Extracts version numbers from tested-versions array in JSON content.
     *
     * @param content           the JSON content containing tested-versions
     * @param allTestedVersions set to add extracted versions to
     */
    private void extractTestedVersions(String content, Set<String> allTestedVersions) {
        Pattern pattern = Pattern.compile("\"tested-versions\"\\s*:\\s*\\[([^\\]]+)\\]");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String versionsStr = matcher.group(1);
            Pattern versionPattern = Pattern.compile("\"([0-9]+\\.[0-9]+\\.[0-9]+)\"");
            Matcher versionMatcher = versionPattern.matcher(versionsStr);

            while (versionMatcher.find()) {
                allTestedVersions.add(versionMatcher.group(1));
            }
        }
    }

    /**
     * Displays comprehensive consolidation statistics for a processed version.
     *
     * @param version            the version that was processed
     * @param outputPath         path to the generated configuration files
     * @param configurationTypes types of configuration files that were processed
     * @throws IOException if reading statistics fails
     */
    private void displayConsolidationStatistics(String version, Path outputPath, Set<String> configurationTypes)
            throws IOException {
        StringBuilder statistics = new StringBuilder();
        statistics.append("Version ").append(version).append(": ");

        for (String configType : configurationTypes) {
            Path configFile = outputPath.resolve(configType);
            if (Files.exists(configFile)) {
                String content = Files.readString(configFile);
                if (configType.contains("proxy-config")) {
                    statistics.append(countJsonArrayEntries(content)).append(" proxy configurations, ");
                } else if (configType.contains("reflect-config")) {
                    statistics.append(countJsonArrayEntries(content)).append(" reflection entries, ");
                } else if (configType.contains("resource-config")) {
                    statistics.append(countResourcePatterns(content)).append(" resource patterns, ");
                } else {
                    statistics.append(configType).append(" consolidated, ");
                }
            }
        }

        // Remove trailing comma and space
        if (statistics.length() > 2) {
            statistics.setLength(statistics.length() - 2);
        }

        System.out.println(statistics);
    }

    /**
     * Counts the number of entries in a JSON array.
     *
     * @param jsonContent the JSON array content
     * @return number of array entries
     */
    private int countJsonArrayEntries(String jsonContent) {
        return (int) jsonContent.chars().filter(ch -> ch == '{').count();
    }

    /**
     * Counts the number of resource patterns in resource-config.json.
     *
     * @param jsonContent the resource configuration content
     * @return number of resource patterns
     */
    private int countResourcePatterns(String jsonContent) {
        int includesIndex = jsonContent.indexOf("\"includes\"");
        if (includesIndex == -1)
            return 0;

        int arrayStart = jsonContent.indexOf("[", includesIndex);
        int arrayEnd = jsonContent.indexOf("]", arrayStart);
        if (arrayStart == -1 || arrayEnd == -1)
            return 0;

        String arrayContent = jsonContent.substring(arrayStart, arrayEnd + 1);
        return (int) arrayContent.chars().filter(ch -> ch == '"').count() / 2;
    }

}
