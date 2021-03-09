# Distributed Systems

## Requirements
* apache-maven 3.6.3 (needed it for MongoDB conenction) ``` https://maven.apache.org/download.cgi ```
* Java SE 1.8 at Java BuildPath
* On project Maven Dependencies  (Java Build Path) should be: 
  ```
  bson-4.2.2.jar
  mongodb-driver-sync-4.2.2.jar
  mongodb-driver-core-4.2.2.jar
  ```
## info:
* Databases are on MongoDBAtlas. URI is included in Connection class. 
  (Deberiamos hacer una cuenta de MongoDB para las dos, porque supongo que puedes conectar a las mias pero no meterte a ver los datos. Not sure.)
* Java Build Path: right click on project > properties > Java Build Path. Should look like this:



<img src="https://github.com/rocioruizruiz/DistributedSystems/blob/main/img/JavaBuildPath.png" alt="App Screenshot" style="zoom: 70%" />

* ServerConnection run configurations: right click on ServerConnection class > Run As > Run Configurations. Shpuld look like this:


<img src="https://github.com/rocioruizruiz/DistributedSystems/blob/main/img/ServerConnectionRunConfiguration.png" alt="App Screenshot" style="zoom: 70%" />
