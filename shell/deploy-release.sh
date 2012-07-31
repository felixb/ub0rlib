#! /bin/bash

ODIR=${PWD}

doamazon=0
if [ "x$1" = "xamazon" ] ; then
	doamazon=1
	shift
fi
export doamazon

if [ -z "$1" ] ; then
	p=$(basename ${PWD})
	cd ..
else
	p=${1}
fi

export PATH="${ODIR}/$(dirname $0):$PATH"

cd "${p}" || exit -1

source deploy.inc.sh

keyfile=../release.ks.pw

preDeploy.sh
ant clean
ant debug || exit -1
ant release 

if [ ${doamazon} -eq 1 ] ; then
	mv bin/*-release.apk bin/${fname}-${pversion}-amazon.apk
else
	[ $(adb devices | grep -ve 'List of devices' -e '^$' | wc -l) -eq 0 ] || adb -d install -r bin/*-debug.apk || adb -d install -r bin/*-release.apk
	mv bin/*-release.apk bin/${fname}-${pversion}.apk
	[ -n "${gproject}" ] && \
		googlecode_upload.py -u ${gcodelogin} -w ${gcodepassw} -p ${gproject}  -s "${sname}-${pversion}"  -l Featured,Type-Package,OpSys-Android${lextra}  bin/${fname}-${pversion}.apk
fi
postDeploy.sh

cd ${ODIR}
