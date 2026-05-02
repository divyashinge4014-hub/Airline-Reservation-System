<?php
$conn = new mysqli("localhost", "root", "", "airline_db");

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$name = $_POST['name'] ?? '';
$email = $_POST['email'] ?? '';
$from = $_POST['from_city'] ?? '';
$to = $_POST['to_city'] ?? '';
$date = $_POST['travel_date'] ?? '';
$class = $_POST['class'] ?? '';
$seats = $_POST['selectedSeats'] ?? '';

$sql = "INSERT INTO bookings (name, email, from_city, to_city, travel_date, class, seats)
        VALUES ('$name', '$email', '$from', '$to', '$date', '$class', '$seats')";

if ($conn->query($sql) === TRUE) {
    // 👉 Redirect to view page
    header("Location: view_bookings.php");
    exit();
} else {
    echo "Error: " . $conn->error;
}

$conn->close();
?>