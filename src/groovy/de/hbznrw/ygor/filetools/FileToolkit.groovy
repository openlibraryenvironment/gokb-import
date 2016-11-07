package de.hbznrw.ygor.filetools

import java.nio.file.Paths
import org.codehaus.groovy.runtime.DateGroovyMethods;

class FileToolkit {

	static String getDateTimePrefixedFileName(filename) {
		def p1 = Paths.get(filename).getParent()
		def p3 = Paths.get(filename).getFileName()
		def p2 = DateGroovyMethods.format(new Date(), 'yyyy-dd-MM-HHmm-')

		return p1.toString() + File.separator + p2 + p3.toString()
	}
}
