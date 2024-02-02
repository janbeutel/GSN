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
* File: src/ch/epfl/gsn/utils/graph/Node.java
*
* @author Mehdi Riahi
* @author Timotee Maret
*
*/

package ch.epfl.gsn.utils.graph;

import java.io.Serializable;
import java.util.ArrayList;

public class Node<T> implements Serializable {

	private static final long serialVersionUID = -62301155506174334L;

	private ArrayList<Edge<T>> inputEdges;

	private ArrayList<Edge<T>> outputEdges;

	private T object;

	private boolean root;

	private boolean visited;

	private boolean removed;

	public Node() {
		this(null);
	}

	/**
	 * Constructs a new `Node` with the specified object of type T.
	 *
	 * @param object The object to be stored in the node.
	 */
	public Node(T object) {
		inputEdges = new ArrayList<Edge<T>>();
		outputEdges = new ArrayList<Edge<T>>();
		root = false;
		visited = false;
		removed = false;
		this.object = object;
	}

	/**
	 * Adds an edge connecting this node to the specified node.
	 *
	 * @param node The target node to which the edge connects.
	 * @return The created edge.
	 * @throws EdgeExistsException If an edge to the specified node already exists.
	 */
	public Edge<T> addEdge(Node<T> node) throws EdgeExistsException {
		if (edgeExists(node)) {
			throw new EdgeExistsException();
		}
		Edge<T> edge = new Edge<T>(this, node);
		outputEdges.add(edge);
		node.getInputEdges().add(edge);
		return edge;
	}

	/**
	 * Removes the edge connecting the current node to the specified node.
	 *
	 * @param node The target node to which the edge connects.
	 * @return `true` if the edge was successfully removed, `false` otherwise.
	 */
	public boolean removeEdge(Node<T> node) {
		boolean removed = false;
		Edge<T> edge = getEdge(node);
		if (edge != null) {
			outputEdges.remove(edge);
			edge.getEndNode().getInputEdges().remove(edge);
			removed = true;
		}
		return removed;
	}

	/**
	 * Checks if an edge exists between the current node object and the specified
	 * node.
	 * 
	 * @param node the node to check for an edge connection
	 * @return true if an edge exists, false otherwise
	 */
	private boolean edgeExists(Node<T> node) {
		for (Edge<T> edge : outputEdges) {
			if (edge.getEndNode().equals(node)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the edge connecting the current node object to the specified node.
	 *
	 * @param node the node to which the edge is connected
	 * @return the edge connecting this node to the specified node, or null if no
	 *         such edge exists
	 */
	private Edge<T> getEdge(Node<T> node) {
		for (Edge<T> edge : outputEdges) {
			if (edge.getEndNode().equals(node)) {
				return edge;
			}
		}
		return null;
	}

	public ArrayList<Edge<T>> getInputEdges() {
		return inputEdges;
	}

	public void setInputEdges(ArrayList<Edge<T>> inputEdges) {
		this.inputEdges = inputEdges;
	}

	public ArrayList<Edge<T>> getOutputEdges() {
		return outputEdges;
	}

	public void setOutputEdges(ArrayList<Edge<T>> outputEdges) {
		this.outputEdges = outputEdges;
	}

	public T getObject() {
		return object;
	}

	public void setObject(T object) {
		this.object = object;
	}

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}

	/**
	 * Compares this Node object with the specified object for equality.
	 * Returns true if the specified object is also a Node and has the same object
	 * reference,
	 * or if the specified object is a Node and its object reference is equal to
	 * this Node's object reference.
	 * Otherwise, returns false.
	 *
	 * @param obj the object to compare with
	 * @return true if the objects are equal, false otherwise
	 */
	public boolean equals(Object obj) {
		if (this.object == obj) {
			return true;
		}
		if (obj instanceof Node && this.object != null) {
			Node node = (Node) obj;
			return this.object.equals(node.getObject());
		}
		return false;
	}

	/**
	 * Returns a string representation of the object.
	 * 
	 * @return a string representation of the object
	 */
	public String toString() {
		return new StringBuilder("Node[").append(object != null ? object.toString() : null).append("]").toString();
	}

	public boolean isRemoved() {
		return removed;
	}

	public void setRemoved(boolean removed) {
		this.removed = removed;
	}

}
