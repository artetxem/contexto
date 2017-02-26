/*
 * Copyright (C) 2017  Mikel Artetxe <artetxem@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mikelartetxe.contexto.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


class InternalNode {
    
    private static final int TOP_DESCENDANTS = 10;
    
    private final byte[] substring;
    private final long[] childrenPointers;
    private final long phrasePointer;
    private final long[] topDescendantPointers;
    
    private InternalNode(Builder builder, int index) {
        substring = Arrays.copyOfRange(builder.string, index, builder.string.length);
        childrenPointers = new long[builder.childrenPointers.size()];
        for (int i = 0; i < childrenPointers.length; i++) {
            childrenPointers[i] = builder.childrenPointers.get(i);
        }
        phrasePointer = builder.phrasePointer;
        topDescendantPointers = new long[builder.topDescendants.size()];
        for (int i = 0; i < topDescendantPointers.length; i++) {
            topDescendantPointers[i] = builder.topDescendants.get(i).pointer;
        }
    }
    
    private InternalNode(InternalDataReader data) {
        // Read substring
        final int substringLength = data.readCompressedInt();
        substring = new byte[substringLength];
        for (int i = 0; i < substringLength; i++) {
            substring[i] = data.readByte();
        }
        
        // Read children pointers
        final int childrenLength = data.readUnsignedByte();
        childrenPointers = new long[childrenLength];
        for (int i = 0; i < childrenLength; i++) {
            childrenPointers[i] = data.readCompressedPointer();
        }
        
        // Read phrase pointer
        phrasePointer = data.readCompressedPointer();
        
        // Read top descendant pointers
        final int topDescendantLength = data.readCompressedInt();
        topDescendantPointers = new long[topDescendantLength];
        for (int i = 0; i < topDescendantLength; i++) {
            topDescendantPointers[i] = data.readCompressedPointer();
        }
    }
    
    
    public long write(InternalDataWriter data) throws IOException {
        // Write substring
        final long pointer = data.writeCompressedInt(substring.length);
        for (final byte b : substring) data.writeByte(b);
        
        // Write children pointers
        data.writeByte(childrenPointers.length);
        for (final long p : childrenPointers) data.writeCompressedPointer(p);
        
        // Write phrase pointer
        data.writeCompressedPointer(phrasePointer);
        
        // Write top descendant pointers
        data.writeCompressedInt(topDescendantPointers.length);
        for (final long p : topDescendantPointers) data.writeCompressedPointer(p);
        
        // Return the pointer to the first written byte
        return pointer;
    }
    
    public static InternalNode read(InternalDataReader data) {
        return new InternalNode(data);
    }
    
    public static byte readFirstByte(InternalDataReader data) {
        final int substringLength = data.readCompressedInt();
        if (substringLength <= 0) {
            throw new IllegalArgumentException("Empty substring");
        }
        return data.readByte();
    }
    
    public List<String> autocomplete(InternalDataReader data, byte query[]) {
        final InternalNode node = searchNode(data, query, 0, false);
        if (node == null) return Collections.EMPTY_LIST;
        final List<String> res = new ArrayList<>();
        for (long ptr : node.topDescendantPointers) {
            res.add(data.seek(ptr).readPhrase().getString());
        }
        return res;
    }
    
    public InternalPhrase search(InternalDataReader data, byte query[]) {
        final InternalNode node = searchNode(data, query, 0, true);
        final long ptr = node == null ? 0 : node.phrasePointer;
        return ptr == 0 ? null : data.seek(ptr).readPhrase();
    }
    
    private InternalNode searchNode(InternalDataReader data, byte query[], int from, boolean exact) {
        final int queryMissing = query.length - from;
        if (substring.length > queryMissing && exact) {
            return null;
        }
        final int n = substring.length > queryMissing ? queryMissing : substring.length;
        boolean mismatch = false;
        for (int i = 0; i < n; i++) {
            if (substring[i] != query[from + i]) {
                mismatch = true;
                break;
            }
        }
        if (mismatch) {
            return null;
        } else if (substring.length >= queryMissing) {
            return this;
        } else {
            // TODO Implement binary search for better performance
            for (long childPointer : childrenPointers) {
                final byte b = data.seek(childPointer).readFirstByteFromNode();
                if (b == query[from + substring.length]) {
                    final InternalNode node = data.seek(childPointer).readNode();
                    return node.searchNode(data, query, from + substring.length, exact);
                }
            }
            return null;
        }
    }
    
    
    public static class Builder {
        
        private final byte[] string;
        private final List<Long> childrenPointers;
        private long phrasePointer;
        private List<WeightedPointer> topDescendants;
        
        public Builder(byte[] string) {
            this.string = Arrays.copyOf(string, string.length);
            this.childrenPointers = new ArrayList<>();
            this.topDescendants = new ArrayList<>();
        }
        
        public int length() {
            return string.length;
        }
        
        public byte[] getString() {
            return string;
        }
        
        public void addChild(Builder childBuilder, int index, InternalDataWriter writer) throws IOException {
            topDescendants.addAll(childBuilder.topDescendants);
            updateTopDescendants();
            final long childPointer = writer.writeNode(childBuilder.build(index));
            childrenPointers.add(childPointer);
        }
        
        public void setPhrase(long pointer, long weight) {
            if (phrasePointer != 0) {
                throw new IllegalStateException("Phrase already set");
            }
            if (pointer == 0) {
                throw new IllegalArgumentException("Null pointer");
            }
            phrasePointer = pointer;
            topDescendants.add(new WeightedPointer(pointer, weight));
            updateTopDescendants();
        }
        
        private void updateTopDescendants() {
            Collections.sort(topDescendants);
            final int n = topDescendants.size() > TOP_DESCENDANTS ?
                    TOP_DESCENDANTS :
                    topDescendants.size();
            final List<WeightedPointer> newTop = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                newTop.add(topDescendants.get(topDescendants.size() - i - 1));
            }
            topDescendants = newTop;
        }
        
        public InternalNode build(int index) {
            return new InternalNode(this, index);
        }
        
    }
    
    
    private static class WeightedPointer implements Comparable<WeightedPointer> {
        
        public final long pointer, weight;
        
        public WeightedPointer(long pointer, long weight) {
            this.pointer = pointer;
            this.weight = weight;
        }

        @Override
        public int compareTo(WeightedPointer other) {
            final int aux = Long.compare(this.weight, other.weight);
            return aux == 0 ? Long.compare(this.pointer, other.pointer) : aux;
        }
        
    }
    
}
