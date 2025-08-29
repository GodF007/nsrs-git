#!/bin/bash

# NSRS Simplified Build Script
echo "========================================"
echo "        NSRS Integrated Build Tool"
echo "========================================"
echo

# Get project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

# Check if running in correct directory
if [ ! -f "pom.xml" ]; then
    echo "Error: Project root pom.xml not found!"
    exit 1
fi

if [ ! -f "nsrs-web/package.json" ]; then
    echo "Error: nsrs-web module not found!"
    exit 1
fi

# Check required tools
if ! command -v npx &> /dev/null; then
    echo "Error: npx command not found, please install Node.js first!"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo "Error: mvn command not found, please install Maven first!"
    exit 1
fi

echo "[1/2] Building frontend project in nsrs-web directory..."
cd nsrs-web
echo "Current directory: $(pwd)"
if [ -d "node_modules" ]; then
    echo "Dependencies already exist, skipping npm install..."
else
    echo "Installing dependencies..."
    npm install
    # Check if node_modules exists after installation
    if [ ! -d "node_modules" ]; then
        echo "Frontend dependency installation failed - node_modules not found!"
        cd ..
        exit 1
    fi
    echo "Dependencies installed successfully"
fi
echo "Building frontend with npx vite build..."
npx vite build
if [ $? -ne 0 ]; then
    echo "Frontend build failed!"
    cd ..
    exit 1
fi
cd ..
echo "Frontend build completed successfully"

# Verify frontend build
if [ ! -d "nsrs-web/dist" ]; then
    echo "Frontend build directory dist does not exist!"
    exit 1
fi

echo
echo "[2/2] Building integrated jar package with Maven..."
echo "Current directory: $(pwd)"
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "Backend build failed!"
    exit 1
fi
echo "Backend build completed successfully"

# Verify jar file
JAR_FILE="nsrs-boot/target/nsrs-boot-1.0.0-SNAPSHOT.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file creation failed!"
    exit 1
fi

jar_size=$(stat -c%s "$JAR_FILE" 2>/dev/null || stat -f%z "$JAR_FILE" 2>/dev/null)
size_mb=$((jar_size / 1024 / 1024))

echo
echo "========================================"
echo "        Build Completed Successfully!"
echo "========================================"
echo "JAR file: $(pwd)/$JAR_FILE"
echo "File size: $jar_size bytes (about ${size_mb} MB)"
echo
echo "To run the application:"
echo "java -jar $JAR_FILE --spring.profiles.active=sharding"
echo
echo "Access URL: http://localhost:8088"
echo "========================================"

echo
echo "Build script completed successfully!"