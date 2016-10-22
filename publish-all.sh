#!/usr/bin/env bash

./publish.sh annotation "$1"
./publish.sh compiler "$1"
./publish.sh plugin "$1"