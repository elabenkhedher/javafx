#!/bin/bash
# ============================================================
#  run.sh — Lancer le projet Système RH Intelligent
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
LIB="$SCRIPT_DIR/lib"
OUT="$SCRIPT_DIR/out"

if [ ! -d "$OUT" ]; then
    echo "Dossier 'out' manquant. Veuillez d'abord exécuter ./compile.sh"
    exit 1
fi

# Classpath
CP_STR="$OUT"
for jar in "$LIB"/*.jar; do
    CP_STR="$CP_STR:$jar"
done

echo "=== Lancement de l'application... ==="

java --module-path "$LIB" \
     --add-modules javafx.controls,javafx.fxml \
     -cp "$CP_STR" \
     main.MainApp
