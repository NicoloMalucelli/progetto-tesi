# -*- coding: utf-8 -*-
"""
Created on Tue Aug 30 09:15:18 2022

@author: Nicolò
"""

import paho.mqtt.client as mqtt
import json
import threading

username = "buttontEmulator-03"
password = "password"

connected = False;

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    global connected
    connected = True
    print("Connected with result code "+str(rc))
    
# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))
    
def on_disconnect(client, userdata, rc):
    global connected
    connected = False
    print("Disconnected")
    
def keep_alive():
    client.publish("keepAlive", "", 1);

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.on_disconnect = on_disconnect

client.username_pw_set(username, password=password);
client.connect("192.168.178.104", 1883, 100)
client.loop_start()

threading.Timer(50.0, keep_alive).start()

payload = {'device-id':"button-03", 
           "model-id":"dtmi:progettotesi:button;1"}
client.publish("createAndBind", json.dumps(payload), 0)

payload = {'isPressed':False}
client.publish("shadowing", json.dumps(payload), 0)

while True:
    msg = input("on y/n: ")
    if msg == "y":
        msg = "on"
        payload = {'isPressed':True}
    elif msg == "n":
        msg = "off"
        payload = {'isPressed':False}
    
    if not connected:
        client.reconnect()
    client.publish("shadowing", json.dumps(payload), 0)