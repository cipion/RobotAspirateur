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
  TRISB = 0x00; //PORTB as Output
  UART_Init(9600);

  do
  {
    if(UART_Data_Ready())
       PORTB = UART_Read();
    __delay_ms(100);
  }while(1);
}