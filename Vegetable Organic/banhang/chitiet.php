<?php
include"connect.php";
$page=$_POST['page'];
$total= 5;// can lay 5 sp tren 1 trang
$pos=($page-1)*$total; // 0,5   5,5
$category_id=$_POST['category_id'];
$query='SELECT * FROM `products` WHERE `category_id` = '.$category_id.' LIMIT '.$page.','.$total.' ';

$data=mysqli_query($conn,$query);
$result=array();
while($row=mysqli_fetch_assoc($data)){
	$result[]=($row);
}
if(!empty($result)){
	$arr=[
		'success'=>true,
		'message'=>"thanh cong",
		'result'=>$result
	];
}else{
	$arr=[
		'success'=>false,
		'message'=>"ko thanh cong",
		'result'=>$result
	];
}
print_r(json_encode($arr));
?>