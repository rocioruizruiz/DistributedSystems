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
* Java Build Path: right click on project > properties > Java Build Path. Should look like this:



<img src="https://github.com/rocioruizruiz/DistributedSystems/blob/main/Practica1/img/JavaBuildPath.png" alt="App Screenshot" style="zoom: 70%" />

* Server run configurations: right click on Server class > Run As > Run Configurations -> With Server selected on Java Application and on Arguments tab -> write on VM arguments text box:
```-Dmongodb.uri="mongodb+srv://user1:user1password@cluster0.t4m8y.mongodb.net/ADMIN_DB?retryWrites=true&w=majority"```
Should look like this:



<img src="https://github.com/rocioruizruiz/DistributedSystems/blob/main/Practica1/img/ServerRunConfiguration.png" alt="App Screenshot" style="zoom: 70%" />

## run:
* RUN SERVER: when asked about ports, insert: 3339
* RUN SERVER: when asked about ports, insert: 3340
* RUN SERVER: when asked about ports, insert: 3341
* RUN SERVER: when asked about ports, insert: 3367
* RUN PROXY (which is already allocated on port 3338, and clients know that info)
* RUN CLIENT: functionalities you can try: Desencriptar mensaje/Ranking de servidores.
