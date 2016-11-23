from subprocess import call
import sys

if len(sys.argv) < 2:
    print("Please enter a number of servers to run")
    sys.exit(1)

n = int(sys.argv[1])
if (n < 4 or n > 10):
    print("Please enter a value between 4 and 10")
    sys.exit(1)

address = "localhost:903"
for i in range(0, n):
    s = "java Site " + str(i) + " 2" + " 3 " + str(n) + " "
    for j in range(0, n-1):
        if i != j:
            s += address+ str(j) +','
    if i != n-1:
        s += address + str(n-1)
    s += " " + str(i) + " &"
    print(s)
    call(s, shell=True)

'''server number,
r
w
list : other servers
'''
