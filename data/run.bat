cd qsa
SET MAVEN_OPTS=-Xmx12g
mvn exec:java -Dexec.mainClass="analysis.Job" -Dexec.args="-h ./qsa"
cd ..
