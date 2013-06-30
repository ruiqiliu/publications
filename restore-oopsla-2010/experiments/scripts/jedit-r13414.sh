#!/bin/bash
BIN_DIR=../MatchSnapshots/bin
SNAP_DIR=../snapshots
SNAP_SUBDIR=jEdit/all-snapshots
OLD1=r13413/run-1
OLD2=r13413/run-2
NEW=r13414/run-1
java -Xmx2048m -cp $BIN_DIR edu.umd.MatchSnapshots.Main \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD1 \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD2 \
    $SNAP_DIR/$SNAP_SUBDIR/$NEW \
    org.gjt.sp.jedit.gui.HistoryText \
    popup