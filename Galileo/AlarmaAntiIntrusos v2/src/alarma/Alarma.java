package alarma;

import java.time.Duration;
import java.time.Instant;
import mraa.*;
import estados.Estados;

public class Alarma {
	/* Pines asociados a la placa Galileo */
	final static int LEDROJO_PIN = 3;
	final static int LEDVERDE_PIN = 4;
	final static int BOTONACTIVACION_PIN = 7;
	final static int SENSORMOVIMIENTO_PIN = 8;
	final static int BUZZER_PIN = 9;
	//Analogicos
	final static int SENSOR_TEMPERATURA_PIN = 2;
	final static int SENSOR_GAS_PIN = 5;
	
	final static int DEFAULT_BUZZ_TONE = 50;
	
	/* Miembros */
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
	private Aio sensorGas;
	private Aio sensorTemperatura;
	//private upm_grove.GroveTemp sensorTemperatura;
	/* Instantes de Tiempo */
	private Instant tiempoIntermitenciaLed;
	private Instant tiempoActivacion;
	
	private volatile Estados estadoAlarma;
	/* Constructor */
	public Alarma() {
		estadoAlarma = Estados.ALARMA_DESACTIVADA;
		estadoActualBotonActivacion = 0;
		estadoAnteriorBotonActivacion = 1;
		estadoSensorMovimiento = 0;
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
        
        sensorGas = new Aio(SENSOR_GAS_PIN);
        sensorTemperatura = new Aio(SENSOR_TEMPERATURA_PIN);
        
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
	
	public Estados getEstado() {
		return estadoAlarma;
	}
	public void setEstado(Estados e) {
		estadoAlarma = e;
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
	
	public int readEstadoSensorMovimiento() {
		if(estadoAlarma == Estados.ALARMA_ACTIVADA) {
			estadoSensorMovimiento = sensorMovimiento.read();
//			System.out.println("Lectura del Estado del sensor: "+estadoSensorMovimiento);
		}
		else
			estadoSensorMovimiento = 0;
		return estadoSensorMovimiento;
	}
	
	public int readEstadoSensorTemeperatura() {
		return sensorTemperatura.read()*500/1024;
	}
	
	public float readEstadoSensorGas() {
		return sensorGas.readFloat();
	}
	
	public void activarAlarma() throws InterruptedException {
		/* Activar Alarma */
		estadoAlarma = Estados.ALARMA_ACTIVANDO;
		buzz(50);
		buzz(50);
		buzz(50);		
		tiempoIntermitenciaLed = Instant.now();
		tiempoActivacion = Instant.now();
	}
	
	public void desactivarAlarma() throws InterruptedException {
		/* Desactivar Alarma */
		estadoSensorMovimiento = 0;
		estadoAlarma = Estados.ALARMA_DESACTIVADA;
		buzz(200);
		buzz(200);
		ledVerde.write(0);
		ledRojo.write(0);
	}
	
	public void sonarAlarma() throws InterruptedException {
		ledRojo.write(1);
		buzz(500);
	}
	
	public void intermitenciaLedVerde() {
		if(estadoAlarma == Estados.ALARMA_ACTIVANDO) {
			if(Duration.between(tiempoActivacion,Instant.now()).toMillis() > 10000) {
				System.out.println("ACTIVACION DE ALARMA: FINALIZADA");
				estadoAlarma = Estados.ALARMA_ACTIVADA;
				estadoSensorMovimiento = 0;
				ledVerde.write(1);
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
	
	/*private void buzz(int delay, int tone) throws InterruptedException {
		if(tone > 0 && tone < 255)
			buzzer.write(tone);
		else
			buzzer.write(170);
		Thread.sleep(delay);
		buzzer.write(0);
		Thread.sleep(delay);
	}*/
	private void buzz(int delay) throws InterruptedException {
		buzzer.write(DEFAULT_BUZZ_TONE);
		Thread.sleep(delay);
		buzzer.write(0);
		Thread.sleep(delay);
	}
	
}
//500 por la lectura/1024
