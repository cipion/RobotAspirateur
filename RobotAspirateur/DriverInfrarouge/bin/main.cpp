#include <string.h>
#include <unistd.h>
#include <stdio.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <iostream>
#include <fstream>
#include <strings.h>
#include <stdlib.h>
#include <string>
#include <pthread.h>
#include <mutex>
#include "log.h"
#include "gestioncapteurs.h"
#include <regex>
#include <signal.h>

#define PORT 1201

/******
*
*	Programme de pilotage des 2 moteurs pas à pas pour faire avancer le robot aspirateur
*	les fichiers sources et le programme compilé sont dans le dossier binary_function
*	
*	Le driver recoit des commande via des sockets, on peut demander de faire tourner les moteur en fonction :
*	- une vitesse entre -100 et 0 pour la marche arriere ou entre 0 et 100 pour la marche avant (c'est un pourcentage)
*	- une direction, entre -100 et 1 pour tourner à gauche OU entre 1 et 100 pour tourner à droite (0 = tout droit)
*	- un nombre de pas, si > à 0 le moteur fera le nombre de pas indiqué sinon si = à 0 le moteurs tournent à l'infinie
*
*	la configuration du host et du port de connexion au serveur de socket se trouve dans le fichier de configuration DriverMoteur.properties du dossier "config"
*	le message à envoyer au serveur est sous la forme : COMMANDE;VITESSE;DIRECTION;NBPAS
*	- COMMANDE = start, stop ou rotation
*	- VITESSE = -100 à 100
*	- DIRECTION = -100 à 100
*	- NBPAS = 0 à valeur max int
*
*	Commande pour conpiler le programme :
*	g++ -std=c++11 -lwiringPi -lpthread  maincapteurs.cpp log.cpp config.cpp gestioncapteurs.cpp libSonar.cpp -o CapteurRobotAspirateur
*
********/

using namespace std;

void *task1(void *);



//Drapeau d'arrêt du programme. Sa valeur d'origine est 0. pour l'instant c'est pas utile
bool stop = false;

// le coeur du programme, c'est l'objet qui commande le GPIO pour faire tourner les moteurs en fonction de vitesse, direction, nb de pas
GestionCapteurs capteur;

// regex pour valider le bon format de la commande
regex const pattern { "^[a-zA-Z]\{0,4}(;)[0-9](;)[0-9]\{1,4}$" };

string message;
bool run=true;

char bufferCopie[256];

std::mutex lockt;

/***********************************
* Fonction de reception des signaux.
* Si le signal reçu est SIGINT Ctl^c, alors arrete la commande du GPIO et on quitte le programme.
* 
************************************/

void signalHandler(int signal){
	
	// Fonction pour afficher et enregister les log
	Log log;

	if(signal==SIGINT){
		
		log.info("Maincapteurs : signalHandler : Signal Ctrl^c reçu\n");
		stop=true;
		if (run)
		{
			log.info("Maincapteurs : signalHandler : arret de la detection necessaire");
			capteur.stopDetection();
		}
		
		exit(0);
	}
}


/******************
* Fonction main qui initialise le driver, lance le serveur de socket, et gere les commande recu via les socket pour piloter le driver
*
*
********************/
int main(int argc, char* argv[])
{
	// Fonction pour afficher et enregister les log
	Log log;
	
	// variable permetant de faire une boucle pour ecouter les demande de connexion client via les socket
    bool run=true;
	
	// le bordel pour le serveur de socket, fuyez pauvre fo
    int pId, portNo, listenFd;
    int connFd;
    bool loop = false;
    struct sockaddr_in svrAdd, clntAdd;
    
	// message de reception et emission du client socket
    string retourClient;
	
	char buffer[256];

	log.info("Maincapteurs : main : Debut du programme");
    log.space();
	
    pthread_t threadA[3];
    
	
	/**********************************************************
	MISE EN PLACE DE LA GESTION DU SIGNAL CONTROL^C (SIGINT)
	Pas touche ca fonctionne !!!!!!!!!!!!!!!!!
	**********************************************************/
	//Structure pour l'enregistrement d'une action déclenchée lors de la reception d'un signal.
	struct sigaction action, oldAction;
 
	action.sa_handler = signalHandler;	//La fonction qui sera appellé est signalHandler
 
	//On vide la liste des signaux bloqués pendant le traitement
	//C'est à dire que si n'importe quel signal (à part celui qui est traité)
	//est declenché pendant le traitement, ce traitement sera pris en compte.
	sigemptyset(&action.sa_mask);
 
	//Redémarrage automatique Des appels systêmes lents interrompus par l'arrivée du signal.
	action.sa_flags = SA_RESTART;
 
	//Mise en place de l'action pour le signal SIGINT, l'ancienne action est sauvegardé dans oldAction
	sigaction(SIGINT, &action, &oldAction);
	/*************************** Fin de pas touche ******************************/

	 log.info("Maincapteurs : main : création du socket");
	
	/********************************
	Demarrage du serveur de socket
	*******************************/
	
    
    
    portNo = PORT;
    
   
    
    //create socket
    listenFd = socket(AF_INET, SOCK_STREAM, 0);
    
    if(listenFd < 0)
    {
        log.erreur("Maincapteurs : main : Cannot open socket");
        return 0;
    }
    
    bzero((char*) &svrAdd, sizeof(svrAdd));
    
    svrAdd.sin_family = AF_INET;
    svrAdd.sin_addr.s_addr = INADDR_ANY;
    svrAdd.sin_port = htons(portNo);
    
    //bind socket
    if(bind(listenFd, (struct sockaddr *)&svrAdd, sizeof(svrAdd)) < 0)
    {
        log.erreur("Maincapteurs : main : Cannot bind");
        return 0;
    }
    
    listen(listenFd, 5);
    
	/*********************** fin de lancement du serveur de socket ***************/
   while (!stop)
   {
	
		
		socklen_t len = sizeof(clntAdd);
		log.info("main : En attente de connexion d'un client ...");
		
		//this is where client connects. svr will hang in this mode until client conn
		connFd = accept(listenFd, (struct sockaddr *)&clntAdd, &len);
		
		if (connFd < 0)
		{
			log.erreur("Maincapteurs : main : Cannot accept connection");
			return 0;
		}
		else
		{
			log.info("Maincapteurs : main : Connection successful connFd =" + to_string(connFd));
		}
			
		int noThread = 0, n=0;
		run = true;
		
		while (run)
		{
			log.info("Maincapteurs : main : Nombre de thread = " + to_string(noThread) + " cree");
			bzero(buffer, 257);
			log.info("Maincapteurs : main : attente de reception d'un message...");
			//n = read(connFd,buffer,255);
			while((n = recv(connFd,buffer,255, 0)) <0)
			{
				this_thread::sleep_for(std::chrono::milliseconds(50));
			}
			
			memset(bufferCopie, 0, 256);
			message.clear();
			message = string(buffer);
			memcpy( bufferCopie , buffer , 255 );
			
			//log.info("task1 : message client ='" + message + "', j=" + to_string(j) + ", n=" + to_string(n));
			
			if (n < 0 )
				log.erreur("Maincapteurs : main : ERROR reading from socket ");
			else if (n != 0)
			{
				log.info("Maincapteurs : main : creation d'un thread pour analyser la commande :" + message);
				
				//thread_cpt1= new thread(&GestionCapteurs::runCapteur, this, 1, &detectionCpt1, &distance, &run);
				pthread_create(&threadA[noThread], NULL, task1, (void *) connFd); 
			
				log.info("Maincapteurs : main : Thread " + to_string(noThread) + " cree");
				
				noThread++;
				
				
			}
			else
			{
				log.info("Maincapteurs : main : Erreur message vide ou connexion interrompue");
				run = false;
			}
			
			
			
		}
	
	}
    
    for(int i = 0; i < 3; i++)
    {
        pthread_join(threadA[i], NULL);
    }
    close(connFd);
    
}

void *task1 ( void *socnum)
{
	// Fonction pour afficher et enregister les log
	Log log;
    log.info("Maincapteurs : task1 : Thread Identifiant: "+ to_string(pthread_self()) + ", message =" + (string)message);
    
    
    
	int n = 0, j = 0, i = 0;
	string  retourClient;
	// tableau pour stocker les commande cliente parametres[0] = commande, parametres[1] = vitesse, parametres[2] = direction, parametres[3] = nb de pas 
    string parametres[2];
	
	int connFd = (int) socnum;
	
	log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : connFd =" + to_string(connFd));
	
	
	
		
		
		
		//message.erase((string)message.size()-1,1);
			
			
			log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : commande du client='" + (string)message + "'");
			
	
			
			if(regex_match((string)message, pattern))
			{
				log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : la regex est valide");
				
				capteur.stopDetection();
				lockt.lock();
				
				i=0;
				parametres[1]="";
				parametres[2]="";
				char *token = strtok(bufferCopie, ";");
				while (token != NULL && i < 3) {
					parametres[i]= token;
					token = strtok(NULL, ";");
					i++;
				}
				
				int IDcommande = stoi(parametres[2]);
				
				log.info("Maincapteurs : task1 : ID= " + to_string(IDcommande) + " : "+ to_string(pthread_self()) + " : Commande = '" + parametres[0] +"' avec timeout = " + parametres[1] );
				
				if (parametres[0] == "GET")
				{
					log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : Demarrage de la detection d'obstcle");
					// capteur.detectionObstacle();
					
					if(stoi(parametres[1]) == 0)
					{
						retourClient= capteur.detectionObstacle(  ) + ";" + to_string(IDcommande) + "\n";
					}
					else
					{
						retourClient= capteur.detectionObstacle(  stoi(parametres[1]) ) + ";" + to_string(IDcommande) + "\n";
					}
					
				}
				else if (parametres[0] == "stop")
				{
					log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : arret de la detection d'obstcle");
					// capteur.detectionObstacle();
					retourClient= to_string(capteur.stopDetection()) + ";" + to_string(IDcommande) + "\n";
					
				}
				else
				{
					log.erreur("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : La commande du client est differente de GET ou stop");
					retourClient= "-1;" + to_string(IDcommande) + "\n";
					
				}
		
				lockt.unlock();
	
			}
			else
			{
				log.erreur("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : La commande du client est mauvaise : regex non validee");
				retourClient="-1;-1\n";
				
			}
			
			// This send() function sends the 13 bytes of the string to the new socket
			log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : retourClient =" + retourClient);
			if (send(connFd, retourClient.c_str(), retourClient.size(), 0) == -1)
			{
				log.erreur("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : Erreur d'envoi du message retour");
			}
			else
				log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : succes d'envoi du message de retour");
				
			//write(connFd, retourClient.c_str(), retourClient.size());
			retourClient.clear();
			
		
		
		
		j++;
		log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : fin boucle");
	
		
		
		
    log.info("Maincapteurs : task1 : "+ to_string(pthread_self()) + " : Closing thread ");
    
}