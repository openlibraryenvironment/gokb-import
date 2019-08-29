package de.hbznrw.ygor.readers

import org.apache.commons.csv.QuoteMode

class KbartReaderConfiguration{
    def delimiter
    def quote
    def quoteMode
    def recordSeparator
    static def resolver = [
            'comma'         : ',',
            'semicolon'     : ';',
            'tab'           : '\t',
            'doublequote'   : '"',
            'singlequote'   : "'",
            'nullquote'     : 'null',
            'all'           : QuoteMode.ALL,
            'nonnumeric'    : QuoteMode.NON_NUMERIC,
            'none'          : QuoteMode.NONE
    ]

    KbartReaderConfiguration(def delimiter, def quote, def quoteMode, def recordSeparator){
        this.delimiter       = resolver.get(delimiter)
        this.quote           = resolver.get(quote)
        this.quoteMode       = resolver.get(quoteMode)
        this.recordSeparator = recordSeparator
    }

    static String resolve(String unresolved){
        resolver.get(unresolved)
    }
}
