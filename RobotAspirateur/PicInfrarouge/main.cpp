#define _XTAL_FREQ 8000000
#include <xc.h>
#include <pic16f877a.h>
#include "uart.h"

// BEGIN CONFIG
#pragma config FOSC = HS 
#pragma config WDTE = OFF 
#pragma config PWRTE = OFF
#pragma config BOREN = ON 
#pragma config LVP = OFF 
#pragma config CPD = OFF 
#pragma config WRT = OFF 
#pragma config CP = OFF 
//END CONFIG



void main()
{
  TRISB = 0xFF; //PORTB as Input
  nRBPU = 0;    //Enables PORTB Internal Pull Up Resistors
  UART_Init(300);

  do
  {
    UART_Write(PORTB);
    __delay_ms(100);
  }while(1);
}