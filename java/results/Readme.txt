Run code: 
	1. add jsoup.jar into the project as external library;
	2. add three .java files: Executer.java, FileParser.java, Summarizer.java into working directory;
	3. add all the html files into files/... directory under project directory;
	4. run Executer.java;

Design Choice:
	1. when doing tokenizing, at first I remove the reference tags like [123...], then I remove special characters except 0-9, a-z, ',', '.', '%' and \s (as space or other kind of spaces like tab), at last I remove the ',', '.' and '%' that are not used with digits;
	2. when splitting files into tokens, I split it by \s;
	3. When summarizing, I used hashtable to store the word, and it's entry of inverted list, and I also created a new class for the entry named "Pointers". Pointers class contains total term frequency and a list of pointers, each pointer class contains a documentId and it's related term frequency within that document. At last, I used a priority_Queue to store the words and their pointers, to print them in order;
	4. when drawing curve, I used log-log scale on X-Y axis, and it's worth mentioned that for trigram, because there are too many data, the excel itself cannot accomdate that much, and I just uesd that maximum data excel can take to draw the plot.