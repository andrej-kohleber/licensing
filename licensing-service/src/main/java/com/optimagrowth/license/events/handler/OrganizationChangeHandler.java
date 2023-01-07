package com.optimagrowth.license.events.handler;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

import com.optimagrowth.license.events.CustomChannels;
import com.optimagrowth.license.events.model.OrganizationChangeModel;

@EnableBinding(CustomChannels.class)
@Log4j2
public class OrganizationChangeHandler {
    
    @StreamListener("inboundOrgChanges")
    public void loggerSink(OrganizationChangeModel organization) {

        log.debug("Received a message of type " + organization.getType());

        switch(organization.getAction()){
            case "GET":
                log.debug("Received a GET event from the organization service for organization id {}", organization.getOrganizationId());
                break;
            case "SAVE":
                log.debug("Received a SAVE event from the organization service for organization id {}", organization.getOrganizationId());
                break;
            case "UPDATE":
                log.debug("Received a UPDATE event from the organization service for organization id {}", organization.getOrganizationId());
                break;
            case "DELETE":
                log.debug("Received a DELETE event from the organization service for organization id {}", organization.getOrganizationId());
                break;
            default:
                log.error("Received an UNKNOWN event from the organization service of type {}", organization.getType());
                break;
        }
    }
}
