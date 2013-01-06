#! /bin/bash

TARGETDIR="/home/flx/dev/translate"

cd "${TARGETDIR}"

for d in */res/ ; do
  cd "${TARGETDIR}/${d}/.."
  echo "update: ${PWD}"
  echo -n "" > translation.stats
  for f in $(ls -1 res/values/*xml ; ls -1 res/values-??/*xml | sort) ; do
    fn=$(basename ${f})
    if [ "${fn}" = "base.xml" -o "${fn}" = "styles.xml" -o "${fn}" = "dimen.xml" -o "${fn}" = "dimen_ambil.xml" -o "${fn}" = "arrays.xml" -o "${fn}" = "updates.xml" -o "${fn}" = "cwac_touchlist_attrs" ] ; then
      echo "skip: ${f}"
    else
      echo -n ${f}: >> translation.stats
      grep -Fe '<string' -e'<item' ${f} | grep -ve 'notranslation="true"' -e 'name="action"' -e 'translatable="false"' -e '^ *\t*$' -e '<item>@' -e '<item></item>' | wc -l >> translation.stats
    fi
  done
  for l in $(cut -d/ -f2 translation.stats  | sort -u) ; do
    #echo $l
    echo -n "res/${l}:" >> translation.stats.sum
    expr $(grep ${l}/ translation.stats | cut -d: -f2 | xargs -n9999 | sed -e 's: : + :g') >> translation.stats.sum
  done
  cat translation.stats.sum >> translation.stats
  rm translation.stats.sum
done

