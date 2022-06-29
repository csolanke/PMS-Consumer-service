package com.cds.org.consumer;

public class BadUserCredentialsException extends RuntimeException{

    public BadUserCredentialsException(String msg){
        super(msg);
    }

}
