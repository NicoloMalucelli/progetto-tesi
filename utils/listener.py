# -*- coding: utf-8 -*-
"""
Created on Thu Aug 25 11:29:51 2022

@author: Nicolò
"""

import paho.mqtt.client as mqtt
import json

username = "listener24817"
password = "password"

channel = input("insert channel to subscribe: ")

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe(channel)
    print("subscribed to channel " + channel)

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print("Received " + str(msg.payload) + " from " + str(msg.topic))

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.username_pw_set(username, password=password);
client.connect("192.168.178.104", 1883, 100)

client.loop_forever()