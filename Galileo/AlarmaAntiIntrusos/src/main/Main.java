package alarma.main;

import java.time.Duration;
import java.time.Instant;

import alarma.alarmaantiintrusos.AlarmaAntiIntrusos;

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
	
	final static long DEBOUNCE = 100;
	
	private static Instant tiempoBoton;

    public static void main(String argv[]) {
		AlarmaAntiIntrusos alarma = new AlarmaAntiIntrusos();
		//tiempoBoton = Instant.now();
        while (true) {
        	alarma.readEstadoActualBotonActivacion();
			if(alarma.getEstadoActualBotonActivacion() == HIGH && alarma.getEstadoAnteriorBotonActivacion() == LOW && Duration.between(Instant.now(), tiempoBoton).toMillis() > DEBOUNCE) {
				try {
					if(alarma.getAlarmaEncendida() == true) {
						alarma.setAlarmaEncendida(false);
					}
					else {
						alarma.setAlarmaEncendida(true);
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
				alarma.sonarAlarma();
				alarma.readEstadoActualBotonActivacion();
				if (alarma.getEstadoActualBotonActivacion() == HIGH  && alarma.getEstadoAnteriorBotonActivacion() == LOW && Duration.between(Instant.now(), tiempoBoton).toMillis() > DEBOUNCE) {
					alarma.setAlarmaEncendida(false);
					tiempoBoton = Instant.now();
				}
				alarma.setEstadoAnteriorBotonActivacion();
			}
			alarma.setEstadoAnteriorBotonActivacion();
        }
    }
}
