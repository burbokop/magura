#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
RELEASE_INFO_PATH=$SCRIPT_DIR/../target/release.info
export MAGURA_TAG=$(awk -F "=" '/tag_name/ {print $2}' $RELEASE_INFO_PATH)
export MAGURA_NEED_RELEASE=$(awk -F "=" '/need_release/ {print $2}' $RELEASE_INFO_PATH)
export MAGURA_DEB=$(awk -F "=" '/deb/ {print $2}' $RELEASE_INFO_PATH)
