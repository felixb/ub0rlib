<? 

header('Content-Type:text/html; charset=UTF-8');

include('global.conf.php');

function decode_string($s) {
  $ret = $s;
  $ret = str_replace('\"', '"', $ret);
  $ret = str_replace("\'", "'", $ret);
  $ret = str_replace('\n', "\n", $ret);
  return $ret;
}


function encode_string($s) {
  $ret = $s;
  $ret = str_replace("'", "\'", $ret);
  $ret = str_replace("\\\\'", "\'", $ret);
  $ret = str_replace("\n", '\n', $ret);
  return $ret;
}


function get_arg($arg, $args) {
  $a = $args[$arg];
  if (!empty($a)) {
    return ' '.$arg.'="'.htmlspecialchars($a).'"';
  }
  return '';
}

function get_args($argnames, $args) {
  $ret = '';
  foreach ($argnames as $arg) {
    $ret = $ret.get_arg($arg, $args);
  }
  return $ret;
}

$defargs = array();
$defargs[] = 'username';
$color_green='style="background:#A0FFA0"';
$color_red='style="background:#FFA0A0"';
$color_yellow='style="background:#FFFFA0"';

$username = $_COOKIE['username'];

$lang = $_GET['lang'];
if (empty($lang)) {
  $lang = $_POST['lang'];
}

$files = array();
$d = dir($location.'res/values/');
while (false !== ($entry = $d->read())) {
  if ($entry == '.' or $entry == '..' or $entry == 'base.xml' or $entry == 'attrs.xml' or $entry == 'update.xml') {
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


?>
<html>
<head>
<title>Edit Translations: <? echo $lang; ?></title>
</head>
<body>

<h1>Translate Strings for <? echo $appname; ?> / <? echo $lang; ?></h1>

Your username: <? echo htmlspecialchars($username); ?></br></br>
Set username or select an other language on the <b><a href="index.php">index page</a></b>.</br>


<h3>Currently available files:</h3>
<?
foreach ($files as $f) {
  echo '<a href="edit.php?lang='.$lang.'&file='.$f.'">Edit file '.$lang.'/'.$f.'</a></br>';
}
?>

<?
$file = $_GET['file'];
if (empty($file)) {
  $file = $_POST['file'];
}
if (!empty($file)) {
  $hidegreen = $_GET['hidegreen'];
  if (empty($hidegreen) || $hidegreen != '1') {
    $hidegreen = 0;
  } else {
    $hidegreen = 1;
  }
  echo '<h3>Current file: '.$lang.'/'.$file."</h3>\n";
  if ($hidegreen) {
    echo '<a href="edit.php?lang='.$lang.'&file='.$file.'">[Show green strings]</a>';
  } else {
    echo '<a href="edit.php?lang='.$lang.'&file='.$file.'&hidegreen=1">[Hide green strings]</a>';
  }
  echo "</br>\n";
  echo "</br>\n";
  
  // load source strings
  $sourcexml = file_get_contents($location.'res/values/'.$file);
  $targetxml = file_get_contents($location.'res/values-'.$lang.'/'.$file);
  $sourcelines = split("\n", $sourcexml);
  $sourcestrings = array();
  $arrayname = '';
  $arrayvalue = array();
  foreach ($sourcelines as $line) {
    if (false === strpos($line, '<string') and 
	false === strpos($line, '</string-array>') and 
	false === strpos($line, '<item>')) {
      continue;
    }

    //echo '<!-- processing line: '.$line." -->\n";
    $tmp = split('name="', $line);
    list($linename, $tmp) = split('"', $tmp[1], 2);

    if (false !== strpos($line, '<string-array')) {
      //echo '<!-- new array: '.$linename." -->\n";
      $arrayname = $linename;
      $arrayvalue = array();
      continue;
    }

    if (false !== strpos($line, '</string-array>')) {
      //echo '<!-- close array: '.$arrayname." -->\n";
      $sourcestrings[$arrayname] = $arrayvalue;
      $arrayname = '';
      //echo '<!--';
      //print_r($arrayvalue);
      //echo '-->';
      continue;
    }


    if (!empty($arrayname)) {
      $tmp = split('<item>', $line, 2);
      list($linevalue, $tmp) = split('</item>', $tmp[1], 2);
      $arrayvalue[] = $linevalue;
      continue;
    }
    
    $tmp = split('>', $line, 2);
    list($linevalue, $tmp) = split('</string>', $tmp[1], 2);
    $sourcestrings[$linename] = $linevalue;
  }

  // load target strings
  $targetlines = split("\n", $targetxml);
  $targetstrings = array();
  $targetargs = array();
  foreach ($targetlines as $line) {
    if (false === strpos($line, '<string') and 
	false === strpos($line, '</string-array>') and 
	false === strpos($line, '<item>')) {
      continue;
    }

    if (false !== strpos($line, '</string-array>')) {
      //echo '<!-- close array: '.$arrayname." -->\n";
      $targetstrings[$arrayname] = $arrayvalue;
      $arrayname = '';
      //echo '<!--';
      //print_r($arrayvalue);
      //echo '-->';
      continue;
    }

    // echo '<!-- processing line: '.$line." -->\n";
    $tmp = split('name="', $line);
    list($linename, $tmp) = split('"', $tmp[1], 2);

    if (!empty($linename)) {
      foreach ($defargs as $arg) {
	$tmp = split($arg.'="', $line);
	list($argval, $tmp) = split('"', $tmp[1], 2);
	$targetargs[$linename][$arg] = htmlspecialchars_decode($argval);
      }
    }

    if (false !== strpos($line, '<string-array')) {
      //echo '<!-- new array: '.$linename." -->\n";
      $arrayname = $linename;
      $arrayvalue = array();
      continue;
    }



    if (!empty($arrayname)) {
      $tmp = split('<item>', $line, 2);
      list($linevalue, $tmp) = split('</item>', $tmp[1], 2);
      $arrayvalue[] = $linevalue;
    }
    
    $tmp = split('>', $line, 2);
    list($linevalue, $tmp) = split('</string>', $tmp[1], 2);
    $targetstrings[$linename] = $linevalue;
  }

  // process new strings
  $action = $_POST['action'];
  if (empty($action)) {
    $action = $_GET['action'];
  }
  if (!empty($action)) {
    if ($action == 'edit-string') {
      foreach ($_POST as $k => $v) {
	if ($k == 'action' or $k == 'lang' or $k == 'file') {
	  continue;
	}
	$targetstrings[$k] = $v;
	$targetargs[$k]['username'] = $username;
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
      if (!empty($arrayname)) {
	$targetstrings[$arrayname] = $arrayvalue;
	$targetargs[$arrayname]['username'] = $username;
      }
    }

    // write xml
    $xml = '<?xml version="1.0" encoding="utf-8"?>
       <!--
               Copyright (C) 2009-2011 Felix Bechstein
       -->
       <!--
               This file is part of '.$appname.'. This program is free software; you can
               redistribute it and/or modify it under the terms of the GNU General
               Public License as published by the Free Software Foundation; either
               version 3 of the License, or (at your option) any later version.
       -->
       <!--
               This program is distributed in the hope that it will be useful, but
               WITHOUT ANY WARRANTY; without even the implied warranty of
               MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
               General Public License for more details. You should have received a
               copy of the GNU General Public License along with this program; If
               not, see <http://www.gnu.org/licenses/>.
       -->
      <!--
               This file is generated automatically by ub0rlib/php.
       -->
';

    $xml = $xml.'<resources>'."\n";
    foreach ($sourcestrings as $k => $v) {
      if (!is_array($v)) {
	$xml = $xml.'  <string name="'.$k.'" formatted="false"'.get_args($defargs, $targetargs[$k]).'>'.encode_string($targetstrings[$k]).'</string>'."\n";
      } else {
	$xml = $xml.'  <string-array name="'.$k.'"'.get_args($defargs, $targetargs[$k]).'>'."\n";
	$tv = $targetstrings[$k];
	foreach ($tv as $tvv) {
	  $xml = $xml.'    <item>'.encode_string($tvv).'</item>'."\n";
	}
	$xml = $xml.'  </string-array>'."\n";
      }
    }
    $xml = $xml.'</resources>'."\n";
    file_put_contents($location.'res/values-'.$lang.'/'.$file, $xml);
  }

  // show forms
  foreach ($sourcestrings as $k => $v) {
    if (!is_array($v)) {
      $numlines = count(split('\\\n', $v));
      $numlines += strlen($v) / 80;
      $decodedv = decode_string($v);

      $tv = $targetstrings[$k];
      $decodedtv = decode_string($tv);
      if (empty($decodedtv)) {
	$color = $color_red;
      } else {
	$color = $color_green;
      }
      if ($hidegreen and $color == $color_green) {
	continue;
      }

      $form = '';
      $form = $form.'<form method="POST" action="edit.php?lang='.$lang.'&file='.$file.'&hidegreen='.$hidegreen.'#'.$k.'" id="'.$k.'">'."\n";
      $form = $form.'  String name: <b>'.$k."</b></br>\n";
      if (!empty($username) and !empty($targetargs[$k]['username'])) {
	$form = $form.'  translator: <input type="text" disabled="disabled" value="'.$targetargs[$k]['username'].'" size="50" />'."</br>\n";
      }
      $form = $form.'  <input name="action" value="edit-string" type="hidden" />'."\n";
      $form = $form.'  en: <textarea disabled="disabled" cols="80" rows="'.$numlines.'">'.$decodedv.'</textarea>'."</br>\n";
      $form = $form.'  '.$lang.': <textarea name="'.$k.'" cols="80" rows="'.$numlines.'" '.$color.'>'.$decodedtv.'</textarea>'."</br>\n";
      $form = $form.'  <input type="submit" />'."</br>\n";
      $form = $form.'</form>'."\n";
      $form = $form."<hr>\n";
      echo $form;
    } else {
      $formcolor = $color_green;
      $form = '';
      $form = $form.'<form method="POST" action="edit.php?lang='.$lang.'&file='.$file.'&hidegreen='.$hidegreen.'#'.$k.'" id="'.$k.'">'."\n";
      $form = $form.'  String name: <b>'.$k."</b></br>\n";
      if (!empty($username) and !empty($targetargs[$k]['username'])) {
	$form = $form.'  translator: <input type="text" disabled="disabled" value="'.$targetargs[$k]['username'].'" size="50" />'."</br>\n";
      }
      $form = $form.'  <input name="action" value="edit-string-array" type="hidden" />'."\n";
      $i = 0;
      foreach ($v as $av) {
	$tv = $targetstrings[$k];
	if (is_array($tv) and count($tv) > $i) {
	  $atv = $tv[$i];
	} else {
	  $atv = "";
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
	// echo '  <!-- '.$av.'-->'."\n";
	$form = $form.'  '.$i.': '."\n";
	$form = $form.'  <input disabled="disabled" value="'.$decodedv.'" size="35" />';
	$form = $form.'  <input name="'.$k.','.$i.'" value="'.$decodedtv.'"  '.$color.' size="35" />';
	$form = $form."  </br>\n";
	$i++;
      }
      $form = $form.'<input type="submit" />'."</br>\n";
      $form = $form.'</form>'."\n";
      $form = $form."<hr>\n";

      if ($hidegreen and $formcolor == $color_green) {
	continue;
      }
      echo $form;
    }
  }
}
?>

</body>