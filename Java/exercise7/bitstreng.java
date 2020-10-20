package exercise7;

import java.io.*;

class bitstreng {
	int lengde;
	long biter;

	bitstreng() {
	}

	bitstreng(int len, long bits) {
		lengde = len;
		biter = bits;
	}

	bitstreng(bitstreng s) {
		lengde = s.lengde;
		biter = s.biter;
	}

	bitstreng(int len, byte b) {
		this.lengde = len;
		this.biter = convertByte(b, len);
	}

	static bitstreng konkatenere(bitstreng s1, bitstreng s2) {
		bitstreng ny = new bitstreng();
		ny.lengde = s1.lengde + s2.lengde;
		if (ny.lengde > 64) {
			System.out.println("For lang bitstreng, går ikke!");
			return null;
		}
		ny.biter = s2.biter | (s1.biter << s2.lengde);
		return ny;
	}

	public long convertByte(byte b, int length) {
		long temp = 0;
		for (long i = 1 << length - 1; i != 0; i >>= 1) {
			if ((b & i) == 0) {
				temp = (temp << 1);
			} else temp = ((temp << 1) | 1);
		}
		return temp;
	}

	public void remove() {
		this.biter = (biter >> 1);
		this.lengde--;
	}
}
