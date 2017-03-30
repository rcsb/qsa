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

After the program finished, the results can be found in ./
