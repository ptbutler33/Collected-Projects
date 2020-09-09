package edu.gatech.cse6242;

import java.io.IOException;
import java.util.StringTokenizer;
import java.lang.Object;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.IOException;

public class Q4 {



//first mapper function
    public static class EdgeMapper
           extends Mapper<Object, Text, IntWritable, IntWritable>{

        private IntWritable source = new IntWritable();
        private IntWritable target = new IntWritable();

        private final static IntWritable one = new IntWritable(1);
        private final static IntWritable neg_one = new IntWritable(-1);


        public void map(Object key, Text value, Context context
                         ) throws IOException, InterruptedException {
          StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
          while (itr.hasMoreTokens()) {
            String[] edge = itr.nextToken().split("\t");
            source.set(Integer.parseInt(edge[0]));
            target.set(Integer.parseInt(edge[1]));

            context.write(source, one);
            context.write(target, neg_one);
          }
        }
      }

      public static class DifferenceReducer
           extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(IntWritable key, Iterable<IntWritable> values,
                           Context context
                           ) throws IOException, InterruptedException {
          int diff = 0;
          for (IntWritable val : values) {
            diff += val.get();
          }

          result.set(diff);
          context.write(key, result);

        }
      }

      public static class DiffMapper
             extends Mapper<Object, Text, IntWritable, IntWritable>{

          //private IntWritable node = new IntWritable();
          private IntWritable diff = new IntWritable();

          private final static IntWritable one = new IntWritable(1);


          public void map(Object key, Text value, Context context
                           ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
            while (itr.hasMoreTokens()) {
              String[] node_diff = itr.nextToken().split("\t");
              diff.set(Integer.parseInt(node_diff[1]));

              context.write(diff, one);
            }
          }
        }

        public static class NodeReducer
             extends Reducer<IntWritable,IntWritable,IntWritable,IntWritable> {
          private IntWritable result = new IntWritable();

          public void reduce(IntWritable key, Iterable<IntWritable> values,
                             Context context
                             ) throws IOException, InterruptedException {
            int summ = 0;
            for (IntWritable val : values) {
              summ += val.get();
            }

            result.set(summ);
            context.write(key, result);

          }
        }

    public static void main(String[] args) throws Exception {
      Configuration conf = new Configuration();

    Job job = Job.getInstance(conf, "Q4");
    job.setJarByClass(Q4.class);
    job.setJobName("Node Degree");

    job.setMapperClass(EdgeMapper.class);
    job.setCombinerClass(DifferenceReducer.class);
    job.setReducerClass(DifferenceReducer.class);
    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[0] + "temp"));


    job.waitForCompletion(true);

    Configuration conf2 = new Configuration();
    Job job2  = Job.getInstance(conf2, "Q4");
    job2.setJarByClass(Q4.class);

    job2.setMapperClass(DiffMapper.class);
    job2.setCombinerClass(NodeReducer.class);
    job2.setReducerClass(NodeReducer.class);
    job2.setOutputKeyClass(IntWritable.class);
    job2.setOutputValueClass(IntWritable.class);

    FileInputFormat.addInputPath(job2, new Path(args[0] + "temp"));
    FileOutputFormat.setOutputPath(job2, new Path(args[1]));
    System.exit(job2.waitForCompletion(true) ? 0 : 1);
  }
}
