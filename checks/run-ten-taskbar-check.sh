#!/usr/bin/env bash
set -euo pipefail
launcher=com.nthexperience.desktop.ten
collect() {
  mkdir -p screenshots
  for name in $(adb shell run-as "$launcher" ls files 2>/dev/null | tr -d '\r'); do
    case "$name" in ten29-*.png|ten22-*.png|ten24-*.png|ten23-*.png|ten13-*.png|ten14-*.png|ten15-*.png|ten17-*.png) adb exec-out run-as "$launcher" cat "files/$name" > "screenshots/$name" 2>/dev/null || true;; esac
  done
  adb logcat -d -t 300 > screenshots/device-log.txt || true
}
trap collect EXIT
adb install -r fixture/build/outputs/apk/debug/fixture-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
timeout -k 3s 240s adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.TenInteractionSmoke" | tee ten-interaction-smoke.txt
grep -q 'PASS:' ten-interaction-smoke.txt
! grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' ten-interaction-smoke.txt

for runner in TenTaskbarRowsSmoke TenTaskbarSizeSmoke TenWindowPositionSmoke TenInitializationSmoke; do
  log="${runner}.txt"
  timeout -k 3s 300s adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.$runner" | tee "$log"
  grep -q 'PASS:' "$log"
  ! grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' "$log"
done
