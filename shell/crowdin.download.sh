#! /bin/sh

[ -z "$1" ] && exit 1

identifier=$1
apikey=$(grep "${identifier}" ../crowdin.apikeys | cut -d\  -f2 | head -n1)

[ -z "${apikey}" ] && exit 2

mkdir tmp
wget http://crowdin.net/api/project/${identifier}/download/all.zip?key=${apikey} -O tmp/all.zip
cd tmp

unzip all.zip
rm all.zip
cd ..

for f in tmp/* ; do
	llang=$(echo $f | cut -d/ -f2)
	lang=$(echo $llang | cut -d- -f1)

	target=""
	if [ -e "res/values-$llang" ] ; then
		target="res/values-$llang"
	elif [ -e "res/values-$lang" ] ; then
		target="res/values-$lang"
	elif [ "$lang" == "no" -a -e "res/values-nb" ] ; then
		target="res/values-nb"
	fi

	if [ -n "$target" ] ; then
		echo mv $f/* $target/
		mv $f/* $target/
		rmdir $f
		sed -e 's:string :string formatted="false" :' -i  $target/*.xml
		for xml in $target/*.xml ; do
			mv $xml $xml.old
			sed -n -e '1,/<resources>/p' res/values/strings.xml > $xml
			sed -e '1,/<resources>/d'  $xml.old >> $xml
			rm $xml.old
		done
	fi
done

ls -1 tmp

rm -r tmp
