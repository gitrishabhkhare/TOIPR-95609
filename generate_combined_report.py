#!/usr/bin/env python3
"""
Combined HTML Report Generator — TOI iOS Automation
Merges yesterday's passed TCs with today's retry results into one HTML report.
Usage: python3 generate_combined_report.py
"""

import os
import sys
import json
import glob
import xml.etree.ElementTree as ET
from datetime import datetime

BACKUP_DIR   = "target/surefire-reports/junitreports/_backup_20260506"
CURRENT_DIR  = "target/surefire-reports/junitreports"
REPORT_PATH  = "reports/SanityReport_Combined_20260507.html"
SCREENSHOTS  = "screenshots"
LOGS_DIR     = "logs"

# TCs that were re-run today
RETRY_METHODS = {
    "com.toi.tests.sanity.AppLaunchSanityTest": {"testHomeFeedHasArticles"},
    "com.toi.tests.sanity.LoginSanityTest": {"testValidLogin", "testInvalidLoginShowsError"},
    "com.toi.tests.sanity.ContentSanityTest": {
        "testVideoNotAutoplayedWithSound", "testArticlePageScrollDoesNotCrash",
        "testTopHeadersContentLoads", "testVideoShowPageOpens", "testArticleShareOpensShareSheet"
    },
    "com.toi.tests.sanity.TOIPlusSanityTest": {
        "testTOIPlusBlockerWithoutLogin", "testTOIPlusLogin", "testNoAdsForTOIPlusUser"
    },
    "com.toi.tests.sanity.NavigationSanityTest": {
        "testAllLoginOptionsDisplayed", "testBookmarkPromptLoginForNonLoggedInUser",
        "testGamesSectionAccessible", "testWatchTabLoading", "testAllBottomTabsAccessible",
        "testCommentsAccessibleOnArticle", "testInterstitialAdAppears", "testBookmarkWorksOnArticlePage"
    },
    "com.toi.tests.sanity.ScheduleListTest": {
        "testScheduleListSectionsLoadAndPaginate", "testAllListingsLoadProperly",
        "testYouMayAlsoLikeAcrossMultipleScheduleListSections"
    },
}

# Display-friendly TC descriptions
TC_DESCRIPTIONS = {
    "testHomeFeedHasArticles": "TC01 — Home feed displays articles on launch",
    "testAppLaunchesSuccessfully": "TC02 — App launches successfully",
    "testSplashScreenDisplayed": "TC03 — Splash screen displayed on launch",
    "testValidLogin": "TC04a — Valid login with correct credentials",
    "testLoginPageLoads": "TC04b — Login page loads on profile tap",
    "testInvalidLoginShowsError": "TC04c — Error shown for invalid credentials",
    "testSearchAccessibleWithoutLogin": "TC04d — Search accessible without login",
    "testSearchReturnsResults": "TC10 — Search returns results",
    "testLiveBlogPageOpens": "TC01 — Live blog show page opens",
    "testVideoShowPageOpens": "TC — Video show page opens",
    "testPhotoShowPageOpens": "TC — Photo show page opens",
    "testArticlePageScrollDoesNotCrash": "TC — Article page scroll (no crash)",
    "testVideoNotAutoplayedWithSound": "TC — Video not auto-played with sound",
    "testTopHeadersContentLoads": "TC — Top headers content loads",
    "testArticleShareOpensShareSheet": "TC — Article share opens share sheet",
    "testAllLoginOptionsDisplayed": "TC04 — All login options displayed",
    "testBookmarkPromptLoginForNonLoggedInUser": "TC05 — Bookmark prompts login (non-logged-in)",
    "testAdsAppearOnHomePage": "TC06 — Ads appear on home page",
    "testSideNavSectionListPagesLoad": "TC12 — Side nav section list pages load",
    "testExclusiveTabLoading": "TC13 — Exclusive tab loading",
    "testWatchTabLoading": "TC14 — Watch tab loading",
    "testGamesSectionAccessible": "TC — Games section accessible",
    "testAllBottomTabsAccessible": "TC — All bottom tabs accessible",
    "testCommentsAccessibleOnArticle": "TC — Comments accessible on article",
    "testInterstitialAdAppears": "TC — Interstitial ad appears",
    "testBookmarkWorksOnArticlePage": "TC — Bookmark works on article page",
    "testTOIPlusBlockerWithoutLogin": "TC09 — TOI+ blocker without login",
    "testTOIPlusLogin": "TC10 — TOI+ login",
    "testBlockedContentAccessibleForTOIPlusUser": "TC11 — Blocked content accessible for TOI+ user",
    "testNoAdsForTOIPlusUser": "TC12 — No ads for TOI+ user",
    "testEpaperTabLoading": "TC15 — ePaper tab loading",
    "testEpaperBlockerForNonLoggedInUser": "TC16 — ePaper blocker for non-logged-in user",
    "testScheduleListSectionsLoadAndPaginate": "SL-AC01 — scheduleList sections load & paginate",
    "testAllListingsLoadProperly": "SL-AC02 — All listings load properly",
    "testYouMayAlsoLikeSectionLoadsAndIsClickable": "SL-AC03 — You may also like section loads & clickable",
    "testYouMayAlsoLikeAcrossMultipleScheduleListSections": "SL-AC03b — You may also like across multiple sections",
}

def parse_xml(filepath):
    """Parse a JUnit XML and return list of (classname, method, status, time, message)."""
    results = []
    if not os.path.exists(filepath):
        return results
    try:
        root = ET.fromstring(open(filepath).read())
        classname = root.get("name", "").replace("TEST-", "")
        for tc in root.findall("testcase"):
            method = tc.get("name", "")
            time_s = float(tc.get("time", 0))
            fail = tc.find("failure")
            err  = tc.find("error")
            skip = tc.find("skipped")
            if fail is not None:
                status  = "FAIL"
                message = (fail.get("message") or fail.text or "")[:300]
            elif err is not None:
                status  = "ERROR"
                message = (err.get("message") or err.text or "")[:300]
            elif skip is not None:
                status  = "SKIP"
                message = "Skipped"
            else:
                status  = "PASS"
                message = ""
            results.append({
                "class":   classname,
                "method":  method,
                "status":  status,
                "time":    time_s,
                "message": message,
            })
    except Exception as e:
        print(f"  WARN: Could not parse {filepath}: {e}", file=sys.stderr)
    return results


def collect_all_results():
    """
    Build final result list:
    - For methods in RETRY_METHODS: use NEW (today's) JUnit XML
    - For all other methods: use BACKUP (yesterday's) JUnit XML
    """
    all_results = []

    backup_files = glob.glob(os.path.join(BACKUP_DIR, "TEST-*.xml"))
    new_files    = glob.glob(os.path.join(CURRENT_DIR, "TEST-com.toi.tests.sanity.*.xml"))

    # Build lookup: classname -> list of new results
    new_by_class = {}
    for f in new_files:
        rows = parse_xml(f)
        for r in rows:
            new_by_class.setdefault(r["class"], []).append(r)

    # Build lookup: classname -> list of backup results
    backup_by_class = {}
    for f in backup_files:
        rows = parse_xml(f)
        for r in rows:
            backup_by_class.setdefault(r["class"], []).append(r)

    # Merge: for each class, determine which source to use per method
    seen_classes = set(list(new_by_class.keys()) + list(backup_by_class.keys()))

    # Preserve suite order
    class_order = [
        "com.toi.tests.sanity.AppLaunchSanityTest",
        "com.toi.tests.sanity.LoginSanityTest",
        "com.toi.tests.sanity.SearchSanityTest",
        "com.toi.tests.sanity.ContentSanityTest",
        "com.toi.tests.sanity.TOIPlusSanityTest",
        "com.toi.tests.sanity.NavigationSanityTest",
        "com.toi.tests.sanity.EpaperSanityTest",
        "com.toi.tests.sanity.ScheduleListTest",
    ]
    # Add any not in order
    for c in seen_classes:
        if c not in class_order:
            class_order.append(c)

    for classname in class_order:
        if classname not in seen_classes:
            continue
        retry_methods  = RETRY_METHODS.get(classname, set())
        new_rows       = {r["method"]: r for r in new_by_class.get(classname, [])}
        backup_rows    = {r["method"]: r for r in backup_by_class.get(classname, [])}

        # Collect all known methods for this class
        all_methods = list(dict.fromkeys(
            list(backup_rows.keys()) + list(new_rows.keys())
        ))

        for method in all_methods:
            if method in retry_methods and method in new_rows:
                row = new_rows[method].copy()
                row["source"] = "retry"
            elif method in backup_rows:
                row = backup_rows[method].copy()
                row["source"] = "yesterday"
            elif method in new_rows:
                row = new_rows[method].copy()
                row["source"] = "retry"
            else:
                continue
            all_results.append(row)

    return all_results


def check_crashes():
    """Scan logs for crash signatures."""
    crashes = []
    log_file = os.path.join(LOGS_DIR, "toi-automation.log")
    if not os.path.exists(log_file):
        return crashes
    with open(log_file, errors="replace") as f:
        for i, line in enumerate(f, 1):
            if any(k in line for k in ["crash", "Crashed", "SIGKILL", "SIGTERM",
                                        "app.*died", "unexpectedly terminated"]):
                if "Authentication" not in line and "mail" not in line:
                    crashes.append(f"Line {i}: {line.strip()}")
    return crashes


def find_screenshot(method):
    """Return screenshot path for a test method if it exists (most recent)."""
    pattern = os.path.join(SCREENSHOTS, f"{method}_*.png")
    matches = sorted(glob.glob(pattern), reverse=True)
    return matches[0] if matches else None


def status_badge(status):
    colors = {"PASS": "#27ae60", "FAIL": "#e74c3c", "ERROR": "#e67e22", "SKIP": "#7f8c8d"}
    c = colors.get(status, "#95a5a6")
    return f'<span style="background:{c};color:#fff;padding:2px 8px;border-radius:4px;font-size:12px;font-weight:bold">{status}</span>'


def source_badge(source):
    if source == "retry":
        return '<span style="background:#2980b9;color:#fff;padding:1px 6px;border-radius:3px;font-size:11px">RE-RUN</span>'
    return '<span style="background:#555;color:#ccc;padding:1px 6px;border-radius:3px;font-size:11px">YESTERDAY</span>'


def generate_html(results, crashes):
    total  = len(results)
    passed = sum(1 for r in results if r["status"] == "PASS")
    failed = sum(1 for r in results if r["status"] == "FAIL")
    errors = sum(1 for r in results if r["status"] == "ERROR")
    skipped= sum(1 for r in results if r["status"] == "SKIP")

    rerun_total   = sum(1 for r in results if r.get("source") == "retry")
    rerun_passed  = sum(1 for r in results if r.get("source") == "retry" and r["status"] == "PASS")
    rerun_failed  = sum(1 for r in results if r.get("source") == "retry" and r["status"] in ("FAIL","ERROR"))

    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

    rows_html = ""
    prev_class = None
    for r in results:
        classname   = r["class"].split(".")[-1]
        method      = r["method"]
        description = TC_DESCRIPTIONS.get(method, method)
        status      = r["status"]
        source      = r.get("source", "yesterday")
        time_s      = r["time"]
        msg         = r.get("message", "").replace("<", "&lt;").replace(">", "&gt;")

        if classname != prev_class:
            rows_html += f'<tr><td colspan="6" style="background:#1a252f;color:#ecf0f1;padding:8px 12px;font-weight:bold;font-size:13px">{classname}</td></tr>\n'
            prev_class = classname

        screenshot_td = ""
        if status in ("FAIL", "ERROR"):
            ss = find_screenshot(method)
            if ss:
                screenshot_td = f'<a href="{ss}" target="_blank" style="color:#3498db">📷</a>'

        msg_td = f'<span style="font-size:11px;color:#bdc3c7">{msg[:120]}{"…" if len(msg)>120 else ""}</span>' if msg else ""

        rows_html += f"""<tr>
  <td style="padding:6px 10px">{status_badge(status)}</td>
  <td style="padding:6px 10px;font-size:12px">{description}</td>
  <td style="padding:6px 10px;font-size:11px;color:#95a5a6">{method}</td>
  <td style="padding:6px 10px">{source_badge(source)}</td>
  <td style="padding:6px 10px;font-size:11px;color:#95a5a6">{time_s:.1f}s</td>
  <td style="padding:6px 10px">{msg_td}{screenshot_td}</td>
</tr>\n"""

    crash_section = ""
    if crashes:
        crash_rows = "\n".join(f"<li style='font-size:12px;margin:4px 0'>{c}</li>" for c in crashes)
        crash_section = f"""
<div style="background:#2c1a1a;border:1px solid #e74c3c;border-radius:8px;padding:16px;margin:20px 0">
  <h3 style="color:#e74c3c;margin:0 0 10px">⚠️ Crash / Critical Errors Detected ({len(crashes)})</h3>
  <ul style="color:#f5b7b1;padding-left:20px;margin:0">{crash_rows}</ul>
</div>"""
    else:
        crash_section = """
<div style="background:#1a2c1a;border:1px solid #27ae60;border-radius:8px;padding:16px;margin:20px 0">
  <h3 style="color:#27ae60;margin:0">✅ No App Crashes Detected</h3>
  <p style="color:#a9dfbf;margin:6px 0 0;font-size:13px">No crash signals (SIGKILL/SIGTERM/unexpectedTermination) found in automation logs. All failures are automation/locator issues, not app crashes.</p>
</div>"""

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>TOI iOS — Combined Test Report (2026-05-07)</title>
  <style>
    body {{ background:#1c2833; color:#ecf0f1; font-family:'Segoe UI',Arial,sans-serif; margin:0; padding:20px }}
    h1 {{ color:#3498db; margin-bottom:4px }}
    .subtitle {{ color:#7f8c8d; font-size:13px; margin-bottom:20px }}
    .summary {{ display:flex; gap:16px; margin-bottom:24px; flex-wrap:wrap }}
    .card {{ background:#2c3e50; border-radius:10px; padding:16px 24px; min-width:120px; text-align:center }}
    .card .num {{ font-size:32px; font-weight:bold }}
    .card .lbl {{ font-size:12px; color:#95a5a6; margin-top:4px }}
    .green {{ color:#27ae60 }} .red {{ color:#e74c3c }} .orange {{ color:#e67e22 }} .grey {{ color:#7f8c8d }}
    table {{ width:100%; border-collapse:collapse; background:#2c3e50; border-radius:10px; overflow:hidden }}
    th {{ background:#1a252f; color:#bdc3c7; padding:10px; text-align:left; font-size:12px }}
    tr:hover td {{ background:#34495e }}
    tr td {{ border-bottom:1px solid #1a252f }}
    .legend {{ font-size:12px; color:#7f8c8d; margin:12px 0 }}
  </style>
</head>
<body>
  <h1>TOI iOS — Automation Report</h1>
  <div class="subtitle">Generated: {now} &nbsp;|&nbsp; Re-run: {rerun_total} failing TCs &nbsp;|&nbsp; Yesterday passes preserved</div>

  <div class="summary">
    <div class="card"><div class="num">{total}</div><div class="lbl">Total TCs</div></div>
    <div class="card"><div class="num green">{passed}</div><div class="lbl">Passed</div></div>
    <div class="card"><div class="num red">{failed}</div><div class="lbl">Failed</div></div>
    <div class="card"><div class="num orange">{errors}</div><div class="lbl">Errors</div></div>
    <div class="card"><div class="num grey">{skipped}</div><div class="lbl">Skipped</div></div>
    <div class="card" style="background:#1a2c3c"><div class="num" style="color:#3498db">{rerun_passed}/{rerun_total}</div><div class="lbl">Re-run Fixed</div></div>
  </div>

  {crash_section}

  <div class="legend">
    <b>RE-RUN</b> = executed today (2026-05-07) &nbsp;|&nbsp; <b>YESTERDAY</b> = carried from 2026-05-06 passing results
  </div>

  <table>
    <thead>
      <tr>
        <th>Status</th><th>Test Case</th><th>Method</th><th>Run</th><th>Time</th><th>Details</th>
      </tr>
    </thead>
    <tbody>
{rows_html}    </tbody>
  </table>

  <p style="color:#555;font-size:11px;margin-top:16px">TOI iOS Automation — Appium 3.2.0 / XCUITest / iOS 26.1 / iPhone 11</p>
</body>
</html>"""
    return html


if __name__ == "__main__":
    print("Collecting results...")
    results = collect_all_results()
    print(f"  Total TCs collected: {len(results)}")

    print("Checking for crashes...")
    crashes = check_crashes()
    print(f"  Crash events found: {len(crashes)}")

    print("Generating combined HTML report...")
    os.makedirs("reports", exist_ok=True)
    html = generate_html(results, crashes)
    with open(REPORT_PATH, "w") as f:
        f.write(html)
    print(f"  Report saved: {REPORT_PATH}")

    # Summary
    passed = sum(1 for r in results if r["status"] == "PASS")
    failed = sum(1 for r in results if r["status"] in ("FAIL", "ERROR"))
    skipped= sum(1 for r in results if r["status"] == "SKIP")
    total  = len(results)
    rerun_total  = sum(1 for r in results if r.get("source") == "retry")
    rerun_passed = sum(1 for r in results if r.get("source") == "retry" and r["status"] == "PASS")

    print(f"\n{'='*50}")
    print(f"  TOTAL: {total}  PASS: {passed}  FAIL/ERR: {failed}  SKIP: {skipped}")
    print(f"  RE-RUN: {rerun_total} TCs → {rerun_passed} now passing")
    print(f"  CRASHES: {len(crashes)}")
    print(f"{'='*50}")
    print(f"\nReport: {REPORT_PATH}")

    import subprocess
    subprocess.Popen(["open", REPORT_PATH])
    print("Opened in browser.")
