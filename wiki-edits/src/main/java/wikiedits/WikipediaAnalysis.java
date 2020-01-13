package wikiedits;

import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditEvent;
import org.apache.flink.streaming.connectors.wikiedits.WikipediaEditsSource;

import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer011;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.common.functions.MapFunction;

public class WikipediaAnalysis {

    public static void main(String[] args) throws Exception {
        //purpose of this tutorial is watch the number of added or removed by bytes that user causes in a certain time window (every 5 seconds.)
        //how to run:
        // open cmd in folder that contain the pom.xml and run these command line.
        //   mvn clean package
        //   mvn exec:java -Dexec.mainClass=wikiedits.WikipediaAnalysis

        //create a StreamExecutionEnvironment
        StreamExecutionEnvironment see = StreamExecutionEnvironment.getExecutionEnvironment();

        //create sources for reading from external systems
        // WikipediaEditsSource's constructor is connector to wikipedia
        DataStream<WikipediaEditEvent> edits = see.addSource(new WikipediaEditsSource());

        //Specify the streaming data by Username! อารมณ์เหมือนว่า มีดาต้ามาก้อนนี้เราจะเอา อะไรเป็ย primary key
        KeyedStream<WikipediaEditEvent, String> keyedEdits = edits
                .keyBy(new KeySelector<WikipediaEditEvent, String>() {
                    @Override
                    public String getKey(WikipediaEditEvent event) {
                        return event.getUser();
                    }
                });


        DataStream<Tuple2<String, Long>> result = keyedEdits
                //timeWindow use to set the range of data in the streaming data to compute
                .timeWindow(Time.seconds(2))
                //specifies a Aggregate transformation on each window slice for each unique key
                //this case start from an initial value of ("", 0L) ---> create Accumulator()
                //next, add to above Tuple
                //The resulting stream now contains a Tuple2<String, Long> for every user which gets emitted every 2 seconds.
                .aggregate(new AggregateFunction<WikipediaEditEvent, Tuple2<String, Long>, Tuple2<String, Long>>() {
                    @Override
                    public Tuple2<String, Long> createAccumulator() {
                        System.out.println("createAccumulator");
                        return new Tuple2<>("", 0L);
                    }

                    @Override
                    public Tuple2<String, Long> add(WikipediaEditEvent value, Tuple2<String, Long> accumulator) {
                        accumulator.f0 = value.getUser();
                        accumulator.f1 += value.getByteDiff();
                        System.out.println("add");
                        return accumulator;
                    }

                    @Override
                    public Tuple2<String, Long> getResult(Tuple2<String, Long> accumulator) {
                        System.out.println("getResult");
                        return accumulator;
                    }

                    @Override
                    public Tuple2<String, Long> merge(Tuple2<String, Long> a, Tuple2<String, Long> b) {
                        System.out.println("merge");
                        return new Tuple2<>(a.f0, a.f1 + b.f1);
                    }
                });
        // print the stream result
//        result.print();
            result
                .map(new MapFunction<Tuple2<String,Long>, String>() {
                    @Override
                    public String map(Tuple2<String, Long> tuple) {
                        return tuple.toString();
                    }
                })
                .addSink(new FlinkKafkaProducer011<>("localhost:9092", "wiki-result", new SimpleStringSchema()));
        // start execute!!!
        see.execute();
    }
}