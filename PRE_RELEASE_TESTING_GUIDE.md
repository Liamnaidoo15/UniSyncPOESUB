# Pre-Release Testing Guide

## ✅ **Staging Build Configured**

A staging/pre-release build type has been configured for production-like testing.

## 🎯 **What is Staging Build?**

The staging build is a **production-like build** that:
- ✅ Uses ProGuard/R8 minification (like production)
- ✅ Has resource shrinking enabled
- ✅ Uses production-like optimizations
- ✅ **BUT** is still debuggable for testing
- ✅ Can be installed alongside debug/release builds
- ✅ Has its own application ID suffix (`.staging`)

## 📦 **Build Variants Available**

| Build Type | Application ID | Debuggable | Minified | Use Case |
|------------|---------------|------------|----------|----------|
| **Debug** | `com.example.unisyncpoe.debug` | ✅ Yes | ❌ No | Development |
| **Staging** | `com.example.unisyncpoe.staging` | ✅ Yes | ✅ Yes | Pre-release testing |
| **Release** | `com.example.unisyncpoe` | ❌ No | ✅ Yes | Production |

## 🚀 **Building Staging APK**

### **Option 1: Android Studio**

1. Open **Build Variants** panel (View → Tool Windows → Build Variants)
2. Select **staging** variant for the `app` module
3. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
4. Or use **Run** button to install directly

### **Option 2: Command Line**

```bash
# Build staging APK
./gradlew assembleStaging

# Install staging APK
./gradlew installStaging

# Build staging AAB (for testing)
./gradlew bundleStaging
```

**Output Location:**
- APK: `app/build/outputs/apk/staging/app-staging.apk`
- AAB: `app/build/outputs/bundle/staging/app-staging.aab`

## 🧪 **Testing with Staging Build**

### **Advantages of Staging Build**

1. **Production-like Performance:**
   - Code is minified and obfuscated
   - Resources are shrunk
   - Same optimizations as release

2. **Still Debuggable:**
   - Can attach debugger
   - Can view logs
   - Can use breakpoints
   - Can inspect variables

3. **Separate Installation:**
   - Can install alongside debug/release
   - Won't conflict with other builds
   - Easy to test side-by-side

### **What to Test**

- [ ] **Performance:** App feels fast and responsive
- [ ] **Functionality:** All features work correctly
- [ ] **ProGuard Issues:** No crashes from obfuscation
- [ ] **API Integration:** Connects to API correctly
- [ ] **Offline Mode:** Works when network is unavailable
- [ ] **Push Notifications:** Receive and handle notifications
- [ ] **Biometric Auth:** Fingerprint/face unlock works
- [ ] **Data Persistence:** Data saves and loads correctly
- [ ] **Memory Usage:** No memory leaks
- [ ] **Battery Usage:** Reasonable battery consumption

## ⚙️ **Configuration**

### **Current Staging Configuration**

```kotlin
staging {
    // Production-like optimizations
    isMinifyEnabled = true
    isShrinkResources = true
    isDebuggable = true  // Still debuggable!
    
    // Separate app ID
    applicationIdSuffix = ".staging"
    versionNameSuffix = "-staging"
    
    // API URL (currently local, update for staging server)
    buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000/api/\"")
}
```

### **Updating Staging API URL**

If you have a staging server, update in `app/build.gradle.kts`:

```kotlin
staging {
    // ... other config ...
    buildConfigField("String", "API_BASE_URL", "\"https://staging-api.yourdomain.com/api/\"")
}
```

## 📱 **Installing Staging Build**

### **On Emulator**

```bash
./gradlew installStaging
```

### **On Physical Device**

1. Build APK: `./gradlew assembleStaging`
2. Transfer `app/build/outputs/apk/staging/app-staging.apk` to device
3. Enable "Install from Unknown Sources" if needed
4. Install the APK

### **Side-by-Side Installation**

You can have all three builds installed simultaneously:
- **Debug:** `com.example.unisyncpoe.debug`
- **Staging:** `com.example.unisyncpoe.staging`
- **Release:** `com.example.unisyncpoe`

They appear as separate apps on your device!

## 🔍 **Debugging Staging Build**

### **Enable Logging**

Staging build has logging enabled by default. Check Logcat:

```bash
adb logcat | grep UniSync
```

### **Attach Debugger**

1. Build and install staging build
2. In Android Studio, click **Run → Attach Debugger to Android Process**
3. Select your staging app
4. Set breakpoints and debug as normal

### **Check ProGuard Mapping**

If you encounter crashes, check the ProGuard mapping file:
- Location: `app/build/outputs/mapping/staging/mapping.txt`
- Use this to deobfuscate stack traces

## 🐛 **Common Issues**

### **ProGuard Issues**

If you see crashes related to ProGuard:

1. Check `app/proguard-rules.pro`
2. Add keep rules for affected classes
3. Rebuild staging APK

### **API Connection Issues**

- Verify API URL is correct
- Check network security config
- Ensure API server is running
- Check firewall settings

### **Build Errors**

If build fails:
```bash
# Clean and rebuild
./gradlew clean
./gradlew assembleStaging
```

## 📊 **Comparing Builds**

### **Size Comparison**

| Build Type | APK Size | AAB Size |
|------------|----------|----------|
| Debug | ~XX MB | N/A |
| Staging | ~XX MB (minified) | ~XX MB |
| Release | ~XX MB (minified) | ~XX MB |

### **Performance Comparison**

Test and compare:
- App startup time
- Screen transition speed
- List scrolling performance
- Memory usage
- Battery consumption

## ✅ **Pre-Release Checklist**

Before moving to production release:

- [ ] Staging build compiles successfully
- [ ] All features tested and working
- [ ] No ProGuard-related crashes
- [ ] Performance is acceptable
- [ ] Memory usage is reasonable
- [ ] API integration works correctly
- [ ] Offline mode works
- [ ] Push notifications work
- [ ] Biometric auth works
- [ ] Tested on multiple devices
- [ ] Tested on multiple Android versions
- [ ] No critical bugs found

## 🚀 **Next Steps**

1. **Build Staging APK:**
   ```bash
   ./gradlew assembleStaging
   ```

2. **Install and Test:**
   - Install on test devices
   - Test all features thoroughly
   - Check for any issues

3. **Fix Issues:**
   - Address any bugs found
   - Update ProGuard rules if needed
   - Rebuild and retest

4. **When Ready:**
   - Move to production release build
   - Update production API URL
   - Set up app signing
   - Build final release

## 📝 **Notes**

- Staging build uses the same ProGuard rules as release
- Staging build can be installed alongside other builds
- Staging build is optimized but still debuggable
- Use staging for final testing before production release

---

**Status:** ✅ **Ready for Pre-Release Testing**

Build the staging APK and start testing!

