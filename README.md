# TOI iOS Automation Framework

Page Object Model (POM) test automation framework for the **Times of India** iOS app, built with **Maven · TestNG · Appium (Selenium WebDriver protocol)**.

---

## Tech Stack

| Tool | Version | Purpose |
|---|---|---|
| Java | 11+ | Language |
| Maven | 3.8+ | Build & dependency management |
| Appium Java Client | 8.6 | iOS driver (XCUITest) |
| Selenium WebDriver | 4.18 | Core WebDriver API |
| TestNG | 7.9 | Test framework |
| Extent Reports | 5.1 | HTML test reports |
| Log4j2 | 2.22 | Logging |
| Jackson | 2.16 | JSON test-data loading |

---

## Project Structure

```
toi-ios-automation/
├── pom.xml
├── src/
│   ├── main/java/com/toi/
│   │   ├── base/
│   │   │   ├── BasePage.java          ← Common element interaction helpers
│   │   │   └── BaseTest.java          ← Driver lifecycle + reporting hooks
│   │   ├── config/
│   │   │   ├── ConfigReader.java      ← Reads config.properties (CLI overrides supported)
│   │   │   └── DriverManager.java     ← Thread-safe IOSDriver management
│   │   ├── listeners/
│   │   │   ├── RetryAnalyzer.java     ← Auto-retry logic (2 retries)
│   │   │   └── RetryListener.java     ← Injects RetryAnalyzer into all tests via XML
│   │   ├── pages/
│   │   │   ├── SplashPage.java
│   │   │   ├── HomePage.java
│   │   │   ├── LoginPage.java
│   │   │   ├── ArticlePage.java
│   │   │   ├── SearchPage.java
│   │   │   ├── MyFeedPage.java
│   │   │   ├── VideoPage.java
│   │   │   ├── CategoryPage.java
│   │   │   ├── CommentsPage.java
│   │   │   ├── SignUpPage.java
│   │   │   └── ForgotPasswordPage.java
│   │   └── utils/
│   │       ├── WaitUtils.java          ← Explicit wait helpers
│   │       ├── ScreenshotUtils.java    ← Screenshot on failure
│   │       ├── ExtentReportManager.java← HTML report generation
│   │       └── TestDataUtils.java      ← JSON test data loader
│   └── test/
│       ├── java/com/toi/tests/
│       │   ├── sanity/
│       │   │   ├── AppLaunchSanityTest.java
│       │   │   ├── LoginSanityTest.java
│       │   │   └── SearchSanityTest.java
│       │   └── regression/
│       │       ├── HomePageRegressionTest.java
│       │       ├── LoginRegressionTest.java
│       │       └── SearchRegressionTest.java
│       └── resources/
│           ├── config.properties
│           ├── log4j2.xml
│           ├── testng-sanity.xml
│           ├── testng-regression.xml
│           ├── testng-all.xml
│           └── testdata/
│               └── login.json
├── reports/    ← Generated HTML reports
├── screenshots/ ← Captured on test failure
└── logs/        ← Rolling log files
```

---

## Prerequisites

1. **Java 11+** installed and `JAVA_HOME` set
2. **Maven 3.8+** installed
3. **Xcode** and **Xcode Command Line Tools** installed
4. **Appium Server 2.x** running: `appium`
5. **Appium XCUITest driver** installed: `appium driver install xcuitest`
6. A physical iOS device or iOS Simulator

---

## Configuration

Edit `src/test/resources/config.properties`:

```properties
appium.server.url=http://127.0.0.1:4723
platform.version=17.0
device.name=iPhone 15
app.bundle.id=com.timesofindia.TOIApp   # real device
# app.path=/path/to/TOIApp.app          # simulator
test.user.email=your@email.com
test.user.password=YourPassword
```

Any key can be overridden at runtime:
```bash
mvn test -Psanity -Ddevice.name="iPhone 14 Pro" -Dplatform.version=16.4
```

---

## Running Tests

### Sanity (quick smoke check)
```bash
mvn test -Psanity
# or
mvn test -Dsuite=sanity
```

### Regression (full coverage)
```bash
mvn test -Pregression
# or
mvn test -Dsuite=regression
```

### All tests (sanity + regression)
```bash
mvn test -Pall
# or
mvn test -Dsuite=all
```

### Run a single test class
```bash
mvn test -Dsuite=sanity -Dtest=LoginSanityTest
```

---

## Adding New Tests

### New Page
1. Create `src/main/java/com/toi/pages/NewPage.java` extending `BasePage`
2. Add locators and action methods
3. Implement `isLoaded()` for page verification

### New Test
1. Create in `src/test/java/com/toi/tests/sanity/` or `.../regression/`
2. Extend `BaseTest`
3. Annotate with `@Test(groups = {"sanity"})` or `@Test(groups = {"regression"})`
4. Add the class to the relevant `testng-*.xml` suite file

---

## Reports & Logs

- **HTML Report**: `reports/TOI_Report_<timestamp>.html`
- **Screenshots**: `screenshots/<testName>_<timestamp>.png` (captured on failure)
- **Logs**: `logs/toi-automation.log`

---

## Parallel Execution

Regression and All suites run with `parallel="classes" thread-count="2"`.
Driver is stored in a `ThreadLocal` so each thread gets its own session.
Increase `thread-count` in the XML to run more classes concurrently.
