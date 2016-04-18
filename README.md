# CS6200---Information-Retrieval---Final-Project

The python code and files should go to /python

The Java code and files should go to /java


## Query Expansion
This part of the project is implemented using Python 2.7.

### External Libraries Required
1. [unirest](http://unirest.io/): The HTTP request library;
2. [Natural Language Toolkit](http://www.nltk.org/): The NLP library;

### Query Expansion Techniques
We use two query expansion techniques:
1. Synonym
2. Derivants
[Words API](https://www.wordsapi.com/) is used to retrieve information about synonyms and derivants of English words. It has 2500 free hits each day, which is enough for our project.

### Implementations
#### Preparation
First, we tokenize the original 64 queries using [parseQueries.py](/python/parseQueries.py). Second, we exclude common words ([CommonWords.py](/python/CommonWords.py)). Although not using stopping at this part, we still exclude common words when applying the queries expansion. Then we use [Words API](https://www.wordsapi.com/) to retreive ([getWordInfo.py](/python/getWordInfo.py))synonyms and derivants of each term that apper in tokenized queries. After that, we got two files [derivative](/python/derivative.txt) and [similar](/python/similar.txt). The first word of each row of these files comes from original queries, and remaining ones are expanded query terms. Finally, use [queryExpansion.py](/python/queryExpansion.py) to appy the query expansion, and got three files: