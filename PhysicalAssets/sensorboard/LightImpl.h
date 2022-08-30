#ifndef __LIGHT_IMPL__
#define __LIGHT_IMPL__

#include "Light.h"

class LightImpl: public Light {
 
public: 
  LightImpl(int pin);
  void turnOn();
  void turnOff();

private:
  int pin;

};

#endif