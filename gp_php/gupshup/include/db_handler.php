<?php

class DBHandler
{
    private $conn;
    
    function __construct()
    {
        require_once __DIR__ . "/db_connect.php";
        
        $db = new DBConnect(); //connection established in constructor;
		$this->conn=$db->connect();
    }
    
    function __destruct()
    {
        $this->conn->close();
    }
    
    public function createUser($name, $email, $phone)
    {
        $response = array();
		

        
        if (!$this->isUserPresent($phone)) {
            $stmt = $this->conn->prepare("INSERT INTO users(name,phone_no,email) VALUES (?,?,?)");
            $stmt->bind_param("sss", $name, $phone, $email);

            $result = $stmt->execute();
            
            $stmt->close();
            
            if ($result) {
                $response["error"] = false;
                $response["message"]  = "User registration successful";
            } else {
                $response["error"]   = true;
                $response["message"] = "An error occurred.";
            }
        } else {
            $response["error"] = true;
			$response["message"] = "Existing User";
			$response['phone'] = $phone;
        }
        return $response;
    }
    
    public function getUserByPhone($phone)
    {
        $response = array();
		mysqli_report(MYSQLI_REPORT_ALL);
        $stmt = $this->conn->prepare("SELECT * FROM users WHERE phone_no = ?");
        $stmt->bind_param("s", $phone);
        
        $result = $stmt->execute();
        $response["error"]   = true;
        //$response["message"] = "Error occurred";
        $response["user"]    = NULL;
        if ($result) {
            $response["error"] = false;
            $user              = array();
            $stmt->bind_result($name, $phone_no, $email, $gcm_id, $creation_date);
            $stmt->fetch();
            
            $user["name"]          = $name;
            $user["phone_no"]      = $phone_no;
            $user["email"]         = $email;
            $user['gcm_id']        = $gcm_id;
            $user["creation_date"] = $creation_date;
            
            $stmt->close();
            
            $response["user"]    = $user;
            //$response["message"] = "Done";
        }
        return $response;
    }
    
    public function getChat($phone1, $phone2)
    {
		mysqli_report(MYSQLI_REPORT_ERROR);
        $stmt = $this->conn->prepare("SELECT * FROM find_chat WHERE (phone1 = ? AND phone2 = ?) OR (phone1 = ? AND phone2 = ?)");
        $stmt->bind_param("ssss", $phone1, $phone2,$phone2, $phone1);
        $response = array();
        $result = $stmt->execute();
        $response['error']=true;
        if ($result) {
            
            $stmt->store_result();
            if ($stmt->num_rows > 0) {
                $stmt->bind_result($ph1,$ph2,$chat_id);
                $stmt->fetch();
                $stmt->close();
				$user['phone1'] = $ph1;
				$user['phone2'] = $ph2;
				$user['chat_id'] = $chat_id;
				$response['user'] = $user;
				$response['error'] = false;
                return $response;
            } else {
                $stmt = $this->conn->prepare("INSERT INTO find_chat(phone1,phone2) VALUES (?,?)");
                $stmt->bind_param("ss", $phone1, $phone2);
                $stmt->execute();
				$stmt->close();
				
                    // $stmt2 = $this->conn->prepare("SELECT chat_id FROM find_chat WHERE (phone1 = ? AND phone2 = ?) OR (phone1 = ? AND phone2 = ?)");
                    // $stmt2->bind_param("ssss", $phone1, $phone2, $phone2, $phone1);
                    
                    // $stmt2->execute();
                    // $stmt2->bind_result($chat_id);
                    // $stmt2->fetch();
                    // $stmt2->close();
                    // $stmt->close();
                    return $this->getChat($phone1,$phone2);
            }
        } else {
            echo "Error";
            return NULL;
        }
    }
    
    public function newMessage($message, $sender, $chat_id)
    {
		mysqli_report(MYSQLI_REPORT_ERROR);
		//invoke new chat if sender receiver not found else return the chat_id
        $response['error']   = true;
        $response['message'] = "An error occurred.";
        //$chat_id             = $this->getChat($sender, $receiver)['chat_id'];
        $stmt                = $this->conn->prepare("INSERT INTO messages(message,sender_phone,chat_id) VALUES (?,?,?)");
        $stmt->bind_param("ssi", $message, $sender, $chat_id);
        if ($stmt->execute()) {
			//$stmt->close();
			//$stmt=$this->conn->prepare("SELECT * FROM messages WHERE sending_time = (SELECT MAX(sending_time) FROM messages)");
			$id = $this->conn->insert_id;
			$stmt=$this->conn->prepare("SELECT * FROM messages WHERE message_id=?");
			$stmt->bind_param("i",$id);
			$stmt->execute();
			$stmt->bind_result($message_id, $message, $time, $sender, $chat_id);
            $stmt->fetch();
			$stmt->close();
            $tmp                 = array();
            $tmp['message_id']   = $message_id;
            $tmp['message']      = $message;
            $tmp['sending_time']  = $time;
			//echo $time;
            $tmp['sender_phone']  = $sender;
            $tmp['chat_id']      = $chat_id;
            $response['message'] = $tmp;
            $response['error']   = false;
			return $response;
        }
        
        //return the response array
        return $response;
    }
    
    public function updateGcmId($phone, $gcm_id)
    {mysqli_report(MYSQLI_REPORT_ALL);
        $response = array();
        $stmt     = $this->conn->prepare("UPDATE users SET gcm_id = ? WHERE phone_no = ?");
        $stmt->bind_param("ss", $gcm_id, $phone);
        
        if ($stmt->execute()) {
            $response['error']   = false;
            $response['message'] = "Successful";
        } else {
            $response['error']   = true;
            $response['message'] = "An error occurred.";
            $stmt->error();
        }
        $stmt->close();
        return $response;
    }
    
    public function getAllUsers($phone_nos)
    {
		
        if (sizeof($phone_nos) > 0) {
            $query = "SELECT * FROM users WHERE phone IN (";
            foreach ($phone_nos as $phone) {
                $query .= $phone . ',';
            }
            $query = substr($query, 0, strlen($query) - 1); //to remove last comma
            $query .= ')';
            
            $stmt = $this->conn->prepare($query);
            $stmt->execute();
            $result = $stmt->get_result();
            
            while ($user = $result->fetch_assoc()) {
                $array_push($users, $user);
            }
        }
        return $users;
    }
    public function getAllMessages($chat_id)
    {
        $query = "SELECT * FROM messages WHERE chat_id = ? ORDER BY message_id ASC";
        
        $stmt = $this->conn->prepare($query);
        $stmt->bind_param("i", $chat_id);
        $stmt->execute();
        $result = $stmt->get_result();
        $msgs = array();
        while ($msg = $result->fetch_assoc()) {
            array_push($msgs, $msg);
        }
        
        return $msgs;
    }
    
	public function isUserPresent($phone) {
		$stmt = $this->conn->prepare("SELECT * FROM users WHERE phone_no = ?");
		$stmt->bind_param("s",$phone);
		$stmt->execute();
		$stmt->store_result();
		$rows = $stmt->num_rows;
		$stmt->close();
		return $rows==1;
	}
	
	public function getAllChats() {
		//mysqli_report(MYSQLI_REPORT_ALL);
		$stmt = $this->conn->prepare("SELECT * FROM find_chat");
		$stmt->execute();
		$response = array();
		$result = $stmt->get_result();
		
		while($tmp = $result->fetch_assoc()) {
			array_push($response,$tmp);
		}
		$stmt->close();
		return $response;
	}
	
	public function getAllUserList() {
		$stmt = $this->conn->prepare("SELECT * FROM users");
		$stmt->execute();
		$response = array();
		$result = $stmt->get_result();
		
		while($tmp = $result->fetch_assoc()) {
			array_push($response,$tmp);
		}
		$stmt->close();
		return $response;
	}

}

//(new DBHandler())->newMessage("ld ls;f fd;ljf","142756",1482);
?>