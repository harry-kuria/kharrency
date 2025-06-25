# 🚀 Release Setup Guide

This guide explains how to set up the automated release workflow for Kharrency that builds, signs, and publishes APK files to GitHub Releases.

## 📋 Prerequisites

1. **Android Keystore**: You need a keystore file to sign your APK
2. **GitHub Repository**: Push your code to GitHub
3. **GitHub Secrets**: Configure signing credentials

## 🔐 Step 1: Create Android Keystore

If you don't have a keystore yet, create one:

```bash
keytool -genkey -v -keystore kharrency-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias kharrency
```

**Important**: Save the passwords you use! You'll need them for GitHub secrets.

## 🔑 Step 2: Configure GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions → New repository secret

Add these 4 secrets:

### 1. `KEYSTORE_BASE64`
Convert your keystore to base64:
```bash
base64 -i kharrency-release.jks | pbcopy  # macOS
base64 -i kharrency-release.jks           # Linux
```
Paste the entire base64 string as the secret value.

### 2. `SIGNING_KEY_ALIAS`
The alias you used when creating the keystore (e.g., `kharrency`)

### 3. `SIGNING_KEY_PASSWORD`
The password for your key alias

### 4. `SIGNING_STORE_PASSWORD`
The password for your keystore file

## 🎯 Step 3: Run the Release Workflow

1. Go to your GitHub repository
2. Click **Actions** tab
3. Find **"Build and Release APK"** workflow
4. Click **"Run workflow"**
5. Fill in the form:
   - **Version name**: e.g., `1.0.0`
   - **Version code**: e.g., `1` (increment for each release)
   - **Release notes**: Describe what's new in this version

6. Click **"Run workflow"** button

## 📱 Step 4: Download and Share

After the workflow completes (5-10 minutes):

1. Go to **Releases** section of your repository
2. Find your new release (e.g., `v1.0.0`)
3. Download the APK: `Kharrency-v1.0.0.apk`
4. Share the release URL with others!

## 🔗 Sharing the APK

The release URL will look like:
```
https://github.com/yourusername/kharrency/releases/tag/v1.0.0
```

Users can:
- Download the APK directly from GitHub
- Install it on Android devices (requires enabling "Unknown sources")
- See release notes and app features

## 🛠️ Workflow Features

The automated workflow:
- ✅ Builds the app with the specified version
- ✅ Signs the APK with your keystore
- ✅ Creates a GitHub release with tag
- ✅ Uploads the signed APK as a downloadable asset
- ✅ Includes detailed release notes with features
- ✅ Provides installation instructions

## 🔧 Troubleshooting

### Build Fails
- Check that all GitHub secrets are correctly set
- Ensure keystore passwords are correct
- Verify the keystore base64 encoding is complete

### Signing Fails
- Double-check the key alias name
- Verify all passwords match your keystore
- Make sure the keystore file was created properly

### Release Creation Fails
- Ensure you have write permissions to the repository
- Check that the tag doesn't already exist
- Verify the version format (use semantic versioning)

## 📝 Version Management

Follow semantic versioning:
- **Major**: `2.0.0` (breaking changes)
- **Minor**: `1.1.0` (new features)
- **Patch**: `1.0.1` (bug fixes)

Version codes should increment with each release:
- v1.0.0 → versionCode: 1
- v1.0.1 → versionCode: 2
- v1.1.0 → versionCode: 3

## 🎉 Success!

Once set up, you can create new releases with just a few clicks! The workflow handles all the complex build and signing processes automatically.

**Happy releasing! 🚀📱** 