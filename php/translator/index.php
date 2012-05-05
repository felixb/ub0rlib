<?

header('Content-Type:text/html; charset=UTF-8');

include('global.conf.php');

if (array_key_exists('username', $_POST)) {
  $username = $_POST['username'];
  if (empty($username)) {
    setcookie('username', '', time()-60*60, '/', '.'.$_SERVER['HTTP_HOST']);
  } else {
    setcookie('username', $username, time()+60*60*24*356, '/', '.'.$_SERVER['HTTP_HOST']);
  }
} else if (array_key_exists('username', $_COOKIE)) {
  $username = $_COOKIE['username'];
} else {
  $username = '';
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
<link rel="stylesheet" type="text/css" href="/default.css" />
<title>Edit Translations</title>
<script type="text/javascript">

  var _gaq = _gaq || [];
  _gaq.push(['_setAccount', 'UA-25757356-1']);
  _gaq.push(['_trackPageview']);

  (function() {
    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  })();

</script>
</head>
<body>

<div class="top">
<ul>
<li><a href="/">ub0r apps</a></li>
<li>&gt; <a href="/translate/">translate</a></li>
<li>&gt; <a href="."><? echo $appname; ?></a></li>
</ul>
<span class="topright"><a href="/contact.html">contact</a></span>
</div>

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
(Bad boys do not have this cookie set ;))
<br/>
</p>


<h3>Currently available Languages:</h3>
<ul>
<?
$stats=file_get_contents($location.'translation.stats');
$sstats=explode("\n", $stats);

foreach ($langs as $l) {
  if (array_key_exists($l, $langnames) && $langnames[$l]) {
    $s='';
    $g='';
    foreach ($sstats as $ss) {
      if (preg_match('/res\/values:/', $ss)) {
        $tmp=explode(':', $ss);
        $g=$tmp[1].')**';
      } else if (preg_match('/res\/values-'.$l.':/', $ss)) {
        $tmp=explode(':', $ss);
        $s='('.$tmp[1].'/';
      }
    }
    if ($s && $g) {
      $s=$s.$g;
    } else {
      $s='';
    }
    echo '<li><a href="'.$l.'">Edit '.$langnames[$l].'</a> '.$s.'</li>'."\n";
  }
}

echo '</ul>';
echo '<p>** Updated every 5min.</p>';
echo '<p>';
echo "<br/>\n";

if (empty($username)) {
	echo 'If your language is missing drop me a mail. To prevent spam, the adress is only shown after setting a username.';
} else {
	echo 'If your language is missing drop me a <a href="mailto:'.$contactmail.'">mail</a>.';
}

?>

</p>
</body>
</html>
