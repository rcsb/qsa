This repository contains the code for benchmarking the performance of Java MMTF parsing. 

## How to run
The best way is to use Maven.</br>
[Where to download Maven](http://maven.apache.org/download.cgi)</br>
[How to install Maven](http://maven.apache.org/install.html)

Enter the following commands into your command-line interface (such as bash in Linux, terminal in MacOS or cmd in Windows):

```
git clone https://github.com/rcsb/mmtf-java-benchmark.git
cd mmtf-java-benchmark/mmtf-java-benchmark
mvn install
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.benchmark.Benchmark" -Dexec.args="."
```

In our tests, we used the following memory settings before running mvn exec:
```
set MAVEN_OPTS=-Xmx6g
```
Linux, MacOS:
```
MAVEN_OPTS=-Xmx6g
```

After the program has finished, the results can be found in ./results.csv
Please note times can be almost two times smaller after second exectution, because opening the file for the first time after restart is slower than second time. Parsing the structure in MMTF file format in BioJava is comparably fast to opening a file. The cost of opening files can be overcome using Hadoop Sequence Files, which is the recommended way for parsing the whole database. To benchmark parsing the whole database, you can use the following command:
```
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.Benchmark" -Dexec.args=". download hsf"
```
The previous command downloads data and performs benchmark. For the second time, download can be omitted:
```
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.Benchmark" -Dexec.args=". hsf"
```

The comparison of MMTF, PDB and mmCIF on the whole PDB can be performed by the following command (again, it is possible to omit download later). Please note that hundreds of GB will be downloaded and the whole process will take many hours.
```
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.Benchmark" -Dexec.args=". full download"
```
