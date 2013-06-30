#!/usr/bin/env python2.5

import os
import re
import sys

RE = re.compile(r'\\newlabel{([\w:-]+)}')

try:
    auxfile = [ i for i in os.listdir(".") if i.endswith(".aux") ][0]
except IndexError:
    print "No .aux file found"
    sys.exit(1)

output = open("tags", 'w')
output.write('!_TAG_FILE_SORTED\t0\t\n')
for line in open(auxfile, 'r'):
    if line.startswith(r'\newlabel{'):
        match = RE.match(line)
        if match is not None:
            tag = match.group(1)
            if not tag.startswith('acro:'):
                output.write('%s\t/dev/null\t\n' % match.group(1))

