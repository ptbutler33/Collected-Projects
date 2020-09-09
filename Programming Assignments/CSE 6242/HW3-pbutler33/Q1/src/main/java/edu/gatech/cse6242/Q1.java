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

public class Q1 {

  public static class TokenizerMapper
         extends Mapper<Object, Text, IntWritable, Text>{

      private IntWritable email = new IntWritable();
      private Text weight = new Text();

      public void map(Object key, Text value, Context context
                      ) throws IOException, InterruptedException {
        StringTokenizer itr = new StringTokenizer(value.toString(), "\n");
        while (itr.hasMoreTokens()) {
          String line = itr.nextToken();
          String tokens[] = line.split("\t");

          if (tokens.length == 3) {

          email.set(Integer.parseInt(tokens[0]));
          weight.set(tokens[1] + "," + tokens[2]);
          context.write(email, weight);
        }
        }
      }
    }

    public static class IntSumReducer
         extends Reducer<IntWritable,Text,IntWritable,Text> {
      private Text result = new Text();

      public void reduce(IntWritable key, Iterable<Text> values,
                         Context context
                         ) throws IOException, InterruptedException {
        int max = -1;
        int t_min = -1;
        //Text targets = new Text();
        for (Text val : values) {

          String target_string = val.toString();
          String[] target_weight = target_string.split(",");
          if(Integer.parseInt(target_weight[1]) > max) {
            max = Integer.parseInt(target_weight[1]);
            t_min = Integer.parseInt(target_weight[0]);
            //targets.set(target_weight[0]);
            result.set(t_min + "," + max);
          }
          else {
            if(Integer.parseInt(target_weight[1]) == max) {


            if(Integer.parseInt(target_weight[0]) < t_min || t_min == -1) {
              t_min = Integer.parseInt(target_weight[0]);
              result.set(t_min + "," + max);
            }
          }
        }


        }
          context.write(key, result);
      }
    }


  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Q1");

    job.setJarByClass(Q1.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(IntWritable.class);
    job.setOutputValueClass(Text.class);

    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
