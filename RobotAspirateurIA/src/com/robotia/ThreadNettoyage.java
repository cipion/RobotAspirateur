package com.robotia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/****
 * methode qui genere un thread gérant la fnction de nettoyage du robot.
 * 
 * @author Administrateur
 * 
 */
public class ThreadNettoyage extends Thread {

    private static final Logger logger = LoggerFactory.getLogger( ThreadNettoyage.class );
    private GestionMouvement    gestionM;
    private static ClientSocket socket = null;

    @Override
    public void run() {
        logger.info( "run : ###################################################" );
        logger.info( "run : Debut du nettoyage" );

        // on demarre les drivers du robot
        // DriverManager.getInstanceDriverManager().runAllDriver();

        // on recupere le socket de communication avec les drivers
        socket = Main.getSocket();
        // objet de gestion des mouvenement pour permettre le nettoyage
        gestionM = Main.getGestionM();

        if ( !socket.initSocket() )
        {
            logger.error( "run : Erreur d'initialisation du socket" );
            socket.stopSocket();
        }

        // lancement du nettoyage
        gestionM.nettoyage();

        // on envoie un signal d'arret aux drivers
        logger.info( "run : Arret du programme de nettoyage" );
        gestionM.StopGestionMouvement();

        logger.info( "run : Position du robot : X=" + PositionRobot.getInstancePosition().getPositionX() + ", Y="
                + PositionRobot.getInstancePosition().getPositionY() + ", Angle="
                + PositionRobot.getInstancePosition().getAngle() );

        // procedure d'arret de la communication avec les drivers
        // DriverManager.getInstanceDriverManager().stopAllDriver();
        logger.info( "run : ###################################################" );

    }
}
