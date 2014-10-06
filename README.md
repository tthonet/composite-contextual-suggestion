Composite Contextual Suggestion
===============================

Foursquare subscription
-----------------------
The venues used for the suggestions are extracted from Foursquare. You need therefore a Foursquare account and a developer status that will grant you access to the Foursquare API and thus allow you to get the JSON files of the Foursquare venues. The developer status is obtained by creating an app, that obviously is not required to be actually implemented. Only a name for the app and a website (that can be any random web page's URL) have to be specified to allow the creation of the app.

1. Sign up on Foursquare (https://foursquare.com).
2. Proceed to https://developer.foursquare.com/.
3. Click on **GET STARTED**.
4. Click on the hyperlink **create an app**.
5. Click on **CREATE A NEW APP**.
6. Specify a name for the app and a website URL.
7. Click on **SAVE CHANGES**.

You will be given a client id and a client secret for this app. Those keys will be used to fetch the Foursquare venues in the next step.

Venue fetching
--------------
This step consists in generating the JSON files of the Foursquare venues that will be used for the suggestions. Since Foursquare has a restriction on the number of JSON files that can be downloaded, we choose to download only the venues of one context (the city of Dubuque, Iowa -- context id 115) to illustrate the program.

1. Download the GitHub repository compressed in the archive "composite-contextual-suggestion-master.zip" (https://github.com/tthonet/composite-contextual-suggestion/archive/master.zip).
2. Extract "composite-contextual-suggestion-master.zip".
3. Go to the directory "jars".
4. Execute the following command line: `java -jar foursquare-venue-fetcher.jar -clid <your-foursquare-client-id> -clsecret <your-foursquare-client-secret> -venidfile "../data/ids/Dubuque.ids.filtered" -ouput "../data/venues"`. The program downloads the JSON files of about 1000 Foursquare venues that are located in Dubuque. The execution may take several minutes depending on the speed of your internet connection. If the exception 500 is raised throughout the execution, it may be due to communication issues with the Foursquare servers. Execute again the command line to resume the venue fetching.
5. Execute the following command line: `java -jar foursquare-venue-fetcher.jar -clid <your-foursquare-client-id> -clsecret <your-foursquare-client-secret> -venidfile "../data/example_venue_foursquare_ids" -ouput "../data/venues"`. The program downloads the JSON files of the 100 Foursquare venues that match the venues of the example, that have been evaluated by users. The execution may take several minutes depending on the speed of your internet connection. If the exception 500 is raised throughout the execution, it may be due to communication issues with the Foursquare servers. Execute again the command line to resume the venue fetching.

After this step, a directory named "venues" is created inside the directory "data". It contains the JSON files of the Foursquare venues located in the context Dubuque, Iowa, as well as the Foursquare venues matching the venues of the example.

Bundle creation
---------------
In this step, the bundles of Foursquare venues are created. To reduce the execution time, we choose to make the suggested bundles only for one user (profile id 701).

1. Go to the directory "jars".
2. Execute the following command line:
`java -Xmx512m -jar composite-contextual-suggester.jar -venues "../data/venues" -categories "../data/categories.json" -profiles "../data/profiles2014-100.csv" -extfs "../data/example_venue2foursquare" -contexts "../data/contexts2014.csv" -venpcity "../data/ids" -output "../data/run-CS2014.txt" -nbunret 10 -nvenpbun 5 -contextids 115 -profileids 701`.

After the execution, a file named "run-CS2014.txt" is generated in the directory "data". The structure of each line of the file is the following:

`<user id>_<context id> <bundle rank>.<venue rank in bundle> <Foursquare id> <bundle score> <Foursquare id of similar venue 1>#...#<Foursquare id of similar venue N>`

The similar venues are extracted from the example venues and are topically similar to the venue of the line. They are used to indicate in the description that a venue is suggested according to some similar venues a user has liked. Here is an example of a line extracted from "run-CS2014.txt":

`701_115 5.3 4bedf312767dc9b683c5d3e9 0.5552144317093463 4bdb5d143904a59354074a9e#4c007d829cf52d7f0eec13e7#4b47d640f964a520ea4026e3#4a97cd54f964a520c32920e3`