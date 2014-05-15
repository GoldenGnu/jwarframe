<?php
function con() {
	return new PDO('mysql:host=localhost;dbname=database', 'user', 'pass');
}
function table() {
	return "jwarframe";
}
function name() {
	return "jWarframe Bug Database";
}
?>