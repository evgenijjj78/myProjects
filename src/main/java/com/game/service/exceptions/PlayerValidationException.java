package com.game.service.exceptions;

public class PlayerValidationException extends Exception {
	public PlayerValidationException() {}
	public PlayerValidationException(String message) {
		super(message);
	}
}
