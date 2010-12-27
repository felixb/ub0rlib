#! /bin/sh

[ -z "$1" ] && exit 1

identifier=$1
apikey=$(grep "${identifier}" ../crowdin.apikeys | cut -d\  -f2 | head -n1)

[ -z "${apikey}" ] && exit 2

lang=$2

if [ -z "$lang" ] ; then
	url=http://crowdin.net/api/project/${identifier}/update-file?key=${apikey}
	arg1="files[strings.xml]=@res/values/strings.xml"
	arg2="files[notes.xml]=@res/values/notes.xml"
else
	url=http://crowdin.net/api/project/${identifier}/upload-translation?key=${apikey}
	arg1="files[strings.xml]=@res/values-${lang}/strings.xml"
	arg2="files[notes.xml]=@res/values-${lang}/notes.xml"
fi

curl -F "${arg1}" "${url}"
curl -F "${arg2}" "${url}"

