from azure.identity import DefaultAzureCredential
from azure.digitaltwins.core import *
import json
import paho.mqtt.client as mqtt

url = "https://DT-prova.api.weu.digitaltwins.azure.net"
credential = DefaultAzureCredential()
dt_client = DigitalTwinsClient(url, credential)

listed_models = dt_client.list_models()
for model in listed_models:
    print(model)

# payload ={'op':msg}
channel=""
payload=""

username = "evenEvaluator-button-01"
password = "password"

"""
# The callback for when the client receives a CONNACK response from the server.
def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

client = mqtt.Client()
client.on_connect = on_connect

client.username_pw_set(username, password=password);
client.connect("192.168.178.104", 1883, 100)

client.publish(channel, json.dumps(payload), 0)
print("published " + msg + " on " + channel)
"""