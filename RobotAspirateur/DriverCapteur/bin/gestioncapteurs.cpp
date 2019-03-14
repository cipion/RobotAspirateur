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
	log.info("GestionCapteurs : detectionObstacle : lancement de la detection d'obstacle");
	run = true;
	detectionCpt1=false;
	detectionCpt2=false;
	detectionCpt3=false;
	unsigned int timeoutMillis = timeout * 1000;
	unsigned int startmilliSeconde = 0;
	int tempsRafraichissement = stoi(config.find("tempsRafraichissement")->second);
	
	do
	{
		//testThread();
		this_thread::sleep_for(std::chrono::milliseconds(100));
	}while ( th1run == true && th2run == true && th3run == true);
	
	log.info("GestionCapteurs : detectionObstacle : Creation des thread");
	
	
	thread_cpt1= new thread(&GestionCapteurs::runCapteur, this, 1, &detectionCpt1, &distance, &run);
	thread_cpt2= new thread(&GestionCapteurs::runCapteur, this, 2, &detectionCpt2, &distance, &run);
	thread_cpt3= new thread(&GestionCapteurs::runCapteur, this, 3, &detectionCpt3, &distance, &run);
	testThread();
	
	string orientationCpt ="";
	
	log.info("GestionCapteurs : detectionObstacle : Attente de la detection d'obstacle par les thread...");
	
	startmilliSeconde = millis();
	
	if ( timeout == 0)
	{
		log.info("GestionCapteurs : detectionObstacle : pas de timeout, attente de detection");
		
		while(detectionCpt1 == false && detectionCpt2 == false && detectionCpt3 == false && run == true) 
		{
			this_thread::sleep_for(std::chrono::milliseconds((int) tempsRafraichissement));
		}
	}
	else
	{
		log.info("GestionCapteurs : detectionObstacle : attente detection ou timeout de " + to_string(timeout) + " secondes");
		while(detectionCpt1 == false && detectionCpt2 == false && detectionCpt3 == false && (timeoutMillis > (millis() - startmilliSeconde)) && run == true ) 
		{
			this_thread::sleep_for(std::chrono::milliseconds((int) tempsRafraichissement));
		}
	}
	
	/*if (run == true)
	{
		stopDetection();
	}*/
	
	
	
	
	if (detectionCpt1 == true)
	{
		orientationCpt = "GAUCHE";
		log.info("GestionCapteurs : detectionObstacle : Detecteur gauche");
	}
	else if (detectionCpt2 == true)
	{
		orientationCpt = "CENTRE";
		log.info("GestionCapteurs : detectionObstacle : Detecteur centre");
	}
	else if (detectionCpt3 == true)
	{
		orientationCpt = "DROITE";
		log.info("GestionCapteurs : detectionObstacle : Detecteur droite");
	}
	else if (timeout == 0)
	{
		orientationCpt = "ARRET";
		log.info("GestionCapteurs : detectionObstacle : Arret");
	}else
	{
		orientationCpt = "TIMEOUT";
		log.info("GestionCapteurs : detectionObstacle : Timeout");
	}
	
	log.info("GestionCapteurs : detectionObstacle : Detection d'un capteur avec la distance de " + to_string(distance));
	
	return orientationCpt + ";" + to_string(distance);
}

bool GestionCapteurs::testThread()
{

        if ( thread_cpt1->joinable())
        {
            th1run = true;
            log.info("GestionCapteurs : testThread : thread du capteur 1 fonctionne");
        }
		else
		{
			th1run = false;
			log.info("GestionCapteurs : testThread : thread du capteur 1 arreté");
		}
    
        if ( thread_cpt2->joinable())
        {
            th2run = true;
            log.info("GestionCapteurs : testThread : thread du capteur 2 fonctionne");
        }
		else
		{
			th2run = false;
			log.info("GestionCapteurs : testThread : thread du capteur 2 arreté");
		}
		
        if ( thread_cpt3->joinable())
        {
            th3run = true;
            log.info("GestionCapteurs : testThread : thread du capteur 2 fonctionne");
        }
		else
		{
			th3run = false;
			log.info("GestionCapteurs : testThread : thread du capteur 3 arreté");
		}
}

int GestionCapteurs::stopDetection()
{

	log.info("GestionCapteurs : stopDetection : demande d'arret des thread, run =" + to_string(run));
	
	if (run)
	{
		log.info("GestionCapteurs : stopDetection : les thread fontionne, arret en cours");
			run=false;
		
		if (th1run)
		{
			log.info("GestionCapteurs : stopDetection : thread 1 is running ...");
			if ( thread_cpt1->joinable())
			{
				th1run = false;
				log.info("GestionCapteurs : stopDetection : arret du thread 1 en cours ...");
				thread_cpt1->join();
				
				log.info("GestionCapteurs : stopDetection : thread du capteur 1 arrete");
			}
		}
		else
		{
			log.info("GestionCapteurs : stopDetection : thread 1 isn't running");
		}
		
		if (th2run)
		{
			log.info("GestionCapteurs : stopDetection : thread 2 is running ...");
			if ( thread_cpt2->joinable())
			{
				th2run = false;
				log.info("GestionCapteurs : stopDetection : arret du thread 2 en cours ...");
				thread_cpt2->join();
				
				log.info("GestionCapteurs : stopDetection : thread du capteur 2 arrete");
			}
		}
		else
		{
			log.info("GestionCapteurs : stopDetection : thread 2 isn't running");
		}
	
		if (th3run)
		{		
			log.info("GestionCapteurs : stopDetection : thread 3 is running ...");
			if ( thread_cpt3->joinable())
			{
				th3run = false;
				log.info("GestionCapteurs : stopDetection : arret du thread 3 en cours ...");
				thread_cpt3->join();
				
				log.info("GestionCapteurs : stopDetection : thread du capteur 3 arrete");
			}
		}
		else
		{
			log.info("GestionCapteurs : stopDetection : thread 3 isn't running");
		}
	}
	else
	{
		log.info("GestionCapteurs : stopDetection : les thread sont deja arretés");
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
	
	
	log.info("GestionCapteurs : runCapteur" + to_string(numCpt) + " : running with pinTrig" + to_string(numCpt) + "=" + to_string(pinTrig) + ", pinEcho " + to_string(numCpt) + "=" + to_string(pinEcho) );
	int tmpDist = 400;
	 sonar.init(pinTrig, pinEcho);
	
	
	
	do
	{
		if (*run == false)
		{
			log.info("GestionCapteurs : runCapteur" + to_string(numCpt) + " : run = false");
			break;
		}
		log.info("GestionCapteurs : runCapteur " + to_string(numCpt) + " : wait");
		lock.lock();
		log.info("GestionCapteurs : runCapteur " + to_string(numCpt) + " : lock , run =" + to_string(*run));
		
				
		
		//this_thread::sleep_for(std::chrono::milliseconds((int) timeout));
		tmpDist = sonar.distance(timeout, numCpt);
		lock.unlock();
		this_thread::sleep_for(std::chrono::milliseconds((int) tempsRafraichissement));
		log.info("GestionCapteurs : runCapteur " + to_string(numCpt) + " : unlock");
		
			
	}while(tmpDist > distanceMaxDetectee || tmpDist <= 0 );
	
	if (*run==true)
	{
		*distance = tmpDist;
	    
		*detection = true;
		log.info("GestionCapteurs : runCapteur" + to_string(numCpt) + " : distance=" + to_string(tmpDist));
	}
	else
	{
		log.info("GestionCapteurs : runCapteur" + to_string(numCpt) + " : fin du thread car 'run'=" + to_string(*run));
	}
   
	

}


