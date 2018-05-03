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

class ControlMoteur
{
    public:
        ControlMoteur();

        int driver(int vitesse, int direction, int nbPas = 0);
        int stopDriver();
		int rotationDriver(int vitesse, int direction, int nbPas);


    protected:
        //thread de control de moteur
        std::thread *thread_mot1;
        std::thread *thread_mot2;



    private:
        // moteur PaP 1
        int mot_1_pin_bobine_1_1 = 0;   //pin 17
        int mot_1_pin_bobine_1_2 = 1;   //pin 18
        int mot_1_pin_bobine_2_1 = 2;   //pin 27
        int mot_1_pin_bobine_2_2 = 3;   //pin 22
        // moteur PaP 2
        int mot_2_pin_bobine_1_1 = 4;   //PIN 23
        int mot_2_pin_bobine_1_2 = 5;   //PIN 24
        int mot_2_pin_bobine_2_1 = 6;   //PIN 25
        int mot_2_pin_bobine_2_2 = 7;   //PIN 04
        //attente entre chaque pas
        int attente_min = 1;
        int attente_max = 20;
        float attente;
        //% de la direction
        int direction;
        // variable pour arreter la commande des moteur
        bool run;
        bool t1run = false;
        bool t2run = false;


        //logger
        Log log;

		// config
		Config configLoader;
		typedef std::map<const std::string,std::string> cfgMap;
		cfgMap config;
		
        // methodes

        void reglagePinsMot1(int pin1, int pin2, int pin3, int pin4);
        void reglagePinsMot2(int pin1, int pin2, int pin3, int pin4);
        void prochainStepMot1(int numero);
        void prochainStepMot2(int numero);
        void configrationPin();
        void runMot1(int vitesse, bool *run, int nbPas);
        void runMot2(int vitesse, bool *run, int nbPas);
};

#endif // CONTROLMOTEUR_H
