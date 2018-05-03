package com.robotia;

public enum Commandes {
    START( "start" ),
    STOP( "stop" ),
    ROTATE( "rotation" ),
    GET( "GET" );

    private String name = "";

    Commandes( String name )
    {
        this.name = name;
    }

    public String toString()
    {
        return name;
    }
}
