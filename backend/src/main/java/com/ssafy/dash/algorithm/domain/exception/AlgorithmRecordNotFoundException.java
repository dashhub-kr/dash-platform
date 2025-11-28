package com.ssafy.dash.algorithm.domain.exception;

public class AlgorithmRecordNotFoundException extends RuntimeException {

    public AlgorithmRecordNotFoundException(Long id) {
        super("Algorithm record not found with id: " + id);
    }
    
}
