package exercise7;

import java.io.*;

public class LZ77 {

	// Buffer-size of 64kB
	private static final int BUFFER_SIZE = 65536;
	private static final int THRESHOLD = 3;
	private static final int MAX_LENGTH = 127;

	private LZ77() {}

	/**
	 * Compress an array of bytes
	 *
	 * @param input the bytes to compress
	 * @return an array of compressed bytes
	 */
	public static byte[] compress(byte[] input) {
		int inputLength = input.length;
		byte[] output = new byte[inputLength * 2];
		int[] indexArray = new int[BUFFER_SIZE];
		int[] chainArray = new int[BUFFER_SIZE];
		int inputIndex = 0;
		int outputIndex = 0;
		int literals = 0;
		while (inputIndex < inputLength) {
			int matchOffset = 0;
			int matchLength = 1;
			// Make sure that the remaining inputs are more than the threshold
			if (inputIndex + THRESHOLD < inputLength) {
				int key = hashByte(input, inputIndex);
				int searchIndex = indexArray[key & 0xFFFF] - 1;
				while ((inputIndex - searchIndex) < BUFFER_SIZE && searchIndex >= 0) {
					if (inputIndex + matchLength < inputLength && input[inputIndex + matchLength] == input[searchIndex + matchLength]) {
						int length = 0;
						// Iterate through the input from the searchIndex to find the length of the equality
						while (inputIndex + length < inputLength && length < MAX_LENGTH && input[searchIndex + length] == input[inputIndex + length]) {
							length++;
						}
						if (length > matchLength) {
							matchOffset = inputIndex - searchIndex;
							matchLength = length;
							if (length >= MAX_LENGTH) {
								break;
							}
						}
					}
					// Update the search index from the chain array
					searchIndex = chainArray[searchIndex & 0xFFFF] - 1;
				}
				// Make sure that it's worth to compress
				if (matchLength <= THRESHOLD) {
					matchOffset = 0;
					matchLength = 1;
				}
				int index = inputIndex;
				int end = index + matchLength;
				if (end + THRESHOLD > inputLength) {
					end = inputLength - THRESHOLD;
				}
				while (index < end) {
					// Update the index for each byte of the input to be compressed.
					int key2 = hashByte(input, index);
					chainArray[index & 0xFFFF] = indexArray[key2 & 0xFFFF];
					indexArray[key2 & 0xFFFF] = index + 1;
					index++;
				}
			}
			if (matchOffset == 0) {
				literals += matchLength;
				inputIndex += matchLength;
			}
			// Flush literals if match found, end of input, or longest encodable run.
			if (literals > 0 && (matchOffset > 0 || inputIndex == inputLength || literals == 127)) {
				output[outputIndex++] = (byte) literals;
				int literalIndex = inputIndex - literals;
				while (literalIndex < inputIndex) {
					output[outputIndex++] = input[literalIndex++];
				}
				literals = 0;
			}
			// Add reference to the match
			if (matchOffset > 0) {
				output[outputIndex++] = (byte) (0x80 | matchLength);
				output[outputIndex++] = (byte) (matchOffset >> 8);
				output[outputIndex++] = (byte) matchOffset;
				inputIndex += matchLength;
			}
		}
		byte[] finalBytes = new byte[outputIndex];
		if (outputIndex >= 0) System.arraycopy(output, 0, finalBytes, 0, outputIndex);
		return finalBytes;
	}

	/**
	 * Create a key from the index in the array and following 3
	 *
	 * @param input the array of bytes
	 * @param index the index in the array
	 * @return an int key
	 */
	private static int hashByte(byte[] input, int index) {
		int key = (input[index] & 0xFF) * 33 + (input[index + 1] & 0xFF);
		key = key * 33 + (input[index + 2] & 0xFF);
		key = key * 33 + (input[index + 3] & 0xFF);
		return key;
	}

	/**
	 * Decompress an array of bytes
	 *
	 * @param input the bytes to decompress
	 * @return an uncompressed array of bytes
	 */
	public static byte[] decompress(byte[] input) {
		int inputLength = input.length;
		byte[] output = new byte[inputLength * 20];
		int inputIndex = 0;
		int outputIndex = 0;
		while (inputIndex < inputLength) {
			int matchOffset = 0;
			int matchLength = input[inputIndex++] & 0xFF;
			if (matchLength > 127) {
				matchLength = matchLength & 0x7F;
				matchOffset = input[inputIndex++] & 0xFF;
				matchOffset = (matchOffset << 8) | (input[inputIndex++] & 0xFF);
			}
			int outputEnd = outputIndex + matchLength;
			if (matchOffset == 0) {
				while (outputIndex < outputEnd) {
					if (inputIndex >= inputLength) break;
					output[outputIndex++] = input[inputIndex++];
				}
			} else {
				while (outputIndex < outputEnd) {
					output[outputIndex] = output[outputIndex - matchOffset];
					outputIndex++;
				}
			}
		}
		byte[] finalBytes = new byte[outputIndex];
		System.arraycopy(output, 0, finalBytes, 0, outputIndex);
		return finalBytes;
	}

	public static void main(String[] args) {
		String inputFilename = "newfile.lz77";
		String outputFilename2 = "decompressed.txt";
		try (
				DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(inputFilename))));
				DataOutputStream output2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename2)))
		) {
			int inputLength = input.available();
			System.out.println("Input: " + inputLength);
			byte[] inputData = new byte[inputLength];
			input.readFully(inputData, 0, inputLength);
			byte[] out = LZ77.decompress(inputData);
			System.out.println("Output: " + out.length);

			output2.write(out);
		} catch (IOException e) {
			System.out.println("Couldn't read file");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
//	public static void main(String[] args) {
//		String inputFilename = "test.txt";
//		String outputFilename = "compressed.lz77";
//		String outputFilename2 = "decompressed.txt";
//		try (
//				DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(new File(inputFilename))));
//				DataOutputStream output = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename)));
//				DataOutputStream output2 = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilename2)))
//		) {
//			int inputLength = input.available();
//			System.out.println("Input: " + inputLength);
//			byte[] inputData = new byte[inputLength];
//			input.readFully(inputData, 0, inputLength);
//			byte[] out = LZ77.compress(inputData);
//			System.out.println("Output: " + out.length);
//
//			output.write(out);
//			byte[] decoded = LZ77.decompress(out);
//			int decodedLength = decoded.length;
//			System.out.println("Expanded: " + decoded.length);
//			output2.write(decoded);
//
//			// Check same length
//			if (inputLength != decodedLength) {
//				throw new Exception("Decoded length differs from original.");
//			}
//			// Check that input and decoded is equal
//			for (int idx = 0; idx < inputLength; idx++) {
//				if (decoded[idx] != inputData[idx]) {
//					throw new Exception("Decoded data corrupt at index " + idx);
//				}
//			}
//		} catch (IOException e) {
//			System.out.println("Couldn't read file");
//			System.exit(0);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
}