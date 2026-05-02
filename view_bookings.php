<?php
// Database connection
$conn = new mysqli("localhost", "root", "", "airline_db");

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Fetch data
$sql = "SELECT * FROM flightsbooking ORDER BY id DESC";
$result = $conn->query($sql);
?>

<!DOCTYPE html>
<html>
<head>
  <title>Registered Bookings</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-900 text-white p-6">

<h1 class="text-3xl font-bold mb-6">Registered Flight Bookings</h1>

<div class="mt-6 overflow-x-auto">
  <table class="w-full border border-gray-600">
    <thead class="bg-slate-700">
      <tr>
        <th class="p-2">ID</th>
        <th class="p-2">Name</th>
        <th class="p-2">Email</th>
        <th class="p-2">From</th>
        <th class="p-2">To</th>
        <th class="p-2">Date</th>
        <th class="p-2">Seats</th>
        <th class="p-2">Time</th>
      </tr>
    </thead>
    <tbody>

    <?php
    if ($result->num_rows > 0) {
        while($row = $result->fetch_assoc()) {
            echo "<tr class='text-center border-t border-gray-700'>
                    <td class='p-2'>".$row['id']."</td>
                    <td class='p-2'>".$row['name']."</td>
                    <td class='p-2'>".$row['email']."</td>
                    <td class='p-2'>".$row['source']."</td>
                    <td class='p-2'>".$row['destination']."</td>
                    <td class='p-2'>".$row['travel_date']."</td>
                    <td class='p-2'>".$row['seats']."</td>
                    <td class='p-2'>".$row['booking_time']."</td>
                  </tr>";
        }
    } else {
        echo "<tr><td colspan='8' class='p-4 text-center'>No bookings found</td></tr>";
    }
    ?>

    </tbody>
  </table>
</div>

</body>
</html>
