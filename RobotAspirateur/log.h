#ifndef LOG_H
#define LOG_H

#include <time.h>
#include <iostream>
#include <stdio.h>
#include <string>
#include <fstream>


class Log
{

    public:
        Log();

        void info(std::string text);
        void erreur(std::string text);
        void space();

    protected:
    private:
        //date pour les log
        time_t temps_act;

		//nom du fichier de log
        std::string const fichierLog = "/logs/RobotAspirateur/log_app.log";
        std::ofstream fluxLog;
};

#endif // LOG_H
