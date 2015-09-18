package se.liu.imt.mi.snomedct.simplepcserver;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.log4j.Logger;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import se.liu.imt.mi.snomedct.expression.tools.ExpressionSyntaxError;
import se.liu.imt.mi.snomedct.expression.tools.SNOMEDCTParserUtil;
import se.liu.imt.mi.snomedct.parser.SVGPart;
import se.liu.imt.mi.snomedct.parser.SVGVisitor;

public class SVGDiagramResource extends ServerResource {

	private static Logger log = Logger.getLogger(SVGDiagramResource.class);

	@Options
	public Response _options() {
		Response response = getResponse();
		response.setAccessControlAllowOrigin("*");
		response.setAccessControlAllowMethods(new HashSet<Method>(Arrays
				.asList(Method.POST, Method.OPTIONS)));
		return response;
	}

	@Post()
	public Representation getGraph(Representation entity) {

		@SuppressWarnings("unchecked")
		HashMap<Long, Boolean> concepts = (HashMap<Long, Boolean>) getContext()
				.getAttributes().get("concepts");

		Response response = getResponse();
		response.setAccessControlAllowOrigin("*");
		response.setAccessControlAllowMethods(new HashSet<Method>(Arrays
				.asList(Method.POST, Method.OPTIONS)));

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

			SVGVisitor visitor = new SVGVisitor(concepts, false);

			SVGPart result = visitor.visit(tree);

			return new StringRepresentation(result.getSVG(),
					MediaType.IMAGE_SVG);
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
