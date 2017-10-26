package xbus;

import xbus.stream.terminal.zk.DefaultConfigurator;

/**
 * 
 * @author bailey
 * @version 1.0
 * @date 2017-10-20 17:24
 */
public class BusProperties{
	public static void main(String[] args) throws Exception{
		DefaultConfigurator b=new DefaultConfigurator();
		b.setServers("127.0.0.1:2181");
		b.setAppName("app1");
		b.setRootPath("/xbus");
		new Thread(){
			@Override
			public void run() {
				b.init();
			}
		}.start();
		Thread.sleep(1000*60*3);
		b.destory();
	}
}
