package com.example.hateoas.exception;

public class FrozenException extends RuntimeException {
    private int id;
    public FrozenException(final int id, final String message) {
        super(message);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public FrozenException(final int id, final String message, final Exception exception) {
        super(message, exception);
        this.id = id;
    }
}
