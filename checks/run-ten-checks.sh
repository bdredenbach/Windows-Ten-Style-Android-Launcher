#!/usr/bin/env bash
set -euo pipefail
launcher=com.nthexperience.desktop.ten
collect() {
  mkdir -p screenshots
  for name in ten-nota ten-settings ten-explorer ten-task-view ten-action-center ten-start-portrait ten-start-landscape ten-failure; do
    adb exec-out run-as "$launcher" cat "files/$name.png" > "screenshots/$name.png" 2>/dev/null || true
  done
  adb logcat -d -t 500 > screenshots/device-log.txt || true
}
trap collect EXIT
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.TenSmoke" | tee ten-smoke.txt
rg -q 'PASS:' ten-smoke.txt
! rg -q 'FAIL:|shortMsg=|Process crashed' ten-smoke.txt
