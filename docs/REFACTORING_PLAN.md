# Kotlin Spring Project Refactoring Plan

## Overview
This document provides step-by-step instructions for refactoring the current flat package structure into a well-organized, maintainable architecture using IntelliJ IDEA's refactoring tools.

## Current Structure Problems
- All 13 Kotlin classes are in the root package `com.divadvo.kotlinspring`
- No separation between controllers, services, models, and configuration
- Mixed concerns (API controllers with web controllers)
- Data models not categorized by their purpose

## Target Package Structure
```
src/main/kotlin/com/divadvo/kotlinspring/
├── config/
│   └── SwaggerConfig.kt
├── controller/
│   ├── api/
│   │   ├── MessageController.kt
│   │   └── LogController.kt
│   └── web/
│       ├── FileUploadController.kt
│       └── FileSystemController.kt
├── service/
│   └── BookingService.kt
├── model/
│   ├── domain/
│   │   └── Booking.kt
│   ├── dto/
│   │   ├── LogEntry.kt (contains LogEntry + LogResponse)
│   │   ├── DirectoryItem.kt
│   │   └── PredefinedFile.kt
│   └── enums/
│       └── SourceType.kt
└── KotlinSpringApplication.kt (remains in root)
```

## Step-by-Step IntelliJ Refactoring Instructions

### Phase 1: Create Package Structure

1. **Create new packages** (Right-click on `src/main/kotlin/com/divadvo/kotlinspring/`):
   - Create package: `config`
   - Create package: `controller`
   - Create package: `controller.api`
   - Create package: `controller.web`
   - Create package: `service`
   - Create package: `model`
   - Create package: `model.domain`
   - Create package: `model.dto`
   - Create package: `model.enums`

### Phase 2: Move Configuration Classes

2. **Move SwaggerConfig.kt**:
   - Right-click on `SwaggerConfig.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.config`
   - Click OK

### Phase 3: Move and Organize Controllers

3. **Move MessageController.kt**:
   - Right-click on `MessageController.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.controller.api`
   - Click OK

4. **Move LogController.kt**:
   - Right-click on `LogController.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.controller.api`
   - Click OK

5. **Move FileUploadController.kt**:
   - Right-click on `FileUploadController.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.controller.web`
   - Click OK

6. **Move FileSystemController.kt**:
   - Right-click on `FileSystemController.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.controller.web`
   - Click OK

### Phase 4: Move Services

7. **Move BookingService.kt**:
   - Right-click on `BookingService.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.service`
   - Click OK

### Phase 5: Move and Categorize Models

8. **Move Booking.kt** (Domain Model):
   - Right-click on `Booking.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.model.domain`
   - Click OK

9. **Move LogEntry.kt** (DTO):
   - Right-click on `LogEntry.kt` → Refactor → Move...
   - Select destination: `com.divadvo.kotlinspring.model.dto`
   - Click OK
   - **Note**: This file contains both `LogEntry` and `LogResponse` classes

10. **Move DirectoryItem.kt** (DTO):
    - Right-click on `DirectoryItem.kt` → Refactor → Move...
    - Select destination: `com.divadvo.kotlinspring.model.dto`
    - Click OK

11. **Move PredefinedFile.kt** (DTO):
    - Right-click on `PredefinedFile.kt` → Refactor → Move...
    - Select destination: `com.divadvo.kotlinspring.model.dto`
    - Click OK

12. **Move SourceType.kt** (Enum):
    - Right-click on `SourceType.kt` → Refactor → Move...
    - Select destination: `com.divadvo.kotlinspring.model.enums`
    - Click OK

### Phase 6: Verify and Fix Import Statements

13. **Check and fix imports automatically**:
    - Press `Ctrl+Alt+O` (Windows/Linux) or `Cmd+Alt+O` (Mac) to optimize imports for each file
    - Or use `Code` → `Optimize Imports` from the menu
    - IntelliJ should automatically update import statements after moving files

14. **Build the project** to ensure all references are correct:
    - Run `./gradlew build` or use IntelliJ's build button
    - Fix any remaining import issues if they exist

### Phase 7: Optional Method Extractions (Advanced Refactoring)

15. **Extract utility methods from FileUploadController** (Optional):
    - Consider moving `getPredefinedFiles()` to `BookingService`
    - Consider creating a `FileUtils` class for `generateDisplayName()`
    
    **To move a method**:
    - Right-click on method → Refactor → Move Members...
    - Choose destination class
    - Update method visibility (make it public if needed)

16. **Extract log parsing logic from LogController** (Optional):
    - Consider creating `LogParsingService` in service package
    - Move `parseLogLine()` method to this new service
    
    **To create new service**:
    - Create new class in `service` package
    - Move method using Refactor → Move Members...
    - Inject the service into `LogController`

### Phase 8: Final Verification

17. **Run tests**:
    - Execute `./gradlew test` to ensure all functionality works
    - Fix any broken tests if they reference moved classes

18. **Run the application**:
    - Start the Spring Boot application
    - Test all endpoints to ensure they work correctly
    - Check Swagger UI at `/swagger-ui.html`

## Expected Benefits After Refactoring

✅ **Clear separation of concerns**:
- API controllers separate from web controllers
- DTOs separate from domain models
- Configuration isolated in its own package

✅ **Improved maintainability**:
- Easy to find related functionality
- Follows Spring Boot conventions
- Easier to add new features

✅ **Better testability**:
- Services can be easily mocked
- Clear boundaries between layers

✅ **Enhanced scalability**:
- Structure supports growth
- Easy to add new controllers, services, or models

## Files That Remain Unchanged
- `KotlinSpringApplication.kt` (stays in root - Spring Boot main class)
- All files in `src/main/resources/` (templates, properties, etc.)
- Test files (unless they need import updates)

## Troubleshooting

**If IntelliJ doesn't automatically update imports**:
1. Try `File` → `Invalidate Caches and Restart`
2. Manually fix imports using `Alt+Enter` on red underlined imports
3. Use `Find and Replace` (`Ctrl+Shift+R`) to update package references

**If build fails after refactoring**:
1. Check for missing imports in all moved files
2. Verify that all references to moved classes are updated
3. Clean and rebuild the project: `./gradlew clean build`

## Notes
- IntelliJ's refactoring tools are generally very reliable and will update most references automatically
- The package structure follows standard Spring Boot conventions
- This refactoring doesn't change functionality, only organization
- Consider using IntelliJ's "Commit" dialog to review all changes before committing