# API Connection Guide

## ✅ **Current Setup - Development Mode**

Your API server is running and configured correctly for development.

### **API Server Status**
```
🚀 UniSync API server running on port 3000
📍 API Base URL: http://localhost:3000/api
🌍 Environment: development
```

### **App Configuration**

The app is currently configured to connect to your local API:

**Debug Build (Development):**
- **API URL:** `http://10.0.2.2:3000/api/`
- **For Android Emulator:** ✅ Works automatically
- **For Physical Device:** Requires your computer's IP address

**Release Build (Production):**
- **API URL:** `https://your-api-domain.com/api/` (needs to be updated)

## 🔧 **Testing Your Setup**

### **Option 1: Android Emulator (Recommended for Development)**

1. **Start your API server:**
   ```bash
   cd api
   npm start
   # or
   npm run dev
   ```

2. **Start Android Emulator:**
   - Open Android Studio
   - Start an emulator

3. **Run the app:**
   - The app will automatically connect to `http://10.0.2.2:3000/api/`
   - `10.0.2.2` is the special IP that maps to `localhost` on your computer

### **Option 2: Physical Device**

If testing on a physical Android device:

1. **Find your computer's IP address:**
   - **Windows:** Open Command Prompt and run `ipconfig`
     - Look for "IPv4 Address" (e.g., `192.168.1.100`)
   - **Mac/Linux:** Run `ifconfig` or `ip addr`
     - Look for your local network IP (e.g., `192.168.1.100`)

2. **Update build.gradle.kts temporarily:**
   ```kotlin
   debug {
       buildConfigField("String", "API_BASE_URL", "\"http://YOUR_IP_ADDRESS:3000/api/\"")
   }
   ```
   Replace `YOUR_IP_ADDRESS` with your actual IP (e.g., `192.168.1.100`)

3. **Ensure your device and computer are on the same network**

4. **Rebuild and run the app**

## ✅ **Verifying Connection**

### **Test API Health Check**

1. **From your computer's browser:**
   ```
   http://localhost:3000/health
   ```
   Should return:
   ```json
   {
     "success": true,
     "message": "UniSync API is running",
     "timestamp": "..."
   }
   ```

2. **From Android Emulator browser:**
   ```
   http://10.0.2.2:3000/health
   ```

### **Test from App**

1. **Run the app in debug mode**
2. **Try to register/login**
3. **Check Logcat for API calls:**
   - Look for Retrofit/OkHttp logs
   - Should show requests to `http://10.0.2.2:3000/api/`

## 🚀 **Production Setup (When Ready)**

When you're ready to deploy to production:

### **1. Deploy Your API**

Deploy your API to a hosting service:
- **Heroku**
- **AWS EC2/Elastic Beanstalk**
- **Google Cloud Run**
- **DigitalOcean**
- **Railway**
- **Render**

### **2. Update Production API URL**

Edit `app/build.gradle.kts`:

```kotlin
release {
    // ... other config ...
    buildConfigField("String", "API_BASE_URL", "\"https://your-production-api.com/api/\"")
}
```

**Important:**
- Use HTTPS (not HTTP) for production
- Ensure your API has a valid SSL certificate
- Test the production API before building release

### **3. Configure API Environment Variables**

On your production server, set:
```env
NODE_ENV=production
JWT_SECRET=your-strong-secret-key
PORT=3000
CORS_ORIGIN=https://your-app-domain.com
```

### **4. Update CORS in API**

In `api/server.js`, ensure CORS is configured for production:
```javascript
const corsOptions = {
  origin: process.env.CORS_ORIGIN || '*', // Set specific origin in production
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization']
};
```

## 📝 **Current Configuration Summary**

| Environment | API URL | Status |
|------------|---------|--------|
| **Development (Debug)** | `http://10.0.2.2:3000/api/` | ✅ Configured |
| **Production (Release)** | `https://your-api-domain.com/api/` | ⚠️ Needs Update |

## 🔍 **Troubleshooting**

### **App Can't Connect to API**

1. **Check API is running:**
   ```bash
   curl http://localhost:3000/health
   ```

2. **Check emulator can reach API:**
   - Open browser in emulator
   - Navigate to `http://10.0.2.2:3000/health`

3. **Check firewall:**
   - Ensure port 3000 is not blocked
   - Windows Firewall may block Node.js

4. **Check network:**
   - For physical device, ensure same Wi-Fi network
   - Some networks block device-to-device communication

### **CORS Errors**

If you see CORS errors:
- API is configured to allow all origins in development
- Check `api/server.js` CORS configuration
- For production, set `CORS_ORIGIN` environment variable

### **Connection Timeout**

- Check API server is actually running
- Verify port 3000 is correct
- Check for firewall blocking
- Try restarting API server

## 📚 **Next Steps**

1. ✅ **Development:** Your setup is ready - just run the app!
2. ⚠️ **Production:** Deploy API and update production URL when ready
3. 📖 **See:** `PRODUCTION_DEPLOYMENT_GUIDE.md` for full deployment guide

---

**Current Status:** ✅ **Ready for Development Testing**

Your API server is running and the app is configured to connect to it. You can now test all features!

