# Victron Cerbo GX

The Victron Cerbo GX devices can be integrated into your home automation over MQTT. 


# How to connect and receive messages

On the Cerbo GX devices runs a dedicated MQTT broker, which is listening on the 
default 1883 port. You can check it on command line (mosquitto toolset)

        mosquitto_sub -h 192.168.2.228 -t \# -d

wheryby 192.168.2.228 is my internal IP adress. You should see messages like:

        Client null sending CONNECT
        Client null received CONNACK (0)
        Client null sending SUBSCRIBE (Mid: 1, Topic: #, QoS: 0, Options: 0x00)
        Client null received SUBACK
        Subscribed (mid: 1): 0
        Client null received PUBLISH (d0, q0, r1, m0, 'N/c0719ba04388/keepalive', ... (12 bytes))
        {"value": 1}
        Client null received PUBLISH (d0, q0, r1, m0, 'N/c0719ba04388/system/0/Serial', ... (25 bytes))
        {"value": "c0719ba04388"}

The interesting part here is the internal *ID* 'c0719ba04388'. Because the broker 
does not send the device data continuously but only on trigger request. 

For receiving data you has to send a empty message to the topic 'R/{ID}/keepalive'

        mosquitto_pub -h 192.168.2.228 -m "" -t R/c0719ba04388/keepalive

Now you recieve a huge bunge of messages.

# Example configuration

Configure a new MQTT connector named *victron-solar* in your 'connectors.yaml'

        - name: victron-solar
          url: tcp://192.168.2.228:1883
          
This should point *medusa* to the Cerbo GX MQTT broker. But no messages will arrive 
until a request has been send from our side. Configuring a heartbeat event for the 
*victron-solor* connector solves that problem.

        - name: victron-solar
          url: tcp://192.168.2.228:1883
          heartbeat:
            cron: "0/55 * * * * ?"
            topic: R/c0719ba04388/keepalive
            message: ""

With this configuration, you should receive regular messages from your Cerbo GX. 
The cron expression defines the intervall. Here every 55s a heartbeat event will 
be send to the broker on Victrons Cerbo GX device. Now you can define the 
processing of the messages into metrics in a mapping file (here *config/cerbo-mappings.yaml*).

        - name: victron-solar
          url: tcp://192.168.2.228:1883
          mapping-file: config/cerbo-mappings.yaml
          heartbeat:
            cron: "0/55 * * * * ?"
            topic: R/c0719ba04388/keepalive
            message: ""


