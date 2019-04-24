package de.hbznrw.ygor.format

import de.hbznrw.ygor.export.Normalizer

class GokbFormatter implements YgorFormatter{

    @Override
    String formatDate(String date) {
        return date
    }

    @Override
    String formatStartDate(String date) {
        return Normalizer.normDate(date, Normalizer.IS_START_DATE)
    }

    @Override
    String formatEndDate(String date) {
        return Normalizer.normDate(date, Normalizer.IS_END_DATE)
    }

    @Override
    String formatId(String id) {
        return Normalizer.normString(id)
    }

    @Override
    String formatNumber(String number) {
        return Normalizer.normInteger(number)
    }

    @Override
    String formatString(String string) {
        return Normalizer.normString(string)
    }

    @Override
    String formatUrl(String url) {
        return url
    }
}
