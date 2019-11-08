package org.apache.vfs2.spring.boot;

import java.io.File;

import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnClass({ FileSystemManager.class})
@EnableConfigurationProperties(VFS2Properties.class)
public class VFS2AutoConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(VFS2AutoConfiguration.class);

	private final ApplicationContext applicationContext;

	private final VFS2Properties properties;

	public VFS2AutoConfiguration(ApplicationContext applicationContext, VFS2Properties properties) {
		this.applicationContext = applicationContext;
		this.properties = properties;
	}
 
	/**
	 * 可以操作的文件
     * jar:../lib/classes.jar!/META-INF/manifest.mf
     * zip:http://somehost/downloads/somefile.zip
     * jar:zip:outer.zip!/nested.jar!/somedir
     * jar:zip:outer.zip!/nested.jar!/some%21dir
     * tar:gz:http://anyhost/dir/mytar.tar.gz!/mytar.tar!/path/in/tar/README.txt
     * tgz:file://anyhost/dir/mytar.tgz!/somepath/somefile
     * gz:/my/gz/file.gz
     * hdfs://somehost:8080/downloads/some_dir
     * hdfs://somehost:8080/downloads/some_file.ext
	 * webdav://somehost:8080/dist
     * http://somehost:8080/downloads/somefile.jar
     * http://myusername@somehost/index.html
	 * ftp://myusername:mypassword@somehost/pub/downloads/somefile.tgz
	 * ftps://myusername:mypassword@somehost/pub/downloads/somefile.tgz
	 * sftp://myusername:mypassword@somehost/pub/downloads/somefile.tgz
	 * tmp://dir/somefile.txt
	 * res:path/in/classpath/image.png
	 * ram:///any/path/to/file.txt
	 * mime:file:///your/path/mail/anymail.mime!/
     * mime:file:///your/path/mail/anymail.mime!/filename.pdf
     * mime:file:///your/path/mail/anymail.mime!/_body_part_0
	 * @return
	 * @throws FileSystemException
	 */
	public FileSystemManager  FileSystemManager() throws FileSystemException {
		FileSystemManager fsm = VFS.getManager();
		
		fsm.
		
		    FileObject file = fsm.resolveFile(new File("D://vfs").getAbsolutePath());
		    
		    DefaultFileMonitor fileMonitor = new DefaultFileMonitor(new FileListener() {
		        @Override
		        public void fileCreated(FileChangeEvent event) throws Exception {
		            resolveEvent("Created",event);
		        }
		
		        @Override
		        public void fileDeleted(FileChangeEvent event) throws Exception {
		            resolveEvent("Deleted",event);
		        }
		
		        @Override
		        public void fileChanged(FileChangeEvent event) throws Exception {
		            resolveEvent("Changed",event);
		        }
		
		        private void resolveEvent(String type, FileChangeEvent event){
		            FileObject fileObject = event.getFile();
		            FileName fileName = fileObject.getName();
		            System.out.println(type + ": " + fileName.toString());
		        }
		    });
		    fileMonitor.addFile(file);
		    fileMonitor.start();
		    while(true){
		        Thread.sleep(1000);
		    }
		return fsm;
	}
	
}
