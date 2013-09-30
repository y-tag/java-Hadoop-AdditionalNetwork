package myorg.examples.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;


public class CommunicationRunner {
    
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        GenericOptionsParser parser = new GenericOptionsParser(conf, args);
        args = parser.getRemainingArgs();

        if (args.length < 4) {
            System.err.println("Usage: input output server_name server_port");
            return;
        }
        String input = args[0];
        String output = args[1];
        String serverName = args[2];
        int serverPort = Integer.parseInt(args[3]);

        conf.set(CommunicationMapper.SERVER_NAME_CONFNAME, serverName);
        conf.setInt(CommunicationMapper.SERVER_PORT_CONFNAME, serverPort);

        Job job = new Job(conf, "communication sample");
        job.setJarByClass(CommunicationRunner.class);
        job.setMapperClass(CommunicationMapper.class);
        job.setReducerClass(CommunicationReducer.class);

        job.setNumReduceTasks(1);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(input));

        FileOutputFormat.setOutputPath(job, new Path(output));
        FileOutputFormat.setCompressOutput(job, true);
        FileOutputFormat.setOutputCompressorClass(job, org.apache.hadoop.io.compress.GzipCodec.class);

        job.setInputFormatClass(TextInputFormat.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        job.waitForCompletion(true);
    }
}

