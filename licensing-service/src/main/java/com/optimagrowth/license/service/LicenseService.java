package com.optimagrowth.license.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.optimagrowth.license.model.Organization;
import com.optimagrowth.license.service.client.OrganizationDiscoveryClient;
import com.optimagrowth.license.service.client.OrganizationFeignClient;
import com.optimagrowth.license.service.client.OrganizationRestTemplateClient;
import com.optimagrowth.license.utils.UserContextHolder;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.optimagrowth.license.config.ServiceConfig;
import com.optimagrowth.license.model.License;
import com.optimagrowth.license.repository.LicenseRepository;

@Service
@Log4j2
public class LicenseService {

	@Autowired
	private MessageSource messages;

	@Autowired
	private LicenseRepository licenseRepository;

	@Autowired
	private ServiceConfig config;
	
	@Autowired
	private OrganizationFeignClient organizationFeignClient;

	@Autowired
	private OrganizationRestTemplateClient organizationRestClient;

	@Autowired
	private OrganizationDiscoveryClient organizationDiscoveryClient;
	
	private final Random rand  = new Random();
	
	public License getLicense(String licenseId, String organizationId){
		License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
		if (null == license) {
			throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, null),licenseId, organizationId));
		}
		return license.withComment(config.getExampleProperty());
	}
	
	public License getLicense(String licenseId, String organizationId, String clientType) {
		License license = licenseRepository.findByOrganizationIdAndLicenseId(organizationId, licenseId);
		if (license == null) {
			throw new IllegalArgumentException(String.format(messages.getMessage("license.search.error.message", null, null), licenseId, organizationId));
		}
		log.debug("Try to retrieve organizationId: " + organizationId + " - clientType: " + clientType);
		Organization organization = retrieveOrganizationInfo(organizationId, clientType);
		if (organization != null) {
			license.setOrganizationName(organization.getName());
			license.setContactName(organization.getContactName());
			license.setContactEmail(organization.getContactEmail());
			license.setContactPhone(organization.getContactPhone());
		}
		return license.withComment(config.getExampleProperty());
	}

	private Organization retrieveOrganizationInfo(String organizationId, String clientType) {
		Organization organization = null;

		switch (clientType) {
			case "feign":
				System.out.println("I am using the feign client");
				organization = organizationFeignClient.getOrganization(organizationId);
				break;
			case "rest":
				System.out.println("I am using the rest client");
				organization = organizationRestClient.getOrganization(organizationId);
				break;
			case "discovery":
				System.out.println("I am using the discovery client");
				organization = organizationDiscoveryClient.getOrganization(organizationId);
				break;
			default:
				organization = organizationRestClient.getOrganization(organizationId);
				break;
		}
		return organization;
	}

	public License createLicense(License license){
		license.setLicenseId(UUID.randomUUID().toString());
		licenseRepository.save(license);

		return license.withComment(config.getExampleProperty());
	}

	public License updateLicense(License license){
		licenseRepository.save(license);
		
		return license.withComment(config.getExampleProperty());
	}

	public String deleteLicense(String licenseId){
		String responseMessage = null;
		License license = new License();
		license.setLicenseId(licenseId);
		licenseRepository.delete(license);
		responseMessage = String.format(messages.getMessage("license.delete.message", null, null),licenseId);
		return responseMessage;
	}

	private void randomlyRunLong() throws TimeoutException {
		int randomNum = rand.nextInt(3) + 1;
		if (randomNum == 3) {
			sleep();
		}
	}
	
	private void sleep() throws TimeoutException {
		try {
			Thread.sleep(5000);
			throw new java.util.concurrent.TimeoutException();
		} catch (InterruptedException e) {
			log.error(e.getMessage());	
		}
	}
	
	@CircuitBreaker(name = "licenseService", fallbackMethod = "buildFallbackLicenseList")
	@Bulkhead(name = "bulkheadLicenseService", fallbackMethod = "buildFallbackLicenseList")
	@Retry(name = "retryLicenseService", fallbackMethod = "buildFallbackLicenseList")
	@RateLimiter(name = "licenseService", fallbackMethod = "buildFallbackLicenseList")
	public List<License> getLicensesByOrganization(String organizationId) throws TimeoutException {
		log.debug("getLicensesByOrganization Correlation id: {}", UserContextHolder.getContext().getCorrelationId());
//		randomlyRunLong();
		return licenseRepository.findByOrganizationId(organizationId);
	}
	
	private List<License> buildFallbackLicenseList(String organizationId, Throwable t) {
		List<License> fallbackList = new ArrayList<>();
		License license = new License();
		license.setLicenseId("0000000-00-00000");
		license.setOrganizationId(organizationId);
		license.setProductName("Sorry no licensing information currently available");
		fallbackList.add(license);
		return fallbackList;
	}
}
