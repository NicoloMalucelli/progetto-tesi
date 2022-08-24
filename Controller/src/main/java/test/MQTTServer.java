package test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.azure.core.credential.TokenCredential;
import com.azure.core.models.JsonPatchDocument;
import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.mqtt.MqttAuth;
import io.vertx.mqtt.MqttServer;

public class MQTTServer {

	public static void main(String[] args) {
		
		//DT platform authentication
		
		TokenCredential credential = new DefaultAzureCredentialBuilder().build();
		System.out.println(credential);
		
		
		DigitalTwinsClient dtClient = new DigitalTwinsClientBuilder()
				.credential(credential)
				.endpoint("https://DT-prova.api.weu.digitaltwins.azure.net")
				.buildClient();  
				
		BasicDigitalTwin dt = dtClient.getDigitalTwin("sensorBoard-01", BasicDigitalTwin.class);
		System.out.println(dt.getContents());

		/*
		JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
		jsonPatchDocument.appendAdd("/Brightness", 23.0);

		dtClient.updateDigitalTwin(
		     "sensorBoard-01",
		     jsonPatchDocument);
		*/
		
		//dtClient.createModels(dtdlModels);
		
		//SERVER
		
		Map<String, String> users = new ConcurrentHashMap<>();
		Map<String, String> devices = new ConcurrentHashMap<>();
		
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
		  
		  endpoint.publishHandler(message -> {
			 if(!endpoint.isConnected()) {
				 return;
			 }
			 System.out.println("Message received from " +  auth.getUsername() + "on topic " + message.topicName() + ": " + message.payload());
			 final JsonObject payload= new JsonObject(message.payload());
			 if(message.topicName().equals("createAndBind")) {
				 if(devices.containsKey(auth.getUsername())) {
					 return;
				 }
				 if(devices.values().contains(payload.getString("device-id"))) {
					 //esiste gi� un dispositivo con l'id dato ma non corrisponde all'utente
					 //TODO dire alla sorgente di cambiare device-id
					 return;
				 }
				 devices.put(auth.getUsername(), payload.getString("device-id"));
				 //TODO usare le API per creare il nuovo DT.
			 }
			 
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
	
	public static String sha256(String s) {
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
