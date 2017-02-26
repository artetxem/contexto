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


class InternalPhraseExample {
    
    private final int sentenceId;
    private final int phraseStartOffset, phraseEndOffset;
    
    public InternalPhraseExample(int sentenceId, int phraseStartOffset, int phraseEndOffset) {
        this.sentenceId = sentenceId;
        this.phraseStartOffset = phraseStartOffset;
        this.phraseEndOffset = phraseEndOffset;
    }
    
    public String getLeftContext(InternalCorpus corpus) {
        return corpus.getLeftContext(sentenceId, phraseStartOffset);
    }
    
    public String getRightContext(InternalCorpus corpus) {
        return corpus.getRightContext(sentenceId, phraseEndOffset);
    }
    
    public String getPhrase(InternalCorpus corpus) {
        return corpus.getPhrase(sentenceId, phraseStartOffset, phraseEndOffset);
    }
    
    public String getFullText(InternalCorpus corpus) {
        return corpus.getSentence(sentenceId);
    }
    
}
