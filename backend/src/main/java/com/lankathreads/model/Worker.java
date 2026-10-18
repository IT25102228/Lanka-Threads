
package com.lankathreads.model;

import jakarta.persistence.*;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String workerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String categoryAssigned; // Men, Women, Children

    @Column(nullable = false)
    private String activeProductionLine;

    @Column(nullable = false)
    private String shift; // Morning, Evening, Night

    @Column
    private String skill; // Sewing, Checking, Ironing, Cutting, Packing

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column
    private String gender = "Male"; // Male, Female

    @Column
    private String status = "Active"; // Active, On Leave, Inactive

    @Column
    private String photoUrl;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWorkerId() { return workerId; }
    public void setWorkerId(String workerId) { this.workerId = workerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategoryAssigned() { return categoryAssigned; }
    public void setCategoryAssigned(String categoryAssigned) { this.categoryAssigned = categoryAssigned; }

    public String getActiveProductionLine() { return activeProductionLine; }
    public void setActiveProductionLine(String activeProductionLine) { this.activeProductionLine = activeProductionLine; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getGender() { return gender != null ? gender : "Male"; }
    public void setGender(String gender) { this.gender = gender; }

    public String getStatus() { return status != null ? status : "Active"; }
    public void setStatus(String status) { this.status = status; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}

