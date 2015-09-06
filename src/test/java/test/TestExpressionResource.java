package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import se.liu.imt.mi.snomedct.simplepcserver.SimplePostcoordinationServerResource;

public class TestExpressionResource {
	
	private static final Logger log = Logger
			.getLogger(SimplePostcoordinationServerResource.class);
	private static Configuration config = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		// initialize configuration
		try {
			config = new XMLConfiguration("config.xml");
			log.debug("Configuration in 'config.xml' loaded");
		} catch (Exception e) {
			log.debug("Exception", e);
			throw e;
		}
		
		SimplePostcoordinationServerResource.main(new String[] {});
		
		log.info("Started server");
	}

	@Test
	public void test1TestSyntaxError() throws IOException {
		String url = "http://localhost:" + config.getString("server.port", "8184");
		ClientResource clientResource = new ClientResource(url);
		clientResource.accept(MediaType.APPLICATION_JSON);
		Form form = new Form();
		form.set("expression", "125605004|fracture of bone|:{363698007|finding site|,=71341001|bone structure of femur|}");
		
		Representation representation = clientResource.post(form, MediaType.TEXT_PLAIN);
		
		String result = representation.getText();
		
		log.info(result);
		assertTrue(result.equals("{\"status\": \"error\", \"cause\": \"line 1, pos 52: extraneous input ',' expecting '='\", \"line\": 1, \"pos\": 52}"));
	}
	
	@Test
	public void test2TestCorrectSyntax() throws IOException {
		String url = "http://localhost:" + config.getString("server.port", "8184");
		ClientResource clientResource = new ClientResource(url);
		clientResource.accept(MediaType.APPLICATION_JSON);
		Form form = new Form();
		form.set("expression", "125605004|fracture of bone|:{363698007|finding site|=71341001|bone structure of femur|}");
		
		Representation representation = clientResource.post(form, MediaType.TEXT_PLAIN);
		
		String result = representation.getText();
		
		log.info(result);
		assertTrue(result.equals("{\"status\": \"ok\"}"));
	}

}
