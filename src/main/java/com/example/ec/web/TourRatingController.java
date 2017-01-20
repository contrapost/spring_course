package com.example.ec.web;

import com.example.ec.domain.Tour;
import com.example.ec.domain.TourRating;
import com.example.ec.domain.TourRatingPK;
import com.example.ec.repo.TourRatingRepository;
import com.example.ec.repo.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.AbstractMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

/**
 * Created by alexandershipunov on 20/01/2017.
 *
 */
@RestController
@RequestMapping(path = "/tours/{tourId}/ratings")
public class TourRatingController {
    TourRatingRepository tourRatingRepository;
    TourRepository tourRepository;

    @Autowired
    public TourRatingController(TourRatingRepository tourRatingRepository, TourRepository tourRepository) {
        this.tourRatingRepository = tourRatingRepository;
        this.tourRepository = tourRepository;
    }

    protected TourRatingController() {

    }

    /**
     * Create a Tour Rating.
     *
     * @param tourId
     * @param ratingDTO
     */
    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public void createTourRating(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDTO ratingDTO) {
        Tour tour = verifyTour(tourId);
        tourRatingRepository.save(new TourRating( new TourRatingPK(tour, ratingDTO.getCustomerId()),
                ratingDTO.getScore(), ratingDTO.getComment()));
    }

    /**
     * Lookup a the Ratings for a tour.
     *
     * @param tourId
     * @param pageable
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    public Page<RatingDTO> getAllRatingsForTour(@PathVariable(value = "tourId") int tourId, Pageable pageable) {
        Tour tour = verifyTour(tourId);
        Page<TourRating> tourRatingPage = tourRatingRepository.findByPkTourId(tour.getId(), (Pageable) pageable);
        List<RatingDTO> RatingDTOList = tourRatingPage.getContent().stream().map(this::toDto).collect(Collectors.toList());
        return new PageImpl<RatingDTO>(RatingDTOList, pageable, tourRatingPage.getTotalPages());
    }

    /**
     * Calculate the average Score of a Tour.
     *
     * @param tourId
     * @return Tuple of "average" and the average value.
     */
    @RequestMapping(method = RequestMethod.GET, path = "/average")
    public AbstractMap.SimpleEntry<String, Double> getAverage(@PathVariable(value = "tourId") int tourId) {
        Tour tour = verifyTour(tourId);
        List<TourRating> ratings = tourRatingRepository.findByPkTourId(tourId);
        OptionalDouble average = ratings.stream().mapToInt(TourRating::getScore).average();
        double result = average.isPresent() ? average.getAsDouble() : null;
        return new AbstractMap.SimpleEntry<String, Double>("average", result);
    }

    /**
     * Update score and comment of a Tour Rating
     *
     * @param tourId
     * @param RatingDTO
     * @return The modified Rating DTO.
     */
    @RequestMapping(method = RequestMethod.PUT)
    public RatingDTO updateWithPut(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDTO RatingDTO) {
        TourRating rating = verifyTourRating(tourId, RatingDTO.getCustomerId());
        rating.setScore(RatingDTO.getScore());
        rating.setComment(RatingDTO.getComment());
        return toDto(tourRatingRepository.save(rating));
    }
    /**
     * Update score or comment of a Tour Rating
     *
     * @param tourId
     * @param RatingDTO
     * @return The modified Rating DTO.
     */
    @RequestMapping(method = RequestMethod.PATCH)
    public RatingDTO updateWithPatch(@PathVariable(value = "tourId") int tourId, @RequestBody @Validated RatingDTO RatingDTO) {
        TourRating rating = verifyTourRating(tourId, RatingDTO.getCustomerId());
        if (RatingDTO.getScore() != null) {
            rating.setScore(RatingDTO.getScore());
        }
        if (RatingDTO.getComment() != null) {
            rating.setComment(RatingDTO.getComment());
        }
        return toDto(tourRatingRepository.save(rating));
    }

    /**
     * Delete a Rating of a tour made by a customer
     *
     * @param tourId
     * @param customerId
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/{customerId}")
    public void delete(@PathVariable(value = "tourId") int tourId, @PathVariable(value = "customerId") int customerId) {
        TourRating rating = verifyTourRating(tourId, customerId);
        tourRatingRepository.delete(rating);
    }

    /**
     * Convert the TourRating entity to a RatingDTO
     *
     * @param tourRating
     * @return RatingDTO
     */
    private RatingDTO toDto(TourRating tourRating) {
        return new RatingDTO(tourRating.getScore(), tourRating.getComment(), tourRating.getPk().getCustomerId());
    }

    /**
     * Verify and return the TourRating for a particular tourId and Customer
     * @param tourId
     * @param customerId
     * @return the found TourRating
     * @throws NoSuchElementException if no TourRating found
     */
    private TourRating verifyTourRating(int tourId, int customerId) throws NoSuchElementException {
        TourRating rating = tourRatingRepository.findByPkTourIdAndPkCustomerId(tourId, customerId);
        if (rating == null) {
            throw new NoSuchElementException("Tour-Rating pair for request("
                    + tourId + " for customer" + customerId);
        }
        return rating;
    }

    /**
     * Verify and return the Tour given a tourId.
     *
     * @param tourId
     * @return the found Tour
     * @throws NoSuchElementException if no Tour found.
     */
    private Tour verifyTour(int tourId) throws NoSuchElementException {
        Tour tour = tourRepository.findOne(tourId);
        if (tour == null) {
            throw new NoSuchElementException("Tour does not exist " + tourId);
        }
        return tour;
    }

    /**
     * Exception handler if NoSuchElementException is thrown in this Controller
     *
     * @param ex
     * @return Error message String.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String return400(NoSuchElementException ex) {
        return ex.getMessage();

    }
}
