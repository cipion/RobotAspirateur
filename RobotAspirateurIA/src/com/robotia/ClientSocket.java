package com.robotia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSocket {

    // variable contenant les ports des serveurs des drivers
    private int                 portSocketMoteur;
    // private int portSocketCapteur;

    // variables pour la socket
    private InetAddress         hostSocket;
    private Socket              socketMoteur;
    // private Socket socketCapteur;
    // variables comptenant les messages à envoyer aux drivers
    private BufferedWriter      messageSendMoteur;
    // private BufferedWriter messageSendCapteur;
    // flux de sortie pour envoyer les messages aux drivers
    OutputStream                osMoteur;
    OutputStreamWriter          oswMoteur;
    // flux d'entrée pour recuperer les messages de retour des drivers
    InputStream                 isMoteur;
    InputStreamReader           isrMoteur;
    // OutputStream osCapteur;
    // OutputStreamWriter oswCapteur;
    // InputStream isCapteur;
    // InputStreamReader isrCapteur;
    // variable pour recuperer les properties
    private ResourceBundle      bundle;

    private BufferedReader      messageReceiveMoteur;
    // private BufferedReader messageReceiveCapteur;
    private String              message;
    private String              msgStart;
    private String              msgStop;
    private String              msgRotate;
    private String              msgGET;

    // Variable pour les logs
    private static final Logger logger = LoggerFactory.getLogger( ClientSocket.class );

    /**
     * Constructeur qui charge les properties et initialise les ports des
     * drivers
     */
    ClientSocket()
    {
        logger.info( "Initialisation des varialbes des sockets" );

        bundle = ResourceBundle.getBundle( "config" );
        portSocketMoteur = Integer.parseInt( bundle.getString( "socket.driverMoteur.port" ) );
        // portSocketCapteur = Integer.parseInt( bundle.getString(
        // "socket.driverCapteur.port" ) );

    }

    /**********************
     * Fonction qui initialise la connexion du socket client sur les serveurs de
     * socket
     * 
     * @return 1 si succes ou 0 si erreur
     */
    public boolean initSocket()
    {
        // recupere l'adresse IP des drivers via une properties
        try {
            logger.info( "initSocket : Recuperation de l'IP" );
            hostSocket = InetAddress.getByName( bundle.getString( "socket.host" ) );
        } catch ( UnknownHostException e1 ) {
            // TODO Auto-generated catch block
            logger.error( "initSocket : Erreur de recuperation de l'adresse IP " + e1.toString() );
            return false;
        }

        logger.info( "initSocket : Initialisation des sockets" );

        boolean init1 = fctInitSocketMoteur();
        // boolean init2 = fctInitSocketCapteur();

        return ( init1 /* && init2 */) ? true : false;

    }

    public boolean fctInitSocketMoteur()
    {
        // ouverture des socket input et output pour le driver des moteurs
        try {
            logger.debug( "fctInitSocketMoteur : hostSocket = " + hostSocket + " portSocketMoteur = "
                    + portSocketMoteur );
            socketMoteur = new Socket( hostSocket, portSocketMoteur );

            logger.debug( "fctInitSocketMoteur : Initialisation des outputStream" );
            osMoteur = socketMoteur.getOutputStream();
            oswMoteur = new OutputStreamWriter( osMoteur );
            messageSendMoteur = new BufferedWriter( oswMoteur );

            logger.debug( "fctInitSocketMoteur : Initialisation des inputStream" );
            isMoteur = socketMoteur.getInputStream();
            isrMoteur = new InputStreamReader( isMoteur );
            messageReceiveMoteur = new BufferedReader( isrMoteur );

            logger.debug( "fctInitSocketMoteur : Initialisation socket moteur finie" );

        } catch ( UnknownHostException e ) {

            logger.error( "fctInitSocketMoteur : probleme d'initialisation des sockets : " + e.toString() );
            return false;

        } catch ( IOException e ) {

            logger.error( "fctInitSocketMoteur : probleme d'initialisation des sockets : " + e.toString() );
            return false;
        }

        return true;
    }

    /*
     * public boolean fctInitSocketCapteur() { // ouverture des sockets input et
     * output du driver des capteurs try { logger.debug(
     * "fctInitSocketCapteur : hostSocket = " + hostSocket +
     * " portSocketCapteur = " + portSocketCapteur ); socketCapteur = new
     * Socket( hostSocket, portSocketCapteur );
     * 
     * logger.debug( "fctInitSocketCapteur : Initialisation des outputStream" );
     * osCapteur = socketCapteur.getOutputStream(); oswCapteur = new
     * OutputStreamWriter( osCapteur ); messageSendCapteur = new BufferedWriter(
     * oswCapteur );
     * 
     * logger.debug( "fctInitSocketCapteur : Initialisation des inputStream" );
     * isCapteur = socketCapteur.getInputStream(); isrCapteur = new
     * InputStreamReader( isCapteur ); messageReceiveCapteur = new
     * BufferedReader( isrCapteur );
     * 
     * logger.debug( "fctInitSocketCapteur : Initialisation socket moteur finie"
     * );
     * 
     * } catch ( UnknownHostException e ) {
     * 
     * logger.error(
     * "fctInitSocketCapteur : probleme d'initialisation des sockets : " +
     * e.toString() ); return false;
     * 
     * } catch ( IOException e ) {
     * 
     * logger.error(
     * "fctInitSocketCapteur : probleme d'initialisation des sockets : " +
     * e.toString() ); return false; } catch ( NullPointerException e ) {
     * logger.error(
     * "fctInitSocketCapteur : erreur d'initialisation des socket : " +
     * e.toString() ); return false; }
     * 
     * return true; }
     */

    /*****
     * Fonction d'arret des sockets et de fermeture des flux
     * 
     * @return
     */
    public Boolean stopSocket()
    {
        logger.info( "stopSocket : Arret des sockets en cours ..." );
        // boolean stop1 = fctStopSocketCapteur();
        boolean stop2 = fctStopSocketMoteur();

        return ( /* stop1 && */stop2 ) ? true : false;

    }

    /*
     * public boolean fctStopSocketCapteur() { try {
     * 
     * // arret des flux input et output du driver moteur socketMoteur.close();
     * messageReceiveMoteur.close(); oswMoteur.close(); osMoteur.close();
     * messageSendMoteur.close(); isrMoteur.close(); isMoteur.close();
     * 
     * logger.info( "fctStopSocketCapteur : Sockets Moteurs arretés" );
     * 
     * return true;
     * 
     * } catch ( IOException e ) { logger.error(
     * "fctStopSocketCapteur : probleme d'arret du socket des Moteurs : " +
     * e.toString() ); return false; } catch ( NullPointerException e ) {
     * logger.error(
     * "fctStopSocketCapteur : erreur de fermeture du socket des Moteurs : " +
     * e.toString() ); return false; } }
     */

    public boolean fctStopSocketMoteur()
    {
        try {

            // arret des flux input et output du driver des capteurs
            socketMoteur.close();
            messageReceiveMoteur.close();
            oswMoteur.close();
            osMoteur.close();
            messageSendMoteur.close();
            isrMoteur.close();
            isMoteur.close();

            logger.info( "fctStopSocketCapteur : Sockets arretés" );

            return true;

        } catch ( IOException e ) {
            logger.error( "fctStopSocketCapteur : probleme d'arret du socket des Moteurs : " + e.toString() );
            return false;
        } catch ( NullPointerException e ) {
            logger.error( "fctStopSocketCapteur : erreur de fermeture du socket des Moteurs : " + e.toString() );
            return false;
        }
    }

    /******
     * Fonction qui envoie une commande des moteurs au serveurs de socket
     * 
     * @param cmd
     * @param vitesse
     * @param direction
     * @return
     */
    public String setDriverMoteurStatut( Commandes cmd, int vitesse, int direction )
    {

        int IDcommande = (int) ( Math.random() * ( 1000 - 0 ) );
        int IDretour = 0;
        logger.info( "setDriverMoteurStatut : ID = " + IDcommande + " : Commande " + cmd.toString()
                + " des moteurs demandée avec les parametres Vitesse=" + vitesse
                + " direction=" + direction );

        // si la commande est "START", et que la vitesse et la direction sont
        // conformes ...
        if ( cmd.equals( Commandes.START )
                && vitesse <= 100 && vitesse >= -100
                && direction <= 100 && direction >= -100 )
        {
            logger.debug( "setDriverMoteurStatut : Envoi de la commande START" );
            try {
                msgStart = Commandes.START.toString() + ";" + vitesse + ";" + direction + ";0;" + IDcommande;
                messageSendMoteur.write( msgStart );
                messageSendMoteur.flush();

                int i = 0;
                do {
                    logger.debug( "setDriverMoteurStatut : Recupération du message de retour" );
                    message = messageReceiveMoteur.readLine();

                    IDretour = Integer.parseInt( message.substring( message.lastIndexOf( ";" ) + 1 ) );

                    if ( IDretour == -1 )
                    {
                        logger.error( "getDetectionObstacle : Erreur de traitenemt de la commande" );
                        return "-1";
                    }

                    if ( i == 3 )
                    {
                        logger.error( "setDriverMoteurStatut : Erreur, 3 essais de reception de message avec le mauvais ID" );
                        return "-1";
                    }

                } while ( IDretour != IDcommande );

                String codeRetour = message.substring( 0, message.indexOf( ";" ) );

                if ( codeRetour.equals( "1" ) )
                {
                    logger.info( "setDriverMoteurStatut : Les moteurs sont démarrés" );

                }
                else
                {
                    logger.error( "setDriverMoteurStatut : Erreur lors du traitement de la commande" );
                    return "-1";
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "setDriverMoteurStatut : erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            } catch ( NullPointerException e ) {
                logger.error( "setDriverMoteurStatut : erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            }

        }
        // si la commande est un "STOP"
        else if ( cmd.equals( Commandes.STOP )
                && vitesse <= 100 && vitesse >= -100
                && direction <= 100 && direction >= -100 )
        {
            logger.debug( "setDriverMoteurStatut : Envoi de la commande STOP" );
            try {
                msgStop = Commandes.STOP.toString() + ";" + vitesse + ";" + direction + ";0;" + IDcommande;
                messageSendMoteur.write( msgStop );
                messageSendMoteur.flush();

                int i = 0;
                do {
                    logger.debug( "setDriverMoteurStatut : Recupération du message de retour" );
                    message = messageReceiveMoteur.readLine();

                    IDretour = Integer.parseInt( message.substring( message.lastIndexOf( ";" ) + 1 ) );

                    if ( IDretour == -1 )
                    {
                        logger.error( "getDetectionObstacle : Erreur de traitenemt de la commande" );
                        return "-1";
                    }

                    if ( i == 3 )
                    {
                        logger.error( "setDriverMoteurStatut : Erreur, 3 essais de reception de message avec le mauvais ID" );
                        return "-1";
                    }

                } while ( IDretour != IDcommande );

                String codeRetour = message.substring( 0, message.indexOf( ";" ) );

                if ( !codeRetour.equals( "-1" ) )
                {
                    logger.info( "setDriverMoteurStatut : Les moteurs sont éteints, retour =" + message );
                    return message;
                }
                else
                {
                    logger.error( "setDriverMoteurStatut : Erreur lors du traitement de la commande" );
                    return "-1";
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "setDriverMoteurStatut : erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            } catch ( NullPointerException e ) {
                logger.error( "setDriverMoteurStatut : erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            }
        }
        else
        {
            logger.error( "setDriverMoteurStatut : Erreur, la commande doit etre START ou STOP avec vitesse et direction <= 100  et >= -100 (180 pour ROTATE)" );
            return "-1";
        }

        return "1";

    }

    /******
     * Fonction qui envoie une commande des moteurs au serveurs de socket en
     * fonction d'un certain nombre de pas.
     * 
     * @param cmd
     * @param vitesse
     * @param direction
     * @param nbPas
     * @return
     */
    public Boolean setDriverMoteurStatut( Commandes cmd, int vitesse, int direction, int nbPas )
    {
        int IDcommande = (int) ( Math.random() * ( 1000 - 0 ) );
        int IDretour = 0;
        logger.info( "setDriverMoteurStatut : ID = " + IDcommande + " : Commande " + cmd.toString()
                + " des moteurs demandée avec les parametres Vitesse=" + vitesse
                + " direction=" + direction + ", nbPas=" + nbPas );

        // commande pour avancer en fonction du nombre de pas "nbPas" en
        // respectant les condition de vitesse et direction
        if ( cmd.equals( Commandes.START )
                && vitesse <= 100 && vitesse >= -100
                && direction <= 100 && direction >= -100
                && nbPas > 0 )
        {
            logger.debug( "setDriverMoteurStatut : Envoi de la commande START" );
            try {
                msgStart = Commandes.START.toString() + ";" + vitesse + ";" + direction + ";" + nbPas + ";"
                        + IDcommande;
                messageSendMoteur.write( msgStart );
                messageSendMoteur.flush();

                int i = 0;
                do {
                    logger.debug( "setDriverMoteurStatut : Recupération du message de retour" );
                    message = messageReceiveMoteur.readLine();

                    IDretour = Integer.parseInt( message.substring( message.lastIndexOf( ";" ) + 1 ) );

                    if ( IDretour == -1 )
                    {
                        logger.error( "getDetectionObstacle : Erreur de traitenemt de la commande" );
                        return false;
                    }

                    if ( i == 3 )
                    {
                        logger.error( "setDriverMoteurStatut : Erreur, 3 essais de reception de message avec le mauvais ID" );
                        return false;
                    }

                } while ( IDretour != IDcommande );

                String codeRetour = message.substring( 0, message.indexOf( ";" ) );

                if ( codeRetour.equals( "1" ) )
                {
                    logger.info( "setDriverMoteurStatut : Les moteurs sont démarrés" );
                    return true;
                }
                else
                {
                    logger.error( "setDriverMoteurStatut : Erreur lors du traitement de la commande" );
                    return false;
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "setDriverMoteurStatut : erreur d'envoie du message au serveur : " + e.toString() );
                return false;
            }

        }
        // fonction pour faire une rotation de "nbPas" pas
        else if ( cmd.equals( Commandes.ROTATE )
                && vitesse <= 100 && vitesse >= -100
                && direction <= 180 && direction >= -180
                && nbPas > 0 )
        {
            logger.debug( "setDriverMoteurStatut : Envoi de la commande ROTATE" );

            if ( direction == 90 || direction == -90 )
            {
                nbPas = Integer.parseInt( bundle.getString( "socket.driverMoteur.nbPas90" ) );
                logger.debug( "setDriverMoteurStatut : l'angle est de " + direction + ", le nombre de pas est de "
                        + nbPas );
            }
            else if ( direction == 180 || direction == -180 )
            {
                nbPas = Integer.parseInt( bundle.getString( "socket.driverMoteur.nbPas180" ) );
                logger.debug( "setDriverMoteurStatut : l'angle est de " + direction + ", le nombre de pas est de "
                        + nbPas );
            }
            else
            {
                logger.error( "setDriverMoteurStatut : Erreur, l'angle doit etre de 90, -90 ou 180" );
                return false;
            }

            try {
                msgStart = Commandes.ROTATE.toString() + ";" + vitesse + ";" + direction + ";" + nbPas + ";"
                        + IDcommande;
                messageSendMoteur.write( msgStart );
                messageSendMoteur.flush();

                int i = 0;
                do {
                    logger.debug( "setDriverMoteurStatut : Recupération du message de retour" );
                    message = messageReceiveMoteur.readLine();

                    IDretour = Integer.parseInt( message.substring( message.lastIndexOf( ";" ) + 1 ) );

                    if ( i == 3 )
                    {
                        logger.error( "setDriverMoteurStatut : Erreur, 3 essais de reception de message avec le mauvais ID" );
                        return false;
                    }

                } while ( IDretour != IDcommande );

                String codeRetour = message.substring( 0, message.indexOf( ";" ) );

                if ( codeRetour.equals( "1" ) )
                {
                    logger.info( "setDriverMoteurStatut : Les moteurs sont démarrés" );
                    return true;
                }
                else
                {
                    logger.error( "setDriverMoteurStatut : Erreur lors du traitement de la commande" );
                    return false;
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "setDriverMoteurStatut : erreur d'envoie du message au serveur : " + e.toString() );
                return false;
            } catch ( NullPointerException e ) {
                logger.error( "setDriverMoteurStatut : erreur d'envoie du message au serveur : " + e.toString() );
                return false;
            }

        }
        else
        {
            logger.error( "setDriverMoteurStatut : Erreur, la commande doit etre START ou ROTATE avec vitesse et direction <= 100  et >= -100 (180 pour ROTATE) et nbPas > 0" );
            return false;
        }

    }

    /******
     * Fonction demande au driver capteur de nous envoer un message quand un
     * capteur detecte un obstacle.
     * 
     * 
     * @return une chaine de caractere contenant le nom du capteur et la
     *         distance de detection.
     */
    /*
     * public String getDetectionObstacle() { int IDcommande = (int) (
     * Math.random() * ( 1000 - 0 ) ); int IDretour = 0; logger.debug(
     * "getDetectionObstacle : ID = " + IDcommande +
     * " : Envoi de la commande GET au driver des capteurs" ); try { msgGET =
     * Commandes.GET.toString() + ";0;" + IDcommande;
     * 
     * messageSendCapteur.write( msgGET );
     * 
     * messageSendCapteur.flush(); logger.info(
     * "getDetectionObstacle : Commande GET envoyée au driver des capteurs" );
     * 
     * int i = 0; do { logger.debug(
     * "getDetectionObstacle : Recupération du message de retour" ); message =
     * messageReceiveCapteur.readLine();
     * 
     * IDretour = Integer.parseInt( message.substring( message.lastIndexOf( ";"
     * ) + 1 ) );
     * 
     * if ( IDretour == -1 ) { logger.error(
     * "getDetectionObstacle : Erreur de traitenemt de la commande" ); return
     * "-2"; }
     * 
     * if ( i == 3 ) { logger.error(
     * "getDetectionObstacle : Erreur, 3 essais de reception de message avec le mauvais ID"
     * ); return "-2"; }
     * 
     * } while ( IDretour != IDcommande );
     * 
     * String codeRetour = message.substring( 0, message.indexOf( ";" ) );
     * 
     * if ( codeRetour.equals( "1" ) ) { logger.error(
     * "getDetectionObstacle : Erreur de traitement du driver" );
     * 
     * } else { logger.info(
     * "getDetectionObstacle : Message de detection des catpeurs : " + message
     * );
     * 
     * }
     * 
     * } catch ( IOException e ) { // TODO Auto-generated catch block
     * logger.error(
     * "getDetectionObstacle : erreur d'envoie du message au serveur : " +
     * e.toString() ); return "-2"; } catch ( NullPointerException e ) {
     * logger.error(
     * "getDetectionObstacle : erreur d'envoie du message au serveur : " +
     * e.toString() ); return "-2"; }
     * 
     * return message;
     * 
     * }
     */

    /******
     * Fonction qui envoie une commande au driver capteur et qui attend un
     * message de retour si un capteur detecte un obstacle durant la periode
     * envoyée en parametre. Si la periode "timeout" est dépassée alors le
     * driver envoie un message de timeout.
     * 
     * @param timeout
     * @return une chaine de caractere contenant le nom du capteur et la
     *         distance de detection.
     */
    /*
     * public String getDetectionObstacle( int timeout ) { int IDcommande =
     * (int) ( Math.random() * ( 1000 - 0 ) ); int IDretour = 0; logger.debug(
     * "getDetectionObstacle : ID= " + IDcommande +
     * " : Envoi de la commande GET au driver des capteurs avec timeout = " +
     * timeout ); try { msgGET = Commandes.GET.toString() + ";" + timeout + ";"
     * + IDcommande; messageSendCapteur.write( msgGET );
     * messageSendCapteur.flush(); logger.info(
     * "getDetectionObstacle : Commande GET envoyée au driver des capteurs" );
     * 
     * int i = 0; do { logger.debug(
     * "getDetectionObstacle : Recupération du message de retour" ); message =
     * messageReceiveCapteur.readLine();
     * 
     * IDretour = Integer.parseInt( message.substring( message.lastIndexOf( ";"
     * ) + 1 ) );
     * 
     * if ( IDretour == -1 ) { logger.error(
     * "getDetectionObstacle : Erreur de traitenemt de la commande" ); return
     * "-2"; }
     * 
     * if ( i == 3 ) { logger.error(
     * "getDetectionObstacle : Erreur, 3 essais de reception de message avec le mauvais ID"
     * ); return "-2"; }
     * 
     * } while ( IDretour != IDcommande );
     * 
     * String codeRetour = message.substring( 0, message.indexOf( ";" ) );
     * 
     * if ( codeRetour.equals( "1" ) ) { logger.error(
     * "getDetectionObstacle : Erreur de traitement du driver" );
     * 
     * } else { logger.error(
     * "getDetectionObstacle : Message de detection des catpeurs : " + message
     * );
     * 
     * }
     * 
     * } catch ( IOException e ) { // TODO Auto-generated catch block
     * logger.error(
     * "getDetectionObstacle : erreur d'envoie du message au serveur : " +
     * e.toString() ); return "-2"; } catch ( NullPointerException e ) {
     * logger.error(
     * "getDetectionObstacle : erreur d'envoie du message au serveur : " +
     * e.toString() ); return "-2"; }
     * 
     * return message;
     * 
     * }
     */

    /******
     * Fonction qui envoie une commande au driver des capteurs pour stopper la
     * detection d'obstacle
     * 
     * 
     * @return le message de retour confirmant ou pas l'arret de la detection
     */
    /*
     * public String stopDetection() { int IDcommande = (int) ( Math.random() *
     * ( 1000 - 0 ) ); logger.debug(
     * "stopDetection : Envoi de la commande STOP au driver des capteurs" ); try
     * { msgStop = Commandes.STOP.toString() + ";0;" + IDcommande;
     * messageSendCapteur.write( msgStop ); messageSendCapteur.flush();
     * logger.info(
     * "stopDetection : Commande STOP envoyée au driver des capteurs" );
     * 
     * logger.debug( "stopDetection : Recupération du message de retour" );
     * message = messageReceiveCapteur.readLine();
     * 
     * String codeRetour = message.substring( 0, message.indexOf( ";" ) );
     * 
     * if ( codeRetour.equals( "1" ) ) { logger.error(
     * "stopDetection : Succes d'arret de la detection" );
     * 
     * } else { logger.info(
     * "stopDetection : Message d'arret de detection des catpeurs : " + message
     * );
     * 
     * }
     * 
     * } catch ( IOException e ) { // TODO Auto-generated catch block
     * logger.error( "stopDetection : erreur d'envoie du message au serveur : "
     * + e.toString() ); return "-2"; } catch ( NullPointerException e ) {
     * logger.error(
     * "stopDetection : erreur d'envoie/reception du message au serveur : " +
     * e.toString() ); return "-2"; }
     * 
     * return message;
     * 
     * }
     */

    /*
     * public boolean fctRestartSocketCapteur() { logger.debug(
     * "fctRestartSocketCapteur : Redémarrage du socket" ); boolean stop =
     * fctStopSocketCapteur(); boolean start = fctInitSocketCapteur();
     * 
     * return ( stop && start ) ? true : false;
     * 
     * }
     */

}
