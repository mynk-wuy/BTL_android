<?php
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");
if ($conn->connect_error) die("Connection failed: " . $conn->connect_error);

$username = $_POST['username'];
$password = $_POST['password'];
$fullname = $_POST['fullname'];
$email = $_POST['email'];
$phone = $_POST['phone'];

$sql = "INSERT INTO users (username, password, fullname, email, phone)
        VALUES ('$username', '$password', '$fullname', '$email', '$phone')";

echo ($conn->query($sql) === TRUE) ? "success" : "error: " . $conn->error;
$conn->close();
?>
