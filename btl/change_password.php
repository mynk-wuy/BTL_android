<?php
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");

// Kiểm tra kết nối
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Nhận dữ liệu từ POST
$user = $_POST['username'];
$oldPassword = $_POST['old_password'];
$newPassword = $_POST['new_password'];

// Sử dụng prepared statement để truy vấn
$stmt = $conn->prepare("SELECT password FROM users WHERE username = ?");
$stmt->bind_param("s", $user);
$stmt->execute();
$stmt->store_result();

// Kiểm tra có user không
if ($stmt->num_rows === 1) {
    $stmt->bind_result($storedPassword);
    $stmt->fetch();

    if ($storedPassword === $oldPassword) {
        // Mật khẩu đúng, tiến hành cập nhật
        $updateStmt = $conn->prepare("UPDATE users SET password = ? WHERE username = ?");
        $updateStmt->bind_param("ss", $newPassword, $user);
        if ($updateStmt->execute()) {
            echo "success";
        } else {
            echo "error";
        }
        $updateStmt->close();
    } else {
        echo "invalid"; // Mật khẩu cũ sai
    }
} else {
    echo "invalid"; // Không tìm thấy tài khoản
}

$stmt->close();
$conn->close();
?>
