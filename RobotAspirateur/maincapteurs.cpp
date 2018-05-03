#include "Log.h"
#include "gestioncapteurs.h"
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

#define PORT 1201

using namespace std;




int main(int argc, char *argv[])
{
    Log log;
    GestionCapteurs capteur;
    int run=1;

    int sockfd, newsockfd, portno;
    socklen_t client;
    char buffer[256];
    struct sockaddr_in serv_addr, cli_addr;
    int n, i;
    string message, retourClient;
    string parametres[3];
    regex const pattern { "^\[a-z]\{4,5}$" };

    log.info("main : Debut du programme");
    log.space();

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
    // This listen() call tells the socket to listen to the incoming connections.
    // The listen() function places all incoming connection into a backlog queue
    // until accept() call accepts the connection.
    // Here, we set the maximum size for the backlog queue to 5.
    log.info("main : places all incoming connection into a backlog queue");
    listen(sockfd,5);

    // The accept() call actually accepts an incoming connection
    client = sizeof(cli_addr);








    while (run)
    {
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

        memset(buffer, 0, 256);

        n = read(newsockfd,buffer,255);
        if (n < 0)
            log.erreur("main : ERROR reading from socket");
        else
            message = string(buffer);

        message.erase(message.size()-1,1);

        //if (buffer == "")
        log.info("main : Here is the message:" + message);



        if(regex_match(message, pattern))
        {
            log.info("main : la regex est valide");
            i=0;
            char *token = strtok(buffer, ";");
            while (token != NULL && i < 3) {
               parametres[i]= token;
                token = strtok(NULL, ";");
                i++;
            }

        }
        else
        {
            log.erreur("main : La commande du client est mauvaise : regex non validee");
            retourClient="La commande doit etre sous la forme : commande;vitesse;direction\n";
            // This send() function sends the 13 bytes of the string to the new socket
            send(newsockfd, retourClient.c_str(), retourClient.size(), 0);

        }

        log.info("main : Commande = " + parametres[0] + ", vitesse =" + parametres[1] + ", direction = " + parametres[2]);

        if (parametres[0] == "start")
        {
            retourClient="demarrage des moteurs";
            controlleur.driver(stoi(parametres[1]), stoi(parametres[2]));

        }
        else if (parametres[0] == "stop")
        {
            retourClient="Arret des moteurs";
            controlleur.stopDriver();
        }
        else
        {
            log.erreur("main : La commande du client est differente de start ou stop");
            retourClient="ERREUR : la commande doit etre start ou stop";
        }

        // This send() function sends the 13 bytes of the string to the new socket
        send(newsockfd, retourClient.c_str(), retourClient.size(), 0);

    }





    close(newsockfd);
    close(sockfd);

    log.info("main : fin du programme");

    return 0;
}
