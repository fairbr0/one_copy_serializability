from subprocess import call
import sys

if len(sys.argv) < 2:
    print("Please enter a number of servers to run")
    sys.exit(1)

n = int(sys.argv[1])
if (n < 4 or n > 10):
    print("Please enter a value between 4 and 10")
    sys.exit(1)

startingAddress = "localhost:9000"

def runClients():

	# java ClientServer <client Server Port Number> <List of all servers that this client can connect to> &
	baseAddress = "localhost:903"
	for server in range(0, n):
	    command = "java ClientServer 900" + str(server) + " "
	    for loop in range(0, n-1):
	        if server != loop:
	            command += baseAddress+ str(loop) +','
	    if server != n-1:
	        command += baseAddress + str(n-1)
	    command += " &"
	    print(command)
		#call(command, shell=True)

runClients()
