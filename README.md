This repository contains the code for benchmarking the performance of Java MMTF parsing. 

## How to run
The best way is to use Maven.</br>
[Where to download Maven](http://maven.apache.org/download.cgi)</br>
[How to install Maven](http://maven.apache.org/install.html)

```
git clone https://github.com/rcsb/mmt-java-benchmark.git
cd mmtf-java-benchmark
mvn install
mvn exec:java -Dexec.mainClass="org.rcsb.mmtf.Benchmark" -Dexec.args="."
```
