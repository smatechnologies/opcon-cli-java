package com.smatechnologies.opcon.command.api.ws;

import java.io.IOException;
import java.util.Objects;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.ContextResolver;

import org.glassfish.jersey.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smatechnologies.opcon.restapiclient.model.Token;
import com.smatechnologies.opcon.restapiclient.model.User;

public class WsLogger implements ClientRequestFilter, ClientResponseFilter {

    private final static Logger LOG = LoggerFactory.getLogger(WsLogger.class);

    private final static String CONTEXT_TIME_PROPERTY_NAME = "SM-Time";

    private ContextResolver<ObjectMapper> objectMapperProvider;

    public WsLogger(ContextResolver<ObjectMapper> objectMapperProvider) {
        this.objectMapperProvider = Objects.requireNonNull(objectMapperProvider, "ObjectMapperProvider cannot be null");
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.setProperty(CONTEXT_TIME_PROPERTY_NAME, System.currentTimeMillis());
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        //Main information
        long time = System.currentTimeMillis() - (long) requestContext.getProperty(CONTEXT_TIME_PROPERTY_NAME);

        LOG.debug("* {} {} => {} ({}) in {}ms", requestContext.getMethod(), requestContext.getUri().toString(), responseContext.getStatusInfo().getReasonPhrase(), responseContext.getStatus(), time);

        //Request information
        String requestBody = "";
        if (objectMapperProvider != null && requestContext.hasEntity()) {
            ObjectMapper objectMapper = objectMapperProvider.getContext(requestContext.getEntityClass());

            if (objectMapper != null) {
                requestBody = removeSensibleData(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(requestContext.getEntity()), requestContext.getEntityClass());
            }
        }

        LOG.trace("  - Request: Header={} Body={}", requestContext.getHeaders(), requestBody);

        //Response information
        String responseBody = "";
        if (objectMapperProvider != null && responseContext.hasEntity()) {
            ObjectMapper objectMapper = objectMapperProvider.getContext(requestContext.getEntityClass());

            if (objectMapper != null && responseContext instanceof ClientResponse) {
                ClientResponse clientResponse = (ClientResponse) responseContext;
                clientResponse.bufferEntity();

                Object json = objectMapper.readValue(clientResponse.readEntity(String.class), Object.class);
                responseBody = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
            }
        }

        LOG.trace("  - Response: Header={} Body={}", responseContext.getHeaders(), responseBody);
    }

    private String removeSensibleData(String string, Class<?> entityClass) {
        if (string == null) {
            return null;
        }

        if (entityClass == Token.class || entityClass == User.class) {
            string = string.replaceAll("\"password\" : \"[^\"]+\"", "\"password\" : \"##### HIDDEN BY LOGGER #####\"");
            string = string.replaceAll("\"externalPassword\" : \"[^\"]+\"", "\"externalPassword\" : \"##### HIDDEN BY LOGGER #####\"");
        }

        return string;
    }
}
