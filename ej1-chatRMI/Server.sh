#!/bin/bash

java \
    -Djava.rmi.server.useCodebaseOnly=false \
    -Djava.rmi.server.codebase="http://192.168.254.133/~agus/RMIchat/" \
    -Djava.rmi.server.hostname=192.168.254.133 \
    -Djava.security.policy=java.policy \
    Server

echo "Servidor detenido."
