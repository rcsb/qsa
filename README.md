This repository contains the code for topology-independent structure similarity search and alignment. 

## How to run
The best way is to use Maven.</br>
[Where to download Maven](http://maven.apache.org/download.cgi)</br>
[How to install Maven](http://maven.apache.org/install.html)

Enter the following commands into your command-line interface (such as bash in Linux, terminal in MacOS or cmd in Windows):

```
git clone https://github.com/rcsb/qsa.git
cd qsa
mvn install
mvn exec:java -Dexec.mainClass="analysis.Job" -Dexec.args="-h data"
```
