package de.hbznrw.ygor.normalizers


import java.time.LocalDate

class DateNormalizer {

    static final String START_DATE = "StartDate"
    static final String END_DATE = "EndDate"

    static String normalizeDate(String str, String dateType){
        if (!str){
            return str
        }
        str = StringNormalizer.normalizeString(str)

        // Take only start part or end part of "01.01.2000-31.12.2000"
        if (str.contains("-")){
            def tmp = str.split("-")
            if (dateType.equals(START_DATE)){
                if (tmp.size() > 1){
                    str = tmp[0]
                }
            }
            else if (dateType.equals(END_DATE)){
                if (tmp.size() > 1){
                    str = tmp[1]
                }
            }
        }

        def strList = parseDate(str, dateType)

        if (4 == strList[0].size()){
            str = strList[0]

            if (strList[1]){
                def y = Integer.parseInt(strList[0])
                def m = Integer.parseInt(strList[1])
                if (m >= 1 && m <= 12){
                    LocalDate date = LocalDate.of(y, m, 1)
                    def mm = String.format('%02d', m)
                    def dd = String.format('%02d', date.lengthOfMonth())

                    if (dateType.equals(START_DATE)){
                        str += ("-" + mm + "-01 00:00:00.000")
                    }
                    else if (dateType.equals(END_DATE)){
                        str += ("-" + mm + "-" + dd + " 23:59:59.000")
                    }
                }
                else {
                    str = ''
                }
            }
            else {
                if (dateType.equals(START_DATE)){
                    str += "-01-01 00:00:00.000"
                }
                else if (dateType.equals(END_DATE)){
                    str += "-12-31 23:59:59.000"
                }
            }
        }
        else {
            str = ''
        }
        str
    }


    static List parseDate(String str, String dateType){
        if (!str){
            return ['', null]
        }
        str = CommonNormalizer.removeText(str).replaceAll(/\s+/,'').trim()

        // remove coverage volume
        def matches00 = (str =~ /^(\d)+[.](\d){2,4}/)
        if (matches00){
            str = str.split("[.]")[1]
        }

        // remove brackets and more
        if (dateType.equals(START_DATE)){
            if (str.startsWith("[")){
                str = str.replaceFirst("\\[", '')
            }
            if (str.endsWith("-")){
                str = str.take(str.length() - 1)
            }
        }
        else if (dateType.equals(END_DATE)){
            if (str.endsWith("]")){
                str = str.take(str.length() - 1)
            }
            if (str.startsWith("-")){
                str = str.replaceFirst("-", '')
            }
        }

        // 2001-2002        -> [2001-2002, 1, null, null, -, 2, null, null]
        // 2001/2002        -> [2001/2002, 1, null, null, /, 2, null, null]
        // 2015,8-2016      -> [2015,8/2016, 5, ,8, 8, -, 6, null, null]
        // 2015,8/2016      -> [2015,8/2016, 5, ,8, 8, /, 6, null, null]
        // 2013-2015,3      -> [2013-2015,3, 3, null, null, -, 5, ,3, 3]
        // 2010/2018,3      -> [2010-2018,3, 0, null, null, /, 8, ,3, 3]
        // 1999,5-2005,11   -> [1999,5-2005,11, 9, ,5, 5, -, 5, ,11, 1]
        // 2001,4/2002,5    -> [2001,4/2002,5, 1, ,4, 4, /, 2, ,5, 5]

        def matches001 = (str =~ /^(\d){4}(,(\d)+)?(\/|-)(\d){4}(,(\d)+)?$/)
        if (matches001){
            def tmp1 = str.split(matches001[0][4])

            if (dateType.equals(START_DATE)){
                if (tmp1[0].contains(",")){
                    def tmp2 = tmp1[0].split(",")
                    return [tmp2[0], tmp2[1]]
                }
                return [tmp1[0], null]
            }
            else if (dateType.equals(END_DATE)){
                if (tmp1[1].contains(",")){
                    def tmp2 = tmp1[1].split(",")
                    return [tmp2[0], tmp2[1]]
                }
                return [tmp1[1], null]
            }
            return [null, null]
        }

        // 2022/23          -> [2022/23, 2, null, null, /, 3, null, null]
        // 2022,5-23        -> [2022,5/23, 2, ,5, 5, -, 3, null, null]
        // 2022/23,11       -> [2022/23,11, 2, null, null, /, 3, ,11, 1]
        // 2022,5/23,11     -> [2022,5/23,11, 2, ,5, 5, /, 3, ,11, 1]

        def matches002 = (str =~ /^(\d){4}(,(\d)+)?(\/|-)(\d){2}(,(\d)+)?$/)
        if (matches002){
            def tmp1 = str.split(matches002[0][4])

            if (dateType.equals(START_DATE)){
                if (tmp1[0].contains(",")){
                    def tmp2 = tmp1[0].split(",")
                    return [tmp2[0], tmp2[1]]
                }
                return [tmp1[0], null]
            }
            else if (dateType.equals(END_DATE)){
                if (tmp1[1].contains(",")){
                    def tmp2 = tmp1[1].split(",")
                    return [tmp1[0].take(2) + tmp2[0], tmp2[1]]
                }
                return [tmp1[0].take(2) + tmp1[1], null]
            }
            return [null, null]
        }

        // 22/23          -> [22/23, 2, null, null, /, 3, null, null]
        // 22,5-23        -> [22,5/23, 2, ,5, 5, -, 3, null, null]
        // 22/23,11       -> [22/23,11, 2, null, null, /, 3, ,11, 1]
        // 22,5/23,11     -> [22,5/23,11, 2, ,5, 5, /, 3, ,11, 1]

        def matches003 = (str =~ /^(\d){2}(,(\d)+)?(\/|-)(\d){2}(,(\d)+)?$/)
        if (matches003){
            def tmp1 = str.split(matches003[0][4])

            if (dateType.equals(START_DATE)){
                if (tmp1[0].contains(",")){
                    def tmp2 = tmp1[0].split(",")
                    return ['20' + tmp2[0], tmp2[1]]
                }
                return ['20' + tmp1[0], null]
            }
            else if (dateType.equals(END_DATE)){
                if (tmp1[1].contains(",")){
                    def tmp2 = tmp1[1].split(",")
                    return ['20' + tmp2[0], tmp2[1]]
                }
                return ['20' + tmp1[1], null]
            }
            return [null, null]
        }


        // 2010,2
        def matches02 = (str =~ /^(\d){4},(\d)+$/)
        if (matches02){
            def tmp = str.split(',')
            return [tmp[0], tmp[1]]
        }

        // 2010
        def matches03 = (str =~ /^(\d){4}$/)
        if (matches03){
            return [str, null]
        }

        // 05
        def matches04 = (str =~ /^(\d){2}$/)
        if (matches04){
            return ['20' + str, null]
        }

        return ['', null]
    }
}
