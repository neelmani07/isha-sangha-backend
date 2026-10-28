package com.ishaworld.demo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ishaworld.common.SlugGenerator;
import com.ishaworld.common.dev.SampleCommunitySeeder;
import com.ishaworld.user.entity.AppUser;
import com.ishaworld.user.repository.AppUserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Preview sample data for the live site, controlled by app.demo-content (DEMO_CONTENT):
 * <ul>
 *   <li>{@code on}: once, adds countries and major cities from demo/world.json, sample centres, teachers and meditators
 *       around them, and the sample stories, initiatives, links and shop items.</li>
 *   <li>{@code remove}: deletes every sample row again (listed in demo_content). Countries and cities are real places
 *       and stay. Anything members or admins added is never touched.</li>
 *   <li>{@code off}: does nothing.</li>
 * </ul>
 * Sample listings say so in their description and have no phone numbers or emails; the site shows a banner while any exist.
 */
@Component
@Order(3)
public class DemoContentSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoContentSeeder.class);

    static final Set<String> PLACE_TYPES = Set.of("COUNTRY", "STATE", "CITY");

    private record WorldCity(String name, double lat, double lng) {
    }

    private record WorldState(String name, double lat, double lng, List<WorldCity> cities) {
    }

    private record WorldCountry(String name, String iso, double lat, double lng, List<WorldState> states) {
    }

    private static final List<String> INDIAN_FIRST = List.of("Priya", "Arjun", "Lakshmi", "Karthik", "Meera", "Ananya", "Rahul",
            "Divya", "Suresh", "Kavya", "Vikram", "Nandini", "Aditya", "Shreya", "Ganesh", "Pooja", "Rohan", "Deepa", "Manoj",
            "Sneha", "Harish", "Gayathri", "Sanjay", "Revathi", "Anand", "Bhavana", "Naveen", "Swathi", "Mahesh", "Keerthana");
    private static final List<String> INDIAN_LAST = List.of("Narayanan", "Krishnan", "Subramanian", "Venkatesh", "Raman", "Iyer",
            "Sharma", "Menon", "Kumar", "Reddy", "Rao", "Pillai", "Joshi", "Nair", "Murthy", "Patel", "Gupta", "Shankar", "Das",
            "Kulkarni", "Agarwal", "Bose", "Chatterjee", "Desai", "Mehta", "Singh", "Verma", "Hegde", "Shetty", "Sundaram");
    private static final List<String> WESTERN_FIRST = List.of("Daniel", "Emma", "Sarah", "Lukas", "Olivia", "Hannah", "James", "Mia",
            "Noah", "Isabella", "Sophie", "Thomas", "Laura", "David", "Anna", "Michael", "Julia", "Mark", "Claire", "Peter");
    private static final List<String> WESTERN_LAST = List.of("Weber", "Larsen", "Thompson", "Fischer", "Brown", "Schmidt", "Wilson",
            "Taylor", "Williams", "Rossi", "Martin", "Dubois", "Jansen", "Novak", "Murphy", "Walsh", "Moreau", "Bauer", "Clark", "Evans");
    private static final List<String> EAST_ASIAN_FIRST = List.of("Wei", "Mei", "Hiro", "Yuki", "Min-jun", "Ji-woo", "Li", "Hana", "Kenji", "Lin");
    private static final List<String> EAST_ASIAN_LAST = List.of("Chen", "Wang", "Tanaka", "Sato", "Kim", "Park", "Lee", "Zhang", "Suzuki", "Liu");
    private static final List<String> SLAVIC_FIRST = List.of("Anastasia", "Dmitri", "Olga", "Ivan", "Elena", "Sergei", "Natalia", "Pavel", "Irina", "Alexei");
    private static final List<String> SLAVIC_LAST = List.of("Ivanova", "Petrov", "Smirnova", "Volkov", "Kuznetsova", "Popov", "Sokolova", "Lebedev", "Novikova", "Morozov");
    private static final List<String> LATIN_FIRST = List.of("Carlos", "Maria", "Lucia", "Diego", "Sofia", "Mateo", "Camila", "Javier", "Valentina", "Pablo");
    private static final List<String> LATIN_LAST = List.of("Garcia", "Silva", "Rodriguez", "Fernandez", "Lopez", "Santos", "Gomez", "Oliveira", "Torres", "Ramirez");
    private static final List<String> MIDDLE_EAST_FIRST = List.of("Omar", "Layla", "Yusuf", "Amira", "Karim", "Nadia", "Samir", "Leila", "Tariq", "Zara");
    private static final List<String> MIDDLE_EAST_LAST = List.of("Haddad", "Rahman", "Khalil", "Nasser", "Saleh", "Farouk", "Aziz", "Mansour", "Hassan", "Darwish");
    private static final List<String> AFRICAN_FIRST = List.of("Amara", "Kwame", "Zanele", "Tendai", "Ayo", "Wanjiru", "Kofi", "Thandiwe", "Chidi", "Nia");
    private static final List<String> AFRICAN_LAST = List.of("Okafor", "Mensah", "Ndlovu", "Kamau", "Adeyemi", "Mutua", "Boateng", "Dlamini", "Otieno", "Banda");

    private static final Set<String> SOUTH_ASIA = Set.of("IN", "NP", "LK", "BD", "BT", "MV");
    private static final Set<String> DIASPORA = Set.of("US", "GB", "CA", "AU", "NZ", "SG", "MY", "AE", "QA", "OM", "KW", "BH", "SA", "MU", "FJ", "ZA", "KE", "TT", "GY");
    private static final Set<String> EAST_ASIA = Set.of("CN", "HK", "TW", "JP", "KR", "MN", "VN", "TH", "PH", "ID", "KH", "LA", "MM", "BN");
    private static final Set<String> SLAVIC = Set.of("RU", "UA", "BY", "PL", "CZ", "SK", "BG", "RS", "HR", "SI", "KZ", "UZ", "GE", "AM", "AZ", "LT", "LV", "EE");
    private static final Set<String> LATIN = Set.of("MX", "GT", "CR", "PA", "CU", "DO", "CO", "VE", "EC", "PE", "BO", "BR", "PY", "UY", "AR", "CL", "ES", "PT");
    private static final Set<String> MIDDLE_EAST = Set.of("EG", "MA", "TN", "TR", "IR", "IL");
    private static final Set<String> AFRICA = Set.of("NG", "GH", "CI", "CM", "ET", "UG", "RW", "TZ", "ZM", "ZW", "MZ", "BW", "NA", "SN", "MG", "SC");
    private static final Set<String> LARGE = Set.of("US", "GB", "CA", "AU", "SG", "MY", "AE", "DE");

    private static final List<String> PRACTICES = List.of("Isha Kriya", "Shambhavi Mahamudra", "Surya Kriya", "Upa Yoga",
            "Angamardana", "Bhuta Shuddhi", "Shoonya Meditation", "Hatha Yoga");
    private static final Map<String, String> LOCAL_LANGUAGE = Map.ofEntries(
            Map.entry("IN", "Hindi"), Map.entry("DE", "German"), Map.entry("FR", "French"), Map.entry("ES", "Spanish"),
            Map.entry("MX", "Spanish"), Map.entry("AR", "Spanish"), Map.entry("CO", "Spanish"), Map.entry("CL", "Spanish"),
            Map.entry("PE", "Spanish"), Map.entry("BR", "Portuguese"), Map.entry("PT", "Portuguese"), Map.entry("IT", "Italian"),
            Map.entry("RU", "Russian"), Map.entry("UA", "Ukrainian"), Map.entry("CN", "Mandarin"), Map.entry("TW", "Mandarin"),
            Map.entry("JP", "Japanese"), Map.entry("KR", "Korean"), Map.entry("NL", "Dutch"), Map.entry("TR", "Turkish"),
            Map.entry("AE", "Arabic"), Map.entry("SA", "Arabic"), Map.entry("EG", "Arabic"), Map.entry("MA", "French"),
            Map.entry("PL", "Polish"), Map.entry("TH", "Thai"), Map.entry("ID", "Indonesian"), Map.entry("MY", "Malay"),
            Map.entry("NP", "Nepali"), Map.entry("LK", "Sinhala"), Map.entry("BD", "Bengali"), Map.entry("GR", "Greek"));
    private static final Map<String, String> INDIA_STATE_LANGUAGE = Map.of(
            "Tamil Nadu", "Tamil", "Karnataka", "Kannada", "Kerala", "Malayalam", "Andhra Pradesh", "Telugu",
            "Telangana", "Telugu", "Maharashtra", "Marathi", "Gujarat", "Gujarati", "West Bengal", "Bengali", "Puducherry", "Tamil");

    private final String mode;
    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;
    private final AppUserRepository appUserRepository;
    private final SampleCommunitySeeder communitySeeder;

    public DemoContentSeeder(
            @Value("${app.demo-content:off}") String mode,
            JdbcTemplate jdbc,
            ObjectMapper objectMapper,
            AppUserRepository appUserRepository,
            SampleCommunitySeeder communitySeeder
    ) {
        this.mode = mode.trim().toLowerCase();
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
        this.appUserRepository = appUserRepository;
        this.communitySeeder = communitySeeder;
    }

    @Override
    @Transactional
    public void run(String... args) throws IOException {
        log.info("Preview sample data mode: {}", mode);
        switch (mode) {
            case "on" -> seed();
            case "remove" -> remove();
            case "off" -> {
            }
            default -> log.warn("Unknown app.demo-content '{}': expected on, remove or off. Doing nothing.", mode);
        }
    }

    private void seed() throws IOException {
        Long existing = jdbc.queryForObject("SELECT count(*) FROM demo_content WHERE entity_type NOT IN ('COUNTRY','STATE','CITY')", Long.class);
        if (existing != null && existing > 0) {
            log.info("Preview sample data is already in place ({} rows).", existing);
            return;
        }
        List<WorldCountry> world;
        try (InputStream in = new ClassPathResource("demo/world.json").getInputStream()) {
            world = objectMapper.readValue(in, new TypeReference<>() {
            });
        }
        Random random = new Random(2026);
        List<Long> programIds = jdbc.queryForList("SELECT id FROM program ORDER BY id", Long.class);
        List<Long> userIds = new ArrayList<>();
        int cities = 0, centres = 0, teachers = 0;

        for (WorldCountry country : world) {
            long countryId = countryId(country);
            boolean firstInCountry = true;
            for (WorldState state : country.states()) {
                long stateId = stateId(countryId, state);
                boolean firstInState = true;
                for (WorldCity city : state.cities()) {
                    long cityId = cityId(countryId, stateId, country.iso(), city);
                    String citySlug = jdbc.queryForObject("SELECT slug FROM city WHERE id = ?", String.class, cityId);
                    cities++;
                    String language = language(country.iso(), state.name());
                    boolean india = "IN".equals(country.iso());
                    boolean large = LARGE.contains(country.iso());

                    // Centres: every Indian city, the first city of each state in large countries, capitals elsewhere
                    Long realCentres = jdbc.queryForObject("SELECT count(*) FROM center WHERE city_id = ?", Long.class, cityId);
                    boolean wantsCentre = india || (large && firstInState) || firstInCountry || random.nextInt(3) == 0;
                    if (wantsCentre && realCentres != null && realCentres == 0) {
                        addCentre(cityId, citySlug, "CITY_CENTER", "Isha " + city.name() + " Centre", city, state, country, language, random, 1);
                        centres++;
                        if (india && random.nextInt(3) == 0) {
                            addCentre(cityId, citySlug, "ISHA_SHOP", "Isha Shop " + city.name(), city, state, country, language, random, 2);
                            centres++;
                        }
                        if (random.nextInt(india ? 12 : 30) == 0) {
                            addCentre(cityId, citySlug, "DEVI_TEMPLE", "Linga Bhairavi Abode, " + city.name(), city, state, country, language, random, 3);
                            centres++;
                        }
                    }

                    int teacherCount = india ? 1 + random.nextInt(4) : large ? (firstInState ? 1 : 0) + random.nextInt(2)
                            : (firstInCountry ? 1 : 0) + (random.nextInt(3) == 0 ? 1 : 0);
                    for (int t = 1; t <= teacherCount; t++) {
                        addTeacher(cityId, citySlug, t, city, country.iso(), language, programIds, random);
                        teachers++;
                    }

                    int meditatorCount = india ? 4 + random.nextInt(6) : large ? 2 + random.nextInt(4) : 1 + random.nextInt(3);
                    for (int m = 0; m < meditatorCount; m++) {
                        userIds.add(addMeditator(userIds.size() + 1, cityId, city, country.iso(), language, random));
                    }
                    firstInState = false;
                    firstInCountry = false;
                }
            }
        }

        List<AppUser> likers = appUserRepository.findAllById(userIds.subList(0, Math.min(60, userIds.size())));
        communitySeeder.seedCommunity(likers, this::record);
        log.warn("Added preview sample data: {} cities, {} centres, {} teachers, {} meditators, plus sample stories, "
                + "initiatives, links and shop items. Remove it all with DEMO_CONTENT=remove.", cities, centres, teachers, userIds.size());
    }

    private void remove() {
        String in = "(SELECT entity_id FROM demo_content WHERE entity_type = '%s')";
        String stories = in.formatted("STORY"), initiatives = in.formatted("INITIATIVE"), links = in.formatted("RESOURCE"),
                products = in.formatted("PRODUCT"), teachers = in.formatted("TEACHER"), centres = in.formatted("CENTER"), users = in.formatted("USER");
        int[] counts = {
                jdbc.update("DELETE FROM story_like WHERE story_id IN " + stories + " OR app_user_id IN " + users),
                jdbc.update("DELETE FROM initiative_supporter WHERE initiative_id IN " + initiatives + " OR app_user_id IN " + users),
                jdbc.update("DELETE FROM resource_vote WHERE resource_id IN " + links + " OR app_user_id IN " + users),
                jdbc.update("UPDATE story SET center_id = NULL WHERE center_id IN " + centres + " AND id NOT IN " + stories),
                jdbc.update("DELETE FROM story WHERE id IN " + stories),
                jdbc.update("DELETE FROM initiative WHERE id IN " + initiatives),
                jdbc.update("DELETE FROM resource_link WHERE id IN " + links),
                jdbc.update("DELETE FROM shop_listing WHERE id IN " + products),
                jdbc.update("DELETE FROM teacher_program WHERE teacher_id IN " + teachers),
                jdbc.update("DELETE FROM social_link WHERE (entity_type = 'TEACHER' AND entity_id IN " + teachers + ") OR (entity_type = 'CENTER' AND entity_id IN " + centres + ")"),
                jdbc.update("DELETE FROM media WHERE (entity_type = 'TEACHER' AND entity_id IN " + teachers + ") OR (entity_type = 'CENTER' AND entity_id IN " + centres + ")"),
                jdbc.update("DELETE FROM teacher WHERE id IN " + teachers),
                jdbc.update("DELETE FROM center WHERE id IN " + centres),
                jdbc.update("DELETE FROM meditator WHERE app_user_id IN " + users),
                jdbc.update("DELETE FROM app_user WHERE id IN " + users),
        };
        int listed = jdbc.update("DELETE FROM demo_content WHERE entity_type NOT IN ('COUNTRY','STATE','CITY')");
        log.warn("Removed preview sample data: {} rows listed, {} stories, {} initiatives, {} links, {} shop items, {} teachers, "
                        + "{} centres, {} members. Countries and cities stay.",
                listed, counts[4], counts[5], counts[6], counts[7], counts[11], counts[12], counts[14]);
    }

    private long countryId(WorldCountry c) {
        List<Long> found = jdbc.queryForList("SELECT id FROM country WHERE iso_code = ?", Long.class, c.iso());
        if (!found.isEmpty()) {
            return found.get(0);
        }
        long id = jdbc.queryForObject("INSERT INTO country (name, iso_code, latitude, longitude) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, c.name(), c.iso(), c.lat(), c.lng());
        record("COUNTRY", id);
        return id;
    }

    private long stateId(long countryId, WorldState s) {
        List<Long> found = jdbc.queryForList("SELECT id FROM state WHERE country_id = ? AND lower(name) = lower(?)", Long.class, countryId, s.name());
        if (!found.isEmpty()) {
            return found.get(0);
        }
        long id = jdbc.queryForObject("INSERT INTO state (country_id, name, latitude, longitude) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, countryId, s.name(), s.lat(), s.lng());
        record("STATE", id);
        return id;
    }

    /** Reuses a city already on the map (same slug, same country); otherwise adds it, with a country suffix if the slug is taken. */
    private long cityId(long countryId, long stateId, String iso, WorldCity c) {
        String slug = SlugGenerator.from(c.name());
        for (String candidate : List.of(slug, slug + "-" + iso.toLowerCase())) {
            List<Map<String, Object>> rows = jdbc.queryForList("SELECT id, country_id FROM city WHERE slug = ?", candidate);
            if (rows.isEmpty()) {
                long id = jdbc.queryForObject("""
                        INSERT INTO city (country_id, state_id, slug, name, latitude, longitude, active, listing_status)
                        VALUES (?, ?, ?, ?, ?, ?, TRUE, 'APPROVED') RETURNING id""",
                        Long.class, countryId, stateId, candidate, c.name(), c.lat(), c.lng());
                record("CITY", id);
                return id;
            }
            if (((Number) rows.get(0).get("country_id")).longValue() == countryId) {
                return ((Number) rows.get(0).get("id")).longValue();
            }
        }
        throw new IllegalStateException("No free slug for city " + c.name());
    }

    private void addCentre(long cityId, String citySlug, String type, String name, WorldCity city, WorldState state,
                           WorldCountry country, String language, Random random, int n) {
        double[] p = near(city, 0.5, 4, random);
        long id = jdbc.queryForObject("""
                INSERT INTO center (city_id, slug, name, type, description, address, latitude, longitude, opening_hours, languages,
                                    verified, listing_status, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, 'APPROVED', TRUE) RETURNING id""",
                Long.class, cityId, "sample-centre-" + citySlug + "-" + n, name, type,
                "Sample listing for the site preview. This is not a real centre.",
                city.name() + ", " + state.name() + ", " + country.name(), p[0], p[1],
                "DEVI_TEMPLE".equals(type) ? "Daily, 6:00 am – 8:00 pm" : "Sat–Sun, 6:00–8:00 am",
                language.equals("English") ? "English" : "English, " + language);
        record("CENTER", id);
    }

    private void addTeacher(long cityId, String citySlug, int n, WorldCity city, String iso, String language,
                            List<Long> programIds, Random random) {
        String name = fullName(iso, random);
        double[] p = near(city, 1, 8, random);
        String mode = List.of("IN_PERSON", "IN_PERSON", "ONLINE", "BOTH").get(random.nextInt(4));
        long id = jdbc.queryForObject("""
                INSERT INTO teacher (city_id, slug, name, bio, latitude, longitude, teaching_mode, languages, years_teaching,
                                     verified, listing_status, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, 'APPROVED', TRUE) RETURNING id""",
                Long.class, cityId, "sample-teacher-" + citySlug + "-" + n, name,
                "Sample listing for the site preview. This is not a real teacher.", p[0], p[1], mode,
                language.equals("English") ? "English" : "English, " + language, 1 + random.nextInt(15));
        List<Long> shuffled = new ArrayList<>(programIds);
        java.util.Collections.shuffle(shuffled, random);
        for (Long programId : shuffled.subList(0, Math.min(shuffled.size(), 1 + random.nextInt(3)))) {
            jdbc.update("INSERT INTO teacher_program (teacher_id, program_id) VALUES (?, ?)", id, programId);
        }
        record("TEACHER", id);
    }

    private long addMeditator(int n, long cityId, WorldCity city, String iso, String language, Random random) {
        String name = fullName(iso, random);
        long userId = jdbc.queryForObject("INSERT INTO app_user (email, name, active) VALUES (?, ?, TRUE) RETURNING id",
                Long.class, "sample-member-" + n + "@example.org", name);
        record("USER", userId);
        List<String> practices = PRACTICES.stream().filter(x -> random.nextInt(3) == 0).limit(4).toList();
        String[] parts = name.split(" ");
        double[] p = near(city, 1.5, 6, random);
        jdbc.update("""
                INSERT INTO meditator (app_user_id, city_id, display_name, approx_latitude, approx_longitude, practices, languages,
                                       open_to_connect, open_to_volunteer, visible, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)""",
                userId, cityId, parts[0] + " " + parts[parts.length - 1].charAt(0) + ".", p[0], p[1],
                String.join(",", practices.isEmpty() ? List.of("Isha Kriya") : practices),
                language.equals("English") ? "English" : "English, " + language,
                random.nextInt(3) > 0, random.nextBoolean(), random.nextInt(10) != 0);
        return userId;
    }

    private void record(String type, Long id) {
        jdbc.update("INSERT INTO demo_content (entity_type, entity_id) VALUES (?, ?) ON CONFLICT DO NOTHING", type, id);
    }

    /** A point a few km from the city centre, rounded like real approximate locations. */
    private static double[] near(WorldCity c, double minKm, double maxKm, Random random) {
        double km = minKm + random.nextDouble() * (maxKm - minKm);
        double bearing = random.nextDouble() * 2 * Math.PI;
        double lat = c.lat() + km * Math.cos(bearing) / 111.32;
        double lng = c.lng() + km * Math.sin(bearing) / (111.32 * Math.max(0.2, Math.cos(Math.toRadians(c.lat()))));
        return new double[]{Math.round(lat * 1e6) / 1e6, Math.round(lng * 1e6) / 1e6};
    }

    private static String language(String iso, String state) {
        if ("IN".equals(iso)) {
            return INDIA_STATE_LANGUAGE.getOrDefault(state, "Hindi");
        }
        return LOCAL_LANGUAGE.getOrDefault(iso, "English");
    }

    private static String fullName(String iso, Random random) {
        List<String> first, last;
        if (SOUTH_ASIA.contains(iso) || (DIASPORA.contains(iso) && random.nextBoolean())) {
            first = INDIAN_FIRST;
            last = INDIAN_LAST;
        } else if (EAST_ASIA.contains(iso)) {
            first = EAST_ASIAN_FIRST;
            last = EAST_ASIAN_LAST;
        } else if (SLAVIC.contains(iso)) {
            first = SLAVIC_FIRST;
            last = SLAVIC_LAST;
        } else if (LATIN.contains(iso)) {
            first = LATIN_FIRST;
            last = LATIN_LAST;
        } else if (MIDDLE_EAST.contains(iso) || Set.of("AE", "SA", "QA", "OM", "KW", "BH").contains(iso)) {
            first = MIDDLE_EAST_FIRST;
            last = MIDDLE_EAST_LAST;
        } else if (AFRICA.contains(iso)) {
            first = AFRICAN_FIRST;
            last = AFRICAN_LAST;
        } else {
            first = WESTERN_FIRST;
            last = WESTERN_LAST;
        }
        return first.get(random.nextInt(first.size())) + " " + last.get(random.nextInt(last.size()));
    }
}
