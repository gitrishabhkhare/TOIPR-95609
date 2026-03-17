"""
TOI App Inspector — dumps page source (XML hierarchy) for each major screen.
Run AFTER Appium is up and tunnel is active.
Usage: python3 inspect_app.py
"""
import json, urllib.request, urllib.error, time, os, sys

SERVER = "http://127.0.0.1:4723"
OUTPUT_DIR = os.path.expanduser("~/toi-ios-automation/locators")
os.makedirs(OUTPUT_DIR, exist_ok=True)

caps = {
    "platformName": "iOS",
    "appium:platformVersion": "26.3",
    "appium:deviceName": "Rishabh Khare's iPhone",
    "appium:udid": "00008150-0001049A3C0A401C",
    "appium:automationName": "XCUITest",
    "appium:bundleId": "com.2ergoTOI.jayant",
    "appium:noReset": True,
    "appium:fullReset": False,
    "appium:newCommandTimeout": 600,
    "appium:xcodeOrgId": "WPW2K252B9",
    "appium:xcodeSigningId": "iPhone Developer",
    "appium:skipServerInstallation": False,
}

def api(path, method="GET", data=None, session_id=None):
    url = f"{SERVER}{path}"
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(url, data=body,
                                  headers={"Content-Type": "application/json"},
                                  method=method)
    with urllib.request.urlopen(req, timeout=60) as r:
        return json.loads(r.read())

def create_session():
    resp = api("/session", "POST", {"capabilities": {"alwaysMatch": caps}})
    return resp["value"]["sessionId"]

def delete_session(sid):
    api(f"/session/{sid}", "DELETE")

def page_source(sid):
    return api(f"/session/{sid}/source")["value"]

def find_elements(sid, strategy, selector):
    try:
        return api(f"/session/{sid}/elements", "POST",
                   {"using": strategy, "value": selector})["value"]
    except:
        return []

def tap(sid, element_id):
    api(f"/session/{sid}/element/{element_id}/click", "POST", {})

def find_element(sid, strategy, selector):
    els = find_elements(sid, strategy, selector)
    return els[0]["ELEMENT"] if els else None

def save_source(name, source):
    path = f"{OUTPUT_DIR}/{name}.xml"
    with open(path, "w") as f:
        f.write(source)
    print(f"  Saved: {path}")

def screenshot(sid, name):
    try:
        import base64
        resp = api(f"/session/{sid}/screenshot")
        img = base64.b64decode(resp["value"])
        path = f"{OUTPUT_DIR}/{name}.png"
        with open(path, "wb") as f:
            f.write(img)
        print(f"  Screenshot: {path}")
    except Exception as e:
        print(f"  Screenshot failed: {e}")

print("=" * 60)
print("TOI App Inspector")
print("=" * 60)

print("\n[1/6] Creating Appium session...")
try:
    sid = create_session()
    print(f"  Session: {sid}")
except Exception as e:
    print(f"FAILED to create session: {e}")
    sys.exit(1)

time.sleep(4)

# --- SCREEN 1: Launch / Splash / Home ---
print("\n[2/6] Capturing Home/Splash screen...")
screenshot(sid, "01_home")
save_source("01_home", page_source(sid))

# --- Try to find and tap Search ---
print("\n[3/6] Looking for Search...")
search_el = (find_element(sid, "accessibility id", "Search") or
             find_element(sid, "-ios predicate string", "label CONTAINS 'Search'") or
             find_element(sid, "-ios predicate string", "type == 'XCUIElementTypeSearchField'"))
if search_el:
    print("  Found Search element — tapping...")
    tap(sid, search_el)
    time.sleep(2)
    screenshot(sid, "02_search")
    save_source("02_search", page_source(sid))
    # Navigate back
    back = find_element(sid, "accessibility id", "Back")
    if back:
        tap(sid, back)
        time.sleep(2)
else:
    print("  Search not found on this screen")

# --- Try to find Login / Sign In ---
print("\n[4/6] Looking for Login / Sign In...")
login_el = (find_element(sid, "accessibility id", "Sign In") or
            find_element(sid, "accessibility id", "Login") or
            find_element(sid, "-ios predicate string", "label CONTAINS 'Sign In'") or
            find_element(sid, "-ios predicate string", "label CONTAINS 'Login'"))
if login_el:
    print("  Found Login element — tapping...")
    tap(sid, login_el)
    time.sleep(3)
    screenshot(sid, "03_login")
    save_source("03_login", page_source(sid))
    back = (find_element(sid, "accessibility id", "Back") or
            find_element(sid, "accessibility id", "Close") or
            find_element(sid, "-ios predicate string", "label == 'Cancel'"))
    if back:
        tap(sid, back)
        time.sleep(2)
else:
    print("  Login not found — capturing current screen")
    screenshot(sid, "03_no_login")
    save_source("03_login_search", page_source(sid))

# --- Tap first article ---
print("\n[5/6] Looking for article to tap...")
# Try tapping first XCUIElementTypeCell
cells = find_elements(sid, "-ios class chain",
                      "**/XCUIElementTypeCell[`visible == true`]")
if cells:
    first_cell = cells[0]["ELEMENT"]
    print(f"  Found {len(cells)} cells — tapping first...")
    tap(sid, first_cell)
    time.sleep(3)
    screenshot(sid, "04_article")
    save_source("04_article", page_source(sid))
    back = (find_element(sid, "accessibility id", "Back") or
            find_element(sid, "-ios predicate string", "label == 'Back'"))
    if back:
        tap(sid, back)
        time.sleep(2)
else:
    print("  No cells found")

# --- Tab bar items ---
print("\n[6/6] Capturing tab bar...")
tabs = find_elements(sid, "-ios class chain", "**/XCUIElementTypeTabBar/XCUIElementTypeButton")
if tabs:
    print(f"  Found {len(tabs)} tab bar items:")
    for t in tabs:
        try:
            label = api(f"/session/{sid}/element/{t['ELEMENT']}/attribute/label")["value"]
            name = api(f"/session/{sid}/element/{t['ELEMENT']}/attribute/name")["value"]
            print(f"    label='{label}'  name='{name}'")
        except:
            pass
save_source("05_tabbar", page_source(sid))
screenshot(sid, "05_tabbar")

print("\n[Done] Closing session...")
delete_session(sid)
print(f"\nAll outputs saved to: {OUTPUT_DIR}/")
print("Open the .xml files to extract locators, .png files to see screens.")
