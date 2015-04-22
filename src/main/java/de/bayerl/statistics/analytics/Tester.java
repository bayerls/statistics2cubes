package de.bayerl.statistics.analytics;

import java.io.UnsupportedEncodingException;

/**
 * Created by sebastianbayerl on 15/04/15.
 */
public class Tester {


    public static void main(String[] args) {

        String stringPacketData="1@3@5 ";

        String[] splitString=stringPacketData.split("@");


        System.out.println(Integer.parseInt(splitString[2]));
    }

}
