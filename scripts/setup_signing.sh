#!/usr/bin/env bash
# Creates keystore.properties (gitignored) for signed release builds.
# Passwords are read without echo, checked against the keystore before anything is written,
# and passed to keytool through environment variables so they never appear in `ps` or shell history.
# Usage: scripts/setup_signing.sh /path/to/keystore.jks [key-alias]
set -euo pipefail
[ $# -ge 1 ] || { echo "Usage: $0 /path/to/keystore.jks [key-alias]"; exit 1; }
KS="$(realpath "$1")"
ALIAS="${2:-mykey}"
cd "$(dirname "$0")/.."
OUT=keystore.properties

git check-ignore -q "$OUT" || { echo "Refusing: $OUT is not gitignored."; exit 1; }
[ -f "$KS" ] || { echo "Keystore not found: $KS"; exit 1; }

read -rsp "Keystore password: " STOREPASS; echo
read -rsp "Key password (press Enter if it's the same): " KEYPASS; echo
[ -n "$KEYPASS" ] || KEYPASS="$STOREPASS"
CHECKPASS=throwaway-check
export STOREPASS KEYPASS CHECKPASS

if ! keytool -list -keystore "$KS" -storepass:env STOREPASS -alias "$ALIAS" >/dev/null 2>&1; then
    echo "Wrong keystore password (or no key named '$ALIAS'). Nothing written."; exit 1
fi
# The key password can differ from the store password; prove it by copying the key into a throwaway store.
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
if ! keytool -importkeystore -noprompt -srckeystore "$KS" -srcstorepass:env STOREPASS \
        -srcalias "$ALIAS" -srckeypass:env KEYPASS -destkeystore "$tmp/check.p12" \
        -deststoretype pkcs12 -deststorepass:env CHECKPASS >/dev/null 2>&1; then
    echo "Keystore password is right but the key password is wrong. Nothing written."; exit 1
fi

# Java properties treat backslashes as escapes and strip leading spaces.
esc() { printf '%s' "$1" | sed -e 's/\\/\\\\/g' -e 's/^ /\\ /'; }
umask 077
{
    echo "storeFile=$(esc "$KS")"
    echo "storePassword=$(esc "$STOREPASS")"
    echo "keyAlias=$ALIAS"
    echo "keyPassword=$(esc "$KEYPASS")"
} > "$OUT"
echo "Passwords OK. Wrote $OUT (readable only by you, ignored by git)."
