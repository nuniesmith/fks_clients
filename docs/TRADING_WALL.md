# Trading Wall Dashboard

**Full-screen trading dashboard designed for large displays (55-65" 4K TVs)**

## Overview

The Trading Wall Dashboard is a real-time, full-screen interface built with Compose Multiplatform that displays:

- **World Clocks** - 6 major trading centers (Sydney, Tokyo, Hong Kong, London, Frankfurt, New York) with market status
- **Price Ticker** - Scrolling real-time prices from FKS data service
- **Trading Metrics** - P&L, Exposure, Risk, Signals, Cache performance

## Features

### World Clocks
- **6 Major Trading Centers**:
  - Sydney (ASX)
  - Tokyo (TSE)
  - Hong Kong (HKEX)
  - London (LSE)
  - Frankfurt (XETR)
  - New York (NYSE)
- **Status Indicators**:
  - 🟢 **Green**: Market open
  - 🟠 **Orange**: Pre-market
  - ⚫ **Gray**: Market closed
  - 🟡 **Yellow (pulsing)**: Overlap period (London/NY)

### Price Ticker
- **Real-time Prices**: Fetched from FKS data service
- **Color Coding**:
  - 🟢 Green: Price up
  - 🔴 Red: Price down
  - ⚪ White: Neutral
- **Symbols**: Configurable list (forex, crypto, stocks, indices)
- **Auto-refresh**: Updates every 8 seconds

### Metrics Panel
- **P&L**: Profit & Loss (from portfolio service)
- **Exposure**: Current exposure (from portfolio service)
- **Risk**: Risk metrics (from portfolio service)
- **Signals**: Signal generation count (from app service)
- **Cache Hit**: Cache performance (from app service)
- **Auto-refresh**: Updates every 5 seconds

## Usage

### As Part of Main Web App

The Trading Wall is available as a screen in the main FKS web application:

1. **Build the web app**:
   ```bash
   cd clients
   ./gradlew :web:build
   ```

2. **Run development server**:
   ```bash
   ./gradlew :web:jsBrowserDevelopmentRun
   ```

3. **Access in browser**:
   - Open `http://localhost:8080`
   - Login (if required)
   - Navigate to "Trading Wall" from the sidebar

### Kiosk Mode (Full-Screen Display)

For dedicated TV displays, use kiosk mode which shows only the Trading Wall without authentication:

1. **Build for kiosk mode**:
   ```bash
   cd clients
   ./gradlew :web:build
   ```

2. **Serve the kiosk HTML**:
   - The kiosk HTML is in `web/src/jsMain/resources/kiosk.html`
   - Serve it using any static file server
   - Or copy `dist` folder contents to your web server

3. **Open in browser**:
   - Open the kiosk HTML file in your browser
   - For full-screen, press F11 or use browser kiosk mode

### Kiosk Mode Setup for Raspberry Pi / TV

1. **Install Chromium**:
   ```bash
   sudo apt update
   sudo apt install chromium-browser -y
   ```

2. **Create autostart**:
   ```bash
   mkdir -p ~/.config/lxsession/LXDE-pi
   nano ~/.config/lxsession/LXDE-pi/autostart
   ```

3. **Add to autostart**:
   ```
   @xset s off
   @xset -dpms
   @xset s noblank
   @chromium-browser --kiosk --noerrdialogs --disable-infobars --incognito http://YOUR-SERVER-IP:PORT/kiosk.html
   ```

   Replace `YOUR-SERVER-IP` and `PORT` with your server's address.

4. **Reboot**:
   ```bash
   sudo reboot
   ```

## Configuration

### API URLs

Configure backend service URLs via JavaScript globals in the HTML:

```html
<script>
    window.FKS_API_URL = 'http://localhost:8001';
    window.FKS_DATA_URL = 'http://localhost:8003';
    window.FKS_PORTFOLIO_URL = 'http://localhost:8012';
</script>
```

### Ticker Symbols

Edit the symbol list in `TradingWallViewModel.kt`:

```kotlin
val tickerSymbols = listOf(
    "EURUSD", "GBPUSD", "USDJPY", "USDCAD", "AUDUSD",
    "BTCUSDT", "ETHUSDT", "SPX", "NDX", "GOLD", "OIL"
)
```

### Refresh Intervals

Edit refresh intervals in `TradingWallViewModel.kt`:

```kotlin
delay(1000)  // Clocks: every 1 second
delay(8000)  // Ticker: every 8 seconds
delay(5000)  // Metrics: every 5 seconds
```

## File Structure

```
clients/
├── shared/src/commonMain/kotlin/xyz/fkstrading/clients/
│   ├── data/models/Models.kt                    # Data models (added Trading Wall models)
│   ├── domain/viewmodel/TradingWallViewModel.kt # State management
│   └── ui/screens/TradingWallScreen.kt          # Compose UI screen
└── web/
    ├── src/jsMain/kotlin/.../web/
    │   ├── Main.kt                              # Main web app (includes Trading Wall)
    │   └── KioskMain.kt                         # Kiosk mode entry point
    └── src/jsMain/resources/
        ├── index.html                           # Main app HTML
        └── kiosk.html                           # Kiosk mode HTML
```

## API Integration

The Trading Wall integrates with FKS backend services:

### Data Service (`FKS_DATA_URL`)
- `GET /api/v1/data/price?symbol={symbol}` - Current price
- `GET /api/v1/data/ohlcv?symbol={symbol}&interval={interval}` - OHLCV data

### App Service (`FKS_API_URL`)
- `GET /api/v1/timeframes/metrics` - Multi-timeframe metrics
- `GET /api/v1/timeframes/signals?symbol={symbol}` - Signals

### Portfolio Service (`FKS_PORTFOLIO_URL`)
- `GET /api/dashboard/overview` - Portfolio overview (P&L, Exposure, Risk)

## Customization

### Colors and Themes

Edit theme colors in `shared/src/commonMain/kotlin/xyz/fkstrading/clients/ui/theme/Theme.kt`:

```kotlin
object FksColors {
    val Primary = Color(0xFFF7931A)      // Bitcoin orange
    val Secondary = Color(0xFF4CAF50)    // Green for positive
    val Error = Color(0xFFE53935)        // Red for negative
}
```

### Layout Adjustments

Modify the layout in `TradingWallScreen.kt`:

- **Clock Grid**: Change from 3x2 to different layout
- **Metrics Panel**: Adjust position (top-right, bottom, etc.)
- **Ticker Height**: Modify ticker bar height
- **Font Sizes**: Adjust for different screen sizes

## Troubleshooting

### No Prices Showing

1. **Check data service**:
   ```bash
   curl http://localhost:8003/api/v1/data/price?symbol=BTCUSDT
   ```

2. **Check browser console** (F12) for errors

3. **Verify API URLs** are correct in HTML

### Clocks Not Updating

- Clocks update every 1 second
- Check browser JavaScript is enabled
- Check browser console for errors

### Metrics Not Showing

- Metrics are optional (gracefully fail if services unavailable)
- Check app service is running:
  ```bash
  curl http://localhost:8001/api/v1/timeframes/metrics
  ```

### CORS Errors

- Ensure backend services have CORS enabled
- Check browser console for specific CORS errors
- Verify API URLs are correct

## Performance

### Expected Performance

- **Clock Updates**: <1ms (client-side)
- **Ticker Updates**: 8 seconds (configurable)
- **Metrics Updates**: 5 seconds (configurable)
- **Memory Usage**: <50MB
- **CPU Usage**: <5% (idle)

### Optimization Tips

1. **Reduce ticker symbols** if performance issues
2. **Increase refresh intervals** if network is slow
3. **Use local caching** for frequently accessed data
4. **Disable unnecessary metrics** if not needed

## TV Display Recommendations

### Recommended TVs (2025)

| Size | Model | Price | Why Perfect |
|------|-------|-------|-------------|
| 55" | Hisense U6 Mini-LED | $298-348 | Brightest budget option |
| 55" | TCL Q6 Pro QLED | $318-378 | Best gaming/response |
| 65" | Hisense U7N Mini-LED | $498-548 | Go big - massive clocks |

### Display Settings

**Recommended TV Settings**:
- **Brightness**: 80-100% (for bright room)
- **Contrast**: 85-90%
- **Color**: Standard/Vivid
- **Sharpness**: Medium
- **Motion**: Off (reduces input lag)
- **Energy Saving**: Off

## Next Steps

1. ✅ **Trading Wall Dashboard** - Complete
2. **Enhanced Features** (optional):
   - Economic calendar
   - Level 2 order book heatmap
   - Mini charts
   - News feed
   - Alert notifications

3. **Advanced Integration**:
   - WebSocket for real-time updates
   - Historical chart integration
   - Strategy performance visualization

## Support

- **Code**: `shared/src/commonMain/kotlin/xyz/fkstrading/clients/ui/screens/TradingWallScreen.kt`
- **ViewModel**: `shared/src/commonMain/kotlin/xyz/fkstrading/clients/domain/viewmodel/TradingWallViewModel.kt`
- **Issues**: Check browser console (F12) for errors

---

**Status**: ✅ Ready for use  
**Last Updated**: 2025-01-15
