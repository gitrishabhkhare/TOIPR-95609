"""
TOI App Inspector — Login & Search screen deep dive.
Run AFTER Appium is up and tunnel is active.
Usage: python3 inspect_login_search.py
"""
import json, urllib.request, urllib.error, time, os, sys, base64

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

def api(path, method="GET", data=None):
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

def source(sid):
    return api(f"/session/{sid}/source")["value"]

def find(sid, strategy, selector):
    try:
        els = api(f"/session/{sid}/elements", "POST", {"using": strategy, "value": selector})["value"]
        return els[0]["ELEMENT"] if els else None
    except:
        return None

def find_all(sid, strategy, selector):
    try:
        return api(f"/session/{sid}/elements", "POST", {"using": strategy, "value": selector})["value"]
    except:
        return []

def tap(sid, eid):
    api(f"/session/{sid}/element/{eid}/click", "POST", {})

def attr(sid, eid, name):
    try:
        return api(f"/session/{sid}/element/{eid}/attribute/{name}")["value"]
    except:
        return ""

def save(name, content):
    path = f"{OUTPUT_DIR}/{name}.xml"
    with open(path, "w") as f:
        f.write(content)
    print(f"  Saved XML: {path}")

def screenshot(sid, name):
    try:
        img = base64.b64decode(api(f"/session/{sid}/screenshot")["value"])
        path = f"{OUTPUT_DIR}/{name}.png"
        with open(path, "wb") as f:
            f.write(img)
        print(f"  Screenshot: {path}")
    except Exception as e:
        print(f"  Screenshot failed: {e}")

def dump_buttons(sid, label=""):
    buttons = find_all(sid, "-ios predicate string", "type == 'XCUIElementTypeButton' AND visible == true")
    print(f"  Buttons found ({label}): {len(buttons)}")
    for b in buttons:
        eid = b["ELEMENT"]
        print(f"    name='{attr(sid, eid, 'name')}'  label='{attr(sid, eid, 'label')}'  value='{attr(sid, eid, 'value')}'")

def dump_textfields(sid, label=""):
    for t in ["XCUIElementTypeTextField", "XCUIElementTypeSecureTextField", "XCUIElementTypeSearchField"]:
        els = find_all(sid, "-ios predicate string", f"type == '{t}' AND visible == true")
        if els:
            print(f"  {t} ({label}): {len(els)}")
            for e in els:
                eid = e["ELEMENT"]
                print(f"    name='{attr(sid, eid, 'name')}'  label='{attr(sid, eid, 'label')}'  placeholder='{attr(sid, eid, 'placeholderValue')}'  value='{attr(sid, eid, 'value')}'")

def dump_statictext(sid, label=""):
    els = find_all(sid, "-ios predicate string", "type == 'XCUIElementTypeStaticText' AND visible == true")
    texts = []
    for e in els:
        eid = e["ELEMENT"]
        v = attr(sid, eid, 'label') or attr(sid, eid, 'value') or attr(sid, eid, 'name')
        if v and v not in texts:
            texts.append(v)
    print(f"  StaticTexts ({label}): {texts}")

# ─────────────────────────────────────────────────────────────────────────────
print("=" * 60)
print("TOI Inspector — Login & Search Screens")
print("=" * 60)

print("\n Creating session...")
try:
    sid = create_session()
    print(f"  Session: {sid}")
except Exception as e:
    print(f"FAILED: {e}")
    sys.exit(1)

time.sleep(4)

# ── SECTION 1: Side Nav ───────────────────────────────────────────────────────
print("\n[1] Tapping side nav (sideNavIconDark)...")
side_nav = find(sid, "accessibility id", "sideNavIconDark")
if side_nav:
    tap(sid, side_nav)
    time.sleep(2)
    screenshot(sid, "06_sidenav")
    save("06_sidenav", source(sid))
    dump_buttons(sid, "side nav open")
    dump_textfields(sid, "side nav open")
    dump_statictext(sid, "side nav open")
else:
    print("  sideNavIconDark not found!")
    save("06_sidenav_notfound", source(sid))

# Look for a search field inside side nav
print("\n[2] Looking for search inside side nav...")
search_field = (find(sid, "-ios predicate string", "type == 'XCUIElementTypeSearchField'") or
                find(sid, "-ios predicate string", "type == 'XCUIElementTypeTextField'") or
                find(sid, "accessibility id", "Search"))
if search_field:
    print(f"  Found search field: {attr(sid, search_field, 'name')}")
    tap(sid, search_field)
    time.sleep(1)
    screenshot(sid, "07_search_active")
    save("07_search_active", source(sid))
    dump_buttons(sid, "search active")
    dump_textfields(sid, "search active")
else:
    print("  No search field in side nav — will look for dedicated Search button")
    search_btn = find(sid, "-ios predicate string",
                      "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'search'")
    if search_btn:
        print(f"  Found search button: {attr(sid, search_btn, 'label')}")
        tap(sid, search_btn)
        time.sleep(2)
        screenshot(sid, "07_search_screen")
        save("07_search_screen", source(sid))
        dump_buttons(sid, "search screen")
        dump_textfields(sid, "search screen")

# Close side nav / go back
print("\n[3] Closing side nav...")
back = (find(sid, "accessibility id", "new backIcon light") or
        find(sid, "accessibility id", "Close") or
        find(sid, "-ios predicate string", "type == 'XCUIElementTypeButton' AND label == 'Cancel'"))
if back:
    tap(sid, back)
    time.sleep(1)
else:
    # swipe right to close drawer
    api(f"/session/{sid}/actions", "POST", {"actions": [{"type": "pointer", "id": "finger1",
        "parameters": {"pointerType": "touch"},
        "actions": [
            {"type": "pointerMove", "duration": 0, "x": 50, "y": 400},
            {"type": "pointerDown", "button": 0},
            {"type": "pointerMove", "duration": 300, "x": 350, "y": 400},
            {"type": "pointerUp", "button": 0}
        ]}]})
    time.sleep(1)

# ── SECTION 2: Profile / Login ────────────────────────────────────────────────
print("\n[4] Tapping profile icon (homePageProfileIcon)...")
profile_btn = find(sid, "accessibility id", "homePageProfileIcon")
if profile_btn:
    tap(sid, profile_btn)
    time.sleep(3)
    screenshot(sid, "08_profile_login")
    save("08_profile_login", source(sid))
    dump_buttons(sid, "profile screen")
    dump_textfields(sid, "profile screen")
    dump_statictext(sid, "profile screen")

    # Look deeper — maybe there's a Sign In / Login button to get to the actual form
    sign_in_btn = (find(sid, "-ios predicate string", "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'sign in'") or
                   find(sid, "-ios predicate string", "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'log in'") or
                   find(sid, "-ios predicate string", "type == 'XCUIElementTypeButton' AND label CONTAINS[cd] 'login'") or
                   find(sid, "-ios predicate string", "type == 'XCUIElementTypeStaticText' AND label CONTAINS[cd] 'sign in'"))
    if sign_in_btn:
        print(f"\n[5] Found sign-in entry point — tapping it...")
        tap(sid, sign_in_btn)
        time.sleep(3)
        screenshot(sid, "09_login_form")
        save("09_login_form", source(sid))
        dump_buttons(sid, "login form")
        dump_textfields(sid, "login form")
        dump_statictext(sid, "login form")
    else:
        print("  No sign-in button found — might already be on login form")
        save("09_login_form_direct", source(sid))
else:
    print("  homePageProfileIcon not found!")

print("\n[Done] Closing session...")
delete_session(sid)
print(f"\nOutputs saved to: {OUTPUT_DIR}/")
