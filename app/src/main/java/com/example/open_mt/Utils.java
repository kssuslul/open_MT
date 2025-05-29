package com.example.open_mt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Utils {

    public static long parseDateTimeToMillis(String dateTime) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        try {

            return sdf.parse(dateTime).getTime();
        }
        catch (ParseException e) {

            e.printStackTrace();
            return 0;
        }
    }
}
