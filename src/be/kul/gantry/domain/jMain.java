package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class jMain {

    public static void main(String[] args){
        try {
            Problem problem = Problem.fromJson(new File("1_10_100_4_FALSE_65_50_50.json"));
            problem.solve();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
