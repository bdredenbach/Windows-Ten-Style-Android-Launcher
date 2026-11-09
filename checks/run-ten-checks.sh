#!/usr/bin/env bash
set -euo pipefail
launcher=com.nthexperience.desktop.ten
collect() {
  mkdir -p screenshots
  for name in ten15-nota-open-dark-portrait ten15-nota-open-dark-landscape ten15-nota-open-light-portrait ten15-nota-open-light-landscape ten15-nota-open-aero-portrait ten15-nota-open-aero-landscape ten15-nota-closed-dark-portrait ten15-nota-closed-dark-landscape ten15-nota-closed-light-portrait ten15-nota-closed-light-landscape ten15-nota-closed-aero-portrait ten15-nota-closed-aero-landscape ten14-actions-dark-portrait ten14-actions-dark-landscape ten14-actions-light-portrait ten14-actions-light-landscape ten14-actions-aero-portrait ten14-actions-aero-landscape ten14-actions-closed ten14-original-explorer-icons ten14-original-desktop-icons ten13-fixed-nota-portrait ten13-fixed-nota-landscape ten13-controls-aero ten13-controls-light ten13-new-desktop-guide ten13-android-app-card ten13-shake-restored ten13-failure ten12-explorer-view ten12-recycle-ribbon ten12-components-portrait ten12-components-landscape ten12-features-portrait ten12-features-landscape ten12-lock-portrait ten12-lock-landscape ten12-actions-white ten12-failure ten-nota ten-settings ten-explorer ten-task-view ten-action-center ten-start-portrait ten-start-landscape ten-failure ten-polish-settings-home ten-polish-settings-sidebar ten-polish-desktop-research ten-polish-task-view ten-polish-explorer ten-polish-recycle ten-polish-start-landscape ten-polish-start-portrait ten-polish-notifications ten-polish-browser ten-polish-failure ten-shell-taskbar-portrait ten-shell-taskbar-landscape ten-shell-news-portrait ten-shell-news-landscape ten-shell-nota-context ten-shell-window-lifecycle ten-shell-failure ten4-start-brand ten4-brand-toast ten4-themes-ten ten4-aero-start ten4-colors ten4-themes-saved ten4-run ten4-performance ten4-failure ten5-empty-desktop ten5-dark-run ten5-dark-settings ten5-dark-explorer ten5-light-explorer ten5-aero-apps ten5-failure ten6-theme-entry ten6-aero-colors ten6-failure ten7-feed-error ten7-news-portrait ten7-news-landscape ten7-failure ten8-settings-portrait ten8-settings-landscape ten8-settings-themes ten8-nota-shortcut ten8-nota-capabilities ten8-settings-narrow-dark ten8-failure ten9-portrait-green ten9-portrait-white ten9-portrait-black ten9-landscape-green ten9-landscape-white ten9-landscape-black ten9-start-portrait ten9-calculator-portrait ten9-calculator-landscape ten9-failure ten10-search-portrait ten10-search-landscape ten10-about-portrait ten10-about-landscape ten10-fonts-portrait ten10-fonts-landscape ten10-tasks-portrait ten10-tasks-landscape ten10-winx-portrait ten10-winx-landscape ten10-news-portrait ten10-news-landscape ten10-failure ten11-nota-portrait-dark ten11-nota-portrait-light ten11-nota-landscape-dark ten11-nota-landscape-light; do
    [[ -s "screenshots/$name.png" ]] && continue
    adb exec-out run-as "$launcher" cat "files/$name.png" > "screenshots/$name.png" 2>/dev/null || true
  done
  for number in $(seq -w 0 16); do adb exec-out run-as "$launcher" cat "files/ten6-theme-$number.png" > "screenshots/ten6-theme-$number.png" 2>/dev/null || true; done
  adb exec-out run-as "$launcher" cat files/ten6-theme-matrix.tsv > screenshots/ten6-theme-matrix.tsv 2>/dev/null || true
  adb exec-out run-as "$launcher" cat files/ten7-feed-live.tsv > screenshots/ten7-feed-live.tsv 2>/dev/null || true
  adb exec-out run-as "$launcher" cat files/ten9-accent-matrix.tsv > screenshots/ten9-accent-matrix.tsv 2>/dev/null || true
  for name in $(adb shell run-as "$launcher" ls files 2>/dev/null | tr -d '\r'); do
    case "$name" in ten22-*.png|ten18-*.png|ten18-*.tsv|ten20-*.png|ten21-*.png|ten21-*.tsv) adb exec-out run-as "$launcher" cat "files/$name" > "screenshots/$name" 2>/dev/null || true;; esac
  done
  adb logcat -d -t 500 > screenshots/device-log.txt || true
}
trap collect EXIT
adb install -r fixture/build/outputs/apk/debug/fixture-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell input keyevent KEYCODE_WAKEUP
adb shell wm dismiss-keyguard
failed=0
run_suite() {
  local runner="$1" output="$2"
  if ! timeout 240s adb shell am instrument -w "$launcher.test/com.nthexperience.desktop.$runner" | tee "$output"; then failed=1; fi
  if ! grep -q 'PASS:' "$output"; then failed=1; fi
  if grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' "$output"; then failed=1; fi
}
run_suite TenTaskbarSizeSmoke ten-taskbar-size-smoke.txt
if [[ "$failed" != 0 ]]; then exit "$failed"; fi
if ! timeout -k 3s 1500s adb shell am instrument -w -e orientation "${TEN_THEME_ORIENTATION:-all}" -e mode "${TEN_THEME_MODE:-all}" "$launcher.test/com.nthexperience.desktop.TenThemeTextSmoke" | tee ten-theme-text-smoke.txt; then failed=1; fi
if ! grep -q "PASS:" ten-theme-text-smoke.txt || grep -Eq "FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED" ten-theme-text-smoke.txt; then failed=1; fi
if [[ "$failed" != 0 ]]; then exit "$failed"; fi
# One partition on each OS also runs the complete existing regression set.
if [[ "${TEN_THEME_ORIENTATION:-portrait}" != portrait || "${TEN_THEME_MODE:-light}" != light ]]; then exit 0; fi
run_suite TenNotaSmoke ten-nota-smoke.txt
if [[ "$failed" != 0 ]]; then exit "$failed"; fi
adb shell am force-stop "$launcher"
if ! timeout 240s adb shell am instrument -w -e phase cold "$launcher.test/com.nthexperience.desktop.TenNotaSmoke" | tee ten-nota-cold-smoke.txt; then failed=1; fi
if ! grep -q "PASS:" ten-nota-cold-smoke.txt || grep -Eq "FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED" ten-nota-cold-smoke.txt; then failed=1; fi
if [[ "$failed" != 0 ]]; then exit "$failed"; fi
run_suite TenSmoke ten-smoke.txt
run_suite TenPolishSmoke ten-polish-smoke.txt
run_suite TenShellSmoke ten-shell-smoke.txt
run_suite TenThemeSmoke ten-theme-smoke.txt
run_suite TenWorkspaceSmoke ten-workspace-smoke.txt
run_suite TenThemeMatrixSmoke ten-theme-matrix-smoke.txt
run_suite TenFeedSmoke ten-feed-smoke.txt
run_suite TenCommandsSmoke ten-commands-smoke.txt
run_suite TenAccentSmoke ten-accent-smoke.txt
run_suite TenReferenceSmoke ten-reference-smoke.txt
run_suite TenAuditSmoke ten-audit-smoke.txt
run_suite TenDarkModeSmoke ten-dark-mode-smoke.txt
run_suite TenInteractionSmoke ten-interaction-smoke.txt

collect
bash checks/run-ten-facets.sh || failed=1
exit "$failed"
