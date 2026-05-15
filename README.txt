========================================================
  SYSTÈME RH INTELLIGENT — JavaFX FXML (sans Maven)
========================================================

STRUCTURE DU PROJET
-------------------
systeme-rh-intelligent/
├── src/
│   ├── main/          MainApp.java
│   ├── controller/    5 contrôleurs FXML
│   ├── model/         5 modèles (Employee, Candidate, Job, Application, CandidateAnalysis)
│   ├── service/       DatabaseService, CVAnalyzerService
│   └── utils/         PDFExtractor, TextProcessor
├── resources/
│   ├── view/          5 fichiers FXML (dashboard, employees, recruitment, analysis, main)
│   └── css/           style.css
├── lib/               ← PLACER LES JARs ICI (voir ci-dessous)
├── out/               dossier de compilation (créé automatiquement)
├── nbproject/         configuration NetBeans
├── compile.sh / compile.bat
└── run.sh / run.bat


ÉTAPE 1 — TÉLÉCHARGER LES JARs (à faire une seule fois)
---------------------------------------------------------
Télécharger ces fichiers et les placer dans le dossier lib/ :

  Nom du fichier              URL de téléchargement
  --------------------------  --------------------------------------------------
  javafx-controls.jar         https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21.0.1/javafx-controls-21.0.1.jar
  javafx-fxml.jar             https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/21.0.1/javafx-fxml-21.0.1.jar
  javafx-base.jar             https://repo1.maven.org/maven2/org/openjfx/javafx-base/21.0.1/javafx-base-21.0.1.jar
  javafx-graphics.jar         https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21.0.1/javafx-graphics-21.0.1.jar
  mysql-connector.jar         https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar
  pdfbox.jar                  https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/3.0.1/pdfbox-3.0.1.jar
  fontbox.jar                 https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/3.0.1/fontbox-3.0.1.jar

⚠️  Renommer chaque JAR exactement comme indiqué dans la colonne "Nom du fichier".


ÉTAPE 2 — CONFIGURER LA BASE DE DONNÉES
-----------------------------------------
1. Démarrer XAMPP → Activer MySQL
2. Ouvrir phpMyAdmin → Créer la base : systeme_rh
3. Si mot de passe MySQL différent de vide, modifier dans :
   src/service/DatabaseService.java  →  ligne DB_PASSWORD

Les tables et données de démonstration sont créées automatiquement
au premier lancement.


ÉTAPE 3 — COMPILER
--------------------
  Linux/Mac :   chmod +x compile.sh && ./compile.sh
  Windows   :   Double-cliquer compile.bat


ÉTAPE 4 — LANCER
-----------------
  Linux/Mac :   ./run.sh
  Windows   :   Double-cliquer run.bat


OUVRIR DANS NETBEANS (sans Maven)
----------------------------------
1. File → Open Project → sélectionner ce dossier
2. Clic droit sur le projet → Properties → Libraries
3. Cliquer "Add JAR/Folder" pour chaque JAR dans lib/
4. Properties → Run → VM Options : ajouter
   --module-path lib --add-modules javafx.controls,javafx.fxml
5. Properties → Run → Main Class : main.MainApp
6. Build → Clean and Build, puis Run


CORRECTIONS APPORTÉES (par rapport au projet Maven original)
--------------------------------------------------------------
✔  Suppression du pom.xml et de la dépendance Maven
✔  Structure source plate (src/) compatible NetBeans/IntelliJ
✔  Ressources dans resources/ (séparées du src/)
✔  Scripts compile.sh / compile.bat / run.sh / run.bat
✔  Configuration nbproject/ pour NetBeans
✔  Correction import erroné : java.util.stream.Collector → Collectors
✔  Options JVM JavaFX intégrées dans les scripts (--module-path / --add-modules)
✔  Encodage CRLF → LF normalisé sur tous les fichiers

========================================================
