[![Dependency Status](https://www.versioneye.com/user/projects/572367feba37ce00350af6cb/badge.svg?style=flat)](https://www.versioneye.com/user/projects/572367feba37ce00350af6cb)

# h2o-scoring-engine-publisher

An application with a RESTful interface that builds H2O scoring engine and exposes it as a JAR file for downloading or publishes it as a service offering in CloudFoundry marketplace.

## Required services
h2o-scoring-engine-publisher requires following services to run on TAP:
* sso 
* marketplace-register-service

## How to build
h2o-scoring-engine-publisher is a spring-boot application build by maven. A command to compile, run tests and build:
```
$ mvn package
```

## How to run locally
To run the service locally define following environment variables:

* `vcap.services.sso.credentials.apiEndpoint` - CloudFoundry API endpoint
* `vcap.application.space_id` - CloudFoundry space guid, where scoring-engine application will be uploaded
* `vcap.services.marketplace-register-service.credentials.applicationBrokerUrl` - application-broker url
* `vcap.services.marketplace-register-service.credentials.username` - application-broker username
* `vcap.services.marketplace-register-service.credentials.username` - application-broker password
* `vcap.services.sso.credentials.tokenUri` - UAA OAuth2 endpoint for token exchanging during CloudFoundry API calls
* `vcap.services.sso.credentials.clientId` - client Id used for OAuth2 authorization during CloudFoundry API calls
* `vcap.services.sso.credentials.clientSecret` - client secret used for OAuth2 authorization during CloudFoundry API calls

and run service:
```
java -jar h2o-scoring-engine-publisher-x.y.z.jar
```

## How to use
h2o-scoring-engine-publisher provides REST API

### Publish scoring engine to TAP marketplace 

**URL**: `http://<application-host>/rest/h2o/engines`

**Headers**: `Content-type: application/json`

**HTTP Method**: `POST`

**Request body**: 
```
{  
   "h2oCredentials":{  
      "host":"http://<h2o sever host>",
      "username":"h2o server username",
      "password":"h2o server password"
   },
   "modelName":"name of the model we want to publish",
   "orgGuid":"guid of organization where we want to publish the model"
}
```
### Download scoring engine as a JAR file
**URL**: `http://<application-host>/rest/h2o/engines/<model-name>/downloads`

**HTTP Method**: `POST`

**Request body**: 
```
host=<h2o server host>&username=<h2o server username>&password=<h2o server password>"
```




