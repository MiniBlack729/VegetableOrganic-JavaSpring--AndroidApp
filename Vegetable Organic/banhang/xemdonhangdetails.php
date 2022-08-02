<?php
include"connect.php";
$order_id=$_POST['order_id'];

$query='SELECT * FROM `order_details` WHERE `order_id`='.$order_id;

$data=mysqli_query($conn,$query);
$result=array();
while($row=mysqli_fetch_assoc($data)){
	// $truyvan='SELECT * FROM `order_details` INNER JOIN products ON order_details.order_id=products.product_id WHERE order_details.order_id='.$row['order_id'];
	$truyvan='SELECT * FROM `order_details` INNER JOIN products ON order_details.product_id= products.product_id WHERE order_details.order_id='.$row['order_id'];
	$data1=mysqli_query($conn,$truyvan);
	$item=array();
	while($row1=mysqli_fetch_assoc($data1)){
		$item[]=$row1;
	}
	$row['item']=$item;
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