<?php
$mysqli = new mysqli("127.0.0.1", "root", "", "systeme_rh");

if ($mysqli->connect_errno) {
    echo "Failed to connect to MySQL: " . $mysqli->connect_error;
    exit();
}

echo "--- APPLICATION TABLE ---\n";
$res = $mysqli->query("SELECT * FROM application ORDER BY id DESC LIMIT 5");
while ($row = $res->fetch_assoc()) {
    print_r($row);
}

echo "\n--- CANDIDATE TABLE (with CV info) ---\n";
$res = $mysqli->query("SELECT id, nom, cv_text, cv_file_path FROM candidate ORDER BY id DESC LIMIT 5");
while ($row = $res->fetch_assoc()) {
    echo "ID: " . $row['id'] . " | Nom: " . $row['nom'] . " | CV_PATH: " . $row['cv_file_path'] . " | CV_TEXT_LEN: " . strlen($row['cv_text']) . "\n";
}
?>
