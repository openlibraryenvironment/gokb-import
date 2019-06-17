package de.hbznrw.ygor.normalizers

import de.hbznrw.ygor.bridges.EzbBridge
import de.hbznrw.ygor.bridges.ZdbBridge
import de.hbznrw.ygor.export.structure.TitleStruct
import groovy.util.logging.Log4j
import ygor.identifier.DoiIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier

import java.util.regex.Pattern

@Log4j
class IdentifierNormalizer {


    static String normIdentifier(String str, Class type, String namespace){
        if (!str){
            return str
        }
        str = StringNormalizer.normalizeString(str)

        if (!type.equals(DoiIdentifier.class) && !type.equals(EissnIdentifier.class) && !type.equals(PissnIdentifier.class)){
            str = str.replaceAll(/[\/-]+/, "")
        }
        if (type.equals(EissnIdentifier.class) || type.equals(PissnIdentifier.class)){
            if (str.length() == 8){
                str = new StringBuilder(str).insert(4, "-").toString()
            }
        }
        else if (type.equals(ZdbBridge.IDENTIFIER)){
            str = new StringBuilder(str).insert(Math.abs(str.length()-1).toInteger(), "-").toString();
        }
        else if (type.equals(EzbBridge.IDENTIFIER)){
            // TODO
        }
        else if (type.equals(TitleStruct.DOI)){
            str = Pattern.compile("^https?://(dx\\.)?doi.org/").matcher(str).replaceAll("")
        }
        else if (type.equals("inID_" + namespace)){
            str = namespace ? str : ''
        }
        str
    }


    static def fixIdentifier(String id, Class clazz){
        if (clazz.equals(EissnIdentifier.class)){
            return fixEISSN(id)
        }
        id
    }


    static def fixEISSN(String id){
        // set the last character to upper case if it is "x" and if the rest of the eissn is valid
        if (id.matches("[\\d]{4}-[\\d]{3}x")){
            id = id.replace("x", "X")
            log.info("Set \"x\" to upper case in " + id)
        }
        id
    }

}
