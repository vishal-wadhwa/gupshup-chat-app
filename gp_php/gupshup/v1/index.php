<?php

error_reporting(-1);
ini_set('display_errors','On');

require_once '../include/db_handler.php';
require '.././libs/Slim/Slim.php';

\Slim\Slim::registerAutoLoader();

//$app = new \Slim\Slim();
class DummyLogWriter {
  public function write($message, $level = null) {
    error_log((string) $message);
    return true;
  }
}
$app = new \Slim\Slim([
    'log.writer' => new DummyLogWriter()
]);
//User login

$app->get('/user/:phone',function($phone) {
	$db = new DBHandler();
	$response = $db->getUserByPhone($phone);
	echoResponse(200,$response);
	//return error + user if existing
});

//chat_id for sender and receiver
$app->get('/find_chat/:ph1/chat_id/:ph2',function($ph1,$ph2) {
	$db = new DBHandler();
	echoResponse(200,$db->getChat($ph1,$ph2));
});
//if not existing create new chat_id and return

$app->post('/user/login',function() use ($app) {
	//check for required parameters
	verifyRequiredParams(array('name','phone_no','email'));
	
	//reading post params
	
	$name = $app->request->post('name');
	$phone = $app->request->post('phone_no');
	$email = $app->request->post('email');
	
	validateEmail($email);
	
	$db=new DBHandler();
	$response = $db->createUser($name,$email,$phone);
	//$response['name'] = $name;
	echoResponse(200,$response);
});

//Updating user -> that is updating user's gcm id

$app->put('/user/:phone',function($phone) use ($app) {
	global $app; //using global app or use getinstance method for slim
	
	verifyRequiredParams(array('gcm_id'));
	
	$gcm_id = $app->request->put('gcm_id');
	
	$db = new DBHandler();
	$response = $db->updateGcmId($phone,$gcm_id);
	
	echoResponse(200,$response);
});

//getting all messages
$app->get('/messages/:chat_id',function($chat_id) {
	
	$db=new DBHandler();
	$result = $db->getAllMessages($chat_id);

	echoResponse(200,$result);
	
});

//getting all chats
$app->get('/find_chat',function() {
	$response=array();
	$db = new DBHandler();
	$result = $db->getAllChats();
	
	$response['error']= false;
	$response['find_chat'] = $result;

	echoResponse(200,$response);
});

$app->get('/users',function() {
	$db=new DBHandler();
	$result = $db->getAllUserList();
	$response['error']= false;
	$response['users'] = $result;
	echoResponse(200,$response);
});

//new message
$app->post('/find_chat/:chat_id/message',function($chat_id) {
	global $app;
	$db=new DBHandler();
	
	verifyRequiredParams(array('sender','receiver','message'));
	$sender = $app->request->post('sender');
	$receiver = $app->request->post('receiver');
	$message = $app->request->post('message');
	
	$response = $db->newMessage($message,$sender,$chat_id);

	if ($response['error']==false) {
		require_once __DIR__ . '/../libs/gcm/gcm.php';
		require_once __DIR__ . '/../libs/gcm/push.php';
		
		$gcm = new GCM();
		$push = new Push();
		
		$user = $db->getUserByPhone($receiver);
		
		$data = array();
		$data['receiver'] = $user;
		$data['message']= $response['message'];
		
		
		//$data['chat_id'] = $chat_id;
		$user2 = $db->getUserByPhone($sender);
		$push->setTitle($user2['user']['name']);
		//$push->setNotification($notification);
		//$push->setIsBackground(FALSE);
		//$push->setFlag(PUSH_FLAG_CHATROOM);
		$push->setData($data);

		$gcm->send($user['user']['gcm_id'], $push->getPush());
		
		//$response['receiver'] = $user;
		//$response['error'] = false;
	}
	echoResponse(200,$data);
});

//No multiple user sending

function verifyRequiredParams($fields) {
	$error = false;
	$error_fields = "";
	$params = array();
	$response = array();
	$params = $_REQUEST;
	//put requests
	if ($_SERVER['REQUEST_METHOD']=='PUT') {
		$app=\Slim\Slim::getInstance();
		parse_str($app->request->getBody(),$params);
	}

	foreach($fields as $field) {
		if (!isset($params[$field]) || strlen(trim($params[$field]))<=0) {
			$error = true;
			$error_fields.=$field.',';
		}
	}
	
	if ($error) {
		//echo json error
		$app = \Slim\Slim::getInstance();
		$response['error'] = true;
		$response['message'] = 'Required field(s) '.substr($error_fields,0,-1).' is(are) missing'; //-1 removes comma
		echoResponse(400,$response);
		$app->stop();
	}
}

//Email validation
function validateEmail($email) {
	$app = \Slim\Slim::getInstance();
	if (!filter_var($email,FILTER_VALIDATE_EMAIL)) {
		$response['error'] = true;
		$response['message'] = 'Invalid email';
		echoResponse(400,$response);
		$app->stop();
	}
}

function echoResponse($status_code,$response) {
	$app = \Slim\Slim::getInstance();
	
	$app->status($status_code);
	
	$app->contentType('application/json');
	
	echo json_encode($response);	
}


$app->run();
?>


