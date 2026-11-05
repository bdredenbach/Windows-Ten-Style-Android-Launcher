#!/usr/bin/env bash
set -euo pipefail
launcher=com.nthexperience.desktop.ten
collect() {
  mkdir -p screenshots
  for name in ten-nota ten-settings ten-explorer ten-task-view ten-action-center ten-start-portrait ten-start-landscape ten-failure ten-polish-settings-home ten-polish-settings-sidebar ten-polish-desktop-research ten-polish-task-view ten-polish-explorer ten-polish-recycle ten-polish-start-landscape ten-polish-start-portrait ten-polish-notifications ten-polish-browser ten-polish-failure ten-shell-taskbar-portrait ten-shell-taskbar-landscape ten-shell-news-portrait ten-shell-news-landscape ten-shell-nota-context ten-shell-window-lifecycle ten-shell-failure; do
    adb exec-out run-as "$launcher" cat "files/$name.png" > "screenshots/$name.png" 2>/dev/null || true
  done
  adb logcat -d -t 500 > screenshots/device-log.txt || true
}
trap collect EXIT
adb install -r fixture/build/outputs/apk/debug/fixture-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
failed=0
run_suite() {
  local runner="$1" output="$2"
  if ! timeout 240s adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.$runner" | tee "$output"; then failed=1; fi
  if ! grep -q 'PASS:' "$output"; then failed=1; fi
  if grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' "$output"; then failed=1; fi
}
run_suite TenSmoke ten-smoke.txt
run_suite TenPolishSmoke ten-polish-smoke.txt
run_suite TenShellSmoke ten-shell-smoke.txt
exit "$failed"
