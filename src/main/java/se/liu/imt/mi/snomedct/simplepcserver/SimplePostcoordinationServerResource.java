package se.liu.imt.mi.snomedct.simplepcserver;

/**
 *
 */

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Form;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.adapter.HttpResponse;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.util.Series;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class SimplePostcoordinationServerResource extends ServerResource {

	private static final Logger log = Logger
			.getLogger(SimplePostcoordinationServerResource.class);
	private static Configuration config = null;

	public static void main(String[] args) throws Exception {

		// initialize configuration
		try {
			config = new XMLConfiguration("config.xml");
			log.debug("Configuration in 'config.xml' loaded");
		} catch (Exception e) {
			log.debug("Exception", e);
			throw e;
		}

		new Server(Protocol.HTTP, config.getInt("server.port"),
				SimplePostcoordinationServerResource.class).start();

	}

	@Options
	public Response _options() {
		Response response = getResponse();
		response.setAccessControlAllowOrigin("*");
		response.setAccessControlAllowMethods(new HashSet<Method>(Arrays.asList(Method.POST, Method.OPTIONS)));
		return response;
	}

	@Post("application/json")
	public Representation check_expression(Representation entity) {

		Response response = getResponse();
		response.setAccessControlAllowOrigin("*");
		response.setAccessControlAllowMethods(new HashSet<Method>(Arrays.asList(Method.POST, Method.OPTIONS)));

		Form form = new Form(entity);
		String expression = form.getFirstValue("expression");
		if (expression == null) {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return new StringRepresentation("{}", MediaType.APPLICATION_JSON);
		}

		try {
			ParseTree tree = null;
			if (expression.startsWith("("))
				tree = se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil
						.parseStatement(expression);
			else
				tree = se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil
						.parseExpression(expression);
			return new StringRepresentation("{\"status\": \"ok\"}",
					MediaType.APPLICATION_JSON);
		} catch (Exception e) {
			Pattern pattern = Pattern.compile("^line (\\d+), pos (\\d+)");
			String message = e.getCause().getMessage();
			Matcher matcher = pattern.matcher(message);
			matcher.find();
			if (matcher.groupCount() == 2) {
				String result = "{\"status\": \"error\", \"cause\": \""
						+ e.getCause().getMessage() + "\", \"line\": "
						+ matcher.group(1) + ", \"pos\": " + matcher.group(2)
						+ "}";
				return new StringRepresentation(result,
						MediaType.APPLICATION_JSON);
			}
			return new StringRepresentation("{\"status\": \"error\"}",
					MediaType.APPLICATION_JSON);
		}
	}
}
