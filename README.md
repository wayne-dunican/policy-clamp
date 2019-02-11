# Summary

CLAMP is a platform for designing and managing control loops. It is used to design a closed loop, configure it with specific parameters for a particular network service, then deploying and undeploying it.  Once deployed, the user can also update the loop with new parameters during runtime, as well as suspending and restarting it.

It interacts with other systems to deploy and execute the closed loop. For example, it pushes the control loop design to the SDC catalog, associating it with the VF resource.  It requests from DCAE the instantiation of microservices to manage the closed loop flow.  Further, it creates and updates multiple policies in the Policy Engine that define the closed loop flow.

The ONAP CLAMP platform abstracts the details of these systems under the concept of a control loop model.  The design of a control loop and its management is represented by a workflow in which all relevant system interactions take place.  This is essential for a self-service model of creating and managing control loops, where no low-level user interaction with other components is required.

At a higher level, CLAMP is about supporting and managing the broad operational life cycle of VNFs/VMs and ultimately ONAP components itself. It will offer the ability to design, test, deploy and update control loop automation - both closed and open. Automating these functions would represent a significant saving on operational costs compared to traditional methods.

# Developer Contact
Owner: ONAP CLAMP Dev team
Mailing List : onap-discuss@lists.onap.org
Add the following prefix to Subject on the mailing list : [CLAMP]
See here to subscribe : https://wiki.onap.org/display/DW/Mailing+Lists

# Wiki
https://wiki.onap.org/display/DW/CLAMP+Project

# Build
Jenkins Job: ${jenkins-joblink}

CLAMP UI: ${cockpit-link}

Logs: ${elk-link}

# Docker image

## Building 
You can use the following command to build the clamp docker image:
```
mvn clean install -P docker
```

## Deployment
Currently, the clamp docker image can be deployed with small configuration needs. Though, you might need to make small adjustments to the configuration. As clamp is spring based, you can use the SPRING_APPLICATION_JSON environment variable to update its parameters. 

### Databases
There are two needed datasource for Clamp. By default, both will try to connect to the localhost server using the credentials available in the example SQL files. If you need to change the default database host and/or credentials, you can do it by using the following json as SPRING_APPLICATION_JSON environment variable :
Note that all others configurations can be configured in the JSON as well, 

```json
{
    "spring.datasource.cldsdb.url": "jdbc:mysql://anotherDB.onap.org:3306/cldsdb4?autoReconnect=true&connectTimeout=10000&socketTimeout=10000&retriesAllDown=3",
    "spring.datasource.cldsdb.username": "admin",
    "spring.datasource.cldsdb.password": "password"
    
    "clamp.config.dcae.inventory.url": "http://dcaegen2.host:8080",
    "clamp.config.dcae.dispatcher.url": "http://dcaegen2.host:8080",
    "clamp.config.policy.pdpUrl1": "https://policy-pdp.host:9091/pdp/ , testpdp, alpha123",
    "clamp.config.policy.pdpUrl2": "https://policy-pdp.host:9091/pdp/ , testpdp, alpha123",
    "clamp.config.policy.papUrl": "https://policy-pap.host:8443/pap/ , testpap, alpha123",
    "clamp.config.policy.clientKey": "5CE79532B3A2CB4D132FC0C04BF916A7"
    "clamp.config.files.sdcController":"file:/opt/clamp/config/sdc-controllers-config.json"
}
```
### SDC-Controllers config

This file is a JSON that must be specified to Spring config, here is an example:

```json
{
  "sdc-connections":{
    "sdc-controller1":{
        "user": "clamp",
        "consumerGroup": "consumerGroup1",
        "consumerId": "consumerId1",
        "environmentName": "AUTO",
        "sdcAddress": "localhost:8443",
        "password": "b7acccda32b98c5bb7acccda32b98c5b05D511BD6D93626E90D18E9D24D9B78CD34C7EE8012F0A189A28763E82271E50A5D4EC10C7D93E06E0A2D27CAE66B981",
        "pollingInterval":30,
        "pollingTimeout":30,
        "activateServerTLSAuth":"false",
        "keyStorePassword":"",
        "keyStorePath":"",
        "messageBusAddresses":["dmaaphost.com"]
    },
    "sdc-controller2":{
        "user": "clamp",
        "consumerGroup": "consumerGroup1",
        "consumerId": "consumerId1",
        "environmentName": "AUTO",
        "sdcAddress": "localhost:8443",
        "password": "b7acccda32b98c5bb7acccda32b98c5b05D511BD6D93626E90D18E9D24D9B78CD34C7EE8012F0A189A28763E82271E50A5D4EC10C7D93E06E0A2D27CAE66B981",
        "pollingInterval":30,
        "pollingTimeout":30,
        "activateServerTLSAuth":"false",
        "keyStorePassword":"",
        "keyStorePath":"",
        "messageBusAddresses":["dmaaphost.com"]
    }
  }
}
```
Multiple controllers can be configured so that Clamp is able to receive the notifications from different SDC servers.
Each Clamp existing in a cluster should have different consumerGroup and consumerId so that they can each consume the SDC notification.
The environmentName is normally the Dmaap Topic used by SDC. 
If the sdcAddress is not specified or not available (connection failure) the messageBusAddresses will be used (Dmaap servers) 

### Docker-compose

A [docker-compose example file](extra/docker/clamp/docker-compose.yml) can be found under the [extra/docker/clamp/ folder](extra/docker/).

Once the image has been built and is available locally, you can use the `docker-compose up` command to deploy a prepopullated database and a clamp instance available on [http://localhost:8080/designer/index.html](http://localhost:8080/designer/index.html).


### Logs

Clamp uses logback framework to generate logs. The logback.xml file cand be found under the [src/main/resources/ folder](src/main/resources). 

With the default log settings, all logs will be generated into console and into root.log file under the Clamp root folder. The root.log file is not allowed to be appended, thus restarting the clamp will result in cleaning of the old log files.

### Api

You can see the swagger definition for the jaxrs apis at `/restservices/clds/v1/openapi.json`


## Clamp Credentials

There are two mechanisms that can enabled for the authentication, one or the other never both at the same time. 
They can be enabled in the application.properties.

1. AAF CA
There is a section for SSL enablement and cadi configuration (for AAF) + one spring profile to enable

server.port=8443
server.ssl.key-store=classpath:/clds/aaf/org.onap.clamp.p12
server.ssl.key-store-password=China in the Spring
server.ssl.key-password=China in the Spring
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=clamp@clamp.onap.org
server.ssl.client-auth=want
server.ssl.trust-store=classpath:/clds/aaf/truststoreONAPall.jks
server.ssl.trust-store-password=changeit

server.http-to-https-redirection.port=8080
....
spring.profiles.active=clamp-default,clamp-aaf-authentication,clamp-sdc-controller
....
clamp.config.cadi.keyFile=classpath:/clds/aaf/org.onap.clamp.keyfile
clamp.config.cadi.cadiLoglevel=DEBUG
clamp.config.cadi.cadiLatitude=37.78187
clamp.config.cadi.cadiLongitude=-122.26147
clamp.config.cadi.aafLocateUrl=https://aaf.api.simpledemo.onap.org:8095
clamp.config.cadi.cadiKeystorePassword=enc:V_kq_EwDNb4itWp_lYfDGXIWJzemHGkhkZOxAQI9IHs
clamp.config.cadi.cadiTruststorePassword=enc:Mj0YQqNCUKbKq2lPp1kTFQWeqLxaBXKNwd5F1yB1ukf
clamp.config.cadi.aafEnv=DEV
clamp.config.cadi.aafUrl=https://AAF_LOCATE_URL/AAF_NS.service:2.0
clamp.config.cadi.cadiX509Issuers=CN=intermediateCA_1, OU=OSAAF, O=ONAP, C=US

In that case a certificate must be added in the browser and is required to login properly
Please check that section to get the certificate
https://wiki.onap.org/display/DW/Control+Loop+Flows+and+Models+for+Casablanca#ControlLoopFlowsandModelsforCasablanca-Configure

Or it can be found in the Clamp source code folder src/main/resources/clds/aaf
(Default Password: "China in the Spring")

2. Spring authentication
It's possible to enable the spring authentication by disabling the "clamp-aaf-authentication" profile and enabling only the "clamp-default-user"
spring.profiles.active=clamp-default,clamp-default-user,clamp-sdc-controller
In that case, the credentials should be specified in `src/main/resources/clds/clds-users.json`. You might specify you own credential file by redefining the `clamp.config.files.cldsUsers` in `application.properties`.

Passwords should be hashed using Bcrypt :
```
# pip3 install bcrypt  # if you don't have the bcrypt python lib installed, should be done once.
# python3 -c 'import bcrypt; print(bcrypt.hashpw("password".encode(), bcrypt.gensalt(rounds=10, prefix=b"2a")))'
```

Default credentials are admin/password and cs0008/password.

There is a spring file that disables the AAF and enable the Spring authentication by default. 
To use it just add

--spring.config.name=application-noaaf

to the jvm parameters. This file is available by default in the java classpath resource folder. 