#!/bin/bash


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
RELEASE_INFO_PATH=$SCRIPT_DIR/../target/release.info
export MAGURA_TAG=${$(awk -F "=" '/tag_name/ {print $2}' $RELEASE_INFO_PATH)##*( )}
export MAGURA_NEED_RELEASE=${$(awk -F "=" '/need_release/ {print $2}' $RELEASE_INFO_PATH)##*( )}
export MAGURA_DEB=${$(awk -F "=" '/deb/ {print $2}' $RELEASE_INFO_PATH)##*( )}

echo "RELEASE_INFO_PATH: $RELEASE_INFO_PATH"

cat $RELEASE_INFO_PATH

echo "MAGURA_TAG: $MAGURA_TAG"
echo "MAGURA_NEED_RELEASE: $MAGURA_NEED_RELEASE"
echo "MAGURA_DEB: $MAGURA_DEB"