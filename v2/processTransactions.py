import sys;

servers = [];

def processLine(line, lineNumber):
	print ("processing line " + str(lineNumber))
	words = line.split();
	print (words)

	try:
		fileName = "trans" + words[1].strip(';')+".txt";
		print (fileName);
		#check if this exists or not - currently it is just appending to the file
		print (words[2:]);
		addLine = " ".join(words[2:]);
		file = open(fileName, 'a');
		file.write(addLine);
		file.write("\n");
	except:
		print ("Matt made a boo boo");
		sys.exit(1);

	#check if ther server is in our array of servers or add it
	word = words[1].strip(';')
	if(word in servers):
		return;
	else :
		servers.append(word)

	file.close();


def checkLine(line, lineNumber):
	#Here we will check that the line is correct

	words = line.split();
	#print words

	if (words[0][:2] not in ["se", "//"]):
		raise ValueError("The transaction on line "+ str(lineNumber) + " is not of the correct format");
	if(words[0][:2] in ["se"]):
		processLine(line.rstrip('\n'), lineNumber);

file = open('transactionFile', 'r');
print ("Processing transaction file");
lineNumber=0;

for line in file:
	lineNumber+=1;
	if(line != '\n'):
		checkLine(line.rstrip('\n'), lineNumber)
file.close();
