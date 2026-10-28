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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    /** One city to fill, with where it sits in the country and state. */
    private record Place(WorldCountry country, WorldState state, WorldCity city, String slug, boolean firstInCountry, boolean firstInState) {
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

    /**
     * Builds every row in memory and writes them in a few multi-row INSERTs: a handful of round trips to the database
     * instead of thousands, so it stays quick even when the database is far from the app.
     */
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
        List<Object[]> recorded = new ArrayList<>();
        Random random = new Random(2026);
        List<Long> programIds = jdbc.queryForList("SELECT id FROM program ORDER BY id", Long.class);

        // 1. Places: reuse what is already on the map, add the rest.
        Map<String, Long> countryIds = new HashMap<>();
        jdbc.query("SELECT id, iso_code FROM country", rs -> {
            countryIds.put(rs.getString("iso_code"), rs.getLong("id"));
        });
        List<Object[]> newCountries = new ArrayList<>();
        for (WorldCountry c : world) {
            if (!countryIds.containsKey(c.iso())) {
                newCountries.add(new Object[]{c.name(), c.iso(), c.lat(), c.lng()});
            }
        }
        for (Map<String, Object> row : insert("country", "name, iso_code, latitude, longitude", newCountries, "id, iso_code")) {
            countryIds.put((String) row.get("iso_code"), number(row, "id"));
            recorded.add(new Object[]{"COUNTRY", number(row, "id")});
        }

        Map<String, Long> stateIds = new HashMap<>();
        jdbc.query("SELECT id, country_id, name FROM state", rs -> {
            stateIds.put(rs.getLong("country_id") + "|" + rs.getString("name").toLowerCase(), rs.getLong("id"));
        });
        List<Object[]> newStates = new ArrayList<>();
        for (WorldCountry c : world) {
            for (WorldState s : c.states()) {
                String key = countryIds.get(c.iso()) + "|" + s.name().toLowerCase();
                if (!stateIds.containsKey(key)) {
                    stateIds.put(key, -1L);
                    newStates.add(new Object[]{countryIds.get(c.iso()), s.name(), s.lat(), s.lng()});
                }
            }
        }
        for (Map<String, Object> row : insert("state", "country_id, name, latitude, longitude", newStates, "id, country_id, name")) {
            stateIds.put(number(row, "country_id") + "|" + ((String) row.get("name")).toLowerCase(), number(row, "id"));
            recorded.add(new Object[]{"STATE", number(row, "id")});
        }

        Map<String, long[]> citiesBySlug = new HashMap<>(); // slug -> {id, countryId}
        jdbc.query("SELECT id, slug, country_id FROM city", rs -> {
            citiesBySlug.put(rs.getString("slug"), new long[]{rs.getLong("id"), rs.getLong("country_id")});
        });
        List<Place> places = new ArrayList<>();
        List<Object[]> newCities = new ArrayList<>();
        for (WorldCountry c : world) {
            long countryId = countryIds.get(c.iso());
            boolean firstInCountry = true;
            for (WorldState s : c.states()) {
                long stateId = stateIds.get(countryId + "|" + s.name().toLowerCase());
                boolean firstInState = true;
                for (WorldCity city : s.cities()) {
                    String base = SlugGenerator.from(city.name());
                    String slug = null;
                    for (String candidate : List.of(base, base + "-" + c.iso().toLowerCase())) {
                        long[] found = citiesBySlug.get(candidate);
                        if (found == null) {
                            citiesBySlug.put(candidate, new long[]{-1, countryId});
                            newCities.add(new Object[]{countryId, stateId, candidate, city.name(), city.lat(), city.lng()});
                            slug = candidate;
                            break;
                        }
                        if (found[1] == countryId) {
                            slug = candidate;
                            break;
                        }
                    }
                    if (slug == null) {
                        throw new IllegalStateException("No free slug for city " + city.name());
                    }
                    places.add(new Place(c, s, city, slug, firstInCountry, firstInState));
                    firstInCountry = false;
                    firstInState = false;
                }
            }
        }
        for (Map<String, Object> row : insert("city", "country_id, state_id, slug, name, latitude, longitude", newCities, "id, slug")) {
            citiesBySlug.get((String) row.get("slug"))[0] = number(row, "id");
            recorded.add(new Object[]{"CITY", number(row, "id")});
        }
        Set<Long> citiesWithCentres = new HashSet<>(jdbc.queryForList("SELECT DISTINCT city_id FROM center", Long.class));

        // 2. People and places of practice around each city.
        List<Object[]> centres = new ArrayList<>();
        List<Object[]> teachers = new ArrayList<>();
        Map<String, List<Long>> teacherPrograms = new HashMap<>();
        List<Object[]> users = new ArrayList<>();
        List<Object[]> meditators = new ArrayList<>(); // filled with the user's email in slot 0, swapped for its id later
        for (Place place : places) {
            long cityId = citiesBySlug.get(place.slug())[0];
            WorldCity city = place.city();
            String iso = place.country().iso();
            String language = language(iso, place.state().name());
            String languages = language.equals("English") ? "English" : "English, " + language;
            boolean india = "IN".equals(iso);
            boolean large = LARGE.contains(iso);

            boolean wantsCentre = india || (large && place.firstInState()) || place.firstInCountry() || random.nextInt(3) == 0;
            if (wantsCentre && !citiesWithCentres.contains(cityId)) {
                centres.add(centre(cityId, place.slug(), "CITY_CENTER", "Isha " + city.name() + " Centre", place, languages, random, 1));
                if (india && random.nextInt(3) == 0) {
                    centres.add(centre(cityId, place.slug(), "ISHA_SHOP", "Isha Shop " + city.name(), place, languages, random, 2));
                }
                if (random.nextInt(india ? 12 : 30) == 0) {
                    centres.add(centre(cityId, place.slug(), "DEVI_TEMPLE", "Linga Bhairavi Abode, " + city.name(), place, languages, random, 3));
                }
            }

            int teacherCount = india ? 1 + random.nextInt(4) : large ? (place.firstInState() ? 1 : 0) + random.nextInt(2)
                    : (place.firstInCountry() ? 1 : 0) + (random.nextInt(3) == 0 ? 1 : 0);
            for (int t = 1; t <= teacherCount; t++) {
                String slug = "sample-teacher-" + place.slug() + "-" + t;
                double[] p = near(city, 1, 8, random);
                teachers.add(new Object[]{cityId, slug, fullName(iso, random), "Sample listing for the site preview. This is not a real teacher.",
                        p[0], p[1], List.of("IN_PERSON", "IN_PERSON", "ONLINE", "BOTH").get(random.nextInt(4)), languages, 1 + random.nextInt(15)});
                List<Long> shuffled = new ArrayList<>(programIds);
                Collections.shuffle(shuffled, random);
                teacherPrograms.put(slug, shuffled.subList(0, Math.min(shuffled.size(), 1 + random.nextInt(3))));
            }

            int meditatorCount = india ? 4 + random.nextInt(6) : large ? 2 + random.nextInt(4) : 1 + random.nextInt(3);
            for (int m = 0; m < meditatorCount; m++) {
                String name = fullName(iso, random);
                String email = "sample-member-" + (users.size() + 1) + "@example.org";
                users.add(new Object[]{email, name});
                List<String> practices = PRACTICES.stream().filter(x -> random.nextInt(3) == 0).limit(4).toList();
                String[] parts = name.split(" ");
                double[] p = near(city, 1.5, 6, random);
                meditators.add(new Object[]{email, cityId, parts[0] + " " + parts[parts.length - 1].charAt(0) + ".", p[0], p[1],
                        String.join(",", practices.isEmpty() ? List.of("Isha Kriya") : practices), languages,
                        random.nextInt(3) > 0, random.nextBoolean(), random.nextInt(10) != 0});
            }
        }

        for (Map<String, Object> row : insert("center",
                "city_id, slug, name, type, description, address, latitude, longitude, opening_hours, languages, verified, listing_status, active",
                centres, "id")) {
            recorded.add(new Object[]{"CENTER", number(row, "id")});
        }
        List<Object[]> programRows = new ArrayList<>();
        for (Map<String, Object> row : insert("teacher",
                "city_id, slug, name, bio, latitude, longitude, teaching_mode, languages, years_teaching",
                teachers, "id, slug")) {
            long id = number(row, "id");
            recorded.add(new Object[]{"TEACHER", id});
            for (Long programId : teacherPrograms.get((String) row.get("slug"))) {
                programRows.add(new Object[]{id, programId});
            }
        }
        insert("teacher_program", "teacher_id, program_id", programRows, null);
        Map<String, Long> userIds = new HashMap<>();
        List<Long> userIdList = new ArrayList<>();
        for (Map<String, Object> row : insert("app_user", "email, name", users, "id, email")) {
            long id = number(row, "id");
            userIds.put((String) row.get("email"), id);
            userIdList.add(id);
            recorded.add(new Object[]{"USER", id});
        }
        for (Object[] m : meditators) {
            m[0] = userIds.get((String) m[0]);
        }
        insert("meditator", "app_user_id, city_id, display_name, approx_latitude, approx_longitude, practices, languages, "
                + "open_to_connect, open_to_volunteer, visible", meditators, null);

        // 3. Stories, initiatives, links and shop items, liked and supported by some of the sample members.
        Collections.sort(userIdList);
        List<AppUser> likers = appUserRepository.findAllById(userIdList.subList(0, Math.min(25, userIdList.size())));
        communitySeeder.seedCommunity(likers, (type, id) -> recorded.add(new Object[]{type, id}));

        insert("demo_content", "entity_type, entity_id", recorded, null);
        log.warn("Added preview sample data: {} new countries, {} new cities, {} centres, {} teachers, {} members, plus sample stories, "
                        + "initiatives, links and shop items. Remove it all with DEMO_CONTENT=remove.",
                newCountries.size(), newCities.size(), centres.size(), teachers.size(), users.size());
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


    private static Object[] centre(long cityId, String citySlug, String type, String name, Place place, String languages,
                                   Random random, int n) {
        double[] at = near(place.city(), 0.5, 4, random);
        return new Object[]{cityId, "sample-centre-" + citySlug + "-" + n, name, type,
                "Sample listing for the site preview. This is not a real centre.",
                place.city().name() + ", " + place.state().name() + ", " + place.country().name(), at[0], at[1],
                "DEVI_TEMPLE".equals(type) ? "Daily, 6:00 am – 8:00 pm" : "Sat–Sun, 6:00–8:00 am", languages, false, "APPROVED", true};
    }

    /** Multi-row INSERT in chunks; returns the RETURNING rows (empty when {@code returning} is null). */
    private List<Map<String, Object>> insert(String table, String columns, List<Object[]> rows, String returning) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (rows.isEmpty()) {
            return out;
        }
        int width = rows.get(0).length;
        String tuple = "(" + String.join(", ", Collections.nCopies(width, "?")) + ")";
        int chunk = Math.max(1, 5000 / width);
        for (int from = 0; from < rows.size(); from += chunk) {
            List<Object[]> part = rows.subList(from, Math.min(rows.size(), from + chunk));
            String sql = "INSERT INTO " + table + " (" + columns + ") VALUES " + String.join(", ", Collections.nCopies(part.size(), tuple))
                    + (returning == null ? "" : " RETURNING " + returning);
            Object[] args = part.stream().flatMap(Arrays::stream).toArray();
            if (returning == null) {
                jdbc.update(sql, args);
            } else {
                out.addAll(jdbc.queryForList(sql, args));
            }
        }
        return out;
    }

    private static long number(Map<String, Object> row, String column) {
        return ((Number) row.get(column)).longValue();
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
