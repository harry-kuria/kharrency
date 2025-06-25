# 🔄 OTA (Over-The-Air) Updates Setup Guide

Your Kharrency app now includes a complete OTA update system that automatically checks for new versions and prompts users to download updates!

## 🌟 Features

- ✅ **Automatic Update Checking**: Checks for updates every 24 hours
- ✅ **Beautiful Update Dialog**: Modern UI with release notes and update info
- ✅ **Force Updates**: Critical updates can be marked as required
- ✅ **GitHub Integration**: Uses your GitHub releases as the update source
- ✅ **Smart Caching**: Prevents excessive API calls
- ✅ **Theme-Aware**: Update dialog matches light/dark mode
- ✅ **Direct Downloads**: Opens browser to download APK directly

## 🔧 How It Works

### 1. **Update Detection**
- App checks GitHub releases API on startup
- Compares current version code with latest release
- Shows update dialog if newer version is available

### 2. **Update Dialog**
- Shows version info, release date, and file size
- Displays formatted release notes
- Provides "Later" and "Download" options
- Force updates only show "Update Now" button

### 3. **Download Process**
- Opens browser to GitHub release page
- User downloads APK directly
- User installs APK (requires "Unknown Sources" permission)

## ⚙️ Configuration

### 1. **Update GitHub API URL**

In `UpdateService.kt`, replace `YOUR_USERNAME` with your actual GitHub username:

```kotlin
private val githubApiUrl = "https://api.github.com/repos/YOUR_GITHUB_USERNAME/kharrency/releases/latest"
```

### 2. **Update Check Frequency**

In `UpdateManager.kt`, you can change how often the app checks for updates:

```kotlin
private val UPDATE_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours
```

### 3. **Force Update Threshold**

In `UpdateService.kt`, you can change when updates become mandatory:

```kotlin
private fun isForceUpdate(currentVersionCode: Int, latestVersionCode: Int): Boolean {
    // Force update if the version difference is more than 3 versions
    return (latestVersionCode - currentVersionCode) > 3
}
```

## 📱 User Experience

### First Time Users
1. Download and install your app
2. App automatically checks for updates on startup
3. If updates available, shows update dialog

### Existing Users
1. App checks for updates every 24 hours
2. Update dialog appears when newer version is found
3. Users can choose "Later" or "Download"
4. Force updates require immediate action

### Update Dialog Features
- **Version Info**: Shows new version number and release date
- **File Size**: Displays APK download size
- **Release Notes**: Formatted changelog from GitHub
- **Smart Buttons**: "Later" for optional, "Update Now" for required
- **Theme Support**: Matches app's light/dark mode

## 🚀 Release Process

### 1. **Create Release with Workflow**
```bash
# Go to GitHub Actions → "Build and Release APK" → "Run workflow"
# Fill in:
# - Version name: 1.0.1
# - Version code: 2 (increment from previous)
# - Release notes: What's new in this version
```

### 2. **Automatic Version Code**
The workflow automatically includes version code in release notes:
```markdown
**Version Code:** 2
```

### 3. **Update Detection**
- Users with version code 1 will see update dialog
- Dialog shows version 1.0.1 is available
- Clicking "Download" opens GitHub release page

## 🎯 Testing Updates

### Test Update Dialog
1. Temporarily lower your app's version code in `build.gradle.kts`
2. Create a release with higher version code
3. Run the app - update dialog should appear
4. Test both "Later" and "Download" buttons

### Test Force Updates
1. Create a release with version code 5+ higher than current
2. Update dialog will show "Update Now" only
3. Back button and outside clicks are disabled

## 🛠️ Customization Options

### Update Dialog Styling
Modify `UpdateDialog.kt` to change:
- Colors and themes
- Button text and styling
- Layout and spacing
- Icon choices

### Update Logic
Modify `UpdateService.kt` to change:
- Update check frequency
- Force update rules
- Release notes parsing
- Error handling

### GitHub Integration
- Works with any GitHub repository
- Parses standard GitHub release format
- Extracts APK download URLs automatically
- Supports custom release note formatting

## 🔒 Security & Best Practices

### APK Signing
- Always use the same keystore for updates
- Users can only install updates with matching signatures
- GitHub releases should always contain signed APKs

### Update Verification
- App checks version codes, not just version names
- Prevents downgrade attacks
- Validates GitHub API responses

### User Permissions
- Requires "Unknown Sources" permission for installation
- Users must manually approve APK installation
- No automatic silent updates (Android security feature)

## 📊 Analytics & Monitoring

### Track Update Adoption
You can add analytics to track:
- How many users see update dialogs
- Update dialog dismiss vs download rates
- Time between release and user updates

### Error Handling
The system handles:
- Network connectivity issues
- GitHub API rate limits
- Malformed release data
- Missing APK files

## 🎉 Benefits

### For Developers
- ✅ No Play Store review delays
- ✅ Instant bug fixes and updates
- ✅ Direct user communication via release notes
- ✅ Full control over update timing

### For Users
- ✅ Faster access to new features
- ✅ Immediate bug fixes
- ✅ Clear information about updates
- ✅ Choice when to update (except force updates)

## 🚨 Important Notes

1. **GitHub Username**: Remember to update the GitHub API URL with your username
2. **Version Codes**: Always increment version codes for each release
3. **APK Signing**: Use the same keystore for all releases
4. **Testing**: Test the update flow before releasing to users
5. **Force Updates**: Use sparingly, only for critical security updates

## 🎯 Next Steps

1. Update the GitHub API URL in `UpdateService.kt`
2. Create your first release using the GitHub workflow
3. Test the update system with a lower version code
4. Monitor user adoption and feedback
5. Iterate and improve based on usage patterns

**Your app now has professional-grade OTA updates! 🚀📱** 