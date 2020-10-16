package exercise7;

import java.io.*;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Huffman {

	// alphabet size of extended ASCII
	private static final int R = 256;

	private static BinaryIn binaryIn;
	private static BinaryOut binaryOut;

	// Do not instantiate.
	private Huffman() {
	}

	/**
	 * Reads a sequence of 8-bit bytes from standard input; compresses them
	 * using Huffman codes with an 8-bit alphabet; and writes the results
	 * to standard output.
	 */
	public static byte[] compress(byte[] input) {
		binaryIn = new BinaryIn(input);
		binaryOut = new BinaryOut();

		// tabulate frequency counts
		int[] freq = new int[R];
		for (byte b : input) {
			if (b < 0) b = (byte) (255 - b);
			freq[b]++;
		}

		// build Huffman trie
		Node root = buildTrie(freq);

		// build code table
		String[] st = new String[R];
		buildCode(st, root, "");

		// print trie for decoder
		writeTrie(root);

		// print number of bytes in original uncompressed message
		binaryOut.write(input.length);

		// use Huffman code to encode input
		for (int i = 0; i < input.length; i++) {
			byte b = input[i];
			if (b < 0) b = (byte) (255 - b);
			String code = st[b];
			for (int j = 0; j < code.length(); j++) {
				if (code.charAt(j) == '0') {
					binaryOut.write(false);
				} else if (code.charAt(j) == '1') {
					binaryOut.write(true);
				} else throw new IllegalStateException("Illegal state");
			}
		}

		// close output stream
		return binaryOut.close();
	}

	// build the Huffman trie given frequencies
	private static Node buildTrie(int[] freq) {

		// initialze priority queue with singleton trees
		PriorityQueue<Node> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(a -> a.freq));
		for (char c = 0; c < R; c++) {
			if (freq[c] > 0) {
				priorityQueue.add(new Node(c, freq[c], null, null));
			}
		}

		// merge two smallest trees
		while (priorityQueue.size() > 1) {
			Node left = priorityQueue.poll();
			Node right = priorityQueue.poll();
			Node parent = new Node('\0', left.freq + right.freq, left, right);
			priorityQueue.add(parent);
		}
		return priorityQueue.poll();
	}

	// write bitstring-encoded trie to standard output
	private static void writeTrie(Node x) {
		if (x.isLeaf()) {
			binaryOut.write(true);
			binaryOut.write(x.ch, 8);
			return;
		}
		binaryOut.write(false);
		writeTrie(x.left);
		writeTrie(x.right);
	}

	// make a lookup table from symbols and their encodings
	private static void buildCode(String[] st, Node x, String s) {
		if (!x.isLeaf()) {
			buildCode(st, x.left, s + '0');
			buildCode(st, x.right, s + '1');
		} else {
			st[x.ch] = s;
		}
	}

	/**
	 * Reads a sequence of bits that represents a Huffman-compressed message from
	 * standard input; expands them; and writes the results to standard output.
	 */
	public static byte[] decompress(byte[] input) {
		binaryIn = new BinaryIn(input);
		binaryOut = new BinaryOut();

		// read in Huffman trie from input stream
		Node root = readTrie();

		// number of bytes to write
		int length = binaryIn.readInt();

		// decode using the Huffman trie
		for (int i = 0; i < length; i++) {
			Node x = root;
			while (!x.isLeaf()) {
				boolean bit = binaryIn.readBoolean();
				if (bit) x = x.right;
				else x = x.left;
			}
			binaryOut.write(x.ch, 8);
		}
		return binaryOut.close();
	}

	private static Node readTrie() {
		boolean isLeaf = binaryIn.readBoolean();
		if (isLeaf) {
			return new Node(binaryIn.readChar(), -1, null, null);
		} else {
			return new Node('\0', -1, readTrie(), readTrie());
		}
	}

	public static void main(String[] args) {
		String inputFilename = "tale.txt";
		String outputFilename = "compressed.lz77";
		String outputFilename2 = "uncompressed.txt";
		try (
				DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(inputFilename))));
				DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename)));
				DataOutputStream output2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename2)))
		) {
			int inputLength = input.available();
			System.out.println("Input: " + inputLength);
			byte[] inputData = new byte[inputLength];
			input.readFully(inputData, 0, inputLength);
			byte[] out = Huffman.compress(inputData);
			System.out.println("Output: " + out.length);

			output.write(out);
			byte[] decoded = Huffman.decompress(out);
			int decodedLength = decoded.length;
			System.out.println("Expanded: " + decoded.length);
			output2.write(decoded);

			// Check same length
			if (inputLength != decodedLength) {
				throw new Exception("Decoded length differs from original.");
			}
			// Check that input and decoded is equal
			for (int idx = 0; idx < inputLength; idx++) {
				if (decoded[idx] != inputData[idx]) {
					throw new Exception("Decoded data corrupt at index " + idx);
				}
			}
		} catch (IOException e) {
			System.out.println("Couldn't read file");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Huffman trie node
	private static class Node implements Comparable<Node> {
		private final char ch;
		private final int freq;
		private final Node left, right;

		Node(char ch, int freq, Node left, Node right) {
			this.ch = ch;
			this.freq = freq;
			this.left = left;
			this.right = right;
		}

		// is the node a leaf node?
		private boolean isLeaf() {
			assert ((left == null) && (right == null)) || ((left != null) && (right != null));
			return (left == null) && (right == null);
		}

		// compare, based on frequency
		public int compareTo(Node that) {
			return this.freq - that.freq;
		}
	}

}