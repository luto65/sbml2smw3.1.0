package il.org.tibc.sbml2smw.L;

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


/**
 * the LanguageSpec contains information necessary for internationalization. If other languages than english and german should be
 * supported, the local translations of the three SMW-links have to be associated with the corresponding language code
 * 
 * @author Tobias
 * 
 */
public class LanguageSpecs {
	
	public static String getExportRdf(String language) {
		
		if (language.equals("de")) {
			return "index.php/Spezial:Exportiere_RDF/";
		}
		else if (language.equals("en")) {
			return "index.php/Special:ExportRDF/";
		}
		
		return null;
	}
	
	public static String getUriResolver(String language, boolean resolve) {
		
		if (resolve) {
			System.out.println("URI-resolving activated!");
			if (language.equals("de")) {
				return "/index.php/Spezial:URIResolver/";
			} else if (language.equals("en")) {
				return "/index.php/Special:URIResolver/";
			} else {
				System.out.println("No valid wiki language set, please set to en or de");
				return "noLanguageFound";
			}
		}
		else {
			return "/";
		}
	}
	
	public static String getUriResolverAttribute(String language, boolean resolve) {

		if (resolve)
			if (language.equals("de")) {
				return "/index.php/Spezial:URIResolver/Attribut-3A";
			} else if (language.equals("en")) {
				return "/index.php/Special:URIResolver/Property-3A";
			}

		return "/Property-3A";
	}
	
}
