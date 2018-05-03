package com.robotia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadNettoyage extends Thread {

    private static final Logger logger = LoggerFactory.getLogger( ThreadNettoyage.class );
    private GestionMouvement    gestionM;
    private static ClientSocket socket = null;

    @Override
    public void run() {
        logger.info( "run : ###################################################" );
        logger.info( "run : Debut du nettoyage" );

        DriverManager.getInstanceDriverManager().runAllDriver();

        socket = Main.getSocket();
        gestionM = Main.getGestionM();

        if ( !socket.initSocket() )
        {
            logger.error( "run : Erreur d'initialisation du socket" );
            socket.stopSocket();
        }

        gestionM.nettoyage();

        logger.info( "run : Arret du programme de nettoyage" );
        gestionM.StopGestionMouvement();

        logger.info( "run : Position du robot : X=" + PositionRobot.getInstancePosition().getPositionX() + ", Y="
                + PositionRobot.getInstancePosition().getPositionY() + ", Angle="
                + PositionRobot.getInstancePosition().getAngle() );

        DriverManager.getInstanceDriverManager().stopAllDriver();
        logger.info( "run : ###################################################" );

    }
}
