<?php
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");
if ($conn->connect_error) die("Connection failed: " . $conn->connect_error);

$username = $_POST['username'];
$email = $_POST['email'];
$phone = $_POST['phone'];

$stmt = $conn->prepare("SELECT * FROM users WHERE username = ? OR email = ? OR phone = ?");
$stmt->bind_param("sss", $username, $email, $phone);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    echo json_encode(["exists" => true]);
} else {
    echo json_encode(["exists" => false]);
}
?>
