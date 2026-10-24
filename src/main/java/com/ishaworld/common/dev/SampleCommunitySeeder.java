package com.ishaworld.common.dev;

import com.ishaworld.city.entity.City;
import com.ishaworld.city.repository.CityRepository;
import com.ishaworld.common.enums.InitiativeKind;
import com.ishaworld.common.enums.ListingStatus;
import com.ishaworld.common.enums.ResourceCategory;
import com.ishaworld.initiative.entity.Initiative;
import com.ishaworld.initiative.repository.InitiativeRepository;
import com.ishaworld.resourcelink.entity.ResourceLink;
import com.ishaworld.resourcelink.repository.ResourceLinkRepository;
import com.ishaworld.user.entity.AppUser;
import com.ishaworld.user.repository.AppUserRepository;
import java.util.List;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Local development only: sample initiatives (with supporters) and resource links, so those pages have
 * something to show. Off unless SEED_SAMPLE_DATA=true; runs after the sample meditators exist.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.dev.seed-sample-data", havingValue = "true")
public class SampleCommunitySeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SampleCommunitySeeder.class);

    private record SampleInitiative(String slug, String title, InitiativeKind kind, String summary, String description,
                                    String citySlug, int year, String by, String needs, String needsNote,
                                    String whatsapp, String email, String website, String photo) {
    }

    private static final String UNSPLASH = "https://images.unsplash.com/photo-%s?w=900&q=70&auto=format&fit=crop";

    private static final List<SampleInitiative> INITIATIVES = List.of(
            new SampleInitiative("saplings-for-school-grounds", "Saplings for School Grounds", InitiativeKind.ENVIRONMENT,
                    "Turning bare government-school compounds into shaded green spaces, one native sapling at a time.",
                    "We plant native, fast-shading trees along school boundaries and teach the students to care for them. Each class adopts a few saplings and waters them through the dry months.\n\nSo far around 1,400 saplings across 9 schools, with roughly 80% surviving their first summer.",
                    "coimbatore", 2023, "Karthik V.", "VOLUNTEERS,MATERIALS",
                    "Weekend volunteers for planting drives, plus tree guards and drip-irrigation kits.",
                    "919800000001", "saplings@example.org", null, "1542601906990-b4d3fb778b09"),
            new SampleInitiative("sunday-yoga-in-the-park", "Sunday Yoga in the Park", InitiativeKind.WELLBEING,
                    "Free simple yoga practices every Sunday morning for anyone who walks into Cubbon Park.",
                    "Every Sunday at 6:30 am we roll out mats under the trees and guide simple practices and a short meditation, free and open to all.",
                    "bengaluru", 2022, "Arjun K.", "VOLUNTEERS,MATERIALS",
                    "Volunteers to set up and welcome people, and spare mats for first-timers.",
                    "919800000002", null, "https://example.org/sunday-yoga", "1601925260368-ae2f83cf8b7f"),
            new SampleInitiative("evening-tuition-for-village-children", "Evening Tuition for Village Children", InitiativeKind.EDUCATION,
                    "After-school maths, English and reading support for children in three villages near the Velliangiri foothills.",
                    "Most children here are the first in their families to finish school. Volunteers run two-hour sessions, five evenings a week, in a borrowed community hall.\n\nAround 70 children attend regularly.",
                    "coimbatore", 2021, "Lakshmi S.", "VOLUNTEERS,FUNDS,MATERIALS",
                    "Teachers (even one evening a week, online works too), notebooks and a small fund for evening snacks.",
                    "919800000003", "tuition@example.org", null, "1497486751825-1233686d5d80"),
            new SampleInitiative("friday-meals", "Friday Meals", InitiativeKind.SEVA,
                    "A warm vegetarian meal every Friday evening for anyone in the neighbourhood who needs one.",
                    "A small team cooks a simple vegetarian meal in a community kitchen and serves whoever comes, no questions asked. Between 80 and 150 people each week.",
                    "berlin", 2024, "Daniel W.", "VOLUNTEERS,SPACE",
                    "Cooks and servers on Friday afternoons, and a larger kitchen for the winter months.",
                    null, "fridaymeals@example.org", "https://example.org/friday-meals", "1593113598332-cd288d649433"),
            new SampleInitiative("native-seed-library", "Native Seed Library", InitiativeKind.ENVIRONMENT,
                    "Saving and sharing heirloom and native seeds with home gardeners and small farmers.",
                    "We collect, dry and catalogue seeds from local gardens and lend them out for free. Borrowers return a handful from their harvest. Over 220 varieties so far.",
                    "mcminnville", 2023, "Sarah T.", "SKILLS,VOLUNTEERS",
                    "Someone with cataloguing or database skills, and helpers for our spring seed swap.",
                    null, "seeds@example.org", null, "1466692476868-aef1dfb1e735"),
            new SampleInitiative("kitchen-garden-collective", "Kitchen Garden Collective", InitiativeKind.VENTURE,
                    "A small social enterprise selling organic vegetables grown on shared suburban plots.",
                    "We turn unused backyards into kitchen gardens. Owners get a share of the harvest; the rest is sold in weekly boxes. Profits fund free gardening workshops for schools.",
                    "melbourne", 2024, "Emma L.", "SPACE,FUNDS",
                    "More backyards to grow in, and early supporters for a delivery van.",
                    null, "garden@example.org", "https://example.org/kitchen-garden", "1464226184884-fa280b87c399"),
            new SampleInitiative("elder-care-visits", "Elder Care Visits", InitiativeKind.SEVA,
                    "Volunteers spend an afternoon a week with elders living alone: conversation, errands and a little chanting.",
                    "Many elders live alone while their children work abroad. Volunteers are paired with one elder and visit weekly to talk, help with errands and sometimes chant together.",
                    "bengaluru", 2022, "Priya N.", "VOLUNTEERS",
                    "Kind, patient people who can commit to one afternoon a week for at least three months.",
                    "919800000004", "eldercare@example.org", null, "1469571486292-0ba58a3f068b"));

    private record SampleLink(String title, String url, ResourceCategory category, String description) {
    }

    private static final List<SampleLink> LINKS = List.of(
            new SampleLink("Volunteer with Isha", "https://isha.sadhguru.org/in/en/volunteer", ResourceCategory.SEVA,
                    "Official page for full-time, part-time and event volunteering at Isha."),
            new SampleLink("Yoga and meditation programmes", "https://isha.sadhguru.org/in/en/yoga-meditation", ResourceCategory.PROGRAMMES,
                    "All Isha programmes in one place, from free guided meditations to Inner Engineering."),
            new SampleLink("Inner Engineering", "https://www.innerengineering.com", ResourceCategory.PROGRAMMES,
                    "Sadhguru's foundational programme for wellbeing, online and in person."),
            new SampleLink("Miracle of Mind app", "https://isha.sadhguru.org/in/en/miracle-of-mind", ResourceCategory.APPS,
                    "Free 7-minute guided meditations and a daily practice tracker."),
            new SampleLink("Sadhguru on YouTube", "https://www.youtube.com/@sadhguru", ResourceCategory.VIDEOS,
                    "Talks, Q&As and guided practices."),
            new SampleLink("Wisdom articles", "https://isha.sadhguru.org/in/en/wisdom", ResourceCategory.READING,
                    "Articles, quotes and podcasts on every topic you can think of."),
            new SampleLink("Free Sadhguru downloads", "https://isha.sadhguru.org/in/en/free-sadhguru-downloads", ResourceCategory.VIDEOS,
                    "Videos you may download and share, with Isha's usage terms."),
            new SampleLink("Linga Bhairavi", "https://lingabhairavi.org", ResourceCategory.READING,
                    "About Devi, offerings and Linga Bhairavi temples."),
            new SampleLink("Save Soil / Conscious Planet", "https://consciousplanet.org", ResourceCategory.SEVA,
                    "The global movement to save soil, with ways to take part."),
            new SampleLink("Cauvery Calling", "https://www.cauverycalling.org", ResourceCategory.SEVA,
                    "Farmer-led tree planting in the Cauvery basin; volunteer or plant a tree."),
            new SampleLink("Isha Outreach", "https://ishaoutreach.org", ResourceCategory.SEVA,
                    "Rural health, education and ecology projects you can support."),
            new SampleLink("Isha Yoga Centre, Coimbatore", "https://isha.sadhguru.org/in/en/center/isha-yoga-center-coimbatore",
                    ResourceCategory.PROGRAMMES, "Visiting the ashram: timings, how to reach and what to see."));

    private final InitiativeRepository initiativeRepository;
    private final ResourceLinkRepository resourceLinkRepository;
    private final CityRepository cityRepository;
    private final AppUserRepository appUserRepository;

    public SampleCommunitySeeder(
            InitiativeRepository initiativeRepository,
            ResourceLinkRepository resourceLinkRepository,
            CityRepository cityRepository,
            AppUserRepository appUserRepository
    ) {
        this.initiativeRepository = initiativeRepository;
        this.resourceLinkRepository = resourceLinkRepository;
        this.cityRepository = cityRepository;
        this.appUserRepository = appUserRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<AppUser> sampleUsers = appUserRepository.findAll().stream()
                .filter(u -> u.getEmail().startsWith("sample-meditator-")).toList();
        Random random = new Random(108);

        if (initiativeRepository.count() == 0) {
            for (SampleInitiative s : INITIATIVES) {
                City city = cityRepository.findBySlug(s.citySlug()).orElse(null);
                if (city == null) {
                    continue;
                }
                Initiative i = new Initiative();
                i.setSlug(s.slug());
                i.setTitle(s.title());
                i.setKind(s.kind());
                i.setSummary(s.summary());
                i.setDescription(s.description());
                i.setCity(city);
                i.setLatitude(city.getLatitude());
                i.setLongitude(city.getLongitude());
                i.setStartedYear(s.year());
                i.setStartedByName(s.by());
                i.setNeeds(s.needs());
                i.setNeedsNote(s.needsNote());
                i.setContactWhatsapp(s.whatsapp());
                i.setContactEmail(s.email());
                i.setContactWebsite(s.website());
                i.setPhotoUrl(UNSPLASH.formatted(s.photo()));
                i.setListingStatus(ListingStatus.APPROVED);
                i = initiativeRepository.save(i);
                for (AppUser u : sampleUsers) {
                    if (random.nextInt(4) == 0) {
                        initiativeRepository.addSupporter(i.getId(), u.getId());
                    }
                }
            }
            log.warn("[DEV] Seeded sample initiatives");
        }

        if (resourceLinkRepository.count() == 0) {
            for (SampleLink s : LINKS) {
                ResourceLink r = new ResourceLink();
                r.setTitle(s.title());
                r.setUrl(s.url());
                r.setCategory(s.category());
                r.setDescription(s.description());
                r.setListingStatus(ListingStatus.APPROVED);
                r = resourceLinkRepository.save(r);
                for (AppUser u : sampleUsers) {
                    if (random.nextInt(6) == 0) {
                        resourceLinkRepository.addVote(r.getId(), u.getId());
                    }
                }
            }
            log.warn("[DEV] Seeded sample resource links");
        }
    }
}
