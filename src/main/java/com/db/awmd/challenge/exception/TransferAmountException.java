package com.db.awmd.challenge.exception;

public class TransferAmountException extends RuntimeException {
	
	private static final long serialVersionUID = -1181272729230441564L;
	private String message;
	private Throwable exception;
	
	public TransferAmountException(String message, Throwable exception) {
		super(message,exception);
	}
	
	public TransferAmountException(String message) {
		super(message);
	}

	public String getMessage() {
		return message;
	}

	public Throwable getException() {
		return exception;
	}
	
}
