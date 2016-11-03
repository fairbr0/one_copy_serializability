for i in {1..9}
do
	serverlist=""
	for j in {1..9}
	do
		if [[ $j -ne $i]]
		then
			serverlist+="localhost:900$i"
		fi
		if [[$j -ne 9]]
		then
			serverlist+=","
		fi
	done
	echo "java Node server 900$i 903$i {server list} db$i.txt"
done
