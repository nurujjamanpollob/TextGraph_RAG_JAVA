package com.nurujjamanpollob.textenginejava.rag.exception;

public class RagException extends Exception {
    public RagException(String message) {
        super(message);
    }

    public RagException(String message, Throwable cause) {
        super(message, cause);
    }
}