#!/bin/bash

function deploy(){
  mvn deploy site-deploy
}

function setPermissions() {
  echo "We are about the set the permissions on bilbo ... "
  ssh bilbo
  cd /mounts/bilbo/disks/0/www/cogcomp/html/m2repo/edu/illinois/cs/cogcomp

  declare -a modules=("illinois-cogcomp-nlp" "illinois-core-utilities" "illinois-curator" "illinois-edison" "illinois-lemmatizer" "illinois-tokenizer")

  for i in "${modules[@]}"
  do
     echo "Setting permissions for $i"
     chmod -R 775 "$i"/*
     chgrp -R cs_danr "$i"/*
  done
}

deploy

setPermissions