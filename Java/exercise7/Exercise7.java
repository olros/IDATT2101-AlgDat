package exercise7;

import java.io.*;

public class Exercise7 {
	public static void main(String[] args) {
		String inputFilename = "tale.txt";
		String outputFilename = "compressed.lz77";
		String outputFilename2 = "decompressed.txt";
		try (
				DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(inputFilename))));
				DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename)));
				DataOutputStream output2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename2)))
		) {
			int inputLength = input.available();
			System.out.println("Input: " + inputLength);
			byte[] inputData = new byte[inputLength];
			input.readFully(inputData, 0, inputLength);
			byte[] out = LZ77.compress(inputData);
			System.out.println("Output1: " + out.length);
			byte[] out2 = Huffman.compress(out);
			System.out.println("Output2: " + out2.length);

			output.write(out2);
			byte[] decoded = Huffman.decompress(out2);
			System.out.println("Hmm: " + (out.length == decoded.length));
			System.out.println("Expanded1: " + decoded.length);
			byte[] decoded2 = LZ77.decompress(decoded);
			int decodedLength = decoded2.length;
			System.out.println("Expanded2: " + decoded2.length);
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
}
