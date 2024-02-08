> [!NOTE]
> Note this repository has been archived because it is not actively maintained anymore.

This repository contains the code for topology-independent structure similarity search and alignment. 

## How to run
The best way is to use Maven.</br>
[Where to download Maven](http://maven.apache.org/download.cgi)</br>
[How to install Maven](http://maven.apache.org/install.html)

Update code and compile:
```
git clone https://github.com/rcsb/qsa.git
cd qsa
mvn install
```

Specify memory (Linux, MacOS):
```
MAVEN_OPTS=-Xmx12g
```

Specify memory (Windows):
```
SET MAVEN_OPTS=-Xmx12g
```

Run:
```
mvn exec:java -Dexec.mainClass="analysis.Job" -Dexec.args="-h data"
```

Parameters are in the file data/parameters.txt. Results will be in data/jobs. Alignment scores are summarized in the file table.csv. The file alignments.py visualizes alignments in PyMOL (after association with the program, this can be done by double clicking the file). 
