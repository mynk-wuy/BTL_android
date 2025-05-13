<?php
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");

$username = $_POST['username'];
$result = $conn->query("SELECT fullname, email, phone FROM users WHERE username='$username'");
if ($row = $result->fetch_assoc()) {
    echo json_encode($row);
} else {
    echo json_encode([]);
}
?>
