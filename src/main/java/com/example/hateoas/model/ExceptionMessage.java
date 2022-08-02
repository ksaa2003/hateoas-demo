package com.example.hateoas.model;

import org.springframework.hateoas.RepresentationModel;

public class ExceptionMessage extends RepresentationModel<ExceptionMessage> {
    private String message;

    public ExceptionMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
