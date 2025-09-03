# Development Setup - Auto-Reload

## IntelliJ IDEA Auto-Reload Configuration

### 1. Enable Auto-Compile
- Go to **Settings** → **Build, Execution, Deployment** → **Compiler**
- Check **"Build project automatically"**

### 2. Enable Runtime Auto-Make
**Modern IntelliJ (2021.2+):**
- Same **Compiler** settings page or **advanced settings**
- Check **"Allow auto-make to start even if developed application is currently running"**

**Older IntelliJ:**
- Press **Ctrl+Shift+A** (Cmd+Shift+A on Mac) → Search "Registry"
- Find `compiler.automake.allow.when.app.running` → **Enable**

### 3. Run Application
- Run your application in **Debug mode** (not Run mode)
- Changes to `.kt` files will auto-reload
- Thymeleaf templates hot-reload automatically

### 4. What Auto-Reloads
✅ Kotlin source code  
✅ HTML templates  
✅ Static resources  
❌ Configuration files (requires restart)

The application uses Spring Boot DevTools for hot-reload functionality.