#include "TemperatureSensorImpl.h"
#include <math.h>
#include "Arduino.h"

TemperatureSensorImpl::TemperatureSensorImpl(int pin){
    this->pin = pin;
    pinMode(pin, INPUT);
}

float TemperatureSensorImpl::getTemperature(){
    int Vo;
    float R1 = 10000;
    float logR2, R2, T, Tc, Tf;
    float c1 = 1.009249522e-03, c2 = 2.378405444e-04, c3 = 2.019202697e-07;

    Vo = analogRead(this->pin);
    R2 = R1 * (4095.0 / (float)Vo - 1.0);
    logR2 = log(R2);
    T = (1.0 / (c1 + c2*logR2 + c3*logR2*logR2*logR2));
    Tc = T - 273.15;
    return Tc;
}
