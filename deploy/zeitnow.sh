#!/bin/bash
thisscript=$(readlink -m $0)
thisdir=$(dirname $thisscript)
basedir=$(readlink -m $thisdir/..)

mv $basedir/target/*.war ./

now --public --token $NOW_TOKEN && now alias --token $NOW_TOKEN

