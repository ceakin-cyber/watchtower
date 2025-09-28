# Database Encryption Testing Guide

## Overview
This guide provides step-by-step instructions to manually verify that database encryption is working correctly in your incident reporting application.

## Prerequisites
- Android Studio installed
- Device/emulator with API level 24+ (required for Android Keystore features)
- Debug build of the application
- ADB access for database inspection

## Test Categories

### 1. Automated Tests

#### Running Unit Tests
```bash
# Run encryption utility tests
./gradlew test --tests DatabaseEncryptionTest

# Expected output: All tests should PASS
# Tests verify: basic encryption/decryption, Base64 encoding, null handling, special characters
```

#### Running Integration Tests  
```bash
# Run repository integration tests
./gradlew connectedAndroidTest --tests EncryptedRepositoryTest

# Expected output: All tests should PASS
# Tests verify: end-to-end encrypted storage and retrieval
```

#### Running Database Verification Tests
```bash
# Run tests that inspect raw database content
./gradlew connectedAndroidTest --tests DatabaseEncryptionVerificationTest

# Expected output: All tests should PASS
# Critical test: Verifies data is actually encrypted in storage
```

#### Running Migration Tests
```bash
# Run migration functionality tests
./gradlew connectedAndroidTest --tests EncryptionMigrationTest

# Expected output: All tests should PASS
# Tests verify: migration from plain text to encrypted storage
```

### 2. Manual Verification Steps

#### Step 1: Install and Run Application
```bash
# Build and install debug version
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch the application
adb shell am start -n com.example.myapplication/.MainActivity
```

#### Step 2: Create Test Data
1. **Open the application**
2. **Create a new incident with sensitive data:**
   - Location: "Building 7, Room 301 - Classified Area"
   - Description: "Unauthorized access detected. Sensitive documents compromised."
   - Case Number: "CONFIDENTIAL-2024-001"
   - Severity: High

3. **Add evidence to the incident:**
   - File Path: "/secure/classified/evidence.jpg"
   - Description: "Photo of compromised security system"

#### Step 3: Verify Data Storage (Database Inspection)

**Extract and examine the database:**
```bash
# Pull database from device
adb root
adb shell cp /data/data/com.example.myapplication/databases/incident_database /sdcard/
adb pull /sdcard/incident_database ./test_database.db

# Examine database content using SQLite
sqlite3 test_database.db

# Query incidents table
.headers on
.mode column
SELECT id, incident_type, location, description, case_number FROM incidents;
```

**Expected Results:**
- ✅ `incident_type` should be readable plain text
- ✅ `location` should be encrypted Base64 string (NOT plain text)
- ✅ `description` should be encrypted Base64 string (NOT plain text) 
- ✅ `case_number` should be encrypted Base64 string (NOT plain text)

**Example of correct output:**
```
id                     incident_type     location              description           case_number
abc-123-def           Security Breach   aGVsbG8gd29ybGQ=     bXlzZWNyZXR0ZXh0     Y29uZmlkZW50aWFs
```

**Query evidence table:**
```sql
SELECT id, file_name, file_path, description FROM evidence_attachments;
```

**Expected Results:**
- ✅ `file_name` should be readable plain text
- ✅ `file_path` should be encrypted Base64 string
- ✅ `description` should be encrypted Base64 string

#### Step 4: Verify Data Retrieval

**Return to the application and verify you can:**
1. **View the incident** - All fields should display the original plain text values
2. **Search for incidents** - Search should work with original text (not encrypted values)
3. **Edit the incident** - Should be able to modify and save changes
4. **View evidence** - File paths and descriptions should display correctly

#### Step 5: Test Key Persistence

**Restart the application:**
```bash
# Force stop and restart
adb shell am force-stop com.example.myapplication
adb shell am start -n com.example.myapplication/.MainActivity
```

**Verify:**
- ✅ All previously entered data is still readable
- ✅ No error messages about decryption failures
- ✅ You can add new incidents and they work correctly

#### Step 6: Test Migration (If Applicable)

**If you have existing unencrypted data:**
1. **Clear migration state** (for testing):
   ```bash
   adb shell pm clear com.example.myapplication
   ```

2. **Add plain text data directly** (simulate legacy data):
   ```bash
   # This step requires custom test code or database manipulation
   # See EncryptionMigrationTest for automated version
   ```

3. **Launch app and verify migration runs automatically**

4. **Check that old data is now encrypted but still readable**

### 3. Security Verification Checklist

#### ✅ Data Encryption Verification
- [ ] Sensitive fields are Base64-encoded in database
- [ ] Same plaintext encrypts to different ciphertext (due to random IV)
- [ ] Non-sensitive fields remain in plain text
- [ ] Encrypted data is not human-readable when viewed directly

#### ✅ Key Management Verification  
- [ ] App works after restart (keys persist)
- [ ] Different app installations have different keys
- [ ] Keys are not visible in app's SharedPreferences (they're encrypted)

#### ✅ Functional Verification
- [ ] All CRUD operations work correctly
- [ ] Search functionality works on decrypted data
- [ ] Data export/import maintains security
- [ ] Error handling doesn't expose encrypted data

#### ✅ Migration Verification
- [ ] Existing plain text data is migrated to encrypted format
- [ ] Migration is idempotent (can run multiple times safely)
- [ ] Migration state is persisted correctly

### 4. Performance Testing

#### Basic Performance Check
```bash
# Time how long it takes to insert 100 encrypted incidents
# Should be comparable to plain text storage (< 100ms difference)

# Check memory usage during encryption operations
# Should not significantly increase memory footprint
```

### 5. Troubleshooting Common Issues

#### Issue: Tests Fail with "Keystore not available"
**Solution:** 
- Ensure testing on API 24+ device/emulator
- Use hardware device if emulator lacks proper Keystore support

#### Issue: Data appears in plain text in database
**Solution:**
- Verify you're using `EncryptedIncidentRepository` not direct DAO access
- Check that `EncryptionConverters.setEncryption()` is called in database initialization

#### Issue: DecryptionException during retrieval
**Solution:**
- Clear app data and start fresh (indicates key corruption)
- Check device storage space (encryption requires temporary storage)

#### Issue: Migration never completes
**Solution:**
- Check logs for specific error messages
- Verify database is not corrupted
- Clear migration state and retry

### 6. Expected Test Results Summary

**All automated tests should PASS:**
- DatabaseEncryptionTest: 8/8 tests pass
- EncryptedRepositoryTest: 7/7 tests pass  
- DatabaseEncryptionVerificationTest: 6/6 tests pass
- EncryptionMigrationTest: 6/6 tests pass

**Manual verification should confirm:**
- ✅ Sensitive data is encrypted in database storage
- ✅ Application functionality works normally
- ✅ Data survives app restarts
- ✅ Migration handles existing data correctly

### 7. Security Validation

**To confirm encryption is working:**
1. **Database dump should show encrypted data** (Base64 strings)
2. **Encrypted values should be different each time** (random IV)
3. **Keys should not be visible** in any plain text storage
4. **Application should work offline** (keys stored locally)

**Red flags that indicate problems:**
- ❌ Sensitive data visible as plain text in database
- ❌ Same input produces same encrypted output
- ❌ Decryption failures after app restart
- ❌ Performance severely degraded

---

## Quick Verification Command

For a fast overall check, run:
```bash
./gradlew test connectedAndroidTest
```

All tests should pass. If any fail, investigate using the troubleshooting guide above.