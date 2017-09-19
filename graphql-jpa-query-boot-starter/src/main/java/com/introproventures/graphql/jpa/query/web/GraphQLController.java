/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.introproventures.graphql.jpa.query.web;

import java.io.IOException;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;

import graphql.ExecutionResult;

/**
 * Provides controller with Json and Form POST mapping endpoints for GraphQLExecutor instance
 * 
 * @author Igor Dianov
 *
 */
@RestController
@ConditionalOnWebApplication
@ConditionalOnClass(GraphQLExecutor.class)
public class GraphQLController {
    
    private  GraphQLExecutor   graphQLExecutor;
    private  ObjectMapper  mapper;

    /**
     * Create instance of Spring GraphQLController RestController
     * 
     * @param graphQLExecutor
     * @param mapper
     */
    public GraphQLController(GraphQLExecutor graphQLExecutor, ObjectMapper mapper) {
        super();
        this.graphQLExecutor = graphQLExecutor;
        this.mapper = mapper;
    }
    
    @RequestMapping(method = RequestMethod.POST, value = "${spring.graphql.jpa.query.path:/graphql}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ExecutionResult postJson(@RequestBody @Valid final GraphQLQueryRequest query) throws IOException 
    {
        Map<String, Object> variablesMap = variablesStringToMap(query.getVariables());

        return graphQLExecutor.execute(query.getQuery(), variablesMap);
    }

    @RequestMapping(method = RequestMethod.POST, value = "${spring.graphql.jpa.query.path:/graphql}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ExecutionResult postForm( 
            @RequestParam final String query, 
            @RequestParam(required = false) final String variables) throws IOException 
    {
        Map<String, Object> variablesMap = variablesStringToMap(variables);
        
        return graphQLExecutor.execute(query, variablesMap);
    }

    /**
     * Convert String argument to a Map as expected by {@link GraphQLJpaExecutor#execute(String, Map)}. GraphiQL posts both
     * query and variables as String, so Spring MVC mapping is useless here.
     *
     * @param variables
     * @return
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> variablesStringToMap(final String variables) throws IOException {
        Map<String, Object> variablesMap = null;
        if (variables != null && !variables.isEmpty())
            variablesMap = mapper.readValue(variables, Map.class);
        return variablesMap;
    }

    /**
     * Query Request Data Transfer Object
     * 
     * @author Igor Dianov
     *
     */
    @Validated
    public static class GraphQLQueryRequest {
        
        @NotNull
        private String query;
        
        private String variables;

        /**
         * Default constructor
         */
        GraphQLQueryRequest() {
        }
        
        /**
         * @param query
         * @param variables
         */
        public GraphQLQueryRequest(String query) {
            super();
            this.query = query;
        }
        
        /**
         * @return the variables
         */
        public String getVariables() {
            return this.variables;
        }
        
        /**
         * @param variables
         *            the variables to set
         */
        public void setVariables(String variables) {
            this.variables = variables;
        }
        
        /**
         * @return the query
         */
        public String getQuery() {
            return this.query;
        }
        
    }
}
