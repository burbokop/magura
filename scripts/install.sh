#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

cd $SCRIPT_DIR/..

sbt assembly
sbt debian:packageBin
source ./scripts/grab_release.sh
sudo dpkg -i $MAGURA_DEB
