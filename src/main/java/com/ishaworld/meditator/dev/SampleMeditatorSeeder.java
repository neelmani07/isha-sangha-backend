package com.ishaworld.meditator.dev;

import com.ishaworld.city.entity.City;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.meditator.dto.MeditatorProfileRequest;
import com.ishaworld.meditator.repository.MeditatorRepository;
import com.ishaworld.meditator.service.MeditatorService;
import com.ishaworld.user.entity.AppUser;
import com.ishaworld.user.repository.AppUserRepository;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local development only: creates sample members with meditator profiles so the map has people on it.
 * Off unless SEED_SAMPLE_MEDITATORS=true. Sample accounts use emails like sample-meditator-12@example.org;
 * remove them with:
 *   DELETE FROM meditator WHERE app_user_id IN (SELECT id FROM app_user WHERE email LIKE 'sample-meditator-%');
 *   DELETE FROM app_user WHERE email LIKE 'sample-meditator-%';
 */
@Component
@ConditionalOnProperty(name = "app.dev.seed-sample-meditators", havingValue = "true")
public class SampleMeditatorSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleMeditatorSeeder.class);

    private static final List<String> NAMES = List.of(
            "Priya Narayanan", "Arjun Krishnan", "Lakshmi Subramanian", "Karthik Venkat", "Meera Raman",
            "Ananya Iyer", "Rahul Sharma", "Divya Menon", "Suresh Kumar", "Kavya Reddy", "Vikram Rao",
            "Nandini Pillai", "Aditya Joshi", "Shreya Nair", "Ganesh Murthy", "Pooja Patel", "Rohan Gupta",
            "Deepa Shankar", "Manoj Das", "Sneha Kulkarni", "Daniel Weber", "Emma Larsen", "Sarah Thompson",
            "Lukas Fischer", "Olivia Brown", "Hannah Schmidt", "James Wilson", "Mia Taylor", "Noah Williams",
            "Isabella Rossi");

    private static final List<String> PRACTICES = List.of(
            "Isha Kriya", "Shambhavi Mahamudra", "Surya Kriya", "Upa Yoga", "Angamardana", "Bhuta Shuddhi",
            "Shoonya Meditation", "Hatha Yoga");

    private static final Map<String, Integer> PER_REAL_CITY = Map.of(
            "coimbatore", 26, "bengaluru", 22, "berlin", 7, "melbourne", 6, "mcminnville", 5);

    private final AppUserRepository appUserRepository;
    private final MeditatorRepository meditatorRepository;
    private final MeditatorService meditatorService;
    private final CityRepository cityRepository;

    public SampleMeditatorSeeder(
            AppUserRepository appUserRepository,
            MeditatorRepository meditatorRepository,
            MeditatorService meditatorService,
            CityRepository cityRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.meditatorRepository = meditatorRepository;
        this.meditatorService = meditatorService;
        this.cityRepository = cityRepository;
    }

    @Override
    public void run(String... args) {
        if (meditatorRepository.count() > 0) {
            return;
        }

        Random random = new Random(108);
        List<City> cities = cityRepository.findAll().stream()
                .filter(city -> city.getLatitude() != null && city.getLongitude() != null)
                .toList();
        int created = 0;

        for (City city : cities) {
            int count = PER_REAL_CITY.getOrDefault(city.getSlug(), random.nextInt(4) == 0 ? 1 + random.nextInt(3) : 0);
            for (int i = 0; i < count; i++) {
                AppUser user = new AppUser();
                user.setEmail("sample-meditator-" + (created + 1) + "@example.org");
                user.setName(NAMES.get(random.nextInt(NAMES.size())));
                user.setActive(true);
                user = appUserRepository.save(user);

                List<String> practices = PRACTICES.stream().filter(p -> random.nextInt(3) == 0).limit(4).toList();
                meditatorService.saveOwnProfile(user.getId(), new MeditatorProfileRequest(
                        user.getName(),
                        city.getId(),
                        null,
                        practices.isEmpty() ? List.of("Isha Kriya") : practices,
                        random.nextBoolean() ? "English, Tamil" : "English",
                        random.nextInt(3) > 0,
                        random.nextBoolean(),
                        // A few members choose to stay hidden, as real members will.
                        random.nextInt(10) != 0
                ));
                created++;
            }
        }

        log.warn("[DEV] Seeded {} sample meditators (sample-meditator-*@example.org)", created);
    }
}
