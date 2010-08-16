#! /bin/sh

function remove_logs {
	echo "remove logs from source: $(basename $1)"

	for f in $(find src/ -name \*.java) ; do
		sed \
			-e 's: Log\.v: //Log.v:' \
			-e 's: Log\.d: //Log.d:' \
			-e 's:\tLog\.v:\t//Log.v:' \
			-e 's:\tLog\.d:\t//Log.d:' \
			-i $f
	done
}

for lib in $PWD $(grep android.library.reference default.properties | cut -d= -f2) ; do
	remove_logs $lib
done

echo "set debuggable=false"

sed -e 's/android:debuggable="true"/android:debuggable="false"/' -i AndroidManifest.xml
