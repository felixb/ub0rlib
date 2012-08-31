#! /bin/bash

if [ "$(basename ${PWD})" == "websms" ] ; then
	lextra="${lextra},Product-WebSMS"
	pname=WebSMS
elif [ "$(basename ${PWD})" == "andGMXsms" ] ; then
	lextra="${lextra},Product-WebSMS-2"
	pname=WebSMS
elif [ "$(basename ${PWD})" == "smsdroid" ] ; then
	lextra="${lextra},Product-SMSdroid"
	pname=SMSdroid
elif [ "$(basename ${PWD})" == "donator-legacy" ] ; then
	pname=Donator
fi

rpath="../"

if echo "$(basename ${PWD})" | grep -q "connector" ; then
	pname=$(grep -F app_name res/values/*.xml | cut -d\> -f2 | cut -d\< -f1)
	gproject=websmsdroid
	pversion=$(grep -F app_version res/values/*.xml | head -n1 | cut -d\> -f2 | cut -d\< -f1)
	lextra=,Connector,Product-WebSMS
	cname=$(echo $pname | cut -d\  -f 3)
	fname="Connector-${cname}"
	tname="#WebSMS #Connector #${cname}"
else
	if [ -z "$pname" ] ; then
		pname=$(grep -F app_name res/values/*.xml | cut -d\> -f2 | cut -d\< -f1 | tr -d \ )
	fi
	gproject=$(grep -F $(basename ${PWD}) ${rpath}ub0rlib/shell/projects.list | head -n1 | cut -d\  -f2)
	pversion=$(grep -F app_version res/values/*.xml | cut -d\> -f2 | cut -d\< -f1)
	if [ -z "${pversion}" ] ; then
		pversion=$(grep -F 'android:versionName' AndroidManifest.xml | cut -d\" -f2)
	fi
	fname=$(echo $pname | tr -d ':' | tr ' ' '_')
	tname="#${fname}"
fi

gcodelogin=$(grep 'machine code.google.com' -A2 ${HOME}/.netrc | grep -oe login.\* | cut -d\  -f2)
gcodepassw=$(grep 'machine code.google.com' -A2 ${HOME}/.netrc | grep -oe password.\* | cut -d\  -f2)

pversion=$(echo $pversion | tr ' ' '-')
sname=$(echo $pname | tr -d ':' | tr ' ' '-')

if [ "$gproject" == "ub0rapps" ] ; then
	lextra="${lextra},Product-${pname}"
fi

echo "rpath     $rpath"
echo "pname     $pname"
echo "fname     $fname"
echo "sname     $sname"
echo "tname     $tname"
echo "gproject  $gproject"
echo "pversion  $pversion"
echo "lextra    $lextra"
echo "gc login  ${gcodelogin}"
echo "gc passw  ${gcodepassw}"

[ -z "${gcodelogin}" ] && exit -1
[ -z "${gcodepassw}" ] && exit -1

