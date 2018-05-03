#ifndef CONFIG_H
#define CONFIG_H

#include <iostream>
#include <map>
#include <string>
#include <fstream>
#include "log.h"

class Config
{
	public:
		Config();
		std::map<const std::string,std::string> getConfig();
	
	private:
		typedef std::map<const std::string,std::string> CfgMap;
		CfgMap config;
		std::string s;
		std::string file="../config/DriverMoteur.properties";
		std::ifstream monflux;
		Log log;

};



#endif // CONFIG_H