# Deploying Isha Sangha (free setup)

Everything below runs on free plans. The only optional cost is a custom domain (about ₹800–1,000 a year);
without one the site lives at free addresses like `isha-sangha.pages.dev`.

| Piece | Service (free plan) | Notes |
|---|---|---|
| Website (frontend) | **Cloudflare Pages** | Builds from GitHub on every push to `main` |
| API (this repo) | **Render**, free web service, region Singapore | Built from the `Dockerfile` |
| Keep the API awake | **UptimeRobot** | Pings every 5 minutes so visitors never wait for a cold start |
| Database | **Neon** PostgreSQL, region Singapore | 0.5 GB, plenty for tens of thousands of members |
| Photos and proofs | **Cloudflare R2** | 10 GB, private bucket |
| Sign-in emails | **Brevo** | 300 emails a day |
| Weekly backup | **GitHub Actions** → R2 | `.github/workflows/db-backup.yml` |

Free plans change their limits from time to time; check each one when you sign up. Nothing in the code is tied to
these providers, so any piece can move to a paid plan or another provider later by changing settings only.

Keep a note of every value you create below (a password manager is ideal). You'll paste them into Render in step 6.

## 1. Cloudflare account

Create a free account at cloudflare.com. It is used for the website (Pages) and file storage (R2).
If you buy a domain, add it to Cloudflare too so DNS and SSL are handled in one place.

## 2. File storage: Cloudflare R2

1. Cloudflare dashboard → **R2** → enable R2 (asks for a card for identity; the free 10 GB is not charged).
2. **Create bucket** named `isha-sangha-uploads`. Leave public access **off**.
3. **Manage R2 API tokens → Create API token**, permission *Object Read & Write*, limited to that bucket.
4. Note: **Access Key ID**, **Secret Access Key**, and the **S3 endpoint** (`https://<account-id>.r2.cloudflarestorage.com`).
5. Optional but recommended: bucket **Settings → Object lifecycle rules**: delete objects with prefix `backups/` after 90 days.

Photos go under `images/`, proof documents under `proofs/`, database backups under `backups/`.
Nothing is publicly readable; the API decides who sees what (proofs only reach admins).

## 3. Database: Neon

1. Sign up at neon.tech, create a project in region **AWS Asia Pacific (Singapore)**, PostgreSQL 17.
2. From **Connect**, copy the connection details. You need two forms of the same thing:
   - For the API (JDBC): `jdbc:postgresql://<host>/<dbname>?sslmode=require`, plus the user and password separately.
   - For backups: `postgresql://<user>:<password>@<host>/<dbname>?sslmode=require`.

Tables are created automatically when the API first starts. The demo map data from development is removed
automatically in production (migration V13).

Neon puts the database to sleep when idle and wakes it in about a second. The API is set up to close idle
connections so this works.

## 4. Email: Brevo

1. Sign up at brevo.com. Under **Senders, Domains & Dedicated IPs**, add the address you will send from
   (e.g. `no-reply@<your domain>`, or your own email if you have no domain yet) and verify it.
   With a domain, also authenticate it (Brevo gives DNS records to add in Cloudflare).
2. **SMTP & API → SMTP**: note the server (`smtp-relay.brevo.com`), port `587`, login, and create an **SMTP key** (the password).

Without working email, members can't sign in with a code (Google sign-in still works).

## 5. Google sign-in

Google Cloud Console → **APIs & Services → Credentials → Create credentials → OAuth client ID** (Web application).
Under *Authorised JavaScript origins* add the site address (e.g. `https://isha-sangha.pages.dev`, plus your domain later).
Note the **Client ID**.

## 6. API: Render

1. Sign up at render.com with GitHub. **New → Web Service** → pick the `isha-sangha-backend` repo.
2. Runtime **Docker**, region **Singapore**, instance type **Free**, branch `main`.
3. **Health check path**: `/api/v1/health`.
4. Add these environment variables:

| Variable | Value |
|---|---|
| `DB_URL` | `jdbc:postgresql://<neon host>/<dbname>?sslmode=require` |
| `DB_USERNAME`, `DB_PASSWORD` | from Neon |
| `JWT_SECRET` | a long random value: run `openssl rand -base64 48` in Terminal |
| `FRONTEND_ORIGIN` | the site address, e.g. `https://isha-sangha.pages.dev` (several allowed, comma-separated) |
| `ADMIN_DEFAULT_EMAIL` | your admin email |
| `ADMIN_DEFAULT_PASSWORD` | a strong password, 12+ characters. Used only to create the first admin; delete it after step 9 |
| `GOOGLE_CLIENT_ID` | from step 5 |
| `MAIL_HOST` | `smtp-relay.brevo.com` |
| `MAIL_PORT` | `587` |
| `MAIL_USERNAME`, `MAIL_PASSWORD` | Brevo SMTP login and SMTP key |
| `OTP_FROM_EMAIL` | the verified sender from step 4 |
| `SITE_CONTACT_EMAIL` | the public contact address |
| `STORAGE_BUCKET` | `isha-sangha-uploads` |
| `STORAGE_ENDPOINT` | the R2 S3 endpoint |
| `STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY` | the R2 token |

`SPRING_PROFILES_ACTIVE=prod` and memory settings for the 512 MB free instance are already in the `Dockerfile`.

5. **Create Web Service**. The first build takes a few minutes. When it is live, Render shows the address,
   e.g. `https://isha-sangha-api.onrender.com`. Open `/api/v1/health` on it: it should say `UP`.

**If it doesn't start**, read the log. In production the API refuses to start with unsafe settings
(weak `JWT_SECRET`, the sample admin password, a localhost `FRONTEND_ORIGIN`, no email) and says exactly what to fix.

## 7. Keep the API awake: UptimeRobot

Render's free instances sleep after 15 minutes without visitors, and the next visitor waits about a minute.
At uptimerobot.com (free), add an **HTTP(s) monitor** for `https://<your api>.onrender.com/api/v1/health`
every **5 minutes**. One always-on service stays within Render's 750 free hours a month.
This ping does not touch the database, so Neon still sleeps when nobody is using the site.

## 8. Website: Cloudflare Pages

1. Cloudflare → **Workers & Pages → Create → Pages → Connect to Git** → pick `isha-sangha-frontend`.
2. Framework preset **Vite** (build command `npm run build`, output folder `dist`).
3. Environment variable `VITE_API_URL` = `https://<your api>.onrender.com/api/v1`.
4. Deploy. The site appears at `https://<project>.pages.dev`. Make sure this exact address is in the API's
   `FRONTEND_ORIGIN` and in Google's authorised origins.

`public/_redirects` makes page links work on refresh; `public/_headers` sets caching and basic security headers.

## 9. Check everything

- [ ] Site opens, the globe and map load.
- [ ] Sign in at `/admin/login` with the admin account. Then in Render **delete `ADMIN_DEFAULT_PASSWORD`**.
- [ ] Sign in as a member with an email code, and with Google.
- [ ] On your phone, open `https://<your api>.onrender.com/api/v1/health/client-ip` and compare `seenAs` with your
      phone's address (search "what is my IP"). If `seenAs` is a Render proxy address instead, sign-in limits would be shared by
      everyone: set `SERVER_TOMCAT_REMOTEIP_INTERNALPROXIES` to a pattern matching that proxy address and redeploy.
- [ ] Submit a centre with a proof file, approve it in `/admin/review`, check it shows on the map.
- [ ] Upload a photo with an initiative and check it shows.

## 10. Weekly backups

Neon's free plan only keeps a short restore window, so a full copy goes to R2 every week.

In GitHub: `isha-sangha-backend` → **Settings → Secrets and variables → Actions → New repository secret**, add:

| Secret | Value |
|---|---|
| `BACKUP_DATABASE_URL` | the `postgresql://…` form from step 3 |
| `STORAGE_BUCKET`, `STORAGE_ENDPOINT`, `STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY` | same as in Render |

Then **Actions → Weekly database backup → Run workflow** once to test. A file like
`backups/isha-sangha-2026-11-01.dump` should appear in the R2 bucket. After that it runs every Monday 03:00 IST.

GitHub pauses scheduled jobs in public repositories after 60 days without commits; keeping the repos private avoids that.

**Restoring** a backup (download the `.dump` file from R2 first):

```bash
pg_restore --clean --if-exists --no-owner --dbname "postgresql://user:password@host/dbname?sslmode=require" isha-sangha-2026-11-01.dump
```

## 11. Custom domain (optional, later)

Buy the domain (Cloudflare Registrar sells at cost), then:
- Pages project → **Custom domains** → add `<domain>` and `www.<domain>`.
- Render service → **Settings → Custom Domains** → add `api.<domain>` and create the CNAME it shows in Cloudflare DNS.
- Update `VITE_API_URL` (Pages), `FRONTEND_ORIGIN` (Render) and Google's authorised origins to the new addresses.

## Growing beyond free

When traffic grows: move the API to Render's paid Starter plan (no sleep, more memory) and drop UptimeRobot,
and move Neon to its paid plan for more storage and longer restore history. No code changes.

## Before going public

The site uses the Isha name and Isha photographs. Send the permission letter to Isha Foundation and wait for
their answer before announcing the site publicly.
