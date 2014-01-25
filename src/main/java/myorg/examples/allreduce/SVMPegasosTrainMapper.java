package myorg.examples.allreduce;

import java.io.File;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import myorg.io.FeatureVector;
import myorg.io.WeightVector;
import myorg.io.WeightVectorWithCount;
import myorg.io.WritableCacheReader;
import myorg.io.WritableCacheWriter;
import myorg.classifier.SVMPegasosLearner;
import myorg.util.SVMLightFormatParser;
import myorg.allreduce.AllReducer;
import myorg.allreduce.AllReduceContext;

public class SVMPegasosTrainMapper extends Mapper<Object, Text, Text, Text> {
    public static String SERVER_NAME_CONFNAME = "myorg.examples.allreduce.SVMPegasosTrainMapper.serverName";
    public static String SERVER_PORT_CONFNAME = "myorg.examples.allreduce.SVMPegasosTrainMapper.serverPort";
    public static String NUM_EPOCHS_CONFNAME = "myorg.examples.allreduce.SVMPegasosTrainMapper.numEpochs";
    public static String LAMBDA_CONFNAME = "myorg.examples.allreduce.SVMPegasosTrainMapper.lambda";
    public static String DIMENSIONS_CONFNAME = "myorg.examples.allreduce.SVMPegasosTrainMapper.dimensions";

    private AllReduceContext allreduceContext;
    private AllReducer<WeightVectorWithCount> allreducer;
    private WeightVector weight;
    private int numEpochs;
    private float lambda;
    private int dim;
    private String cacheName;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        String serverName = context.getConfiguration().get(SERVER_NAME_CONFNAME, "");
        int serverPort = context.getConfiguration().getInt(SERVER_PORT_CONFNAME, -1);

        this.numEpochs = context.getConfiguration().getInt(NUM_EPOCHS_CONFNAME, 10);
        this.lambda = context.getConfiguration().getFloat(LAMBDA_CONFNAME, 1e-4f);
        this.dim = context.getConfiguration().getInt(DIMENSIONS_CONFNAME, 1 << 24);
        
        if (serverName.equals("") || serverPort == -1) {
            throw new RuntimeException("server name is not defined");
        } else if (serverPort == -1) {
            throw new RuntimeException("server port is not defined");
        } else if (serverPort < 1024 || 65535 < serverPort) {
            throw new RuntimeException("server port is not valid: " + Integer.toString(serverPort));
        }

        if (this.numEpochs < 1) {
            throw new RuntimeException("numEpocs is not valid");
        } else if (this.lambda <= 0.0f) {
            throw new RuntimeException("lambda is not valid");
        } else if (this.dim <= 0) {
            throw new RuntimeException("dim is not valid");
        }

        String jobId = context.getJobID().toString();
        String taskAttemptId = context.getTaskAttemptID().toString();

        this.allreduceContext = new AllReduceContext(serverName, serverPort, jobId);
        this.allreducer = new WeightVectorWithCountSumAllReducer();

        this.cacheName = "cache_" + taskAttemptId;
        File cacheFile = new File(cacheName);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }
    
    @Override
    public void run(Context context) throws IOException, InterruptedException {
        setup(context);
        try {
            int i = 1;
            WeightVector weight = new WeightVector(dim);
            WeightVectorWithCount wwc = new WeightVectorWithCount(1, weight);
            FeatureVector datum = new FeatureVector();
            WritableCacheWriter<FeatureVector> cacheWriter = new WritableCacheWriter<FeatureVector>(cacheName);

            while (context.nextKeyValue()) {
                Object inKey = context.getCurrentKey();
                Text inValue = context.getCurrentValue();

                datum.clear();
                SVMLightFormatParser.parse(inValue.toString(), datum);
                cacheWriter.write(datum);

                float eta = 1.0f / (lambda * i);
                SVMPegasosLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
                i++;
            }

            wwc.setCount(1);
            wwc.setWeight(weight);
            allreducer.allreduce(allreduceContext, wwc);
            weight = wwc.getWeight();
            weight.scale(1.0f / wwc.getCount()); // averaging

            WritableCacheReader<FeatureVector> cacheReader = new WritableCacheReader<FeatureVector>(cacheName);
            for (int epoch = 1; epoch < numEpochs; epoch++) {
                while (cacheReader.read(datum) > 0) {
                    float eta = 1.0f / (lambda * i);
                    SVMPegasosLearner.learnWithStochasticOneStep(datum, eta, lambda, weight);
                    i++;
                }

                wwc.setCount(1);
                wwc.setWeight(weight);
                allreducer.allreduce(allreduceContext, wwc);
                weight = wwc.getWeight();
                weight.scale(1.0f / wwc.getCount()); // averaging

                cacheReader.reopen();
            }
            cacheReader.close();

            context.write(new Text(), new Text(weight.toString()));

        } finally {
            cleanup(context);

        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        File cacheFile = new File(cacheName);
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }
}

