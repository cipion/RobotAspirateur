#ifndef GESTIONCAPTEURS_H
#define GESTIONCAPTEURS_H

// distance en cm
#define DISTANCE_MIN 5

class GestionCapteurs
{
public:
    GestionCapteurs();

    int getDistanceCapeteur(int numCpt);
	
private:
	int pinTrig = 26;
	int pinEcho = 27;
	long timeout   = 500000; // 0.5 sec ~ 171 m
	long ping      = 0;
	long pong      = 0;
	float distance = 0;
};

#endif // GESTIONCAPTEURS_H
