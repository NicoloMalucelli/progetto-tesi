package test;

import io.vertx.core.Vertx;
import io.vertx.mqtt.MqttServer;

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
		  System.out.println(endpoint.will());
		  if (endpoint.will() != null) {
			final byte[] rawPayload = endpoint.will().getWillMessageBytes();
			final String payload = rawPayload == null ? "null" : new String(rawPayload);
		    System.out.println("[will topic = " + endpoint.will().getWillTopic() + 
		    				   " msg = " + payload +
		    				   " QoS = " + endpoint.will().getWillQos() + 
		    				   " isRetain = " + endpoint.will().isWillRetain() + "]");
		  }
	
		  System.out.println("[keep alive timeout = " + endpoint.keepAliveTimeSeconds() + "]");
	
		  // accept connection from the remote client
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