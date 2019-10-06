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

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet does the saving of the information for a reaction-modification to the wiki
 * 
 * @author Tobias
 *
 */
public class StoreModificationServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5748124521075355162L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		Map<?, ?> map = req.getParameterMap();
		String sub = ((String[]) map.get("name"))[0];
		String type = ((String[]) map.get("modificationType"))[0];
		String modifier = ((String[]) map.get("modifier"))[0];
		String wikiUrl =  ((String[]) map.get("wiki"))[0];
		String user = ((String[]) map.get("user"))[0];
		String password = ((String[]) map.get("pass"))[0];
		String language= ((String[]) map.get("language"))[0];
		
		
		String wikiContent = "Name: [[modificationName::" + sub + "]]\n\nmodifier: [[modifier::" + modifier + "]]\n\ntype: [[modificationType::" + type + "]]" ;
		
		System.out.println("Storing modification: " + sub);
		WikiAccessor.writeToWikiUnstructuredPreserved(sub, user, password, wikiContent, "", "text", wikiUrl, language);
		
	}
	
	

}
