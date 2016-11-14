from subprocess import call
import sys

if len(sys.argv) < 2:
    print("Please enter a number of servers to run")
    sys.exit(1)

n = int(sys.argv[1])
p = int(sys.argv[2])
if (n < 1 or n > 10):
    print("Please enter a value between 4 and 10")
    sys.exit(1)

startingAddress = "localhost:9000"

def runClients():

    #java ClientServer <client Server Port Number> <List of all servers that this client can connect to> &
	baseAddress = "localhost:900"
	for server in range(0, n):
	    command = "java ClientServer " + str(server) + " "
	    for loop in range(0, p):
			if(loop != p-1):
				command += baseAddress+ str(loop) +','
			else:
				command += baseAddress+ str(loop)

	    command += " &"
	    #print(command)
        call(command, shell=True)

runClients()
