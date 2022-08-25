# -*- coding: utf-8 -*-
"""
Created on Thu Aug 25 10:41:55 2022

@author: Nicolò
"""


import paho.mqtt.client as mqtt
import json

username = "lightEmulator37642"
password = "password"

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("action/" + username)
    print("Subscribed to action/" + username)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print("Received " + str(msg.payload))

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.username_pw_set(username, password=password);
client.connect("192.168.178.104", 1883, 100)

payload ={'device-id':'light-01', 
          'model-id':'dtmi:contosocom:DigitalTwins:Light;1'}
client.publish("createAndBind", json.dumps(payload), 1);

shadowingInfo = {'isOn': False}
client.publish("shadowing", json.dumps(shadowingInfo), 1);

client.loop_forever()