#!/bin/bash
# ============================================================
#  compile.sh — Compiler le projet Système RH Intelligent
# ============================================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC="$SCRIPT_DIR/src"
RES="$SCRIPT_DIR/resources"
LIB="$SCRIPT_DIR/lib"
OUT="$SCRIPT_DIR/out"

# Vérifier les JARs (avec support des noms versionnés)
MISSING=""
for jar_base in javafx-controls javafx-fxml javafx-base javafx-graphics mysql-connector pdfbox fontbox; do
    if ! ls "$LIB/$jar_base"*".jar" >/dev/null 2>&1; then
        MISSING="$MISSING $jar_base"
    fi
done

if [ ! -z "$MISSING" ]; then
    echo "ERREUR: JARs manquants dans lib/:$MISSING"
    echo "ATTENTION: Vos fichiers JavaFX dans lib/ semblent être trop petits (~300 octets)."
    echo "Vous devez télécharger les versions PLATFORME (ex: javafx-controls-21-linux.jar)."
    echo "Voir README.txt pour plus d'informations."
    exit 1
fi

# Construire le Classpath
CP_STR="$OUT"
for jar in "$LIB"/*.jar; do
    CP_STR="$CP_STR:$jar"
done

rm -rf "$OUT"
mkdir -p "$OUT"

echo "=== Compilation en cours... ==="
find "$SRC" -name "*.java" > /tmp/sources.txt

javac --module-path "$LIB" \
      --add-modules javafx.controls,javafx.fxml \
      -cp "$CP_STR" \
      -d "$OUT" \
      @/tmp/sources.txt

if [ $? -ne 0 ]; then
    echo "ECHEC de la compilation."
    exit 1
fi

# Copier les ressources
cp -r "$RES/view" "$OUT/" 2>/dev/null
cp -r "$RES/css"  "$OUT/" 2>/dev/null
cp -r "$SRC/ressources/view" "$OUT/" 2>/dev/null

echo "=== Compilation réussie ! Lancez ./run.sh pour démarrer. ==="
