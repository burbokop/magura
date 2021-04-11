#!/bin/sh

sbt assembly
sbt debian:packageBin
sudo dpkg -i ./target/magura_0.1.1_all.deb
