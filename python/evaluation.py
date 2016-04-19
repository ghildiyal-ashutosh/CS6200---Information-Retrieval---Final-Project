import glob
import relevance
# DirName = "BM25_Expanded_Derivants"
# DirName = "BM25_Expanded_Synonym"
# DirName = "BM25_Origin"
# DirName = "Lucene"
# DirName = "Stop"
# DirName = "StopExtend"
DirNameList = []
DirNameList.append("BM25_Expanded_Derivants")
DirNameList.append("BM25_Expanded_Synonym")
DirNameList.append("BM25_Origin")
DirNameList.append("Lucene")
DirNameList.append("Stop")
DirNameList.append("StopExtend")
DirNameList.append("TFIDF_Origin")
for DirName in DirNameList:
	MAP = 0.0
	MRR = 0.0
	MK5 = 0.0
	MK20 = 0.0
	outFileName = "../evaluation/" + DirName + "_Evaluation.txt"
	of = open(outFileName, 'w')
	of.write("query rank precision recall\n")
	for fileName in glob.glob("../results/" + DirName + "/*.txt"):
		preLength = 17 + len(DirName)
		if DirName == "Lucene":
			# print fileName
			queryNum = fileName[preLength : -11]
		else:
			queryNum = fileName[preLength : -4]
		# print queryNum

		searchRes = open(fileName, 'r')

		averagePrecision = 0.0
		reciprocalPrecision = 0.0
		precisionAt5 = 0.0
		precisionAt20 = 0.0
		relevanceRetrieved = 0.0
		recall = 0.0

		totalRelevant = float(relevance.numRelevant(queryNum))

		for line in searchRes:
			lineList = line.split()
			# queryNumber = lineList[0]
			doc = lineList[2]
			rank = float(lineList[3])
			isRelevant = relevance.isRelevant(queryNum, doc)
			# print queryNumber + " " + doc + " " + str(rank)

			if isRelevant:
				relevanceRetrieved += 1
				averagePrecision += relevanceRetrieved / rank
				if reciprocalPrecision == 0.0:
					reciprocalPrecision = 1 / rank
				if totalRelevant > 0.0:
					recall = relevanceRetrieved / totalRelevant
				else:
					recall = 0
			if rank == 5.0:
				precisionAt5 = relevanceRetrieved / rank
			if rank == 20.0:
				precisionAt20 = relevanceRetrieved / rank

			of.write(str(queryNum) + " ")
			of.write(str(int(rank)) + " ")
			of.write(str(relevanceRetrieved / rank) + " ")
			of.write(str(recall) + "\n")


		if relevanceRetrieved == 0.0:
			averagePrecision = 0.0
		else:
			averagePrecision /= relevanceRetrieved

		MAP += averagePrecision
		MRR += reciprocalPrecision
		MK5 += precisionAt5
		MK20 += precisionAt20

		
		# print queryNum + ":"
		# print "    AP = " + str(averagePrecision)
		# print "    RP = " + str(reciprocalPrecision)
		# print "    P5 = " + str(precisionAt5)
		# print "    P20= " + str(precisionAt20)
		searchRes.close()

	totalQueryNum = 64
	MAP /= totalQueryNum
	MRR /= totalQueryNum
	MK5 /= totalQueryNum
	MK20 /= totalQueryNum

	of.write('\n') 
	of.write( "MAP  " + str(MAP) + '\n')
	of.write( "MRR  " + str(MRR) + '\n')
	of.write( "MK5  " + str(MK5) + '\n')
	of.write( "MK20 " + str(MK20) + '\n')
	of.close()

	print DirName
	print "  MAP = " + str(MAP)
	print "  MRR = " + str(MRR)
	print "  MK5 = " + str(MK5)
	print "  MK20= " + str(MK20)