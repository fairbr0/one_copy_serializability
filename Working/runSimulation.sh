#! /bin/bash

#The runner for our databases simulation

numberOfServers=$1
numberOfClients=$2

echo "The number of clients that you want to run is: " $numberOfServers
echo "The number of clients that you want to run is: " $numberOfClients

./removeTextFiles.sh
python processTransactions.py
python runServers.py $numberOfServers
echo "Servers started"
python runClients.py $numberOfClients
echo "Clients Started"
