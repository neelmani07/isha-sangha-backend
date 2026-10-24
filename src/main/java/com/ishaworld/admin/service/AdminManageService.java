package com.ishaworld.admin.service;

import com.ishaworld.admin.dto.ManageRow;
import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.initiative.repository.InitiativeRepository;
import com.ishaworld.meditator.repository.MeditatorRepository;
import com.ishaworld.resourcelink.repository.ResourceLinkRepository;
import com.ishaworld.shop.repository.ShopListingRepository;
import com.ishaworld.story.repository.StoryRepository;
import com.ishaworld.teacher.repository.TeacherRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Search and show/hide across every kind of entry, for the admin "Manage" screen. */
@Service
public class AdminManageService {

    private final CenterRepository centerRepository;
    private final TeacherRepository teacherRepository;
    private final CityRepository cityRepository;
    private final MeditatorRepository meditatorRepository;
    private final InitiativeRepository initiativeRepository;
    private final ResourceLinkRepository resourceLinkRepository;
    private final StoryRepository storyRepository;
    private final ShopListingRepository shopListingRepository;

    public AdminManageService(
            CenterRepository centerRepository,
            TeacherRepository teacherRepository,
            CityRepository cityRepository,
            MeditatorRepository meditatorRepository,
            InitiativeRepository initiativeRepository,
            ResourceLinkRepository resourceLinkRepository,
            StoryRepository storyRepository,
            ShopListingRepository shopListingRepository
    ) {
        this.centerRepository = centerRepository;
        this.teacherRepository = teacherRepository;
        this.cityRepository = cityRepository;
        this.meditatorRepository = meditatorRepository;
        this.initiativeRepository = initiativeRepository;
        this.resourceLinkRepository = resourceLinkRepository;
        this.storyRepository = storyRepository;
        this.shopListingRepository = shopListingRepository;
    }

    @Transactional(readOnly = true)
    public List<ManageRow> search(String kind, String q) {
        String term = q == null ? "" : q.trim();
        return switch (kind.toUpperCase(Locale.ROOT)) {
            case "CENTER" -> centerRepository.findTop50ByNameContainingIgnoreCaseOrderByName(term).stream()
                    .map(c -> new ManageRow("CENTER", c.getId(), c.getName(), c.getCity().getName() + " · " + c.getType().name(),
                            c.getListingStatus().name(), c.isActive(), d(c.getLatitude()), d(c.getLongitude()), "/center/" + c.getSlug()))
                    .toList();
            case "TEACHER" -> teacherRepository.findTop50ByNameContainingIgnoreCaseOrderByName(term).stream()
                    .map(t -> new ManageRow("TEACHER", t.getId(), t.getName(), t.getCity().getName(),
                            t.getListingStatus().name(), t.isActive(), d(t.getLatitude()), d(t.getLongitude()), "/teacher/" + t.getSlug()))
                    .toList();
            case "CITY" -> cityRepository.findTop50ByNameContainingIgnoreCaseOrderByName(term).stream()
                    .map(c -> new ManageRow("CITY", c.getId(), c.getName(), c.getState().getName() + ", " + c.getCountry().getName(),
                            c.getListingStatus().name(), c.isActive(), d(c.getLatitude()), d(c.getLongitude()), "/city/" + c.getSlug()))
                    .toList();
            case "MEDITATOR" -> meditatorRepository.findTop50ByDisplayNameContainingIgnoreCaseOrderByDisplayName(term).stream()
                    .map(m -> new ManageRow("MEDITATOR", m.getId(), m.getDisplayName(),
                            m.getCity().getName() + (m.isVisible() ? "" : " · hidden by member"),
                            null, m.isActive(), d(m.getApproxLatitude()), d(m.getApproxLongitude()), null))
                    .toList();
            case "INITIATIVE" -> initiativeRepository.findTop50ByTitleContainingIgnoreCaseOrderByTitle(term).stream()
                    .map(i -> new ManageRow("INITIATIVE", i.getId(), i.getTitle(), i.getCity().getName(),
                            i.getListingStatus().name(), i.isActive(), d(i.getLatitude()), d(i.getLongitude()), "/initiatives/" + i.getSlug()))
                    .toList();
            case "RESOURCE" -> resourceLinkRepository.findTop50ByTitleContainingIgnoreCaseOrderByTitle(term).stream()
                    .map(r -> new ManageRow("RESOURCE", r.getId(), r.getTitle(), r.getUrl(),
                            r.getListingStatus().name(), r.isActive(), null, null, "/resources"))
                    .toList();
            case "STORY" -> storyRepository.findTop50ByTitleContainingIgnoreCaseOrderByTitle(term).stream()
                    .map(st -> new ManageRow("STORY", st.getId(), st.getTitle(),
                            st.getAuthorName() + (st.isFeatured() ? " · featured" : ""),
                            st.getListingStatus().name(), st.isActive(), null, null, "/stories/" + st.getSlug()))
                    .toList();
            case "PRODUCT" -> shopListingRepository.findTop50ByTitleContainingIgnoreCaseOrderByTitle(term).stream()
                    .map(p -> new ManageRow("PRODUCT", p.getId(), p.getTitle(), p.getSellerName() + " · " + p.getCity().getName(),
                            p.getListingStatus().name(), p.isActive(), null, null, "/shop/" + p.getSlug()))
                    .toList();
            default -> throw new NotFoundException("Unknown kind");
        };
    }

    /** One story at a time is shown big at the top of the Stories page; featuring one un-features the rest. */
    @Transactional
    public void setFeatured(Long storyId, boolean featured) {
        if (featured) {
            storyRepository.findAll().stream().filter(s -> s.isFeatured()).forEach(s -> s.setFeatured(false));
        }
        storyRepository.findById(storyId).orElseThrow(() -> new NotFoundException("Not found")).setFeatured(featured);
    }

    /** Hidden entries disappear from every public page but keep their data, so hiding is always reversible. */
    @Transactional
    public void setActive(String kind, Long id, boolean active) {
        switch (kind.toUpperCase(Locale.ROOT)) {
            case "CENTER" -> centerRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            case "TEACHER" -> teacherRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            case "CITY" -> cityRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            case "MEDITATOR" -> meditatorRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            case "INITIATIVE" -> initiativeRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            case "RESOURCE" -> resourceLinkRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            case "STORY" -> storyRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            case "PRODUCT" -> shopListingRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found")).setActive(active);
            default -> throw new NotFoundException("Unknown kind");
        }
    }

    private static Double d(BigDecimal value) {
        return value == null ? null : value.doubleValue();
    }
}
