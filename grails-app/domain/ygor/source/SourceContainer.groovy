package ygor.source

class SourceContainer {

    static KbartSource kbartSource
    static ZdbSource   zdbSource
    static EzbSource   ezbSource

    static constraints = {
        kbartSource nullable : false
        zdbSource   nullable : false
        ezbSource   nullable : false
    }

}
