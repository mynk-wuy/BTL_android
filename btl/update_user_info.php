<?php
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");

$username = $_POST['username'];
$fullname = $_POST['fullname'];
$email = $_POST['email'];
$phone = $_POST['phone'];

$sql = "UPDATE users SET fullname='$fullname', email='$email', phone='$phone' WHERE username='$username'";

if ($conn->query($sql) === TRUE) {
    echo "success";
} else {
    echo "error";
}
?>
