NIST Data Processor
============

wstumbo@frontiernet.net

Overview
----------------

A Java utility to copy vulnerability data from NIST National 
Vulnerability Database feeds process them producing a CSV file consisting
of the CVE ID and the results of performing Name Entity Recognition on
the Description.

Usage:
----------------

### Prerequisites:

This package depends on *MySql* being installed and an account created for 
the NIST Data Processor.

### Building:

First the ```flywayConfig.properities``` file needs to be updated.

Update the following two lines with the user name and password selected above:
```text
flyway.user=name
flyway.password=password
```
replacing name and password with the appropriate account information.

Next, update the ```application.properties``` file updating the following two
lines:
```text
spring.datasource.username=name
spring.datasource.password=password
```
using the same username password combination as above.

Build the jar file:

```sh
mvn clean package
```

If this is the initial time the package has been built, then the database
will also need to be setup.  This is done using ```flyway```, a tool for 
configuring and managing databases.  Using maven, run the following command:
```shell script
mvn clean flyway:migrate -Dflyway.configFiles=flywayConfig.properties
```
Flyway will set up the appropriate schemas and tables.  

### Running:

```sh
java -jar target/nerProcessor-1.0-SNAPSHOT.jar <csv file location>
```

Flow of Processing
----------------

The program runs by first downloading the latest NIST data feeds.  Files
retrieved are stored in the ```data``` subdirectory.  Each new execution
of the program, checks to see if any of the data feed files have been 
updated and only downloads new versions of files.
 
Once the data feeds have been downloaded, each JSON file is processed.  
File processing consists of parsing the JSON in to a list of POJOs that
represent the JSON data.  Each JSON Node is processed extracting the
CVE-ID, update date and the Description.  

The database is queried to determine if the CVE-ID has been processed
before, and if so, whether this represents an update, defined by the
update date from the file being newer than the version stored in the
database.  

Only new or updated values are written to the database.  Duplicate entries
are ignored.  Writing to the database consists of updating two tables, 
one holding the CVE-ID and another 
consisting of the description text and a slot for the Annotated Description.

Processing continues through all the data load files.  

*NOTE:*  The initial running will need to process every entry and can take
several hours.  Once the data loads have all been processed, future runs 
will only add new or updated entries and run much quicker.

Once the data load phase has completed, processing moves on to Name Entity
Recognition.  This starts by finding every entry in the description table
that lacks a Name Entity Recognition value.  For each of these entries
the description is passed to the Stanford NLP NER pipeline for Entity 
Recognition.  The resulting string is written into the NER field for
that entity.

Finally, once Name Entity Recognition has completed the CSV file can be 
generated.  The file is created by querying the database for every
CVE-ID entry and it's corresponding NER entry.  The CVE-ID tags are
sorted in ascending alphabetical order and each entry is written to a 
file formatted as a CSV.

Once writing the table has completed processing ends.

Output:
----------------

While executing the program writes minimal status information to the terminal
(System.out).  This rudimentary information provides feedback on the state
of processing.  The program provides more extensive information in a logfile,
```log/nerProcessor.log```, The log file captures startup information from
 the NIST parser and detailed information on processing of individual
 CVE descriptions.  
 
 The other piece of output is the CSV file.  The file will be located in the
 directory specified on the command line.
 
 Components:
 ----------------
 
 - NIST data loads are managed by a package developed by Steve
 Springett.  His package downloads and uncompresses the JSON data feeds.  
 In addition, each time it is run, it checks for updates and will 
 update the data feeds as needed.  
 - JSON POJO creation was done using an IntelliJ plugin to help with the
 POJO generation.  It didn't handle duplicate type names within the
 JSON.  Fixing this required a little hand editing.  A large part of
 the JSON isn't used and could be ignored.  However, there are likely
 additional fields that could be of interested - modified date is one.
 - JSON processing is done using the Jackson libraries.
 - CSV writing is done using a couple simple static methods.  One to add the
 headers and a second to write a line of data.  Prior to writing the annotated
 description information a string replace operation is executed to ensure any
 double quote characters in the string are correctly escaped.
 
 Future Enhancements:
----------------
 
 There remain several opportunities to improve this code.  Whether any of
 them are worth pursuing depends on how this module fits into the 
 overall processing architecture.
 
 If processing the NIST National Vulnerability Database files is a one-off
 operation, there is little value in doing anything additional with the code
 base.
 
 1. JSON parsing as a stream or iterator.  Processing the JSON as a stream 
 and only bringing into memory one JSON CSE node at a time would reduce the 
 memory footprint.  The current processing builds a data structure holding
 all the CSE elements in a file -- the CVECollection object.  I used the
 Jackson JSON libraries and while they have iterators, it appeared they 
 worked by processing the complete JSON file.  More investigation into
 stream based JSON processing could be useful.  By processing the JSON in
 this manner, overall processing could represent a pipeline - read one
 JSON node, extract the needed fields, perform NER on the description, 
 write the result to the CSV file.
 
 2. Duplicate Descriptions. ~~There are a number of identical descriptions
 in the data.  Instead of performing Entity Recognition on the same
 description multiple times, we could be smarter and find duplicate entries
 and only do NER once per string.  An easy approach would be to create a key
 for each description - a hashcode based on the string contents, and use the
 hashcode as the key into a description table.  Duplicate hashcodes would
 lead to identical strings.  Once a recognition is performed on a string, 
 future occurrences of the string could leverage it.  The downside of this
 approach would be either a large memory footprint, holding all the hashcodes
 and descriptions in memory.~~   Using a database allows easy detection of
 duplicate descriptions.  A hash code is generated for each description and
 used as the key to map from a Vulnerability identifier to its associated
 description.  If a description already exists with the hashcode, it is a
 duplicate, we use that entry and do not create a second entry.  At the 
 time of this writing there are 138208 vulnerabilities in the database and
 128423 unique descriptions, or approximately 10000 duplicate entries.
 
 3. The rate of change of existing vulnerabilities appears to be minimal.
 Using the ```*.modified.json``` updates could easily be made to existing 
 records without reprocessing the complete collection of JSON files.  
 Using this approach, writing each CVE-ID, it's Description, the Annotated
 Description along with the modified date (found in the JSON node), a
 database record could be constructed.  The database would be treated
 as the record of truth for NER processing.  Modified CVE descriptions
 could be updated and a new CSV file written.
