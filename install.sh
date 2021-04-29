#!/bin/sh

sbt assembly
sbt debian:packageBin
source ./scripts/grab_release.sh
sudo dpkg -i $MAGURA_DEB
