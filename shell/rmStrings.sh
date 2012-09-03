#! /bin/bash

for p in $@ ; do
  echo "remove string: $p"
  for f in $(grep -lF 'name="'$p'"' res/values*/*.xml) ; do
    echo "clean file: $f"
    if (grep -Fq '<string name="'$p'"' $f) ; then
      sed -e '/string name="'$p'"/d' -i $f
    elif (grep -Fq '<string-array name="'$p'"' $f) ; then
      base=$(grep -Fn '<string-array name="'$p'"' $f | cut -d: -f1)
      end=$(tail -n +$base $f | grep -n '</string-array>' | head -n1 | cut -d: -f1)
      end=$(($end - 1))
      sed -e "${base},+${end}d" -i $f
    fi
  done
done
