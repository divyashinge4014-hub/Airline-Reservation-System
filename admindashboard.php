<?php
$conn = new mysqli("localhost", "root", "", "airline_system");

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Get stats
$totalBookings = $conn->query("SELECT COUNT(*) as total FROM bookings")->fetch_assoc()['total'];
$totalSeats = $conn->query("SELECT SUM(seats) as seats FROM bookings")->fetch_assoc()['seats'];
?>

<!DOCTYPE html>
<html>
<head>
  <title>Admin Dashboard</title>
  <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-slate-900 text-white p-6">

<h1 class="text-3xl font-bold mb-6">✈ Admin Dashboard</h1>

<div class="grid md:grid-cols-3 gap-6">
  <div class="bg-slate-800 p-6 rounded-xl text-center">
    <h2 class="text-xl">Total Bookings</h2>
    <p class="text-3xl mt-2 text-green-400"><?php echo $totalBookings; ?></p>
  </div>

  <div class="bg-slate-800 p-6 rounded-xl text-center">
    <h2 class="text-xl">Total Seats Booked</h2>
    <p class="text-3xl mt-2 text-blue-400"><?php echo $totalSeats ? $totalSeats : 0; ?></p>
  </div>

  <div class="bg-slate-800 p-6 rounded-xl text-center">
    <h2 class="text-xl">System Status</h2>
    <p class="text-3xl mt-2 text-yellow-400">Active</p>
  </div>
</div>

<div class="mt-8">
  <h2 class="text-2xl mb-4">Quick Actions</h2>
  <div class="flex gap-4">
    <a href="view_bookings.php" class="bg-blue-500 px-4 py-2 rounded">View Bookings</a>
    <a href="index.html" class="bg-green-500 px-4 py-2 rounded">New Booking</a>
  </div>
</div>

<div class="mt-10">
  <h2 class="text-2xl mb-4">Recent Bookings</h2>
  <div class="overflow-x-auto">
    <table class="w-full border border-gray-600">
      <thead class="bg-slate-700">
        <tr>
          <th class="p-2">Name</th>
          <th class="p-2">From</th>
          <th class="p-2">To</th>
          <th class="p-2">Date</th>
          <th class="p-2">Seats</th>
        </tr>
      </thead>
      <tbody>

      <?php
      $result = $conn->query("SELECT * FROM bookings ORDER BY id DESC LIMIT 5");
      while($row = $result->fetch_assoc()) {
          echo "<tr class='text-center border-t border-gray-700'>
                  <td class='p-2'>".$row['name']."</td>
                  <td class='p-2'>".$row['source']."</td>
                  <td class='p-2'>".$row['destination']."</td>
                  <td class='p-2'>".$row['travel_date']."</td>
                  <td class='p-2'>".$row['seats']."</td>
                </tr>";
      }
      ?>

      </tbody>
    </table>
  </div>
</div>

</body>
</html>
