<?php

class DBConnect {
	private $con;
	function __construct() {
		//$this->connect();
	}
	
	function connect() {
		include_once __DIR__."/config.php"; //dirname(__FILE__).'/db_config.php';
		
		$this->con=mysqli_connect(DB_HOST,DB_USER,DB_PASSWORD,DB_NAME);
		if (mysqli_connect_errno()) {
			echo "failed to connect".mysqli_connect_error();
		}
		
		return $this->con;
	}
	
	function close() {
		mysqli_close();
	}
}
?>