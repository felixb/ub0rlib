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
$defargs[] = 'orig';
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
  if ($entry == '.' or $entry == '..' or $entry == 'base.xml' or $entry == 'attrs.xml' or $entry == 'update.xml' or $entry == 'cwac_touchlist_attrs.xml') {
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

$hidegreen = $_GET['hidegreen'];
if (empty($hidegreen) || $hidegreen != '1') {
  $hidegreen = 0;
} else {
  $hidegreen = 1;
}


?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
<head>
<meta http-equiv="Content-type" content="text/html;charset=UTF-8" />
<title>Edit Translations: <? echo $lang; ?></title>
</head>
<body>

<h1>Translate Strings for <? echo $appname; ?> / <? echo $lang; ?></h1>
<p>
Your username: <? echo htmlspecialchars($username); ?><br/><br/>
Set username or select an other language on the <b><a href="./">index page</a></b>.
</p>

<h3>Currently available files:</h3>
<p>
<?

$file = $_GET['file'];
if (empty($file)) {
  $file = $_POST['file'];
}

foreach ($files as $f) {
  echo '<a href="edit.php?lang='.$lang.'&amp;file='.$f.'&amp;hidegreen='.$hidegreen.'">';
  if ($file == $f) {
    echo '<b>';
  }
  echo 'Edit file '.$lang.'/'.$f;
  if ($file === $f) {
    echo '</b>';
  }
  echo '</a><br/>';
  echo "\n";
}
?>
</p>
<?
if (!empty($file)) {
  echo '<h3>Current file: '.$lang.'/'.$file."</h3>\n";
  echo '<p>';
  echo 'Color codes: <ul><li>green == ok</li><li>yellow == original text changed since last translation</li><li>red == missing string</li></ul>'."\n";
  if ($hidegreen) {
    echo '<a href="edit.php?lang='.$lang.'&amp;file='.$file.'">[Show green strings]</a>';
  } else {
    echo '<a href="edit.php?lang='.$lang.'&amp;file='.$file.'&amp;hidegreen=1">[Hide green strings]</a>';
  }
  echo "<br/>\n";
  echo "<br/>\n";
  echo '</p>';
  echo "<hr/>\n";
  
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
	false === strpos($line, '<item')) {
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
      if (count($tmp) < 2) {
        $arrayvalue[] = '';
      } else {
        list($linevalue, $tmp) = split('</item>', $tmp[1], 2);
        $arrayvalue[] = $linevalue;
      }
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
	$spos = strpos($sourcestrings[$k], 'http:');
	$tpos = strpos($v, 'http:');
	if (($spos === false && $tpos === false) || $spos !== false) {
		$targetstrings[$k] = $v;
		$targetargs[$k]['username'] = $username;
		$targetargs[$k]['orig'] = $sourcestrings[$k];
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
               Visit http://'.$_SERVER['SERVER_NAME'].$_SERVER['PHP_SELF'].'?lang='.$lang.'&file='.$file.' to edit the file.
       -->
';

    $xml = $xml.'<resources>'."\n";
    foreach ($sourcestrings as $k => $v) {
      if (!is_array($v)) {
	if(!empty($targetstrings[$k])) {
	  $xml = $xml.'  <string name="'.$k.'" formatted="false"'.get_args($defargs, $targetargs[$k]).'>'.encode_string($targetstrings[$k]).'</string>'."\n";
	}
      } else {
	$empty = true;
	$xmlsnip = '';
	$xmlsnip = $xmlsnip.'  <string-array name="'.$k.'"'.get_args($defargs, $targetargs[$k]).'>'."\n";
	$tv = $targetstrings[$k];
	$i = 0;
	foreach ($tv as $tvv) {
	  if (!empty($tvv)) {
	    $empty = false;
	    $xmlsnip = $xmlsnip.'    <item>'.encode_string($tvv).'</item>'."\n";
	  } else {
            $tvv = $sourcestrings[$k][$i];
	    $xmlsnip = $xmlsnip.'    <item notranslation="true">'.encode_string($tvv).'</item>'."\n";
	  }
	  $i++;
	}
	$xmlsnip = $xmlsnip.'  </string-array>'."\n";
	if (!$empty) {
	  $xml = $xml.$xmlsnip;
	}
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
      } else if (!empty($targetargs[$k]['orig']) and $v != $targetargs[$k]['orig']) {
	$color = $color_yellow;
      } else {
	$color = $color_green;
      }
      if ($hidegreen and $color == $color_green) {
	continue;
      }

      $form = '';
      $form = $form.'<form method="post" action="edit.php?lang='.$lang.'&amp;file='.$file.'&amp;hidegreen='.$hidegreen.'#'.$k.'" id="'.$k.'">'."\n";
      $form = $form.'<p>';
      $form = $form.'  String name: <b>'.$k."</b><br/>\n";
      if (!empty($username) and !empty($targetargs[$k]['username'])) {
	$form = $form.'  translator: <input type="text" disabled="disabled" value="'.$targetargs[$k]['username'].'" size="50" />'."<br/>\n";
      }
      $form = $form.'  <input name="action" value="edit-string" type="hidden" />'."\n";
      $form = $form.'  en: <textarea disabled="disabled" cols="80" rows="'.$numlines.'">'.$decodedv.'</textarea>'."<br/>\n";
      $form = $form.'  '.$lang.': <textarea name="'.$k.'" cols="80" rows="'.$numlines.'" '.$color.'>'.$decodedtv.'</textarea>'."<br/>\n";
      $form = $form.'  <input type="submit" />'."<br/>\n";
      $form = $form.'</p>';
      $form = $form.'</form>'."\n";
      $form = $form."<hr/>\n";
      echo $form;
    } else {
      $formcolor = $color_green;
      $form = '';
      $form = $form.'<form method="post" action="edit.php?lang='.$lang.'&amp;file='.$file.'&amp;hidegreen='.$hidegreen.'#'.$k.'" id="'.$k.'">'."\n";
      $form = $form.'  String name: <b>'.$k."</b><br/>\n";
      if (!empty($username) and !empty($targetargs[$k]['username'])) {
	$form = $form.'  translator: <input type="text" disabled="disabled" value="'.$targetargs[$k]['username'].'" size="50" />'."<br/>\n";
      }
      $form = $form.'  <input name="action" value="edit-string-array" type="hidden" />'."\n";
      $form = $form.'  <table>';
      $i = 0;
      foreach ($v as $av) {
	$tv = $targetstrings[$k];
	if (is_array($tv) and count($tv) > $i) {
	  $atv = $tv[$i];
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
      $form = $form.'  <input type="submit" />'."<br/>\n";
      $form = $form.'</form>'."\n";
      $form = $form."<hr/>\n";

      if ($hidegreen and $formcolor == $color_green) {
	continue;
      }
      echo $form;
    }
  }
}
?>

</body>
</html>
