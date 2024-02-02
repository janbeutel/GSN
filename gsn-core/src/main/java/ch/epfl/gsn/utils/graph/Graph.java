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
* File: src/ch/epfl/gsn/utils/graph/Graph.java
*
* @author Mehdi Riahi
* @author gsn_devs
*
*/

package ch.epfl.gsn.utils.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Graph<T> implements Serializable {

	private static final long serialVersionUID = 9015284213829329797L;

	private ArrayList<Node<T>> nodes;

	private ArrayList<Node<T>> rootNodes;

	/**
	 * Constructs an empty graph.
	 * The graph is initialized with empty lists for nodes and root nodes.
	 */
	public Graph() {
		nodes = new ArrayList<Node<T>>();
		rootNodes = new ArrayList<Node<T>>();
	}

	/**
	 * Retrieves the descending nodes of the specified node in the graph using a
	 * depth-first search traversal.
	 * The method resets the visiting status of all nodes before performing the
	 * traversal.
	 *
	 * @param node the node for which to retrieve the descending nodes
	 * @return a list of descending nodes of the specified node
	 */
	public List<Node<T>> getDescendingNodes(Node<T> node) {
		resetVisitingStatus();
		ArrayList<Node<T>> list = new ArrayList<Node<T>>();
		dfs(node, list);
		return list;
	}

	/**
	 * Retrieves the nodes in the graph by performing a depth-first search
	 * traversal.
	 * The method starts the traversal from the root nodes of the graph.
	 *
	 * @return a list of nodes visited during the depth-first search traversal
	 */
	public List<T> getNodesByDFSSearch() {
		ArrayList<Node<T>> list = new ArrayList<Node<T>>();
		for (Node<T> node : rootNodes) {
			dfs(node, list);
		}
		ArrayList<T> objectList = new ArrayList<T>();
		for (Node<T> node : list) {
			objectList.add(node.getObject());
		}
		return objectList;
	}

	/**
	 * Retrieves the ascending nodes of the specified node in the graph using a
	 * recursive depth-first search traversal.
	 * The method resets the visiting status of all nodes before performing the
	 * traversal.
	 *
	 * @param node the node for which to retrieve the ascending nodes
	 * @return a list of ascending nodes of the specified node
	 */
	private List<Node<T>> getAscendingNodes(Node<T> node) {
		resetVisitingStatus();
		ArrayList<Node<T>> list = new ArrayList<Node<T>>();
		rdfs(node, list);
		return list;
	}

	/**
	 * Performs a recursive depth-first search traversal starting from the specified
	 * node.
	 * The method marks each visited node and adds it to the provided list.
	 *
	 * @param node the starting node for the depth-first search traversal
	 * @param list the list to store the visited nodes in ascending order
	 */
	private void rdfs(Node<T> node, ArrayList<Node<T>> list) {
		if (node == null) {
			return;
		}
		node.setVisited(true);
		for (Edge<T> edge : node.getInputEdges()) {
			if (edge.getStartNode().isVisited() == false) {
				rdfs(edge.getStartNode(), list);
			}
		}
		list.add(node);

	}

	/**
	 * Returns list of nodes that are ascendings of the <code>node</code> including
	 * the <code>node</code> itself
	 * 
	 * @param node
	 * @return
	 */
	public List<Node<T>> nodesAffectedByRemoval(Node<T> node) {
		return getAscendingNodes(node);
	}

	/**
	 * Checks whether the graph contains a cycle by performing a depth-first search
	 * traversal.
	 * The method resets the visiting status of all nodes before performing the
	 * traversal.
	 *
	 * @return true if the graph contains a cycle, false otherwise
	 */
	public boolean hasCycle() {
		resetVisitingStatus();
		for (Node<T> node : rootNodes) {
			if (isNodeInCycle(node)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether the given node is part of a cycle in the graph using a
	 * depth-first search traversal.
	 * The method marks each visited node and checks if any of the output edges lead
	 * to a cycle.
	 *
	 * @param node the node to check for cycle presence
	 * @return true if the node is part of a cycle, false otherwise
	 */
	private boolean isNodeInCycle(Node<T> node) {
		if (node.isVisited()) {
			return true;
		}
		node.setVisited(true);
		for (Edge<T> edge : node.getOutputEdges()) {
			if (isNodeInCycle(edge.getEndNode())) {
				return true;
			}
		}
		node.setVisited(false);
		return false;
	}

	/**
	 * Adds a new node with the specified data to the graph.
	 * If a node with the same data already exists, the method returns null.
	 *
	 * @param object the data to be stored in the new node
	 * @return the newly created node if added successfully, null otherwise
	 */
	public Node<T> addNode(T object) {
		if (findNode(object) == null) {
			Node<T> node = new Node<T>(object);
			nodes.add(node);
			rootNodes.add(node);
			return node;
		}
		return null;
	}

	/**
	 * Adds an edge between two nodes in the graph.
	 * If either the start or end node does not exist, a NodeNotExistsException is
	 * thrown.
	 * If the edge already exists, an EdgeExistsException is thrown.
	 *
	 * @param startObject the data of the start node for the edge
	 * @param endObject   the data of the end node for the edge
	 * @throws NodeNotExistsException if either the start or end node does not exist
	 *                                in the graph
	 * @throws EdgeExistsException    if the edge already exists between the
	 *                                specified nodes
	 */
	public void addEdge(T startObject, T endObject)
			throws NodeNotExistsExeption {
		Node<T> startNode = findNode(startObject);
		if (startNode == null) {
			throw new NodeNotExistsExeption(startObject == null ? "null" : startObject.toString());
		}
		Node<T> endNode = findNode(endObject);
		if (endNode == null) {
			throw new NodeNotExistsExeption(endObject == null ? "null" : endObject.toString());
		}
		try {
			startNode.addEdge(endNode);
			if (!endNode.equals(findRootNode(startNode))) {
				rootNodes.remove(endNode);
			}
		} catch (EdgeExistsException e) {
			// TODO Auto-generated catch block
		}
	}

	/**
	 * Finds the root node of the graph starting from the given start node.
	 * The method iterates over the ascending nodes list and checks if each node is
	 * a root node.
	 *
	 * @param startNode the node to start the search from
	 * @return the root node if found, null otherwise
	 */
	public Node<T> findRootNode(Node<T> startNode) {
		List<Node<T>> ascendingNodes = getAscendingNodes(startNode);
		for (Node<T> node : ascendingNodes) {
			if (rootNodes.contains(node)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Removes node having <code>object</code> as node's object and also
	 * removes all ascending nodes of it, except root node.
	 * 
	 * @param Object
	 * @return a boolean indicating whether the node is removed
	 * @throws NodeNotExistsExeption
	 */
	public boolean removeNode(T object) throws NodeNotExistsExeption {
		Node<T> node = findNode(object);
		if (node == null) {
			throw new NodeNotExistsExeption(object == null ? "null" : object.toString());
		}

		List<Node<T>> ascendingNodes = getAscendingNodes(node);
		for (Node<T> ascendingNode : ascendingNodes) {
			ArrayList<Edge<T>> outputEdges = ascendingNode.getOutputEdges();
			ArrayList<Node<T>> nodesToRemove = new ArrayList<Node<T>>(
					outputEdges.size());
			for (Edge<T> edge : outputEdges) {
				nodesToRemove.add(edge.getEndNode());
			}
			for (Node<T> node2 : nodesToRemove) {
				ascendingNode.removeEdge(node2);
			}

			nodes.remove(ascendingNode);
			rootNodes.remove(ascendingNode);
		}
		nodes.remove(node);
		rootNodes.remove(node);

		for (Node<T> remainedNode : nodes) {
			if (remainedNode.getInputEdges().isEmpty()
					&& rootNodes.contains(remainedNode) == false) {
				rootNodes.add(remainedNode);
			}
		}
		return true;
	}

	/**
	 * Searches for a node in the graph that contains the specified data.
	 *
	 * @param object the data to search for in the nodes
	 * @return the node containing the specified data if found, null otherwise
	 */
	public Node<T> findNode(T object) {
		for (Node<T> node : nodes) {
			if (node.getObject() == null && object == null) {
				return null;
			}

			if (node.getObject() != null && node.getObject().equals(object)) {
				return node;
			}
		}
		return null;
	}

	/**
	 * Performs a depth-first search (DFS) traversal starting from the given node
	 * and populates a list with the visited nodes.
	 * The method recursively traverses the graph by iterating over the output edges
	 * of the current node.
	 * If the end node of an edge has not been visited, the method calls itself with
	 * the end node as the new current node.
	 * Non-root nodes are added to the list and marked as visited.
	 *
	 * @param node the current node being visited
	 * @param list the list to be populated with the visited nodes
	 */
	private void dfs(Node<T> node, List<Node<T>> list) {
		if (node == null) {
			return;
		}
		for (Edge<T> edge : node.getOutputEdges()) {
			if (edge.getEndNode().isVisited() == false) {
				dfs(edge.getEndNode(), list);
			}

		}
		if (node.isRoot() == false) {
			list.add(node);
			node.setVisited(true);
		}
	}

	/**
	 * Resets the visiting status of all nodes in the graph.
	 * The method iterates over the nodes list and sets the visiting status of each
	 * node to false.
	 */
	private void resetVisitingStatus() {
		for (Node<T> node : nodes) {
			node.setVisited(false);
		}
	}

	/**
	 * Returns an ArrayList containing all the nodes in the graph.
	 *
	 * @return an ArrayList of nodes in the graph
	 */
	public ArrayList<Node<T>> getNodes() {
		return nodes;
	}

	/**
	 * Returns an ArrayList containing all the root nodes in the graph.
	 *
	 * @return an ArrayList of root nodes in the graph
	 */
	public ArrayList<Node<T>> getRootNodes() {
		return rootNodes;
	}

	/**
	 * Returns a string representation of the graph, including information about the
	 * nodes and edges.
	 *
	 * @return a string representation of the graph
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder("[Graph]\n");
		for (Node<T> node : nodes) {
			if (node.getOutputEdges().isEmpty() && rootNodes.contains(node)) {
				stringBuilder.append("\t").append(node).append("\n");
			}
			for (Edge<T> edge : node.getOutputEdges()) {
				stringBuilder.append("\t").append(node).append(" -- > ")
						.append(edge.getEndNode()).append("\n");
			}
		}
		return stringBuilder.toString();
	}

}
