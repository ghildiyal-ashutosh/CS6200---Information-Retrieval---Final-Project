from nltk.tokenize import regexp_tokenize  #sudo pip install -U nltk
import re

def getTokenizedQueries():
	queriesFileName = "../cacm.query"

	f = open(queriesFileName, 'r')
	i = 0
	queriesList = {}
	isText = False
	for lineWithEnter in f:
		line = lineWithEnter[:-1]

		if len(line) == 0:
			continue
		elif line[0] == '<' or (line[0] == ' ' and len(line) == 1):
			isText = False
			continue
		else:
			if not isText:
				isText = True
				queriesList[i] = ""
				queriesList[i] += line
				i += 1
			else:
				queriesList[i - 1] += " "
				queriesList[i - 1] += line
			# print line

	tokenizedQueriesList = {}
	for q in queriesList:
		tokenizedQueriesList[q] = regexp_tokenize(queriesList[q], pattern='[\d]+[\.\,\d]*[\d]+\%?|\[\d+\]|[\w\-]+')

	return tokenizedQueriesList