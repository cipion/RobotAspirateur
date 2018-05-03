package com.robotia;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger     logger = LoggerFactory.getLogger( Main.class );
    private static ClientSocket     socket = null;
    private static GestionMouvement gestionM;

    public static void main( String[] args ) {
        logger.info( "Démarrage du programme RobotAspirateur IA" );
        socket = new ClientSocket();
        gestionM = new GestionMouvement();
        // DriverManager.getInstanceDriverManager().runAllDriver();

        ThreadNettoyage nettoyage = new ThreadNettoyage();
        nettoyage.start();

        try {
            TimeUnit.SECONDS.sleep( 20 );
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.info( "Demande d'arret du nettoyage" );
        GestionMouvement.setContinuerTraitement( false );

        try {
            TimeUnit.SECONDS.sleep( 10 );
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        ThreadRetourBase retourBase = new ThreadRetourBase();
        retourBase.start();

    }

    /*****
     * Fonction de partage du singleton du socket client
     * 
     * @return
     */
    public static ClientSocket getSocket()
    {
        return socket;

    }

    public static GestionMouvement getGestionM() {
        return gestionM;
    }

}
