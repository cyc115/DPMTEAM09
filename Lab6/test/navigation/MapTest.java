package navigation;

import static org.junit.Assert.*;

import java.util.HashSet;

import lejos.robotics.pathfinding.Node;

import org.junit.Test;

import robotcore.Configuration;

/**
 * Various unit tests for determining the integrity of the internal map representation
 * @author Peter Henderson
 *
 */
public class MapTest {

	/**
	 * Tests that the map of the correct size is created with proper node connectons
	 */
	@Test
	public void correctSize() {
		Map map = Map.getInstance();
		map.populateMap(); //since is singleton, doesn't reset

		Node start = map.getClosestNode(0, 0);

		assertEquals(Configuration.GRID_SIZE*Configuration.GRID_SIZE, 
						countNodes(start, new HashSet<Node>()));
		
	}
	
	/**
	 * Tests that the nodes are blocked successfully
	 */
	@Test
	public void nodeBlockedSuccessfully(){
		Map map = Map.getInstance();
		map.populateMap();

		Node start = map.getClosestNode(-15, -15);

		map.blockNodeAt(45, 45);
		
		assertEquals(Configuration.GRID_SIZE*Configuration.GRID_SIZE -1, 
				countNodes(start, new HashSet<Node>()));
		
		assertEquals(map.isNodeBlocked(45, 45), true);
		assertEquals(map.isNodeBlocked(210, 245), true);
		assertEquals(!map.isNodeBlocked(150, 150), true);

	}
	
	/**
	 * Tests that the correct (closest) nodes are removed based on x and y coords
	 */
	@Test
	public void correctNodesRemoved(){
		Map map = Map.getInstance();
		map.populateMap();

		Node start = map.getClosestNode(-15, -15);

		map.blockNodeAt(40, 40);
		map.blockNodeAt(-23, 40);
		map.blockNodeAt(45, 45);
		
		assertFalse(containsNode(start, new HashSet<Node>(), map.getClosestNode(45,45)));
		assertFalse(containsNode(start, new HashSet<Node>(), map.getClosestNode(-15,45)));
		

		assertEquals(Configuration.GRID_SIZE*Configuration.GRID_SIZE -2, 
				countNodes(start, new HashSet<Node>()));
	}
	
	/**
	 * Test that a blocked node has no neighbors
	 */
	@Test
	public void blockedNodeHasNoNeighbors(){
		Map map = Map.getInstance();
		map.populateMap();

		Node start = map.getClosestNode(0, 0);
		
		map.blockNodeAt(0, 0);
		assertEquals(1, countNodes(start, new HashSet<Node>()));
	}
	
	/**
	 * counts the total number of nodes from a start node. Recursion!
	 * @param start
	 * @return
	 */
	private boolean containsNode(Node start, HashSet<Node> counted, Node removed){
		boolean contains = false;
		if(!counted.contains(start)){
			counted.add(start);
		}
		
		for(Node neighbor : start.getNeighbors()){
			if(!counted.contains(neighbor)){
				contains |= containsNode(neighbor, counted, removed);
			}
		}
		
		return contains;
	}
	
	/**
	 * counts the total number of nodes from a start node. Recursion!
	 * @param start
	 * @return
	 */
	private int countNodes(Node start, HashSet<Node> counted){
		int accumulator = 0;
		if(!counted.contains(start)){
			counted.add(start);
		}
		else{
			return 0;
		}
		
		for(Node neighbor : start.getNeighbors()){
			if(!counted.contains(neighbor)){
				accumulator += countNodes(neighbor, counted);
			}
		}
		
		return accumulator + 1;
	}

}
