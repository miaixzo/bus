#!/bin/bash

# =============================================================================
# Build and deploy modules to central repository following dependency order
# =============================================================================

set -e  # Exit immediately on error

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
        log_warning "Please ensure central repository deployment permissions are configured"
        return
    fi

    if grep -q "ossrh" "$settings_file" || grep -q "central" "$settings_file"; then
        log_success "Central repository configuration detected"
    else
        log_warning "Central repository configuration not detected, please check settings.xml"
    fi
}

# Build and deploy a single module
build_module() {
    local module=$1
    local phase=$2

    log_info "Building and deploying module: $module (phase: $phase)"

    cd "$module"

    # Execute Maven command
    if [ "$phase" = "deploy" ]; then
        # Deploy to central repository
        mvn clean deploy -DskipTests
    else
        # Local install
        mvn clean install -DskipTests
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
            mvn clean install -DskipTests
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

    log_info "Starting Bus project one-click build and deploy (phase: $phase)"
    log_info "Current time: $(date)"
    log_info "Working directory: $(pwd)"

    # Environment checks
    check_maven
    check_gpg
    check_maven_settings

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
    echo "  ./$(basename "$0") [install|deploy]"
    echo
    echo "Parameters:"
    echo "  install  - Build and install to local repository (default)"
    echo "  deploy   - Build and deploy to central repository"
    echo
    echo "Environment Requirements:"
    echo "  1. Maven 3.6+"
    echo "  2. GPG (required for deployment)"
    echo "  3. Configured ~/.m2/settings.xml (required for deployment)"
    echo
    echo "Examples:"
    echo "  ./$(basename "$0") install   # Local build and install"
    echo "  ./$(basename "$0") deploy    # Deploy to central repository"
}

# Script entry point
case "$1" in
    -h|--help)
        show_help
        exit 0
        ;;
    install|"")
        main "install"
        ;;
    deploy)
        main "deploy"
        ;;
    *)
        log_error "Unknown parameter: $1"
        show_help
        exit 1
        ;;
esac