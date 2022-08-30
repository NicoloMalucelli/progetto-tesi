# -*- coding: utf-8 -*-
"""
Created on Tue Aug 30 09:15:18 2022

@author: Nicolò
"""

import paho.mqtt.client as mqtt
import json

username = "Esp32-button"
password = "PXvSHt"

channel = "action/light-01"

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.username_pw_set(username, password=password);
client.connect("192.168.178.104", 1883, 100)

while True:
    msg = input("on y/n: ")
    if msg == "y":
        msg = "on"
    elif msg == "n":
        msg = "off"
    payload ={'op':msg}
    client.publish(channel, json.dumps(payload), 0)
    print("published " + msg + " on " + channel)