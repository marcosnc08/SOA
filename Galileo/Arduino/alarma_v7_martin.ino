//#include <Wire.h>

void buzz(unsigned char t, int tone = 170);

int botonPin = 7;
int sensorPin = 8;
int buzzPin = 9;
int ledRojoPin = 3;
int ledVerdePin = 4;

int alarmaEncendida = LOW;
int botonEstadoActual = LOW;
int botonEstadoAnterior = HIGH;
int sensorEstado;

long tiempo = 0;        // the last time the output pin was toggled
long debounce = 50;   	// the debounce time, increase if the output flickers
long tiempoActivacion = 0;

void setup() {
	pinMode(botonPin, INPUT);
	pinMode(ledRojoPin, OUTPUT);
	pinMode(ledVerdePin, OUTPUT);
	pinMode(buzzPin,OUTPUT);
	pinMode(sensorPin, INPUT);
	digitalWrite(ledRojoPin, LOW);
	digitalWrite(ledVerdePin, LOW);
	buzz(500);
	//buzz(200);
	//buzz(200);
	//delay(2000);
}

void loop() {
	botonEstadoActual = digitalRead(botonPin);
	if (botonEstadoActual == HIGH && botonEstadoAnterior == LOW && millis() - tiempo > debounce) {
		if (alarmaEncendida == HIGH) {
			desactivarAlarma();
		}
		else {
			activarAlarma();
		}
		//digitalWrite(ledRojoPin, alarmaEncendida);
		tiempo = millis();    
	}
 //tiempoActivacion = millis() - tiempoActivacion;
	if(millis() - tiempoActivacion > 5000 && alarmaEncendida == HIGH) {
	  sensorEstado = digitalRead(sensorPin);
	  digitalWrite(ledVerdePin, HIGH);
	}
	while(alarmaEncendida == HIGH && sensorEstado == HIGH) {
		digitalWrite(ledRojoPin, HIGH);
		buzz(500);
		botonEstadoActual = digitalRead(botonPin);
		if (botonEstadoActual == HIGH  && botonEstadoAnterior == LOW && millis() - tiempo > debounce) {
			desactivarAlarma();
			sensorEstado = LOW;
		}
		botonEstadoAnterior = botonEstadoActual;
	}
	botonEstadoAnterior = botonEstadoActual;
}

void activarAlarma() {
	alarmaEncendida = HIGH;
	buzz(50);
	buzz(50);
	buzz(50);
	//digitalWrite(ledVerdePin, HIGH);
	//delay(5000);	//Este delay representa el tiempo que da la alarma antes de activarse y empezar a sonar.
  tiempoActivacion = millis();
}

void desactivarAlarma() {
	alarmaEncendida = LOW;
  delay(100);
	buzz(200,40);
  buzz(200,40);
	digitalWrite(ledVerdePin, LOW);
  digitalWrite(ledRojoPin, LOW);
  //tiempoActivacion = millis();
}

void buzz(unsigned char t, int tone) {
	analogWrite(buzzPin,tone);
	delay(t);
	analogWrite(buzzPin,0);
	delay(t);
}
