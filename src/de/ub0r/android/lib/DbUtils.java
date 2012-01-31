/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of ub0rlib.
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; If
 * not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.lib;

/**
 * @author flx
 */
public final class DbUtils {

	/**
	 * Default Constructor.
	 */
	private DbUtils() {

	}

	/**
	 * SQL AND for where clause.
	 * 
	 * @param arg0
	 *            arg0
	 * @param arg1
	 *            arg1
	 * @return ( arg0 ) AND ( arg1 )
	 */
	public static String sqlAnd(final String arg0, final String arg1) {
		if (arg0 != null && arg1 != null) {
			return "( " + arg0 + " ) AND ( " + arg1 + " )";
		} else if (arg0 != null) {
			return arg0;
		} else if (arg1 != null) {
			return arg1;
		} else {
			return null;
		}
	}
}
