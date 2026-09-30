#!/usr/bin/env bash
# Arranque del entorno de desarrollo/verificación (bloque C del plan de
# optimización del ciclo de verificación).
#
# Qué hace:
#   1. Exporta JAVA_HOME (el JBR de Android Studio: en este equipo JAVA_HOME no
#      está en el PATH de Git Bash — ver memory/gradle-jdk-path.md).
#   2. Arranca el emulador Pixel_9 headless si no hay ningún dispositivo.
#   3. Espera al boot completo y deja adb listo.
#   4. (opcional) `--install` reinstala el debug APK en el emulador.
#
# Uso:
#   ./scripts/dev/arrancar-entorno.sh            # solo entorno
#   ./scripts/dev/arrancar-entorno.sh --install  # entorno + APK debug
#
# Nota de push: el push a GitHub no funciona con GCM; usar
#   git -c credential.credentialStore=dpapi push origin master

set -euo pipefail

REPO="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SDK="${LOCALAPPDATA:-$HOME/AppData/Local}/Android/Sdk"
EMULATOR="$SDK/emulator/emulator.exe"
ADB="$SDK/platform-tools/adb.exe"
AVD="${GV_AVD:-Pixel_9}"

if [ ! -d "C:/Program Files/Android/Android Studio/jbr" ]; then
  echo "✗ No se encontró el JBR de Android Studio (JAVA_HOME)." >&2
  exit 1
fi
export JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"

device() { "$ADB" devices | awk 'NR>1 && $2=="device" {print $1; exit}'; }

if [ -z "$(device)" ]; then
  echo "→ Arrancando emulador $AVD (headless)…"
  "$EMULATOR" -avd "$AVD" -no-window -no-audio -no-boot-anim -no-snapshot \
    -gpu swiftshader_indirect >/dev/null 2>&1 &
fi

echo "→ Esperando dispositivo…"
"$ADB" wait-for-device
until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
  sleep 2
done
echo "✓ Emulador listo: $(device)"

if [ "${1:-}" = "--install" ]; then
  APK="$REPO/app/build/outputs/apk/debug/app-debug.apk"
  [ -f "$APK" ] || { echo "✗ No hay APK debug: antes, ./gradlew assembleDebug" >&2; exit 1; }
  "$ADB" install -r "$APK"
  "$ADB" shell monkey -p es.androidtfm.gamevision -c android.intent.category.LAUNCHER 1 >/dev/null
  echo "✓ App instalada y arrancada"
fi
