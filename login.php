<?php
session_start();
$valid_email="";
$valid_pass="";
$error="";
if (isset($_POST['Login'])){
$email = $_POST['email'];
$password = $_POST['password'];
if($email == $valid_email && $password == $valid_pass){
$__SESSION['user']=$username;
header("location: dashboard.php");
exit();
}else{
$error="Invalid Email or Password";
}
}
?>

<!DOCTYPE html>
<html>
<head>
<title>Airline Booking </title>
<link rel ="Stylesheet" href="Style.css">
</head>
<body>
<div class ="login-box">
<h2>Login </h2>

<?php if($error){?>
<p class = "error"> <?php echo $error;?>
</p>
<?php }?>
<form method =" pst">
<div class ="textbox">
<input type="text" name="email" required>
<lable>Email</lable>
</div>
<input type="text" name="email" required>
<lable>Password</lable>
</div>
<button type="Submit" name="login">Login</button>
</form>
</div>
<body>
<html> 