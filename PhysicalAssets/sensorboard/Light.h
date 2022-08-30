#ifndef __LIGHT__
#define __LIGHT__

class Light {
 
public:
  virtual void turnOn() = 0;
  virtual void turnOff() = 0;
};

#endif