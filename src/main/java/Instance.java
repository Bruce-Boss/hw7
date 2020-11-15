import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.Writable;

/*自定义坐标类*/

public class Instance implements Writable{
    ArrayList<Double> value;
//无参数时的构造函数
    public Instance(){
        value = new ArrayList<Double>();
    }
//参数为坐标字符串的构造函数
    public Instance(String line){
        String[] valueString = line.split(",");
        value = new ArrayList<Double>();
        for(int i = 0; i < valueString.length; i++){
            value.add(Double.parseDouble(valueString[i]));
        }
    }
//参数为Instance对象时的构造函数
    public Instance(Instance ins){
        value = new ArrayList<Double>();
        for(int i = 0; i < ins.getValue().size(); i++){
            value.add( ins.getValue().get(i));
        }
    }
//参数为整型（代表坐标维数）时的构造函数
    public Instance(int k){
        value = new ArrayList<Double>();
        for(int i = 0; i < k; i++){
            value.add(0.0);
        }
    }
//getValue方法用于获取坐标值
    public ArrayList<Double> getValue(){
        return value;
    }
//add方法用于当前对象代表的坐标与参数代表的坐标相加
    public Instance add(Instance instance){
        if(value.size() == 0)
            return new Instance(instance);
        else if(instance.getValue().size() == 0)
            return new Instance(this);
        else if(value.size() != instance.getValue().size())
            try {
                throw new Exception("can not add! dimension not compatible!" + value.size() + ","
                        + instance.getValue().size());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        else{
            Instance result = new Instance();
            for(int i = 0;i < value.size(); i++){
                result.getValue().add(value.get(i) + instance.getValue().get(i));
            }
            return result;
        }
    }
//multiply方法用于坐标值的数乘
    public Instance multiply(double num){
        Instance result = new Instance();
        for(int i = 0; i < value.size(); i++){
            result.getValue().add(value.get(i) * num);
        }
        return result;
    }
//divide方法用于坐标值的数除
    public Instance divide(double num){
        Instance result = new Instance();
        for(int i = 0; i < value.size(); i++){
            result.getValue().add(value.get(i) / num);
        }
        return result;
    }
//toString方法返回字符串形式的坐标值
    public String toString(){
        String s = new String();
        for(int i = 0;i < value.size() - 1; i++){
            s += (value.get(i) + ",");
        }
        s += value.get(value.size() - 1);
        return s;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        // TODO Auto-generated method stub
        out.writeInt(value.size());
        for(int i = 0; i < value.size(); i++){
            out.writeDouble(value.get(i));
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        // TODO Auto-generated method stub
        int size = 0;
        value = new ArrayList<Double>();
        if((size = in.readInt()) != 0){
            for(int i = 0; i < size; i++){
                value.add(in.readDouble());
            }
        }
    }
}
