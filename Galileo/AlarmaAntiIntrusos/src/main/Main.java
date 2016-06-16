package main;

import java.time.Duration;
import java.time.Instant;

import alarma.Alarma;

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
	final static int LOW = 0;
	final static int HIGH = 1;
//  final static int BOTON_PIN = 7;
//	final static int SENSOR_PIN = 8;
//	final static int BUZZER_PIN = 9;
//	final static int LEDROJO_PIN = 3;
//	final static int LEDVERDE_PIN = 4;
//	final static int SENSOR_GAS_PIN = 10;
	
	final static long DEBOUNCE = 200;
	
	private static Instant tiempoBoton = Instant.now();
	private static boolean botonPresionado = false;
	
    public static void main(String argv[]) {
		Alarma alarma = new Alarma();
        while (true) {
        	System.out.println("Sensor Gas: "+alarma.readEstadoSensorGas());
        	alarma.readEstadoActualBotonActivacion();
			if(alarma.getEstadoActualBotonActivacion() == HIGH && alarma.getEstadoAnteriorBotonActivacion() == LOW && Duration.between(tiempoBoton,Instant.now()).toMillis() > DEBOUNCE) {
				botonPresionado = true;
				tiempoBoton = Instant.now();
			}

			switch(alarma.getEstado()) {
				case ALARMA_ACTIVADA:
					if(alarma.readEstadoSensorMovimiento() == HIGH) {
						try {
							alarma.sonarAlarma();
						}
						catch(InterruptedException ex) {
							ex.printStackTrace();
							System.err.println("Error while sleeping... (Main: ALARMA_ACTIVADA)");
							System.exit(1);
						}
					}
					if(botonPresionado) {
						try {
							alarma.desactivarAlarma();
						} catch(InterruptedException ex) {
							ex.printStackTrace();
							System.err.println("Error while sleeping... (Main: ALARMA_ACTIVADA - botonPresionado)");
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
						} catch(InterruptedException ex) {
							ex.printStackTrace();
							System.err.println("Error while sleeping... (Main: ALARMA_DESACTIVADA)");
							System.exit(1);
						}
					}
					
					break;

				case ALARMA_SONANDO:
					try {
						alarma.sonarAlarma();
					} catch(InterruptedException ex) {
						ex.printStackTrace();
						System.err.println("Error while sleeping... (Main: ALARMA_SONANDO)");
						System.exit(1);
					}
					if(botonPresionado) {
						try {
							alarma.desactivarAlarma();
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
        }
    }
}
