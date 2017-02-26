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

package com.mikelartetxe.contexto.web;

import com.mikelartetxe.contexto.core.Translation;
import com.mikelartetxe.contexto.core.TranslationExample;
import java.util.ArrayList;
import java.util.List;


public class SearchResponse {
        
    private List<TranslationResponse> translations;

    public List<TranslationResponse> getTranslations() {
        return translations;
    }

    public void setTranslations(List<TranslationResponse> translations) {
        this.translations = translations;
    }
    
    public static SearchResponse fromAPI(List<Translation> translations) {
        final SearchResponse response = new SearchResponse();
        final List<TranslationResponse> list = new ArrayList<>();
        for (final Translation translation : translations) {
            final TranslationResponse t = new TranslationResponse();
            t.setTranslation(translation.getTranslation());
            t.setFrequency(translation.getFrequency());
            final List<TranslationExampleResponse> examples = new ArrayList<>(translation.getExamples().size());
            for (TranslationExample example : translation.getExamples()) {
                try {
                    final TranslationExampleResponse e = new TranslationExampleResponse();
                    e.setSrcLeftContext(example.getSrcLeftContext());
                    e.setSrcPhrase(example.getSrcPhrase());
                    e.setSrcRightContext(example.getSrcRightContext());
                    e.setTrgLeftContext(example.getTrgLeftContext());
                    e.setTrgPhrase(example.getTrgPhrase());
                    e.setTrgRightContext(example.getTrgRightContext());
                    examples.add(e);
                } catch (Exception e) {
                    e.printStackTrace(); // TODO Proper error handling
                }
            }
            t.setExamples(examples);
            list.add(t);
        }
        response.setTranslations(list);
        return response;
    }
    
    
    public static class TranslationResponse {
        
        private String translation;
        private double frequency;
        private List<TranslationExampleResponse> examples;

        public String getTranslation() {
            return translation;
        }

        public void setTranslation(String translation) {
            this.translation = translation;
        }

        public double getFrequency() {
            return frequency;
        }

        public void setFrequency(double frequency) {
            this.frequency = frequency;
        }

        public List<TranslationExampleResponse> getExamples() {
            return examples;
        }

        public void setExamples(List<TranslationExampleResponse> examples) {
            this.examples = examples;
        }
        
    }
    
    public static class TranslationExampleResponse {
        
        private String srcLeftContext, srcPhrase, srcRightContext;
        private String trgLeftContext, trgPhrase, trgRightContext;

        public String getSrcLeftContext() {
            return srcLeftContext;
        }

        public void setSrcLeftContext(String srcLeftContext) {
            this.srcLeftContext = srcLeftContext;
        }

        public String getSrcPhrase() {
            return srcPhrase;
        }

        public void setSrcPhrase(String srcPhrase) {
            this.srcPhrase = srcPhrase;
        }

        public String getSrcRightContext() {
            return srcRightContext;
        }

        public void setSrcRightContext(String srcRightContext) {
            this.srcRightContext = srcRightContext;
        }

        public String getTrgLeftContext() {
            return trgLeftContext;
        }

        public void setTrgLeftContext(String trgLeftContext) {
            this.trgLeftContext = trgLeftContext;
        }

        public String getTrgPhrase() {
            return trgPhrase;
        }

        public void setTrgPhrase(String trgPhrase) {
            this.trgPhrase = trgPhrase;
        }

        public String getTrgRightContext() {
            return trgRightContext;
        }

        public void setTrgRightContext(String trgRightContext) {
            this.trgRightContext = trgRightContext;
        }
        
    }
    
}
