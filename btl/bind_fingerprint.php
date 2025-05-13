<?php
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");
if ($conn->connect_error) die("Connection failed: " . $conn->connect_error);

$username = $_POST['username'] ?? '';
$fingerprint_id = $_POST['fingerprint_id'] ?? '';

if (empty($username) || empty($fingerprint_id)) {
    echo json_encode(["status" => "fail", "message" => "Missing data"]);
    exit();
}

$sql = "UPDATE users SET fingerprint_id='$fingerprint_id' WHERE username='$username'";
if ($conn->query($sql) === TRUE) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode(["status" => "fail", "message" => "Database error"]);
}
$conn->close();
?>
