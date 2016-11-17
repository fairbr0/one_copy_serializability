#! /bin/bash

#The runner for our databases simulation

numberOfServers=$1

echo "The number of servers that you want to run is: " $numberOfServers

./removeTextFiles.sh
python processTransactions.py
python runServers.py $numberOfServers
echo "Servers started"
