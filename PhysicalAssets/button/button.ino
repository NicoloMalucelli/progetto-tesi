#include <WiFi.h>
#include <PubSubClient.h>

#define BUTTON_PIN 5

const char* ssid = "EOLO - FRITZ!Box 7430 YT";
const char* password = "41195728343377587300";

bool publishRequest = false;
long lastClick = 0;

WiFiClient wifiClient;
PubSubClient client(wifiClient); 

char *mqttServer = "192.168.178.104";
int mqttPort = 1883;

void connectToWifi(const char* ssid, const char* password){
  int timeout_counter = 0;
  WiFi.begin(ssid, password);
  Serial.println("Connecting to WiFi");
  while(WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
    timeout_counter++;
    if(timeout_counter >= 30*2){
      Serial.println("Connection failed");
      ESP.restart();
    }
  }
  Serial.print("Connected: ");
  Serial.println(WiFi.localIP());
  connectToBroker();
}

void connectToBroker() {
  client.setServer(mqttServer, mqttPort);
  Serial.println("Connecting to MQTT Broker...");
  String clientId = String("esp32")+String(random(0xffff), HEX);
  while (!client.connected()) {
      if(client.connect(clientId.c_str(), "Esp32-button", "PXvSHt")){
         Serial.println("connected"); 
      }else{
        delay(3000);
        Serial.println("trying to reconnect"); 
      }
  }
  
  Serial.println("Binding...");
  client.publish("createAndBind", "{\"device-id\":\"button-01\", \"model-id\":\"dtmi:progettotesi:button;1\"}");
  Serial.println("Shadowing...");
  client.publish("shadowing", "{\"isPressed\":false}");
}

void setup() {
  Serial.begin(115200);
  pinMode(BUTTON_PIN, INPUT);
  attachInterrupt(digitalPinToInterrupt(BUTTON_PIN), sendData, CHANGE);
  connectToWifi(ssid, password);
}

void loop() {
  if(WiFi.status()== WL_CONNECTED && client.connected()){
    if(publishRequest){
      publishRequest = false;
      Serial.println("Shadowing...");
      if(digitalRead(BUTTON_PIN) == HIGH){
        client.publish("shadowing", "{\"isPressed\":true}");
      }else{
        client.publish("shadowing", "{\"isPressed\":false}");
      }
    }
  }else{
      connectToWifi(ssid, password);
  }
}

void sendData(){
  if(millis() - lastClick < 100){
    return;
  }
  lastClick = millis();
  publishRequest = true;
  return;
}
