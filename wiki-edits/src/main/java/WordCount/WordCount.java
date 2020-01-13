package WordCount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.io.File;
import java.util.Arrays;

public class WordCount {
    public static void main(String[] args) throws Exception {
        File myFile = new File("src/main/java/WordCount/words.txt");
        File myOutFile = new File("src/main/java/WordCount/words2");
//        System.out.println(myFile.getAbsolutePath());
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<String> text = env.readTextFile(myFile.getAbsolutePath());
//        System.out.println("aaaaaaaaaaaaa");
//        text.print();
//        System.out.println("bbbbbbbbbb");
        String outputPath = myOutFile.getAbsolutePath();
        DataSet<Tuple2<String, Integer>> counts =
            text.flatMap(new Tokenizer())
            .groupBy(0)
            .sum(1);
//        counts.writeAsCsv(outputPath,"\n"," --> ");
//        System.out.println("--------------------------------------------");
        System.out.println(counts.count());
//        System.out.println("--------------------------------------------");
        counts.print();
//        env.execute();
//        System.out.println("--------------------------------------------");

//        DataSet<Tuple2<String, Integer>> counts2 =
//                text.flatMap(new Tokenizer());
//        counts2.print();
    }

    public static class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> collector) throws Exception {
//            System.out.println("before split string !!!!!");
//            System.out.println(value);
//            System.out.println("-----------------------------------");
            String[] tokens = value.toLowerCase().split("\\W+");
//            System.out.println("after split string !!!!!");
//            System.out.println(Arrays.toString(tokens));
//            System.out.println("start for loop");
            for(String token: tokens){
//                System.out.println(token);
                if(token.length()>0){
                    collector.collect(new Tuple2<String, Integer>(token, 1));
                }
            }
        }
    }
}
