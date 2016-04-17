relFileName = "../cacm.rel"

def isRelevant(queryNumber, fileName):
	rf = open(relFileName, 'r')
	queryNumber = str(queryNumber)
	isRelevantDict = {}
	for i in range(1, 65):
		isRelevantDict[str(i)] = set()
		# print i
	for line in rf:
		lineList = line.split()
		queryNum = lineList[0]
		fileNm = lineList[2]
		isRelevantDict[queryNum].add(fileNm)

	rf.close()

	if fileName in isRelevantDict[queryNumber]:
		return True
	else:
		return False

# print isRelevant(1, "CACM-1410") # T
# print isRelevant(1, "CACM-1411") # F
# print isRelevant(2, "CACM-2863") # T
# print isRelevant(3, "CACM-1234") # F
# print isRelevant(4, "CACM-1811") # T
# print isRelevant(5, "CACM-1111") # F
# print isRelevant(6, "CACM-2828") # T