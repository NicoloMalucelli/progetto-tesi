#include <WiFi.h>
#include <HTTPClient.h>
#include <math.h>

#include "LightImpl.h"
#include "LightSensorImpl.h"
#include "TemperatureSensorImpl.h"

#define UPDATE_TIME 5000
#define PHOTO_RESISTOR_PIN 33
#define TEMP_RESISTOR_PIN 32
#define LED_PIN 16

//Azure IoT Hub
const String AzureIoTHubURI="https://test2-progetto-tesi.azure-devices.net/devices/esp-32/messages/events?api-version=2020-03-13";
//openssl s_client -servername myioteventhub.azure-devices.net -connect myioteventhub.azure-devices.net:443 | openssl x509 -fingerprint -noout //
const String AzureIoTHubFingerPrint="{YourGeneratedFingerPrint}"; 
//az iot hub generate-sas-token --device-id {YourIoTDeviceId} --hub-name {YourIoTHubName} 
const String AzureIoTHubAuth="SharedAccessSignature sr=test2-progetto-tesi.azure-devices.net%2Fdevices%2Fesp-32&sig=3v08Iu3ucYbnu4BXIQh%2B4ntCReE91qnFjdacyBXTEYQ%3D&se=1660377401";

int light;
int temp;
bool ledOn = true;

const char* ssid = "EOLO - FRITZ!Box 7430 YT";
const char* password = "41195728343377587300";
const char* serviceURI = "http://192.168.178.104:8124";

LightSensor* lightSensor;
TemperatureSensor* temperatureSensor;

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
}

void setup() {
  Serial.begin(115200);
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, HIGH);
  connectToWifi(ssid, password);

  lightSensor = new LightSensorImpl(PHOTO_RESISTOR_PIN);
  temperatureSensor = new TemperatureSensorImpl(TEMP_RESISTOR_PIN);
}

void loop() {
  if(WiFi.status()== WL_CONNECTED){ 
    light = map(lightSensor->getValue(), 0, 4095, 1, 100);
    temp = int(temperatureSensor->getTemperature());
    
    Serial.println("light: " + String(light) + "; temp: " + String(temp));
    sendData(light, temp);
    delay(UPDATE_TIME);
   }else{
    connectToWifi(ssid, password);
  }
}

void sendData(int light, int temp){
   HTTPClient http;
   http.begin(AzureIoTHubURI);//, fingerPrint);
   #http.addHeader("Authorization",AzureIoTHubAuth);
   http.addHeader("Content-Type", "application/atom+xml;type=entry;charset=utf-8");

   String PostData = "{'Brightness':" + String(light) + ",'Temperature': " + String(temp) + "}";
   
   int ret=http.POST(PostData);
   Serial.println("ret: " + String(ret));
   Serial.println("payload: " + http.getString());
   http.end();
   return;
}
