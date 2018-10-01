#!/bin/bash

######### Declaration #########

VERSION="$1"
# DOWNLOADER_URL=http://localhost:8083
DOWNLOADER_URL=http://agbqhsbldd014v.agb.rbxd.ds/interlok-downloader

######### Validation #########

function validate()
{
  if [[ $1 = "" ]]
  then
    echo $2
    exit 1
  fi
}

validate "$VERSION" "VERSION cannot be empty"

######### Functions #########

function download()
{
  echo "### Resolve optional components and dependencies"
  
  # COMPONENTS=interlok-amqp interlok-apache-http interlok-as2 interlok-xml-security
  COMPONENTS=$(cat optional-projects.list)
  for c in $COMPONENTS
  do
    # Remove end of line
    c=${c//[$'\t\r\n ']}
    if [[ ${c:0:1} = "#" ]]
    then
      echo "Ignore $c"
    else
      command="curl -X GET --output $DOWNLOAD_DIR/$c-$VERSION.zip --header 'Accept:application/zip' --header 'Content-Type:application/json' $DOWNLOADER_URL/api/artifacts/com.adaptris/$c/$VERSION --fail --silent --show-error"
      echo "Download $DOWNLOADER_URL/api/artifacts/com.adaptris/$c/$VERSION"
      ${command}
    fi
	echo -----
  done
}

######### Run #########

download
