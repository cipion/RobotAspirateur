#define _BSD_SOURCE

#include "log.h"
#include "controlmoteur.h"
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

//g++ -std=c++11 -lwiringPi -lpthread  main.cpp log.cpp config.cpp controlmoteur.cpp -o RobotAspirateur


using namespace std;
//Drapeau d'arrêt du programme. Sa valeur d'origine est 0.
unsigned short stop = 0;
ControlMoteur controlleur;

/***********************************
* Fonction de reception des signaux.
* Si le signal reçu est SIGINT Ctl^c, alors on increment le nombre de signaux de ce type reçu.
* Quand ce nombre est egal a 5 on met stop à 1.
************************************/

void signalHandler(int signal){
	Log log;
	

	if(signal==SIGINT){
		
		log.info("signalHandler : Signal Ctrl^c reçu\n");
		stop=1;
		controlleur.stopDriver();
		exit(0);
	}
}

int main(int argc, char *argv[])
{
    Log log;
	
    
    bool run=true;
	

    int sockfd, newsockfd, portno;
    socklen_t client;
    char buffer[256];
    struct sockaddr_in serv_addr, cli_addr;
    int n, i, j=0;
    string message, retourClient;
    string parametres[4];
    regex const pattern { "^\[a-z]\{4,8}\[;]-?\[0-9]\{1,3}\[;]-?\[0-9]\{1,3}[;]\{0,1}\[0-9]\{0,5}$" };

    log.info("main : Debut du programme");
    log.space();

	/**********************************************************
	MISE EN PLACE DE LA GESTION DU SIGNAL CONTROL^C (SIGINT)
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
	/*********************************************************/

    log.info("main : création du socket");
    // socket(int domain, int type, int protocol)
    sockfd =  socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0)
        log.erreur("main : erreur d'ouverture du socket");

    log.info("main : reset de la structure adresse");
    memset((char *) &serv_addr, 0, sizeof(serv_addr));

    log.info("main : numero de port =" + to_string(PORT));
    portno = PORT;

    log.info("main : creation de la structure host_addr pour utiliser la fonction bind");
    // server byte order
    serv_addr.sin_family = AF_INET;

    log.info("main : implementation de l'adresse IP local");
    serv_addr.sin_addr.s_addr = INADDR_ANY;

    // convert short integer value for port must be converted into network byte order
    serv_addr.sin_port = htons(portno);

    // bind(int fd, struct sockaddr *local_addr, socklen_t addr_length)
    // bind() passes file descriptor, the address structure,
    // and the length of the address structure
    // This bind() call will bind  the socket to the current IP address on port, portno
    log.info("main : Bind la socket, l'adresse IP et le porto");
    //bind  the socket to the current IP address on port, portn
    if (bind(sockfd, (struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0)
        log.erreur("main : ERROR on binding");
    








    while (!stop)
	{
		// This listen() call tells the socket to listen to the incoming connections.
		// The listen() function places all incoming connection into a backlog queue
		// until accept() call accepts the connection.
		// Here, we set the maximum size for the backlog queue to 5.
		log.info("main : places all incoming connection into a backlog queue");
		listen(sockfd,5);
	
		// The accept() call actually accepts an incoming connection
		client = sizeof(cli_addr);
		// This accept() function will write the connecting client's address info
		// into the the address structure and the size of that structure is clien.
		// The accept() returns a new socket file descriptor for the accepted connection.
		// So, the original socket file descriptor can continue to be used
		// for accepting new connections while the new socker file descriptor is used for
		// communicating with the connected client.
		log.info("main : This accept() function will write the connecting client's address in");
		newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &client);
		
		if (newsockfd < 0)
			log.erreur("main : ERROR on accept");
		log.info("main : Client accepté");
		// log.info("main: got connection from " + inet_ntoa(cli_addr.sin_addr) + " port " + ntohs(cli_addr.sin_port));
		run=true;
		
		while (run)
		{
		
			memset(buffer, 0, 256);
			
			
			//n = read(newsockfd,buffer,255);
			n = recv(newsockfd,buffer,255, 0);
			message = string(buffer);
			log.info("debut boucle message='" + message + "', j=" + to_string(j) + ", n=" + to_string(n));
			
			if (n < 0 )
				log.erreur("main : ERROR reading from socket j=" + to_string(j));
			else if (n != 0)
			{
			
				
				//message.erase(message.size()-1,1);
				
				
				log.info("main : Here is the message:" + message);
		
		
				
				if(regex_match(message, pattern))
				{
					log.info("main : la regex est valide");
					i=0;
					char *token = strtok(buffer, ";");
					while (token != NULL && i < 4) {
						parametres[i]= token;
						token = strtok(NULL, ";");
						i++;
					}
					
					log.info("main : Commande = " + parametres[0] + ", vitesse =" + parametres[1] + ", direction = " + parametres[2] + ", nbPas = '" + parametres[3] +"'");
					
					if (parametres[0] == "start")
					{
						int b;
						
						if (parametres[3].empty())
						{
							log.info("demarrage des moteurs");
							b = controlleur.driver(stoi(parametres[1]), stoi(parametres[2]));
						}
						else
						{
							log.info("demarrage des moteurs avec " + parametres[3] + " pas");
							b = controlleur.driver(stoi(parametres[1]), stoi(parametres[2]), stoi(parametres[3]));
						}
						
						if (b != -1)
						{
							log.info("driver : succes de démarrage");
							retourClient="1\n";
						}
						else
						{
							log.info("driver : erreur de démarrage");
							retourClient="-1\n";
						}
					}
					else if (parametres[0] == "stop")
					{
						log.info("Arret des moteurs");
						
						if (controlleur.stopDriver() != -1)
						{
							log.info("driver : succes de démarrage");
							retourClient="0\n";
						}
						else
						{
							log.info("driver : erreur de démarrage");
							retourClient="-1\n";
						}
					}
					else if (parametres[0] == "rotation")
					{
						log.info("Rotation du robot");
						
						if (controlleur.rotationDriver(stoi(parametres[1]), stoi(parametres[2]), stoi(parametres[3]) ) != -1)
						{
							log.info("driver : succes de démarrage");
							retourClient="1\n";
						}
						else
						{
							log.info("driver : erreur de démarrage");
							retourClient="-1\n";
						}
					}
					else
					{
						log.erreur("main : La commande du client est differente de start, stop ou rotation");
						retourClient="-1\n";
					}
			
					
		
				}
				else
				{
					log.erreur("main : La commande du client est mauvaise : regex non validee");
					retourClient="-1\n";
					
				}
				
				// This send() function sends the 13 bytes of the string to the new socket
				log.info("retourClient =" + retourClient);
				if (send(newsockfd, retourClient.c_str(), retourClient.size(), 0) == -1)
				{
					log.erreur("Erreur d'envoi du message retour");
				}
				else
					log.info("succes d'envoi du message de retour");
					
				//write(newsockfd, retourClient.c_str(), retourClient.size());
				retourClient.clear();
					
				
				
			}
			else
			{
				log.erreur("Erreur message vide ou connexion interrompue");
				run = false;
			}
			
			
			j++;
			log.info("fin boucle");
		}
	}





    close(newsockfd);
    close(sockfd);

    log.info("main : fin du programme");

    return 0;
}
