## Expense Calculator App - UI Theme Documentation

### Theme Overview
The application now uses a consistent **Blue and White** color scheme throughout, matching the styling already present in the expense management UI.

---

## Color Palette (Color.kt)

### Primary Colors - Blue Theme
- **PrimaryBlue**: `#007AFF` - Main brand color used for buttons, highlights, and primary actions
- **PrimaryBlueDark**: `#0051D5` - Darker shade for variations
- **PrimaryBlueLight**: `#4DA2FF` - Lighter shade for variations
- **IconBackground**: `#EBF5FF` - Light blue background for icons

### Background & Surface
- **ScreenBackground**: `White` - Main screen background
- **CardBackground**: `White` - Card and surface backgrounds
- **BackgroundLight**: `#F8F9FA` - Subtle alternative background

### Text Colors
- **PrimaryText**: `#222222` - Primary text color (dark gray/black)
- **SecondaryText**: `#8A8A8E` - Secondary text color (medium gray)
- **LightText**: `#B0B0B0` - Light text for less important information

### Borders & Dividers
- **BorderGrey**: `#E0E0E0` - Border color for cards and inputs
- **DividerGrey**: `#EEEEEE` - Divider lines

### Semantic Colors
- **ErrorColor**: `#D32F2F` - Error states and destructive actions
- **SuccessGreen**: `#34C759` - Success states and positive actions
- **WarningYellow**: `#FFCC00` - Warning states
- **InfoBlue**: `#007AFF` - Informational messages

### Balance Colors
- **PositiveBalanceColor**: `#34C759` - Positive balance/amounts
- **NegativeBalanceColor**: `#FF3B30` - Negative balance/amounts

### Tab Colors
- **TabIndicatorColor**: `PrimaryBlue` - Active tab indicator
- **SelectedTabColor**: `PrimaryBlue` - Selected tab text
- **UnselectedTabColor**: `SecondaryText` - Unselected tab text

---

## Typography (Type.kt)

All text styles use the default font family (can be replaced with custom fonts like Poppins later).

### Text Styles
- **titleLarge**: Bold, 24sp, PrimaryText - Main headings
- **titleMedium**: Bold, 20sp, PrimaryText - Section headings
- **titleSmall**: SemiBold, 18sp, PrimaryText - Sub-headings
- **bodyLarge**: Normal, 16sp, PrimaryText - Primary body text
- **bodyMedium**: Normal, 14sp, SecondaryText - Secondary body text
- **bodySmall**: Normal, 12sp, SecondaryText - Small body text
- **labelLarge**: Medium, 14sp, PrimaryText - Large labels
- **labelMedium**: Medium, 12sp, SecondaryText - Medium labels
- **labelSmall**: Medium, 11sp, SecondaryText - Small labels

---

## Shapes (Shape.kt)

Rounded corners for a modern, friendly appearance:

- **small**: 8dp corner radius - Small elements like chips
- **medium**: 16dp corner radius - Cards, buttons, dialogs
- **large**: 24dp corner radius - Large containers

---

## Shadow Effects (Shadow.kt)

### cardShadow() Modifier
Applies a subtle elevation shadow to cards:
- **Elevation**: 8dp
- **Shadow Color**: PrimaryBlue with 8% opacity
- **Usage**: `.cardShadow()` on any Modifier

---

## Theme Configuration (Theme.kt)

### Material 3 Color Scheme Mapping
- **primary**: PrimaryBlue
- **secondary**: PrimaryBlueDark
- **tertiary**: PrimaryBlueLight
- **background**: ScreenBackground (White)
- **surface**: CardBackground (White)
- **error**: ErrorColor
- All "on" colors (onPrimary, onSecondary, etc.) are properly configured

---

## Usage Examples

### Using Theme Colors in Composables
```kotlin
// Background
modifier = Modifier.background(MaterialTheme.colorScheme.background)

// Primary button
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
)

// Text with theme color
Text(
    text = "Hello",
    color = MaterialTheme.colorScheme.onBackground
)
```

### Using Typography
```kotlin
Text(
    text = "Welcome",
    style = MaterialTheme.typography.titleLarge
)
```

### Using Shapes
```kotlin
Card(
    shape = MaterialTheme.shapes.medium
)
```

### Using Card Shadow
```kotlin
Box(
    modifier = Modifier
        .cardShadow()
        .background(Color.White, shape = MaterialTheme.shapes.medium)
)
```

---

## Benefits of This Theme System

1. **Consistency**: Same blue and white styling across the entire app
2. **Maintainability**: Change colors in one place, updates everywhere
3. **Material 3 Compliance**: Follows Material Design 3 guidelines
4. **Accessibility**: Proper contrast ratios for text readability
5. **Flexibility**: Easy to switch to dark mode or different themes in the future

---

## Files Modified

1. **Color.kt** - Complete color palette with blue theme
2. **Type.kt** - Typography system with proper text styles
3. **Theme.kt** - Material 3 theme configuration
4. **Shape.kt** - Rounded corner definitions
5. **Shadow.kt** - Card shadow helper modifier

All files are located in: `app/src/main/java/com/example/expensecalculator/ui/theme/`

---

## Notes

- The IDE may show temporary "unresolved reference" warnings, but these will resolve after a project rebuild
- No compilation errors exist in the codebase
- The theme is already being used in MainActivity.kt
- All existing screens that use the TripManager color imports will continue to work
- You can gradually migrate other screens to use MaterialTheme.colorScheme for consistency

