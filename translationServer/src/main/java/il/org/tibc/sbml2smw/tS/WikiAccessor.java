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
import il.org.tibc.sbml2smw.L.LanguageSpecs;

import net.sourceforge.jwbf.core.actions.util.ActionException;
import net.sourceforge.jwbf.core.actions.util.ProcessException;
import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

/**
 * The WikiAccessor is the component encapsulating all direct wiki accesses. It takes care of authentification and read/write
 * accesses to the actual SMW-instance
 * 
 * @author Tobias
 * 
 */
public class WikiAccessor {

	public static String getWikiPage(String page, String wikiUrl, String language) throws IOException {

		URL url = new URL(wikiUrl + LanguageSpecs.getExportRdf(language) + page);
		URLConnection con = url.openConnection();
		con.setDoOutput(true);
		StringBuilder sb = new StringBuilder();

		BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;

		while ((line = rd.readLine()) != null) {
			sb.append(line + "\n");
		}

		String s = sb.toString();

		if (!s.contains("<text") || !s.contains("</text>")) {
			return "";
		}

		String pageContent = s.substring(s.indexOf("<text"), s.indexOf("</text>"));
		return pageContent;

	}

	public static String getStructuredData(String page, String wikiUrl, String language) throws IOException {

		String pageContent = getWikiPage(page, wikiUrl, language);

		pageContent = pageContent.substring(pageContent.indexOf("&lt;div&gt;") + 11, pageContent.indexOf("&lt;/div&gt;"));
		System.out.println(pageContent);

		return pageContent;
	}

	public static String getUnstructuredData(String page, String wikiUrl, String language) throws IOException {
		return getUnstructuredDataBefore(page, wikiUrl, language) + "   " + getUnstructuredDataAfter(page, wikiUrl, language);
	}

	public static String getUnstructuredDataBefore(String page, String wikiUrl, String language) throws IOException {

		String pageContent = getWikiPage(page, wikiUrl, language);

		if (!pageContent.contains("&lt;div&gt;") || !pageContent.contains("&lt;/div&gt;")) {
			return "";
		}

		String beforeStructured = pageContent.substring(pageContent.indexOf(">") + 1, pageContent.indexOf("&lt;div&gt;"));
		return beforeStructured;
	}

	public static String getUnstructuredDataAfter(String page, String wikiUrl, String language) throws IOException {

		String pageContent = getWikiPage(page, wikiUrl, language);

		if (!pageContent.contains("&lt;div&gt;") || !pageContent.contains("&lt;/div&gt;")) {
			return "";
		}

		String afterStructured = pageContent.substring(pageContent.indexOf("&lt;/div&gt;") + 12);
		return afterStructured;
	}

	public static void writeToWikiUnstructuredPreserved(String page, String user, String passwd, String content,
			String annotation, String append, String wikiUrl, String language) throws IOException {

		String unstructuredDataBefore = getUnstructuredDataBefore(page, wikiUrl, language);
		String unstructuredDataAfter = getUnstructuredDataAfter(page, wikiUrl, language);

		writeToWiki(page, user, passwd, unstructuredDataBefore + "<div>\n\n" + content + "</div>" + "\n\n"
				+ unstructuredDataAfter, append, wikiUrl);

	}

	public static void writeToWiki(String page, String user, String passwd, String content, String append, String wikiUrl) {

		MediaWikiBot b;
		// try {
		try {
			b = new MediaWikiBot(wikiUrl);

			b.login(user, passwd);

			Article a = new Article(b, page);

			a.addText(content);
			a.save();

		} catch (ActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProcessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// } catch (Exception e) {
		// System.out.println("Login failed, please check credentials!");
		// }

	}

	public static String getFullPage(String subject, String wikiUrl) {

		try {
			
			MediaWikiBot b = new MediaWikiBot(wikiUrl);
			
			Article a = new Article(b, subject);

            String cont = a.getText();
            String x = "";
            try {
            	x = cont.substring(cont.indexOf("MIRIAM") + 20, cont.indexOf("/rdf:RDF") + 9);
            }
            catch (Exception e) {
            	return "";
            }
			
            System.out.println(x);
			
			return x;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
