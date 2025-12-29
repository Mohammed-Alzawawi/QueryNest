package com.example.querynest.storage;

import java.util.BitSet;

public class NullableBitmap {
    public final BitSet bitSet = new BitSet();

    public void setNull(int index) { bitSet.set(index); }
    public boolean isNull(int index) { return bitSet.get(index); }
    public int size() { return bitSet.size(); }
}
