#! /bin/bash

if echo "$(basename $PWD)" | grep -q "connector" ; then
	echo -n "bump connector: "
	basename $PWD
	n=$(fgrep app_name res/values/*.xml | cut -d\> -f2 | cut -d\< -f1 | tr ' ' '-' | tr -d ':')
else
	echo "bump app"
	n=$(fgrep app_name res/values/*.xml | cut -d\> -f2 | cut -d\< -f1 | tr -d \ )
fi

v=${1}

if [ -n "$2" ] ; then
	vn="$v $2"
else
	vn=$v
fi
tag="${n}-${vn/ /-}"

vv=$(echo ${vn}000000 | tr -cd '[:digit:]' | head -c6)
msdk=$(grep -o 'minSdkVersion="[0-9]*"' AndroidManifest.xml | cut -d\" -f2)
if [ -z "${msdk}" ] ; then
  msdk=3
fi
vv="${msdk}${vv}"
avv=$(grep -o 'versionCode="[0-9]*"' AndroidManifest.xml | cut -d\" -f2)
if [ ${vv} -le ${avv} ] ; then
  vv=$((${avv} + 1))
fi

echo v    $v
echo vn   $vn
echo msdk $msdk
echo vv   $vv
echo n    $n
echo tag  ${tag}
echo tagm  "$(echo ${n} | tr '-' ' ' | sed -e 's/Connector /Connector: /') v${vn}"

sed -i -e "s/android:versionName=\"[^\"]*/android:versionName=\"${vn}/" AndroidManifest.xml
sed -i -e "s/android:versionCode=\"[^\"]*/android:versionCode=\"${vv}/" AndroidManifest.xml
vfile=$(grep -l app_version res/values/*.xml)
sed -i -e "/app_version/s/>[^<]*/>${vn}/" "${vfile}"
sed -i -e "s/HEAD/v$v/" res/values/update.xml

git diff

ant debug || exit -1

mv bin/*-debug.apk ~/public_html/h/flx/ 2> /dev/null

echo "enter for commit+tag"
read a
git commit -m "bump to ${n} v${vn}" .
git tag -a "${tag}" -m "${n} v${vn}" 

