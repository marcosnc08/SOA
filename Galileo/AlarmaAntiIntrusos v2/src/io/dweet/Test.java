package io.dweet;

import java.io.IOException;
import java.text.ParseException;

import com.google.gson.JsonObject;

public class Test {

	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		 String thingName= "soa_alarma";
		 JsonObject json= new JsonObject();
	    	json.addProperty("activada", false);
	    	json.addProperty("sonando", false);
	    	try {
				DweetIO.publish("soa_alarma", json);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		 /**
	         * Get latest Dweets. 
	         */
		Dweet dweet= DweetIO.getLatestDweet(thingName);
//        System.out.println(dweet.getThingName()+ " said : "+ dweet.getContent() +" at "+ dweet.getCreationDate()); 
		System.out.println(dweet.getContent().get("temperatura").getAsString()); 
	}

}
