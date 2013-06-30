#!/bin/bash
BIN_DIR=../MatchSnapshots/bin
SNAP_DIR=../snapshots
SNAP_SUBDIR=javaEmailServer-1.3.3-1.3.4
OLD1=old1
OLD2=old2
NEW=new
java -Xmx2048m -cp $BIN_DIR edu.umd.MatchSnapshots.Main \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD1 \
    $SNAP_DIR/$SNAP_SUBDIR/$OLD2 \
    $SNAP_DIR/$SNAP_SUBDIR/$NEW \
    com.ericdaugherty.mail.server.configuration.ConfigurationManager \
    deliveryAttemptThreshold