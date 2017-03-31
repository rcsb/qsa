This repository contains the code for benchmarking the performance of Java MMTF parsing. 

## How to run
The best way is to use Maven.</br>
[Where to download Maven](http://maven.apache.org/download.cgi)</br>
[How to install Maven](http://maven.apache.org/install.html)

Enter the following commands into your command-line interface (such as bash in Linux, terminal in MacOS or cmd in Windows):

```
git clone https://github.com/rcsb/mmt-java-benchmark.git
cd mmtf-java-benchmark
mvn install
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.Benchmark" -Dexec.args="."
```

After the program finished, the results can be found in ./results.csv
Please note times can be almost two times smaller after second exectution, because opening the file for the first time after restart is slower than second time. Parsing the structure in MMTF file format in BioJava is comparably fast to opening a file. The cost of opening files can be overcome using Hadoop Sequence Files, which is the recommended way for parsing the whole database. To benchmark parsing the whole database, you can use the following command:
```
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.Benchmark" -Dexec.args=". download hsf"
```
The previous command downloads data and performs benchmark. For the second time, download can be omitted:
```
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.Benchmark" -Dexec.args=". hsf"
```

