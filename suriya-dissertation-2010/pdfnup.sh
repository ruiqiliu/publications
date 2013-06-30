#!/usr/bin/env bash

if [ $# != 1 ]; then
    echo "Usage: $0 [PAGE SPEC]"
    exit 1
fi

pdfnup --nup 1x1 --paper letterpaper --pages $1 --outfile /tmp/out.pdf thesis.pdf
