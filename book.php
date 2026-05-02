<?php
$conn = new mysqli("localhost", "root", "", "airline_db");

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get form data
$name = $_POST['name'];
$email = $_POST['email'];
$source = $_POST['source'];
$destination = $_POST['destination'];
$travel_date = $_POST['travel_date'];
$seats = $_POST['seats'];

// Insert into database
$sql = "INSERT INTO flightbooking (name, email, source, destination, travel_date, seats)
        VALUES ('$name', '$email', '$source', '$destination', '$travel_date', '$seats')";

if ($conn->query($sql) === TRUE) {
    echo "<script>alert('Booking Successful!'); window.location.href='view_bookings.php';</script>";
} else {
    echo "Error: " . $conn->error;
}

$conn->close();
?>