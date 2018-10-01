package com.robotia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadRetourBase extends Thread {

    private static final Logger logger = LoggerFactory.getLogger( ThreadRetourBase.class );
    private GestionMouvement    gestionM;
    private static ClientSocket socket = null;

    @Override
    public void run() {
        logger.info( "run : ###################################################" );
        logger.info( "run : Debut du retour à la base" );
        // DriverManager.getInstanceDriverManager().runAllDriver();

        // objet pour communiquer avec les drivers moteur et capteur
        socket = Main.getSocket();

        // objet pour gerer les mouvements du robot
        gestionM = Main.getGestionM();

        if ( !socket.initSocket() )
        {
            logger.error( "run : Erreur d'initialisation du socket" );
            socket.stopSocket();
        }

        // lancement du retour a la base du robot
        gestionM.retourAlaBase();

        logger.info( "run : Arret du programme du retour à la base" );

        // on souhaite arreter les action en cours
        gestionM.StopGestionMouvement();

        logger.info( "run : Position du robot : X=" + PositionRobot.getInstancePosition().getPositionX() + ", Y="
                + PositionRobot.getInstancePosition().getPositionY() + ", Angle="
                + PositionRobot.getInstancePosition().getAngle() );

        // on envoie un signal aux driver pour arreter les deplacement en cours
        gestionM.StopGestionMouvement();

        // on arrete les drivers
        // DriverManager.getInstanceDriverManager().stopAllDriver();
        logger.info( "run : ###################################################" );

    }

}
