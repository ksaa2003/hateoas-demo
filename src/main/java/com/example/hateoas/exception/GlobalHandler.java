package com.example.hateoas.exception;

import com.example.hateoas.controller.Home;
import com.example.hateoas.model.ExceptionMessage;
import com.example.hateoas.model.Relationships;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@ControllerAdvice
public class GlobalHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(FrozenException.class)
    public ResponseEntity<ExceptionMessage> handleFrozenException(final FrozenException ex) {
        var exMessage = new ExceptionMessage(ex.getMessage());
        exMessage.add(linkTo(methodOn(Home.class).getHome()).withRel(Relationships.HOME));
        exMessage.add(linkTo(methodOn(Home.class).getAccountById(ex.getId())).withRel(Relationships.SELF));
        return ResponseEntity.badRequest().body(exMessage);
    }
}
