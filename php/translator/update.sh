#! /bin/sh

# $1 = branch to merge from

eval $(keychain -q --eval)

dir=$(grep location $(dirname $0)/local.conf.php | cut -d\' -f2)
cd $dir

git commit -am "auto commit for translation" --author "ub0r bot <noreply@ub0r.de>"
git pull
git merge $1
git push
