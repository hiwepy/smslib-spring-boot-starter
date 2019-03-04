# spring-boot-starter-smslib


SMS短信（Short Messaging Service）客户端实现
1、基于定义通用的短消息收发接口
2、基于第三方接口实现不同的短消息收发实现(百度提供的各种第三方短信调用接口)，以便给业务系统直接使用
http://apistore.baidu.com/astore/classificationservicelist/39.html?pageNum=1

https://blog.csdn.net/xuepiaohan2006/article/details/38172495


RxTx : 
http://rxtx.qbang.org/wiki/index.php/Download
http://fizzed.com/oss/rxtx-for-java

#描述
======================================================================

SMSLib实现Java短信收发的功能
http://sjsky.iteye.com/blog/1045502
https://blog.csdn.net/xuepiaohan2006/article/details/38172495

用java实现短信收发的功能，目前一般项目中短信群发功能的实现方法大致有下面三种： 
1、 向运行商申请短信网关，不需要额外的设备，利用运行商提供的API调用程序发送短信，适用于大型的通信公司。
2、 借助像GSM MODEM之类的设备（支持AT指令的手机也行），通过数据线连接电脑来发送短信，这种方法比较适用于小公司及个人。要实现这种方式必须理解串口通信、AT指令、短信编码、解码。
3、 借助第三方运行的网站实现，由网站代发短信数据，这种方法对网站依赖性太高，对网络的要求也比较高。

       鉴于项目的情况和多方考虑，同时又找到了一个开源的SMSLib项目的支持，比较倾向于第二种方法，SMSLib的出现就不需要我们自己去写底层的AT指令，这样就可以直接通过调用SMSLib的API来实现通过GSM modem来收发送短信了。 

SMSLib官方网站：http://smslib.org/，使用SMSLib的一些基本要点： 
SUN JDK 1.6 or newer. （Java环境）
Java Communications Library. （Java串口通信）
Apache ANT for building the sources. （编译源码时需要的）
Apache log4j. （日志工具）
Apache Jakarta Commons - NET. （网络操作相关的）
JSMPP Library （SMPP协议时需要的）

有关Java串口通信需要补充说明： 
window系统可以用SUN Java Comm v2. （该版本好像也支持solaris）
         其下载地址：http://smslib.googlecode.com/files/javacomm20-win32.zip
其他操作系统(比如：Linux, Unix, BSD,等等),你可以选择 Java Comm v3 或者是RxTx。
         Java Comm v3下载地址：http://java.sun.com/products/javacomm/（需要注册）； 
         RxTx官网：http://users.frii.com/jarvi/rxtx/index.html or http://rxtx.qbang.org/wiki/index.php/Main_Page 

附件提供相关下载： 
java串口通信v2：javacomm20-win32.zip
smslib-3.5.1.jar
短信 modem驱动：PL2303_Prolific_DriverInstaller_v130.zip

本次测试的环境是window，GSM modem是wavecom，所以这次主要描述window环境下简单的实现过程： 
【一】、配置相应的环境 
      首先解压下载的Java Comm v2文件javacomm20-win32.zip，具体配置步骤如下： 
把文件：comm.jar copy 到目录：<JDKDIR>/jre/lib/ext/，当然这一步也可以不要这样做，你只需把comm.jar copy到所要运行的项目对应的lib/下既可
把文件：javax.comm.properties copy 到目录：<JDKDIR>/jre/lib/
把DLL文件：win32com.dll（windows） copy 到目录：<JDKDIR>/jre/bin/
如果存在JRE目录, 最好按照上面步骤把文件copy到<JREDIR>相应的目录下

【二】、测试串口端口程序：