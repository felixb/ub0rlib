#! /bin/sh

# $1 = target directory
# $2 = branch to merge from

[ -z "$1" ] && return 1
[ -d "$1" ] || return 2

if [ -z "$TERM" -o "$TERM" = "dumb" ] ; then
	export TERM=xterm
fi

eval $(keychain -q --eval)

cd $1

rm commit.log

ret=0
if [ $(git diff --shortstat | wc -l) != 0 ] ; then
	/home/flx/dev/android/ub0rlib/shell/updatestats.sh > commit.log 2> commit.log
	git commit -am "auto commit for translation" --author "ub0r bot <noreply@ub0r.de>" >> commit.log 2>> commit.log
	rc=$?
	if [ $rc != 0 ] ; then
		ret=$rc
		echo rc=$ret | tee -a commit.log
	fi
fi
git pull >> commit.log 2>> commit.log
rc=$?
if [ $rc != 0 ] ; then
	ret=$rc
	echo rc=$ret | tee -a commit.log
fi
if [ -n "$2" ] ; then
	git merge $2 >> commit.log 2>> commit.log
	rc=$?
	/home/flx/dev/android/ub0rlib/shell/updatestats.sh >> commit.log 2>> commit.log
	git commit -am "update translation stats" --author "ub0r bot <noreply@ub0r.de>" >> commit.log 2>> commit.log
fi
if [ $rc != 0 ] ; then
	ret=$rc
	echo rc=$ret | tee -a commit.log
fi
git push origin $(git branch --no-color | grep '*' | cut -d\  -f2) >> commit.log 2>> commit.log
rc=$?
if [ $rc != 0 ] ; then
	ret=$rc
	echo rc=$ret | tee -a commit.log
fi

if [ $ret != 0 ] ; then
	cat commit.log
fi
