#!/bin/bash
BIN_DIR=../MatchSnapshots/bin
SNAP_DIR=../snapshots
SNAP_SUBDIR=Azureus/leak-1-even
OLD1=980024434f3551df9dbfc75457589dc493a66e89
OLD2=980024434f3551df9dbfc75457589dc493a66e89-2
NEW=cc61212e65f7a3e7252c4fce36c064a8b4d90965
java -Xmx4096m -cp $BIN_DIR edu.umd.MatchSnapshots.Main \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD1 \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD2 \
    $SNAP_DIR/$SNAP_SUBDIR/$NEW \
    org.gudy.azureus2.core3.peer.impl.control.PEPeerControlImpl \
    _server.adapter
