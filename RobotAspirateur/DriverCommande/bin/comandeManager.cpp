#include "comandeManager.h"


/** Controle d'un moteur pas a pas
PARAM 1 : vitesse en % (mini 5ms)
PARAM 2 : Direction en %
PARAM 3 : 1 sens positif, 0 sens negatif
**/


using namespace std;



ComandeManager::ComandeManager()
{
    log.info("ComandeManager : lancement du driver des commandes");
    configrationPin();
	config = configLoader.getConfig();
	
	cmd_1_pin = stoi(config.find("numpincmd1")->second);
	
	
	log.info("ComandeManager : commande 1 au pin =" + to_string(cmd_1_pin));
	
}

void ComandeManager::configrationPin()
{
	if(wiringPiSetup() == -1)
    {
        log.info("ComandeManager : Librairie Wiring PI introuvable, veuillez lier cette librairie...");
        

    }
	
    log.info("ComandeManager : configrationPin : Configuration des pins ...");

    pinMode(cmd_1_pin, INPUT);
    

    //digitalWrite(mot_1_pin_bobine_1_1, HIGH);
    log.info("ComandeManager : configrationPin : Pins GPIO configures en sortie");
}



/*******
*
********/
int ComandeManager::driver()
{
    
	int i;
	log.info("driver : Checking if processor is available...");
	if (system(NULL)) puts ("Ok");
	else exit (EXIT_FAILURE);
	
	log.info("driver : Execution de la commande d'initialisation du port GPIO (front montant)");
	i=system ("gpio edge " + to_string(cmd_1_pin) + " falling");
	log.info("driver : La valeur de retour est :" + to_string(i));
	
	log.info("driver : En attente d'un front montant de la commande ...");
	waitForInterrupt (cmd_1_pin, -1) ; // -1 pour timeout infini
	
	log.info("driver : front detecte");
  
    return 0;

}








