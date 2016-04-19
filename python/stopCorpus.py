import glob
import CommonWords

for fileName in glob.glob("../corpus/*.html"):
	fName = "../stoppedCorpus/" + fileName[10:]
	f = open(fileName, 'r')
	fw = open(fName, 'w')
	fileContent = f.read().split()
	for word in fileContent:
		if not CommonWords.isCommonWord(word):
			fw.write(word)
			fw.write(" ")
		else:
			print "Word " + word + " removed."
	f.close()
	fw.close()

