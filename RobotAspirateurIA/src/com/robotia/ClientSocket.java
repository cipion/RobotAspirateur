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

    private int                 portSocketMoteur;
    private int                 portSocketCapteur;
    private InetAddress         hostSocket;
    private Socket              socketMoteur;
    private Socket              socketCapteur;
    private BufferedWriter      messageSendMoteur;
    private BufferedWriter      messageSendCapteur;
    OutputStream                osMoteur;
    OutputStreamWriter          oswMoteur;
    InputStream                 isMoteur;
    InputStreamReader           isrMoteur;
    OutputStream                osCapteur;
    OutputStreamWriter          oswCapteur;
    InputStream                 isCapteur;
    InputStreamReader           isrCapteur;
    private ResourceBundle      bundle;
    private BufferedReader      messageReceiveMoteur;
    private BufferedReader      messageReceiveCapteur;
    private String              message;
    private String              msgStart;
    private String              msgStop;
    private String              msgRotate;
    private String              msgGET;

    private static final Logger logger = LoggerFactory.getLogger( ClientSocket.class );

    ClientSocket()
    {
        logger.info( "Initialisation des varialbes des sockets" );

        bundle = ResourceBundle.getBundle( "config" );
        portSocketMoteur = Integer.parseInt( bundle.getString( "socket.driverMoteur.port" ) );
        portSocketCapteur = Integer.parseInt( bundle.getString( "socket.driverCapteur.port" ) );

    }

    /**********************
     * Fonction qui initialise la connexion du socket client sur les serveurs de
     * socket
     * 
     * @return 1 si succes ou 0 si erreur
     */
    public Boolean initSocket()
    {
        try {
            logger.info( "Recuperation de l'IP" );
            hostSocket = InetAddress.getByName( bundle.getString( "socket.host" ) );
        } catch ( UnknownHostException e1 ) {
            // TODO Auto-generated catch block
            logger.error( "Erreur de recuperation de l'adresse IP " + e1.toString() );
            return false;
        }

        logger.info( "Initialisation des sockets" );

        try {
            logger.debug( "hostSocket = " + hostSocket + " portSocketMoteur = " + portSocketMoteur );
            socketMoteur = new Socket( hostSocket, portSocketMoteur );

            logger.debug( "Initialisation des outputStream" );
            osMoteur = socketMoteur.getOutputStream();
            oswMoteur = new OutputStreamWriter( osMoteur );
            messageSendMoteur = new BufferedWriter( oswMoteur );

            logger.debug( "Initialisation des inputStream" );
            isMoteur = socketMoteur.getInputStream();
            isrMoteur = new InputStreamReader( isMoteur );
            messageReceiveMoteur = new BufferedReader( isrMoteur );

            logger.debug( "Initialisation socket moteur finie" );

        } catch ( UnknownHostException e ) {

            logger.error( "probleme d'initialisation des sockets : " + e.toString() );
            return false;

        } catch ( IOException e ) {

            logger.error( "probleme d'initialisation des sockets : " + e.toString() );
            return false;
        }

        try {
            logger.debug( "hostSocket = " + hostSocket + " portSocketCapteur = " + portSocketMoteur );
            socketCapteur = new Socket( hostSocket, portSocketCapteur );

            logger.debug( "Initialisation des outputStream" );
            osCapteur = socketCapteur.getOutputStream();
            oswCapteur = new OutputStreamWriter( osCapteur );
            messageSendCapteur = new BufferedWriter( oswCapteur );

            logger.debug( "Initialisation des inputStream" );
            isCapteur = socketCapteur.getInputStream();
            isrCapteur = new InputStreamReader( isCapteur );
            messageReceiveCapteur = new BufferedReader( isrCapteur );

            logger.debug( "Initialisation socket moteur finie" );

        } catch ( UnknownHostException e ) {

            logger.error( "probleme d'initialisation des sockets : " + e.toString() );
            return false;

        } catch ( IOException e ) {

            logger.error( "probleme d'initialisation des sockets : " + e.toString() );
            return false;
        } catch ( NullPointerException e ) {
            logger.error( "erreur d'initialisation des socket : " + e.toString() );
            return false;
        }

        return true;

    }

    /*****
     * Fonction d'arret des sockets et de fermeture des flux
     * 
     * @return
     */
    public Boolean stopSocket()
    {
        logger.info( "Arret des sockets en cours ..." );
        try {
            socketMoteur.close();
            messageReceiveMoteur.close();
            oswMoteur.close();
            osMoteur.close();
            messageSendMoteur.close();
            isrMoteur.close();
            isMoteur.close();

            socketCapteur.close();
            messageReceiveCapteur.close();
            oswCapteur.close();
            osCapteur.close();
            messageSendCapteur.close();
            isrCapteur.close();
            isCapteur.close();

            logger.info( "Sockets arretés" );

            return true;

        } catch ( IOException e ) {
            logger.error( "probleme d'arret des sockets : " + e.toString() );
            return false;
        } catch ( NullPointerException e ) {
            logger.error( "erreur de fermeture des sockets : " + e.toString() );
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
        logger.info( "Commande " + cmd.toString() + " des moteurs demandée avec les parametres Vitesse=" + vitesse
                + " direction=" + direction );

        if ( cmd.equals( Commandes.START )
                && vitesse <= 100 && vitesse >= -100
                && direction <= 100 && direction >= -100 )
        {
            logger.debug( "Envoi de la commande START" );
            try {
                msgStart = Commandes.START.toString() + ";" + vitesse + ";" + direction;
                messageSendMoteur.write( msgStart );
                messageSendMoteur.flush();

                logger.debug( "Recupération du message de retour" );
                message = messageReceiveMoteur.readLine();

                if ( message.equals( "1" ) )
                {
                    logger.info( "Les moteurs sont démarrés" );

                }
                else
                {
                    logger.error( "Erreur lors du traitement de la commande" );
                    return "-1";
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            } catch ( NullPointerException e ) {
                logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            }

        }
        else if ( cmd.equals( Commandes.STOP )  
                && vitesse <= 100 && vitesse >= -100
                && direction <= 100 && direction >= -100)
        {
            logger.debug( "Envoi de la commande STOP" );
            try {
                msgStop = Commandes.STOP.toString() + ";" + vitesse + ";" + direction;
                messageSendMoteur.write( msgStop );
                messageSendMoteur.flush();

                logger.debug( "Recupération du message de retour" );
                message = messageReceiveMoteur.readLine();

                if ( !message.equals( "-1" ) )
                {
                    logger.info( "Les moteurs sont éteints, retour =" + message );
                    return message;
                }
                else
                {
                    logger.error( "Erreur lors du traitement de la commande" );
                    return "-1";
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            } catch ( NullPointerException e ) {
                logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
                return "-1";
            }
        }
        else
        {
            logger.error( "Erreur, la commande doit etre START, ROTATE ou STOP avec vitesse et direction <= 100  et >= -100 (180 pour ROTATE)" );
            return "-1";
        }

        return "1";

    }

    /******
     * Fonction qui envoie une commande des moteurs au serveurs de socket
     * 
     * @param cmd
     * @param vitesse
     * @param direction
     * @return
     */
    public Boolean setDriverMoteurStatut( Commandes cmd, int vitesse, int direction, int nbPas )
    {
        logger.info( "Commande " + cmd.toString() + " des moteurs demandée avec les parametres Vitesse=" + vitesse
                + " direction=" + direction + ", nbPas=" + nbPas );

        if ( cmd.equals( Commandes.START )
                && vitesse <= 100 && vitesse >= -100
                && direction <= 100 && direction >= -100
                && nbPas > 0 )
        {
            logger.debug( "Envoi de la commande START" );
            try {
                msgStart = Commandes.START.toString() + ";" + vitesse + ";" + direction + ";" + nbPas;
                messageSendMoteur.write( msgStart );
                messageSendMoteur.flush();

                logger.debug( "Recupération du message de retour" );
                message = messageReceiveMoteur.readLine();

                if ( message.equals( "1" ) )
                {
                    logger.info( "Les moteurs sont démarrés" );
                    return true;
                }
                else
                {
                    logger.error( "Erreur lors du traitement de la commande" );
                    return false;
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
                return false;
            }

        }
        else if ( cmd.equals( Commandes.ROTATE )
                && vitesse <= 100 && vitesse >= -100
                && direction <= 180 && direction >= -180
                && nbPas > 0 )
        {
            logger.debug( "Envoi de la commande ROTATE" );

            if ( direction == 90 || direction == -90 )
            {
                nbPas = Integer.parseInt( bundle.getString( "socket.driverMoteur.nbPas90" ) );
                logger.debug( "l'angle est de " + direction + ", le nombre de pas est de " + nbPas );
            }
            else if ( direction == 180 || direction == -180 )
            {
                nbPas = Integer.parseInt( bundle.getString( "socket.driverMoteur.nbPas180" ) );
                logger.debug( "l'angle est de " + direction + ", le nombre de pas est de " + nbPas );
            }
            else
            {
                logger.error( "Erreur, l'angle doit etre de 90, -90 ou 180" );
                return false;
            }

            try {
                msgStart = Commandes.ROTATE.toString() + ";" + vitesse + ";" + direction + ";" + nbPas;
                messageSendMoteur.write( msgStart );
                messageSendMoteur.flush();

                logger.debug( "Recupération du message de retour" );
                message = messageReceiveMoteur.readLine();

                if ( message.equals( "1" ) )
                {
                    logger.info( "Les moteurs sont démarrés" );
                    return true;
                }
                else
                {
                    logger.error( "Erreur lors du traitement de la commande" );
                    return false;
                }

            } catch ( IOException e ) {
                // TODO Auto-generated catch block
                logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
                return false;
            } catch ( NullPointerException e ) {
                logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
                return false;
            }

        }
        else
        {
            logger.error( "Erreur, la commande doit etre START, ROTATE ou STOP avec vitesse et direction <= 100  et >= -100 (180 pour ROTATE) et nbPas > 0" );
            return false;
        }

    }

    public String getDetectionObstacle()
    {
        logger.debug( "Envoi de la commande GET au driver des capteurs" );
        try {
            msgGET = Commandes.GET.toString();
            messageSendCapteur.write( msgGET );
            messageSendCapteur.flush();
            logger.info( "Commande GET envoyée au driver des capteurs" );

            logger.debug( "Recupération du message de retour" );
            message = messageReceiveCapteur.readLine();

            if ( message.equals( "-1" ) )
            {
                logger.error( "Erreur de traitement du driver" );

            }
            else
            {
                logger.info( "Message de detection des catpeurs : " + message );

            }

        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
            return "-2";
        } catch ( NullPointerException e ) {
            logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
            return "-2";
        }

        return message;

    }

    public String getDetectionObstacle( int timeout )
    {
        logger.debug( "Envoi de la commande GET au driver des capteurs" );
        try {
            msgGET = Commandes.GET.toString() + ";" + timeout;
            messageSendCapteur.write( msgGET );
            messageSendCapteur.flush();
            logger.info( "Commande GET envoyée au driver des capteurs" );

            logger.debug( "Recupération du message de retour" );
            message = messageReceiveCapteur.readLine();

            if ( message.equals( "-1" ) )
            {
                logger.error( "Erreur de traitement du driver" );

            }
            else
            {
                logger.error( "Message de detection des catpeurs : " + message );

            }

        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
            return "-2";
        } catch ( NullPointerException e ) {
            logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
            return "-2";
        }

        return message;

    }

    public String stopDetection()
    {
        logger.debug( "Envoi de la commande STOP au driver des capteurs" );
        try {
            msgStop = Commandes.STOP.toString();
            messageSendCapteur.write( msgStop );
            messageSendCapteur.flush();
            logger.info( "Commande STOP envoyée au driver des capteurs" );

            logger.debug( "Recupération du message de retour" );
            message = messageReceiveCapteur.readLine();

            if ( message.equals( "-1" ) )
            {
                logger.error( "Erreur de traitement du driver" );

            }
            else
            {
                logger.info( "Message d'arret de detection des catpeurs : " + message );

            }

        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            logger.error( "erreur d'envoie du message au serveur : " + e.toString() );
            return "-2";
        } catch ( NullPointerException e ) {
            logger.error( "erreur d'envoie/reception du message au serveur : " + e.toString() );
            return "-2";
        }

        return message;

    }

}
