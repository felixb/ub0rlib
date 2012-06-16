<? 

header('Content-Type:text/html; charset=UTF-8');

include('global.conf.php');

function decode_string($s) {
  $ret = $s;
  $ret = str_replace('\"', '"', $ret);
  $ret = str_replace("\'", "'", $ret);
  $ret = str_replace('\n', "\n", $ret);
  //$ret = str_replace('&lt;', '<', $ret);
  //$ret = str_replace('&gt;', '>', $ret);
  //$ret = str_replace('&amp;', '&', $ret);
  $ret = str_ireplace('</textarea>', '</ta>', $ret);
  return $ret;
}


function encode_string($s) {
  $ret = $s;
  $ret = str_replace("'", "\'", $ret);
  $ret = str_replace("\\\\'", "\'", $ret);
  $ret = str_replace("\r\n", '\n', $ret);
  $ret = str_replace("\n", '\n', $ret);
  $ret = str_replace('&', '&amp;', $ret);
  $ret = str_replace('<', '&lt;', $ret);
  $ret = str_replace('>', '&gt;', $ret);
  $ret = str_replace('&amp;lt;', '&lt;', $ret);
  $ret = str_replace('&amp;lt;', '&lt;', $ret);
  $ret = str_replace('&amp;gt;', '&gt;', $ret);
  $ret = str_replace('&amp;gt;', '&gt;', $ret);
  $ret = str_replace('&amp;amp;', '&amp;', $ret);
  $ret = str_replace('&amp;amp;', '&amp;', $ret);
  return $ret;
}

function clean_nl($s) {
  $ret = $s;
  $ret = str_replace("\r\n", '\n', $ret);
  $ret = str_replace("\n", '\n', $ret);
  return trim($ret);
}

function clean_username($un) {
  $ret = $un;
  $ret = str_replace("'", '', $ret);
  $ret = str_replace('"', '', $ret);
  return $ret;
}

function sxmle_setarg($sxmle, $arg, $value) { 
  if (!empty($sxmle[$arg])) {
    $sxmle[$arg] = $value;
  } else {
    $sxmle->addAttribute($arg, $value);
  }
}

function sxmle_rmarg($sxmle, $arg) {
  if (!empty($sxmle[$arg])) {
    $sxmle[$arg] = NULL;
  }
}

function sxmle_readxml($location, $filename) {
  $xml = file_get_contents($location.$filename);
  $elems = 'b,i,u,big,small,sub,sup,strike';
  foreach (explode(',', $elems) as $e) {
    $xml = str_ireplace('<'.$e.'>', '&lt;'.$e.'&gt;', $xml);
    $xml = str_ireplace('</'.$e.'>', '&lt;/'.$e.'&gt;', $xml);
  }
  return new SimpleXMLElement($xml);
}

function sxmle_writexml($sxmle, $location, $lang, $file) {
  global $lang, $file;
  $xml = $sxmle->asXML();
  $xml = preg_replace('/\/android\/translate\//', '/translate/', preg_replace('/Visit http.*to edit the file./', 'Visit http://ub0r.de'.preg_replace('/edit.php/', $lang.'/'.$file, $_SERVER['SCRIPT_NAME']).' to edit the file.', $xml));
  $xml = preg_replace('/\>\<string/', '>'."\n".'<string', $xml);
  $xml = preg_replace('/\>\<item/', '>'."\n".'<item', $xml);
  $xml = preg_replace('/.*\<string/', '  <string', $xml);
  $xml = preg_replace('/.*\<item/', '    <item', $xml);
  $xml = preg_replace('/\>\<\/string/', '>'."\n".'  </string', $xml);
  $xml = preg_replace('/\>\<\/resources/', '>'."\n".'</resources', $xml);
  $xml = preg_replace('/ orig=""/', '', $xml);
  $xml = preg_replace('/ username=""/', '', $xml);
  $xml = preg_replace('/ notranslation=""/', '', $xml);
  $xml = preg_replace('/.*\<item\/\>/', '', $xml);
  $xml = preg_replace('/.*\<item\>\<\/item\>/', '', $xml);

  $nxml = '';
  foreach (explode("\n", $xml) as $line) {
    if (empty($line)) {
      continue;
    }
    $parts = explode('>', $line, 2);
    if (count($parts) > 1) {
      $parts[1] = str_ireplace("'", "\\'", $parts[1]);
      $parts[1] = str_ireplace('\\\\', '\\', $parts[1]);
      $elems = 'b,i,u,big,small,sub,sup,strike';
      foreach (explode(',', $elems) as $e) {
	$parts[1] = str_ireplace('&lt;'.$e.'&gt;', '<'.$e.'>', $parts[1]);
	$parts[1] = str_ireplace('&lt;/'.$e.'&gt;', '</'.$e.'>', $parts[1]);
      }
    }
    $line = implode('>', $parts);
    $nxml = $nxml . $line . "\n";
  }
  $xml = $nxml;
  file_put_contents($location.'res/values-'.$lang.'/'.$file, $xml);
}

function hidegreen() {
  if (array_key_exists('hidegreen', $_POST) && $_POST['hidegreen'] == 1) {
    return 1;
  } else {
    return 0;
  }
}

function hidegreenclass($isgreen) {
  if (hidegreen() && $isgreen) {
    return ' hide';
  } else {
    return '';
  }
}

$color_green='class="greenbg"';
$color_red='class="redbg"';
$color_yellow='class="yellowbg"';

if (array_key_exists('username', $_COOKIE)) {
  $username = $_COOKIE['username'];
} else {
  $username = '';
}
$username = clean_username($username);

$lang = $_GET['lang'];
if (empty($lang)) {
  $lang = $_POST['lang'];
}

$files = array();
$d = dir($location.'res/values/');
while (false !== ($entry = $d->read())) {
  if ($entry == '.' or $entry == '..' or $entry == 'base.xml' or $entry == 'attrs.xml' or $entry == 'update.xml' or $entry == 'cwac_touchlist_attrs.xml' or $entry == 'dimen.xml' or $entry == 'styles.xml') {
    continue;
  }
  $files[] = $entry;
}
$d->close();

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

// print_r($_GET);

if (array_key_exists('file', $_GET)) {
  $file = $_GET['file'];
} else if (array_key_exists('file', $_POST)) {
  $file = $_POST['file'];
} else {
  $file = '';
}

?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<?
if (!file_exists($location . 'res/values-' . $lang)) {
  echo '<meta HTTP-EQUIV="REFRESH" content="0; url=.">';
}
?>
<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
<link rel="stylesheet" type="text/css" href="/default.css" />
<title>Edit Translations: <? echo $lang; ?></title>
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
<script type = "text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" ></script>
<script type = "text/javascript">
  $(document).ready(function(){
    $("#hidegreen").click(function(event) {
      $(".greenbg").parent().parent('form').hide();
      $(".greenbg").parent().parent().parent().parent().parent('form').hide();
      $("#hidegreen").hide();
      $("#showgreen").show();
      $('input[name="hidegreen"]').val("1");
      event.preventDefault();
    });
    $("#showgreen").click(function(event) {
      $(".greenbg").parent().parent('form').show();
      $(".greenbg").parent().parent().parent().parent().parent('form').show();
      $("#showgreen").hide();
      $("#hidegreen").show();
      $('input[name="hidegreen"]').val("0");
      event.preventDefault();
    });
  });
</script>
<style type="text/css">
  .greenbg  { background: #A0FFA0; }
  .redbg    { background: #FFA0A0; }
  .yellowbg { background: #FFFFA0; } 
  .hide     { display: none; }
  .stringblock { border-top: 2px dashed grey; }
</style>
</head>
<body>


<?
$redirect=false;
$basedir='./';
if (preg_match('/\.php/', $_SERVER['REQUEST_URI'])) {
  $redirect=false;
  $basedir='./';
} else {
  $redirect=true;
  if (preg_match('/\/'.$file.'\//', $_SERVER['REQUEST_URI'])) {
    $basedir='../../';
  } else if (preg_match('/\/'.$lang.'\//', $_SERVER['REQUEST_URI'])) {
    $basedir='../';
  } else {
    $basedir='./';
  }
}
?>

<div class="top">
<ul>
<li><a href="/">ub0r apps</a></li>
<li>&gt; <a href="/translate/">translate</a></li>
<li>&gt; <a href="<? echo $basedir; ?>"><? echo $appname; ?></a></li>
<li>&gt; 
<?
  if ($redirect) {
    echo '<a href="'.$basedir.$lang.'">';
  } else {
    echo '<a href="'.$basedir.'edit.php?lang='.$lang.'">';
  }
?>
<? echo $lang; ?></a></li>
<?
  
  if (!empty($file)) {
    echo '<li>&gt; ';
    if ($redirect) {
      echo '<a href="'.$basedir.$lang.'/'.$file.'">';
    } else {
      echo '<a href="'.$basedir.'edit.php?lang='.$lang.'&amp;file='.$file.'">';
    }
    echo $file.'</a></li>';
  }
?>
</ul>
<span class="topright"><a href="/contact.html">contact</a></span>
</div>

<div class="left">

<h1>Translate Strings for <? echo $appname; ?> / <? echo $lang; ?></h1>
<p>
Your username: <? echo htmlspecialchars($username); ?><br/><br/>
Set username or select an other language on the <b>
<?
echo '<a href="' . $basedir . '">index page</a>';
?>
</b>.
</p>

<h3>Currently available files:</h3>
<p>
<?


$stats=file_get_contents($location.'translation.stats');
$sstats=explode("\n", $stats);

echo '<ul>';
foreach ($files as $f) {

  $s='';
  $g='';
  foreach ($sstats as $ss) {
    if (preg_match('/res\/values\/'.$f.':/', $ss)) {
      $tmp=explode(':', $ss);
      $g=$tmp[1].')*';
    } else if (preg_match('/res\/values-'.$lang.'\/'.$f.':/', $ss)) {
      $tmp=explode(':', $ss);
      $s='('.$tmp[1].'/';
    }
  }
  if ($s && $g) {
    $s=$s.$g;
  } else {
    $s='';
  }
  echo '<li>';
  if ($redirect) {
    echo '<a href="'.$basedir.$lang.'/'.$f.'">';
  } else {
    echo '<a href="'.$basedir.'edit.php?lang='.$lang.'&amp;file='.$f.'">';
  }
  if ($file == $f) {
    echo '<b>';
  }
  echo 'Edit file '.$lang.'/'.$f;
  if ($file === $f) {
    echo '</b>';
  }
  echo '</a> '.$s.'</li>'."\n";
}
echo '</ul>';
?>
* Updated every 5min.
</p>
<?
if (!empty($file)) {
  echo '<h3>Current file: '.$lang.'/'.$file."</h3>\n";
  echo '<p>';
  echo 'Color codes: <ul><li>green == ok</li><li>yellow == original text changed since last translation</li><li>red == missing string</li></ul>'."\n";

  if (hidegreen()) {
    echo '<a href="#" id="hidegreen" class="hide">[ Hide green strings ]</a>';
    echo '<a href="#" id="showgreen">[ Show green strings ]</a>';
  } else {
    echo '<a href="#" id="hidegreen">[ Hide green strings ]</a>';
    echo '<a href="#" id="showgreen" class="hide">[ Show green strings ]</a>';
  }
  echo "<br/>\n";
  echo "<br/>\n";
  echo '</p>';
  
  // load source strings
  $sxml = sxmle_readxml($location, 'res/values/'.$file);
  $txml = sxmle_readxml($location,  'res/values-'.$lang.'/'.$file);

  // process new strings
  if (array_key_exists('action', $_POST)) {
    $action = $_POST['action'];
  } else if (array_key_exists('action', $_GET)) {
    $action = $_GET['action'];
  }
  if (!empty($action) && !empty($username)) {
    if ($action == 'edit-string') {
      foreach ($_POST as $k => $v) {
	if ($k == 'action' or $k == 'lang' or $k == 'file') {
	  continue;
	}
	$strings = $sxml->xpath('//string[@name="' . $k . '"]');
	$tstrings = $txml->xpath('//string[@name="' . $k . '"]');
	if (is_array($strings) && array_key_exists(0, $strings)) {
	  $string = $strings[0];
	} else {
	  continue;
	}
	echo '<!-- ' . $v . ' -->';
	$v = clean_nl($v);
        if (strlen($v) == 0)  {
          continue;
        }
	$spos = strpos($string, 'http:');
	$tpos = strpos($v, 'http:');
	if (($spos === false && $tpos === false) || $spos !== false) {
	  if (is_array($tstrings) && array_key_exists(0, $tstrings)) {
	    $tstring = $tstrings[0];
	    $tstring[0] = (string) $v;
	  } else {
	    $tstring = $txml->addChild('string', $v);
	    $tstring->addAttribute('name', $k);
	    $tstring->addAttribute('formatted', 'false');
	  }
	  sxmle_setarg($tstring, 'username', $username);
	  sxmle_setarg($tstring, 'orig', (string) $string);
	}
      }
    } else if ($action == 'edit-string-array') {
      $arrayname = '';
      $arrayvalue = array();
      foreach ($_POST as $k => $v) {
	if ($k == 'action' or $k == 'lang' or $k == 'file') {
	  continue;
	}
	list($arrayname, $i) = split(',', $k);
	$arrayvalue[$i] = $v;
      }
      $add = true;
      if (!empty($arrayname)) {
	$k = $arrayname;
	$strings = $sxml->xpath('//string-array[@name="' . $k . '"]');
	$tstrings = $txml->xpath('//string-array[@name="' . $k . '"]');
	if (is_array($strings) && array_key_exists(0, $strings)) {
	  $string = $strings[0];
	} else {
	  $add = false;
	}
	$i = 0;
	foreach ($string->item as $item) {
	  $spos = strpos($item, 'http:');
	  if (array_key_exists($i, $arrayvalue)) {
	    $tpos = strpos($arrayvalue[$i], 'http:');
	  } else {
	    $tpos = false;
	  }
	  if ($spos === false && $tpos !== false) {
	    $add = false;
	    break;
	  }
	  $i++;
	}
      }
      if ($add === true) {
	if (is_array($tstrings) && array_key_exists(0, $tstrings)) {
	  $tstring = $tstrings[0];
	} else {
	  $tstring = $txml->addChild('string-array');
	  $tstring->addAttribute('name', $k);	
	}
	if (!empty($tstring['username'])) {
	  $tstring['username'] = $username;
	} else {
	  $tstring->addAttribute('username', $username);
	}
	for ($i = 0; $i<count($arrayvalue); $i++) {
	  if (!empty($tstring->item[$i])) {
	    if (!empty($arrayvalue[$i])) {
	      $tstring->item[$i] = $arrayvalue[$i];
	      sxmle_rmarg($tstring->item[$i], 'notranslation');
	    } else {
	      $tstring->item[$i] = $string->item[$i];
	      sxmle_setarg($tstring->item[$i], 'notranslation', 'true');
	    }
	  } else {
	    if (!empty($arrayvalue[$i])) {
	      $tstring->addChild('item', $arrayvalue[$i]);
	      sxmle_rmarg($tstring->item[$i], 'notranslation');
	    } else {
	      $tstring->addChild('item', $string->item[$i]);
	      sxmle_setarg($tstring->item[$i], 'notranslation', 'true');
	    }
	  }
	}
      }
    }

    // clean xml
    foreach ($txml->string as $string) {
      if (!empty($string['username'])) {
	$string['username'] = clean_username($string['username']);
      }
    }
    foreach ($txml->{'string-array'} as $string) {
      if (!empty($string['username'])) {
	$string['username'] = clean_username($string['username']);
      }
    }

    // write xml
    sxmle_writexml($txml, $location, $lang, $file);
  }

  $alltext = '';
  // show forms
  foreach ($sxml->string as $string) {
    if ($string['translatable'] == 'false') {
      continue;
    }
    $k = $string['name'];
    $v = (string) $string;

    $numlines = count(split('\\\n', $v));
    $numlines += strlen($v) / 80;
    $decodedv = decode_string($v);

    $tstrings = $txml->xpath('//string[@name="' . $k . '"]');
    if (is_array($tstrings) && array_key_exists(0, $tstrings)) {
      $tstring = array();
      $tstring = $tstrings[0];
      $tv =  $tstring;
      $decodedtv = decode_string($tv);
      if (empty($decodedtv)) {
	$color = $color_red;
	$alltext = $alltext . "\n\n" . $decodedv;
      } else if (!empty($tstring['orig']) && $v != $tstring['orig']) {
	$color = $color_yellow;
	$alltext = $alltext . "\n\n" . $decodedtv;
      } else {
	$color = $color_green;
	$alltext = $alltext . "\n\n" . $decodedtv;
      }
    } else {
      $tv = '';
      $decodedtv = decode_string($tv);
      $color = $color_red;
    }

    $form = '';
    if ($redirect) {
      $action=$basedir.$lang.'/'.$file.'#'.$k.'" id="'.$k;
    } else {
      $action=$basedir.'edit.php?lang='.$lang.'&amp;file='.$file.'#'.$k.'" id="'.$k;
    }
    $form = $form.'<form method="post" action="'.$action.'" class="stringblock'.hidegreenclass($color == $color_green).'">'."\n";
    $form = $form.'<p>';
    $form = $form.'  String name: <b>'.$k."</b><br/>\n";
    if (!empty($username) && !empty($tstring['username'])) {
      $form = $form.'  Translator: <input type="text" disabled="disabled" value="'.clean_username($tstring['username']).'" size="50" />'."<br/>\n";
    }
    $form = $form.'  <input name="action" value="edit-string" type="hidden" />'."\n";
    $form = $form.'  en: <textarea disabled="disabled" cols="80" rows="'.$numlines.'">'.$decodedv.'</textarea>'."<br/>\n";
    $form = $form.'  '.$lang.': <textarea name="'.$k.'" cols="80" rows="'.$numlines.'" '.$color.'>'.$decodedtv.'</textarea>'."<br/>\n";
    $form = $form.'  <input name="hidegreen" value="'.hidegreen().'" type="hidden" />'."\n";
    if (empty($username)) {
      $form = $form.'  <input type="submit" disabled="disabled" /> To prevent spam, you must set your username on the <a href="'.$basedir.'">index page</a> before submitting translation.'."<br/>\n";
    } else {
      $form = $form.'  <input type="submit" />'."<br/>\n";
    }
    $form = $form.'</p>';
    $form = $form.'</form>'."\n";
    echo $form;
  }

  // show array forms
  foreach ($sxml->{'string-array'} as $stringarray) {
    if ($stringarray['translatable'] == 'false') {
      continue;
    }
    $k = $stringarray['name'];
    $tstringarrays = $txml->xpath('//string-array[@name="' . $k . '"]');
    if (is_array($tstringarrays) && array_key_exists(0, $tstringarrays)) {
      $tstringarray = $tstringarrays[0];
    } else {
      $tstringarray = array(); // FIXME
    }
    
    $formcolor = $color_green;
    $form = '';
    if ($redirect) {
      $action=$basedir.$lang.'/'.$file.'#'.$k.'" id="'.$k;
    } else {
      $action=$basedir.'edit.php?lang='.$lang.'&amp;file='.$file.'#'.$k.'" id="'.$k;
    }
    $form = $form.'<form method="post" action="'.$action.'" class="stringblock'.hidegreenclass($color == $color_green).'">'."\n"; // FIXME
    $form = $form.'  String name: <b>'.$k."</b><br/>\n";
    if (!empty($username) && !empty($tstringarray['username'])) {
      $form = $form.'  translator: <input type="text" disabled="disabled" value="'.clean_username($tstringarray['username']).'" size="50" />'."<br/>\n";
    }
    $form = $form.'  <input name="action" value="edit-string-array" type="hidden" />'."\n";
    $form = $form.'  <table>';
    $i = 0;
    foreach ($stringarray->item as $item) {
	$av = (string) $item;
	$tv = (string) $tstringarray;
	if (count($tstringarray) > $i && empty($tstringarray->item[$i]['notranslation'])) {
	  $atv = (string) $tstringarray->item[$i];
	} else {
	  $atv = '';
	}
	$decodedv = decode_string($av);
	$decodedtv = decode_string($atv);
	if (
	  empty($decodedtv) && !empty($decodedv) ||
	  empty($decodedv) && !empty($decodedtv)
	  ) {
	  $color = $color_red;
	  $formcolor = $color;
	} else {
	  $color = $color_green;
	}
        $numlines = count(split('\\\n', $av));
        $numlines += strlen($av) / 50;
	// echo '  <!-- '.$av.'-->'."\n";
        $form = $form.'  <tr>';
        $form = $form.'  <td>';
	$form = $form.'  '.$i.': '."\n";
        $form = $form.'  </td>';
        $form = $form.'  <td>';
	$form = $form.'  <textarea disabled="disabled" value="'.$decodedv.'" cols="50" rows="'.$numlines.'">'.$decodedv.'</textarea>';
        $form = $form.'  </td>';
        $form = $form.'  <td>';
	$form = $form.'  <textarea name="'.$k.','.$i.'" value="'.$decodedtv.'"  '.$color.' cols="60" rows="'.$numlines.'">'.$decodedtv.'</textarea>';
        $form = $form.'  </td>';
        $form = $form.'  </tr>';
        $form = $form."\n";
	$i++;
      }
      $form = $form.'  </table>';
      $form = $form.'  <input name="hidegreen" value="'.hidegreen().'" type="hidden" />'."\n";
      if (empty($username)) {
        $form = $form.'  <input type="submit" disabled="disabled" /> To prevent spam, you must set your username on the <a href="'.$basedir.'">index page</a> before submitting translation.'."<br/>\n";
      } else {
        $form = $form.'  <input type="submit" />'."<br/>\n";
      }
      $form = $form.'</form>'."\n";

      echo $form;
  }
  
  // show $alltext if $file==market.xml
  if ($file == 'market.xml') {
    $numlines = count(split("\n", $alltext));
    $numlines += strlen($alltext) / 80;
    $form = '<form>';
    $form = $form."<b>Merged text:</b><br/>\n";
    $form = $form.'<textarea disabled="disabled" cols="80" rows="'.$numlines.'">' . $alltext . '</textarea></form>';
    $form = $form."<hr/>\n";
    echo $form;
  }
}
?>

</div>

</body>
</html>
