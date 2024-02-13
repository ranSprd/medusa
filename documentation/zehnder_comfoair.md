# Zehnder Comfoair Q350/450/600

The Zehnder ComfoAir Qxxx devices can be integrated into your home automation with the 
additional 'ComfoConnect LAN C' device. The ComfoConnect offers a mqtt functionality. 


# How to connect and receive messages

For me it was a little bit tricky to get some data from my Zehnder ComfoAir/ComfoConnect 
pair, because you need a UUID for authorisation.
I used python library _aicomfoconnect_. Either you clone the [repository](https://github.com/michaelarnauts/aiocomfoconnect)
or install it locally

    pip install aicomfoconnect

Then, you have to register on your comfoconnect device (creates a UUID for your calls)

    python -m aiocomfoconnect --debug register --host IP-of-comfoconnect-device

After this you should be able to see values

    python -m aiocomfoconnect show-sensors --host IP-of-comfoconnect-device

From here, I created a small cron job (_based on show-sensors code_) which reads the sensor values and publish 
it on my local mqtt. If somebody is interesed, I can drop the code here.


# Example configuration



