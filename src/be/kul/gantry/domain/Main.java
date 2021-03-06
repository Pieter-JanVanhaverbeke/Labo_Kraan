package be.kul.gantry.domain;

import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args){

        String input, output;
        if(args.length > 0) input = args[0];
        else input = "2_10_100_4_TRUE_65_50_50.json";
        if(args.length > 1) output = args[1];
        else output = "output_2_TRUE.csv";

        try {
            Problem problem = Problem.fromJson(new File(input));
            problem.setOutputWriter(new PrintWriter(new File(output)));
            problem.solve();
        } catch (IOException |
                ParseException |
                NoSlotAvailableException |
                SlotAlreadyHasItemException |
                SlotUnreachableException e) {
            e.printStackTrace();
        }
    }
}
