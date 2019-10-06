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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Statement;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;

/**
 * This servlet receives the information about a species, translates it into SMW syntax and writes it to the wiki
 * 
 * @author Tobias
 *
 */
public class StoreSpeciesServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5917348660476883947L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		Map<?, ?> map = req.getParameterMap();

		String sub = ((String[]) map.get("name"))[0];

		String typ = ((String[]) map.get("type"))[0];

		String hyp = ((String[]) map.get("hyp"))[0];
		
		String wikiUrl = ((String[]) map.get("wiki"))[0];
		String user = ((String[]) map.get("user"))[0];
		String password = ((String[]) map.get("pass"))[0];
		String language = ((String[]) map.get("language"))[0];
		
		String annot = ((String[]) map.get("annotation"))[0];
		System.out.println(annot);
		
		
		
		String[] modifications = (String[]) map.get("modification");
		
		String annotation = ((String[]) map.get("annotation"))[0];
		
		String wikiContent = "Species name: [[speciesName::" + sub + "]]\n\n" + "Species Type: [[type::"
		+ typ + "]]\n\n" + "is hypothetical: [[isHypothetical::" + hyp + "]]\n\n";
		
		String[] protType = ((String[]) map.get("protType"));
		if (protType != null) {
			String protTypeSingle = protType[0];
			wikiContent += "Protein type: [[proteinType::" + protTypeSingle + "]]\n\n";
		}
		
		if (modifications != null && !sub.contains("-modified-") && !sub.contains("_modified_")) {
			String modWikiContent = "";
			String modSub = sub + "-modified";
			for (String mod : modifications) {
				modSub += "-" + mod;
				modWikiContent += "Species Type: [[type::" + typ + "]]\n\n";
				modWikiContent += "Protein type: [[proteinType::" + protType[0] + "]]\n\n";
				modWikiContent += "Modification State: [[modState::" + mod + "]]\n\n";
				modWikiContent += "MIRIAM-ANNOTATION: \n";
				modWikiContent += annot;
			}
			wikiContent += "hasModification: [[hasModification::" + modSub + "]]\n\n";
			
			
			WikiAccessor.writeToWikiUnstructuredPreserved(modSub, user, password, modWikiContent, "", "text", wikiUrl, language);
			
		} else {
			wikiContent += "MIRIAM-ANNOTATION: \n";
			wikiContent += annot;
		}
		System.out.println("Storing species: " + sub);
		WikiAccessor.writeToWikiUnstructuredPreserved(sub, user, password, wikiContent, "", "text", wikiUrl, language);

	}

}
