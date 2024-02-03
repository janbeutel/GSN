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
* File: src/ch/epfl/gsn/utils/LazyTimedHashMap.java
*
* @author Ali Salehi
*
*/

package ch.epfl.gsn.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LazyTimedHashMap {

   private int lifeTimeOfEachElement;

   private Map<Object, Long> keyToTimeMapping = new ConcurrentHashMap();

   private Map<Object, Object> keyToValueMapping = new ConcurrentHashMap();

   private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();

   /**
    * This class is thread-safe .
    * 
    * @param lifeTimeOfEachElementInMilliSeconds This value is a positive
    *                                            integer representing the maximum
    *                                            time an element is valid in the
    *                                            hashmap.
    *                                            The value is in milliseconds.
    */
   public LazyTimedHashMap(int lifeTimeOfEachElementInMilliSeconds) {
      this.lifeTimeOfEachElement = lifeTimeOfEachElementInMilliSeconds;
   }

   /**
    * Associates the specified value with the specified key in this map.
    * If the map previously contained a mapping for the key, the old value is
    * replaced.
    * If the map does not contain a mapping for the key, a new mapping is created.
    * Additionally, a change event is fired if a new mapping is created.
    *
    * @param key   the key with which the specified value is to be associated
    * @param value the value to be associated with the specified key
    */
   public void put(Object key, Object value) {
      keyToTimeMapping.put(key, System.currentTimeMillis());
      if (!keyToValueMapping.containsKey(key)) {
         keyToValueMapping.put(key, value);
         fireChange(ITEM_ADDED, key, value);
      }

   }

   /**
    * If the element doesn't Exist returns null.
    * 
    * @param key
    * @return The element or Null if it doesn't exist or outdated.
    */

   public Object get(Object key) {
      Long insertionTime = keyToTimeMapping.get(key);
      if (insertionTime == null) {
         return null;
      }
      if (System.currentTimeMillis() - insertionTime > lifeTimeOfEachElement) {
         remove(key);
         return null;
      }
      return keyToValueMapping.get(key);
   }

   /**
    * Removes the mapping for a key from this map if it is present.
    * 
    * @param key the key whose mapping is to be removed from the map
    * @return the previous value associated with the key, or null if there was no
    *         mapping for the key
    */
   public Object remove(Object key) {
      keyToTimeMapping.remove(key);
      Object value = keyToValueMapping.remove(key);
      fireChange(ITEM_REMOVED, key, value);
      return value;
   }

   /**
    * Returns an ArrayList containing all the keys in the LazyTimedHashMap.
    *
    * @return an ArrayList of keys
    */
   public ArrayList getKeys() {
      ArrayList arrayList = new ArrayList();
      Iterator it = keyToValueMapping.keySet().iterator();
      while (it.hasNext()) {
         Object key = it.next();
         Object value = keyToValueMapping.get(key);
         if (value != null) {
            arrayList.add(key);
         }
      }
      return arrayList;
   }

   /**
    * Returns an ArrayList containing all the values in the LazyTimedHashMap.
    *
    * @return an ArrayList containing all the values in the LazyTimedHashMap
    */
   public ArrayList getValues() {
      ArrayList arrayList = new ArrayList();
      Iterator it = keyToValueMapping.keySet().iterator();
      while (it.hasNext()) {
         Object key = it.next();
         Object value = keyToValueMapping.get(key);
         if (value != null) {
            arrayList.add(value);
         }
      }
      return arrayList;
   }

   /**
    * Adds a change listener to the LazyTimedHashMap.
    * 
    * @param cl the change listener to be added
    */
   public void addChangeListener(ChangeListener cl) {
      changeListeners.add(cl);
   }

   /**
    * Removes the specified ChangeListener from the list of registered listeners.
    *
    * @param cl the ChangeListener to be removed
    */
   public void removeChangeListener(ChangeListener cl) {
      changeListeners.remove(cl);
   }

   /**
    * Notifies all registered change listeners about a change that occurred in the
    * map.
    * 
    * @param changeAction the action that describes the change
    * @param changedKey   the key that was changed
    * @param changedValue the new value associated with the changed key
    */
   public void fireChange(String changeAction, Object changedKey, Object changedValue) {
      for (ChangeListener cl : changeListeners) {
         cl.changeHappended(changeAction, changedKey, changedValue);
      }
   }

   public static final String ITEM_REMOVED = "REMOVED";

   public static final String ITEM_ADDED = "ADDED";

   /**
    * Updates the values of all keys in the LazyTimedHashMap.
    * This method iterates over all keys in the keyToValueMapping and calls the
    * get() method for each key.
    * This ensures that the values are up-to-date and lazily computed if necessary.
    */
   public void update() {
      Iterator it = keyToValueMapping.keySet().iterator();
      while (it.hasNext()) {
         get(it.next());
      }
   }
}
