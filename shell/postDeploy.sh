#! /bin/bash

function add_logs {
	echo "add logs in source: $(basename $1)"

	for f in $(find $1/src/ -name \*.java) ; do
		sed \
			-e 's:///*Log\.v:Log.v:' \
			-e 's:///*Log\.d:Log.d:' \
			-i $f
	done
	sed -e 's:GOOGLE_SKIP = true:GOOGLE_SKIP = false:' -i $f
}

for lib in $PWD $(grep android.library.reference *.properties | grep -Fve ActionBarSherlock -e Android-ViewPagerIndicator | cut -d= -f2) ; do
	add_logs $lib
done

echo "set debuggable=true"
sed -e 's/android:debuggable="false"/android:debuggable="true"/' -i AndroidManifest.xml

echo "set api key"
for f in $(grep -rl "0qZA1bZBqi-FBUCzyDAFazseN7fOJ3kzsoBtGIg" res/ src/) ; do
	sed -e 's/0qZA1bZBqi-FBUCzyDAFazseN7fOJ3kzsoBtGIg/0qZA1bZBqi-GIWgquIfvMAjb3StJluB8mW_xz-w/' -i $f
done
