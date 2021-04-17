#!/bin/bash

java -Djava.rmi.server.codebase="http://192.168.254.133/~agus/RMIchat/ http://192.168.254.158/~agus/RMIchat/" -Djava.security.policy=java.policy Client 192.168.254.133 20

echo "Usuario desconectado."
