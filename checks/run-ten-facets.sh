#!/usr/bin/env bash
# Disposable emulator regression of the shared engines underneath the Ten shell.
set -euo pipefail
launcher=com.nthexperience.desktop.ten
runner="$launcher.test/com.nthexperience.desktop"
failed=0
collect_facet() {
  local suite="$1" name
  mkdir -p "screenshots/facets/$suite"
  for name in $(adb shell run-as "$launcher" ls files 2>/dev/null | tr -d '\r'); do
    case "$name" in *.png|*tree.txt) adb exec-out run-as "$launcher" cat "files/$name" > "screenshots/facets/$suite/$name" 2>/dev/null || true;; esac
  done
}
fresh() {
  adb shell am force-stop "$launcher"
  adb shell pm clear "$launcher"
  adb shell input keyevent KEYCODE_WAKEUP
  adb shell wm dismiss-keyguard
}
facet() {
  local name="$1" class="$2"; shift 2
  local output="ten-facet-$name.txt"
  if ! timeout -k 3s 300s adb shell am instrument -w "$@" "$runner.$class" | tee "$output"; then failed=1; fi
  if ! grep -q 'PASS:' "$output" || grep -Eq 'FAIL:|shortMsg=|Process crashed|INSTRUMENTATION_FAILED' "$output"; then failed=1; fi
  collect_facet "$name"
}
fresh
facet browser BrowserSmoke
fresh
adb shell appops set --uid "$launcher" MANAGE_EXTERNAL_STORAGE allow
facet photos PhotoSmoke
fresh
adb shell appops set --uid "$launcher" MANAGE_EXTERNAL_STORAGE allow
adb shell appops set "$launcher" WRITE_SETTINGS deny
facet media MediaSmoke
fresh
facet setup StorageSmoke -e suite storage -e phase setup
fresh
adb shell appops set --uid "$launcher" MANAGE_EXTERNAL_STORAGE allow
facet storage StorageSmoke -e suite storage -e phase access
adb shell am force-stop "$launcher"
adb shell appops set --uid "$launcher" MANAGE_EXTERNAL_STORAGE deny
facet revoked StorageSmoke -e suite storage -e phase revoked
fresh
facet explorer StorageSmoke
fresh
facet resize StorageSmoke -e suite storage -e phase resize
fresh
adb shell mkdir -p /sdcard/Download
adb push installprobe/build/outputs/apk/debug/installprobe-debug.apk /sdcard/Download/Nth-Install-Probe.apk
adb shell pm uninstall com.nthexperience.desktop.installprobe || true
adb shell appops set --uid "$launcher" MANAGE_EXTERNAL_STORAGE allow
adb shell appops set "$launcher" REQUEST_INSTALL_PACKAGES deny
facet file-denied FileOpenSmoke -e phase blocked
adb shell am force-stop "$launcher"
adb shell appops set "$launcher" REQUEST_INSTALL_PACKAGES allow
facet file-open FileOpenSmoke
adb shell rm -f /sdcard/Download/Nth-Install-Probe.apk
exit "$failed"
