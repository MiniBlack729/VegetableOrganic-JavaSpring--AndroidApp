<?php
include"connect.php";
$email= $_POST['email'];
$name= $_POST['name'];
$password=$_POST['password'];

//check data
$query='SELECT * FROM 'user' WHERE 'email'="'.$email.'"';
$data=mysqli_query($conn,$query);
$numrow=mysqli_num_rows($data);
if($numrow>0){
	$arr = [
		'success'=>false,
		'message'=>"Email đã tồn tại"
	];
}else{
	//insert
$query='INSERT INTO `user`(`email`, `name`, `password`) VALUES ("'.$email.'","'.$name.'","'.$password.'")';

$data = mysqli_query($conn,$query);

if ($data == true){
	$arr = [
		'success'=> true,
		'message'=>"thanh cong"
	];
	}else{
		$arr = [
			'success'=>false,
			'message'=>"ko thanh cong"
		];
	}
}


print_r(json_encode($arr));
?>