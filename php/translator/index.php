<?

include('global.conf.php');

$username = $_POST['username'];
if (!empty($username)) {
  setcookie('username', $username, time()+60*60*24*356, '/');
}

if (empty($username)) {
  $username = $_COOKIE['username'];
}

$langs = array();
$d = dir($location.'res/');
while (false !== ($entry = $d->read())) {
  if (false === strpos($entry, 'values-')) {
    continue;
  }
  $entry = str_replace('values-', '', $entry);
  $langs[] = $entry;
}
$d->close();

?>
<html>
<head>
<title>Edit Translations</title>
</head>
<body>

<h1>Translate Strings for <? echo $appname; ?></h1>

<h3>User settings</h3>
You might set you username in the following form to mark all of your edited strings with this name as author.
Future versions of my app might show this username in "about" view.
It is planed to let translator load the noads code automatically, too.
</br>
Please use a format like "My Name &lt;myaddress@myprovider.tld&gt;".
</br>
</br>
<form method="POST">
  <input name="username" type="text" value="<? echo $username; ?>" />
  <input type="submit" />
*
</form>
* Your username will be saved on your computer as cookie.
To prevent spam, usernames are shown only if this cookie is set.
</br>
</br>

<h3>Currently available Languages:</h3>
<?
foreach ($langs as $l) {
  echo '<a href="edit.php?lang='.$l.'">Edit '.$langnames[$l].'</a></br>';
}
?>

</body>
