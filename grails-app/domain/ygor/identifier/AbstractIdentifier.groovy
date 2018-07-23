package ygor.identifier

class AbstractIdentifier {

    String id

    static constraints = {
        id nullable : false
    }

    String toString(){return id}
}
