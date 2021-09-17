package xbus.stream.terminal.ek;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.util.Asserts;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import xbus.BusLoggerHolder;
import xbus.stream.terminal.Terminal;
import xbus.stream.terminal.TerminalNode;
import com.lz.components.common.enums.MSDEnum;
import com.lz.components.common.util.variable.SimpleActionGenerator;
import com.lz.components.security.codec.B64Encrypter;

/**
 * Eureka注册监听
 * 
 * @author fuli
 * @date 2019年5月27日
 * @version 1.0.0
 */
public class EurekaListener implements BusLoggerHolder{
	private static final int MIN_FETCH_INTERVAL = 10;
	private SimpleActionGenerator simpleActionGenerator;
	private Consumer<Set<Terminal>> terminalUpdater;
	private List<String[]> zones;
	private int renewInterval;
	private boolean running;
	
	public EurekaListener(String zone, int renewInterval) {
		this.zones = new ArrayList<>();
		for (String z : zone.split(",")) {
			if (!StringUtils.isBlank(z)) {
				if (z.indexOf("@") == -1) {
					this.zones.add(new String[] { z.trim() + "/apps" });
				} else {
					String[] zoneInfo = new String[3];
					String userInfo=StringUtils.substringBetween(z, "http://", "@");
					zoneInfo[0] = z.replace(userInfo + "@", "") + "/apps";
					String[] userInfoArray = userInfo.split(":");
					zoneInfo[1] = userInfoArray[0];
					zoneInfo[2] = userInfoArray[1];
					this.zones.add(zoneInfo);
				}
			}
		}
		Asserts.check(zones.size() > 0, "eureka's zone format error !");
		this.renewInterval = renewInterval;
		this.running = false;
	}
	private void flush() {
		if (terminalUpdater != null) {
			RestTemplate template = new RestTemplate();
			for (String[] zoneInfo : zones) {
				Set<Terminal> terminals = null;
				try {
					String resultString = null;
					if (zoneInfo.length == 3) {
						HttpHeaders requestHeaders = new HttpHeaders();
						requestHeaders.add("Authorization", "Basic " + B64Encrypter.encrypt(zoneInfo[1] + ":" + zoneInfo[2]));
						HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);
						resultString = template.exchange(zoneInfo[0], HttpMethod.GET, requestEntity, String.class).getBody();
					} else {
						resultString = template.getForObject(zoneInfo[0], String.class);
					}
					terminals = parseJSON(resultString);
				} catch (Exception e) {
					LOGGER.error("EurekaListener flush error !", e);
				}
				terminalUpdater.accept(terminals);
				break;
			}
		}
	}
	Set<Terminal> parseXML(String resultString)throws Exception{
		Set<Terminal> terminals = new HashSet<>();
		Document document = null;
		try (StringReader sr = new StringReader(resultString.trim())) {
			document = new SAXReader().read(sr);
		}
		// applications
		Element applications = document.getRootElement();
		Iterator<?> appIt = applications.elementIterator();
		INNER:while (appIt.hasNext()) {
			Element application = (Element) appIt.next();
			String name = application.elementTextTrim("name").toLowerCase();
			MSDEnum msdEnum = MSDEnum.valueOfServiceName(name);
			if (msdEnum == MSDEnum.NOT_SPECIFIED) {
				continue INNER;
			}
			Set<TerminalNode> nodes = new HashSet<>();
			//instance
			Iterator<?> insIt = application.elementIterator("instance");
			while (insIt.hasNext()) {
				Element instance = (Element) insIt.next();
				TerminalNode terminalNode = new TerminalNode(msdEnum.SERVICE_ID);
				terminalNode.setIp(instance.elementTextTrim("ipAddr"));
				terminalNode.setPort(NumberUtils.toInt(instance.elementTextTrim("port "), 0));
				nodes.add(terminalNode);
			}
			Terminal terminal = new Terminal();
			terminal.setName(msdEnum.SERVICE_ID);
			terminal.setNodes(nodes);
			terminals.add(terminal);
		}
		return terminals;
	}
	Set<Terminal> parseJSON(String resultString) {
		Set<Terminal> terminals = new HashSet<>();
		JSONObject root = JSON.parseObject(resultString);
		JSONObject applications = root.getJSONObject("applications");
		if (applications != null) {
			JSONArray appArray = applications.getJSONArray("application");
			for (int i = 0; i < appArray.size(); i++) {
				JSONObject app = appArray.getJSONObject(i);
				String name = app.getString("name").toLowerCase();
				MSDEnum msdEnum = MSDEnum.valueOfServiceName(name);
				if (msdEnum == MSDEnum.NOT_SPECIFIED) {
					continue;
				}
				Set<TerminalNode> nodes = new HashSet<>();
				JSONArray insArray = app.getJSONArray("instance");
				for (int j = 0; j < insArray.size(); j++) {
					JSONObject ins = insArray.getJSONObject(j);
					TerminalNode terminalNode = new TerminalNode(msdEnum.SERVICE_ID);
					terminalNode.setIp(ins.getString("ipAddr"));
					terminalNode.setPort(ins.getJSONObject("port").getIntValue("$"));
					nodes.add(terminalNode);
				}
				Terminal terminal = new Terminal();
				terminal.setName(msdEnum.SERVICE_ID);
				terminal.setNodes(nodes);
				terminals.add(terminal);
			}
		}
		return terminals;
	}
	void setTerminalUpdater(Consumer<Set<Terminal>> terminalUpdater) {
		this.terminalUpdater = terminalUpdater;
	}

	synchronized void start() {
		if (!running) {
			running = true;
			if (renewInterval <= 0) {
				flush();
			} else {
				simpleActionGenerator = new SimpleActionGenerator(renewInterval < MIN_FETCH_INTERVAL ? MIN_FETCH_INTERVAL : renewInterval, TimeUnit.SECONDS, this::flush);
				simpleActionGenerator.setName("EurekaListener");
				simpleActionGenerator.start();
			}
		}
	}

	synchronized void stop() {
		if (simpleActionGenerator != null) {
			simpleActionGenerator.stop();
		}
	}

}
