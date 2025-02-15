/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* Copyright (c) 2020-2023, University of Innsbruck
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
* File: src/ch/epfl/gsn/utils/protocols/AbstractHCIQueryWithoutAnswer.java
*
* @author Jerome Rousselot
* @author Ali Salehi
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.utils.protocols;

import java.util.Vector;

/**
 * This class provides an empty implementation of the methods
 * getWaitTime, needsAnswer and getAnswers to make it
 * easier to implement queries that don't require an answer.
 */
public abstract class AbstractHCIQueryWithoutAnswer extends AbstractHCIQuery {

   /**
    * Represents an abstract HCI query without an answer.
    * This class extends the base HCIQuery class and provides a constructor to
    * initialize the query.
    *
    * @param Name               the name of the query
    * @param queryDescription   the description of the query
    * @param paramsDescriptions an array of descriptions for the query parameters
    */
   public AbstractHCIQueryWithoutAnswer(String Name, String queryDescription, String[] paramsDescriptions) {
      super(Name, queryDescription, paramsDescriptions);
   }

   // we usually dont expect an answer
   public int getWaitTime(Vector<Object> params) {
      // TODO Auto-generated method stub
      return NO_WAIT_TIME;
   }

   /*
    * By default we dont expect an answer.
    */
   public boolean needsAnswer(Vector<Object> params) {
      return false;
   }

   /*
    * No answer by default so this is a placeholder method.
    */
   public Object[] getAnswers(byte[] rawAnswer) {
      return null;
   }
}
