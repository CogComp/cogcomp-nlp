# MD
A candidate mention detection system for the pipeline

## Install and run

- Clone the project
- cd into the project path
- `wget http://ddns.meiguo.work/public/MD.zip`
- (Alternatively on Mac OSX: `curl -O http://ddns.meiguo.work/public/MD.zip`)
- `unzip MD.zip`
- `mvn lbjava:generate`
- `mvn clean`
- `mvn install`
- `mvn exec:java -Dexec.mainClass="org.cogcomp.md.BIOTester"`
