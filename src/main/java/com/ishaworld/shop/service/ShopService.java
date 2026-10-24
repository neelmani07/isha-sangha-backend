package com.ishaworld.shop.service;

import com.ishaworld.city.entity.City;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.UrlRules;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.ShopCategory;
import com.ishaworld.common.enums.ShopDelivery;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.shop.dto.ShopListingRequest;
import com.ishaworld.shop.dto.ShopListingResponse;
import com.ishaworld.shop.entity.ShopListing;
import com.ishaworld.shop.repository.ShopListingRepository;
import com.ishaworld.storage.ImageStorage;
import com.ishaworld.user.repository.AppUserRepository;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The community shop: listings only. Buyers contact sellers directly; the site never takes payment. */
@Service
public class ShopService {

    private static final int MAX_OPEN_SUBMISSIONS = 10;

    private final ShopListingRepository repository;
    private final CityService cityService;
    private final ImageStorage imageStorage;
    private final AppUserRepository appUserRepository;

    public ShopService(ShopListingRepository repository, CityService cityService, ImageStorage imageStorage, AppUserRepository appUserRepository) {
        this.repository = repository;
        this.cityService = cityService;
        this.imageStorage = imageStorage;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public List<ShopListingResponse> findPublic(ShopCategory category) {
        return repository.findByActiveTrueAndListingStatusOrderByCreatedAtDesc(ListingStatus.APPROVED).stream()
                .filter(l -> category == null || l.getCategory() == category)
                .map(ShopService::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShopListingResponse findPublicBySlug(String slug) {
        return toResponse(repository.findBySlugAndActiveTrueAndListingStatus(slug, ListingStatus.APPROVED)
                .orElseThrow(() -> new NotFoundException("Listing not found")));
    }

    @Transactional
    public ShopListing submit(Long userId, ShopListingRequest request) {
        long open = repository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(l -> l.getListingStatus() == ListingStatus.PENDING).count();
        if (open >= MAX_OPEN_SUBMISSIONS) {
            throw new BusinessException("You already have " + MAX_OPEN_SUBMISSIONS + " products waiting for review.");
        }
        ShopListing listing = new ShopListing();
        listing.setSubmittedByUserId(userId);
        apply(listing, userId, request);
        listing.setSlug(uniqueSlug(request.title(), listing.getCity()));
        return repository.save(listing);
    }

    @Transactional
    public ShopListing update(Long userId, Long id, ShopListingRequest request) {
        ShopListing listing = own(userId, id);
        if (listing.getListingStatus() == ListingStatus.APPROVED) {
            throw new BusinessException("This product is already listed. To change it, please contact us through Feedback.");
        }
        apply(listing, userId, request);
        return repository.save(listing);
    }

    @Transactional(readOnly = true)
    public ShopListing own(Long userId, Long id) {
        return repository.findById(id)
                .filter(l -> Objects.equals(l.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Listing not found"));
    }

    private void apply(ShopListing listing, Long userId, ShopListingRequest request) {
        City city = cityService.getCity(request.cityId());
        listing.setCity(city);
        listing.setTitle(request.title().trim());
        listing.setCategory(request.category());
        listing.setDescription(blankToNull(request.description()));
        listing.setPriceAmount(request.priceAmount());
        listing.setPriceNote(blankToNull(request.priceNote()));
        listing.setDelivery(request.delivery() == null ? ShopDelivery.PICKUP : request.delivery());
        String seller = blankToNull(request.sellerName());
        listing.setSellerName(seller != null ? seller
                : appUserRepository.findById(userId).map(u -> u.getName()).filter(n -> n != null && !n.isBlank()).orElse("A volunteer"));
        listing.setContactWhatsapp(request.contactWhatsapp() == null ? null : blankToNull(request.contactWhatsapp().replaceAll("[^0-9]", "")));
        listing.setContactPhone(blankToNull(request.contactPhone()));
        listing.setContactEmail(blankToNull(request.contactEmail()));
        listing.setContactWebsite(UrlRules.normalize(request.contactWebsite()));
        if (listing.getContactWhatsapp() == null && listing.getContactPhone() == null
                && listing.getContactEmail() == null && listing.getContactWebsite() == null) {
            throw new BusinessException("Please add at least one way for buyers to reach you.");
        }
        if (request.photoKey() != null && !request.photoKey().isBlank()) {
            if (!request.photoKey().equals(listing.getPhotoKey()) && !imageStorage.isOwnedBy(request.photoKey(), userId)) {
                throw new BusinessException("Please upload the photo again.");
            }
            listing.setPhotoKey(request.photoKey());
        }
        listing.setRulesConfirmed(request.rulesConfirmed());
        listing.setActive(true);
        listing.setListingStatus(ListingStatus.PENDING);
        listing.setRejectionReason(null);
    }

    private String uniqueSlug(String title, City city) {
        String base = SlugGenerator.from(title + " " + city.getName());
        String slug = base;
        for (int i = 2; repository.existsBySlug(slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static ShopListingResponse toResponse(ShopListing l) {
        City city = l.getCity();
        return new ShopListingResponse(
                l.getId(), l.getSlug(), l.getTitle(), l.getCategory(), l.getDescription(),
                l.getPriceAmount(), l.getPriceNote(), l.getCurrency(), l.getDelivery(),
                city.getId(), city.getName(), city.getSlug(), l.getSellerName(),
                l.getContactWhatsapp(), l.getContactPhone(), l.getContactEmail(), l.getContactWebsite(),
                l.getPhotoKey() != null ? ImageStorage.publicUrl(l.getPhotoKey()) : l.getPhotoUrl(),
                l.getCreatedAt());
    }
}
