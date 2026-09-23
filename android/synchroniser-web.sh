#!/bin/sh
# Recopie l'application web dans les ressources Android.
# À lancer depuis android/ après chaque modification de index.html.
set -e
DEST="app/src/main/assets/www"
rm -rf "$DEST" && mkdir -p "$DEST"
cp ../index.html ../sw.js ../manifest.webmanifest "$DEST/"
cp -r ../icons ../media "$DEST/"
echo "Application web copiée dans $DEST"
