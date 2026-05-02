<?php
include "db.php";

$name=$_POST['name'];
$flight=$_POST['flight'];
$seat=$_POST['seat'];
$price=$_POST['price'];

$ticket=rand(10000,99999);

mysqli_query($conn,
"INSERT INTO bookings(name,flight,seat,price,ticket)
VALUES('$name','$flight','$seat','$price','$ticket')");

header("location:success.php?ticket=$ticket");
?>