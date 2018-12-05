#include "gestioncapteurs.h"

using namespace std;

GestionCapteurs::GestionCapteurs()
{
	log.info("GestionCapteurs : initialisation du driver des capteurs...");
	if (wiringPiSetup () == -1) {
		exit(EXIT_FAILURE);
	}
	
	config = configLoader.getConfig();
	
	
}


int GestionCapteurs::getDistanceCapeteur(int numCpt)
{
	return 0;
}



string GestionCapteurs::detectionObstacle( int timeout)
{
	log.info("detectionObstacle : lancement de la detection d'obstacle");
	run = true;
	detectionCpt1=false;
	detectionCpt2=false;
	detectionCpt3=false;
	unsigned int timeoutMillis = timeout * 1000;
	unsigned int startmilliSeconde = 0;
	
	
	thread_cpt1= new thread(&GestionCapteurs::runCapteur, this, 1, &detectionCpt1, &distance, &run);
	thread_cpt2= new thread(&GestionCapteurs::runCapteur, this, 2, &detectionCpt2, &distance, &run);
	thread_cpt3= new thread(&GestionCapteurs::runCapteur, this, 3, &detectionCpt3, &distance, &run);
	testThread();
	
	string orientationCpt ="";
	
	log.info("detectionObstacle : Attente de la detection d'obstacle par les thread...");
	
	startmilliSeconde = millis();
	
	if ( timeout == 0)
	{
		log.info("detectionObstacle : pas de timeout, attente de detection");
		
		while(detectionCpt1 == false && detectionCpt2 == false && detectionCpt3 == false && run == true) 
		{
			
		}
	}
	else
	{
		log.info("detectionObstacle : attente detection ou timeout de " + to_string(timeout) + "secondes");
		while(detectionCpt1 == false && detectionCpt2 == false && detectionCpt3 == false && (timeoutMillis > (millis() - startmilliSeconde)) && run == true ) {}
	}
	
	if (run == true)
	{
		stopDetection();
	}
	
	
	
	
	if (detectionCpt1 == true)
	{
		orientationCpt = "GAUCHE";
		log.info("detectionObstacle : Detecteur gauche");
		log.info("detectionObstacle : Detection d'un capteur avec la distance de " + to_string(distance));
	}
	else if (detectionCpt2 == true)
	{
		orientationCpt = "CENTRE";
		log.info("detectionObstacle : Detecteur centre");
		log.info("detectionObstacle : Detection d'un capteur avec la distance de " + to_string(distance));
	}
	else if (detectionCpt3 == true)
	{
		orientationCpt = "DROITE";
		log.info("detectionObstacle : Detecteur droite");
		log.info("detectionObstacle : Detection d'un capteur avec la distance de " + to_string(distance));
	}
	else if (timeout == 0)
	{
		orientationCpt = "ARRET";
		log.info("detectionObstacle : Arret");
	}else
	{
		orientationCpt = "TIMEOUT";
		log.info("detectionObstacle : Timeout");
	}
	
	
	
	return orientationCpt + ";" + to_string(distance);
}

bool GestionCapteurs::testThread()
{

        if ( thread_cpt1->joinable())
        {
            th1run = true;
            log.info("testThread : thread du capteur 1 fonctionne");
        }
    
        if ( thread_cpt2->joinable())
        {
            th2run = true;
            log.info("testThread : thread du capteur 2 fonctionne");
        }
		
        if ( thread_cpt3->joinable())
        {
            th3run = true;
            log.info("testThread : thread du capteur 2 fonctionne");
        }
}

int GestionCapteurs::stopDetection()
{

	log.info("stopDetection : demande d'arret des thread, run =" + to_string(run));
	
	if (run)
	{
		log.info("stopDetection : les thread fontionne, arret en cours");
			run=false;
		
		if (th1run)
		{
			log.info("stopDetection : thread 1 is running ...");
			if ( thread_cpt1->joinable())
			{
				th1run = false;
				log.info("stopDetection : arret du thread 1 en cours ...");
				thread_cpt1->join();
				
				log.info("stopDetection : thread du capteur 1 arrete");
			}
		}
		else
		{
			log.info("stopDetection : thread 1 isn't running");
		}
		
		if (th2run)
		{
			log.info("stopDetection : thread 2 is running ...");
			if ( thread_cpt2->joinable())
			{
				th2run = false;
				log.info("stopDetection : arret du thread 2 en cours ...");
				thread_cpt2->join();
				
				log.info("stopDetection : thread du capteur 2 arrete");
			}
		}
		else
		{
			log.info("stopDetection : thread 2 isn't running");
		}
	
		if (th3run)
		{		
			log.info("stopDetection : thread 3 is running ...");
			if ( thread_cpt3->joinable())
			{
				th3run = false;
				log.info("stopDetection : arret du thread 3 en cours ...");
				thread_cpt3->join();
				
				log.info("stopDetection : thread du capteur 3 arrete");
			}
		}
		else
		{
			log.info("stopDetection : thread 3 isn't running");
		}
	}
	else
	{
		log.info("stopDetection : les thread sont deja arretés");
	}
	
	return 1;
}

void GestionCapteurs::runCapteur(int numCpt, bool *detection, int *distance, bool *run)
{
	int pinTrig = stoi(config.find("pinTrigCatpeur" + to_string(numCpt))->second);
	int pinEcho = stoi(config.find("pinEchoCapteur" + to_string(numCpt))->second);
	int timeout = stoi(config.find("timeout")->second);
	int distanceMaxDetectee = stoi(config.find("distanceMaxDetectee")->second);
	int tempsRafraichissement = stoi(config.find("tempsRafraichissement")->second);
	Sonar sonar;
	
	
	log.info("runCapteur" + to_string(numCpt) + " : running with pinTrig" + to_string(numCpt) + "=" + to_string(pinTrig) + ", pinEcho " + to_string(numCpt) + "=" + to_string(pinEcho) );
	int tmpDist = 400;
	 sonar.init(pinTrig, pinEcho);
	
	
	
	do
	{
		lock.lock();
		//cout << "capteur " << numCpt << " run =" << *run << endl;
		tmpDist = sonar.distance(timeout);
		lock.unlock();
		this_thread::sleep_for(std::chrono::milliseconds((int) tempsRafraichissement));
		
		if (*run == false)
		{
			cout << "runCapteur" + to_string(numCpt) + " : run = false" << endl;
			break;
		}
			
	}while(tmpDist > distanceMaxDetectee || tmpDist <= 0 );
	
	if (*run==true)
	{
		*distance = tmpDist;
	    
		*detection = true;
		log.info("runCapteur" + to_string(numCpt) + " : distance=" + to_string(tmpDist));
	}
	else
	{
		log.info("runCapteur" + to_string(numCpt) + " : fin du thread car 'run'=" + to_string(*run));
	}
   
	

}


