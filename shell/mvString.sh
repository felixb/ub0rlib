#! /bin/bash

p1=$1
p2=$2

echo "move string: $p1 to $p2"
for f in $(ls res/values*/strings.xml) ; do
	sed -i -e "s:name=\"${p1}\":name=\"${p2}\":" $f
done
