#! /bin/bash

if [ -z "$2" ] ; then
  echo "usage: $0 file string-array"
  echo "  this script will translate existing <item> elements into single strings and clean up the xml files"
  echo ""
  echo "  file: file within res/values/"
  echo "  string-array: <string-array> element within that file"
  echo "    this <string-array> needs to has <item>@string/whateverstring</item> items only"
  exit 5
fi

tfn=$1
tf=res/values/${tfn}
ta=$2

tmpfile=/tmp/splitarray.$$

if [ ! -e "${tf}" ] ; then
  echo "file not found: ${tf}"
  exit 5
fi

cnt=0
for i in $(seq 1 1000) ; do
  if $(sed -e '/name="'${ta}'"/,+'${i}'{ /<\/string-array>/p; }' -n "${tf}" | grep -q 'string-array') ; then
    cnt=${i}
    break
  fi
done

if [ $cnt -le 0 ] ; then
  echo "string-array has no elements"
  exit 5
fi

sed -e '/name="'${ta}'"/,+'${cnt}'p' -n "${tf}" | tee "${tmpfile}"

for i in $(seq 2 ${cnt}) ; do
  sn=$(sed -e ${i}p -n "${tmpfile}" | cut -d/ -f2 | cut -d\< -f1)
  i=$((${i} - 1))
  sed -e '/name="'${ta}'"/,+'${i}'{ s:item:string:g; s:<string>:<string name="'${sn}'">: ; }' -i res/values-*/${tfn}
done

sed \
  -e '/name="'${ta}'"/,+'${cnt}'{ /string-array/d; /notranslation/d; /item/d; }' \
  -e 's:    <string :  <string :' \
  -i res/values-*/${tfn}

rm "${tmpfile}"
