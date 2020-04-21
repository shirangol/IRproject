
# Better than Google Search Engine

This GUI represents a Search Engine, built out of a given corpus and a list of stop-word
The output will be a dictionary, composed of an inverted index.

## Getting Startted

1. Generate a new Dictionary:
	For generating a new dictionary, please insert a path to the first text field or browse a directory, with a directory namd "corpus" and a file named "stop_wards" in it. 
	Then, insert the path you wish to save the output files in. 
	Then, click on "start" button- and WAIT. YEAH, WAIT.
	---Keep Waiting--- until a massage will appear on the screen! It might take some time, please be patient.

2. Display Dictionary:
	After generation is resumed, you can click on "Display Dictionary" to view the dictionary itself.

3. Restart Button:
	You may click on "restart" to delete the dictionary from the RAM, and all related files.

PAY ATTENTION:
	The user can choose weather to use stemming or not, by selecting or unselecting the "stemming" check-box.

4. Load Dictionary
	For loading an already built dictionary, insert the path of dictionary's files and click "Load Dictionary". 
	NOTE- in this stage, you must declare if you want to upload a stemmed or unstemmed dictionary. If you will choose a dictionary type that wasn't generated before (means the related files are not in the directory),
	you won't be able to display or load it!

5. Run a single query:
	After loading a dictionary, you may now search anything you like. type a query in the query's textField, and click RUN.

6. Run a queries file:
	You can browse a txt file with queries in it, in the given format.
	Now, click RUN and hold on a little bit. 

7. DocID viewer:
	When query's results will appear on the screen, yow can choose to view a query result's list of docIDs. 
	
8. Entity Viewer:
	When the list of docIDs for a single result will appear on the screen, you can choose "View Entity" button to see the most dominant entities in that document.

9. Saving Qury's results into a file:
	For saving the results into a file, browse first a directory to save it into.

10. Use DataMuse API:
	Select the relevant checkbox.

11. Use Word2Vec semantic model:	
	Select the relevant checkbox.

## Built With
* opennlp porter's stemmer jar
* Jsoup jar
* medallia-word2vec-0.10.2-jar-with-dependencies jar
* word2vec.c.output.model.txt 
* datamuse.com/api

##Authors
Shiran Golzar
Amit Shakarchy