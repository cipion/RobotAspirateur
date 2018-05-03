#include "controlmoteur.h"


/** Controle d'un moteur pas a pas
PARAM 1 : vitesse en % (mini 5ms)
PARAM 2 : Direction en %
PARAM 3 : 1 sens positif, 0 sens negatif
**/


using namespace std;

ControlMoteur::ControlMoteur()
{
    log.info("ControlMoteur : lancement du controlleur des moteurs");
    configrationPin();
	config = configLoader.getConfig();
	//TODO
	attente_min = stoi(config.find("minTime")->second);
	attente_max = stoi(config.find("maxTime")->second);
	log.info("attente_min=" + to_string(attente_min) + ", attente_max=" + to_string(attente_max));
	
}

void ControlMoteur::configrationPin()
{
	if(wiringPiSetup() == -1)
    {
        log.info("Librairie Wiring PI introuvable, veuillez lier cette librairie...");
        

    }
	
    log.info("configrationPin : Configuration des pins ...");

    pinMode(mot_1_pin_bobine_1_1, OUTPUT);
    pinMode(mot_1_pin_bobine_1_2, OUTPUT);
    pinMode(mot_1_pin_bobine_2_1, OUTPUT);
    pinMode(mot_1_pin_bobine_2_2, OUTPUT);
	pinMode(mot_2_pin_bobine_1_1, OUTPUT);
    pinMode(mot_2_pin_bobine_1_2, OUTPUT);
    pinMode(mot_2_pin_bobine_2_1, OUTPUT);
    pinMode(mot_2_pin_bobine_2_2, OUTPUT);

    //digitalWrite(mot_1_pin_bobine_1_1, HIGH);
    log.info("configrationPin : Pins GPIO configures en sortie");
}



/*******
* Fonction pour faire avancer le robot en controllant les moteurs via 2 thread
* Calcul des vitesses assignées à chaque thread en fonction de la direction
********/
int ControlMoteur::driver(int vitesse, int direction, int nbPas)
{
    // pour faire tourner le robot il faut qu'un moteur est un temps d'attente plus long que le deuxiene

    int vitesse_mot1;
    int vitesse_mot2;

    log.info("driver: Nouvelle commande des moteur : vitesse=" + to_string(vitesse) +" direction=" + to_string(direction));

    if (vitesse > 100 || vitesse < -100)
    {
        log.erreur("driver : Erreur, la variable vitesse doit etre comprise entre -100 et 100, elle est de " + vitesse);
        return -1;
    }

    if (direction < 0)
    {
        vitesse_mot1 = vitesse + (vitesse * (direction/100.0));
        vitesse_mot2 = vitesse;
        log.info("driver : direction gauche a " + to_string(direction) + "\%, vitesse moteur 1 = " + to_string(vitesse_mot1) + ",  vitesse moteur 2 = " + to_string(vitesse_mot2));

    }
    else if (direction > 0)
    {
        vitesse_mot2 = vitesse - (vitesse * (direction/100.0));
        vitesse_mot1 = vitesse;
        log.info("driver : direction droite a " + to_string(direction) + "\%,vitesse moteur 1 = " + to_string(vitesse_mot1) + ", vitesse moteur 2 = " + to_string(vitesse_mot2));

    }
    else if (direction > 100 || direction < -100)
    {
        log.erreur("driver : Erreur, la variable direction n'est pas comprise entre -100 et 100, elle est de " + to_string(direction));
        return -1;
    }
    else
    {
        // direction = 0

        vitesse_mot1 = vitesse;
        vitesse_mot2 = vitesse;

        log.info("driver : la direction est de " + to_string(direction) + "% la vitesse moteur 1 est de " + to_string(vitesse_mot1) + "%, la vitesse du moteur 2 est de " + to_string(vitesse_mot2) + "%");

    }



    // threads des moteurs
	
    log.info("driver : test threads ...");
    log.info("driver : test thread 1 =" + to_string(t1run) + " et test thread 2 =" + to_string(t2run));
    if (!t1run && !t2run)
    {
        run=true;
        log.info("driver: lancement des threads, run =" + to_string(run));
        thread_mot1= new thread(&ControlMoteur::runMot1, this, vitesse_mot1, &run, nbPas);
        thread_mot2= new thread(&ControlMoteur::runMot2, this, vitesse_mot2, &run, nbPas);

        log.info("driver : etat du thread 1 =" + to_string(thread_mot1->joinable()));
        if (thread_mot1->joinable())
        {
            t1run = true;
        }
        log.info("driver : etat du thread 2 =" + to_string(thread_mot2->joinable()));
        if (thread_mot2->joinable())
        {
            t2run =true;
        }
    }
    else
    {
        log.info("driver : arret des thread pour initialisr une nouvelle commande ...");
        stopDriver();

        run=true;
        log.info("driver: lancement des threads, run =" + to_string(run));
        thread_mot1= new thread(&ControlMoteur::runMot1, this, vitesse_mot1, &run, nbPas);
        thread_mot2= new thread(&ControlMoteur::runMot2, this, vitesse_mot2, &run, nbPas);

        log.info("driver : etat du thread 1 =" + to_string(thread_mot1->joinable()));
        if (thread_mot1->joinable())
        {
            t1run = true;
        }
        log.info("driver : etat du thread 2 =" + to_string(thread_mot2->joinable()));
        if (thread_mot2->joinable())
        {
            t2run =true;
        }
    }





    return 0;

}


/*******
* Fonction pour faire pivoter le robot en controllant les moteurs via 2 thread
* Calcul des vitesses assignées à chaque thread en fonction de la direction et du nombre de pas que dois réaliser chaque thread
********/
int ControlMoteur::rotationDriver(int vitesse, int direction, int nbPas)
{
    // pour faire tourner le robot il faut qu'un moteur est un temps d'attente plus long que le deuxiene

    int vitesse_mot1;
	int vitesse_mot2;
    

    log.info("rotationDriver: Rotation du robot, Nouvelle commande des moteur : vitesse=" + to_string(vitesse) +" direction=" + to_string(direction));

    if (vitesse > 100 || vitesse < -100)
    {
        log.erreur("rotationDriver : Erreur, la variable vitesse doit etre comprise entre -100 et 100, elle est de " + vitesse);
        return 1;
    }

    if (direction == 90)
    {
		
		//TODO : calcul des pas, if -180 alors = 180
        vitesse_mot1 = vitesse;
        vitesse_mot2 = -vitesse;
		//nbPas = 50;
        log.info("rotationDriver : direction gauche a " + to_string(direction) + "\%, vitesse moteur 1 = " + to_string(vitesse_mot1) + ",  vitesse moteur 2 = " + to_string(vitesse_mot2) + " et nbPas=" + to_string(nbPas));

    }
    else if (direction == -90)
    {
		//TODO : calcul des pas
        vitesse_mot2 = -vitesse;
        vitesse_mot1 = vitesse;
		//nbPas=50;
        log.info("rotationDriver : direction droite a " + to_string(direction) + "\%,vitesse moteur 1 = " + to_string(vitesse_mot1) + ", vitesse moteur 2 = " + to_string(vitesse_mot2) + " et nbPas=" + to_string(nbPas));

    }
	else if (direction == -180 || direction == 180)
    {
		//TODO : calcul des pas
        vitesse_mot2 = -vitesse;
        vitesse_mot1 = vitesse;
		//nbPas=100;
        log.info("rotationDriver : direction droite a " + to_string(direction) + "\%,vitesse moteur 1 = " + to_string(vitesse_mot1) + ", vitesse moteur 2 = " + to_string(vitesse_mot2) + " et nbPas=" + to_string(nbPas));

    }
    else if (direction > 180 || direction < -180)
    {
        log.erreur("rotationDriver : Erreur, la variable direction n'est pas comprise entre -180 et 180, elle est de " + to_string(direction));
        return 1;
    }
    else
    {
        // direction = 0

        log.info("rotationDriver : Erreur la direction est de " + to_string(direction) + "°, la valeur doit etre de 90, -90 ou 180");
		return 1;
    }



    // threads des moteurs
    log.info("rotationDriver : test threads ...");
    log.info("rotationDriver : test thread 1 =" + to_string(t1run) + " et test thread 2 =" + to_string(t2run));
    if (!t1run && !t2run)
    {
        run=true;
        log.info("rotationDriver: lancement des threads, run =" + to_string(run) + " avec " + to_string(nbPas) + " pas");
        thread_mot1= new thread(&ControlMoteur::runMot1, this, vitesse_mot1, &run, nbPas);
        thread_mot2= new thread(&ControlMoteur::runMot2, this, vitesse_mot2, &run, nbPas);

        log.info("rotationDriver : etat du thread 1 =" + to_string(thread_mot1->joinable()));
        if (thread_mot1->joinable())
        {
            t1run = true;
        }
        log.info("rotationDriver : etat du thread 2 =" + to_string(thread_mot2->joinable()));
        if (thread_mot2->joinable())
        {
            t2run =true;
        }
    }
    else
    {
        log.info("rotationDriver : arret des thread pour initialisr une nouvelle commande ...");
        stopDriver();

        run=true;
        log.info("rotationDriver: lancement des threads, run =" + to_string(run) + " avec " + to_string(nbPas) + " pas");
        thread_mot1= new thread(&ControlMoteur::runMot1, this, vitesse_mot1, &run, nbPas);
        thread_mot2= new thread(&ControlMoteur::runMot2, this, vitesse_mot2, &run, nbPas);

        log.info("rotationDriver : etat du thread 1 =" + to_string(thread_mot1->joinable()));
        if (thread_mot1->joinable())
        {
            t1run = true;
        }
        log.info("rotationDriver : etat du thread 2 =" + to_string(thread_mot2->joinable()));
        if (thread_mot2->joinable())
        {
            t2run =true;
        }
    }





    return 0;

}




int ControlMoteur::stopDriver()
{
    run = false;

    log.info("stopDriver : demande d'arret des thread, run =" + to_string(run));

    if (t1run)
    {
        log.info("stopDriver : thread 1 is running ...");
        if ( thread_mot1->joinable())
        {
            log.info("stopDriver : arret du thread 1 en cours ...");
            thread_mot1->join();
            t1run=false;
            log.info("stopDriver : thread du moteur 1 arrete");
        }
    }

    if (t2run)
    {
        log.info("stopDriver : thread 2 is running ...");
        if ( thread_mot2->joinable())
        {
            log.info("stopDriver : arret du thread 2 en cours ...");
            thread_mot2->join();
            t2run=false;
            log.info("stopDriver : thread du moteur 2 arrete");
        }
    }
	
	log.info("stopDriver : reinitialisation des sortie gpio a 0");
	digitalWrite(mot_1_pin_bobine_1_1, LOW);
	digitalWrite(mot_1_pin_bobine_1_2, LOW);
	digitalWrite(mot_1_pin_bobine_2_1, LOW);
	digitalWrite(mot_1_pin_bobine_2_2, LOW);
	
	digitalWrite(mot_2_pin_bobine_1_1, LOW);
	digitalWrite(mot_2_pin_bobine_1_2, LOW);
	digitalWrite(mot_2_pin_bobine_2_1, LOW);
	digitalWrite(mot_2_pin_bobine_2_2, LOW);



    return 0;
}




void ControlMoteur::runMot1(int vitesse, bool *run, int nbPas)
{
    int i =0;
	unsigned int tmpV = 0;
	
    log.info("runMot1 : thread du moteur 1 lance avec la vitesse =" + to_string(vitesse));
    if (vitesse == 0)
    {
        log.info("runMot1 : vitesse nulle, fin du thread");

    }
    else
    {
		tmpV = fabs(vitesse);
		attente = attente_min + ((attente_max - attente_min) * (1 -(tmpV/100.0)));
			
		log.info("runMot1 : sleep time calcule a " + to_string((int) attente) + "ms");
        


        while (*run)
        {
            //log.info("runMot1 : step = " + to_string(i % 4) );
			if (vitesse > 0)
			{
				this->prochainStepMot1(i % 4);
			}
			else if (vitesse < 0)
			{
				this->prochainStepMot1(3 - (i % 4));
			}
			
            this_thread::sleep_for(std::chrono::milliseconds((int) attente));
            i++;
			
			if (i == nbPas)
			{
				log.info("runMot1 : fin de boucle, nombre de pas validé :" + to_string(i));
				*run = false;
			}
        }

    }


}





void ControlMoteur::prochainStepMot1(int numero)
{

   if (numero == 0)
        reglagePinsMot1(HIGH, LOW, LOW, LOW);
    //if (numero == 1)
    //    reglagePinsMot1(HIGH, LOW, HIGH, LOW);
    if (numero == 1)
        reglagePinsMot1(LOW, LOW, HIGH, LOW);
   // if (numero == 3)
    //    reglagePinsMot1(LOW, HIGH, HIGH, LOW);
    if (numero == 2)
        reglagePinsMot1(LOW, HIGH, LOW, LOW);
    //if (numero == 5)
    //    reglagePinsMot1(LOW, HIGH, LOW, HIGH);
    if (numero == 3)
        reglagePinsMot1(LOW, LOW, LOW, HIGH);
    //if (numero == 7)
    //    reglagePinsMot1(HIGH, LOW, LOW, HIGH);

}

void ControlMoteur::reglagePinsMot1(int pin1, int pin2, int pin3, int pin4)
{
    // HIGH ou LOW

    digitalWrite(mot_1_pin_bobine_1_1, pin1);
    digitalWrite(mot_1_pin_bobine_1_2, pin2);
    digitalWrite(mot_1_pin_bobine_2_1, pin3);
    digitalWrite(mot_1_pin_bobine_2_2, pin4);

}





void ControlMoteur::runMot2(int vitesse, bool *run, int nbPas)
{
    int i =0;
	unsigned int tmpV = 0;
	
    log.info("runMot2 : thread du moteur 2 lance avec la vitesse =" + to_string(vitesse));
    if (vitesse == 0)
    {
        log.info("runMot2 : vitesse nulle, fin du thread");

    }
    else
    {
		tmpV = fabs(vitesse);
        attente = attente_min + ((attente_max - attente_min) * (1 - (tmpV/100.0)));
        log.info("runMot2 : sleep time calcule a " + to_string((int) attente) + "ms");

        while (*run)
        {
            if (vitesse > 0)
			{
				this->prochainStepMot2(i % 4);
			}
			else if (vitesse < 0)
			{
				this->prochainStepMot2(3 - (i % 4));
			}
			
            //log.info("runMot2 : step =" + (i % 4) );
            this_thread::sleep_for(std::chrono::milliseconds((int)attente));
            i++;
			
			if (i == nbPas)
			{
				log.info("runMot2 : fin de boucle, nombre de pas validé :" + to_string(i));
				*run = false;
			}

        }

    }




}

void ControlMoteur::prochainStepMot2(int numero)
{

    if (numero == 0)
        reglagePinsMot2(HIGH, LOW, LOW, LOW);
    
    if (numero == 1)
        reglagePinsMot2(LOW, LOW, HIGH, LOW);
   
    if (numero == 2)
        reglagePinsMot2(LOW, HIGH, LOW, LOW);
    
    if (numero == 3)
        reglagePinsMot2(LOW, LOW, LOW, HIGH);
    

}

void ControlMoteur::reglagePinsMot2(int pin1, int pin2, int pin3, int pin4)
{
    // HIGH ou LOW

    digitalWrite(mot_2_pin_bobine_1_1, pin1);
    digitalWrite(mot_2_pin_bobine_1_2, pin2);
    digitalWrite(mot_2_pin_bobine_2_1, pin3);
    digitalWrite(mot_2_pin_bobine_2_2, pin4);

}






