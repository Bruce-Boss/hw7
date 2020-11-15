import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/*
 调度整个KMeans运行的过程
 */
public class KMeansDriver {
    private int k;
    private int iterationNum;
    private String sourcePath;
    private String outputPath;

    private Configuration conf;
/*
KMeans类的构造函数
将传入main函数的参数赋予类变量
非别为聚类数、迭代数、输入和输出路径
 */
    public KMeansDriver(int k, int iterationNum, String sourcePath, String outputPath, Configuration conf){
        this.k = k;
        this.iterationNum = iterationNum;
        this.sourcePath = sourcePath;
        this.outputPath = outputPath;
        this.conf = conf;
    }

    public void clusterCenterJob() throws IOException, InterruptedException, ClassNotFoundException{
        for(int i = 0;i < iterationNum; i++){
            //为迭代过程中的Job取名
            Job clusterCenterJob = Job.getInstance(conf,"Intermediate Job "+(i+1));
            clusterCenterJob .setJobName("clusterCenterJob" + i);
            clusterCenterJob .setJarByClass(KMeans.class);

            clusterCenterJob.getConfiguration().set("clusterPath", outputPath + "/cluster-" + i +"/");

            clusterCenterJob.setMapperClass(KMeans.KMeansMapper.class);
            clusterCenterJob.setMapOutputKeyClass(IntWritable.class);
            clusterCenterJob.setMapOutputValueClass(Cluster.class);

            clusterCenterJob.setCombinerClass(KMeans.KMeansCombiner.class);
            clusterCenterJob.setReducerClass(KMeans.KMeansReducer .class);
            clusterCenterJob.setOutputKeyClass(NullWritable.class);
            clusterCenterJob.setOutputValueClass(Cluster.class);

            FileInputFormat.addInputPath(clusterCenterJob, new Path(sourcePath));
            FileOutputFormat.setOutputPath(clusterCenterJob, new Path(outputPath + "/cluster-" + (i + 1) +"/"));

            clusterCenterJob.waitForCompletion(true);
            System.out.println("finished!");
        }
    }
 /*
 下面的Jod应该是原作者写错了，正确为Job
  */
    public void KMeansClusterJod() throws IOException, InterruptedException, ClassNotFoundException{
        /*
        修改最终输出的格式，key与value之间用逗号分隔，便于时候可视化的时候读入结果（懒得在读入之后再使用逗号和制表符去切割字符串）
         */
        conf.set("mapreduce.output.textoutputformat.separator", ",");
        //修改了源码中Job对象构造的new()方法，而使用可以使用指定conf并设定Job名称的静态方法Job.getInstance
        Job kMeansClusterJob = Job.getInstance(conf,"Final Job");

        kMeansClusterJob.setJobName("KMeansClusterJob");
        kMeansClusterJob.setJarByClass(KMeansCluster.class);

        kMeansClusterJob.getConfiguration().set("clusterPath", outputPath + "/cluster-" + (iterationNum - 1) +"/");

        kMeansClusterJob.setMapperClass(KMeansCluster.KMeansClusterMapper.class);
        kMeansClusterJob.setMapOutputKeyClass(Text.class);
        kMeansClusterJob.setMapOutputValueClass(IntWritable.class);

        kMeansClusterJob.setNumReduceTasks(0);

        FileInputFormat.addInputPath(kMeansClusterJob, new Path(sourcePath));
        FileOutputFormat.setOutputPath(kMeansClusterJob, new Path(outputPath + "/clusteredInstances" + "/"));

        kMeansClusterJob.waitForCompletion(true);
        System.out.println("finished!");
    }

    public void generateInitialCluster(){
        RandomClusterGenerator generator = new RandomClusterGenerator(conf, sourcePath, k);
        generator.generateInitialCluster(outputPath + "/");
    }

    public static void main(String[] args) throws IOException, InterruptedException, ClassNotFoundException{
        System.out.println("start");
        Configuration conf = new Configuration();
        //解析参数
    
        int k = Integer.parseInt(args[0]);
        int iterationNum = Integer.parseInt(args[1]);
        String sourcePath = args[2];
        String outputPath = args[3];
        //实例化一个KMeansDriver对象
        KMeansDriver driver = new KMeansDriver(k, iterationNum, sourcePath, outputPath, conf);
        driver.generateInitialCluster();
        //随机生成k个初始中心
        System.out.println("initial cluster finished");
        driver.clusterCenterJob();
        driver.KMeansClusterJod();
        
    }
}
