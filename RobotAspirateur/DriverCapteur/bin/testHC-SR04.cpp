/* 
 * Code d'exemple pour un capteur Ã  ultrasons HC-SR04.
 */

/* Constantes pour les broches */
const byte TRIGGER_PIN = 2; // Broche TRIGGER
const byte ECHO_PIN = 3;    // Broche ECHO
 
/* Constantes pour le timeout */
const unsigned long MEASURE_TIMEOUT = 25000UL; // 25ms = ~8m Ã  340m/s

/* Vitesse du son dans l'air en mm/us */
const float SOUND_SPEED = 340.0 / 1000;

/** Fonction setup() */
void setup() {
   
  /* Initialise le port sÃ©rie */
  Serial.begin(115200);
   
  /* Initialise les broches */
  pinMode(TRIGGER_PIN, OUTPUT);
  digitalWrite(TRIGGER_PIN, LOW); // La broche TRIGGER doit Ãªtre Ã  LOW au repos
  pinMode(ECHO_PIN, INPUT);
}
 
/** Fonction loop() */
void loop() {
  
  /* 1. Lance une mesure de distance en envoyant une impulsion HIGH de 10Âµs sur la broche TRIGGER */
  digitalWrite(TRIGGER_PIN, HIGH);
  delayMicroseconds(10);
  digitalWrite(TRIGGER_PIN, LOW);
  
  /* 2. Mesure le temps entre l'envoi de l'impulsion ultrasonique et son Ã©cho (si il existe) */
  long measure = pulseIn(ECHO_PIN, HIGH, MEASURE_TIMEOUT);
   
  /* 3. Calcul la distance Ã  partir du temps mesurÃ© */
  float distance_mm = measure / 2.0 * SOUND_SPEED;
   
  /* Affiche les rÃ©sultats en mm, cm et m */
  Serial.print(F("Distance: "));
  Serial.print(distance_mm);
  Serial.print(F("mm ("));
  Serial.print(distance_mm / 10.0, 2);
  Serial.print(F("cm, "));
  Serial.print(distance_mm / 1000.0, 2);
  Serial.println(F("m)"));
   
  /* DÃ©lai d'attente pour Ã©viter d'afficher trop de rÃ©sultats Ã  la seconde */
  }