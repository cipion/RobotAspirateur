#define _BSD_SOURCE

#include "log.h"
#include "comandeManager.h"
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <cstring>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <regex>
#include <signal.h>


#define PORT 1200


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
*	g++ -std=c++11 -lwiringPi -lpthread  main.cpp log.cpp config.cpp controlmoteur.cpp -o RobotAspirateur
*	Kill process utilisant le port 1200 : kill `netstat | grep 1200 | awk '/[0-9]/ {print $1}'`
********/


using namespace std;

//Drapeau d'arrêt du programme. Sa valeur d'origine est 0. pour l'instant c'est pas utile
unsigned short stop = 0;

// le coeur du programme, c'est l'objet qui commande le GPIO pour faire tourner les moteurs en fonction de vitesse, direction, nb de pas
ComandeManager comande;



/***********************************
* Fonction de reception des signaux.
* Si le signal reçu est SIGINT Ctl^c, alors arrete la commande du GPIO et on quitte le programme.
* 
************************************/

void signalHandler(int signal){
	Log *log=Log::Instance();
	

	if(signal==SIGINT){
		
		log->info("signalHandler : Signal Ctrl^c reçu\n");
		stop=1;
		controlleur.stopDriver();
		exit(0);
	}
}





/******************
* Fonction main qui initialise le driver, lance le serveur de socket, et gere les commande recu via les socket pour piloter le driver
*
*
********************/
int main(int argc, char *argv[])
{
	// Fonction pour afficher et enregister les log
    Log *log=Log::Instance();
	
	// Objet de configuration afin de recuperer les properties
	Config configLoader;
	typedef std::map<const std::string,std::string> cfgMap;
	cfgMap config;
	config = configLoader.getConfig();
	
    int sockfd, portno, n;
    struct sockaddr_in serv_addr;
    struct hostent *server;
	
	char buffer[256];
	
	portno = stoi(config.find("serveurport")->second);
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
	
	
    log->info("main : Debut du programme");
    log->space(); // trop cool on peut sauter des lignes...

	
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

	
	
	
    log->info("main : création du socket");
	
	if (sockfd < 0)
        error("ERROR opening socket");
    server = gethostbyname(argv[1]);
    if (server == NULL) {
        fprintf(stderr,"ERROR, no such host\n");
        exit(0);
    }
    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, (char *)&serv_addr.sin_addr.s_addr, server->h_length);
    serv_addr.sin_port = htons(portno);
    if (connect(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0)
        error("ERROR connecting");
    printf("Please enter the message: ");
    bzero(buffer,256);
    fgets(buffer,255,stdin);
    n = write(sockfd, buffer, strlen(buffer));
    if (n < 0)
         error("ERROR writing to socket");
    bzero(buffer,256);
    n = read(sockfd, buffer, 255);
    if (n < 0)
         error("ERROR reading from socket");
    printf("%s\n", buffer);
    close(sockfd);


    log->info("main : fin du programme");

    return 0;
}
