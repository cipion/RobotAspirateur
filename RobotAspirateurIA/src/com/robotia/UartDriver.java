package com.robotia;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pi4j.io.gpio.exception.UnsupportedBoardType;
import com.pi4j.io.serial.Baud;
import com.pi4j.io.serial.DataBits;
import com.pi4j.io.serial.FlowControl;
import com.pi4j.io.serial.Parity;
import com.pi4j.io.serial.Serial;
import com.pi4j.io.serial.SerialConfig;
import com.pi4j.io.serial.SerialDataEvent;
import com.pi4j.io.serial.SerialDataEventListener;
import com.pi4j.io.serial.SerialFactory;
import com.pi4j.io.serial.SerialPort;
import com.pi4j.io.serial.StopBits;

/**
 * This example code demonstrates how to perform serial communications using the
 * Raspberry Pi.
 * 
 * @author Robert Savage
 */
public class UartDriver {

    // create serial config object
    private SerialConfig        config = new SerialConfig();

    private String              retour;

    private static final Logger logger = LoggerFactory.getLogger( UartDriver.class );

    // create an instance of the serial communications class
    final Serial                serial = SerialFactory.createInstance();

    final static Boolean        isInit = false;

    /**
     * This example program supports the following optional command
     * arguments/options: "--device (device-path)" [DEFAULT: /dev/ttyAMA0]
     * "--baud (baud-rate)" [DEFAULT: 38400] "--data-bits (5|6|7|8)" [DEFAULT:
     * 8] "--parity (none|odd|even)" [DEFAULT: none] "--stop-bits (1|2)"
     * [DEFAULT: 1] "--flow-control (none|hardware|software)" [DEFAULT: none]
     * 
     * @param args
     * @throws InterruptedException
     * @throws IOException
     */
    public Boolean initUART()
    {
        // !! ATTENTION !!
        // By default, the serial port is configured as a console port
        // for interacting with the Linux OS shell. If you want to use
        // the serial port in a software program, you must disable the
        // OS from using this port.
        //
        // Please see this blog article for instructions on how to disable
        // the OS console for this port:
        // https://www.cube-controls.com/2015/11/02/disable-serial-port-terminal-output-on-raspbian/

        try {

            // set default serial settings (device, baud rate, flow control,
            // etc)
            //
            // by default, use the DEFAULT com port on the Raspberry Pi (exposed
            // on GPIO header)
            // NOTE: this utility method will determine the default serial port
            // for the
            // detected platform and board/model. For all Raspberry Pi models
            // except the 3B, it will return "/dev/ttyAMA0". For Raspberry Pi
            // model 3B may return "/dev/ttyS0" or "/dev/ttyAMA0" depending on
            // environment configuration.
            config.device( SerialPort.getDefaultPort() )
                    .baud( Baud._38400 )
                    .dataBits( DataBits._8 )
                    .parity( Parity.NONE )
                    .stopBits( StopBits._1 )
                    .flowControl( FlowControl.NONE );

            // parse optional command argument options to override the default
            // serial settings.
            // config = CommandArgumentParser.getSerialConfig( config, args );

            return true;

        } catch ( IOException ex ) {

            return false;
        } catch ( UnsupportedBoardType e ) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return false;
        } catch ( InterruptedException e ) {
            // TODO Auto-generated catch block

            e.printStackTrace();
            return false;
        }

    }

    public String getDetectionObstacle() {

        retour = "null";

        if ( !isInit )
        {
            return "Error, UART not init";
        }

        // create and register the serial data listener
        serial.addListener( new SerialDataEventListener() {

            @Override
            public void dataReceived( SerialDataEvent event ) {

                // NOTE! - It is extremely important to read the data received
                // from the
                // serial port. If it does not get read from the receive buffer,
                // the
                // buffer will continue to grow and consume memory.
                byte[] arrayByte = new byte[4];
                Arrays.fill( arrayByte, (byte) 0 );

                try {
                    logger.info( "UartDriver : nombre de bit recu = " + event.length() + " (" + event.toString() + ")" );

                    arrayByte = event.getBytes();

                } catch ( IOException e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                int i = 1;
                for ( byte bit : arrayByte )
                {
                    logger.info( "UartDriver : Capteur" + i + " = " + bit );
                    i++;
                }

                retour = event.toString();

            }
        } );

        return retour;
    }

    public String getDetectionObstacle( int timeout ) {

        if ( !isInit )
        {
            return "Error, UART not init";
        }

        // create and register the serial data listener
        serial.addListener( new SerialDataEventListener() {
            @Override
            public void dataReceived( SerialDataEvent event ) {

                // NOTE! - It is extremely important to read the data received
                // from the
                // serial port. If it does not get read from the receive buffer,
                // the
                // buffer will continue to grow and consume memory.

            }
        } );

        return "null";
    }

    public Boolean setDetectionObstacle() {

        if ( !isInit )
        {
            return false;
        }

        // open the default serial device/port with the configuration
        // settings
        try {
            this.serial.open( config );
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // continuous loop to keep the program running until the user
        // terminates the program

        try {
            // write a formatted string to the serial transmit buffer
            this.serial.write( "CURRENT TIME: " + new Date().toString() );

            // write a individual bytes to the serial transmit buffer
            this.serial.write( (byte) 13 );
            this.serial.write( (byte) 10 );

            // write a simple string to the serial transmit buffer
            this.serial.write( "Second Line" );

            // write a individual characters to the serial transmit
            // buffer
            serial.write( '\r' );
            serial.write( '\n' );

            // write a string terminating with CR+LF to the serial
            // transmit buffer
            serial.writeln( "Third Line" );

            return true;
        } catch ( IllegalStateException ex ) {
            ex.printStackTrace();
            return false;
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
}
