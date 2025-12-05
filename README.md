# FKS Clients - Kotlin Multiplatform Native Apps

Native client applications for FKS Trading Platform built with Kotlin Multiplatform (KMP) and Compose Multiplatform.

## Overview

This repository contains native client applications for:
- **Android** - Mobile trading app
- **Desktop** - Linux, macOS, Windows desktop app  
- **iOS** - iPhone/iPad app
- **Web** - Browser-based web app (NEW! 🎉)

All platforms share common business logic, networking, and UI components through Kotlin Multiplatform.

## Architecture

```
fks_clients/
├── shared/                    # Shared KMP module
│   └── src/
│       ├── commonMain/        # Shared code for all platforms
│       │   └── kotlin/xyz/fkstrading/clients/
│       │       ├── api/       # API client, token management
│       │       ├── data/      # Models and repositories
│       │       ├── domain/    # ViewModels
│       │       └── ui/        # Shared Compose UI
│       ├── androidMain/       # Android-specific code
│       ├── desktopMain/       # Desktop-specific code
│       └── jsMain/            # Web-specific code (NEW!)
├── android/                   # Android app module
├── desktop/                   # Desktop app module
├── ios/                       # iOS app module
└── web/                       # Web app module (NEW!)
```

## Features

### Signal Matrix
- Real-time trading signals via WebSocket
- Multiple signal categories (Swing, Scalp, Position, Bitcoin)
- Signal strength and confidence indicators
- AI-enhanced signal generation

### Portfolio Dashboard  
- Portfolio value in BTC and USD
- Asset prices with 24h change
- BTC correlations
- Rebalancing suggestions

### Authentication
- JWT-based authentication
- Automatic token refresh
- Secure credential storage

## Technology Stack

- **Kotlin** 2.0.20
- **Compose Multiplatform** 1.8.0
- **Ktor Client** 3.0.0 - HTTP & WebSocket
- **Kotlinx Serialization** - JSON parsing
- **Koin** 4.0.0 - Dependency injection
- **Voyager** 1.0.0 - Navigation

## Building

### Prerequisites

- JDK 17+
- Android SDK (for Android builds)
- Gradle 8.5+
- Node.js (for web builds) - v20.19.4 or later
- Yarn (for web builds) - install via: `sudo npm install -g yarn`

### Build Commands

```bash
# Build all
./gradlew build

# Build Android
./gradlew :android:assembleDebug

# Build Desktop
./gradlew :desktop:run

# Build Web (NEW!)
./gradlew :web:build
./gradlew :web:jsBrowserDevelopmentRun
```

## Configuration

Set environment variables for backend URLs:

```bash
export FKS_API_URL=http://localhost:8001
export FKS_DATA_URL=http://localhost:8003
export FKS_AUTH_URL=http://localhost:8009
export FKS_PORTFOLIO_URL=http://localhost:8012
```

Or configure programmatically:

```kotlin
FksApiClient.configure(
    apiUrl = "https://api.fkstrading.xyz",
    authUrl = "https://auth.fkstrading.xyz",
    // ...
)
```

## Module Structure

### shared/

Common code shared across all platforms:

- `api/FksApiClient.kt` - HTTP and WebSocket client
- `api/TokenManager.kt` - JWT token management
- `data/models/Models.kt` - Data classes matching backend APIs
- `data/repository/` - Repository layer for each service
- `domain/viewmodel/` - ViewModels for each screen
- `ui/components/` - Reusable Compose components
- `ui/screens/` - Screen composables
- `ui/theme/` - Theme colors and styles

### android/

Android-specific:
- MainActivity
- Android manifest
- Platform-specific implementations

### desktop/

Desktop-specific:
- Main window configuration
- Desktop menu
- Platform-specific implementations

### web/ (NEW!)

Web-specific:
- Browser entry point
- HTML wrapper
- Web-specific configuration

### ios/

iOS-specific:
- SwiftUI integration
- iOS-specific implementations

## API Integration

The app integrates with FKS backend services:

| Service | Port | Purpose |
|---------|------|---------|
| API Gateway | 8001 | Main API entry point |
| Data Service | 8003 | Market data (prices, OHLCV) |
| Auth Service | 8009 | Authentication & authorization |
| Portfolio Service | 8012 | Portfolio management & signals |

## Code Reuse

**100% Code Sharing Across All Platforms!**

All screens, ViewModels, repositories, and API clients are shared:
- ✅ LoginScreen
- ✅ SignalMatrixScreen
- ✅ PortfolioDashboardScreen
- ✅ EvaluationMatrixScreen
- ✅ All ViewModels
- ✅ All repositories
- ✅ All API clients

Write once, run everywhere! 🚀

## Documentation

For detailed documentation, see:
- **[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)** - Complete documentation index
- **[MIGRATION_README.md](MIGRATION_README.md)** - Web migration overview
- **[STATUS.md](STATUS.md)** - Current project status
- **[KNOWN_ISSUES.md](KNOWN_ISSUES.md)** - Known build issues

## License

MIT License - see LICENSE file
