package com.optimagrowth.license.model;

import org.springframework.hateoas.RepresentationModel;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;


@Getter @Setter @ToString
@Entity
@Table(name = "licenses")
public class License extends RepresentationModel<License> {

	@Id
	@Column(name = "license_id", nullable = false)
	private String licenseId;
	
	private String description;
	
	@Column(name = "organization_id", nullable = false)
	private String organizationId;
	
	@Column(name = "product_name", nullable = false)
	private String productName;
	
	@Column(name = "licenseType", nullable = false)
	private String licenseType;
	
	private String comment;
	
	@Transient
	private String organizationName;
	
	@Transient
	private String contactName;
	
	@Transient
	private String contactPhone;
	
	@Transient
	private String contactEmail;
	
	public License withComment(String comment) {
		this.setComment(comment);
		return this;
	}

	public static void main(String[] args) {
		int[] arr = {4, 3, 0, 10, 1, 2, 3, 4};
		for (int i = 0; i < arr.length - 1; i++) {
			int min = arr[i];
			System.out.println(min);
			for (int j = ++i; j <= arr.length - 1; j++) {
				
			}
		}
	}
}
