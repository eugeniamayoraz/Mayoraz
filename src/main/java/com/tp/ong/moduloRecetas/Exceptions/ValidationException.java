package com.tp.ong.moduloRecetas.Exceptions;

public class ValidationException extends RuntimeException {
	
	private static final long serialVersionUID = 1L; 
    public ValidationException(String message) {
        super(message);
    }
}