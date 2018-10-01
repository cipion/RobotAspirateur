package com.robotia;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/****
 * Class qui permet de demarrer les drivers C++ pour piloter les moteur et les
 * capteurs
 * 
 * @author Administrateur
 * 
 */
public class DriverManager {

    private String               runCommande;

    // cet objet permet de generer un singleton, un simple instance disponible
    // depuis tous les objet du programme.
    private static DriverManager INSTANCE = new DriverManager();
    // objet pour stocker toutes les properties
    private ResourceBundle       bundle;

    // objet pour les log
    private static final Logger  logger   = LoggerFactory.getLogger( DriverManager.class );

    private String               dossierDriverMoteur;
    private String               dossierDriverCapteur;
    private String               executableDriverMoteur;
    private String               executableDriverCapteur;
    private String               fihierLogDriverCapteur;
    private String               fihierLogDriverMoteur;

    /****
     * constructeur qui charge les properties, qui recupere les nom des
     * dossiers, des executables ...
     */
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

    /****
     * methode permettant de recuperer l'instance du singleton
     * 
     * @return
     */
    public static DriverManager getInstanceDriverManager()
    {
        return INSTANCE;
    }

    /****
     * methode pour démarrer les deux driver (moteur et capteur)
     * 
     * @return vrai s'il n'y a pas de probleme d'exection des commandes
     */
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

    /****
     * methode qui arrete le driver moteur puis les redémarres.
     * 
     * TODO : finir le developpement
     * 
     * @return
     */
    public Boolean restartDriverMoteur()
    {
        String retour;

        runCommande = "./" + executableDriverMoteur + " > " + fihierLogDriverMoteur;

        retour = executeCommand( runCommande, dossierDriverMoteur );

        return true;
    }

    /****
     * methode qui arrete le driver des capteurs puis le redémarre
     * 
     * @return
     */
    public Boolean restartDriverCapteur()
    {
        String retour;

        runCommande = "./" + executableDriverCapteur + " > " + fihierLogDriverCapteur;
        retour = executeCommand( runCommande, dossierDriverCapteur );

        return true;
    }

    /****
     * methode qui execute la commandes envoyées en parametre sur l'OS linux
     * TODO : finir le dev
     * 
     * @param command
     * @param dossier
     * @return
     */
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

    /****
     * methode pour arreter tous les drivers (moteur et capteur)
     * 
     * @return
     */
    public Boolean stopAllDriver()
    {

        return stopDriverMoteur() && stopDriverCapteur();

    }

    /****
     * methode pour arreter le driver moteur
     * 
     * @return
     */
    public Boolean stopDriverMoteur()
    {
        return null;

    }

    /****
     * methode pour arreter le driver capteur
     * 
     * @return
     */
    public Boolean stopDriverCapteur()
    {
        return null;

    }
}
