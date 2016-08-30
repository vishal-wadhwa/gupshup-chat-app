<?php

class Home {
	private $conn;
	
	function __construct() {
		require_once __DIR__ .'\include\db_connect.php';
		$db = new DBConnect();
		$this->conn = $db->connect();
	}
	
	public function getAllChats() {
		//mysqli_report(MYSQLI_REPORT_ALL);
		$stmt = $this->conn->prepare("SELECT * FROM find_chat");
		$stmt->execute();
		$result = $stmt->get_result();
		$stmt->close();
		return $result;
	}
	
	public function getAllUsers() {
		$stmt = $this->conn->prepare("SELECT * FROM users");
		$stmt->execute();
		$result = $stmt->get_result();
		$stmt->close();
		return $result;
	}
	
	public function getDemoUser() {
		mysqli_report(MYSQLI_REPORT_ALL);
		$name = 'Demo Name';
		$email = 'demo_man@demo.com';
		$phone = '1111111111';
		$stmt = $this->conn->prepare("SELECT phone_no FROM users WHERE phone_no = ?");
		$stmt->bind_param("s",$phone);
		$stmt->execute();
		$stmt->store_result();
		if ($stmt->num_rows > 0) {
			$stmt->bind_result($phone);
			$stmt->fetch();
			$stmt->close();
			return $phone;
		} else {
			$stmt = $this->conn->prepare("INSERT INTO users(name,email,phone_no) VALUES(?,?,?) ");
			$stmt->bind_param("sss",$name,$email,$phone);
			$stmt->execute();
			//$id = $stmt->insert_id;
			$stmt->close();
			return $this->getDemoUser();
		}
	}
}
?>