import parseQueries

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

sOutputFile = "queryExpansion/expandedQueriesTokensUsingSynonym.txt"
dOutputFile = "queryExpansion/expandedQueriesTokensUsingDerivants.txt"
oOutputFile = "queryExpansion/originalQueriesTokens.txt"

sfo = open(sOutputFile, 'w')
dfo = open(dOutputFile, 'w')
ofo = open(oOutputFile, 'w')
queries = parseQueries.getTokenizedQueries()
for q in queries:
	sfo.write(str(q + 1) + " ")
	dfo.write(str(q + 1) + " ")
	ofo.write(str(q + 1) + " ")
	for term in queries[q]:
		ofo.write(term.lower())
		ofo.write(" ")
		if term in dictSimilar:
			for sTerm in dictSimilar[term]:
				sfo.write(sTerm)
				sfo.write(" ")
		if term in dictDerivative:
			for dTerm in dictDerivative[term]:
				dfo.write(dTerm)
				dfo.write(" ")
	sfo.write('\n')
	dfo.write('\n')
	ofo.write('\n')
sfo.close()
dfo.close()