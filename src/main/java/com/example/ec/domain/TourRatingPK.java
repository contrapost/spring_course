package com.example.ec.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * Tour Rating Primary Key containing a Tour and a Customer Identifier
 *
 * Created by alexandershipunov on 20/01/2017.
 */
@Embeddable
public class TourRatingPK implements Serializable {
    @ManyToOne
    private Tour tour;

    @Column(insertable = false, updatable = false, nullable = false)
    private Integer customerId;

    public TourRatingPK() {
    }

    /**
     * Fully initialize a Tour Rating Pk
     *
     * @param tour       the tour.
     * @param customerId the customer identifier.
     */
    public TourRatingPK(Tour tour, Integer customerId) {
        this.tour = tour;
        this.customerId = customerId;
    }

    public Tour getTour() {
        return tour;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TourRatingPK that = (TourRatingPK) o;

        if (!tour.equals(that.tour)) return false;
        return customerId.equals(that.customerId);

    }

    @Override
    public int hashCode() {
        int result = tour.hashCode();
        result = 31 * result + customerId.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TourRatingPk{" +
                "tour=" + tour +
                ", customerId=" + customerId +
                '}';
    }
}
