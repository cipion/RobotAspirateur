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

        socket = Main.getSocket();
        gestionM = Main.getGestionM();

        if ( !socket.initSocket() )
        {
            logger.error( "run : Erreur d'initialisation du socket" );
            socket.stopSocket();
        }

        gestionM.retourAlaBase();

        logger.info( "run : Arret du programme du retour à la base" );

        gestionM.StopGestionMouvement();

        logger.info( "run : Position du robot : X=" + PositionRobot.getInstancePosition().getPositionX() + ", Y="
                + PositionRobot.getInstancePosition().getPositionY() + ", Angle="
                + PositionRobot.getInstancePosition().getAngle() );

        gestionM.StopGestionMouvement();
        logger.info( "run : ###################################################" );

    }

}
