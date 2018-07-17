package ygor.identifier

class EissnIdentifier extends AbstractIdentifier{

    static constraints = {
        // TODO: check EISSN format
    }

    EissnIdentifier(String id){
        this.id = id
    }
}
