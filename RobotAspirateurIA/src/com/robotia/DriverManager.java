package com.robotia;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DriverManager {

    private String               runCommande;
    private static DriverManager INSTANCE = new DriverManager();
    private ResourceBundle       bundle;
    private static final Logger  logger   = LoggerFactory.getLogger( DriverManager.class );
    private String               dossierDriverMoteur;
    private String               dossierDriverCapteur;
    private String               executableDriverMoteur;
    private String               executableDriverCapteur;
    private String               fihierLogDriverCapteur;
    private String               fihierLogDriverMoteur;

    public DriverManager() {
        logger.info( "DriverManager : Initialisation des properties des drivers" );

        bundle = ResourceBundle.getBundle( "config" );
        dossierDriverMoteur = bundle.getString( "driver.dossierDriverMoteur" );
        dossierDriverCapteur = bundle.getString( "driver.dossierDriverCapteur" );
        executableDriverMoteur = bundle.getString( "driver.executableDriverMoteur" );
        executableDriverCapteur = bundle.getString( "driver.executableDriverCapteur" );
        fihierLogDriverMoteur = bundle.getString( "driver.fihierLogDriverMoteur" );
        fihierLogDriverCapteur = bundle.getString( "driver.fihierLogDriverCapteur" );
    }

    public static DriverManager getInstanceDriverManager()
    {
        return INSTANCE;
    }

    public Boolean runAllDriver()
    {
        String retour;
        logger.info( "runAllDriver : Démarrage des drivers ..." );
        runCommande = "./" + executableDriverMoteur + " > " + fihierLogDriverMoteur;
        retour = executeCommand( runCommande, dossierDriverMoteur );

        runCommande = "./" + executableDriverCapteur + " > " + fihierLogDriverCapteur;
        retour = executeCommand( runCommande, dossierDriverCapteur );

        logger.info( "runAllDriver : Drivers démarrés" );
        return true;
    }

    public Boolean restartDriverMoteur()
    {
        String retour;

        runCommande = "./" + executableDriverMoteur + " > " + fihierLogDriverMoteur;

        retour = executeCommand( runCommande, dossierDriverMoteur );

        return true;
    }

    public Boolean restartDriverCapteur()
    {
        String retour;

        runCommande = "./" + executableDriverCapteur + " > " + fihierLogDriverCapteur;
        retour = executeCommand( runCommande, dossierDriverCapteur );

        return true;
    }

    private String executeCommand( String command, String dossier ) {

        logger.info( "executeCommand : passage de la commande linux : " + command );
        StringBuffer output = new StringBuffer();

        Process p;
        try {

            p = Runtime.getRuntime().exec( command, null, new File( "/logiciel/Driver" ) );
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader( new InputStreamReader( p.getInputStream() ) );

            String line = "";
            while ( ( line = reader.readLine() ) != null ) {
                output.append( line + "\n" );
            }

        } catch ( Exception e ) {

            logger.error( "executeCommand : Erreur lors de l'execution de la commande " + e );
        }

        return output.toString();

    }

    public Boolean stopAllDriver()
    {

        return stopDriverMoteur() && stopDriverCapteur();

    }

    public Boolean stopDriverMoteur()
    {
        return null;

    }

    public Boolean stopDriverCapteur()
    {
        return null;

    }
}
