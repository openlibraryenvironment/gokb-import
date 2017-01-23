package de.hbznrw.ygor.tools

import java.text.SimpleDateFormat
import java.util.Formatter.DateTime
import java.util.Calendar
import groovy.time.TimeCategory

class DateToolkit {

	static String getDateMinusOneMinute(String date) {
        
        try {
            def sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
            Date d  = sdf.parse(date)
            
            use(TimeCategory){
                d = d - 1.minute
                return sdf.format(d)
            }
        }
        catch (Exception e) {
            return date
        }
	}
}
