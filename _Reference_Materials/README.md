# Reference Materials

**Private reference documentation for Hytale-Takaro Integration development**

> ⚠️ **Note**: These files are NOT in the GitHub repository. They're kept locally for reference.

---

## 📁 Files in This Directory

### 1. **Hytale_Server_Manual.md**
Complete official Hytale server documentation covering:
- Server setup and installation
- Authentication and configuration
- Multiserver architecture
- Tips, tricks, and best practices
- Future features and API endpoints

**Source**: Official Hytale Support Documentation
**Last Updated**: January 2026

### 2. **REQUIRED_DOWNLOADS.md**
Checklist of all downloads needed to run the integration:
- Java 25 from Adoptium
- Hytale server files
- Takaro authentication tokens
- Hytale server authentication
- Optional plugins and tools

**Purpose**: Track what you've downloaded and what's still needed
**Use**: Check off items as you complete them

### 3. **QUICK_REFERENCE.md**
Fast lookup guide for common tasks:
- Quick start commands
- Important file paths
- Configuration examples
- Troubleshooting steps
- Common tasks and pro tips

**Purpose**: Quick answers without searching through full docs
**Use**: Keep this open while working on the server

### 4. **README.md** (This File)
Index of reference materials with descriptions

---

## 🗂️ Directory Structure

```
_Reference_Materials/
├── README.md                    # This file - index of reference docs
├── Hytale_Server_Manual.md      # Full official server documentation
├── REQUIRED_DOWNLOADS.md         # Download checklist and links
└── QUICK_REFERENCE.md            # Fast lookup guide
```

---

## 📚 Related Documentation

### In GitHub Repository
These files ARE in the public GitHub repo:

- **[README.md](../HytaleTakaroMod/README.md)** - Main project documentation
- **[IMPLEMENTATION_STATUS.md](../HytaleTakaroMod/IMPLEMENTATION_STATUS.md)** - Current development status
- **[HYTALE_API_INTEGRATION.md](../HytaleTakaroMod/HYTALE_API_INTEGRATION.md)** - Complete API reference
- **[CONTRIBUTING.md](../HytaleTakaroMod/CONTRIBUTING.md)** - Contribution guidelines
- **[LICENSE](../HytaleTakaroMod/LICENSE)** - MIT License

### External Resources
- **Hytale Official**: https://hytale.com/
- **Takaro Platform**: https://takaro.dev/
- **Java 25 (Adoptium)**: https://adoptium.net/
- **GitHub Repository**: https://github.com/mad-001/Hytale-Takaro-Integration

---

## 🔗 Important Links from Server Manual

### Authentication & Accounts
- **Device Auth**: https://accounts.hytale.com/device
- **Hytale API**: https://api.hytale.com

### Downloads
- **Java 25**: https://adoptium.net/
- **Hytale Downloader**: `hytale-downloader.zip` (Linux & Windows)
  - Source: Official Hytale documentation/support

### Documentation
- **JVM Parameters**: https://www.baeldung.com/jvm-parameters
- **JEP-514 (AOT)**: https://openjdk.org/jeps/514

---

## 🎯 When to Use Each File

### Starting Fresh?
1. Read **REQUIRED_DOWNLOADS.md** first
2. Download everything needed
3. Follow **QUICK_REFERENCE.md** for setup

### Running into Issues?
1. Check **QUICK_REFERENCE.md** troubleshooting section
2. Search **Hytale_Server_Manual.md** for specific topics
3. Review plugin docs in GitHub repo

### Need API Details?
1. Check **HYTALE_API_INTEGRATION.md** in GitHub repo
2. Reference **Hytale_Server_Manual.md** for endpoint details

### Want to Contribute?
1. Read **CONTRIBUTING.md** in GitHub repo
2. Check **IMPLEMENTATION_STATUS.md** for what needs work

---

## 🔐 Security Notes

These reference files may contain:
- ❌ DO NOT commit authentication tokens
- ❌ DO NOT commit API keys
- ❌ DO NOT commit server passwords
- ✅ Safe to share: Documentation and guides
- ✅ Safe to share: Configuration templates (without tokens)

**Why These Files Aren't in Git:**
- They're reference materials, not source code
- They may contain personal notes
- They include download links that could become outdated
- Keeps the GitHub repo focused on code and public docs

---

## 📌 Quick Access

### Most Used Commands

```bash
# Navigate to reference materials
cd /home/zmedh/Takaro-Projects/Hytale/Hytale-Takaro-Integration/_Reference_Materials

# View files
cat QUICK_REFERENCE.md
cat REQUIRED_DOWNLOADS.md

# Search documentation
grep -i "authentication" *.md
grep -i "port" *.md
```

### Open in Editor

```bash
# VS Code
code .

# Nano
nano QUICK_REFERENCE.md

# Less (read-only)
less Hytale_Server_Manual.md
```

---

## 🔄 Keeping Updated

### When Hytale Updates
- [ ] Check for new server manual version
- [ ] Update **Hytale_Server_Manual.md**
- [ ] Review **REQUIRED_DOWNLOADS.md** for new dependencies
- [ ] Update **QUICK_REFERENCE.md** with new features

### When Plugin Updates
- [ ] Update **IMPLEMENTATION_STATUS.md** in GitHub repo
- [ ] Reflect changes in **QUICK_REFERENCE.md** if needed
- [ ] Add new API endpoints to **HYTALE_API_INTEGRATION.md**

---

## 💾 Backup Strategy

These files are not version-controlled. Consider backing them up:

```bash
# Create timestamped backup
tar -czf reference_backup_$(date +%Y%m%d).tar.gz _Reference_Materials/

# Or copy to safe location
cp -r _Reference_Materials ~/Backups/hytale-reference/
```

---

## 📞 Support

If you need help:
1. **Documentation Issues**: Check GitHub Issues
2. **Hytale Server Help**: Official Hytale support
3. **Takaro Issues**: Takaro support (https://takaro.dev/)
4. **Plugin Bugs**: GitHub Issues
5. **General Questions**: GitHub Discussions

---

**Created**: January 2026
**Location**: `/home/zmedh/Takaro-Projects/Hytale/Hytale-Takaro-Integration/_Reference_Materials/`
**Status**: Private reference documentation (not in Git)
