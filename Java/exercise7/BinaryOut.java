package exercise7;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class BinaryOut {

	private ByteArrayOutputStream out;  // the output stream
	private int buffer;                // 8-bit buffer of bits to write out
	private int n;                     // number of bits remaining in buffer

	public BinaryOut() {
		out = new ByteArrayOutputStream();
	}

	public void write(int x) {
		writeByte((x >>> 24) & 0xff);
		writeByte((x >>> 16) & 0xff);
		writeByte((x >>>  8) & 0xff);
		writeByte((x >>>  0) & 0xff);
	}

	/**
	 * Writes the specified bit to the binary output stream.
	 *
	 * @param x the bit
	 */
	private void writeBit(boolean x) {
		// add bit to buffer
		buffer <<= 1;
		if (x) buffer |= 1;

		// if buffer is full (8 bits), write out as a single byte
		n++;
		if (n == 8) clearBuffer();
	}

	/**
	 * Writes the 8-bit byte to the binary output stream.
	 *
	 * @param x the byte
	 */
	private void writeByte(int x) {
		assert x >= 0 && x < 256;

		// optimized if byte-aligned
		if (n == 0) {
			out.write(x);
			return;
		}

		// otherwise write one bit at a time
		for (int i = 0; i < 8; i++) {
			boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
			writeBit(bit);
		}
	}

	// write out any remaining bits in buffer to the binary output stream, padding with 0s
	private void clearBuffer() {
		if (n == 0) return;
		if (n > 0) buffer <<= (8 - n);
		out.write(buffer);
		n = 0;
		buffer = 0;
	}

	/**
	 * Flushes the binary output stream, padding 0s if number of bits written so far
	 * is not a multiple of 8.
	 */
	public void flush() {
		clearBuffer();
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Flushes and closes the binary output stream.
	 * Once it is closed, bits can no longer be written.
	 */
	public byte[] close() {
		flush();
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}


	/**
	 * Writes the specified bit to the binary output stream.
	 *
	 * @param x the {@code boolean} to write
	 */
	public void write(boolean x) {
		writeBit(x);
	}

	/**
	 * Writes the 8-bit byte to the binary output stream.
	 *
	 * @param x the {@code byte} to write.
	 */
	public void write(byte x) {
		writeByte(x & 0xff);
	}

	/**
	 * Writes the 8-bit char to the binary output stream.
	 *
	 * @param x the {@code char} to write
	 * @throws IllegalArgumentException unless {@code x} is betwen 0 and 255
	 */
	public void write(char x) {
		if (x < 0 || x >= 256) throw new IllegalArgumentException("Illegal 8-bit char = " + x);
		writeByte(x);
	}

	/**
	 * Write the 16-bit int to the binary output stream.
	 * @param x the {@code short} to write.
	 */
	public void write(short x) {
		writeByte((x >>>  8) & 0xff);
		writeByte((x >>>  0) & 0xff);
	}

	/**
	 * Writes the <em>r</em>-bit char to the binary output stream.
	 *
	 * @param x the {@code char} to write
	 * @param r the number of relevant bits in the char
	 * @throws IllegalArgumentException unless {@code r} is between 1 and 16
	 * @throws IllegalArgumentException unless {@code x} is between 0 and 2<sup>r</sup> - 1
	 */
	public void write(char x, int r) {
		if (r == 8) {
			write(x);
			return;
		}
		if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value for r = " + r);
		if (x >= (1 << r)) throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
		for (int i = 0; i < r; i++) {
			boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
			writeBit(bit);
		}
	}
}
