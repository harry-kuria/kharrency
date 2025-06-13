# Kharrency - Currency Converter App

A modern Android currency converter app built with Jetpack Compose, following MVVM architecture and best practices.

## Features

- Real-time currency conversion using [ExchangeRate.host](https://api.exchangerate.host/latest)
- Support for USD, EUR, and JPY currencies
- Conversion history tracking (last 5 conversions)
- Offline support with local caching
- Modern Material 3 UI with Jetpack Compose
- Error handling and retry mechanisms

## Technical Stack

- **Architecture**: MVVM with Clean Architecture
- **UI**: Jetpack Compose with Material 3
- **State Management**: Kotlin Flow
- **Dependency Injection**: Hilt
- **Local Storage**: Room Database
- **Networking**: Retrofit
- **Testing**: JUnit, Mockito, Coroutines Test

## Project Structure

```
app/
├── composables/     # UI components
├── viewmodels/      # ViewModels and state management
├── repository/      # Data layer and API integration
└── model/          # Data models and Room entities
```

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the app

## Dependencies

- AndroidX Core KTX
- Jetpack Compose
- Hilt for dependency injection
- Room for local storage
- Retrofit for networking
- Coroutines for asynchronous operations

## Testing

The project includes unit tests for:
- ViewModel logic (minimum 3 tests)
- Repository operations
- Data caching

Run tests using:
```bash
./gradlew test
```

## Technical Requirements

### Core Features
- Real-time currency rates from ExchangeRate.host API
- Support for USD, EUR, and JPY currencies
- Last 5 conversion history tracking
- Offline support with local caching
- Network error handling

### Technical Constraints
- MVVM architecture
- Kotlin Coroutines and Flow for async operations
- Hilt for dependency injection
- Room for local caching
- Minimum 3 unit tests for ViewModel/Repository

## License

This project is licensed under the MIT License - see the LICENSE file for details. 

![Screenshot from 2025-06-13 09 42 57](https://github.com/user-attachments/assets/0e39a48f-6ea0-49f1-a7cb-9015cb0fc2bf)
![Screenshot from 2025-06-13 11 12 35](https://github.com/user-attachments/assets/2bff3dfc-1edc-4a2d-a944-5be76432f45d)
![Screenshot from 2025-06-13 11 12 46](https://github.com/user-attachments/assets/46fd0b17-233e-4286-97f3-3e0815c6a482)
![Screenshot from 2025-06-13 11 13 31](https://github.com/user-attachments/assets/364a7021-151a-4cc5-a509-9149324296c8)


