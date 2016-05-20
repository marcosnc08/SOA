package alarma;

import java.time.Duration;
import java.time.Instant;

import mraa.Dir;
import mraa.Gpio;
import mraa.Result;
import mraa.mraa;

public class Alarma {
	/* Pines asociados a la placa Galileo */
	final static int BOTONACTIVACION_PIN = 7;
	final static int SENSORMOVIMIENTO_PIN = 8;
	final static int BUZZER_PIN = 9;
	final static int LEDROJO_PIN = 3;
	final static int LEDVERDE_PIN = 4;
	
	final static int DEFAULT_BUZZ_TONE = 255;
	
	/* Miembros */
	private boolean alarmaEncendida;
	private int estadoActualBotonActivacion;
	private int estadoAnteriorBotonActivacion;
	private int estadoSensorMovimiento;
	/* GPIOs */
	private Gpio botonActivacion;
	//private Gpio botonAntipanico;
	private Gpio ledRojo;
	private Gpio ledVerde;
	private Gpio buzzer;
	private Gpio sensorMovimiento;
	/* Instantes de Tiempo */
	private Instant tiempoIntermitenciaLed;
	private Instant tiempoActivacion;
	
	/* Constructor */
	public Alarma() {
		
		alarmaEncendida = false;
		estadoActualBotonActivacion = 0;
		estadoAnteriorBotonActivacion = 1;
		tiempoIntermitenciaLed = Instant.now();
		tiempoActivacion = Instant.now();
		/* Inicializacion de los objetos GPIO */
		botonActivacion = new Gpio(BOTONACTIVACION_PIN);
        Result result = botonActivacion.dir(Dir.DIR_IN);
        if (result != Result.SUCCESS) {
        	System.out.println("Error opening Boton Activacion GPIO");
            mraa.printError(result);
            System.exit(1);
        }
		
		ledRojo = new Gpio(LEDROJO_PIN);
        result = ledRojo.dir(Dir.DIR_OUT);
        if (result != Result.SUCCESS) {
        	System.out.println("Error opening Led Rojo GPIO");
        	mraa.printError(result);
            System.exit(1);
        }
		
		ledVerde = new Gpio(LEDVERDE_PIN);
        result = ledVerde.dir(Dir.DIR_OUT);
        if (result != Result.SUCCESS) {
        	System.out.println("Error opening Led Verde GPIO");
            mraa.printError(result);
            System.exit(1);
        }
		
		buzzer = new Gpio(BUZZER_PIN);
        result = buzzer.dir(Dir.DIR_OUT);
        if (result != Result.SUCCESS) {
        	System.out.println("Error opening Buzzer GPIO");
            mraa.printError(result);
            System.exit(1);
        }
		
		sensorMovimiento = new Gpio(SENSORMOVIMIENTO_PIN);
        result = sensorMovimiento.dir(Dir.DIR_IN);
        if (result != Result.SUCCESS) {
        	System.out.println("Error opening Sensor Movimiento GPIO");
            mraa.printError(result);
            System.exit(1);
        }
		
		try {
			buzz(500);
		} catch(InterruptedException ex) {
			ex.printStackTrace();
			System.err.println("Error while sleeping... (AlarmaAntiIntrusos constructor)");
			System.exit(1);
		}
		ledRojo.write(0);
		ledVerde.write(0);
		sensorMovimiento.write(0);
	}
	
	/* Metodos */
	
	public boolean getAlarmaEncendida() {
		return alarmaEncendida;
	}
	
	public int getEstadoActualBotonActivacion() {
		return estadoActualBotonActivacion;
	}
	
	public int getEstadoAnteriorBotonActivacion() {
		return estadoAnteriorBotonActivacion;
	}
	
	public void readEstadoActualBotonActivacion() {
		estadoActualBotonActivacion = botonActivacion.read();
	}
	
	public void setEstadoAnteriorBotonActivacion() {
		estadoAnteriorBotonActivacion = estadoActualBotonActivacion;
	}
	
	public int getEstadoSensorMovimiento() {
		return estadoSensorMovimiento;
	}
	/*
	public void readEstadoSensorMovimiento() {
		estadoSensorMovimiento = sensorMovimiento.read();
	}*/
	public void activarAlarma() throws InterruptedException {
		/* Activar Alarma */
		//lock
		alarmaEncendida = true;
		//unlock
		buzz(50);
		buzz(50);
		buzz(50);		
		tiempoIntermitenciaLed = Instant.now();
		tiempoActivacion = Instant.now();
	}
	
	public void desactivarAlarma() throws InterruptedException {
		/* Desactivar Alarma */
		sensorMovimiento.write(0);
		alarmaEncendida = false;
		//Thread.sleep(100);
		buzz(200);
		buzz(200);
		ledVerde.write(0);
		ledRojo.write(0);
	}
	
	public void setAlarmaEncendida(boolean state) throws InterruptedException {
		if(state == true) {
			/* Activar Alarma */
			//lock
			alarmaEncendida = true;
			//unlock
			buzz(50);
			buzz(50);
			buzz(50);		
			tiempoIntermitenciaLed = Instant.now();
			tiempoActivacion = Instant.now();
		}
		else {
			/* Desactivar Alarma */
			sensorMovimiento.write(0);
			alarmaEncendida = false;
			//Thread.sleep(100);
			buzz(200);
			buzz(200);
			ledVerde.write(0);
			ledRojo.write(0);
		}
	}
	
	public void sonarAlarma() throws InterruptedException {
		ledRojo.write(1);
		buzz(500);
	}
	
	public void intermitenciaLedVerde() {
		if(alarmaEncendida == true) {
			//long t = Duration.between(tiempoActivacion,Instant.now()).toMillis();
			//System.out.println("Time: "+ t);
			if(Duration.between(tiempoActivacion,Instant.now()).toMillis() > 5000) {
				ledVerde.write(1);
				estadoSensorMovimiento = sensorMovimiento.read();
			}
			else {
				if(Duration.between(tiempoIntermitenciaLed,Instant.now()).toMillis() > 100) {
					if(ledVerde.read() == 1)
						ledVerde.write(0);
					else
						ledVerde.write(1);
					tiempoIntermitenciaLed = Instant.now();
				}
			}
		}
	}
	
	private void buzz(int delay, int tone) throws InterruptedException {
		if(tone > 0 && tone < 255)
			buzzer.write(tone);
		else
			buzzer.write(170);
		Thread.sleep(delay);
		buzzer.write(0);
		Thread.sleep(delay);
	}
	private void buzz(int delay) throws InterruptedException {
		buzzer.write(DEFAULT_BUZZ_TONE);
		Thread.sleep(delay);
		buzzer.write(0);
		Thread.sleep(delay);
	}
	
}
