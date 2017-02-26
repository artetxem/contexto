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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ContextDictionary {
    
    private static final Charset CHARSET = Charset.forName("utf-8");
    
    private final InternalNode root;
    private final InternalDataReader data;
    private final InternalCorpus srcCorpus;
    private final InternalCorpus trgCorpus;
    
    private ContextDictionary(ByteBuffer buffer) {
        data = new InternalDataReader(buffer);
        final int size = buffer.capacity();
        long rootPointer = data.seek(size - 24).readPointer();
        long srcCorpusPointer = data.seek(size - 16).readPointer();
        long trgCorpusPointer = data.seek(size - 8).readPointer();
        root = data.seek(rootPointer).readNode();
        srcCorpus = data.seek(srcCorpusPointer).readCorpus();
        trgCorpus = data.seek(trgCorpusPointer).readCorpus();
    }
    
    public List<Translation> search(String query) {
        final InternalPhrase phrase = root.search(data, query.getBytes(CHARSET));
        if (phrase == null) return Collections.EMPTY_LIST;
        int total = 0;
        for (InternalTranslation translation : phrase.getTranslations()) {
            total += translation.getNumberOfOccurrences();
        }
        final List<Translation> res = new ArrayList<>(phrase.getTranslations().size());
        for (InternalTranslation translation : phrase.getTranslations()) {
            res.add(new TranslationImpl(translation, total));
        }
        return res;
    }
    
    public List<String> autocomplete(String query) {
        return root.autocomplete(data, query.getBytes(CHARSET));
    }
    
    public static ContextDictionary fromBinaryModel(File f) throws IOException {
        return fromBinaryModel(f, false);
    }
    
    public static ContextDictionary fromBinaryModel(File f, boolean inMemory) throws IOException {
        if (inMemory) {
            final byte bytes[] = Files.readAllBytes(Paths.get(f.getAbsolutePath()));
            return new ContextDictionary(ByteBuffer.wrap(bytes));
        } else {
            final FileChannel fc = new RandomAccessFile(f, "r").getChannel();
            final ByteBuffer buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return new ContextDictionary(buffer);
        }
    }
    
    public static void buildContextDictionary(
            InputStream phrasesInputStream, InputStream srcInputStream,
            InputStream trgInputStream, OutputStream os) throws IOException {
        final InternalDataWriter writer = new InternalDataWriter(os);
        writer.writeByte(0); // Make sure that 0 is a safe null pointer
        
        final long srcCorpusPointer = InternalCorpus.write(srcInputStream, writer);
        final long trgCorpusPointer = InternalCorpus.write(trgInputStream, writer);
        
        final BufferedReader in = new BufferedReader(new InputStreamReader(phrasesInputStream));
        String line;
        final Deque<InternalNode.Builder> stack = new ArrayDeque<>();
        stack.push(new InternalNode.Builder(new byte[]{}));
        while ((line = in.readLine()) != null) {
            final String fields[] = line.split("\t");
            final InternalPhrase phrase = InternalPhrase.parse(line);
            final long phrasePointer = writer.writePhrase(phrase);
            final long phraseWeight = phrase.getWeight();
            final byte src[] = fields[0].getBytes(CHARSET);
            final byte top[] = stack.peek().getString();
            final int minLength = src.length < top.length ? src.length : top.length;
            int matchLength = minLength;
            for (int i = 0; i < minLength; i++) {
                final int src_i = src[i] & 0xFF; // to unsigned int
                final int top_i = top[i] & 0xFF; // to unsigned int
                if (src_i < top_i) {
                    throw new RuntimeException("Unsorted input");
                } else if (src_i > top_i) {
                    matchLength = i;
                    break;
                }
            }
            if (matchLength == minLength && top.length >= src.length) {
                throw new RuntimeException("Unsorted input");
            }
            
            InternalNode.Builder newNode = new InternalNode.Builder(src);
            newNode.setPhrase(phrasePointer, phraseWeight);
            while (true) {
                InternalNode.Builder node = stack.pop();
                InternalNode.Builder prevNode = stack.peek();
                int nodeLength = node.length();
                int prevNodeLength = prevNode == null ? -1 : prevNode.length();
                if (prevNodeLength > matchLength) {
                    prevNode.addChild(node, prevNode.length(), writer);
                } else if (prevNodeLength == matchLength) {
                    prevNode.addChild(node, prevNode.length(), writer);
                    stack.push(newNode);
                    break;
                } else if (nodeLength == matchLength) {
                    stack.push(node);
                    stack.push(newNode);
                    break;
                } else {
                    InternalNode.Builder parent = new InternalNode.Builder(Arrays.copyOf(src, matchLength));
                    parent.addChild(node, matchLength, writer);
                    stack.push(parent);
                    stack.push(newNode);
                    break;
                }
            }
        }
        
        while (stack.size() > 1) {
            InternalNode.Builder node = stack.pop();
            InternalNode.Builder prevNode = stack.peek();
            prevNode.addChild(node, prevNode.length(), writer);
        }
        final InternalNode root = stack.pop().build(0);
        final long rootPointer = writer.writeNode(root);
        
        writer.writePointer(rootPointer);
        writer.writePointer(srcCorpusPointer);
        writer.writePointer(trgCorpusPointer);
    }
    
    private class TranslationImpl implements Translation {
        
        private final InternalTranslation translation;
        private final int total;
        
        public TranslationImpl(InternalTranslation translation, int total) {
            this.translation = translation;
            this.total = total;
        }
        
        @Override
        public String getTranslation() {
            return translation.getString();
        }

        @Override
        public double getFrequency() {
            return (double)translation.getNumberOfOccurrences() / total;
        }

        @Override
        public List<TranslationExample> getExamples() {
            final List<TranslationExample> examples = new ArrayList<>();
            for (InternalPhrasePairExample example : translation.getExamples()) {
                examples.add(new TranslationExampleImpl(example));
            }
            return examples;
        }
        
    }
    
    private class TranslationExampleImpl implements TranslationExample {

        private final InternalPhrasePairExample example;
        
        public TranslationExampleImpl(InternalPhrasePairExample example) {
            this.example = example;
        }
        
        @Override
        public String getSrcLeftContext() {
            return example.getSrc().getLeftContext(srcCorpus);
        }

        @Override
        public String getSrcPhrase() {
            return example.getSrc().getPhrase(srcCorpus);
        }

        @Override
        public String getSrcRightContext() {
            return example.getSrc().getRightContext(srcCorpus);
        }

        @Override
        public String getTrgLeftContext() {
            return example.getTrg().getLeftContext(trgCorpus);
        }

        @Override
        public String getTrgPhrase() {
            return example.getTrg().getPhrase(trgCorpus);
        }

        @Override
        public String getTrgRightContext() {
            return example.getTrg().getRightContext(trgCorpus);
        }
        
    }
    
}
