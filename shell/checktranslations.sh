#! /bin/bash

for f in $(ls -1 res/values/*xml | sort) ; do
  fn=$(basename ${f})
  grep -Fe '<string' ${f} | grep -ve 'notranslation="true"' -e 'name="action"' -e 'translatable="false"' | grep -o 'name="[^"]*"' | \
  while read line ; do
    for d in $(ls -1d res/values-??/ | sort) ; do
      if [ -e "${d}/${fn}" ] ; then
        echo -n "${d}${fn}	${line}	"
        if ( grep -q "${line}" res/values-??/${fn} ) ; then
          echo ": ok"
        else
          echo ": missing"
        fi
      else
        echo "file not found: ${d}${fn}" 
      fi
    done
  done
done

