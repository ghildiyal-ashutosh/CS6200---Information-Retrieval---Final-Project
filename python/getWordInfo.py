# -*- coding: utf-8 -*-
import json
import unirest
import parseQueries
import CommonWords

from nltk.corpus import words

def is_json(myjson):
    try:
        json_object = json.loads(myjson)
    except ValueError, e:
        return False
    return True

queriesTermsFileName = "queriesTerms.txt"
# qt = open(queriesTermsFileName, 'w')
# queries = parseQueries.getTokenizedQueries()
# queriesWords = set()
# for q in queries:
# 	for term in queries[q]:
# 		term = term.lower()
# 		if not CommonWords.isCommonWord(term) and (term in words.words()):
# 			queriesWords.add(term)
# for term in queriesWords:
# 	qt.write(term + " ")
# qt.close()

qt = open(queriesTermsFileName, 'r')
for string in qt:
	terms = string.split()
qt.close()



similarFileName = "similar.txt"
derivativeFileName = "derivative.txt"

sf = open(similarFileName, 'w')
df = open(derivativeFileName, 'w')


for term in terms:
	print term
	response = unirest.get("https://wordsapiv1.p.mashape.com/words/" + term,
	  headers={
	    "X-Mashape-Key": "tju4Z6vvqBmshqOvvJ0uVyjYNIhXp10qdAJjsnms5t3DtGHpPb",
	    "Accept": "application/json"
	  }
	)

	if not is_json(response.raw_body):
		continue

	jsonObj = json.loads(response.raw_body)

	similar = set()
	derivative = set()
	attRes = u'results'
	if attRes in jsonObj:
		for res in jsonObj["results"]:
			attAlso = u'also'
			attSimilar = u'similarTo'
			attSynonyms = u'synonyms'
			attDerivative = u'derivation'
			if attAlso in res:
				for word in res[attAlso]:
					if ' ' not in word:
						similar.add(word)
			if attSimilar in res:
				for word in res[attSimilar]:
					if ' ' not in word:
						similar.add(word)
			if attSynonyms in res:
				for word in res[attSynonyms]:
					if ' ' not in word:
						similar.add(word)
			if attDerivative in res:
				for word in res[attDerivative]:
					if ' ' not in word:
						derivative.add(word)

	sf.write(term)
	for word in similar:
		sf.write(" ")
		sf.write(word)
	sf.write('\n')
	df.write(term)
	for word in derivative:
		df.write(" ")
		df.write(word)
	df.write('\n')

sf.close()
df.close()
