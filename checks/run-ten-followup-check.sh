#!/usr/bin/env bash
set -euo pipefail
launcher=com.nthexperience.desktop.ten
collect() {
 mkdir -p screenshots
 for name in $(adb shell run-as "$launcher" ls files 2>/dev/null | tr -d '\r'); do
  case "$name" in ten9-*.png|ten9-*.tsv|storage-*.png|storage-*.txt|explorer-*.png|explorer-*.txt|nth-media-*.png) adb exec-out run-as "$launcher" cat "files/$name" > "screenshots/$name" 2>/dev/null || true;; esac
 done
 adb logcat -d -t 500 > screenshots/followup-device-log.txt || true
}
trap collect EXIT
adb install -r fixture/build/outputs/apk/debug/fixture-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
run() {
 local runner="$1"; shift
 local log="ten-followup-$runner.txt"
 timeout -k 3s 600s adb shell am instrument -w "$@" "$launcher.test/com.nthexperience.desktop.$runner" | tee "$log"
 grep -q 'PASS:' "$log"
 ! grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' "$log"
}
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
run TenAccentSmoke
for runner in MediaSmoke StorageSmoke; do
 adb shell am force-stop "$launcher"
 adb shell pm clear "$launcher"
 adb shell appops set --uid "$launcher" MANAGE_EXTERNAL_STORAGE allow
 if [[ "$runner" == StorageSmoke ]]; then run "$runner" -e suite storage -e phase access; else run "$runner"; fi
done
adb shell am force-stop "$launcher"
adb shell pm clear "$launcher"
run StorageSmoke
