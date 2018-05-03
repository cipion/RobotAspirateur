#ifndef CONTROLMOTEUR_H
#define CONTROLMOTEUR_H

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

class ControlMoteur
{
    public:
        ControlMoteur();

        int driver(int vitesse, int direction, int nbPas = 0);
        std::string stopDriver();
		void resetPin();
		int rotationDriver(int vitesse, int direction, int nbPas);


    protected:
        //thread de control de moteur
        std::thread *thread_mot1;
        std::thread *thread_mot2;



    private:
        // Configuration des pin GIPO pour le moteur PaP 1
        int mot_1_pin_bobine_1_1 = 0;   //pin 17
        int mot_1_pin_bobine_1_2 = 1;   //pin 18
        int mot_1_pin_bobine_2_1 = 2;   //pin 27
        int mot_1_pin_bobine_2_2 = 3;   //pin 22
		
        // Configuration des pin GIPO pour le moteur PaP 2
        int mot_2_pin_bobine_1_1 = 4;   //PIN 23
        int mot_2_pin_bobine_1_2 = 5;   //PIN 24
        int mot_2_pin_bobine_2_1 = 6;   //PIN 25
        int mot_2_pin_bobine_2_2 = 7;   //PIN 04
		
        //attente entre chaque pas, ca defini la vitesse min et max, la valeur indiqué ici est inutile car elle sont configurer dans le constructeur via les properties
        int attente_min = 1;
        int attente_max = 20;
        float attente;
		
		
        //% de la direction
        int direction;
		
        // variable pour arreter la commande des moteur, arret des boucle des threads
        bool run;
		
		// variable d'etat des thread de moteur 1 et 2
        bool t1run = false;
        bool t2run = false;

		unsigned int nbPasParcoursMot1 =0;
		unsigned int nbPasParcoursMot2 =0;
		
		
        //logger
        Log log;

		// Objet de configuration afin de recuperer les properties
		Config configLoader;
		typedef std::map<const std::string,std::string> cfgMap;
		cfgMap config;
		
        /********
		Methode internes
		********/

        void reglagePinsMot1(int pin1, int pin2, int pin3, int pin4);
        void reglagePinsMot2(int pin1, int pin2, int pin3, int pin4);
        void prochainStepMot1(int numero);
        void prochainStepMot2(int numero);
        void configrationPin();
        void runMot1(int vitesse, bool *run, int nbPas);
        void runMot2(int vitesse, bool *run, int nbPas);
};

#endif // CONTROLMOTEUR_H
