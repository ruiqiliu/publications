#!/bin/bash
BIN_DIR=../MatchSnapshots/bin
SNAP_DIR=../snapshots
SNAP_SUBDIR=crossftp
OLD1=1.05-1
OLD2=1.05-2
NEW=1.06-1
java -Xmx2048m -cp $BIN_DIR edu.umd.MatchSnapshots.Main \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD1 \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD2 \
    $SNAP_DIR/$SNAP_SUBDIR/$NEW \
    org.apache.ftpserver.gui.ServerFrame \
    VERSION -static