#! /bin/bash

echo "The number of servers you wish to start is $1"

i=0
localhosts=9001
outport=9004
inport=9500
string=",localhost:"
main="localhost:9003"

#A loop to create the string arguments for location of all other servers that the coordinator needs
while [ $i -lt $[$1 -2] ]
do
  j=$[$i+1]
  main=$main$string$outport
  i=$[$i+1]
  outport=$[$outport+1]
done

#Run the coordinator
echo java  runServer server 9001 9002 $main db0.txt &

  sleep 1

i=0
localhosts=9001
outport=9003
inport=9031

#A loop to create all the slave servers
while [ $i -lt $[$1 -1] ]
do
  j=$[$i+1]
  #Run the slave servers
  echo java runServer server $outport $inport localhost:$localhosts db$j.txt & 
  i=$[$i+1]
  inport=$[$inport+1]
  outport=$[$outport+1]
done

