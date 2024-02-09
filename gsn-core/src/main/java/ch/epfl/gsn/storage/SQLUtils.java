/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* 
* This file is part of GSN.
* 
* GSN is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* GSN is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with GSN.  If not, see <http://www.gnu.org/licenses/>.
* 
* File: src/ch/epfl/gsn/storage/SQLUtils.java
*
* @author gsn_devs
* @author Timotee Maret
* @author Ali Salehi
*
*/

package ch.epfl.gsn.storage;

import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.epfl.gsn.utils.CaseInsensitiveComparator;

public class SQLUtils {

	private static Pattern pattern = Pattern.compile("(\"[^\"]*\")|((\\w+)(\\.((\\w+)|\\*)))",
			Pattern.CASE_INSENSITIVE);

	/**
	 * Table renaming, note that the renameMapping should be a tree map. This
	 * method gets a sql query and changes the table names using the mappings
	 * provided in the second argument.<br>
	 * 
	 * @param query
	 * @param renameMapping
	 * @return
	 */
	public static StringBuilder newRewrite(CharSequence query, TreeMap<CharSequence, CharSequence> renameMapping) {
		// Selecting strings between pair of "" : (\"[^\"]*\")
		// Selecting tableID.tableName or tableID.* : (\\w+(\\.(\w+)|\\*))
		// The combined pattern is : (\"[^\"]*\")|(\\w+\\.((\\w+)|\\*))
		Pattern pattern = Pattern.compile("(\"[^\"]*\")|((\\w+)(\\.((\\w+)|\\*)))", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(query);
		StringBuffer result = new StringBuffer();
		if (!(renameMapping.comparator() instanceof CaseInsensitiveComparator)) {
			throw new RuntimeException("Query rename needs case insensitive treemap.");
		}
		while (matcher.find()) {
			if (matcher.group(2) == null) {
				continue;
			}
			String tableName = matcher.group(3);
			CharSequence replacement = renameMapping.get(tableName);
			// $4 means that the 4th group of the match should be appended to the
			// string (the forth group contains the field name).
			if (replacement != null) {
				matcher.appendReplacement(result, new StringBuilder(replacement).append("$4").toString());
			}
		}
		String toReturn = matcher.appendTail(result).toString().toLowerCase();

		// TODO " from " has to use regular expressions because now from is separated
		// through space which is not always the case, for instance if the user uses
		// \t(tab) for separating "from" from the rest of the query, then we get
		// exception. The same issue with other sql keywords in this method.

		int indexOfFrom = toReturn.indexOf(" from ") >= 0 ? toReturn.indexOf(" from ") + " from ".length() : 0;
		int indexOfWhere = (toReturn.lastIndexOf(" where ") > 0 ? (toReturn.lastIndexOf(" where "))
				: toReturn.length());
		String selection = toReturn.substring(indexOfFrom, indexOfWhere);
		Pattern fromClausePattern = Pattern.compile("\\s*(\\w+)\\s*", Pattern.CASE_INSENSITIVE);
		Matcher fromClauseMather = fromClausePattern.matcher(selection);
		result = new StringBuffer();
		while (fromClauseMather.find()) {
			if (fromClauseMather.group(1) == null) {
				continue;
			}
			String tableName = fromClauseMather.group(1);
			CharSequence replacement = renameMapping.get(tableName);
			if (replacement != null) {
				fromClauseMather.appendReplacement(result, replacement.toString() + " ");
			}

		}
		String cleanFromClause = fromClauseMather.appendTail(result).toString();
		// String finalResult = StringUtils.replace( toReturn , selection ,
		// cleanFromClause );
		StringBuilder finalResult = new StringBuilder(toReturn.substring(0, indexOfFrom)).append(cleanFromClause)
				.append(toReturn.substring(indexOfWhere));
		return finalResult;
	}

	/**
	 * Returns the table name from the given SQL query.
	 *
	 * @param query the SQL query
	 * @return the table name extracted from the query, or null if not found
	 */
	public static String getTableName(String query) {
		String q = SQLValidator.removeSingleQuotes(SQLValidator.removeQuotes(query)).toLowerCase();
		StringTokenizer tokens = new StringTokenizer(q, " ");
		while (tokens.hasMoreElements()) {
			if (tokens.nextToken().equalsIgnoreCase("from") && tokens.hasMoreTokens()) {
				return tokens.nextToken();
			}

		}

		return null;
	}

	/**
	 * Rewrites a SQL query by replacing occurrences of a specific table name with a
	 * new name.
	 *
	 * This static method takes a SQL query, a table name to be replaced, and a
	 * replacement table name. It uses a regular
	 * expression pattern to find occurrences of table names in the query and
	 * replaces them with the new table name. The
	 * replacement is performed within the FROM clause of the query, maintaining the
	 * case sensitivity of the original query.
	 * The modified query is then returned as a StringBuilder.
	 *
	 * @param query             The original SQL query to be rewritten.
	 * @param tableNameToRename The table name to be replaced in the query.
	 * @param replaceTo         The new table name to replace the occurrences of the
	 *                          original table name.
	 * @return A StringBuilder containing the modified SQL query with the specified
	 *         table name replaced.
	 */
	public static StringBuilder newRewrite(CharSequence query, CharSequence tableNameToRename, CharSequence replaceTo) {
		// Selecting strings between pair of "" : (\"[^\"]*\")
		// Selecting tableID.tableName or tableID.* : (\\w+(\\.(\w+)|\\*))
		// The combined pattern is : (\"[^\"]*\")|(\\w+\\.((\\w+)|\\*))
		Matcher matcher = pattern.matcher(query);
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			if (matcher.group(2) == null) {
				continue;
			}
			String tableName = matcher.group(3);
			if (tableName.equals(tableNameToRename) && replaceTo != null) {
				// $4 means that the 4th group of the match should be appended to the
				// string (the forth group contains the field name).
				matcher.appendReplacement(result, new StringBuilder(replaceTo).append("$4").toString());
			}
		}
		String toReturn = matcher.appendTail(result).toString().toLowerCase();
		int indexOfFrom = toReturn.indexOf(" from ") >= 0 ? toReturn.indexOf(" from ") + " from ".length() : 0;
		int indexOfWhere = (toReturn.lastIndexOf(" where ") > 0 ? (toReturn.lastIndexOf(" where "))
				: toReturn.length());
		String selection = toReturn.substring(indexOfFrom, indexOfWhere);
		Pattern fromClausePattern = Pattern.compile("\\s*(\\w+)\\s*", Pattern.CASE_INSENSITIVE);
		Matcher fromClauseMather = fromClausePattern.matcher(selection);
		result = new StringBuffer();
		while (fromClauseMather.find()) {
			if (fromClauseMather.group(1) == null) {
				continue;
			}
			String tableName = fromClauseMather.group(1);
			if (tableName.equals(tableNameToRename) && replaceTo != null) {
				fromClauseMather.appendReplacement(result, replaceTo.toString() + " ");
			}
		}
		String cleanFromClause = fromClauseMather.appendTail(result).toString();
		// String finalResult = StringUtils.replace( toReturn , selection ,
		// cleanFromClause );
		StringBuilder finalResult = new StringBuilder(toReturn.substring(0, indexOfFrom)).append(cleanFromClause)
				.append(toReturn.substring(indexOfWhere));
		return finalResult;
	}

	/**
	 * Extracts the projection from a SQL query.
	 *
	 * @param pQuery the SQL query from which to extract the projection
	 * @return the projection part of the SQL query
	 */
	public static String extractProjection(String pQuery) {
		String query = pQuery.trim().toLowerCase();
		int indexOfFrom = query.indexOf(" from ");
		int indexOfSelect = query.indexOf("select");
		return pQuery.substring(indexOfSelect + "select".length(), indexOfFrom);
	}

	/**
	 * Extracts the WHERE clause from a SQL query.
	 *
	 * @param pQuery the SQL query from which to extract the WHERE clause
	 * @return the extracted WHERE clause as a String
	 */
	public static String extractWhereClause(String pQuery) {
		int indexOfWhere = pQuery.toLowerCase().indexOf(" where ");
		if (indexOfWhere < 0) {
			return " true ";
		}
		return pQuery.substring(indexOfWhere + " where".length(), pQuery.length());
	}

	public static void main(String[] args) {
		TreeMap<CharSequence, CharSequence> map = new TreeMap<CharSequence, CharSequence>(
				new CaseInsensitiveComparator());
		String query = "seLect ali.fd, x.x, fdfd.fdfd, *.r, * from x,x, bla, x whEre k";
		map.put("x", "done");
		CharSequence out = newRewrite(query, map);
		System.out.println(out.toString());
		System.out.println(extractProjection(query));
		out = newRewrite(extractProjection(query), map);
		System.out.println(out.toString());
	}

	/**
	 * Returns the index of the last occurrence of the substring " where " in the
	 * given CharSequence.
	 *
	 * @param c the CharSequence to search in
	 * @return the index of the last occurrence of " where ", or -1 if not found
	 */
	public static int getWhereIndex(CharSequence c) {
		return c.toString().toLowerCase().lastIndexOf(" where ");
	}

	/**
	 * Returns the index of the last occurrence of the "order by" substring in the
	 * given CharSequence.
	 *
	 * @param c the CharSequence to search in
	 * @return the index of the last occurrence of "order by", or -1 if not found
	 */
	public static int getOrderByIndex(CharSequence c) {
		return c.toString().toLowerCase().lastIndexOf(" order by ");
	}

	/**
	 * Returns the index of the last occurrence of the "GROUP BY" clause in the
	 * given CharSequence.
	 *
	 * @param c the CharSequence to search in
	 * @return the index of the last occurrence of "GROUP BY", or -1 if not found
	 */
	public static int getGroupByIndex(CharSequence c) {
		return c.toString().toLowerCase().lastIndexOf(" group by ");
	}
}
