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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.SailException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * This servlet writes the information about a reaction to the wiki
 * 
 * @author Tobias
 *
 */
public class StoreReactionServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8298757541094356262L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		
		MemoryStore ms = new MemoryStore();
		SailRepository repo = new SailRepository(ms);
		try {
			ms.initialize();
		} catch (SailException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String defaultNS = "http://www.test.de/";
		Map<?, ?> map = req.getParameterMap();

		try {

			ValueFactory f = ValueFactoryImpl.getInstance();

			RepositoryConnection con = repo.getConnection();
			
			// extract the information to store from the request
			String wikiUrl = ((String[]) map.get("wiki"))[0];
			String user = ((String[]) map.get("user"))[0];
			String password = ((String[]) map.get("pass"))[0];
			
			String type = ((String[]) map.get("type"))[0];
			URI typeUri = f.createURI(defaultNS + type);
			String language = ((String[]) map.get("language"))[0];

			String sub = ((String[]) map.get("name"))[0];
			if (sub == null || sub.equals("")) {
				sub = "defaultReaction" + System.currentTimeMillis();
			}
			URI subject = f.createURI( defaultNS + sub);

			String reacType = ((String[]) map.get("reactionType"))[0];
			URI reactionType = f.createURI(defaultNS + reacType);
			
			String isFast = ((String[]) map.get("isFast"))[0];
			Literal isFastLit = f.createLiteral(isFast);
			
			String isReversible = ((String[]) map.get("isReversible"))[0];
			Literal isReversibleLit = f.createLiteral(isFast);
			
			String annotation = ((String[]) map.get("annotation"))[0];
			Literal annotationLit = f.createLiteral(annotation);

			Statement stmt = f.createStatement(subject, RDF.TYPE, typeUri);
			con.add(stmt);
			
			stmt = f.createStatement(subject, f.createURI(defaultNS + "reactionType"), reactionType);
			con.add(stmt);
			
			stmt = f.createStatement(subject, f.createURI(defaultNS + "isFast"), isFastLit);
			con.add(stmt);
			
			stmt = f.createStatement(subject, f.createURI(defaultNS + "isReversible"), isReversibleLit);
			con.add(stmt);

			String[] reactants = (String[]) map.get("hasReactant");
			URI hasReactantUri = f.createURI(defaultNS + "hasReactant");

			String[] products = (String[]) map.get("hasProduct");
			URI hasProductUri = f.createURI(defaultNS + "hasProduct");
			
			String [] modifications = (String[]) map.get("hasModification");
			
			for (String reactant : reactants) {
				URI reactantUri = f.createURI(defaultNS + reactant);
				stmt = f.createStatement(subject, hasReactantUri, reactantUri);
				con.add(stmt);
			}

			for (String product : products) {
				URI productUri = f.createURI(defaultNS + product);
				stmt = f.createStatement(subject, hasProductUri, productUri);
				con.add(stmt);
			}
			

			con.commit();
			
			String wikiContent = "";
			
			// translate the extracted information into wiki syntax
			wikiContent += "reaction name: [[reactionName::" + sub + "]]\n\n" + "reaction Type: [[reactionType::"
					+ reacType + "]]\n\n" + "is fast: [[isFast::" + isFast + "]]\n\n" + "is reversible: [[isReversible::" + isReversible + "]]\n\n";
			
			for (String product : products) {
//				if (product.contains("-modified"))
//					product = product.replace("-modified", "_modified");
				wikiContent += "product: [[hasProduct::" + product + "]]\n\n";
			}

			for (String reactant : reactants) {
//				if (reactant.contains("-modified"))
//					reactant = reactant.replace("-modified", "_modified");
				wikiContent += "reactant: [[hasReactant::" + reactant + "]]\n\n";
			}
			
			if (modifications != null)

				for (String modification : modifications) {
					wikiContent += "modification: [[hasModification::" + modification + "]]\n\n";

					String[] modSplit = modification.split("__");

					URL url;
					try {
						url = new URL("http://" + req.getServerName() + ":" + req.getLocalPort() +  "/store/modification/?name=" + modification + "&modifier="
								+ modSplit[0] + "&modificationType=" + modSplit[1] + "&wiki=" + wikiUrl + "&user=" + user + "&pass=" + password + "&language=" + language);
						URLConnection conn = url.openConnection();

						// Get the response
						BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
						String line;
						while ((line = rd.readLine()) != null) {
							// Process line...
							System.out.println(line);
						}

					} catch (MalformedURLException e2) {
						e2.printStackTrace();
					} catch (IOException e3) {
						e3.printStackTrace();
					}

				}
			
			wikiContent += "MIRIAM-ANNOTATION: \n";
			wikiContent += annotation;
			
			System.out.println("Storing reaction: " + sub);
			WikiAccessor.writeToWikiUnstructuredPreserved(sub, user, password, wikiContent, "", "text", wikiUrl, language);
			
			con.close();

		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}

}
