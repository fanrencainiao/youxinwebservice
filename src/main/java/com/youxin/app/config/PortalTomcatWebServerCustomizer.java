package com.youxin.app.config;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;
/**
 * 传入特殊字符json参数处理
 * @author cf
 *
 */
@Component
public class PortalTomcatWebServerCustomizer implements
        WebServerFactoryCustomizer {



	@Override
	public void customize(WebServerFactory factory) {
	     TomcatServletWebServerFactory containerFactory = (TomcatServletWebServerFactory) factory;
	        containerFactory.addConnectorCustomizers(new TomcatConnectorCustomizer() {
	            @Override
	            public void customize(Connector connector) {
	            	//这里需要转义，微博上被转换了
	                connector.setAttribute("relaxedQueryChars", "[]|{}^&#x5c;&#x60;&quot;&lt;&gt;\\n;");
	                connector.setAttribute("relaxedPathChars", "[]|");
	            }
	        });
		
	}
}