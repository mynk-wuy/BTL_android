<?php
$conn = new mysqli("localhost", "username", "password", "database");

$user = $_POST['username'];
$fullname = $_POST['fullname'];
$phone = $_POST['phone'];
$email = $_POST['email'];

$sql = "UPDATE users SET fullname='$fullname', phone='$phone', email='$email' WHERE username='$user'";
if ($conn->query($sql) === TRUE) {
    echo "success";
} else {
    echo "error";
}
?>
