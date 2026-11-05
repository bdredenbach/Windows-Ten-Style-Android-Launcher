"""Check the APK that Android will install, including both custom runner registrations."""
import os
from pathlib import Path
import subprocess
sdk = Path(os.environ['ANDROID_HOME'])
aapt = sdk / 'build-tools' / '35.0.0' / 'aapt'
def manifest(path):
    return subprocess.check_output([str(aapt), 'dump', 'xmltree', path, 'AndroidManifest.xml'], text=True)
tests = manifest('app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')
for runner in ('TenSmoke', 'TenPolishSmoke'):
    assert 'com.nthexperience.desktop.' + runner in tests, runner + ' is missing'
assert 'com.nthexperience.desktop.ten.test' in tests
assert 'com.nthexperience.desktop.ten' in tests
main = manifest('app/build/outputs/apk/debug/app-debug.apk')
assert 'com.nthexperience.desktop.TenNotifications' in main or '.TenNotifications' in main
assert 'android.permission.BIND_NOTIFICATION_LISTENER_SERVICE' in main
assert 'com.nthexperience.desktop.ten.storage' in main
print('PASS: Ten package, protected notification listener and both native runners are registered')
