#!/bin/bash

set -euo pipefail

thisscript=$(readlink -m $0)
thisdir=$(dirname $thisscript)
basedir=$(readlink -m $thisdir/..)


warfile=$(find $basedir/target -type f -name "*.war" |head -1)

if [ "$warfile" == "" ]; then
    pushd $basedir
    mvn clean package
    popd
    warfile=$(find $basedir/target -type f -name "*.war" |head -1)
fi 

exploded=$(echo $warfile|sed s/.war//)
echo 'copying ' $exploded
rm ./ROOT -rf
cp $exploded ./ROOT -R

now --public --token $NOW_TOKEN && now alias --token $NOW_TOKEN

