#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>


#define LIGHT_PIN 19

const char* ssid = "EOLO - FRITZ!Box 7430 YT";
const char* password = "41195728343377587300";

WiFiClient wifiClient;
PubSubClient client(wifiClient); 

char *mqttServer = "192.168.178.104";
int mqttPort = 1883;

void connectToBroker() {
  client.setServer(mqttServer, mqttPort);
  client.setCallback(callback);
  Serial.println("Connecting to MQTT Broker...");
  String clientId = String("esp32")+String(random(0xffff), HEX);
  while (!client.connected()) {
      if(client.connect(clientId.c_str(), "Esp32-light", "PXvSHt")){
         Serial.println("connected"); 
      }else{
        delay(3000);
        Serial.println("trying to reconnect"); 
      }
  }
  
  Serial.println("binding");
  client.publish("createAndBind", "{\"device-id\":\"light-01\", \"model-id\":\"dtmi:contosocom:DigitalTwins:Light;1\"}");
  Serial.println("binded");
  Serial.println("shadowing");
  client.publish("shadowing", "{\"isOn\":false}");
  Serial.println("shadowed");

  client.subscribe("action/light-01");
  Serial.println("subscribed to action/light-01");
}

void connectToWifi(const char* ssid, const char* password){
  int timeout_counter = 0;
  WiFi.begin(ssid, password);
  while(WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
    timeout_counter++;
    if(timeout_counter >= 30*2){
      ESP.restart();
    }
  }
  Serial.println(WiFi.localIP());
  connectToBroker();
}

void setup() {
  Serial.begin(115200);
  pinMode(LIGHT_PIN, OUTPUT);
  connectToWifi(ssid, password);
}

void loop() {
    if(WiFi.status()== WL_CONNECTED && client.connected()){
      client.loop();
    }else{
      connectToWifi(ssid, password);
    } 
}

void callback(char* topic, byte* message, unsigned int length) {
  Serial.print("Message arrived on topic: ");
  Serial.print(topic);
  Serial.print(". Message: ");
  String messageTemp;
  
  for (int i = 0; i < length; i++) {
    Serial.print((char)message[i]);
    messageTemp += (char)message[i];
  }

  char msgChar[messageTemp.length() + 1];
  messageTemp.toCharArray(msgChar, messageTemp.length() + 1);
  
  StaticJsonDocument<200> doc;
  deserializeJson(doc, msgChar);
  const char* out = doc["op"];

  
  if (String(topic) == "action/light-01") {
    if((String)out == "on"){
      Serial.println("on");
      digitalWrite(LIGHT_PIN, HIGH);
    }
    else if((String)out == "off"){
      Serial.println("off");
      digitalWrite(LIGHT_PIN, LOW);
    }
  }
  
}
