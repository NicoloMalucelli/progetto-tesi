package test;

import digitalTwin.DtManager;
import mqtt.MQTTBroker;

public class Controller{

	public static void main(String[] args) {
		System.out.println("Running Controller");
		DtManager dtManager = new DtManager();
		MQTTBroker mqttBroker = new MQTTBroker(dtManager);
	}

}