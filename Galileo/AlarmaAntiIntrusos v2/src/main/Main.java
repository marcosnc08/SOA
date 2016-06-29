package main;

import io.dweet.Dweet;
import io.dweet.DweetIO;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.time.Duration;
import alarma.Alarma;

import com.google.gson.JsonObject;

import estados.Estados;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static {
        try {
            System.loadLibrary("mraajava");
        } catch (UnsatisfiedLinkError e) {
            System.err.println(
                    "Native code library failed to load. See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\n" +
                            e);
            System.exit(1);
        }
    }
    private final static String THING_ACTIVATED = "soa_alarma_activated";
    private final static String THING_RINGING = "soa_alarma_ringing";
    private final static String THING_PANIC = "soa_alarma_panic";
    private final static String THING_SENSORS = "soa_alarma_sensor_values";
    private final static String THING_LOGS = "soa_alarma_logs";
    private final static String THING_CHANGE_STATE = "soa_alarma_change_state";
    
	final static int LOW = 0;
	final static int HIGH = 1;
	private final static long DEBOUNCE = 200;
	private static final int MAX_LOG_SIZE = 10;
	
	private static Instant tiempoBoton = Instant.now();
	private static Instant tiempoPublicacionSensores = Instant.now();
	private static ArrayList<String> logging = new ArrayList<String>();
	private static boolean botonPresionado = false;
	private static Alarma alarma = new Alarma();
	private static ReentrantLock estadosLock = new ReentrantLock();
	private static JsonObject jsonAct = new JsonObject();
	private static JsonObject jsonRing = new JsonObject();
	private static JsonObject jsonPanic = new JsonObject();
	private static JsonObject jsonChange = new JsonObject();

	private volatile static Boolean sonando = false;
	
    public static void main(String argv[]) {
    	ThreadAlarmaSonando thread = new ThreadAlarmaSonando();
    	thread.start();
    	jsonChange.addProperty("change", false);
    	publicarDweet(THING_CHANGE_STATE, jsonChange);
		jsonAct.addProperty("activated", false);
		publicarDweet(THING_ACTIVATED, jsonAct);
		jsonRing.addProperty("ringing", false);
		publicarDweet(THING_RINGING, jsonRing);
		jsonPanic.addProperty("panic", false);
		publicarDweet(THING_PANIC, jsonPanic);
    	ThreadEventos threadEvents = new ThreadEventos();
    	threadEvents.start();
    	
    	while (true) {
        	//System.out.println("Sensor Gas: "+alarma.readEstadoSensorGas());
//        	System.out.println("Sensor Temperatura: "+alarma.readEstadoSensorTemeperatura());
    		alarma.readEstadoActualBotonActivacion();
			if(alarma.getEstadoActualBotonActivacion() == HIGH && alarma.getEstadoAnteriorBotonActivacion() == LOW && Duration.between(tiempoBoton,Instant.now()).toMillis() > DEBOUNCE) {
				System.out.println("BOTON PRESIONADO.");
				botonPresionado = true;
				tiempoBoton = Instant.now();
			}				
			switch(alarma.getEstado()) {
				case ALARMA_ACTIVADA:
					if(alarma.readEstadoSensorMovimiento() == HIGH) {
						alarma.setEstado(Estados.ALARMA_SONANDO);
						synchronized (jsonRing) {
							System.out.println("INTRUSO: Escribiendo ringing (true)");
							jsonRing.addProperty("ringing", true);
							publicarDweet(THING_RINGING, jsonRing);
						}
						log("Intruso detectado");
					}
					if(botonPresionado) {
						try {
							alarma.desactivarAlarma();
							synchronized (jsonAct) {
								System.out.println("BOTON PRESIONADO: Desactivando...");
								jsonAct.addProperty("activated", false);
								publicarDweet(THING_ACTIVATED, jsonAct);
							}
							synchronized (jsonRing) {
								System.out.println("BOTON PRESIONADO: Ringing false...");
								jsonRing.addProperty("ringing", false);
								publicarDweet(THING_RINGING, jsonRing);
							}
							synchronized (jsonPanic) {
								System.out.println("BOTON PRESIONADO: Panic False...");
								jsonPanic.addProperty("panic", false);
								publicarDweet(THING_PANIC, jsonPanic);
							}
							log("Alarma desactivada");
						} catch(InterruptedException ex) {
							ex.printStackTrace();
							System.err.println("Error when deactivating alarm... (Main: ALARMA_ACTIVADA - botonPresionado)");
							System.exit(1);
						}
					}
					break;

				case ALARMA_ACTIVANDO:
					alarma.intermitenciaLedVerde();
					break;

				case ALARMA_DESACTIVADA:
					if(botonPresionado) {
						try {
							alarma.activarAlarma();
							synchronized (jsonAct) {
								System.out.println("BOTON PRESIONADO: Activando...");
								jsonAct.addProperty("activated", true);
								publicarDweet(THING_ACTIVATED, jsonAct);
							}
							synchronized (jsonPanic) {
								System.out.println("BOTON PRESIONADO: Panic False...");
								jsonPanic.addProperty("panic", false);
								publicarDweet(THING_PANIC, jsonPanic);
							}
							log("Alarma activada");
						} catch(InterruptedException ex) {
							ex.printStackTrace();
							System.err.println("Error when activating alarm... (Main: ALARMA_DESACTIVADA - botonPresionado)");
							System.exit(1);
						}
					}
					
					break;

				case ALARMA_SONANDO:
					synchronized(Main.sonando) {
						Main.sonando.notify();
					}
					if(botonPresionado) {
						try {
							alarma.desactivarAlarma();
							synchronized (jsonAct) {
								System.out.println("BOTON PRESIONADO: Desactivando...");
								jsonAct.addProperty("activated", false);
								publicarDweet(THING_ACTIVATED, jsonAct);
							}
							synchronized (jsonRing) {
								System.out.println("BOTON PRESIONADO: Ringing false...");
								jsonRing.addProperty("ringing", false);
								publicarDweet(THING_RINGING, jsonRing);
							}
							synchronized (jsonPanic) {
								System.out.println("BOTON PRESIONADO: Panic False...");
								jsonPanic.addProperty("panic", false);
								publicarDweet(THING_PANIC, jsonPanic);
							}
							log("Alarma desactivada");
						} catch(InterruptedException ex) {
							ex.printStackTrace();
							System.err.println("Error while sleeping... (Main: ALARMA_SONANDO - botonPresionado)");
							System.exit(1);
						}
					}
					break;

				default: break;
			}
			botonPresionado = false;
			alarma.setEstadoAnteriorBotonActivacion();
			try {
				Thread.sleep(1);
			} catch(InterruptedException ex) {
				ex.printStackTrace();
				System.err.println("Error while sleeping... (Fin del While)");
				System.exit(1);
			}
			try {
				if(Duration.between(tiempoPublicacionSensores, Instant.now()).toMillis() > 10000) {
					JsonObject valoresSensores = new JsonObject();
					//valoresSensores.addProperty("temperature", alarma.readEstadoSensorTemeperatura());
					valoresSensores.addProperty("gas", alarma.readEstadoSensorGas());
					DweetIO.publish(THING_SENSORS, valoresSensores);
					tiempoPublicacionSensores = Instant.now();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        //thread.join();
    }
    
    private static void publicarDweet(String thingName, JsonObject json) {
		try {
			DweetIO.publish(thingName, json);
		} catch (IOException e) {
			System.out.println("Error al publicar el Dweet a "+thingName);
			e.printStackTrace();
		}
    }
    
    private static void publicarEstadoAlarma() {
    	String thing = "soa_alarma_debug";
    	JsonObject json = new JsonObject();
    	try {
    		switch(alarma.getEstado()) {
				case ALARMA_ACTIVADA:
					json.addProperty("state", "activada");
					break;
				case ALARMA_ACTIVANDO:
					json.addProperty("state", "activando");
					break;
				case ALARMA_DESACTIVADA:
					json.addProperty("state", "desactivada");
					break;
				case ALARMA_SONANDO:
					json.addProperty("state", "sonando");
					break;
				default:
					json.addProperty("state", "null");
					break;
    		}
			DweetIO.publish(thing, json);
		} catch (IOException e) {
			System.out.println("Error al publicar el Dweet a "+thing);
			e.printStackTrace();
		}
    }
    
	private static class ThreadAlarmaSonando extends Thread {
    	public ThreadAlarmaSonando(){
    		
    	}
    	@Override
    	public void run() {
    		while(true) {
    			try {
    				synchronized(Main.sonando) {
    					Main.sonando.wait();
    				}					
				} catch (InterruptedException e) {
					System.out.println("Error while waiting for lock");
					e.printStackTrace();
				}
    			while(alarma.getEstado() == Estados.ALARMA_SONANDO) {
    				try {
						alarma.sonarAlarma();
					} catch(InterruptedException ex) {
						ex.printStackTrace();
						System.err.println("Error while sleeping... (Main: ALARMA_SONANDO)");
						System.exit(1);
					}
    			}
    		}
    	}
    }
	
	private static void log(String message) {
		if(logging.size() >= MAX_LOG_SIZE) {
			logging.remove(0);
		}
		Calendar cal = Calendar.getInstance();
		String date = String.valueOf(cal.get(Calendar.DATE))+"/"+String.valueOf(cal.get(Calendar.MONTH)+1)+"/"+String.valueOf(cal.get(Calendar.YEAR))+", "+String.valueOf(cal.get(Calendar.HOUR_OF_DAY))+":"+String.valueOf(cal.get(Calendar.MINUTE))+":"+String.valueOf(cal.get(Calendar.SECOND))+" ";
		logging.add(date+message);
		String dweetContent = "";
		for(String s : logging) {
			dweetContent = dweetContent.concat(s+"|");
		}
		JsonObject json = new JsonObject();
		json.addProperty("logs", dweetContent);
		publicarDweet(THING_LOGS, json);
	}
	
	private static class ThreadEventos extends Thread {
		@Override
		public void run() {
			Dweet /*dweetAct = null,*/ dweetPanic = null, dweetChange = null;
			while(true) {
				try {
					System.out.println("Leyendo Dweets...");
					dweetPanic = DweetIO.getLatestDweet(THING_PANIC);
					dweetChange = DweetIO.getLatestDweet(THING_CHANGE_STATE);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				
				if(dweetPanic!=null && dweetPanic.getContent().get("panic").getAsBoolean() == true &&alarma.getEstado()!=Estados.ALARMA_SONANDO){
					System.out.println("Dweet: Antipanico activado");
					try {
						alarma.activarAlarma();
						synchronized (jsonAct) {
							System.out.println("DWEET PANIC: Activando...");
							jsonAct.addProperty("activated", true);
							publicarDweet(THING_ACTIVATED, jsonAct);
						}
						synchronized (jsonRing) {
							System.out.println("DWEET PANIC: Ringing true...");
							jsonRing.addProperty("ringing", true);
							publicarDweet(THING_RINGING, jsonRing);
						}
						alarma.setEstado(Estados.ALARMA_SONANDO);
						log("Presionado el boton antipanico");
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	    		}
				else if(dweetChange != null && dweetChange.getContent().get("change").getAsBoolean() == true) {
					
					if(alarma.getEstado() == Estados.ALARMA_DESACTIVADA) {
						System.out.println("Dweet: Activando alarma...");
		        		try {
							alarma.activarAlarma();
							synchronized (jsonAct) {
								System.out.println("DWEET ACTIVAR: Activando...");
								jsonAct.addProperty("activated", true);
								publicarDweet(THING_ACTIVATED, jsonAct);
							}
							log("Alarma activada desde Sistema Android");
							System.out.println("Dweet: Alarma activada");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(alarma.getEstado() == Estados.ALARMA_SONANDO || alarma.getEstado() == Estados.ALARMA_ACTIVADA) {
						System.out.println("Dweet: Desactivando alarma...");
						try {
							alarma.desactivarAlarma();
							synchronized (jsonAct) {
								System.out.println("DWEET DESACTIVAR: Desactivando...");
								jsonAct.addProperty("activated", false);
								publicarDweet(THING_ACTIVATED, jsonAct);
							}
							synchronized (jsonRing) {
								System.out.println("DWEET DESACTIVAR: Ringing False...");
								jsonRing.addProperty("ringing", false);
								publicarDweet(THING_RINGING, jsonRing);
							}
							synchronized (jsonPanic) {
								System.out.println("DWEET DESACTIVAR: Panic False...");
								jsonPanic.addProperty("panic", false);
								publicarDweet(THING_PANIC, jsonPanic);
							}
							log("Alarma desactivada desde Sistema Android");
							System.out.println("Dweet: Alarma desactivada");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					jsonChange.addProperty("change", false);
					publicarDweet(THING_CHANGE_STATE, jsonChange);
				}
				//publicarEstadoAlarma();
				try {
//					System.out.println("Durmiendo por 2 segundos...");
					Thread.sleep(2000);
				} catch(InterruptedException ex) {
					ex.printStackTrace();
					System.err.println("Error while sleeping... (Fin del While)");
					System.exit(1);
				}
			}
		}
	}
}