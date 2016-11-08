from subprocess import call
import sys

n = int(sys.argv[1])
address = "localhost:903"
for i in range(0, n):
    s = "java Quorum server 903" + str(i) + " 900" + str(i) + " ";
    for j in range(0, n-1):
        if i != j:
            s += address+ str(j) +','
    if i != n-1:
        s += address + str(n-1)
    s += " " + str(i) + " &";
    #print(s)
    call(s, shell=True)
