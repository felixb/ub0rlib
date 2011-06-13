<?

header('Content-Type:text/html; charset=UTF-8');

include('global.conf.php');

if (array_key_exists('username', $_POST)) {
  $username = $_POST['username'];
  if (!empty($username)) {
    setcookie('username', $username, time()+60*60*24*356, '/');
  }
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
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
<title>Edit Translations</title>
</head>
<body>

<h1>Translate Strings for <? echo $appname; ?></h1>

<h3>Other apps</h3>

Other apps you might want to translate are available <a href="../">here</a>.

<h3>User settings</h3>
<p>
You might set you username in the following form to mark all of your edited strings with this name as author.
Future versions of my app might show this username in "about" view.
It is planed to let translator load the noads code automatically, too.
<br/>
Please use a format like "My Name &lt;myaddress@myprovider.tld&gt;".
<br/>
<br/>
<form method="post" action="./">
  <p>
  <input name="username" type="text" value="<? echo $username; ?>" />
  <input type="submit" />
*
  </p>
</form>
* Your username will be saved on your computer as cookie.
To prevent spam, usernames are shown only if this cookie is set.
<br/>
</p>

<h3>Currently available Languages:</h3>
<p>
<?
foreach ($langs as $l) {
  echo '<a href="edit.php?lang='.$l.'">Edit '.$langnames[$l].'</a><br/>'."\n";
}

echo '</p>';
echo '<p>';

if (!empty($username)) {
	echo "<br/>\n";
	echo 'If your language is missing drop me a <a href="mailto:'.$contactmail.'">mail</a>.';
}

?>

</p>
</body>
</html>
