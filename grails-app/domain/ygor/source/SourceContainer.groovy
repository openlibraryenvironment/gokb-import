package ygor.source

class SourceContainer {

    KbartSource kbartSource
    ZdbSource   zdbSource
    EzbSource   ezbSource

    static constraints = {
        kbartSource nullable : false
        zdbSource   nullable : false
        ezbSource   nullable : false
    }

}
