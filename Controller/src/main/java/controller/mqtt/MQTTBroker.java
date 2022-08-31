package mqtt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import digitalTwin.DtManager;
import io.netty.handler.codec.mqtt.MqttProperties;
import io.netty.handler.codec.mqtt.MqttQoS;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttAuth;
import io.vertx.mqtt.MqttEndpoint;
import io.vertx.mqtt.MqttServer;
import io.vertx.mqtt.MqttTopicSubscription;
import io.vertx.mqtt.messages.codes.MqttSubAckReasonCode;

public class MQTTBroker {
	
	private final DtManager dtManager;
	
	private final Map<String, String> users = new ConcurrentHashMap<>();
	private final Map<String, String> devices = new ConcurrentHashMap<>();
	private final Map<String, Set<MqttEndpoint>> subscriptions = new ConcurrentHashMap<>();

	public MQTTBroker(DtManager dtManager) {
		this.dtManager = dtManager;
		this.dtManager.setMQTTBroker(this);
		
		Vertx vertx = Vertx.vertx();
		MqttServer mqttServer = MqttServer.create(vertx);
		
		mqttServer.exceptionHandler(t -> System.out.println("refused message"));
		
		mqttServer.endpointHandler(endpoint -> {
			
		  // shows main connect info
		  System.out.println("MQTT client [" + endpoint.clientIdentifier() + "] request to connect, clean session = " + endpoint.isCleanSession());
		  
		  //connection validation
		  final MqttAuth auth = endpoint.auth();
		  if(auth == null) {
			  System.out.println("Connection refused: unauthenticated connection.");
			  //TODO endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_AUTHENTICATION_METHOD);
		  }

		  if(users.containsKey(auth.getUsername()) && !sha256(auth.getPassword()).equals(users.get(auth.getUsername()))) {
			  System.out.println("Connection refused: wrong password for username " + auth.getUsername());
			  //TODO endpoint.reject(MqttConnectReturnCode.CONNECTION_REFUSED_BAD_USER_NAME_OR_PASSWORD);
		  }

		  if(users.containsKey(auth.getUsername()) && sha256(auth.getPassword()).equals(users.get(auth.getUsername()))) {
			  System.out.println("Connection accepted: " + auth.getUsername() + " logged in");
		  }
		  
		  if(!users.containsKey(auth.getUsername())) {
			  System.out.println("Connection accepted: " + auth.getUsername() + " enrolled");
			  users.put(auth.getUsername(), sha256(auth.getPassword()));
		  }
		  
		  endpoint.subscribeHandler(subscribe -> {

			  List<MqttSubAckReasonCode> reasonCodes = new ArrayList<>();
			  for (MqttTopicSubscription s: subscribe.topicSubscriptions()) {
			    System.out.println("Subscription for " + s.topicName() + " with QoS " + s.qualityOfService());
			    reasonCodes.add(MqttSubAckReasonCode.qosGranted(s.qualityOfService()));
			    if(subscriptions.get(s.topicName()) == null) {
			    	subscriptions.put(s.topicName(), new ConcurrentHashSet<>());
			    }
			    subscriptions.get(s.topicName()).add(endpoint);
			  }
			  // ack the subscriptions request
			  endpoint.subscribeAcknowledge(subscribe.messageId(), reasonCodes, MqttProperties.NO_PROPERTIES);
		  });
		  
		  
		  endpoint.publishHandler(message -> {
			 if(!endpoint.isConnected()) {
				 return;
			 }
			 System.out.println("Message received from " +  auth.getUsername() + " on topic " + message.topicName() + ": " + message.payload());
			 final JsonObject payload= new JsonObject(message.payload());
			 
			 publish(message.topicName(), payload);
			 
			 if(message.topicName().equals("createAndBind")) {
				 //wrong message format
				 if(!payload.containsKey("device-id") || !payload.containsKey("model-id")) {
					 return;
				 }
				 
				 //create new device's digital twin
				 devices.put(auth.getUsername(), payload.getString("device-id"));
				 dtManager.createDT(payload.getString("device-id"), payload.getString("model-id"));
			 }else if(message.topicName().equals("shadowing")) {
				 if(!devices.containsKey(auth.getUsername())) {
					 return;
				 }
				 String deviceId = devices.get(auth.getUsername());
				 dtManager.shadowDT(deviceId, payload);
			 }
		  });
		  
		  endpoint.accept(false);
		});
		  
		mqttServer.listen(ar -> {
	
		    if (ar.succeeded()) {
	
		      System.out.println("MQTT server is listening on port " + ar.result().actualPort());
		    } else {
	
		      System.out.println("Error on starting the server");
		      ar.cause().printStackTrace();
		    }
		  });
		
	}
	
	public void publish(String topic, JsonObject payload) {
		for (String s : subscriptions.keySet()) {
			if(topic.startsWith(s + "/") || topic.equals(s)) {
				for (MqttEndpoint e : subscriptions.get(s)) {
					e.publish(topic, payload.toBuffer(), MqttQoS.AT_LEAST_ONCE, false, false);
					System.out.println("message redirected to " + e.auth().getUsername());
				}
			}
		 }
	}
	
	private String sha256(String s) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA3-256");
			final byte[] hashbytes = digest.digest(
					  s.getBytes(StandardCharsets.UTF_8));
			String sha3Hex = new String(hashbytes);
			return sha3Hex;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return s;
	}

}
