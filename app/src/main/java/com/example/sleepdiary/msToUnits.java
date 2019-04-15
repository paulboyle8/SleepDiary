package com.example.sleepdiary;

public class msToUnits { //Class for converting times to milliseconds or hours and minutes
    public static String get(long ms){ //If converting from milliseconds to hours/minutes
        String result = ""; //Initialise string for result
        ms /= 1000; //Divide milliseconds by 1000 to get seconds
        long mins = ms/60; //Divide seconds by 60 to get minutes
        if (mins > 60) { //If the number of minutes are more than 60
            long hours = mins / 60; //Hours equals amount of times 60 goes into minutes
            mins %= 60; //Modulus 60 to get remaining minutes
            result = Long.toString(hours) + " hours "; //Add hours to result
        }
        if (mins > 0){
            result += Long.toString(mins) + " mins"; //Add minutes to result
        }
        return result; //Return result
    }

    public static long getMSfromUnits(int hours, int mins){ //If converting from hours/minutes to milliseconds
        long result = 0; //Initialise result
        if (hours > 0){
            result += hours*3600*1000; //Hours*60(minutes)*60(seconds)*1000(milliseconds) equals time in milliseconds
        }
        if (mins > 0){
            result += mins *60*1000; //Minutes*60(seconds)*1000(milliseconds) equals time in milliseconds
        }
        return result; //Return result
    }
}
