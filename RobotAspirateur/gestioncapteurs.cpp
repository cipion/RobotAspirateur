#include "gestioncapteurs.h"


GestionCapteurs::GestionCapteurs()
{
	if (wiringPiSetup () == -1) {
		exit(EXIT_FAILURE);
	}
	
	if (setuid(getuid()) < 0) {
		perror("Dropping privileges failed.\n");
		exit(EXIT_FAILURE);
	}
	
	pinMode(pinTrig, OUTPUT);
	pinMode(pinEcho, INPUT);
}


int GestionCapteurs::getDistanceCapeteur(int numCpt)
{
// Ensure trigger is low.
	digitalWrite(TRIG, LOW);
	delay(50);
	
	// Trigger the ping.
	digitalWrite(TRIG, HIGH);
	delayMicroseconds(10); 
	digitalWrite(TRIG, LOW);
	
	// Wait for ping response, or timeout.
	while (digitalRead(ECHO) == LOW && micros() < timeout) {
	}
	
	// Cancel on timeout.
	if (micros() > timeout) {
		printf("Out of range.\n");
		return 0;
	}
	
	ping = micros();
	
	// Wait for pong response, or timeout.
	while (digitalRead(ECHO) == HIGH && micros() < timeout) {
	}
	
	// Cancel on timeout.
	if (micros() > timeout) {
		printf("Out of range.\n");
		return 0;
	}
	
	pong = micros();
	
	// Convert ping duration to distance.
	distance = (float) (pong - ping) * 0.017150;
	
	printf("Distance: %.2f cm.\n", distance);
	
    return 0;
}


