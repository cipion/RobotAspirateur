#ifndef COMANDEMANAGER_H
#define COMANDEMANAGER_H

#include <wiringPi.h>
#include <iostream>
#include <stdio.h>
#include <sys/time.h>
#include <time.h>
#include <stdlib.h>
//#include <sched.h>
#include <sstream>
#include <unistd.h>
#include <thread>
#include <mutex>
#include "log.h"
#include <math.h>       /* fabs */
#include "config.h"


/************************************

Classe permettant de controler les moteurs pas a pas via le GPIO du raspberry

la classe met à disposition 3 methodes :

driver : qui permet de faire tourner les deux moteurs (un thread par moteur) en fonction d'une vitesse de rotation, une direction et un nombre de pas (si = 0 alors les moteurs tournent en continu)
	- vitesse : entre -100 et 0 pour la marche arriere, entre 0 et 100 pour la marche avant
	- direction : entre -100 et -1 pour tourner à gauche et entre 1 et 100 pour tourner à droite. 0 pour aller tout droit.
	- nbPas : nombre de pas que doivent faire les moteurs
	
stopDriver : methode qui arrete les thread de pilotage des moteurs, et qui reinitialise les port du GPIO

rotationDriver : permete de faire une rotation sur place du robot de 90°, -90° ou 180° en fonction d'une vitesse de rotation des moteur et d'un nombre de pas
	- vitesse : entre -100 et 0 pour la marche arriere, entre 0 et 100 pour la marche avant
	- direction : 90, -90 ou 180°
	- nbPas : nombre de pas que doivent faire les moteurs
	
	


*************************************/

class ComandeManager
{
    public:
        ComandeManager();

        int driver();
        
		

    protected:
        



    private:
        // Configuration des pin GIPO pour le bouton démarrer
        int cmd_1_pin = 0;   //pin 
        
			
				
        //logger
        Log log;

		// Objet de configuration afin de recuperer les properties
		Config configLoader;
		typedef std::map<const std::string,std::string> cfgMap;
		cfgMap config;
		
        /********
		Methode internes
		********/
        void configrationPin();
        
};

#endif // COMANDEMANAGER_H
