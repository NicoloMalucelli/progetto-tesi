#ifndef __LIGHT_SENSOR_IMPL__
#define __LIGHT_SENSOR_IMPL__

#include "LightSensor.h"

class LightSensorImpl: public LightSensor{
    public:
        LightSensorImpl(int pin);
        int getValue();
    
    private:
        int pin;
};

#endif