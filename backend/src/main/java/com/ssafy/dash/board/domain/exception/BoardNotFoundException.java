package com.ssafy.dash.board.domain.exception;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(Long id) {
        super("Board not found with id: " + id);
    }
    
}
