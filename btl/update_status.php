<?php
date_default_timezone_set('Asia/Ho_Chi_Minh'); // Giờ Việt Nam
$conn = new mysqli("localhost", "root", "minhquy2003", "smart_home");
if ($conn->connect_error) die("Connection failed: " . $conn->connect_error);

$fullname = $_POST['fullname'];
$device = $_POST['device'];
$status = $_POST['status'];
$timestamp = date("Y-m-d H:i:s"); // Sử dụng $timestamp đã khai báo

// ✅ GHI VÀO history_log (luôn thêm bản ghi mới)
$stmt1 = $conn->prepare("INSERT INTO history_log (fullname, device, status, time) VALUES (?, ?, ?, ?)");
$stmt1->bind_param("ssss", $fullname, $device, $status, $timestamp); // Thay $timeNow bằng $timestamp
$stmt1->execute();

// ✅ GHI VÀO light_status (cập nhật trạng thái hiện tại của thiết bị)
$stmt2 = $conn->prepare("SELECT id FROM light_status WHERE device = ?");
$stmt2->bind_param("s", $device);
$stmt2->execute();
$result = $stmt2->get_result();

if ($result->num_rows > 0) {
    // Nếu đã có thiết bị => cập nhật
    $stmt3 = $conn->prepare("UPDATE light_status SET status = ?, updated_at = ? WHERE device = ?");
    $stmt3->bind_param("sss", $status, $timestamp, $device); // Sử dụng $timestamp
    $stmt3->execute();
} else {
    // Nếu chưa có => thêm mới
    $stmt4 = $conn->prepare("INSERT INTO light_status (device, status, updated_at) VALUES (?, ?, ?)");
    $stmt4->bind_param("sss", $device, $status, $timestamp); // Sử dụng $timestamp
    $stmt4->execute();
}

echo json_encode(["success" => true]);

$conn->close();
?>
