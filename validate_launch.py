"""
Quick Appium launch validation — no Maven/Java compile needed.
Launches TOI app on Harsh's iPhone and confirms it starts.
"""
import json, urllib.request, urllib.error, time

SERVER = "http://127.0.0.1:4723"

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
    # WDA signing — use your Apple Developer Team ID
    "appium:xcodeOrgId": "WPW2K252B9",
    "appium:xcodeSigningId": "iPhone Developer",
    "appium:showXcodeLog": True,
    "appium:skipServerInstallation": False,
}

payload = json.dumps({"capabilities": {"alwaysMatch": caps}}).encode()
req = urllib.request.Request(f"{SERVER}/session",
                              data=payload,
                              headers={"Content-Type": "application/json"},
                              method="POST")

print("Creating Appium session — launching TOI app...")
try:
    with urllib.request.urlopen(req, timeout=600) as resp:
        body = json.loads(resp.read())
        session_id = body["value"]["sessionId"]
        print(f"SUCCESS — Session ID: {session_id}")
        print("TOI app launched on device!")

        time.sleep(3)   # let the app settle

        # Delete the session (close app)
        del_req = urllib.request.Request(f"{SERVER}/session/{session_id}",
                                          method="DELETE")
        urllib.request.urlopen(del_req, timeout=30)
        print("Session closed.")

except urllib.error.HTTPError as e:
    err = json.loads(e.read())
    print(f"FAILED — {err.get('value', {}).get('message', str(e))}")
except Exception as e:
    print(f"ERROR — {e}")
