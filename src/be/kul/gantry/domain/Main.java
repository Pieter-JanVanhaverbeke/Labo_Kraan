package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args){

        String input, output;
        if(args.length > 0) input = args[0];
        else input = "1_10_100_4_FALSE_65_50_50.json";
        if(args.length > 1) output = args[1];
        else output = "output_1_FALSE.csv";

        try {
            Problem problem = Problem.fromJson(new File(input));
            problem.setOutputWriter(new PrintWriter(new File(output)));
            problem.solve();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
