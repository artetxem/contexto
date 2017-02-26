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

import com.mikelartetxe.contexto.core.ContextDictionary;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;


@Path("rest")
public class RestAPI {
        
    private static final Map<String, ContextDictionary> ID2MODEL;
    
    static {
        try {
            final String modelDir = System.getProperty("dictionary.models");
            if (modelDir == null) {
                throw new RuntimeException("ERROR: Required system property unset (dictionary.models)");
            }
            ID2MODEL = new HashMap<>();
            for (File f : new File(modelDir).listFiles()) {
                if (f.getName().endsWith(".dict.bin")) {
                    final String id = f.getName().substring(0, f.getName().length() - 9);
                    ID2MODEL.put(id, ContextDictionary.fromBinaryModel(f));
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    @Context
    private UriInfo context;

    public RestAPI() {}
    
    @GET
    @Path("list_dictionaries")
    @Produces(MediaType.APPLICATION_JSON)
    public ListDictionariesResponse search() {
        System.err.println("LIST_DICTIONARIES");
        return ListDictionariesResponse.fromCollection(ID2MODEL.keySet());
    }
    
    @GET
    @Path("search")
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResponse search(@QueryParam("q") String query, @QueryParam("dict") String dict) {
        System.err.println("SEARCH (" + dict + "): " + query.replace(" ", "_"));
        return SearchResponse.fromAPI(ID2MODEL.get(dict).search(query));
    }
    
    @GET
    @Path("autocomplete")
    @Produces(MediaType.APPLICATION_JSON)
    public AutocompleteResponse autocomplete(@QueryParam("q") String query, @QueryParam("dict") String dict) {
        System.err.println("AUTOCOMPLETE (" + dict + "): " + query.replace(" ", "_"));
        return AutocompleteResponse.fromAPI(ID2MODEL.get(dict).autocomplete(query));
    }
    
}
