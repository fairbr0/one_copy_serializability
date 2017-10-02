#! /bin/bash

#The runner for our databases simulation

numberOfServers=$1
rValue=$2
wValue=$3

echo "The number of servers that you want to run is: " $numberOfServers
echo "The R value is: " $rValue
echo "The W value is: " $wValue

./removeTextFiles.sh
python processTransactions.py
python runServers.py $numberOfServers $rValue $wValue
echo "Servers started"
