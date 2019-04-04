package com.example.sleepdiary;

public class msToUnits {
    public static String get(long ms){
        String result = "";
        ms /= 1000;
        long mins = ms/60;
        if (mins > 60) {
            long hours = mins / 60;
            mins %= 60;
            result = Long.toString(hours) + " hours ";
        }
        if (mins > 0){
            result += Long.toString(mins) + " mins";
        }
        return result;
    }

    public static long getMSfromUnits(int hours, int mins){
        long result = 0;
        if (hours > 0){
            result += hours*3600*1000;
        }
        if (mins > 0){
            result += mins *60*1000;
        }
        return result;
    }
}
