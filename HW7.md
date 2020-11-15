# 作业7——KMeans

代码仓库https://github.com/Bruce-Boss/hw7(BDKIT git push 出现问题 远程仓库未更新)![image-20201115234755710](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115234755710.png)

同时，由于无法git push，bdkit打包的.jar未提供。

## 运行环境

- win10+IDEA+hadoop单机完成编码调试

- bdkit完成集群中任务提交和运行。

## 源码解读与修改

​	开始时，因为有了先前两次的mapreduce编码经验，也完全掌握了K-Means算法，我考虑从零完成这一任务。后来经过测试，我发现本次作业可以直接使用黄宜华老师教材提供的源码。因此，我决定将本次编码任务转变为代码解读任务，尝试分析该项目源码，并进行细微修改。

​	我刚打开源码时有些意外，因为不是原来那种一个.java文件就能完成作业的情况。该项目有11个.java文件，还有名为king的包，让我吃了一惊。本以为可能只有名为KMeans的文件是真正有用的（其他都是扩充功能用到的类），只用五分钟就能看个大概。没想到这个类里面甚至连main函数都没有，还导入了奇怪的名为King的包。（后来发现，King其实是项目的作者...）

​	在放弃了短时间内读懂一个源文件就能解决任务的幻想后，我开始仔细分析这个项目所用到的各个类以及他们之间的关系和在任务中所起的作用。

​	首先，我在KMeansDriver中找到了main函数。找到main函数，就找到了程序运行的入口。之后我抽丝剥茧，逐渐理清了项目的逻辑（具体说明见源码中的注释）：

| 类（.java文件）名      | 描述                                                         |
| ---------------------- | ------------------------------------------------------------ |
| Instance               | 代表坐标的数据类型                                           |
| Cluster                | 代表簇的数据类型（核心数据成员是簇id，中心坐标，包含点数）   |
| RandomClusterGenerator | 用于生成初始聚类中心                                         |
| Kmeans                 | 用于完成算法迭代过程中的簇更新（不包括初始化）               |
| King.Utils.*           | 定义了距离接口，完成了几个实现，用于在算法中计算各种自定义的距离。（本例中使用到的文件只有接口Distance和欧氏距离类EuclideanDistance。 |
| KMeansCluster          | 完成最后一次迭代的结果输出                                   |
| KMeansDriver           | 整个流程的控制单元，负责读入原始数据。之后调用不同的类，完成初始聚类中心的产生，簇的迭代和最终结果的生成。 |

​	之后我开始尝试对代码进行一些有价值的修改。我感觉类定义的有些多了，封装过度了，但是在尝试修改后发现当前的封装模式确实很清晰，也便于维护。后来我又尝试将KMeans类和KMeansCluster合并，因为二者都是迭代的步骤，只是输出的结果不同。但是后来发现，二者在功能上和逻辑上确实有不小区别，理应作为两个类。

​	最后我只对源码进行了小幅修改，删除了King.Utils中的一些无用文件，修改了最终结果的输出格式（便于可视化过程中的解析）。



## 运行说明

### 输入文件
格式如下:<br/>
1,2,3,4,5<br/>
3,4,6,5,1<br/>
每行一个实例。
### 运行
输入参数：<br>
k: 簇中心数<br/>
iteration num: 迭代数<br/>
input path: 输入路径<br/>
output path: 输出路径<br/>
打包成jar后，运行：<br/>


 >hadoop  jar  target/K-Means-1.0.jar  <k\> <iteration num\> <input path\> <output path\> 



## 运行截图

源码打包

![image-20201115224825925](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115224825925.png)



Resource-Manager中显示先完成了5次迭代，最后一次完成最终结果![image-20201115224115318](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115224115318.png)



输出结果（我修改了输出格式，使得key和value之间以逗号分隔，便于之后可视化时读入后的字符串解析）

![image-20201115224446514](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115224446514.png)



下载到本地的输出文件

![image-20201115225006345](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115225006345.png)

## 结果可视化

使用R语言的ggplot2包进行绘图。设置相同迭代次数和不同聚类数，观察聚类结果

- K=2

  ![image-20201115230119278](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115230119278.png)

- k=3

  ![image-20201115225756191](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115225756191.png)

- K=4![image-20201115230723379](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115230723379.png)

## 使用手肘法找出最优的K：
### 手肘法

#### - 核心指标：SSE(sum of the squared errors，误差平方和)

![img](https:////upload-images.jianshu.io/upload_images/6315044-0f940635bb586388.png?imageMogr2/auto-orient/strip|imageView2/2/w/387/format/webp)

- Ci是第i个簇
- p是Ci中的样本点
- mi是Ci的质心（Ci中所有样本的均值）
- SSE是所有样本的聚类误差，代表了聚类效果的好坏。

#### -手肘法核心思想

- 随着聚类数k的增大，样本划分会更加精细，每个簇的聚合程度会逐渐提高，那么误差平方和SSE自然会逐渐变小。

- 当k小于真实聚类数时，由于k的增大会大幅增加每个簇的聚合程度，故SSE的下降幅度会很大，而当k到达真实聚类数时，再增加k所得到的聚合程度回报会迅速变小，所以SSE的下降幅度会骤减，然后随着k值的继续增大而趋于平缓，也就是说SSE和k的关系图是一个手肘的形状，而这个肘部对应的k值就是数据的真实聚类数

#### -手肘法可视化

![image-20201115231805603](C:\Users\CYJ\AppData\Roaming\Typora\typora-user-images\image-20201115231805603.png)



  可以认为手肘的拐点在4，因此最佳的K为4。（其实也看不出来是4，因为数据是随机生成的，本身没有很强的聚类性）

## 有趣的发现

之前mapreduce任务输出的文件名为part-r-00000，而本次作业中，中间迭代任务的输出文件为part-r-00000，最终结果为part-m-00000。

原因：当Reduce函数中有落盘操作，且指定CombinerClass为Reduce函数，则输出结果文件为多个包含“-m-”的文件，如果不指定CombinerClass，则生成文件为包含“-r-”的单个文件。当然，输出的文件名也是可以自定义的。