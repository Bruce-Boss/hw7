import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Random;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;

/**
该类生成初始聚类中心作为后续处理的输入。
它随机选择k个坐标点作为初始k个中心。
 */
public final class RandomClusterGenerator {
	private int k;
	
	private FileStatus[] fileList;
	private FileSystem fs;
	private ArrayList<Cluster> kClusters;
	private Configuration conf;
	
	public RandomClusterGenerator(Configuration conf,String filePath,int k){
		this.k = k;
		try {
			fs = FileSystem.get(URI.create(filePath),conf);
			fileList = fs.listStatus((new Path(filePath)));
			kClusters = new ArrayList<Cluster>(k);
			this.conf = conf;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @param destinationPath the destination Path we will store
	 * our cluster file in.the initial file will be named clusters-0
	 */
	public void generateInitialCluster(String destinationPath){
		Text line = new Text();
		FSDataInputStream fsi = null;
		try {
			for(int i = 0;i < fileList.length;i++){
				fsi = fs.open(fileList[i].getPath());
				LineReader lineReader = new LineReader(fsi,conf);
				while(lineReader.readLine(line) > 0){
		//判断是否应该加入到中心集合中去
					System.out.println("read a line:" + line);
					Instance instance = new Instance(line.toString());
					makeDecision(instance);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				//in.close();
				fsi.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		writeBackToFile(destinationPath);
		
	}
	
	public void makeDecision(Instance instance){
		if(kClusters.size() < k){
			Cluster cluster = new Cluster(kClusters.size() + 1, instance);
			kClusters.add(cluster);
		}else{
			int choice = randomChoose(k);
			if(!(choice == -1)){
				int id = kClusters.get(choice).getClusterID();
				kClusters.remove(choice);
				Cluster cluster = new Cluster(id, instance);
				kClusters.add(cluster);
			}
		}
	}
	
	/**
	 * 以1/(1+k)的概率返回一个[0,k-1]中的正整数,以
	 * k/k+1的概率返回-1.
	 */
	public int randomChoose(int k){
		Random random = new Random();
		if(random.nextInt(k + 1) == 0){
			return new Random().nextInt(k);
		}else
			return -1;
	}
	
	public void writeBackToFile(String destinationPath){
		Path path = new Path(destinationPath + "cluster-0/clusters");
		FSDataOutputStream fsi = null;
		try {
			fsi = fs.create(path);
			for(Cluster cluster : kClusters){
				fsi.write((cluster.toString() + "\n").getBytes());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fsi.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}	
}
