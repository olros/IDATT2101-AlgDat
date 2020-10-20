package exercise7;

import java.io.*;

public class Exercise7 {

	// Compress
	public static void main(String[] args) {
		String inputFilename = "forelesning.pdf";
		String outputFilename = "compressed.lz77";
		String outputFilename2 = "compressed";
		try (
				DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(inputFilename))));
				DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename)));
		) {
			int inputLength = input.available();
			System.out.println("Input size: " + inputLength);
			byte[] inputData = new byte[inputLength];
			input.readFully(inputData, 0, inputLength);
			byte[] out = LZ77.compress(inputData);
			System.out.println("Size after LZ: " + out.length);
			output.write(out);
			int compressedSize = Compress.compress(outputFilename, outputFilename2);
			System.out.println("Size after LZ and Huffman: " + compressedSize);
		} catch (IOException e) {
			System.out.println("Couldn't read file");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Decompress
	public static void main2(String[] args) throws IOException {
		String inputFilename = "compressed";
		String outputFilename = "decompressed.huffman";
		String outputFilename2 = "decompressed.pdf";
		int decompressedSize = Compress.decompress(inputFilename, outputFilename);
		System.out.println("Size after Huffman decompress: " + decompressedSize);

		DataInputStream lzInput = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(outputFilename))));
		DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename2)));

		int inputLength = lzInput.available();
		byte[] inputData = new byte[inputLength];
		lzInput.readFully(inputData, 0, inputLength);
		byte[] decoded = LZ77.decompress(inputData);
		System.out.println("Final size: " + decoded.length);
		output.write(decoded);
	}
}
