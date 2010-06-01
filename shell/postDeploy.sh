#! /bin/sh

function add_logs {
	echo "add logs in source: $(basename $1)"

	for f in $(find src/ -name \*.java) ; do
		sed \
			-e 's:///*Log.v:Log.v:' \
			-e 's:///*Log.d:Log.d:' \
			-i $f
	done
}

for lib in $PWD $(grep android.library.reference default.properties | cut -d= -f2) ; do
	add_logs $lib
done

echo "set debuggable=true"

sed -e 's/android:debuggable="false"/android:debuggable="true"/' -i AndroidManifest.xml
