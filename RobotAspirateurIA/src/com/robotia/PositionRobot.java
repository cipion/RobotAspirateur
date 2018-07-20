package com.robotia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionRobot {

    private PositionRobot()
    {

    }

    // varialbe pour les logs
    private static final Logger  logger    = LoggerFactory.getLogger( PositionRobot.class );

    // Instance a partager aux autres class = singleton
    private static PositionRobot INSTANCE  = new PositionRobot();
    private int                  PositionX = 0;
    private int                  PositionY = 0;
    private int                  Angle     = 0;

    public static PositionRobot getInstancePosition()
    {
        return INSTANCE;
    }

    public int getAngle() {
        return Angle;
    }

    public int getPositionX() {
        return PositionX;
    }

    public int getPositionY() {
        return PositionY;
    }

    /****
     * Fonction qui permet de mettre à jour la position du robot sur l'axe X et
     * Y. Le nombres de pas est recu en parametre et l'axe impacté est
     * selectionné en fonction de l'angle.
     * 
     * @param nbPas
     */
    public void ajouterNbPAs( int nbPas )
    {
        if ( nbPas > 0 )
        {
            if ( Angle == -90 )
            {
                logger.info( "ajouterNbPAs : Angle = " + Angle + ", ajout de " + nbPas + "pas sur l'axe Y" );
                PositionY -= nbPas;
            }
            else if ( Angle == 90 )
            {
                logger.info( "ajouterNbPAs : Angle = " + Angle + ", soustraction de " + nbPas + "pas sur l'axe Y" );
                PositionY += nbPas;
            }
            else if ( Angle == 0 )
            {
                logger.info( "ajouterNbPAs : Angle = " + Angle + ", ajout de " + nbPas + "pas sur l'axe X" );
                PositionX += nbPas;
            }
            else if ( Angle == 180 || Angle == -180 )
            {
                logger.info( "ajouterNbPAs : Angle = " + Angle + ", soustration de " + nbPas + "pas sur l'axe X" );
                PositionX -= nbPas;
            }
            else
            {
                logger.error( "ajouterNbPAs : Erreur , la variable Angle n'a pas une valeur valide : Angle=" + Angle );
            }
        }
        else
        {
            logger.error( "ajouterNbPAs : Erreur le nombre de pas doit etre > 0" );
        }
    }

    /****
     * Fonction qui permet de metre à jour l'angle en fonctio nde l'angle actuel
     * et de celui envoyé en parametre.
     * 
     * @param angle
     */
    public void setAngle( int angle ) {

        if ( angle == 90 || angle == -90 || angle == 180 || angle == -180 )
        {
            logger.info( "setAngle : Angle actuel =" + Angle + ", demande de modification d'angle valide :" + angle );

            if ( Angle == 90 )
            {
                if ( angle == 180 || angle == -180 )
                {
                    Angle = -90;
                }
                else if ( angle == 90 )
                {
                    Angle = 180;
                }
                else if ( angle == -90 )
                {
                    Angle = 0;
                }
                else
                {
                    logger.error( "setAngle : erreur de calcul d'angle (90)" );
                }

            }
            else if ( Angle == -90 )
            {
                if ( angle == 180 || angle == -180 )
                {
                    Angle = 90;
                }
                else if ( angle == 90 )
                {
                    Angle = 0;
                }
                else if ( angle == -90 )
                {
                    Angle = 180;
                }
                else
                {
                    logger.error( "setAngle : erreur de calcul d'angle (-90)" );
                }
            }
            else if ( Angle == 180 )
            {
                if ( angle == 180 || angle == -180 )
                {
                    Angle = 0;
                }
                else if ( angle == 90 )
                {
                    Angle = -90;
                }
                else if ( angle == -90 )
                {
                    Angle = 90;
                }
                else
                {
                    logger.error( "setAngle : erreur de calcul d'angle (180)" );
                }
            }
            else if ( Angle == -180 )
            {
                if ( angle == 180 || angle == -180 )
                {
                    Angle = 0;
                }
                else if ( angle == 90 )
                {
                    Angle = -90;
                }
                else if ( angle == -90 )
                {
                    Angle = 90;
                }
                else
                {
                    logger.error( "setAngle : erreur de calcul d'angle (-180)" );
                }
            }
            else if ( Angle == 0 )
            {
                Angle = angle;
            }
            else
            {
                logger.error( "setAngle : erreur , la variable Angle n'est pas correcte Angle=" + Angle );
            }

            logger.debug( "setAngle : Angle final du robot =" + Angle );
        }
        else
        {
            logger.error( "setAngle : angle invalide :" + angle );
        }

    }

}
