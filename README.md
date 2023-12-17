# Medusa

Medusa is a mapper for prometheus metrics. Currently it can handle mqtt
messages and convert they in a prometheus compatible metric format.

## Features

- transform MQTT message into Prometheus metrics
- automatic mode detects metrics in messages without manual configuration

## How it works

After startup the application connects to the configured MQTT broker in your system 
and subscripes to all available topics. In the background it collects all incoming
messages. For the configured topics it tries to extract metrics, all other metrics 
will be unprocessed and collected as 'unknow' topics.

The web-server part of this application provides a prometheus endpoint. You can 
configure your running prometheus to read all metrics from there.  

### endpoints

The default port of the appilcation is 8081. After startup the following resources
are available:

- prometheus endpoint with collected metrics http://localhost:8081/metrics
- a list of received but nit processed metrics http://localhost:8081/topics/unknown
- Quarkus application start page http://localhost:8081/
- Quarkus application devloper page 
- Swagger UI http://localhost:8081/q/swagger-ui/

### How to start

Medusa is shipped with a docker configuration. The easiest way is to build and 
start with docker. Build the image with:

    docker build -f src/main/docker/Dockerfile.jvm -t quarkus/medusa-jvm .

Then run the container using:

    docker run -i --rm -p 8081:8081 quarkus/medusa-jvm

#### Quick start configuration



## configuration

- [Example Configuration for Zehnder ComfoAir Q350/450/600](documentation/zehnder_comfoair.md)




### mapping configuration