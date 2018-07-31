package ygor.identifier

class PissnIdentifier extends AbstractIdentifier{

    static constraints = {
        // TODO: check PISSN format
    }

    PissnIdentifier(String id){
        this.identifier = id
    }
}
