#!/usr/bin/env sh
# Small Gradle bootstrapper for this repository.
# Downloads the pinned Gradle distribution if it is not installed locally.
set -eu

GRADLE_VERSION="9.6.0"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
INSTALL_DIR="$GRADLE_USER_HOME/jumanji-gradle"
GRADLE_HOME="$INSTALL_DIR/gradle-$GRADLE_VERSION"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$INSTALL_DIR"
  ARCHIVE="$INSTALL_DIR/gradle-$GRADLE_VERSION-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  echo "Pobieranie Gradle $GRADLE_VERSION..."
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$ARCHIVE" "$URL"
  else
    echo "Wymagany jest curl lub wget oraz unzip." >&2
    exit 1
  fi
  if ! command -v unzip >/dev/null 2>&1; then
    echo "Nie znaleziono unzip. Zainstaluj unzip i uruchom ponownie." >&2
    exit 1
  fi
  unzip -q "$ARCHIVE" -d "$INSTALL_DIR"
  rm -f "$ARCHIVE"
fi

exec "$GRADLE_BIN" "$@"
