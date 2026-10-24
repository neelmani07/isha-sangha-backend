package com.ishaworld.listing.service;

import com.ishaworld.center.entity.Center;
import com.ishaworld.center.repository.CenterRepository;
import com.ishaworld.center.service.CenterService;
import com.ishaworld.city.entity.City;
import com.ishaworld.city.service.CityService;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.TeachingMode;
import com.ishaworld.exception.BusinessException;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.initiative.dto.InitiativeSubmissionRequest;
import com.ishaworld.initiative.entity.Initiative;
import com.ishaworld.initiative.repository.InitiativeRepository;
import com.ishaworld.initiative.service.InitiativeService;
import com.ishaworld.listing.dto.CenterSubmissionRequest;
import com.ishaworld.listing.dto.MyListingResponse;
import com.ishaworld.listing.dto.OwnListingResponse;
import com.ishaworld.listing.dto.TeacherSubmissionRequest;
import com.ishaworld.program.service.ProgramService;
import com.ishaworld.resourcelink.dto.ResourceSubmissionRequest;
import com.ishaworld.resourcelink.entity.ResourceLink;
import com.ishaworld.resourcelink.repository.ResourceLinkRepository;
import com.ishaworld.resourcelink.service.ResourceLinkService;
import com.ishaworld.storage.ProofStorage;
import com.ishaworld.teacher.entity.Teacher;
import com.ishaworld.teacher.repository.TeacherRepository;
import com.ishaworld.teacher.service.TeacherService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Members submitting teachers and centers. Every submission needs a proof document and starts as
 * PENDING; it only becomes public once an admin approves it.
 */
@Service
public class ListingSubmissionService {

    /** Keeps one account from flooding the review queue. */
    private static final int MAX_OPEN_SUBMISSIONS = 5;

    private final TeacherRepository teacherRepository;
    private final CenterRepository centerRepository;
    private final CityService cityService;
    private final ProgramService programService;
    private final ProofStorage proofStorage;
    private final TeacherService teacherService;
    private final InitiativeService initiativeService;
    private final InitiativeRepository initiativeRepository;
    private final ResourceLinkService resourceLinkService;
    private final ResourceLinkRepository resourceLinkRepository;
    private final CenterService centerService;

    public ListingSubmissionService(
            TeacherRepository teacherRepository,
            CenterRepository centerRepository,
            CityService cityService,
            ProgramService programService,
            ProofStorage proofStorage,
            TeacherService teacherService,
            CenterService centerService,
            InitiativeService initiativeService,
            InitiativeRepository initiativeRepository,
            ResourceLinkService resourceLinkService,
            ResourceLinkRepository resourceLinkRepository
    ) {
        this.teacherRepository = teacherRepository;
        this.centerRepository = centerRepository;
        this.cityService = cityService;
        this.programService = programService;
        this.proofStorage = proofStorage;
        this.teacherService = teacherService;
        this.initiativeService = initiativeService;
        this.initiativeRepository = initiativeRepository;
        this.resourceLinkService = resourceLinkService;
        this.resourceLinkRepository = resourceLinkRepository;
        this.centerService = centerService;
    }

    public String uploadProof(Long userId, MultipartFile file) {
        return proofStorage.store(userId, file);
    }

    @Transactional(readOnly = true)
    public List<MyListingResponse> myListings(Long userId) {
        List<MyListingResponse> listings = new ArrayList<>();
        teacherRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).forEach(t -> listings.add(new MyListingResponse(
                "TEACHER", t.getId(), t.getName(), t.getSlug(), t.getCity().getName(),
                t.getListingStatus(), t.getRejectionReason(), t.getCreatedAt())));
        centerRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).forEach(c -> listings.add(new MyListingResponse(
                "CENTER", c.getId(), c.getName(), c.getSlug(), c.getCity().getName(),
                c.getListingStatus(), c.getRejectionReason(), c.getCreatedAt())));
        initiativeRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).forEach(i -> listings.add(toMine(i)));
        resourceLinkRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).forEach(r -> listings.add(toMine(r)));
        listings.sort(Comparator.comparing(MyListingResponse::submittedAt).reversed());
        return listings;
    }

    @Transactional(readOnly = true)
    public OwnListingResponse ownTeacher(Long userId, Long id) {
        Teacher t = teacherRepository.findById(id)
                .filter(found -> Objects.equals(found.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        return new OwnListingResponse(t.getListingStatus(), t.getRejectionReason(), t.getProofUrl(), teacherService.toResponse(t), null, null, null);
    }

    @Transactional(readOnly = true)
    public OwnListingResponse ownCenter(Long userId, Long id) {
        Center c = centerRepository.findById(id)
                .filter(found -> Objects.equals(found.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        return new OwnListingResponse(c.getListingStatus(), c.getRejectionReason(), c.getProofUrl(), null, centerService.toResponse(c), null, null);
    }

    @Transactional
    public MyListingResponse submitInitiative(Long userId, InitiativeSubmissionRequest request) {
        return toMine(initiativeService.submit(userId, request));
    }

    @Transactional
    public MyListingResponse updateInitiative(Long userId, Long id, InitiativeSubmissionRequest request) {
        return toMine(initiativeService.update(userId, id, request));
    }

    @Transactional(readOnly = true)
    public OwnListingResponse ownInitiative(Long userId, Long id) {
        Initiative i = initiativeService.own(userId, id);
        return new OwnListingResponse(i.getListingStatus(), i.getRejectionReason(), i.getPhotoKey(), null, null,
                initiativeService.toResponses(List.of(i), userId).get(0), null);
    }

    @Transactional
    public MyListingResponse submitResource(Long userId, ResourceSubmissionRequest request) {
        return toMine(resourceLinkService.submit(userId, request));
    }

    @Transactional
    public MyListingResponse updateResource(Long userId, Long id, ResourceSubmissionRequest request) {
        return toMine(resourceLinkService.update(userId, id, request));
    }

    @Transactional(readOnly = true)
    public OwnListingResponse ownResource(Long userId, Long id) {
        ResourceLink r = resourceLinkService.own(userId, id);
        return new OwnListingResponse(r.getListingStatus(), r.getRejectionReason(), null, null, null, null,
                resourceLinkService.toResponses(List.of(r), userId).get(0));
    }

    @Transactional
    public MyListingResponse submitTeacher(Long userId, TeacherSubmissionRequest request) {
        ensureCanSubmit(userId);
        Teacher teacher = new Teacher();
        teacher.setSubmittedByUserId(userId);
        applyTeacher(teacher, userId, request);
        teacher.setSlug(uniqueSlug(request.name(), teacher.getCity(), teacherRepository::existsBySlug));
        return toMine(teacherRepository.save(teacher));
    }

    @Transactional
    public MyListingResponse updateTeacher(Long userId, Long id, TeacherSubmissionRequest request) {
        Teacher teacher = teacherRepository.findById(id)
                .filter(t -> Objects.equals(t.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        ensureEditable(teacher.getListingStatus());
        applyTeacher(teacher, userId, request);
        return toMine(teacherRepository.save(teacher));
    }

    @Transactional
    public MyListingResponse submitCenter(Long userId, CenterSubmissionRequest request) {
        ensureCanSubmit(userId);
        Center center = new Center();
        center.setSubmittedByUserId(userId);
        applyCenter(center, userId, request);
        center.setSlug(uniqueSlug(request.name(), center.getCity(), centerRepository::existsBySlug));
        return toMine(centerRepository.save(center));
    }

    @Transactional
    public MyListingResponse updateCenter(Long userId, Long id, CenterSubmissionRequest request) {
        Center center = centerRepository.findById(id)
                .filter(c -> Objects.equals(c.getSubmittedByUserId(), userId))
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        ensureEditable(center.getListingStatus());
        applyCenter(center, userId, request);
        return toMine(centerRepository.save(center));
    }

    private void applyTeacher(Teacher teacher, Long userId, TeacherSubmissionRequest request) {
        City city = cityService.getCity(request.cityId());
        teacher.setCity(city);
        teacher.setName(request.name().trim());
        teacher.setBio(request.bio());
        teacher.setPrograms(new LinkedHashSet<>(programService.findByIds(request.programIds())));
        teacher.setTeachingMode(request.teachingMode() == null ? TeachingMode.IN_PERSON : request.teachingMode());
        teacher.setLanguages(request.languages());
        teacher.setYearsTeaching(request.yearsTeaching());
        teacher.setPhone(request.phone());
        teacher.setEmail(request.email());
        teacher.setWebsite(request.website());
        teacher.setLatitude(request.latitude() != null ? request.latitude() : city.getLatitude());
        teacher.setLongitude(request.longitude() != null ? request.longitude() : city.getLongitude());
        teacher.setProofUrl(checkedProof(userId, request.proofKey()));
        resetForReview(teacher::setListingStatus, teacher::setRejectionReason);
        teacher.setActive(true);
        teacher.setVerified(false);
    }

    private void applyCenter(Center center, Long userId, CenterSubmissionRequest request) {
        City city = cityService.getCity(request.cityId());
        center.setCity(city);
        center.setName(request.name().trim());
        center.setType(request.type());
        center.setDescription(request.description());
        center.setAddress(request.address());
        center.setLatitude(request.latitude() != null ? request.latitude() : city.getLatitude());
        center.setLongitude(request.longitude() != null ? request.longitude() : city.getLongitude());
        center.setPhone(request.phone());
        center.setEmail(request.email());
        center.setWebsite(request.website());
        center.setOpeningHours(request.openingHours());
        center.setLanguages(request.languages());
        center.setProofUrl(checkedProof(userId, request.proofKey()));
        resetForReview(center::setListingStatus, center::setRejectionReason);
        center.setActive(true);
        center.setVerified(false);
    }

    private String checkedProof(Long userId, String proofKey) {
        if (!proofStorage.isOwnedBy(proofKey, userId)) {
            throw new BusinessException("Please upload your proof document again and resubmit.");
        }
        return proofKey;
    }

    private static void resetForReview(
            Consumer<ListingStatus> setStatus,
            Consumer<String> setReason
    ) {
        setStatus.accept(ListingStatus.PENDING);
        setReason.accept(null);
    }

    private void ensureCanSubmit(Long userId) {
        long open = teacherRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(t -> t.getListingStatus() == ListingStatus.PENDING).count()
                + centerRepository.findBySubmittedByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(c -> c.getListingStatus() == ListingStatus.PENDING).count();
        if (open >= MAX_OPEN_SUBMISSIONS) {
            throw new BusinessException("You already have " + MAX_OPEN_SUBMISSIONS
                    + " listings waiting for review. Please wait until they are reviewed.");
        }
    }

    private static void ensureEditable(ListingStatus status) {
        if (status == ListingStatus.APPROVED) {
            throw new BusinessException("This listing is already approved. To change it, please contact us through Feedback.");
        }
    }

    /** "Ananya" in Coimbatore -> "ananya-coimbatore", then "-2", "-3" if taken. */
    private static String uniqueSlug(String name, City city, Predicate<String> exists) {
        String base = SlugGenerator.from(name + " " + city.getName());
        String slug = base;
        for (int i = 2; exists.test(slug); i++) {
            slug = base + "-" + i;
        }
        return slug;
    }

    private static MyListingResponse toMine(Initiative i) {
        return new MyListingResponse("INITIATIVE", i.getId(), i.getTitle(), i.getSlug(), i.getCity().getName(),
                i.getListingStatus(), i.getRejectionReason(), i.getCreatedAt());
    }

    private static MyListingResponse toMine(ResourceLink r) {
        return new MyListingResponse("RESOURCE", r.getId(), r.getTitle(), null, r.getCity() == null ? null : r.getCity().getName(),
                r.getListingStatus(), r.getRejectionReason(), r.getCreatedAt());
    }

    private static MyListingResponse toMine(Teacher t) {
        return new MyListingResponse("TEACHER", t.getId(), t.getName(), t.getSlug(), t.getCity().getName(),
                t.getListingStatus(), t.getRejectionReason(), t.getCreatedAt());
    }

    private static MyListingResponse toMine(Center c) {
        return new MyListingResponse("CENTER", c.getId(), c.getName(), c.getSlug(), c.getCity().getName(),
                c.getListingStatus(), c.getRejectionReason(), c.getCreatedAt());
    }
}
