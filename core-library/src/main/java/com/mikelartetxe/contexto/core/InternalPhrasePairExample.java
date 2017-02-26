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


class InternalPhrasePairExample {
    
    private final int sentenceId;
    private final int srcPhraseStartOffset, srcPhraseEndOffset;
    private final int trgPhraseStartOffset, trgPhraseEndOffset;
    
    private InternalPhrasePairExample(String s) {
        final String fields[] = s.split(":");
        sentenceId = Integer.parseInt(fields[0]);
        srcPhraseStartOffset = Integer.parseInt(fields[1]);
        srcPhraseEndOffset = Integer.parseInt(fields[2]);
        trgPhraseStartOffset = Integer.parseInt(fields[3]);
        trgPhraseEndOffset = Integer.parseInt(fields[4]);
    }
    
    private InternalPhrasePairExample(InternalDataReader data) {
        // Read sentence id
        sentenceId = data.readCompressedInt();
        
        // Read src offsets
        srcPhraseStartOffset = data.readCompressedInt();
        srcPhraseEndOffset = data.readCompressedInt();
        
        // Read trg offsets
        trgPhraseStartOffset = data.readCompressedInt();
        trgPhraseEndOffset = data.readCompressedInt();
    }
    
    public InternalPhraseExample getSrc() {
        return new InternalPhraseExample(sentenceId, srcPhraseStartOffset, srcPhraseEndOffset);
    }
    
    public InternalPhraseExample getTrg() {
        return new InternalPhraseExample(sentenceId, trgPhraseStartOffset, trgPhraseEndOffset);
    }
    
    public long write(InternalDataWriter data) throws IOException {
        // Write sentence id
        final long pointer = data.writeCompressedInt(sentenceId);
        
        // Write src offsets
        data.writeCompressedInt(srcPhraseStartOffset);
        data.writeCompressedInt(srcPhraseEndOffset);
        
        // Write trg offsets
        data.writeCompressedInt(trgPhraseStartOffset);
        data.writeCompressedInt(trgPhraseEndOffset);
        
        // Return the pointer to the first written byte
        return pointer;
    }
    
    public static InternalPhrasePairExample read(InternalDataReader data) {
        return new InternalPhrasePairExample(data);
    }
    
    public static InternalPhrasePairExample parse(String s) {
        return new InternalPhrasePairExample(s);
    }
    
}
