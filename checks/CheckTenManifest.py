"""Check the APK that Android will install, including both custom runner registrations."""
import os
from pathlib import Path
import subprocess
sdk = Path(os.environ['ANDROID_HOME'])
aapt = sdk / 'build-tools' / '35.0.0' / 'aapt'
def manifest(path):
    return subprocess.check_output([str(aapt), 'dump', 'xmltree', path, 'AndroidManifest.xml'], text=True)
tests = manifest('app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')
for runner in ('TenSmoke', 'TenPolishSmoke', 'TenShellSmoke', 'TenThemeSmoke', 'TenWorkspaceSmoke', 'TenThemeMatrixSmoke', 'TenFeedSmoke', 'TenCommandsSmoke', 'TenAccentSmoke', 'TenReferenceSmoke', 'TenAuditSmoke', 'TenInteractionSmoke', 'TenDarkModeSmoke', 'TenNotaSmoke', 'TenThemeTextSmoke'):
    assert 'com.nthexperience.desktop.' + runner in tests, runner + ' is missing'
assert 'com.nthexperience.desktop.ten.test' in tests
assert 'com.nthexperience.desktop.ten' in tests
main = manifest('app/build/outputs/apk/debug/app-debug.apk')
assert 'com.nthexperience.desktop.TenNotifications' in main or '.TenNotifications' in main
assert 'android.permission.BIND_NOTIFICATION_LISTENER_SERVICE' in main
assert 'com.nthexperience.desktop.ten.storage' in main
assert 'com.nthexperience.desktop.NotaTextActivity' in main or '.NotaTextActivity' in main
assert 'android.intent.action.PROCESS_TEXT' in main
print('PASS: Ten package, protected notification listener and twelve native runners and Nota text selection are registered')
