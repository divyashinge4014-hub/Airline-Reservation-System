<?php
include "db.php";

$result=mysqli_query($conn,"SELECT * FROM bookings");
?>

<h2>All Bookings</h2>

<table border="1">

<tr>
<th>Name</th>
<th>Flight</th>
<th>Seat</th>
<th>Price</th>
<th>Ticket</th>
</tr>

<?php

while($row=mysqli_fetch_array($result)){

echo "<tr>";
echo "<td>".$row['name']."</td>";
echo "<td>".$row['flight']."</td>";
echo "<td>".$row['seat']."</td>";
echo "<td>".$row['price']."</td>";
echo "<td>".$row['ticket']."</td>";
echo "</tr>";

}

?>

</table>