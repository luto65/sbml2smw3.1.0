package il.org.tibc.sbml2smw.tS;

/*
 * Copyright (C) 2008-2010, fluid Operations GmbH
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
import il.org.tibc.sbml2smw.o.Species;
import il.org.tibc.sbml2smw.L.LanguageSpecs;
import net.sourceforge.jwbf.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;


/**
 * Requests for Reactions to be loaded from the wiki are routed to this servlet
 * 
 * @author Tobias
 *
 */
public class LoadReactionServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8298757541094356262L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		ServletOutputStream out = resp.getOutputStream();
		SailRepository rep = new SailRepository(new MemoryStore());
		try {
			rep.initialize();
		} catch (RepositoryException e1) {
			e1.printStackTrace();
		}

		try {
			// get all important information from the request
			RepositoryConnection conn = rep.getConnection();
			System.out.println();
			System.out.println("****CHECKING PARAMETERS FROM CONFIG****");
			String name = req.getParameter("name");
			System.out.println("Name: " + name.replace("-", "-2D"));
			System.out.println();
			String type = req.getParameter("type");
			System.out.println("Type: " + type);
			String protType = req.getParameter("protType");
			System.out.println("Protein Type: " + protType);
			String wikiUrl = req.getParameter("wiki");
			System.out.println("Wiki URL");
			String namespace = req.getParameter("namespace");
			System.out.println("Namespace: " + namespace);
			String language = req.getParameter("language");
			System.out.println("Language: " + language);
			String resolveString = req.getParameter("resolve");
			System.out.println("*********resolve as in file: " + resolveString);
			boolean resolve = Boolean.parseBoolean(req.getParameter("resolve"));
			System.out.println("Resolve URIs " + resolve);
			resolve = true;
			
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
			if (name.contains("modified_")) {
				name = name.replace("modified_", "modified-");
			}
			URL url = new URL(wikiUrl + LanguageSpecs.getExportRdf(language) + name);
			URLConnection con = url.openConnection();
			con.setDoOutput(true);

			BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
			
			conn.add(rd, "", RDFFormat.RDFXML);
			RepositoryResult<Statement> res11 = conn.getStatements(null, null, null, false);
			
			System.out.println(conn.size());
			/*
			 * This query checks if the type of the species marked in the model equals the type of the species stored in the wiki
			 */
			String checkQueryString = "SELECT ?type WHERE { ?x <" + RDFS.LABEL + "> \"" + name + "\" . ?x <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "Type> ?type . }";
			System.out.println("Type check query: " + checkQueryString);
			TupleQueryResult checkRes = conn.prepareTupleQuery(QueryLanguage.SPARQL, checkQueryString).evaluate();
			if (!checkRes.hasNext()) {
				System.out.println("type checking query did not get any results...");
			}
			while (checkRes.hasNext()) {
				System.out.println("Found species in DB: " + name);
				BindingSet temp = checkRes.next();
				if (!((URI) temp.getBinding("type").getValue()).getLocalName().toString().equals(type))
					return;
				else if (((URI) temp.getBinding("type").getValue()).getLocalName().toString().equals("PROTEIN")) {
					checkQueryString = "SELECT ?type WHERE { ?x <" + RDFS.LABEL + "> '" + name +  "\' . ?x <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "ProteinType> ?type . }";
					System.out.println(checkQueryString);
					checkRes = conn.prepareTupleQuery(QueryLanguage.SPARQL, checkQueryString).evaluate();
					while (checkRes.hasNext()) {
						temp = checkRes.next();
						if (!((URI) temp.getBinding("type").getValue()).getLocalName().toString().equals(protType))
							return;
					}
				}
			}
			
			/*
			 * find all reactions having the passed species as a product or a reactant
			 */
			String queryString = "SELECT ?x WHERE { {?x <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasProduct> ?prod . ?prod <" + RDFS.LABEL + "> \"" + name + "\" . }" +
					"UNION " +
					"{ ?x <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasReactant> ?reactant. ?reactant <" + RDFS.LABEL + "> \"" + name + "\"  }}";

			System.out.println(queryString);
			TupleQueryResult res = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString).evaluate();

			List<URI> participatingReactions = new ArrayList<URI>();

			// add all these reactions to the set of reactions to be loaded
			while (res.hasNext()) {
				System.out.println("Found reaction: " + name);
				BindingSet temp = res.next();
				if (!participatingReactions.contains((URI) temp.getBinding("x").getValue()))
					participatingReactions.add((URI) temp.getBinding("x").getValue());
			}

			Set<Species> speciesesToLoad = new HashSet<Species>();
			
			Set<String> productsNames = new HashSet<String>();
			
			// for all these reactions, get all other participating species (here: the products)
			for (URI reaction : participatingReactions) {
				
				URL url1 = new URL(wikiUrl + LanguageSpecs.getExportRdf(language) + reaction.getLocalName());
				URLConnection con1 = url1.openConnection();
				
				BufferedReader rd1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
				
				conn.add(rd1, "", RDFFormat.RDFXML);
				
				queryString = "SELECT ?x ?label WHERE { <" + reaction.stringValue() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasProduct> ?x. ?x <" + RDFS.LABEL + "> ?label .} ";
				System.out.println("Reaction query: " + queryString);
				TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
								
				res = query.evaluate();
				
				// for these participating species, get all the information from the wiki
				while (res.hasNext()) {
					BindingSet temp = res.next();
					URI speciesURI = (URI) temp.getBinding("x").getValue();
					Literal label = (Literal) temp.getBinding("label").getValue();
					url1 = new URL(wikiUrl + LanguageSpecs.getExportRdf(language) + speciesURI.getLocalName());
					con1 = url1.openConnection();
					
					rd1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
					
					conn.add(rd1, "", RDFFormat.RDFXML);
					Species s = new Species(label.stringValue().replace(" ", "_"));
					
					String typeQueryString = "SELECT ?x WHERE { <" + speciesURI.toString() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "Type> ?x }";
					TupleQuery typeQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, typeQueryString);
					
					TupleQueryResult typeRes = typeQuery.evaluate();

					while (typeRes.hasNext()) {
						BindingSet types = typeRes.next();
						s.speciesType = ((URI)types.getBinding("x").getValue()).getLocalName();
					}
					
					typeQueryString = "SELECT ?x WHERE { <" + speciesURI.toString() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "ProteinType> ?x }";
					typeQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, typeQueryString);
					
					typeRes = typeQuery.evaluate();
					
					while (typeRes.hasNext()) {
						BindingSet types = typeRes.next();
						s.proteinType = ((URI)types.getBinding("x").getValue()).getLocalName();
					}
					// the marked species itself does not have to be loaded, since it is already part of the model
					
					
					
					if (!s.speciesName.equals(name)) {
						speciesesToLoad.add(s);
						
					}
					productsNames.add(s.speciesName);
				}
				
				// for all these reactions, get all other participating species (here: the reactants)
				queryString = "SELECT ?x ?label WHERE { <" + reaction.stringValue() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasReactant> ?x . ?x <" + RDFS.LABEL + "> ?label . }";
				
				query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				
				res = query.evaluate();
				int aliasInt = 0;
				
				while (res.hasNext()) {
					BindingSet temp = res.next();
					URI speciesURI = (URI) temp.getBinding("x").getValue();
					Literal label = (Literal) temp.getBinding("label").getValue();
					url1 = new URL(wikiUrl + LanguageSpecs.getExportRdf(language) + speciesURI.getLocalName());
					con1 = url1.openConnection();
					
					rd1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
					
					conn.add(rd1, "", RDFFormat.RDFXML);
					Species s = new Species(label.stringValue().replace(" ", "-"));
					
					String typeQueryString = "SELECT ?x WHERE { <" + speciesURI.toString() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "Type> ?x }";
					TupleQuery typeQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, typeQueryString);
					
					TupleQueryResult typeRes = typeQuery.evaluate();
					
					while (typeRes.hasNext()) {
						BindingSet types = typeRes.next();
						s.speciesType = ((URI)types.getBinding("x").getValue()).getLocalName();
					}
					
					typeQueryString = "SELECT ?x WHERE { <" + speciesURI.toString() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "ProteinType> ?x }";
					typeQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, typeQueryString);
					
					typeRes = typeQuery.evaluate();
					
					while (typeRes.hasNext()) {
						BindingSet types = typeRes.next();
						s.proteinType = ((URI)types.getBinding("x").getValue()).getLocalName();
					}
					if (productsNames.contains(s.speciesName) && !s.speciesName.equals(name)) {
						aliasInt = (int) (Math.random() * 10000);
						s.speciesName = s.speciesName + aliasInt;
					}
					if (!s.speciesName.equals(name)){
						speciesesToLoad.add(s);
					}
						
				}
				
				queryString = "SELECT ?y WHERE { <" + reaction.stringValue() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasModification> ?y. }";
		
				query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				
				res = query.evaluate();
				
				while (res.hasNext()) {
		
					String mod = ((URI)res.next().getBinding("y").getValue()).getLocalName();
					url1 = new URL(wikiUrl + LanguageSpecs.getExportRdf(language) + mod);
					con1 = url1.openConnection();
					
					rd1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
					
					System.out.println(conn.size());
					conn.add(rd1, "", RDFFormat.RDFXML);
					System.out.println(conn.size());
				}
				// get the modifiers of the current reaction (i.e. the catalysts etc).
				queryString = "SELECT ?label WHERE { <" + reaction.stringValue() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasModification> ?y." +
						"?y <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "Modifier> ?x . ?x <" + RDFS.LABEL + "> ?label}";
				
				query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
				
				res = query.evaluate();
				
				while (res.hasNext()) {
					BindingSet temp = res.next();
					Literal speciesURI = (Literal) temp.getBinding("label").getValue();
					
					Species s = new Species(speciesURI.stringValue());
					
					url1 = new URL(wikiUrl + LanguageSpecs.getExportRdf(language) + speciesURI.stringValue());
					con1 = url1.openConnection();
					
					rd1 = new BufferedReader(new InputStreamReader(con1.getInputStream()));
					
					System.out.println(conn.size());
					conn.add(rd1, "", RDFFormat.RDFXML);
					System.out.println(conn.size());
					
					// get the type of the modifier
					String typeQueryString = "SELECT ?x WHERE { ?mod <" + RDFS.LABEL + "> \""+ speciesURI.stringValue() +"\" . ?mod <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "Type> ?x }";
					TupleQuery typeQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, typeQueryString);
					
					TupleQueryResult typeRes = typeQuery.evaluate();
					
					while (typeRes.hasNext()) {
						BindingSet types = typeRes.next();
						s.speciesType = ((URI)types.getBinding("x").getValue()).getLocalName();
					}
					
					typeQueryString = "SELECT ?x WHERE { ?mod <" + RDFS.LABEL + "> \""+ speciesURI.stringValue() +"\" . ?mod <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "ProteinType> ?x }";
					typeQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, typeQueryString);
					
					typeRes = typeQuery.evaluate();
					
					while (typeRes.hasNext()) {
						BindingSet types = typeRes.next();
						s.proteinType = ((URI)types.getBinding("x").getValue()).getLocalName();
					}
					// if the modifier is not already in the list of species to load, add it
					if (!s.speciesName.equals(name)) {
						s.speciesName = s.speciesName;
						speciesesToLoad.add(s);
					}
				}
				
				// wrap the information about every species that has to be loaded in a request and send it to the plugin
				for (Species s : speciesesToLoad) {
					
//					BufferedReader r = new BufferedReader(new FileReader(new File("c:/Data/plugintestout.txt")));
					
					
					String st = WikiAccessor.getFullPage(s.speciesName, wikiUrl );
					s.annotation = URLEncoder.encode(st, "UTF-8");
					
					String linkToWiki = wikiUrl + "index.php/" + s.speciesName;
					System.out.println("Loading species: " + s.speciesName);
					out.println("speciesName=" + s.speciesName + ";speciesType=" + s.speciesType + ";proteinType=" + s.proteinType + ";annotation=" + s.annotation);
					out.flush();
				}
				speciesesToLoad = new HashSet<Species>();
				
				/*
				 * get all information about the reaction itself (isFast, isReversible, all the participating species)
				 */
				String reactionQueryString = "SELECT ?type ?fast ?rev ?reactant ?product ?modification WHERE {{<" + reaction.stringValue()+ ">  <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "ReactionType> ?type. " +
						"<" + reaction.stringValue()+ "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "IsFast> ?fast. " +
						"<" + reaction.stringValue()+ "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "IsReversible> ?rev. " +
						"<" + reaction.stringValue()+ "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasReactant> ?r. " +
						"?r <" + RDFS.LABEL + "> ?reactant . " +
						"<" + reaction.stringValue()+ "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasProduct> ?p. " +
						"?p <" + RDFS.LABEL +  "> ?product . } " +
						"OPTIONAL {<" + reaction.stringValue()+ "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "HasModification> ?modification. } " +
						"}";
				
				TupleQuery typeQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, reactionQueryString);
				
				TupleQueryResult reactionRes = typeQuery.evaluate();
				
				Set<String> reactants = new HashSet<String>();
				Set<String> products = new HashSet<String>();
				Set<URI> modifications = new HashSet<URI>();
				String reactionName = null;
				String reactionType = null;
				String isFast = null;
				String isReversible = null;
				
				while (reactionRes.hasNext()) {
					BindingSet reactionFacts = reactionRes.next();
					reactionName = reaction.getLocalName();
					reactionType = ((URI)reactionFacts.getBinding("type").getValue()).getLocalName();
					isFast = ((URI)reactionFacts.getBinding("fast").getValue()).getLocalName().toLowerCase();
					isReversible = ((URI)reactionFacts.getBinding("rev").getValue()).getLocalName().toLowerCase();
					reactants.add(((Literal)reactionFacts.getBinding("reactant").getValue()).stringValue().replace(" ", "_"));
					products.add(((Literal)reactionFacts.getBinding("product").getValue()).stringValue().replace(" ", "-"));
					try {
						modifications.add(((URI)reactionFacts.getBinding("modification").getValue()));
					} catch (NullPointerException npe) {
						
					}
				}
				
				String productList = "";
				for (String s : products) {
					productList += "hasProduct=" +s.replace("-2D", "-") + ";";
				}
				
				String reactantList = "";
				for (String s : reactants) {
					if (productsNames.contains(s) && !s.equals(name)) {
						reactantList += "hasReactant=" + s.replace("-2D", "-") + aliasInt + ";";
					}
					else {
						
						reactantList += "hasReactant=" + s + ";";
					}
				}
				
				
				String modifierList = "";
				for (URI uri : modifications) {
					
					// get all the modifications for the reaction
					String modQueryString = "SELECT ?modLabel ?mod ?type WHERE { <" + uri.stringValue() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "Modifier> ?mod." +
							"<" + uri.stringValue() + "> <" + namespace + LanguageSpecs.getUriResolverAttribute(language, resolve) + "ModificationType> ?type . ?mod <" + RDFS.LABEL + "> ?modLabel }";
					TupleQuery modQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, modQueryString);
					
					TupleQueryResult modRes = modQuery.evaluate();
					
					while (modRes.hasNext()) {
						BindingSet modFacts = modRes.next();
						String modType = ((URI)modFacts.getBinding("type").getValue()).getLocalName();
						String modName = ((Literal)modFacts.getBinding("modLabel").getValue()).stringValue();
						modifierList += "hasModification=" + modName + "__" + modType + ";";
					}
					
				}
				
				String annotation = URLEncoder.encode(WikiAccessor.getFullPage(reactionName, wikiUrl), "UTF-8");
				
				// send a request containing all information necessary for adding the reaction to the plugin (it is already made
				// sure, that all participating species are already in the model, since these requests have been sent before)
				System.out.println("Loading reaction: " + reactionName);
				out.println("reactionName=" + reactionName + ";reactionType=" + reactionType +";isFast=" + isFast + ";annotation=" + annotation + ";isReversible=" + isReversible + ";" + reactantList + productList + modifierList + ";");
				out.flush();
			}
			conn.close();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} catch (RDFParseException e) {
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			e.printStackTrace();
		} catch (MalformedQueryException e) {
			e.printStackTrace();
		}

	}

}
