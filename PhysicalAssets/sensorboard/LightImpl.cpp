#include "LightImpl.h"
#include "Arduino.h"

LightImpl::LightImpl(int pin){
  this->pin = pin;
  pinMode(pin, OUTPUT);  
}

void LightImpl::turnOn(){
  digitalWrite(this->pin, HIGH);
}

void LightImpl::turnOff(){
  digitalWrite(this->pin, LOW);
}
