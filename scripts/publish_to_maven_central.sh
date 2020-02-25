#!/usr/bin/env bash

if [ "${CIRCLE_BRANCH}" == "master" ]; then
  echo "$MAVEN_CENTRAL_SEC_RING" | base64 -d >"$HOME"/secring.gpg
  gpg --import --batch "$HOME"/secring.gpg
  gradle publish -Psonatype.username="$SONATYPE_USERNAME" -Psonatype.password="$SONATYPE_PASSWORD" -Psigning.keyId=0E7A8B89 -Psigning.password="$MAVEN_CENTRAL_KEY_PASSPHRASE" -Psigning.secretKeyRingFile="$HOME"/secring.gpg -Porg.gradle.parallel=false
fi
