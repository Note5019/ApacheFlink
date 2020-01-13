package WordCount;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

import java.io.File;

public class WordCountNoDescription {

    public static void main(String[] args) throws Exception {
        File myFile = new File("src/main/java/WordCount/words.txt");
        File myOutFile = new File("src/main/java/WordCount/words2");
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<String> text = env.readTextFile(myFile.getAbsolutePath());
        String outputPath = myOutFile.getAbsolutePath();
        DataSet<Tuple2<String, Integer>> counts =
                text.flatMap(new Tokenizer())
                        .groupBy(0)
                        .sum(1);
        counts.print();
    }

    public static class Tokenizer implements FlatMapFunction<String, Tuple2<String, Integer>> {
        @Override
        public void flatMap(String value, Collector<Tuple2<String, Integer>> collector) throws Exception {
            String[] tokens = value.toLowerCase().split("\\W+");
            for(String token: tokens){
                if(token.length()>0){
                    collector.collect(new Tuple2<String, Integer>(token, 1));
                }
            }
        }
    }
}
