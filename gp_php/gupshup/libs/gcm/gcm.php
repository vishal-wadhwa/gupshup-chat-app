<?php

class GCM {
	function __construct() {
		
	}
	
	//target single user
	public function send($to,$message) {
		$fields = array(
			'to'=>$to,
			'data'=>$message
		);
		return $this->sendPushNotif($fields);
	}
	
	public function sendToMultiple($phones,$message) {
		$fields = array(
			'to'=>$phones,
			'data'=>$message
		);
		
		return $this->sendPushNotif($fields);
	}
	
	public function sendPushNotif($fields) {
		
		include_once __DIR__.'/../../include/config.php';
		
		$url = 'https://fcm.googleapis.com/fcm/send';
		
		$headers = array(
			'Authorization: key='.GOOGLE_API_KEY,
			'Content-Type: application/json'
		);
		
		//echo 'Fields'.$fields;
		//echo 'Headers'.$headers;
		//var_dump($fields);
		//var_dump($headers);
		$curl_con = curl_init();
		curl_setopt($curl_con,CURLOPT_URL,$url);
		curl_setopt($curl_con,CURLOPT_HTTPHEADER,$headers);
		curl_setopt($curl_con,CURLOPT_RETURNTRANSFER,true);
		curl_setopt($curl_con,CURLOPT_SSL_VERIFYPEER,false);
		curl_setopt($curl_con,CURLOPT_POSTFIELDS,json_encode($fields));
		
		$result = curl_exec($curl_con);
		if ($result===false) {
			//echo $result;
			die('cURL failed:'.curl_error($curl_con));
		} 
		//echo "hello".$result;
		curl_close($curl_con);
		
		return $result;
	}
	
}
	//(new GCM())->sendPushNotif(array("to"=>"f4l24O_MCy8:APA91bGzVHrala6WwpQUFKArNp3qthzIigVtgHGqe29STUfM50vatlDJ0fMw9Vlh0onskbQrKv5l_dRyB1W9kwdgbychaeiNdbAJOw6FSH0NUBNRDWxZUdr84SFdTucazYYvGQJyxG_4"));

?>