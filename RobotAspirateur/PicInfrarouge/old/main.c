#define _XTAL_FREQ 8000000

// PIC10F222 Configuration Bit Settings

// 'C' source line config statements

// CONFIG
#pragma config IOSCFS = 8MHZ    // Internal Oscillator Frequency Select bit (8 MHz)
#pragma config MCPU = OFF       // Master Clear Pull-up Enable bit (Pull-up disabled)
#pragma config WDTE = OFF        // Watchdog Timer Enable bit (WDT enabled)
#pragma config CP = OFF         // Code protection bit (Code protection off)
#pragma config MCLRE = OFF       // GP3/MCLR Pin Function Select bit (GP3/MCLR pin function is MCLR)

// #pragma config statements should precede project file includes.
// Use project enums instead of #define for ON and OFF.

#include <xc.h>

int main()
{
  TRIS = 0x0E; //GPIO as Output PIN, GPIO 3, 2, 1 as Input
  
  
  
  GPIObits.GP0 = 0;
  char capteur1 = GPIObits.GP1;
  char capteur2 = GPIObits.GP2;
  char capteur3 = GPIObits.GP3;
  int count =0, i=0;
    
  while(1)
  {
    count =0;
    __delay_ms(200);
    capteur1 = GPIObits.GP1;
    capteur2 = GPIObits.GP2;
    capteur3 = GPIObits.GP3;
    
    
    //GPIObits.GP0 = GPIObits.GP3;
    
    if (capteur1 == 1)
        count ++;
    
    if (capteur2 == 1)
        count ++;
        
    if (capteur3 == 1)
        count ++;
    
    
    for (i=0; i < count; i++)
    {
        GPIObits.GP0 = 1;  // LED ON
        __delay_ms(100); // 1 Second Delay
        GPIObits.GP0 = 0;  // LED OFF
        __delay_ms(100); // 1 Second Delay
    }  
    
  }
  return 0;
}