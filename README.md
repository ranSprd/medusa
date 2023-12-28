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

- prometheus endpoint with collected metrics http://localhost:8085/metrics
- a list of received but not processed metrics http://localhost:8085/topics/unprocessed
  for each entry you see the received count (means, how often a message came in) and a
  possible list of values and labels. The values are important, because these are good
  candidates for metric values.
- Quarkus application start page http://localhost:8085/
- Quarkus application devloper page 
- Swagger UI http://localhost:8085/q/swagger-ui/

### How to start

Medusa is shipped with a docker configuration. The easiest way is to build and 
start with docker. Build the image with:

    docker build -f src/main/docker/Dockerfile.jvm -t quarkus/medusa-jvm .

Then run the container using:

    docker run -i --rm -p 8081:8081 quarkus/medusa-jvm

#### Quick start configuration

Medusa can handle several data sources and transform the received data into metric compatible
formats. To define a new mqtt data sources, add a new connector entry in section **mqtt-brokers**
of 'config/connectors.yaml' file

        mqtt-brokers:
        - name: central
          url: tcp://192.168.1.2:1883
          mapping-file: config/topic-mappings.yaml

This creates a new connector named *central* connected to a local mqtt broker. 
The *central* connector subsripes to all mqtt topics but, as now, no metrics are read from the
messages. You can find all detected/received topics under the *unprocessed endpoint* (see above).



## MQTT configuration



Metrics are only collected if the topic is defined in mapping file. The mapping 
file is  set in 

    # /home/kitchen/ESP8266-1076281
    - topic: /home/{place}/ESP8266-{deviceId}
      metrics:
      - valueField: freeheap
        labels: [place, deviceId]
        name: devices_free_heap

## configuration examples 

- [Example Configuration for Victron Cerbo GX](documentation/victron_cerbo.md)
- [Example Configuration for Zehnder ComfoAir Q350/450/600](documentation/zehnder_comfoair.md)




### mapping configuration