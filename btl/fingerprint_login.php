<?php
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");
if ($conn->connect_error) die("Connection failed: " . $conn->connect_error);

$fingerprint_id = $_POST['fingerprint_id'] ?? '';

if (empty($fingerprint_id)) {
    echo json_encode(["status" => "fail", "message" => "No fingerprint ID"]);
    exit();
}

$sql = "SELECT * FROM users WHERE fingerprint_id='$fingerprint_id'";
$result = $conn->query($sql);

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();
    echo json_encode([
        "status" => "success",
        "fullname" => $user['fullname'],
        "username" => $user['username']
    ]);
} else {
    echo json_encode(["status" => "fail"]);
}
$conn->close();
?>
