<?php
$mysqli = new mysqli("127.0.0.1", "root", "", "systeme_rh");

if ($mysqli->connect_errno) {
    echo "Failed to connect to MySQL: " . $mysqli->connect_error;
    exit();
}

echo "--- JOBS TABLE ---\n";
$res = $mysqli->query("SELECT id, titre, statut FROM job");
while ($row = $res->fetch_assoc()) {
    print_r($row);
}
?>
