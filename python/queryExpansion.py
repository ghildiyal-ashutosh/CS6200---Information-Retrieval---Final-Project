import parseQueries
import CommonWords


similarFile = "similar.txt"
derivativeFile = "derivative.txt"

sf = open(similarFile, 'r')
df = open(derivativeFile, 'r')

dictSimilar = {}
dictDerivative = {}
for word in sf:
	similar = word.split()
	if(len(similar) > 1):
		dictSimilar[similar[0]] = similar[1:]

for word in df:
	derivative = word.split()
	if(len(derivative) > 1):
		dictDerivative[derivative[0]] = derivative[1:]

sf.close()
df.close()

# print dictSimilar['code']
# print dictDerivative['code']

# sOutputFile = "../queries/expandedQueriesTokensUsingSynonym.txt"
# dOutputFile = "../queries/expandedQueriesTokensUsingDerivants.txt"
sOutputFile = "../queries/expandedQueriesTokensUsingSynonymOriginalIncluded.txt"
dOutputFile = "../queries/expandedQueriesTokensUsingDerivantsOriginalIncluded.txt"
oOutputFile = "../queries/originalQueriesTokens.txt"
pOutputFile = "../queries/stoppedQueriesTokens.txt"
# dpOutputFile = "../queries/stoppedExpandedQueriesTokensUsingDerivants.txt"
dpOutputFile = "../queries/stoppedExpandedQueriesTokensUsingDerivantsOriginalIncluded.txt"


sfo = open(sOutputFile, 'w')
dfo = open(dOutputFile, 'w')
ofo = open(oOutputFile, 'w')
pfo = open(pOutputFile, 'w')
dpfo = open(dpOutputFile, 'w')

queries = parseQueries.getTokenizedQueries()
for q in queries:
	sfo.write(str(q + 1) + " ")
	dfo.write(str(q + 1) + " ")
	ofo.write(str(q + 1) + " ")
	pfo.write(str(q + 1) + " ")
	dpfo.write(str(q + 1) + " ")
	for term in queries[q]:
		ofo.write(term.lower())
		ofo.write(" ")
		sfo.write(term.lower())
		sfo.write(" ")
		dfo.write(term.lower())
		dfo.write(" ")
		if not CommonWords.isCommonWord(term):
			pfo.write(term.lower())
			pfo.write(" ")
			dpfo.write(term.lower())
			dpfo.write(" ")
		if term in dictSimilar:
			for sTerm in dictSimilar[term]:
				sfo.write(sTerm)
				sfo.write(" ")
		if term in dictDerivative:
			for dTerm in dictDerivative[term]:
				dfo.write(dTerm)
				dfo.write(" ")
				if not CommonWords.isCommonWord(term):
					dpfo.write(dTerm)
					dpfo.write(" ")
	sfo.write('\n')
	dfo.write('\n')
	ofo.write('\n')
	pfo.write('\n')
	dpfo.write('\n')
sfo.close()
dfo.close()
ofo.close()
pfo.close()
dpfo.close()