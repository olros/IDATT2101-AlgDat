package Exercise8;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

public class Exercise8 {

}

class Graph {
	private static final String[] inputFields = new String[6];
	Node[] nodes;
	HashMap<String, Integer> places = new HashMap<>();
	private int amountOfNodes;

	public static void main(String[] args) {
		try (
				BufferedReader nodeFile = new BufferedReader(new FileReader("island/noder.txt"));
				BufferedReader edgeFile = new BufferedReader(new FileReader("island/kanter.txt"));
				BufferedReader POIFile = new BufferedReader(new FileReader("island/interessepkt.txt"))
		) {
			Graph graph = new Graph(nodeFile, edgeFile, POIFile);
//			System.out.println("\nNearest: ");
//			graph.findNearestByTypeWithDijkstra(96862, 2);
			System.out.println("\nRoute: ");
			graph.findRouteWithDijkstra(graph.places.get("\"Svínafell\""), graph.places.get("\"Flaga\""));
//			System.out.println("\nRoute: ");
//			graph.findRouteWithAstar(206, 1497);
		} catch (IOException e) {
			System.out.println("ERROR: Couldn't read some of the files");
		}

	}

	public Graph(BufferedReader nodesFile, BufferedReader edgesFiles, BufferedReader POIsFile) throws IOException {
		readNodes(nodesFile);
		readEdges(edgesFiles);
		readPOIs(POIsFile);
	}

	private static void lineSplit(String line, int amount) {
		int j = 0;
		int length = line.length();
		for (int i = 0; i < amount; ++i) {
			while (line.charAt(j) <= ' ') ++j;
			int wordStart = j;
			while (j < length && line.charAt(j) > ' ') ++j;
			inputFields[i] = line.substring(wordStart, j);
		}
	}

	private void readNodes(BufferedReader nodesFile) throws IOException {
		lineSplit(nodesFile.readLine(), 1);
		this.amountOfNodes = Integer.parseInt(inputFields[0]);
		this.nodes = new Node[amountOfNodes];
		for (int i = 0; i < amountOfNodes; ++i) {
			lineSplit(nodesFile.readLine(), 3);
//			System.out.println(inputFields[0] + ", " + inputFields[1] + ", " + inputFields[2]);
			int index = Integer.parseInt(inputFields[0]);
			double lat = Double.parseDouble(inputFields[1]) * (180 / Math.PI);
			double lon = Double.parseDouble(inputFields[2]) * (180 / Math.PI);
			this.nodes[index] = new Node(index, lat, lon);
			this.nodes[index].data = new Previous();
		}
	}

	private void readEdges(BufferedReader edgesFile) throws IOException {
		lineSplit(edgesFile.readLine(), 1);
		for (int i = 0; i < Integer.parseInt(inputFields[0]); ++i) {
			lineSplit(edgesFile.readLine(), 5);
			int from = Integer.parseInt(inputFields[0]);
			int to = Integer.parseInt(inputFields[1]);
			int time = Integer.parseInt(inputFields[2]);
			int distance = Integer.parseInt(inputFields[3]);
			int speedLimit = Integer.parseInt(inputFields[4]);
//			System.out.println(inputFields[0] + ", " + inputFields[1] + ", " + inputFields[2] + ", " + inputFields[3] + ", " + inputFields[4]);
			Edge edge = new Edge(this.nodes[to], this.nodes[from].firstEdge, time, distance, speedLimit);
			this.nodes[from].firstEdge = edge;
		}
	}

	private void readPOIs(BufferedReader POIsFile) throws IOException {
		lineSplit(POIsFile.readLine(), 1);
		int amountOfPOIs = Integer.parseInt(inputFields[0]);
		for (int i = 0; i < amountOfPOIs; ++i) {
			lineSplit(POIsFile.readLine(), 3);
			int nodeNr = Integer.parseInt(inputFields[0]);
			int type = Integer.parseInt(inputFields[1]);
			String name = inputFields[2];
			nodes[nodeNr].name = name;
			nodes[nodeNr].type = type;
			this.places.put(name, nodeNr);
		}
	}

	private int findDistance(Node node1, Node node2) {
		double sinLat = Math.sin((node1.latitude - node2.latitude) / 2.0);
		double sinLng = Math.sin((node1.longitude - node2.longitude) / 2.0);
		return (int) (35285538.46153846153846153846 * Math.asin(Math.sqrt(
				sinLat * sinLat + node1.cosLat * node2.cosLat * sinLng * sinLng)));
	}

	private PriorityQueue<Node> getDijkstraPriorityQueue() {
		return new PriorityQueue<>(Comparator.comparingInt(a -> (a.data.distance)));
	}

	private PriorityQueue<Node> getAstarPriorityQueue() {
		return new PriorityQueue<>(Comparator.comparingInt(a -> (a.data.fullDistance)));
	}

	private Node[] dijkstraNearestByType(Node startNode, int type) {
		PriorityQueue<Node> queue = getDijkstraPriorityQueue();
		queue.add(startNode);
		Node[] nearest = new Node[10];
		int noFound = 0;
		for (int i = this.amountOfNodes; i > 1; --i) {
			Node node = queue.poll();
			if (node == null) continue;
			if ((node.type == type || ((type == 2 || type == 4) && node.type == 6)) && !node.visited) {
				nearest[noFound] = node;
				noFound++;
				node.visited = true;
			}
			if (noFound == 10) break;
			for (Edge edge = node.firstEdge; edge != null; edge = edge.nextEdge) {
				shorten(node, edge, queue);
			}
		}
		return nearest;
	}

	public void findNearestByTypeWithDijkstra(int startNodeNr, int type) {
		System.out.println(this.nodes[startNodeNr].toString());
		Node[] nodes = dijkstraNearestByType(this.nodes[startNodeNr], type);
		for (Node node : nodes) {
			if (node != null) {
				System.out.println(node.name + " " + node.type + " " + node.index);
			}
		}
	}

	private int dijkstraFromTo(Node startNode, Node endNode) {
		startNode.data.distance = 0;
		endNode.endNode = true;
		PriorityQueue<Node> queue = getDijkstraPriorityQueue();
		queue.add(startNode);
		int count = 0;
		while (queue.size() > 0) {
			Node node = queue.poll();
			System.out.println(node.firstEdge);
			count++;
			if (node.endNode) return count;
			for (Edge edge = node.firstEdge; edge != null; edge = edge.nextEdge) {
				shorten(node, edge, queue);
			}
		}
		return -1;
	}

	private int astarFromTo(Node startNode, Node endNode) {
		startNode.data.distance = 0;
		startNode.data.distanceToEnd = findDistance(startNode, endNode);
		startNode.data.fullDistance = startNode.data.distanceToEnd;
		endNode.endNode = true;
		PriorityQueue<Node> queue = getAstarPriorityQueue();
		queue.add(startNode);
		int count = 0;
		while (!queue.isEmpty()) {
			Node node = queue.poll();
			count++;
			if (node.endNode) return count;
			for (Edge edge = node.firstEdge; edge != null; edge = edge.nextEdge) {
				shorten(node, edge, endNode, queue);
			}
		}
		return -1;
	}

	public void findRouteWithDijkstra(int from, int to) {
		Node startNode = this.nodes[from];
		Node endNode = this.nodes[to];
		long startTime = System.nanoTime();
		int checked = dijkstraFromTo(startNode, endNode);
		long time = System.nanoTime() - startTime;
		System.out.println("Dijkstra: " + checked + " nodes checked, " + (double) time / 1000000 + "ms taken.");
		printRoute(startNode, endNode);
	}

	public void findRouteWithAstar(int from, int to) {
		Node startNode = this.nodes[from];
		Node endNode = this.nodes[to];
		long startTime = System.nanoTime();
		int checked = astarFromTo(startNode, endNode);
		long time = System.nanoTime() - startTime;
		System.out.println("Astar: " + checked + " nodes checked, " + (double) time / 1000000 + "ms taken.");
		printRoute(startNode, endNode);
	}

	private void printRoute(Node startNode, Node endNode) {
//		try (FileWriter outputStream = new FileWriter(startNode.name + "-" + endNode.name + ".txt")) {
		try (FileWriter outputStream = new FileWriter("route.txt")) {
			Node node = endNode;
			System.out.println(node.data.distance);
			while (node != null) {
				outputStream.write(node.toString() + "\n");
				node = node.data.previousNode;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROR: Couldn't print route.");
		}
	}

	private void shorten(Node node, Edge edge, PriorityQueue<Node> queue) {
		Previous nodeData = node.data;
		Previous nextNodeData = edge.to.data;
		System.out.println("Info: " + nextNodeData.distance  + ", " +  nodeData.distance  + ", " +  edge.time);
		if (nextNodeData.distance > nodeData.distance + edge.time) {
			nextNodeData.distance = nodeData.distance + edge.time;
			nextNodeData.previousNode = node;
			queue.add(edge.to);
		}
	}

	private void shorten(Node startNode, Edge edge, Node endNode, PriorityQueue<Node> queue) {
		if (edge.to.data.distanceToEnd == -1) {
			int dist = findDistance(edge.to, endNode);
			edge.to.data.distanceToEnd = dist;
			edge.to.data.fullDistance = dist + edge.to.data.distance;
		}
		Previous startPrevious = startNode.data;
		Previous endPrevious = edge.to.data;
		if (endPrevious.distance > startPrevious.distance + edge.time) {
			endPrevious.distance = startPrevious.distance + edge.time;
			endPrevious.previousNode = startNode;
			endPrevious.fullDistance = endPrevious.distance + endPrevious.distanceToEnd;
			queue.add(edge.to);
		}
	}
}

class Node {
	Edge firstEdge;
	Previous data;
	int index;
	double latitude;
	double longitude;
	double cosLat;
	boolean visited;
	boolean endNode;
	int type;
	String name;

	Node(int index, double latitude, double longitude) {
		this.index = index;
		this.latitude = latitude;
		this.longitude = longitude;
		this.cosLat = Math.cos(latitude);
	}

	@Override
	public String toString() {
		return "Node{" +
				"index=" + index +
				", latitude=" + latitude +
				", longitude=" + longitude +
				", type=" + type +
				", name='" + name + '\'' +
				'}';
	}
}

class Edge {
	Edge nextEdge;
	Node to;
	int time;
	int distance;
	int speedLimit;

	public Edge(Node to, Edge nextEdge, int time, int distance, int speedLimit) {
		this.to = to;
		this.nextEdge = nextEdge;
		this.time = time;
		this.distance = distance;
		this.speedLimit = speedLimit;
	}

	@Override
	public String toString() {
		return "Edge{" +
				"to=" + to +
				", time=" + time +
				'}';
	}
}

class Previous {
	static int infinity = 1000000000;
	int distance;
	int fullDistance;
	int distanceToEnd;
	Node previousNode;

	Previous() {
		this.distance = infinity;
		this.fullDistance = infinity;
		this.distanceToEnd = -1;
	}
}
