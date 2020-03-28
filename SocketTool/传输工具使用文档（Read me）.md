# 传输工具使用文档（Read me）

### Java环境：jdk10

### 相关信息类介绍：

1. 文件夹信息类(DirInfoObject)：

   位于DirTree.java文件下

   信息方法：

   ```java
   //返回文件夹最后修改时间 格式:年年年年/月月/日日 时时/分分/秒秒
   String getDirChangeTime();
   
   //返回文件夹下项目数量
   long getNumbers();
   
   ```

2. 文件信息类(FileInfo):

   位于FileInfo.java文件下

   信息方法:

   ```java
   //返回文件名
   String getFileName();
   
   //返回文件原大小(上传时原文件大小)
   Long getFileLength();
   
   //返回文件最后修改时间 格式：年年/月月/日日
   String getFileChangeTime();	
   ```

   3.当前传输速度类(splitSpeedWatch)：

   位于SocketClient.java文件内

   信息方法：

   ```java
   //返回上次测得速度时间 (数据每秒更新 单位为kb)
   Long Speed();
   ```

### 相关操作类介绍：

客户端类（MainClient）:
位于MainClient.java文件内

方法：

```java
//初始化
MainClient(String hostName,int port);	//变量为IP地址和端口

MainClient();			//默认服务器连接

//关闭
void Close();

//用户登入 返回登入信息 格式：T或F:Fail Reasons
//Reasons 格式：1-Error UserName
//			   2-Error PWD
//			   404-Link lose
String Login(String UserName,String PWD);	//用户名 密码

//用户注册 返回注册信息 格式:T或F:Fail Reasons
//密码不得超过16位
//Reasons 格式：1-UserName exits
//			   2-Error form of UserName
//			   3-Error form of PWD
//			   404-Link lose
String Logon(String UserName,String PWD);

//文件夹进入（*登入后应默认进入0号文件夹* 即用户根文件夹 返回文件夹内信息
//返回信息格式：null 获取失败
//DirInfo;FileName-num,FileName-num...;DirName@@num,...;
//DirInfo可用于新建一个DirInfoObject对象
//FileName为当前文件夹内文件的文件名 num为文件记录内标号 用于查询
//DirName为当前文件夹下子文件夹名 num为文件夹记录内标号 用于查询
String DirIn(long DirNum); //切记初次登入调用进入0号文件夹

//获取据上次登入退出后的更新内容（格式与所述格式相同）
String[] getLastUpdate();

//刷新当前文件夹 返回值与DirIn相同
String DirFlush();

//获取一个指定文件夹的文件夹信息类 
DirInfoObject DirCheck(long DirNum);

//获取一个指定文件的文件信息类
FileInfo FileCheck(long FileNum);

//文件夹重命名
boolean DirRename(String OldName,long DirNum,String NewName);

//在当前目录下新建文件夹
boolean DirMake(String DirName);

//文件夹删除
boolean DirDelete(long Dirnum,String DirName);

//文件删除 
boolean FileDelete(String FileName);

//文件重命名
boolean FileRename(long Filenum,String NewName,String OldName);

//文件夹退回	返回父文件夹 到达根时将重复返回根 返回格式与DirIn相同
String Back();

//获取当前与根的相对路径
String getPath();

//文件传输
//mode:Send/Read  send为发送文件 read为接收当前目录文件
//FileName:要传输文件名
//LimitSpeed:限制速度 -1为不限速
//CB:传输完成后的返回函数
boolean FileTranslate(String mode,String LocalPath,String FileName,long LimitSpeed,CallBack CB);

//返回当前正在进行的传输的速度信息类
splitSpeedWatch SpeedWatch();

//暂停当前正在运行传输
void Pause();

//继续当前正在运行传输
void Active();

```

服务端类(MainServer,SSLSocketServer)：
位于MianServer.java与SSLSocketServer.java文件下
需要环境    postgresql-42.2.10.jar

方法：

```
//启动 直接在服务器端控制台启动 class文件即可
启动命令：将控制台跳转到/home/javatest/JavaSocket/src/ 下
    启动服务端
    java -cp ./postgresql-42.2.10.jar:./ MainServer
    启动传输服务端
    java -cp ./ SSLSocketServer
    (传输时确保两服务端都已启动)
    
```



### 环境配置：

从官网上下载jdk的.tar.gz压缩包传输到服务器 

在/usr目录下新建java文件夹  并在此文件夹中解压压缩包

在/etc/profile 文件中配置环境变量 在末尾加上

```
 JAVA_HOME=/usr/java/jdk1.x.x_111
      PATH=$JAVA_HOME/bin:$PATH
      CLASSPATH=.:$JAVA_HOME/lib/dt.jar:
      $JAVA_HOME/lib/tools.jar
      export JAVA_HOME
      export PATH
      export CLASSPATH
```

并使用 

```
source /etc/profile
```

使配置生效 最后使用

```
java -version
```

进行版本检验即可。

参考：

[]: https://jingyan.baidu.com/article/c1465413f3fb860bfdfc4c6a.html



### 服务端启动：

将源文件上传到服务端后输入服务端启动操作命令即可



### 客户端使用：

开启一个新的控制线程后新建客户端连接对象并调用客户端方法即可