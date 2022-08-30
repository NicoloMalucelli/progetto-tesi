#include "LightSensorImpl.h"
#include <math.h>
#include "Arduino.h"

LightSensorImpl::LightSensorImpl(int pin){
    this->pin = pin;
    pinMode(pin, INPUT);
}

int LightSensorImpl::getValue(){
    return analogRead(this->pin);
}
