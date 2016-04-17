# import CommonWords
# import parseQueries

# print CommonWords.isCommonWord('ugly')
# print CommonWords.isCommonWord('is')

# q = parseQueries.getTokenizedQueries()
# print q[0]
# print q[63]

# import unirest

# response = unirest.get("https://wordsapiv1.p.mashape.com/words/example",
#   headers={
#     "X-Mashape-Key": "tju4Z6vvqBmshqOvvJ0uVyjYNIhXp10qdAJjsnms5t3DtGHpPb",
#     "Accept": "application/json"
#   }
# )


# print response.raw_body

# print type(response)

from nltk.corpus import words

import nltk

print "23" in words.words()
