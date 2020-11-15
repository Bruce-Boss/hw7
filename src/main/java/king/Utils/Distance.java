package king.Utils;

import java.util.List;
//定义Distance接口，可以用于之后定义各种类型的距离
public interface Distance<T> {
	double getDistance(List<T> a,List<T> b) throws Exception;
}
