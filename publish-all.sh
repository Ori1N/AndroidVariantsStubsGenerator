#!/usr/bin/env bash

./publish.sh annotation "$1"
./publish.sh plugin "$1"

git commit -am "issued version $1"
git tag "$1"
git push --tags