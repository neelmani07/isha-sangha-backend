package com.ishaworld.teacher.service;

import com.ishaworld.city.service.CityService;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.api.PageResponse;
import com.ishaworld.exception.NotFoundException;
import com.ishaworld.program.dto.ProgramResponse;
import com.ishaworld.program.mapper.ProgramMapper;
import com.ishaworld.program.service.ProgramService;
import com.ishaworld.teacher.dto.TeacherResponse;
import com.ishaworld.teacher.dto.TeacherUpsertRequest;
import com.ishaworld.teacher.entity.Teacher;
import com.ishaworld.teacher.repository.TeacherRepository;
import java.util.LinkedHashSet;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final CityService cityService;
    private final ProgramService programService;
    private final ProgramMapper programMapper;

    public TeacherService(
            TeacherRepository teacherRepository,
            CityService cityService,
            ProgramService programService,
            ProgramMapper programMapper
    ) {
        this.teacherRepository = teacherRepository;
        this.cityService = cityService;
        this.programService = programService;
        this.programMapper = programMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> findAll(
            String city,
            String country,
            String program,
            Long cityId,
            String q,
            Long programId,
            Double lat,
            Double lng,
            boolean sortByDistance,
            Pageable pageable
    ) {
        if (cityId != null) {
            return PageResponse.from(
                    teacherRepository.findByCityFilters(cityId, q, programId, lat, lng, sortByDistance, pageable)
                            .map(this::toResponse)
            );
        }

        var page = teacherRepository.findAll(pageable);
        if (city != null && !city.isBlank()) {
            page = teacherRepository.findByCityName(city, pageable);
        } else if (country != null && !country.isBlank()) {
            page = teacherRepository.findByCountryName(country, pageable);
        } else if (program != null && !program.isBlank()) {
            page = teacherRepository.findByProgramName(program, pageable);
        }
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public TeacherResponse findById(Long id) {
        return toResponse(getTeacher(id));
    }

    @Transactional(readOnly = true)
    public TeacherResponse findBySlug(String slug) {
        return toResponse(teacherRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Teacher not found")));
    }

    @Transactional
    public TeacherResponse create(TeacherUpsertRequest request) {
        Teacher teacher = new Teacher();
        apply(teacher, request);
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public TeacherResponse update(Long id, TeacherUpsertRequest request) {
        Teacher teacher = getTeacher(id);
        apply(teacher, request);
        return toResponse(teacherRepository.save(teacher));
    }

    @Transactional
    public void delete(Long id) {
        teacherRepository.delete(getTeacher(id));
    }

    public Teacher getTeacher(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Teacher not found"));
    }

    private void apply(Teacher teacher, TeacherUpsertRequest request) {
        teacher.setCity(cityService.getCity(request.cityId()));
        teacher.setSlug(request.slug() == null || request.slug().isBlank() ? SlugGenerator.from(request.name()) : request.slug());
        teacher.setName(request.name());
        teacher.setBio(request.bio());
        teacher.setPhotoUrl(request.photoUrl());
        teacher.setLatitude(request.latitude());
        teacher.setLongitude(request.longitude());
        teacher.setPhone(request.phone());
        teacher.setEmail(request.email());
        teacher.setWebsite(request.website());
        // Omitting "active" used to silently hide a new listing; default to visible.
        teacher.setActive(request.active() == null || request.active());
        if (request.teachingMode() != null) {
            teacher.setTeachingMode(request.teachingMode());
        }
        teacher.setLanguages(request.languages());
        teacher.setYearsTeaching(request.yearsTeaching());
        if (request.verified() != null) {
            teacher.setVerified(request.verified());
        }
        teacher.setPrograms(new LinkedHashSet<>(programService.findByIds(request.programIds())));
    }

    private TeacherResponse toResponse(Teacher teacher) {
        var programs = teacher.getPrograms()
                .stream()
                .map(programMapper::toResponse)
                .toList();

        return new TeacherResponse(
                teacher.getId(),
                teacher.getCity().getId(),
                teacher.getCity().getName(),
                teacher.getSlug(),
                teacher.getName(),
                teacher.getBio(),
                teacher.getPhotoUrl(),
                teacher.getLatitude(),
                teacher.getLongitude(),
                teacher.getPhone(),
                teacher.getEmail(),
                teacher.getWebsite(),
                teacher.isActive(),
                programs,
                teacher.getTeachingMode(),
                teacher.getLanguages(),
                teacher.getYearsTeaching(),
                teacher.isVerified()
        );
    }
}

