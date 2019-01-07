package be.kul.gantry.domain;

import be.kul.gantry.solution.Solution;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;

public class Main {

    public static void main(String[] args){

        String input, output;
        if(args.length > 0) input = args[0];
        else input = "2_10_100_4_TRUE_65_50_50.json";
        if(args.length > 1) output = args[1];
        else output = "output_2_TRUE.csv";

        String[] todo = {
                "1_10_100_4_FALSE_65_50_50.json",
                "1_10_100_4_TRUE_65_50_50.json",
                "1_10_100_5_FALSE_65_65_100.json",
                "1_10_100_5_TRUE_65_65_100.json",
                "2_10_100_4_FALSE_65_50_50.json",
                "2_10_100_4_TRUE_65_50_50.json"
        };
        String[] result = {
                "1_10_100_4_FALSE_65_50_50.csv",
                "1_10_100_4_TRUE_65_50_50.csv",
                "1_10_100_5_FALSE_65_65_100.csv",
                "1_10_100_5_TRUE_65_65_100.csv",
                "2_10_100_4_FALSE_65_50_50.csv",
                "2_10_100_4_TRUE_65_50_50.csv"
        };

        for (int i = 0; i < todo.length; i++) {
            try {
                Problem problem = Problem.fromJson(new File(todo[i]));
                problem.setOutputWriter(new PrintWriter(new File(result[i])));
                problem.solve();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < todo.length; i++) {
            try {
                Solution.main(new String[]{todo[i], result[i]});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
