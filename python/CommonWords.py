commonWordsFileName = "../common_words"

def commonWords():
	f = open(commonWordsFileName, 'r')
	commonWords = set()
	for line in f:
		commonWords.add(line[:-1]) # Remove last \n
	f.close()
	return commonWords

def isCommonWord(word):
	word = word.lower()
	commonWordsSet = commonWords()
	return word in commonWordsSet