#!/bin/bash

#################################################################################
#                                                                               #
# The MIT License (MIT)                                                         #
#                                                                               #
# Copyright (c) 2015-2026 miaixz.org and other contributors.                    #
#                                                                               #
# Permission is hereby granted, free of charge, to any person obtaining a copy  #
# of this software and associated documentation files (the "Software"), to deal #
# in the Software without restriction, including without limitation the rights  #
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     #
# copies of the Software, and to permit persons to whom the Software is         #
# furnished to do so, subject to the following conditions:                      #
#                                                                               #
# The above copyright notice and this permission notice shall be included in    #
# all copies or substantial portions of the Software.                           #
#                                                                               #
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    #
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      #
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   #
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        #
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, #
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     #
# THE SOFTWARE.                                                                 #
#                                                                               #
#################################################################################

#-------------------------------------------------------------------
# Build and deploy modules to central repository following dependency order
#-------------------------------------------------------------------

set -e  # Exit immediately on error

# Global configuration
DEFAULT_JAVA_VERSION=21

# Color definitions
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Auto detect and setup JAVA_HOME
setup_java() {
    local java_version=${1:-$DEFAULT_JAVA_VERSION}
    log_info "Setting up Java $java_version environment..."

    # If JAVA_HOME is already set, verify it
    if [ -n "$JAVA_HOME" ] && [ -d "$JAVA_HOME" ]; then
        log_info "JAVA_HOME is already set to: $JAVA_HOME"
        if [ -x "$JAVA_HOME/bin/java" ]; then
            local detected_version=$("$JAVA_HOME/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
            if [ "$detected_version" = "$java_version" ]; then
                log_success "Java $java_version detected at: $JAVA_HOME"
                export JAVA_HOME
                export PATH="$JAVA_HOME/bin:$PATH"
                return 0
            else
                log_warning "Current Java version is $detected_version, need Java $java_version"
            fi
        fi
    fi

    # Try to find Java installation
    local java_home_patterns=(
        "/usr/lib/jvm/java-${java_version}-*"
        "/usr/lib/jvm/graalvm-ce-java${java_version}"
        "/usr/local/opt/openjdk@${java_version}"
        "/opt/homebrew/opt/openjdk@${java_version}"
        "/Library/Java/JavaVirtualMachines/openjdk-${java_version}.jdk/Contents/Home"
        "/Library/Java/JavaVirtualMachines/graalvm-ce-java${java_version}/Contents/Home"
        "$HOME/.sdkman/candidates/java/${java_version}.*"
    )

    # Try to detect Java using java command
    if command -v java >/dev/null 2>&1; then
        local java_exec=$(which java)
        local detected_version=$("$java_exec" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)

        if [ "$detected_version" = "$java_version" ]; then
            # Try to get the actual JAVA_HOME using java -XshowSettings
            local detected_home=$("$java_exec" -XshowSettings:properties -version 2>&1 | grep 'java.home' | awk '{print $3}')

            if [ -n "$detected_home" ] && [ -d "$detected_home" ]; then
                log_success "Found Java $java_version at: $detected_home"
                export JAVA_HOME="$detected_home"
                export PATH="$JAVA_HOME/bin:$PATH"
                return 0
            else
                log_warning "Java $java_version detected but JAVA_HOME path not clear, using system path"
                return 0
            fi
        fi
    fi

    # Search in common paths
    for pattern in "${java_home_patterns[@]}"; do
        for expanded_path in $pattern; do
            if [ -d "$expanded_path" ] && [ -x "$expanded_path/bin/java" ]; then
                local version=$("$expanded_path/bin/java" -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
                if [ "$version" = "$java_version" ]; then
                    log_success "Found Java $java_version at: $expanded_path"
                    export JAVA_HOME="$expanded_path"
                    export PATH="$JAVA_HOME/bin:$PATH"
                    return 0
                fi
            fi
        done
    done

    # If we can't find the requested Java version
    log_error "Java $java_version not found. Please install Java $java_version or set JAVA_HOME manually"
    log_error "You can install Java $java_version using:"
    log_error "  - Ubuntu/Debian: sudo apt install openjdk-${java_version}-jdk"
    log_error "  - macOS: brew install openjdk@${java_version}"
    log_error "  - SDKMAN: sdk install java ${java_version}.0.2-tem"
    exit 1
}

# Check Java installation and version
check_java() {
    local java_version=${1:-$DEFAULT_JAVA_VERSION}
    log_info "Checking Java installation..."

    # Setup Java environment first
    setup_java "$java_version"

    # Verify Java is working
    if ! command -v java &> /dev/null; then
        log_error "Java command not found after setup"
        exit 1
    fi

    if ! command -v javac &> /dev/null; then
        log_error "Java compiler (javac) not found"
        exit 1
    fi

    # Show Java version
    local actual_version=$(java -version 2>&1 | head -n 1)
    log_success "Java version: $actual_version"

    # Show JAVA_HOME
    if [ -n "$JAVA_HOME" ]; then
        log_success "JAVA_HOME: $JAVA_HOME"
    else
        log_warning "JAVA_HOME is not set, using system Java"
    fi
}

# Check if Maven is installed
check_maven() {
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed, please install Maven first"
        exit 1
    fi

    local mvn_version=$(mvn -version | head -n 1)
    log_info "Detected Maven: $mvn_version"
}

# Check GPG configuration (required for central repository deployment)
check_gpg() {
    if ! command -v gpg &> /dev/null; then
        log_error "GPG is not installed, deployment to central repository requires GPG signature"
        exit 1
    fi

    if ! gpg --list-secret-keys | grep -q "sec"; then
        log_error "No GPG keys found, please generate GPG keys first"
        exit 1
    fi

    log_success "GPG configuration check passed"
}

# Check Maven settings.xml configuration
check_maven_settings() {
    local settings_file="$HOME/.m2/settings.xml"

    if [ ! -f "$settings_file" ]; then
        log_warning "Maven settings.xml file not found"
        log_info "Maven will use command-line Java configuration"
    else
        log_info "Maven settings.xml found, but script will use command-line Java configuration to ensure consistency"
    fi

    if grep -q "ossrh" "$settings_file" 2>/dev/null || grep -q "central" "$settings_file" 2>/dev/null; then
        log_success "Central repository configuration detected"
    else
        log_warning "Central repository configuration not detected"
        if [ "${1:-}" = "deploy" ]; then
            log_error "For deployment to central repository, please configure settings.xml with proper credentials"
        fi
    fi
}

# Build and deploy a single module
build_module() {
    local module=$1
    local phase=$2

    log_info "Building and deploying module: $module (phase: $phase)"

    cd "$module"

    # Execute Maven command with Java version from environment variable
    if [ "$phase" = "deploy" ]; then
        # Deploy to central repository
        mvn clean deploy -DskipTests -Djava.version=$JAVA_VERSION
    else
        # Local install
        mvn clean install -DskipTests -Djava.version=$JAVA_VERSION
    fi

    local exit_code=$?

    cd ..

    if [ $exit_code -eq 0 ]; then
        log_success "Module $module build and deployment successful"
    else
        log_error "Module $module build and deployment failed (exit code: $exit_code)"
        exit $exit_code
    fi
}

# Parallel build modules without dependencies
build_parallel() {
    local modules=("$@")
    local pids=()

    log_info "Starting parallel build of modules: ${modules[*]}"

    for module in "${modules[@]}"; do
        (
            log_info "Parallel building module: $module"
            cd "$module"
            mvn clean install -DskipTests -Djava.version=$JAVA_VERSION
            log_success "Parallel build of module $module completed"
        ) &
        pids+=($!)
    done

    # Wait for all parallel tasks to complete
    for pid in "${pids[@]}"; do
        wait $pid
        if [ $? -ne 0 ]; then
            log_error "Parallel build task failed (PID: $pid)"
            exit 1
        fi
    done

    log_success "All parallel modules build completed"
}

# Main function
main() {
    local phase=${1:-install}  # Default to install, can be specified as deploy
    local java_version=${2:-$DEFAULT_JAVA_VERSION}  # Default to specified version or global default

    log_info "Starting Bus project one-click build and deploy (phase: $phase, Java: $java_version)"
    log_info "Current time: $(date)"
    log_info "Working directory: $(pwd)"

    # Environment checks
    check_java "$java_version"
    check_maven
    check_gpg
    check_maven_settings "$phase"

    # Export Java version for all Maven commands
    export JAVA_VERSION=$java_version

    echo
    log_info "Building and deploying modules following dependency order..."
    echo

    # Phase 1: Core foundation modules (can be built in parallel)
    log_info "=== Phase 1: Core Foundation Modules ==="
    build_parallel "bus-core" "bus-logger" "bus-setting" "bus-validate"

    # Phase 2: Intermediate layer modules
    log_info "=== Phase 2: Intermediate Layer Modules ==="
    build_module "bus-http" "$phase"
    build_module "bus-crypto" "$phase"
    build_module "bus-proxy" "$phase"
    build_module "bus-extra" "$phase"
    build_module "bus-mapper" "$phase"

    # Phase 3: Advanced feature modules
    log_info "=== Phase 3: Advanced Feature Modules ==="
    build_module "bus-base" "$phase"
    build_module "bus-cache" "$phase"
    build_module "bus-auth" "$phase"
    build_module "bus-shade" "$phase"

    # Phase 4: Other independent modules
    log_info "=== Phase 4: Other Independent Modules ==="
    build_module "bus-cron" "$phase"
    build_module "bus-gitlab" "$phase"
    build_module "bus-health" "$phase"
    build_module "bus-image" "$phase"
    build_module "bus-limiter" "$phase"
    build_module "bus-notify" "$phase"
    build_module "bus-office" "$phase"
    build_module "bus-opencv" "$phase"
    build_module "bus-pay" "$phase"
    build_module "bus-sensitive" "$phase"
    build_module "bus-socket" "$phase"
    build_module "bus-storage" "$phase"
    build_module "bus-tracer" "$phase"
    build_module "bus-vortex" "$phase"
    build_module "bus-starter" "$phase"

    # Phase 5: BOM and aggregation modules
    log_info "=== Phase 5: BOM and Aggregation Modules ==="
    build_module "bus-bom" "$phase"
    build_module "bus-all" "$phase"

    echo
    log_success "=== All modules build and deployment completed ==="
    log_info "Completion time: $(date)"

    if [ "$phase" = "deploy" ]; then
        echo
        log_info "Deployment notes:"
        log_info "1. Central repository synchronization may take some time"
        log_info "2. Check deployment status through the following links:"
        log_info "   - https://central.sonatype.com/"
        log_info "   - https://search.maven.org/"
    fi
}

# Show help information
show_help() {
    echo "Bus Project One-Click Build and Deploy Script"
    echo
    echo "Usage:"
    echo "  ./$(basename "$0") [install|deploy] [java-version]"
    echo
    echo "Parameters:"
    echo "  install      - Build and install to local repository (default)"
    echo "  deploy       - Build and deploy to central repository"
    echo "  java-version - Java version to use (default: $DEFAULT_JAVA_VERSION)"
    echo
    echo "Environment Requirements:"
    echo "  1. JDK (auto-detected, default: $DEFAULT_JAVA_VERSION)"
    echo "  2. Maven 3.6+"
    echo "  3. GPG (required for deployment)"
    echo "  4. Configured ~/.m2/settings.xml (required for deployment)"
    echo
    echo "Examples:"
    echo "  ./$(basename "$0") install        # Local build with Java $DEFAULT_JAVA_VERSION"
    echo "  ./$(basename "$0") deploy         # Deploy with Java $DEFAULT_JAVA_VERSION"
}

# Script entry point
case "$1" in
    -h|--help)
        show_help
        exit 0
        ;;
    install|"")
        main "install" "$2"
        ;;
    deploy)
        main "deploy" "$2"
        ;;
    *)
        log_error "Unknown parameter: $1"
        show_help
        exit 1
        ;;
esac