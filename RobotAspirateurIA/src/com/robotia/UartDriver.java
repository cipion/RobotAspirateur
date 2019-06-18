package com.robotia;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class UartDriver {
    UART         host = null;
    InputStream  is   = null;
    OutputStream os   = null;

    public UartDriver() {
        try {
            host = (UART) PeripheralManager.open( "HOST", UART.class,
                    (String[]) null );
            is = host.getInputStream();
            os = host.getOutputStream();
            StringBuffer cmd = new StringBuffer();
            int c = 0;
            while ( true ) {
                os.write( '$' );
                os.write( ' ' ); // echo prompt
                while ( c != '\n' && c != '\003' ) { // echo input
                    c = is.read();
                    os.write( c );
                    cmd.append( c );
                }
                if ( c == '\003' ) { // CTL-C
                    break;
                }
                // process(cmd);
            }
        } catch ( IOException ioe ) {
            // Handle exception
        } catch ( PeripheralException pe ) {
            // Handle exception
        } finally {
            if ( is != null ) {
                try {
                    is.close();
                } catch ( IOException ex ) {
                }
            }
            if ( os != null ) {
                try {
                    os.close();
                } catch ( IOException ex ) {
                }
            }
            if ( host != null ) {
                try {
                    host.close();
                } catch ( IOException ex ) {
                }
            }
        }
    }
}
