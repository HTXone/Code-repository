# Code-repository
代码存放

#### 19/12/20：

​	新建了java的对外接口类文件 暂时不确定主要方法与组成

#### 20/1/15：

​	完成了简单客户端和服务端之间的文本通信

​	源码：ChartServer.java(服务端) ClientTest.java(客户端)  位于socketLearning目录下

​	完成了简单的客户端和服务端之间的kb级文件传输

​	源码：TextFileServer.java(服务端) TextFileServer.java(客户端) 

​	客户端获取.txt文件后将其转为二进制进行传输 传输到服务端后以.bin形式保存 服务端将同一个.bin文件以二进制流传输给客户端 在客户端下转为.txt文件保存

​	完成了简单的客户端和服务端Mb级二进制文件传输

​	源码：NIOServer.java(服务端) NIOClient.java(客户端)

​	使用NIO流技术 完成了简单的Mb级别文本传输Demo 暂时使用BufferInputStream 如果传输要求文件过大可用MappedByteBuffer进行优化