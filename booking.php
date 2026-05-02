<?php
$flight=$_POST['flight'];
?>

<!DOCTYPE html>
<html>
<head>
<title>Seat Booking</title>
<link rel="stylesheet" href="style.css">
</head>

<body>

<h2>Select Seat</h2>

<form action="payment.php" method="post">

<input type="hidden" name="flight" value="<?php echo $flight; ?>">

Name
<input name="name">

Seat
<select name="seat">

<option>A1</option>
<option>A2</option>
<option>A3</option>
<option>A4</option>

<option>B1</option>
<option>B2</option>
<option>B3</option>
<option>B4</option>

</select>

Price
<input name="price" value="5000">

<button>Proceed Payment</button>

</form>

</body>
</html>