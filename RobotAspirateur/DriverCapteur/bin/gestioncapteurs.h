#ifndef GESTIONCAPTEURS_H
#define GESTIONCAPTEURS_H

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
#include "libSonar.h"
#include <sys/socket.h>

// distance en cm
#define DISTANCE_MIN 5

class GestionCapteurs
{
public:
    GestionCapteurs();

    int getDistanceCapeteur(int numCpt);
	std::string detectionObstacle(  int timeout = 0);
	int stopDetection();
	
	
protected:
        //thread de control de moteur
        std::thread *thread_cpt1;
        std::thread *thread_cpt2;
        std::thread *thread_cpt3;
	
private:
	
	long timeout   = 500000; // 0.5 sec ~ 171 m
	long ping      = 0;
	long pong      = 0;
	int distance = 0;
	bool detectionCpt1 = false;
	bool detectionCpt2 = false;
	bool detectionCpt3 = false;
	
	bool th1run = false;
	bool th2run = false;
	bool th3run = false;
	
	
	std::mutex lock;
	
	// variable pour arreter la commande des moteur
        bool run = true;
	
	//logger
    Log log;
		
	// config
	Config configLoader;
	typedef std::map<const std::string,std::string> cfgMap;
	cfgMap config;
	
	void runCapteur(int numCpt, bool *detection, int *distance, bool *run);
	bool testThread();
};

#endif // GESTIONCAPTEURS_H
