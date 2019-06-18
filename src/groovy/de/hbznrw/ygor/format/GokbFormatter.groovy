package de.hbznrw.ygor.format

import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.normalizers.NumberNormalizer
import de.hbznrw.ygor.normalizers.StringNormalizer

class GokbFormatter implements YgorFormatter{

    @Override
    String formatDate(String date) {
        return date
    }

    @Override
    String formatStartDate(String date) {
        return DateNormalizer.normalizeDate(date, DateNormalizer.START_DATE)
    }

    @Override
    String formatEndDate(String date) {
        return DateNormalizer.normalizeDate(date, DateNormalizer.END_DATE)
    }

    @Override
    String formatId(String id) {
        return StringNormalizer.normalizeString(id)
    }

    @Override
    String formatNumber(String number) {
        return NumberNormalizer.normalizeInteger(number)
    }

    @Override
    String formatString(String string) {
        return StringNormalizer.normalizeString(string)
    }

    @Override
    String formatUrl(String url) {
        return url
    }
}
