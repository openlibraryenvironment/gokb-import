package de.hbznrw.ygor.processing

/**
 * Wrapper class for exceptions that shall be caught and not lead to the screen print of a stacktrace.
 */
class YgorProcessingException extends Exception{

    YgorProcessingException(){
        super()
    }

    YgorProcessingException(String message){
        super(message)
    }
}
