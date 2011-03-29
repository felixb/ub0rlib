#! /bin/bash

function remove_logs {
	echo "remove logs from source: $(basename $1)"

	for f in $(find $1/src/ -name \*.java) ; do
		sed \
			-e 's: Log\.v: //Log.v:' \
			-e 's: Log\.d: //Log.d:' \
			-e 's:\tLog\.v:\t//Log.v:' \
			-e 's:\tLog\.d:\t//Log.d:' \
			-i $f
		if [ ${doamazon} -eq 1 ] ; then
			sed -e 's:GOOGLE_SKIP = false:GOOGLE_SKIP = true:' -i $f
		fi
	done
}

for lib in $PWD $(grep android.library.reference default.properties | cut -d= -f2) ; do
	remove_logs $lib
done

echo "set debuggable=false"

sed -e 's/android:debuggable="true"/android:debuggable="false"/' -i AndroidManifest.xml
