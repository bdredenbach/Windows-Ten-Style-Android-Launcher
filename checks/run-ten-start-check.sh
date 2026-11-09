#!/usr/bin/env bash
set -euo pipefail
launcher=com.nthexperience.desktop.ten
collect() {
  mkdir -p screenshots
  for name in $(timeout 15s adb shell run-as "$launcher" ls files 2>/dev/null | tr -d '\r'); do
    case "$name" in ten25-*.png|ten24-*.png|ten22-*.png) timeout 15s adb exec-out run-as "$launcher" cat "files/$name" > "screenshots/$name" 2>/dev/null || break;; esac
  done
  timeout 15s adb logcat -d -t 300 > screenshots/device-log.txt || true
}
trap collect EXIT
adb install -r fixture/build/outputs/apk/debug/fixture-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
for runner in TenStartTilesSmoke TenWindowPositionSmoke TenTaskbarSizeSmoke; do
  log=ten-start-${runner}.txt
  timeout -k 3s 240s adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.$runner" | tee "$log"
  grep -q 'PASS:' "$log"
  ! grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' "$log"
done
