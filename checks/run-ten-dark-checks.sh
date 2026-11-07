#!/usr/bin/env bash
set -euo pipefail
launcher=com.nthexperience.desktop.ten
collect() {
  mkdir -p screenshots
  for name in $(adb shell run-as "$launcher" ls files 2>/dev/null | tr -d '\r'); do
    case "$name" in ten18-*.png|ten18-*.tsv) adb exec-out run-as "$launcher" cat "files/$name" > "screenshots/$name" 2>/dev/null || true;; esac
  done
  if [ -f screenshots/ten18-dark-audit.tsv ]; then awk -F '\t' 'NR==1 || $NF=="FAIL" { print }' screenshots/ten18-dark-audit.tsv; fi
  adb logcat -d -t 400 > screenshots/device-log.txt || true
}
trap collect EXIT
adb install -r fixture/build/outputs/apk/debug/fixture-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard || true
timeout -k 3s 180s adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.TenAccentSmoke" | tee ten-accent-smoke.txt
grep -q 'PASS:' ten-accent-smoke.txt
! grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' ten-accent-smoke.txt
timeout -k 3s 360s adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.TenDarkModeSmoke" | tee ten-dark-mode-smoke.txt
grep -q 'PASS:' ten-dark-mode-smoke.txt
! grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' ten-dark-mode-smoke.txt
