package com.ishaworld.meditator.service;

import com.ishaworld.city.entity.City;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.meditator.dto.MeditatorProfileRequest;
import com.ishaworld.meditator.dto.MeditatorResponse;
import com.ishaworld.meditator.entity.Meditator;
import com.ishaworld.meditator.repository.MeditatorRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MeditatorService {

    /** Approximate points are placed this far (km) from the city centre, in a random direction. */
    private static final double MIN_OFFSET_KM = 1.5;
    private static final double MAX_OFFSET_KM = 6.0;
    private static final int MAX_GLOBE_POINTS = 5000;

    private final MeditatorRepository meditatorRepository;
    private final CityService cityService;
    private final SecureRandom random = new SecureRandom();

    public MeditatorService(MeditatorRepository meditatorRepository, CityService cityService) {
        this.meditatorRepository = meditatorRepository;
        this.cityService = cityService;
    }

    @Transactional
    public MeditatorResponse saveOwnProfile(Long userId, MeditatorProfileRequest request) {
        Meditator meditator = meditatorRepository.findByAppUserId(userId).orElseGet(() -> {
            Meditator created = new Meditator();
            created.setAppUserId(userId);
            return created;
        });

        City city = cityService.getCity(request.cityId());
        boolean cityChanged = meditator.getCity() == null || !Objects.equals(meditator.getCity().getId(), city.getId());
        meditator.setCity(city);
        if (cityChanged) {
            placeApproximately(meditator, city);
        }

        meditator.setDisplayName(toDisplayName(request.name()));
        meditator.setAbout(blankToNull(request.about()));
        meditator.setPractices(joinPractices(request.practices()));
        meditator.setLanguages(blankToNull(request.languages()));
        meditator.setOpenToConnect(request.openToConnect());
        meditator.setOpenToVolunteer(request.openToVolunteer());
        meditator.setVisible(request.visible() == null || request.visible());

        return toResponse(meditatorRepository.save(meditator));
    }

    @Transactional(readOnly = true)
    public MeditatorResponse getOwnProfile(Long userId) {
        return meditatorRepository.findByAppUserId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("No meditator profile yet"));
    }

    @Transactional
    public void deleteOwnProfile(Long userId) {
        meditatorRepository.findByAppUserId(userId).ifPresent(meditatorRepository::delete);
    }

    @Transactional(readOnly = true)
    public PageResponse<MeditatorResponse> findVisible(Long cityId, Pageable pageable) {
        var page = cityId == null
                ? meditatorRepository.findByVisibleTrueAndActiveTrue(pageable)
                : meditatorRepository.findByCityIdAndVisibleTrueAndActiveTrue(cityId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<MeditatorResponse> findVisibleForGlobe() {
        return meditatorRepository.findByVisibleTrueAndActiveTrue(PageRequest.of(0, MAX_GLOBE_POINTS))
                .map(this::toResponse)
                .getContent();
    }

    @Transactional(readOnly = true)
    public Map<Long, Long> countVisibleByCity() {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : meditatorRepository.countVisibleByCity()) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    /** Puts the member at a random point 1.5–6 km from the city centre; their real location is never asked for. */
    void placeApproximately(Meditator meditator, City city) {
        if (city.getLatitude() == null || city.getLongitude() == null) {
            throw new BusinessException("This city has no location on the map yet. Please choose a nearby city.");
        }
        double lat = city.getLatitude().doubleValue();
        double lng = city.getLongitude().doubleValue();
        double distanceKm = MIN_OFFSET_KM + random.nextDouble() * (MAX_OFFSET_KM - MIN_OFFSET_KM);
        double bearing = random.nextDouble() * 2 * Math.PI;
        double dLat = (distanceKm * Math.cos(bearing)) / 111.32;
        double dLng = (distanceKm * Math.sin(bearing)) / (111.32 * Math.max(0.2, Math.cos(Math.toRadians(lat))));
        meditator.setApproxLatitude(BigDecimal.valueOf(lat + dLat).setScale(6, RoundingMode.HALF_UP));
        meditator.setApproxLongitude(BigDecimal.valueOf(lng + dLng).setScale(6, RoundingMode.HALF_UP));
    }

    /** "priya narayanan" -> "Priya N." */
    static String toDisplayName(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        String first = capitalize(parts[0]);
        return parts.length > 1 ? first + " " + Character.toUpperCase(parts[parts.length - 1].charAt(0)) + "." : first;
    }

    private static String capitalize(String word) {
        return word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase(Locale.ROOT);
    }

    private static String joinPractices(List<String> practices) {
        if (practices == null || practices.isEmpty()) {
            return null;
        }
        return String.join(",", practices.stream().map(p -> p.replace(",", " ").trim()).filter(p -> !p.isEmpty()).distinct().toList());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private MeditatorResponse toResponse(Meditator meditator) {
        City city = meditator.getCity();
        List<String> practices = meditator.getPractices() == null
                ? List.of()
                : Arrays.stream(meditator.getPractices().split(",")).toList();
        return new MeditatorResponse(
                meditator.getId(),
                meditator.getDisplayName(),
                city.getId(),
                city.getName(),
                city.getSlug(),
                meditator.getApproxLatitude().doubleValue(),
                meditator.getApproxLongitude().doubleValue(),
                meditator.getAbout(),
                practices,
                meditator.getLanguages(),
                meditator.isOpenToConnect(),
                meditator.isOpenToVolunteer(),
                meditator.isVisible()
        );
    }
}
