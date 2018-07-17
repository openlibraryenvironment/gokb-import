package ygor.identifier

class ZdbIdentifier extends AbstractIdentifier{

    static constraints = {
        // TODO: check ZdbID format
    }

    ZdbIdentifier(String id){
        this.id = id
    }
}
