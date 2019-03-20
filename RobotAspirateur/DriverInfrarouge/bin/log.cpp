#include "log.h"

using namespace std;

Log::Log()
{
    fluxLog.open(fichierLog.c_str());
	
}



void Log::info (string text)
{
    //Décommenter pour avoir les logs
	time(&temps_act);
    cout << ctime(&temps_act) << " | INFO | " << text << endl;
	
	if (fluxLog)
	{
		fluxLog << ctime(&temps_act) << " | INFO | " << text << endl;
	}

}

void Log::erreur (string text)
{
    //Décommenter pour avoir les logs
	time(&temps_act);
    cout << ctime(&temps_act) << " | ERREUR | " << text << endl;
	
	if (fluxLog)
	{
		fluxLog << ctime(&temps_act) << " | ERREUR | " << text << endl;
	}
}


void Log::space ()
{
    //Décommenter pour avoir les logs
    cout << " " << endl;

    if (fluxLog)
    {
        fluxLog << " " << endl;
    }
}
