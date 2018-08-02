package ygor.identifier

class EissnIdentifier extends AbstractIdentifier{

    static constraints = {
        // TODO: check EISSN format
    }

    EissnIdentifier(String identifier){
        this.identifier = identifier
    }
}
