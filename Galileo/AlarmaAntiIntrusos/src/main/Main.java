package main;

import java.time.Duration;
import java.time.Instant;

import alarma.*;

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

    final static int BOTON_PIN = 7;
	final static int SENSOR_PIN = 8;
	final static int BUZZER_PIN = 9;
	final static int LEDROJO_PIN = 3;
	final static int LEDVERDE_PIN = 4;
	
	final static long DEBOUNCE = 200;
	
	private static Instant tiempoBoton = Instant.now();

    public static void main(String argv[]) {
		Alarma alarma = new Alarma();
		//tiempoBoton = Instant.now();
        while (true) {
        	alarma.readEstadoActualBotonActivacion();
			if(alarma.getEstadoActualBotonActivacion() == HIGH && alarma.getEstadoAnteriorBotonActivacion() == LOW && Duration.between(tiempoBoton,Instant.now()).toMillis() > DEBOUNCE) {
				try {
					if(alarma.getAlarmaEncendida() == true) {
						//alarma.setAlarmaEncendida(false);
						alarma.desactivarAlarma();
					}
					else {
						//alarma.setAlarmaEncendida(true);
						alarma.activarAlarma();
					}
				} catch(InterruptedException ex) {
					ex.printStackTrace();
					System.err.println("Error while sleeping... (setAlarmaEncendida)");
					System.exit(1);
				}
				tiempoBoton = Instant.now();
			}
			
			alarma.intermitenciaLedVerde();
			
			while(alarma.getAlarmaEncendida() == true && alarma.getEstadoSensorMovimiento() == HIGH) {
				try {
					alarma.sonarAlarma();
				} catch (InterruptedException ex) {
					ex.printStackTrace();
					System.err.println("Error while sleeping... (sonarAlarma)");
					System.exit(1);
				}
				
				alarma.readEstadoActualBotonActivacion();
				if (alarma.getEstadoActualBotonActivacion() == HIGH  && alarma.getEstadoAnteriorBotonActivacion() == LOW && Duration.between(tiempoBoton,Instant.now()).toMillis() > DEBOUNCE) {
					try {
						//alarma.setAlarmaEncendida(false);
						alarma.desactivarAlarma();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
						System.err.println("Error while sleeping... (setAlarmaEncendida)");
						System.exit(1);
					}
					
					tiempoBoton = Instant.now();
				}
				alarma.setEstadoAnteriorBotonActivacion();
			}
			alarma.setEstadoAnteriorBotonActivacion();
        }
    }
}