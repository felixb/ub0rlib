#! /bin/sh

APIPATH='/opt/android-sdk-update-manager/'

if [ -z "$2" ] ; then
  echo "usage: $0 pngname source_api target_api"
  exit 1
fi

base="$APIPATH/platforms/android-$2/"
if [ -d "${base}" ] ; then
  for f in $(find $base/data/res/drawable-* -name $1.png) ; do
    if [ -n "$3" ] ; then
      tpath="res/$(basename $(dirname $f))-v$3/"
    else
      tpath="res/$(basename $(dirname $f))/"
    fi
    [ -e "$tpath" ] || mkdir -p "$tpath"
    cp $f "$tpath"
  done
else
  echo "$base not fround"
  exit 2
fi
