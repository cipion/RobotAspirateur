#ifndef CONFIG_H
#define CONFIG_H

#include <iostream>
#include <map>
#include <string>
#include <fstream>
#include "log.h"

/**********************
Objet permettant de recuperer les properties
les properties sont sous la forme : nomproperties=valeur

L'objet permet de recuperer un objet map contenant la liste des properties

TODO : en faire un singleton = une instance

**********************/

class Config
{
	public:
		Config();
		std::map<const std::string,std::string> getConfig();
	
	private:
		typedef std::map<const std::string,std::string> CfgMap;
		CfgMap config;
		std::string s;
		std::string file="../config/DriverCapteur.properties";
		std::ifstream monflux;
		Log log;

};



#endif // CONFIG_H