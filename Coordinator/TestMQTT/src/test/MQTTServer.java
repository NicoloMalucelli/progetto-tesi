package test;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttWill;

public class MQTTServer {

	public static void main(String[] args) {

		Vertx vertx = Vertx.vertx();
		MqttServer mqttServer = MqttServer.create(vertx);
		mqttServer.endpointHandler(endpoint -> {
	
		  // shows main connect info
		  System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());
		  
		  if (endpoint.auth() != null) {
		    System.out.println("[username = " + endpoint.auth().getUsername() + ", password = " + endpoint.auth().getPassword() + "]");
		  }
		 
		  endpoint.publishHandler(message -> {
			 System.out.println("Message received: topic: " + message.topicName() + "; payload: " + message.payload());
		  });
		  
		  endpoint.accept(false);
	
		})
		  .listen(ar -> {
	
		    if (ar.succeeded()) {
	
		      System.out.println("MQTT server is listening on port " + ar.result().actualPort());
		    } else {
	
		      System.out.println("Error on starting the server");
		      ar.cause().printStackTrace();
		    }
		  });
		
	}

}
