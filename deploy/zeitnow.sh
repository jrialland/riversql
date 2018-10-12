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
rm ./ROOT -rf
cp $exploded ./ROOT -R

projectname=$(git config --local remote.origin.url | sed s_^.*github.com/__ | sed s-/-_-)

cat > now.json << EOF
{
    "name": "$projectname",
    "alias": "$projectname"
}
EOF

#now --public --token $NOW_TOKEN && now alias --token $NOW_TOKEN

