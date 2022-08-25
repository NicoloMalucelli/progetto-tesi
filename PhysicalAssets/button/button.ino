#include <WiFi.h>
#include <PubSubClient.h>

#define BUTTON_PIN 5

const char* ssid = "EOLO - FRITZ!Box 7430 YT";
const char* password = "41195728343377587300";

int light;
int temp;
bool clicked = false;
bool needConnection = false;
bool publishRequest = false;
long lastClick = 0;

WiFiClient wifiClient;
PubSubClient client(wifiClient); 

char *mqttServer = "192.168.178.104";
int mqttPort = 1883;

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
  
  Serial.println("binding");
  client.publish("createAndBind", "{\"device-id\":\"button-01\", \"model-id\":\"dtmi:contosocom:DigitalTwins:Button;1\"}");
  Serial.println("binded");
  Serial.println("shadowing");
  client.publish("shadowing", "{\"isPressed\":false}");
  Serial.println("shadowed");
  
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
  pinMode(BUTTON_PIN, INPUT);
  attachInterrupt(digitalPinToInterrupt(BUTTON_PIN), sendData, RISING);
  connectToWifi(ssid, password);

}

void loop() {
  if(publishRequest){
    if(WiFi.status()== WL_CONNECTED && client.connected()){
      publishRequest = false;
      Serial.println("shadowing");
      if(clicked){
        client.publish("shadowing", "{\"isPressed\":true}");
      }else{
        client.publish("shadowing", "{\"isPressed\":false}");
      }
      Serial.println("shadowed");
    }else{
      connectToWifi(ssid, password);
    }
  }
}

void sendData(){
  if(millis() - lastClick < 200){
    return;
  }
  lastClick = millis();
  clicked = !clicked;
  publishRequest = true;
  return;
}
