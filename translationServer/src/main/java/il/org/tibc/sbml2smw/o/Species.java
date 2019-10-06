package il.org.tibc.sbml2smw.o;

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
 * since specieses can become a little more complex then reactions, and we have to work with several ones of them, information
 * about them is stored in an own object
 * 
 * @author Tobias
 * 
 */
public class Species {
	
	public Species(String speciesName) {
		this.speciesName = speciesName;
	}
	
	public String speciesName;
	
	public String speciesType;
	
	public String proteinType;
	
	public String annotation;

}

