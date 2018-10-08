package com.robotia;

import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GestionMouvement {

    private ClientSocket        socket              = null;
    private static final Logger logger              = LoggerFactory.getLogger( GestionMouvement.class );
    private int                 nbPas               = 0;
    private ResourceBundle      bundle;
    private PositionRobot       position;
    private int                 timeoutRotation     = 0;
    private int                 timeoutDecalage     = 0;
    private int                 NBPas_obj;
    private static Boolean      continuerTraitement = true;
    private static Boolean      stopDetection       = false;
    private Boolean             sysrun              = true;
    private Boolean             obstacleDetecte     = false;
    // si les mouvement etait une rotation, alors on ne soustrait pas les pas à
    // la position.
    private Boolean             rotationEnCours     = false;
    private Boolean             ajoutDesPas         = true;

    private int                 nbPasDecalage       = 0;                                                // on

    // ajoute
    // ou
    // soustrait
    // les
    // pas

    /****
     * Constructeur qui recupere le socket initialisé par la class ClientSocket,
     * qui recupere les properties (le timeout de detection) et recupere la
     * position du robot.
     */
    public GestionMouvement() {
        logger.info( "GestionMouvement : Initialisation du gestionnaire des mouvements" );
        socket = Main.getSocket();

        logger.info( "GestionMouvement : Initialisation des varialbes des sockets" );

        bundle = ResourceBundle.getBundle( "config" );

        position = PositionRobot.getInstancePosition();
        timeoutRotation = Integer.parseInt( bundle.getString( "gestionMouvement.timeoutRotation" ) );
        timeoutDecalage = Integer.parseInt( bundle.getString( "gestionMouvement.timeoutDecalage" ) );
        nbPasDecalage = Integer.parseInt( bundle.getString( "gestionMouvement.nbPasDecalage" ) );
    }

    /****
     * fonction qui demande a l'objet client socket d'arreter la connexion avec
     * les drivers.
     * 
     * @return True si l'arret est fait, sinon false
     */
    public Boolean StopGestionMouvement()
    {
        if ( socket.stopSocket() )
        {
            logger.info( "StopGestionMouvement : Arret de la commande des mouvements" );
            return true;
        }
        else
        {
            logger.error( "StopGestionMouvement : Erreur lors de l'arret de la commande des mouvements" );
            return false;
        }
    }

    /***
     * Fonction de rotation sur place de 90°, -90° ou 180°
     * 
     * @param degres
     * @return true si la rotation est faite sinon false
     */
    private Boolean rotationRobot( int degres )
    {
        rotationEnCours = true;

        if ( degres == 90 || degres == -90 )
        {
            nbPas = Integer.parseInt( bundle.getString( "socket.driverMoteur.nbPas90" ) );

            logger.debug( "rotationRobot : degres est de " + degres + ", le nombre de pas est de " + nbPas );

            PositionRobot.getInstancePosition().setAngle( degres );

            return socket.setDriverMoteurStatut( Commandes.ROTATE, 85, degres, nbPas );
        }
        else if ( degres == 180 || degres == -180 )
        {
            nbPas = Integer.parseInt( bundle.getString( "socket.driverMoteur.nbPas180" ) );

            logger.debug( "rotationRobot : degres est de " + degres + ", le nombre de pas est de " + nbPas );

            PositionRobot.getInstancePosition().setAngle( degres );

            return socket.setDriverMoteurStatut( Commandes.ROTATE, 85, degres, nbPas );
        }
        else
        {
            logger.error( "rotationRobot : Erreur, l'angle doit etre de 90, -90 ou 180" );
            return false;
        }

    }

    /****
     * fonction qui permet de faire une rotation de "nbPas" dans la direction
     * "direction"
     * 
     * @param direction
     * @param nbPas
     * @return
     */
    private Boolean rotationRobotnbPas( int direction, int nbPas )
    {
        rotationEnCours = true;
        logger.info( "rotationRobotnbPas : rotation de " + direction + "% pour " + nbPas + "pas" );
        return socket.setDriverMoteurStatut( Commandes.START, 100, direction, nbPas );
    }

    /****
     * Fonction qui fait avancer les robot en fonction du nobmre de pas demandés
     * S'il n'y a pas de valeur de pas indiqué, le robot avance idefiniement
     * 
     * @param nbPas
     *            (0 pour infini)
     * @return
     */
    private Boolean marcheAvantRobot()
    {
        rotationEnCours = false;

        String retour = socket.setDriverMoteurStatut( Commandes.START, 100, 0 );

        if ( retour.equals( "1" ) )
        {
            return true;
        }
        else
            return false;
    }

    /****
     * Fonction qui permet de faire un deplacement en avant de "nbPas" pas
     * 
     * @param nbPas
     * @return true si le deplacement est fait
     */
    private Boolean marcheAvantRobot( int nbPas )
    {
        rotationEnCours = false;

        FutureTask<Boolean> timeoutTask = null;
        Boolean statut = false;
        NBPas_obj = nbPas;
        stopDetection = false;

        timeoutTask = new FutureTask<Boolean>( new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                logger.debug( "marcheAvantRobot : lancement du thread de marche avant avec " + NBPas_obj + " pas " );
                Boolean msg = socket.setDriverMoteurStatut( Commandes.START, 100, 0, Math.abs( NBPas_obj ) );

                if ( msg )
                {
                    stopDetection = true;
                }

                return msg;
            }
        } );

        new Thread( timeoutTask ).start();

        return true;

    }

    /****
     * Fonction qui arrete le deplacement du robot
     * 
     * @return true si l'arret est fait
     */
    private Boolean arretRobot()
    {

        String retour1 = socket.stopDetection();
        String retour2 = socket.setDriverMoteurStatut( Commandes.STOP, 0, 0 );

        logger.info( "arretRobot : arret des Capteur =" + retour1 + ", arret des moteur =" + retour2 );

        if ( !retour2.equals( "-1" ) )
        {
            String delims = "[;]";
            String[] tokens = retour2.split( delims );
            int mot1 = Integer.parseInt( tokens[0] );
            int mot2 = Integer.parseInt( tokens[1] );

            int nbPas = ajusterPosition( mot1, mot2 );

            if ( nbPas == -1 )
            {
                logger.error( "arretRobot : erreur lors du traitement de l'ajustement" );
                return false;
            }

            if ( !rotationEnCours )
            {
                logger.debug( "arretRobot : on ajoute les pas :" + nbPas );
                PositionRobot.getInstancePosition().ajouterNbPAs( nbPas );
            }
            else
            {
                logger.debug( "arretRobot : on n'ajoute aucun pas car c'est une rotation" );
            }
            rotationEnCours = false;
            return true;
        }
        else
            return true;

    }

    /****
     * Fonction qui genere un thread pour attendre la detection d'obstacle par
     * un capteur. le thred de detection peu etre killer si continuerTraitement
     * est faux ou si stopDetection est vrai
     * 
     * @return 1 si la fonction de detection est arretée, 0 si une detection est
     *         faite et -1 s'il y a un probleme
     */
    private int detectionObstacle()
    {
        // créaion du Thread qui peut etre killer
        FutureTask<String> timeoutTask = null;
        Boolean statut = false;

        timeoutTask = new FutureTask<String>( new Callable<String>() {

            @Override
            public String call() throws Exception {
                logger.debug( "detectionObstacle : lancement des detections des capteurs sans timeout " );
                String msg = socket.getDetectionObstacle();

                return msg;
            }
        } );

        new Thread( timeoutTask ).start();

        while ( !timeoutTask.isDone() )
        {
            // logger.info( "Test cancel =" + continuerTraitement );
            if ( !continuerTraitement || stopDetection )
            {
                statut = timeoutTask.cancel( true );
                logger.info( "detectionObstacle : Demande d'arret du traitement =" + statut );
            }

        }

        if ( statut )
        {
            logger.info( "detectionObstacle : Fonction getDetectionObstacle arretée" );
            return 1;
        }
        else if ( timeoutTask.isDone() )
        {
            logger.info( "detectionObstacle : Arret normal du thread de detection, thread arreté" );
            return 0;
        }
        else
        {
            logger.error( "detectionObstacle : Erreur d'arret de la fonction " );
            continuerTraitement = false;
            return -1;
        }

    }

    /****
     * Cette fonction ajuste la position du robot apres une ligne droite, en
     * effet chaque moteur est géré par un thread, et il est possible qu'a la
     * fin de cette ligne droite les deux thread n'aient pas fait le même nombre
     * de pas.
     * 
     * @param mot1
     *            : nombre de pas du mot 1
     * @param mot2
     *            : nombre de pas du mot 2
     * @return -1 s'il y a un probleme de calcul, sinon le nombre de pas à
     *         incrémenter dans les coordonnées.
     */
    private int ajusterPosition( int mot1, int mot2 )
    {
        // si les deux moteurs ont fait le même nombre de pas alors on retourne
        // le nombre de pas à incrementer.
        if ( mot1 == mot2 )
        {
            logger.info( "ajusterPosition : pas d'ajustement à faire, le nombre de pas des deux moteur est identique" );
            return mot1;
        }
        // si le mot 1 à fait plus de pas que le mot 2 alors il faut faire
        // torner le mot 2
        else if ( mot1 > mot2 )
        {
            int nbpas = mot1 - mot2;
            logger.info( "ajusterPosition : le moteur 1 a fait " + nbpas + " pas de plus que le moteur 2" );
            Boolean retour = rotationRobotnbPas( -100, nbpas );
            // on fait tourner de nbpas le moteur 2 en indiquand -100 afin de
            // faire tourner seulement le moteur 2
            rotationEnCours = false; // ce n'est pas une vrai rotation, on
            // souhaite soustraire les pas, donc on n'enregistre pas la rotation
            // dans les coordonnées.
            if ( retour )
            {

                return mot1;
            }
            else
                return -1;

        }
        // si le mot 2 à fait plus de pas que le mot 1 alors il faut faire
        // torner le mot 1
        else if ( mot2 > mot1 )
        {
            int nbpas = mot2 - mot1;
            logger.info( "ajusterPosition : le moteur 2 a fait " + nbpas + " pas de plus que le moteur 1" );
            Boolean retour = rotationRobotnbPas( 100, nbpas );
            // on fait tourner de nbpas le moteur 1 en indiquand 100 afin de
            // faire tourner seulement le moteur 1
            rotationEnCours = false;
            if ( retour )
            {

                return mot2;
            }
            else
                return -1;
        }
        else
        {
            logger.error( "ajusterPosition : Erreur lors du calcul d'ajustement" );
            return -1;
        }

    }

    /****
     * fonction de detection d'obstacle avec un parametre de timeout. si le
     * timeout est dépasé alors le thread est killé et la methode est se
     * termine.
     * 
     * @param timeout
     * @return -1 s'il y a un probleme sur le thread, -2 si le timeout est
     *         dépassé et 0 si un obstacle est detecté.
     */
    public int detectionObstacle( int timeout )
    {
        FutureTask<String> timeoutTask = null;
        String msg = "-3";

        try {
            timeoutTask = new FutureTask<String>( new Callable<String>() {

                @Override
                public String call() throws Exception {
                    logger.debug( "detectionObstacle : lancement des detections des capteurs avec timeout: " );
                    String msg = socket.getDetectionObstacle( timeoutRotation );

                    return msg;
                }
            } );

            new Thread( timeoutTask ).start();
            msg = timeoutTask.get( timeout, TimeUnit.SECONDS );

        } catch ( InterruptedException e ) {

        } catch ( ExecutionException e ) {
            logger.error( "detectionObstacle : Erreur d'execussion de la detection d'obstacle : " + e.toString() );
            return -1;

        } catch ( TimeoutException e ) {
            logger.debug( "detectionObstacle : Timeout dépassé, pas d'obstacle detecté :" + e.toString() );
            return -2;
        }

        socket.stopDetection();
        if ( msg.equals( "-1" ) )
        {
            logger.error( "detectionObstacle : Erreur, le driver de capteur à rencontré un probleme" );
            return -1;
        }
        else if ( msg.equals( "-2" ) )
        {
            logger.error( "detectionObstacle : Erreur d'execution de connexion au driver" );
            continuerTraitement = false;
            return -1;
        }
        else if ( msg.equals( "-3" ) )
        {
            logger.error( "detectionObstacle : Erreur d'execution du tread de timeout" );
            return -1;
        }
        else
        {
            logger.debug( "detectionObstacle : Obstacle detecté : " + msg );
            return 0;
        }

    }

    /****
     * fonction permettant au robot de faire le menage, voir la documentation
     * pour comprendre l'algorithme de netoyyage A chaque evennement du trajet
     * (obstacle, rotation ...) les coordonnées sont mis à jour pour qe le robot
     * puisse se reperer dans l'espace (nombree de pas en X et Y et
     * l'orientation en degres)
     * 
     * @return vrai si le nettoyage c'est bien déroulé, sinon faux
     */

    public Boolean nettoyage()
    {
        logger.debug( "-----------------" );
        logger.debug( "nettoyage : Debut du nettoyage, marche avant" );
        int i = 0;
        ajoutDesPas = true; // on ajoute les pas

        while ( sysrun )
        {
            logger.debug( "-----------------" );
            marcheAvantRobot();
            logger.debug( "-----------------" );
            detectionObstacle();
            logger.debug( "-----------------" );
            if ( !continuerTraitement )
            {
                logger.info( "nettoyage : Demande d'arret du traitement" );
                arretRobot();
                logger.debug( "-----------------" );
                return true;
            }
            else
            {
                logger.debug( "arret" );
                arretRobot();
                logger.debug( "-----------------" );

                Boolean obstacle = true;

                do
                {
                    logger.debug( "rotation 90" );
                    rotationRobot( 90 );
                    logger.debug( "-----------------" );

                    int retourDetection = detectionObstacle( timeoutRotation );

                    if ( !continuerTraitement )
                    {
                        logger.info( "nettoyage : Demande d'arret du traitement" );

                        return arretRobot();

                    }
                    else if ( continuerTraitement && retourDetection == 0 )
                    {
                        logger.debug( "nettoyage : detection obstacle à 90° alors rotation a 180" );
                        rotationRobot( 180 );
                        logger.debug( "-----------------" );

                        retourDetection = detectionObstacle( timeoutRotation );
                        logger.debug( "-----------------" );

                        if ( !continuerTraitement )
                        {
                            logger.info( "nettoyage : Demande d'arret du traitement" );
                            arretRobot();
                            logger.debug( "-----------------" );
                            return true;
                        }
                        else if ( continuerTraitement && retourDetection == 0 ) {
                            logger.debug( "nettoyage : detection obstacle à 180° alors rotation a 90°" );
                        }
                        else
                        {
                            logger.debug( "nettoyage : Pas d'obstacle, on peut avancer tout droit" );
                            obstacle = false;
                        }
                    }
                    else if ( continuerTraitement && retourDetection == -2 )
                    {
                        logger.info( "nettoyage : Pas dobstacle, on reboucle pour la merche avant" );
                        obstacle = false;
                    }
                    else
                    {
                        logger.error( "nettoyage : Erreur lors du traitement de la detection d'obstacle avec timeout : "
                                + retourDetection );
                        return false;
                    }
                } while ( obstacle );

            }
        }

        return false;

    }

    /****
     * fonction static permettant d'arreter le traitement des threads en cours
     * (detection d'objet) depuis le processus principale ayant lancé le
     * nettoyage
     * 
     * Si varContinuerTraitement est faux, alors les threads sont arretés
     * 
     * @param varContinuerTraitement
     */
    public static void setContinuerTraitement( Boolean varContinuerTraitement ) {
        continuerTraitement = varContinuerTraitement;
    }

    /****
     * methode permettant au robot de retourner au point de départ en utilisant
     * les coordonnées qui ont été incrémentées lors du nettoyage
     * 
     * @return
     */
    public Boolean retourAlaBase()
    {
        Boolean processRetour = true;

        logger.info( "retourAlaBase : Debut du retour à la base" );
        ajoutDesPas = false; // on soustrait les pas
        continuerTraitement = true;
        int positionX = 0;
        int positionY = 0;
        int angle = 0;

        if ( PositionRobot.getInstancePosition().getPositionX() != 0
                && PositionRobot.getInstancePosition().getPositionY() != 0 )
        {
            do
            {
                positionX = PositionRobot.getInstancePosition().getPositionX();
                positionY = PositionRobot.getInstancePosition().getPositionY();
                angle = PositionRobot.getInstancePosition().getAngle();
                logger.debug( "retourAlaBase : positionX =" + positionX + ", postionY =" + positionY + ", angle ="
                        + angle );

                if ( angle == 0 )
                {

                    if ( positionX > 0 )
                    {
                        logger.debug( "retourAlaBase : positionX > 0, angle du robot = 0 donc demi tour (180°)" );
                        if ( rotationRobot( 180 ) )
                        {
                            logger.debug( "retourAlaBase : rotation 180° faite" );
                        }
                        else
                        {
                            logger.error( "retourAlaBase : Erreur lors de la rotation" );
                            return false;
                        }
                    }
                    else if ( positionX < 0 )
                    {
                        logger.debug( "retourAlaBase : Robot dans le bon angle (0°)" );

                        logger.debug( "retourAlaBase : Le robot avance tout droit, marche avant de "
                                + Math.abs( positionX ) + " pas" );
                        // Avancer tout droit
                        marcheAvantRobot( Math.abs( positionX ) );

                        int retourDetection = detectionObstacle();

                        if ( retourDetection == 0 ) // obstacle détécté
                        {
                            logger.debug( "retourAlaBase : demande d'arret du robot" );
                            arretRobot(); // une fois la detection faite ou
                        }

                        while ( continuerTraitement )
                        {

                            // annulée
                            // on met à jour les coordonée via la
                            // fontion d'arret

                            positionX = PositionRobot.getInstancePosition().getPositionX();
                            positionY = PositionRobot.getInstancePosition().getPositionY();
                            angle = PositionRobot.getInstancePosition().getAngle();
                            logger.debug( "" );
                            logger.debug( "retourAlaBase : positionX =" + positionX + ", postionY =" + positionY
                                    + ", angle ="
                                    + angle );

                            logger.debug( "retourAlaBase : Debut de la boucle, traitement en cours (continuerTraitement = TRUE)" );

                            if ( retourDetection == 0 && positionX != 0 )
                            {

                                logger.info( "retourAlaBase : obstacle detecté et positionX est different de 0, analyse de l'angle ..." );

                                if ( angle == 0 )
                                {

                                    logger.debug( "retourAlaBase : position > 0, detection obstacle à 0° alors rotation de -90°" );
                                    do
                                    {

                                        rotationRobot( -90 );

                                        retourDetection = detectionObstacle( timeoutRotation );
                                        if ( retourDetection == 0 )
                                            logger.debug( "retourAlaBase : angle=0, obstacle détécté, on recommence la rotation de -90" );

                                    } while ( retourDetection == 0 );

                                }
                                else if ( angle == 180 )
                                {
                                    if ( positionX > 0 )
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 180° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=180, obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : positionX < 0, detection obstacle à 180° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=180, obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );
                                    }

                                }
                                else if ( angle == 90 )
                                {
                                    if ( positionX > 0 )
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 90° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=90, obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : positionX < 0, detection obstacle à 90° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=90, obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );
                                    }

                                }
                                else if ( angle == -90 )
                                {
                                    if ( positionX > 0 )
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 90° alors rotation de -90° à nouveau" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=-90, obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 90° alors rotation de 90° à nouveau" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=-90, obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );
                                    }

                                }
                                else
                                {
                                    logger.error( "retourAlaBase : Error, l'angle est incorect" );
                                    continuerTraitement = false;
                                }

                                if ( !continuerTraitement )
                                {
                                    logger.info( "retourAlaBase : Demande d'arret du traitement" );

                                    return arretRobot();

                                }

                                if ( angle == 90 || angle == -90 )
                                {
                                    logger.debug( "retourAlaBase : le robot n'est pas dans l'axe X alors on avance sur Y de seulement "
                                            + nbPasDecalage + " pas" );
                                    marcheAvantRobot( nbPasDecalage );

                                    retourDetection = detectionObstacle( timeoutDecalage );
                                    logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                            + " demande d'arret du robot" );
                                    if ( retourDetection != 0 )
                                    {
                                        arretRobot(); // une fois la
                                                      // detection
                                                      // faite ou
                                    }
                                }
                                else
                                {
                                    logger.debug( "retourAlaBase : le robot est dans l'axe X, alors on avance de "
                                            + Math.abs( positionX ) + " pas" );
                                    marcheAvantRobot( Math.abs( positionX ) );

                                    retourDetection = detectionObstacle();
                                    logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                            + " demande d'arret du robot" );
                                    if ( retourDetection != 0 )
                                    {
                                        arretRobot(); // une fois la detection
                                                      // faite ou
                                    }
                                }

                                logger.debug( "retourAlaBase : marche avant" );
                            }
                            else if ( positionX == 0 && positionY != 0 )
                            {
                                logger.info( "retourAlaBase : obstacle detecté, positionX = 0 et positionY differente de 0, on analyse l'angle" );
                                if ( positionY > 0 )
                                {
                                    if ( angle == 0 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 0° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 180° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 90 )
                                    {

                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de 180°" );
                                        rotationRobot( 180 );

                                    }
                                    else if ( angle == -90 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de 90° à nouveau" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else
                                    {
                                        logger.error( "retourAlaBase : Error, l'angle est incorect" );
                                        continuerTraitement = false;
                                    }

                                    if ( !continuerTraitement )
                                    {
                                        logger.info( "retourAlaBase : Demande d'arret du traitement" );

                                        return arretRobot();

                                    }

                                    if ( angle == 0 || angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : le robot n'est pas dans l'axe Y, alors on avance sur l'axe X de seulement "
                                                + nbPasDecalage + " pas" );
                                        marcheAvantRobot( nbPasDecalage );

                                        retourDetection = detectionObstacle( timeoutDecalage );
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : on est sur l'axe Y alors on avance de "
                                                + Math.abs( positionY ) + " pas" );
                                        marcheAvantRobot( Math.abs( positionY ) );

                                        retourDetection = detectionObstacle();
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }

                                }
                                else if ( positionY < 0 )
                                {
                                    if ( angle == 0 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 0° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 180° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 90 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == -90 )
                                    {

                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de 180° à nouveau" );
                                        rotationRobot( 180 );

                                    }
                                    else
                                    {
                                        logger.error( "retourAlaBase : Error, l'angle est incorect" );
                                        continuerTraitement = false;
                                    }

                                    if ( !continuerTraitement )
                                    {
                                        logger.info( "retourAlaBase : Demande d'arret du traitement" );

                                        return arretRobot();

                                    }

                                    if ( angle == 0 || angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : le robot n'est pas dans l'axe Y, alors on avance sur l'axe X de seulement "
                                                + nbPasDecalage + " pas" );
                                        marcheAvantRobot( nbPasDecalage );

                                        retourDetection = detectionObstacle( timeoutDecalage );
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : on est sur l'axe Y alors on avance de "
                                                + Math.abs( positionY ) + " pas" );
                                        marcheAvantRobot( Math.abs( positionY ) );

                                        retourDetection = detectionObstacle();
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }
                                }
                                else
                                {
                                    logger.error( "retourAlaBase : positionY invalide" );
                                    continuerTraitement = false;
                                }
                            }
                            else
                            {
                                logger.debug( "retourAlaBase : Fin du traitement de retour à la base" );
                                continuerTraitement = false;
                                processRetour = false;
                            }

                        }
                    }
                    else
                    {
                        logger.debug( "retourAlaBase : positionX = 0, aucune action a faire" );
                    }

                }

                else if ( angle == 90 )
                {
                    /*
                     * if ( positionY < 0 ) { logger.debug(
                     * "retourAlaBase : angle=90, position Y < 0, on avance de "
                     * + positionY + "pas" ); marcheAvantRobot( positionY ); }
                     * 
                     * detectionObstacle();
                     */

                    if ( positionX > 0 )
                    {
                        logger.debug( "retourAlaBase : positionX > 0, angle du robot = 90 donc quart de tour (90°)" );
                        if ( rotationRobot( 90 ) )
                        {
                            logger.debug( "retourAlaBase : rotation 90° faite" );
                        }
                        else
                        {
                            logger.error( "retourAlaBase : Erreur lors de la rotation" );
                            return false;
                        }
                    }
                    else if ( positionX < 0 )
                    {
                        logger.debug( "retourAlaBase : positionX < 0, angle du robot = 90 donc quart de tour (-90°)" );
                        if ( rotationRobot( -90 ) )
                        {
                            logger.debug( "retourAlaBase : rotation -90° faite" );
                        }
                        else
                        {
                            logger.error( "retourAlaBase : Erreur lors de la rotation" );
                            return false;
                        }
                    }
                    else
                    {
                        logger.debug( "retourAlaBase : positionX = 0, aucune action a faire" );
                    }

                }
                else if ( angle == -90 )
                {
                    /*
                     * if ( positionY > 0 ) { logger.debug(
                     * "retourAlaBase : angle=-90, position Y > 0, on avance de "
                     * + positionY + "pas" ); marcheAvantRobot( positionY ); }
                     */

                    if ( positionX > 0 )
                    {
                        logger.debug( "retourAlaBase : angle du robot = -90 donc quart de tour (-90°)" );
                        if ( rotationRobot( -90 ) )
                        {
                            logger.debug( "retourAlaBase : rotation -90° faite" );
                        }
                        else
                        {
                            logger.error( "retourAlaBase : Erreur lors de la rotation" );
                            return false;
                        }
                    }
                    else if ( positionX < 0 )
                    {
                        logger.debug( "retourAlaBase : angle du robot = -90 donc quart de tour (90°)" );
                        if ( rotationRobot( 90 ) )
                        {
                            logger.debug( "retourAlaBase : rotation 90° faite" );
                        }
                        else
                        {
                            logger.error( "retourAlaBase : Erreur lors de la rotation" );
                            return false;
                        }
                    }
                    else
                    {
                        logger.debug( "retourAlaBase : positionX = 0, aucune action a faire" );
                    }
                }
                else if ( angle == 180 )
                {
                    if ( positionX > 0 )
                    {

                        logger.debug( "retourAlaBase : Robot dans le bon angle (180°)" );

                        logger.debug( "retourAlaBase : Le robot avance tout droit, on avance de "
                                + Math.abs( positionX ) + " pas" );
                        // Avancer tout droit
                        marcheAvantRobot( Math.abs( positionX ) );

                        int retourDetection = detectionObstacle();
                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                + " demande d'arret du robot" );
                        if ( retourDetection != 0 )
                        {
                            arretRobot(); // une fois la detection
                                          // faite ou
                        }

                        while ( continuerTraitement )
                        {

                            // annulée
                            // on met à jour les coordonée via la
                            // fontion d'arret

                            positionX = PositionRobot.getInstancePosition().getPositionX();
                            positionY = PositionRobot.getInstancePosition().getPositionY();
                            angle = PositionRobot.getInstancePosition().getAngle();
                            logger.debug( "" );
                            logger.debug( "retourAlaBase : positionX =" + positionX + ", postionY =" + positionY
                                    + ", angle ="
                                    + angle );

                            logger.debug( "retourAlaBase : Debut de la boucle, traitement en cours (continuerTraitement = TRUE) et retourDetection="
                                    + retourDetection );

                            if ( positionX != 0 )
                            {

                                logger.info( "retourAlaBase : positionX est different de 0, analyse de l'angle ..." );

                                if ( angle == 0 )
                                {

                                    logger.debug( "retourAlaBase : position > 0, detection obstacle à 0° alors rotation de -90°" );
                                    do
                                    {

                                        rotationRobot( -90 );

                                        retourDetection = detectionObstacle( timeoutRotation );
                                        if ( retourDetection == 0 )
                                            logger.debug( "retourAlaBase : angle=0, obstacle détécté, on recommence la rotation de -90" );

                                    } while ( retourDetection == 0 );

                                }
                                else if ( angle == 180 )
                                {
                                    if ( positionX > 0 )
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 180° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=180, obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : positionX < 0, detection obstacle à 180° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=180, obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );
                                    }

                                }
                                else if ( angle == 90 )
                                {
                                    if ( positionX > 0 )
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 90° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=90, obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : positionX < 0, detection obstacle à 90° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=90, obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );
                                    }

                                }
                                else if ( angle == -90 )
                                {
                                    if ( positionX > 0 )
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 90° alors rotation de -90° à nouveau" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=-90, obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : positionX > 0, detection obstacle à 90° alors rotation de 90° à nouveau" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : angle=-90, obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );
                                    }

                                }
                                else
                                {
                                    logger.error( "retourAlaBase : Error, l'angle est incorect" );
                                    continuerTraitement = false;
                                }

                                if ( !continuerTraitement )
                                {
                                    logger.info( "retourAlaBase : Demande d'arret du traitement" );

                                    return arretRobot();

                                }

                                if ( angle == 90 || angle == -90 )
                                {
                                    logger.debug( "retourAlaBase : le robot n'est pas dans l'axe X alors on avance sur Y de seulement "
                                            + nbPasDecalage + " pas" );
                                    marcheAvantRobot( nbPasDecalage );

                                    retourDetection = detectionObstacle( timeoutDecalage );
                                    logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                            + " demande d'arret du robot" );
                                    if ( retourDetection != 0 )
                                    {
                                        arretRobot(); // une fois la
                                                      // detection
                                                      // faite ou
                                    }
                                }
                                else
                                {
                                    logger.debug( "retourAlaBase : le robot est dans l'axe X, alors on avance de "
                                            + Math.abs( positionX ) + " pas" );
                                    marcheAvantRobot( Math.abs( positionX ) );

                                    retourDetection = detectionObstacle();
                                    logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                            + " demande d'arret du robot" );
                                    if ( retourDetection != 0 )
                                    {
                                        arretRobot(); // une fois la detection
                                                      // faite ou
                                    }
                                }

                                logger.debug( "retourAlaBase : marche avant" );
                            }
                            else if ( positionX == 0 && positionY != 0 )
                            {
                                logger.info( "retourAlaBase : positionX = 0 et positionY differente de 0, on analyse l'angle" );
                                if ( positionY > 0 )
                                {
                                    if ( angle == 0 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 0° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 180° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 90 )
                                    {

                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de 180°" );
                                        rotationRobot( 180 );

                                    }
                                    else if ( angle == -90 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de 90° à nouveau" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else
                                    {
                                        logger.error( "retourAlaBase : Error, l'angle est incorect" );
                                        continuerTraitement = false;
                                    }

                                    if ( !continuerTraitement )
                                    {
                                        logger.info( "retourAlaBase : Demande d'arret du traitement" );

                                        return arretRobot();

                                    }

                                    if ( angle == 0 || angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : le robot n'est pas dans l'axe Y, alors on avance sur l'axe X de seulement "
                                                + nbPasDecalage + " pas" );
                                        marcheAvantRobot( nbPasDecalage );

                                        retourDetection = detectionObstacle( timeoutDecalage );
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : on est sur l'axe Y alors on avance de "
                                                + Math.abs( positionY ) + " pas" );
                                        marcheAvantRobot( Math.abs( positionY ) );

                                        retourDetection = detectionObstacle();
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }

                                }
                                else if ( positionY < 0 )
                                {
                                    if ( angle == 0 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 0° alors rotation de 90°" );
                                        do
                                        {

                                            rotationRobot( 90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de 90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : angle = 180° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == 90 )
                                    {
                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de -90°" );
                                        do
                                        {

                                            rotationRobot( -90 );

                                            retourDetection = detectionObstacle( timeoutRotation );
                                            if ( retourDetection == 0 )
                                                logger.debug( "retourAlaBase : obstacle détécté, on recommence la rotation de -90" );

                                        } while ( retourDetection == 0 );

                                    }
                                    else if ( angle == -90 )
                                    {

                                        logger.debug( "retourAlaBase : detection obstacle à 90° alors rotation de 180° à nouveau" );
                                        rotationRobot( 180 );

                                    }
                                    else
                                    {
                                        logger.error( "retourAlaBase : Error, l'angle est incorect" );
                                        continuerTraitement = false;
                                    }

                                    if ( !continuerTraitement )
                                    {
                                        logger.info( "retourAlaBase : Demande d'arret du traitement" );

                                        return arretRobot();

                                    }

                                    if ( angle == 0 || angle == 180 )
                                    {
                                        logger.debug( "retourAlaBase : le robot n'est pas dans l'axe Y, alors on avance sur l'axe X de seulement "
                                                + nbPasDecalage + " pas" );
                                        marcheAvantRobot( nbPasDecalage );

                                        retourDetection = detectionObstacle( timeoutDecalage );
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }
                                    else
                                    {
                                        logger.debug( "retourAlaBase : on est sur l'axe Y alors on avance de "
                                                + Math.abs( positionY ) + " pas" );
                                        marcheAvantRobot( Math.abs( positionY ) );

                                        retourDetection = detectionObstacle();
                                        logger.debug( "retourAlaBase : retourDetection= " + retourDetection
                                                + " demande d'arret du robot" );
                                        if ( retourDetection != 0 )
                                        {
                                            arretRobot(); // une fois la
                                                          // detection
                                                          // faite ou
                                        }
                                    }
                                }
                                else
                                {
                                    logger.error( "retourAlaBase : positionY invalide" );
                                    continuerTraitement = false;
                                }
                            }
                            else
                            {
                                logger.debug( "retourAlaBase : Fin du traitement de retour à la base" );
                                continuerTraitement = false;
                                processRetour = false;
                            }

                        }
                    }
                    else if ( positionX < 0 )
                    {
                        logger.debug( "retourAlaBase : positionX > 0, angle du robot = 0 donc demi tour (180°)" );
                        if ( rotationRobot( 180 ) )
                        {
                            logger.debug( "retourAlaBase : rotation 180° faite" );
                        }
                        else
                        {
                            logger.error( "retourAlaBase : Erreur lors de la rotation" );
                            return false;
                        }
                    }
                    else
                    {
                        logger.debug( "retourAlaBase : positionX = 0, aucune action a faire" );
                    }

                }
                else
                {
                    logger.error( "retourAlaBase : Erreur, l'angle n'est pas valide" );
                    return false;
                }
            } while ( processRetour );

        }
        else
        {
            logger.error( "retourAlaBase : Erreur le robot est en position nulle, ou deja à la base" );
            return false;
        }
        return false;

    }

    /****
     * Methode de debug des rotations, inutile au programme
     */
    public void testRotation()
    {
        logger.debug( "testRotation : rotation 180°" );
        rotationRobot( 180 );
        try {
            TimeUnit.SECONDS.sleep( 20 );
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.debug( "testRotation : rotation 90°" );
        rotationRobot( 90 );
        try {
            TimeUnit.SECONDS.sleep( 20 );
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.debug( "testRotation : rotation -90°" );
        rotationRobot( -90 );
        try {
            TimeUnit.SECONDS.sleep( 20 );
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        logger.debug( "testRotation : rotation 90°" );
        rotationRobot( 90 );
        try {
            TimeUnit.SECONDS.sleep( 20 );
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
