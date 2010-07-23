#! /bin/bash

ODIR=${PWD}

if [ -z "$1" ] ; then
	p=$(basename ${PWD})
	cd ..
else
	p=${1}
fi

export PATH="${ODIR}/$(dirname $0):$PATH"

cd "${p}" || exit -1

source deploy.inc.sh

pversion=${pversion}-doc-$(date +%Y%m%d)
./builddoc.sh
rm doc.zip
zip -r doc.zip doc
cp doc.zip "${pname}-${pversion}.zip"

[ -n "${gproject}" ] && \
googlecode_upload.py -u ${gcodelogin} -w ${gcodepassw} -p ${gproject}  -s ${pname}-${pversion}  -l Type-Docs,OpSys-Android "${pname}-${pversion}.zip"
rm "${pname}-${pversion}.zip"

cd ${ODIR}
