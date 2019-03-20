#include "config.h"

using namespace std;


Config::Config()
{
  monflux.open(file.c_str());
  
  if (monflux == NULL)
  {
	log.erreur("Config : Erreur le fichier de configuration n'est pas ouvert");
	exit(1);
  }
  
  // recupere ligne pas ligne le nom de l'atribut et la valeur pour les stocker dans la map
  while(getline(monflux,s)) 
  {
	config[s.substr(0,s.find('='))]=s.substr(s.find('=')+1);
  }
  
  

  log.info("Config : Config du Driver moteur :");
  for(CfgMap::iterator i=config.begin();i!=config.end();i++) 
  {
    log.info("Config : Propriete \"" + i->first + "\" = \"" + i->second + '\"');
  }
}

map<const std::string,std::string> Config::getConfig()
{
	return config;
}