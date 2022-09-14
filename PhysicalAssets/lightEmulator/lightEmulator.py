# -*- coding: utf-8 -*-
"""
Created on Thu Aug 25 10:41:55 2022

@author: Nicolò
"""

import paho.mqtt.client as mqtt
import json
import threading

username = "lightEmulator-03"
password = "password"

lightOn = False;

# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    client.subscribe("action/light-01")
    print("Subscribed to action/light-01")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    if msg.topic ==  "action/light-01":
        payload = json.loads(msg.payload.decode())
        if('op' in payload):
            global lightOn
            if payload['op'] == 'on':
                lightOn = True
            if payload['op'] == 'off':
                lightOn = False
                
        print("lightOn: " + 'on' if lightOn else 'off')
        shadowingInfo = {'isOn': lightOn}
        client.publish("shadowing", json.dumps(shadowingInfo), 1);    
        
    print("Received " + str(msg.payload))

def keep_alive():
    client.publish("keepAlive", "", 1);

client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message

client.username_pw_set(username, password=password);
client.connect("192.168.178.104", 1883, 100)

payload ={'device-id':'light-03', 
          'model-id':'dtmi:progettotesi:light;1'}
client.publish("createAndBind", json.dumps(payload), 1);

shadowingInfo = {'isOn': lightOn}
client.publish("shadowing", json.dumps(shadowingInfo), 1);

threading.Timer(50.0, keep_alive).start()

client.loop_forever()