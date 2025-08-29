# NSRS-Web Local Build Script (PowerShell Version)
# Generate WAR file for manual deployment

Write-Host "========================================" -ForegroundColor Green
Write-Host "       NSRS-Web Local Build Tool" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Check if running in correct directory
if (-not (Test-Path "package.json")) {
    Write-Host "Error: Please run this script in the project root directory!" -ForegroundColor Red
    Write-Host "Current directory: $(Get-Location)" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

$APP_NAME = "nsrs-web"

# Check required tools
try {
    $null = npm --version
} catch {
    Write-Host "Error: npm command not found, please install Node.js first!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

try {
    $null = jar 2>$null
} catch {
    Write-Host "Error: jar command not found, please install JDK first!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "[1/3] Cleaning old files..." -ForegroundColor Yellow
if (Test-Path "dist") {
    Write-Host "Removing dist directory..."
    Remove-Item "dist" -Recurse -Force
}
if (Test-Path "nsrs.war") {
    Write-Host "Removing old WAR file..."
    Remove-Item "nsrs.war" -Force
}
if (Test-Path "war-temp") {
    Write-Host "Removing temp directory..."
    Remove-Item "war-temp" -Recurse -Force
}
Write-Host "Cleanup completed" -ForegroundColor Green

Write-Host ""
Write-Host "[2/3] Building project..." -ForegroundColor Yellow
Write-Host "Building with Vite (skipping TypeScript checks)..."
$buildResult = & .\node_modules\.bin\vite.cmd build
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Build failed!" -ForegroundColor Red
    Write-Host "Please check the error messages above and fix any issues." -ForegroundColor Red
    Write-Host ""
    exit 1
}
Write-Host "Build completed successfully" -ForegroundColor Green

Write-Host ""
Write-Host "[3/3] Creating WAR package..." -ForegroundColor Yellow

# Check build results
if (-not (Test-Path "dist")) {
    Write-Host "X Build directory dist does not exist!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

if (-not (Test-Path "dist\index.html")) {
    Write-Host "X Build result incomplete, missing index.html!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

# Create WAR directory structure
Write-Host "Creating temp directory..."
New-Item -ItemType Directory -Name "war-temp" -Force | Out-Null
Write-Host "Copying build files..."
Copy-Item -Path "dist\*" -Destination "war-temp\" -Recurse -Force

# Create WEB-INF directory and web.xml
Write-Host "Creating WEB-INF structure..."
New-Item -ItemType Directory -Path "war-temp\WEB-INF" -Force | Out-Null

Write-Host "Creating web.xml..."
$webXmlContent = @'
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <display-name>NSRS-Web</display-name>

    <!-- SPA routing support -->
    <error-page>
        <error-code>404</error-code>
        <location>/index.html</location>
    </error-page>

    <!-- Static resource caching -->
    <filter>
        <filter-name>CacheFilter</filter-name>
        <filter-class>org.apache.catalina.filters.ExpiresFilter</filter-class>
        <init-param>
            <param-name>ExpiresByType text/css</param-name>
            <param-value>access plus 1 year</param-value>
        </init-param>
        <init-param>
            <param-name>ExpiresByType application/javascript</param-name>
            <param-value>access plus 1 year</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>CacheFilter</filter-name>
        <url-pattern>*.css</url-pattern>
    </filter-mapping>
    <filter-mapping>
        <filter-name>CacheFilter</filter-name>
        <url-pattern>*.js</url-pattern>
    </filter-mapping>

</web-app>
'@

$webXmlContent | Out-File -FilePath "war-temp\WEB-INF\web.xml" -Encoding UTF8

# Package as WAR file
Write-Host "Creating WAR file..."
Set-Location "war-temp"
$jarResult = & jar -cf "..\nsrs.war" *
Set-Location ".."

if ($LASTEXITCODE -ne 0) {
    Write-Host "X WAR file creation failed!" -ForegroundColor Red
    Write-Host "Please check if jar command is properly installed" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Remove-Item "war-temp" -Recurse -Force

# Verify WAR file
if (-not (Test-Path "nsrs.war")) {
    Write-Host "X WAR file creation failed!" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

$warFile = Get-Item "nsrs.war"
$sizeMB = [math]::Round($warFile.Length / 1MB, 2)

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "           Package completed!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host "WAR file: $(Get-Location)\nsrs.war" -ForegroundColor Cyan
Write-Host "File size: $($warFile.Length) bytes (about $sizeMB MB)" -ForegroundColor Cyan
Write-Host ""
Write-Host "Manual deployment steps:" -ForegroundColor Yellow
Write-Host "1. Copy nsrs.war to your Tomcat server" -ForegroundColor White
Write-Host "2. Stop Tomcat service" -ForegroundColor White
Write-Host "3. Delete webapps/nsrs directory (if exists)" -ForegroundColor White
Write-Host "4. Put nsrs.war into webapps directory" -ForegroundColor White
Write-Host "5. Start Tomcat service" -ForegroundColor White
Write-Host "6. Access: http://server-ip:8080/nsrs/" -ForegroundColor White
Write-Host "========================================" -ForegroundColor Green

Write-Host ""
Read-Host "Press Enter to exit"