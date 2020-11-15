import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

//定义Cluster类，代表簇
public class Cluster implements Writable{
    //簇的三个特征信息
    private int clusterID;
    private long numOfPoints;
    private Instance center;
//以下是一些基本的构造函数和类变量交互函数等
    public Cluster(){
        this.setClusterID(-1);
        this.setNumOfPoints(0);
        this.setCenter(new Instance());
    }

    public Cluster(int clusterID,Instance center){
        this.setClusterID(clusterID);
        this.setNumOfPoints(0);
        this.setCenter(center);
    }

    public Cluster(String line){
        String[] value = line.split(",",3);
        clusterID = Integer.parseInt(value[0]);
        numOfPoints = Long.parseLong(value[1]);
        center = new Instance(value[2]);
    }

    public String toString(){
        String  result = String.valueOf(clusterID) + ","
                + String.valueOf(numOfPoints) + "," + center.toString();
        return result;
    }

    public int getClusterID() {
        return clusterID;
    }

    public void setClusterID(int clusterID) {
        this.clusterID = clusterID;
    }

    public long getNumOfPoints() {
        return numOfPoints;
    }

    public void setNumOfPoints(long numOfPoints) {
        this.numOfPoints = numOfPoints;
    }

    public Instance getCenter() {
        return center;
    }

    public void setCenter(Instance center) {
        this.center = center;
    }
//observeInstance（）用于在簇中新增一个点
    public void observeInstance(Instance instance){
        try {
            Instance sum = center.multiply(numOfPoints).add(instance);
            numOfPoints++;
            center = sum.divide(numOfPoints);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void write(DataOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(clusterID);
        out.writeLong(numOfPoints);
        center.write(out);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        // TODO Auto-generated method stub
        clusterID = in.readInt();
        numOfPoints = in.readLong();
        center.readFields(in);
    }
}
