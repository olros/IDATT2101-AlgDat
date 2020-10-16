package exercise7;

import java.io.ByteArrayInputStream;
import java.util.NoSuchElementException;

public final class BinaryIn {
	private static final int EOF = -1;   // end of file

	private ByteArrayInputStream in;      // the input stream
	private int buffer;                  // one character buffer
	private int n;                       // number of bits left in buffer

	public BinaryIn(byte[] input) {
		in = new ByteArrayInputStream(input);
		fillBuffer();
	}

	private void fillBuffer() {
		buffer = in.read();
		n = 8;
	}

	/**
	 * Returns true if this binary input stream is empty.
	 *
	 * @return {@code true} if this binary input stream is empty;
	 * {@code false} otherwise
	 */
	public boolean isEmpty() {
		return buffer == EOF;
	}

	/**
	 * Reads the next bit of data from this binary input stream and return as a boolean.
	 *
	 * @return the next bit of data from this binary input stream as a {@code boolean}
	 * @throws NoSuchElementException if this binary input stream is empty
	 */
	public boolean readBoolean() {
		if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
		n--;
		boolean bit = ((buffer >> n) & 1) == 1;
		if (n == 0) fillBuffer();
		return bit;
	}

	public int readInt() {
		int x = 0;
		for (int i = 0; i < 4; i++) {
			char c = readChar();
			x <<= 8;
			x |= c;
		}
		return x;
	}

	/**
	 * Reads the next 8 bits from this binary input stream and return as an 8-bit char.
	 *
	 * @return the next 8 bits of data from this binary input stream as a {@code char}
	 * @throws NoSuchElementException if there are fewer than 8 bits available
	 */
	public char readChar() {
		if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

		// special case when aligned byte
		if (n == 8) {
			int x = buffer;
			fillBuffer();
			return (char) (x & 0xff);
		}

		// combine last N bits of current buffer with first 8-N bits of new buffer
		int x = buffer;
		x <<= (8 - n);
		int oldN = n;
		fillBuffer();
		if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
		n = oldN;
		x |= (buffer >>> n);
		return (char) (x & 0xff);
		// the above code doesn't quite work for the last character if N = 8
		// because buffer will be -1
	}


	/**
	 * Reads the next <em>r</em> bits from this binary input stream and return
	 * as an <em>r</em>-bit character.
	 *
	 * @param r number of bits to read
	 * @return the next {@code r} bits of data from this binary input streamt as a {@code char}
	 * @throws NoSuchElementException   if there are fewer than {@code r} bits available
	 * @throws IllegalArgumentException unless {@code 1 <= r <= 16}
	 */
	public char readChar(int r) {
		if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value of r = " + r);

		// optimize r = 8 case
		if (r == 8) return readChar();

		char x = 0;
		for (int i = 0; i < r; i++) {
			x <<= 1;
			boolean bit = readBoolean();
			if (bit) x |= 1;
		}
		return x;
	}


	/**
	 * Reads the next 8 bits from this binary input stream and return as an 8-bit byte.
	 *
	 * @return the next 8 bits of data from this binary input stream as a {@code byte}
	 * @throws NoSuchElementException if there are fewer than 8 bits available
	 */
	public byte readByte() {
		char c = readChar();
		return (byte) (c & 0xff);
	}

	public short readShort() {
		short x = 0;
		for (int i = 0; i < 2; i++) {
			char c = readChar();
			x <<= 8;
			x |= c;
		}
		return x;
	}
}
