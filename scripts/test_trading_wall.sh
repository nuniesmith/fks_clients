#!/bin/bash
# Test script for Trading Wall Dashboard
# This script helps verify the Trading Wall Dashboard is working correctly

set -e

echo "========================================="
echo "FKS Trading Wall Dashboard - Test Script"
echo "========================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if we're in the clients directory
if [ ! -f "build.gradle.kts" ]; then
    echo -e "${RED}Error: Please run this script from the clients directory${NC}"
    exit 1
fi

echo "Step 1: Checking prerequisites..."
echo "-----------------------------------"

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1)
    echo -e "${GREEN}✓${NC} Java found: $JAVA_VERSION"
else
    echo -e "${RED}✗${NC} Java not found. Please install JDK 17+"
    exit 1
fi

# Check Node.js
if command -v node &> /dev/null; then
    NODE_VERSION=$(node --version)
    echo -e "${GREEN}✓${NC} Node.js found: $NODE_VERSION"
else
    echo -e "${RED}✗${NC} Node.js not found. Please install Node.js v20.19.4+"
    exit 1
fi

# Check Gradle wrapper
if [ -f "gradlew" ]; then
    echo -e "${GREEN}✓${NC} Gradle wrapper found"
else
    echo -e "${RED}✗${NC} Gradle wrapper not found"
    exit 1
fi

echo ""
echo "Step 2: Checking backend services..."
echo "-----------------------------------"

# Check data service
DATA_URL="${FKS_DATA_URL:-http://localhost:8003}"
if curl -s -f "$DATA_URL/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Data service is running at $DATA_URL"
else
    echo -e "${YELLOW}⚠${NC} Data service not reachable at $DATA_URL"
    echo "   (This is OK if you're testing without backend services)"
fi

# Check app service
APP_URL="${FKS_API_URL:-http://localhost:8001}"
if curl -s -f "$APP_URL/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} App service is running at $APP_URL"
else
    echo -e "${YELLOW}⚠${NC} App service not reachable at $APP_URL"
    echo "   (This is OK - metrics are optional)"
fi

# Check portfolio service
PORTFOLIO_URL="${FKS_PORTFOLIO_URL:-http://localhost:8012}"
if curl -s -f "$PORTFOLIO_URL/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Portfolio service is running at $PORTFOLIO_URL"
else
    echo -e "${YELLOW}⚠${NC} Portfolio service not reachable at $PORTFOLIO_URL"
    echo "   (This is OK - portfolio metrics are optional)"
fi

echo ""
echo "Step 3: Testing data service API..."
echo "-----------------------------------"

# Test price endpoint
TEST_SYMBOL="BTCUSDT"
if curl -s -f "$DATA_URL/api/v1/data/price?symbol=$TEST_SYMBOL" > /dev/null 2>&1; then
    PRICE_RESPONSE=$(curl -s "$DATA_URL/api/v1/data/price?symbol=$TEST_SYMBOL")
    PRICE=$(echo "$PRICE_RESPONSE" | grep -o '"price":[0-9.]*' | cut -d: -f2 || echo "N/A")
    echo -e "${GREEN}✓${NC} Price API working - $TEST_SYMBOL price: $PRICE"
else
    echo -e "${YELLOW}⚠${NC} Price API test failed for $TEST_SYMBOL"
    echo "   (Dashboard will work but may show no prices)"
fi

echo ""
echo "Step 4: Building web app..."
echo "-----------------------------------"

# Build the web app
echo "Running: ./gradlew :web:build"
if ./gradlew :web:build --quiet; then
    echo -e "${GREEN}✓${NC} Build successful"
else
    echo -e "${RED}✗${NC} Build failed. Please check the error messages above"
    exit 1
fi

echo ""
echo "Step 5: Checking build outputs..."
echo "-----------------------------------"

# Check if build outputs exist
WEB_DIST="web/dist"
if [ -d "$WEB_DIST" ]; then
    echo -e "${GREEN}✓${NC} Build output directory exists: $WEB_DIST"
    
    # Check for JS bundle
    if [ -f "$WEB_DIST/fks-web-kmp.js" ] || find "$WEB_DIST" -name "*.js" -type f | grep -q .; then
        echo -e "${GREEN}✓${NC} JavaScript bundle found"
    else
        echo -e "${YELLOW}⚠${NC} JavaScript bundle not found in expected location"
    fi
    
    # Check for HTML files
    if [ -f "$WEB_DIST/index.html" ]; then
        echo -e "${GREEN}✓${NC} index.html found"
    fi
    
    if [ -f "web/src/jsMain/resources/kiosk.html" ]; then
        echo -e "${GREEN}✓${NC} kiosk.html source found"
    fi
else
    echo -e "${YELLOW}⚠${NC} Build output directory not found: $WEB_DIST"
    echo "   (This may be normal - distribution files may be in a different location)"
fi

echo ""
echo "Step 6: Checking source files..."
echo "-----------------------------------"

# Check key source files
FILES=(
    "shared/src/commonMain/kotlin/xyz/fkstrading/clients/domain/viewmodel/TradingWallViewModel.kt"
    "shared/src/commonMain/kotlin/xyz/fkstrading/clients/ui/screens/TradingWallScreen.kt"
    "web/src/jsMain/kotlin/xyz/fkstrading/clients/web/Main.kt"
    "web/src/jsMain/kotlin/xyz/fkstrading/clients/web/KioskMain.kt"
)

ALL_EXIST=true
for file in "${FILES[@]}"; do
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $file"
    else
        echo -e "${RED}✗${NC} $file (NOT FOUND)"
        ALL_EXIST=false
    fi
done

if [ "$ALL_EXIST" = false ]; then
    echo -e "${RED}Some source files are missing!${NC}"
    exit 1
fi

echo ""
echo "========================================="
echo "Test Summary"
echo "========================================="
echo -e "${GREEN}✓${NC} All checks passed!"
echo ""
echo "Next steps:"
echo "1. Start the development server:"
echo "   ${YELLOW}./gradlew :web:jsBrowserDevelopmentRun${NC}"
echo ""
echo "2. Open in browser:"
echo "   ${YELLOW}http://localhost:8080${NC}"
echo ""
echo "3. For kiosk mode:"
echo "   - Serve the kiosk.html file"
echo "   - Or use browser kiosk mode (F11)"
echo ""
echo "4. Test the Trading Wall:"
echo "   - Login to the app"
echo "   - Click 'Trading Wall' in the sidebar"
echo "   - Verify world clocks update"
echo "   - Verify ticker prices load"
echo "   - Verify metrics panel displays"
echo ""
echo "========================================="
