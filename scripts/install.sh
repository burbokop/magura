#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
CURRENT_DIR=`pwd`
cd $SCRIPT_DIR/..

if [[ "$1" != "--offline" ]]; then
  sbt "run generate-release-info github.com:burbokop.magura"
fi
sbt assembly
sbt debian:packageBin
if [[ "$1" != "--offline" ]]; then
  source ./scripts/grab_release.sh
  sudo dpkg -i $MAGURA_DEB
fi

cd $CURRENT_DIR