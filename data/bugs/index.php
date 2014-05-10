<!DOCTYPE html>
<html>
<head>
	<title>jWarframe Bug Database</title>
	<link rel="icon" type="image/png" href="favicon.ico" />
</head>
<body>
	<h1>jWarframe Bug Database</h1>
	<hr>
<?php
include 'conn.php';

$order_in = filter_input(INPUT_POST, 'order');
$order = makeSafe(strtolower($order_in), array("date", "count", "id"), 'date');

$desc_in = filter_input(INPUT_POST, 'desc');
$desc = makeSafe(strtoupper($desc_in), array("DESC", "ASC"), 'DESC');

echo '<form method="post" action="">';
select(array("Date", "Count", "ID"), 'order', $order);
select(array("DESC", "ASC"), 'desc', $desc);
echo '<input type="submit" value="Submit">';
echo '</form">';
echo "<hr>";

$dbh = con();
$statement = $dbh->prepare("SELECT * FROM jwarframe ORDER BY $order $desc");
$statement->execute();
$rows = $statement->fetchAll(PDO::FETCH_ASSOC);
foreach ($rows as &$row) {
	echo " <b>Date:</b> ".format($row['date']);
	echo " <b>Count:</b> ".format($row['count']);
	echo " <b>Id:</b> ".format($row['id'])."<br>";
	echo "<b>OS:</b> ".format_list($row['os'])."<br>";
	echo "<b>Java:</b> ".format_list($row['java'])."<br>";
	echo "<b>Version:</b> ".format_list($row['version'])."<br>";
	echo "<br>".format($row['log'])."<br>";
	echo "<hr>";
}

function format_space($string) {
	$string = preg_replace('/[\r\n]+/', '<br>', $string);
	$string = preg_replace('/\t/', '&nbsp;&nbsp;&nbsp;&nbsp;', $string);
	$string = preg_replace('/\s/', '&nbsp;', $string);
	return $string;
}

function format($string) {
	$string = htmlentities($string);
	$string = format_space($string);
	return $string;
}
function format_list($string) {
	$string = str_replace(";", "&nbsp;&nbsp;&nbsp;&nbsp;", $string);
	return format_space($string); 
}
function select($values, $name, $selected) {
	print '<select name="'.$name.'">';
	foreach ($values as &$value) {
		$select = '';
		if (strtolower($selected) == strtolower($value)) {
			$select = ' selected="selected"';
		}
		print '<option value="'.$value.'"'.$select.'>'.$value.' </option>';
	}
	print "<select>";
}
function makeSafe($find, $in, $default) {
	$key = array_search($find, $in);
	$value = $in[$key];
	if (empty($value)) {
		return $default;
	} else {
		return $value;
	}
}
?>
</body>
</html>