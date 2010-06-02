#! /bin/bash

[ -z "$1" ] && exit -1

export PATH="$PWD/$(dirname $0):$PATH"

cd "$1" || exit -1

source deploy.inc.sh

pversion=${pversion}-doc-$(date +%Y%m%d)
./builddoc.sh
rm doc.zip
zip -r doc.zip doc
cp doc.zip "${pname}-${pversion}.zip"

[ -n "${gproject}" ] && \
googlecode_upload.py -u ${gcodelogin} -w ${gcodepassw} -p ${gproject}  -s ${pname}-${pversion}  -l Type-Docs,OpSys-Android "${pname}-${pversion}.zip"
rm "${pname}-${pversion}.zip"

