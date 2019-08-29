package de.hbznrw.ygor.normalizers

import de.hbznrw.ygor.tools.UrlToolkit

class UrlNormalizer {

    /**
     * Returns an url (or only the url authority) including protocol.
     * Adding http:// if none given
     *
     * @param str
     * @param onlyAuthority
     * @return
     */
    static String normURL(String str, boolean onlyAuthority) {
        if(!str)
            return str

        if(onlyAuthority){
            return UrlToolkit.getURLAuthorityWithProtocol(str)
        }
        else {
            return UrlToolkit.getURLWithProtocol(str)
        }
    }
}
