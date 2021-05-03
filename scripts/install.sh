#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
CURRENT_DIR=`pwd`
cd $SCRIPT_DIR/..

sbt assembly
sbt debian:packageBin
source ./scripts/grab_release.sh
sudo dpkg -i $MAGURA_DEB

cd $CURRENT_DIR