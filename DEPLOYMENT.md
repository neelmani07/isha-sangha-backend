# Deploying Isha Sangha

Two pieces go online:

| Piece | What it is | Where it goes |
|---|---|---|
| **Frontend** (`isha-sangha-frontend`) | Static files built by Vite | A static host with a CDN (Cloudflare Pages recommended) |
| **Backend** (this repo) | Spring Boot API in a Docker container | A container host near India (e.g. DigitalOcean App Platform, Bangalore) |

Plus three services the backend uses: **PostgreSQL** (managed), **file storage** (an S3-compatible bucket) and **email** (SMTP for sign-in codes).

## Recommended setup

| Need | Recommendation | Why | Rough cost |
|---|---|---|---|
| Domain + DNS | Cloudflare | Free DNS, SSL, CDN in front of everything | domain only |
| Frontend | Cloudflare Pages | Free, builds from GitHub on every push to `main` | free |
| Backend | DigitalOcean App Platform, region **BLR1** (Bangalore), from this `Dockerfile` | Close to Indian users, deploys from GitHub, HTTPS included | ~$12/month |
| Database | DigitalOcean Managed PostgreSQL, **BLR1** | Daily backups + point-in-time restore, no admin work | ~$15/month |
| Uploads | Cloudflare R2 (private bucket) | 10 GB free, no download fees | free to start |
| Email | Amazon SES (Mumbai) or Brevo | Reliable delivery for sign-in codes | free / a few $ |

Roughly **₹2,500–3,000 a month** to start, comfortably enough for tens of thousands of members.
Render, Railway or a single VPS work too; nothing here is tied to one provider.

Suggested addresses: site at `https://<domain>`, API at `https://api.<domain>`.

## 1. Database

1. Create a managed PostgreSQL 16 database in the same region as the backend.
2. Note the host, port, database name, user and password.
3. Turn on daily backups (on by default with managed databases).

Tables are created automatically on first start (Flyway migrations).

## 2. File storage (Cloudflare R2)

1. Create a bucket, e.g. `isha-sangha-uploads`. Keep it **private** (no public access).
2. Create an API token with *Object Read & Write* on that bucket. Note the access key, secret key and the account endpoint
   (`https://<account-id>.r2.cloudflarestorage.com`).

Photos and proof documents both go here (`images/` and `proofs/`). Proofs are only ever served to admins;
photos are served through the API with a 30-day cache.

To use AWS S3 instead: leave `STORAGE_ENDPOINT` blank and set `STORAGE_REGION` (e.g. `ap-south-1`).
For DigitalOcean Spaces: endpoint `https://blr1.digitaloceanspaces.com`.

## 3. Email

Set up SMTP with Amazon SES or Brevo, verify the sending domain (SPF + DKIM records in Cloudflare DNS),
and note the SMTP host, port, user and password. Without email, members cannot sign in with a code.

## 4. Google sign-in

In Google Cloud Console → APIs & Services → Credentials, create an *OAuth client ID* (Web application) and add
`https://<domain>` under *Authorised JavaScript origins*. Put the client ID in `GOOGLE_CLIENT_ID`.

## 5. Backend

Create an app from the GitHub repo, using the `Dockerfile`, HTTP port `8080`, health check path `/api/v1/health`.
Set these environment variables (mark the secret ones as *secret*):

| Variable | Example | Notes |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` | Already set in the Dockerfile |
| `DB_URL` | `jdbc:postgresql://<host>:25060/<db>?sslmode=require` | |
| `DB_USERNAME`, `DB_PASSWORD` | | secret |
| `JWT_SECRET` | output of `openssl rand -base64 48` | secret, 32+ characters |
| `FRONTEND_ORIGIN` | `https://<domain>,https://www.<domain>` | comma-separated |
| `ADMIN_DEFAULT_EMAIL` | your admin email | |
| `ADMIN_DEFAULT_PASSWORD` | a strong password, 12+ characters | secret; only used to create the first admin. Remove it after the first start |
| `GOOGLE_CLIENT_ID` | `…apps.googleusercontent.com` | |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | | secret |
| `OTP_FROM_EMAIL` | `no-reply@<domain>` | must be a verified sender |
| `STORAGE_BUCKET` | `isha-sangha-uploads` | |
| `STORAGE_ENDPOINT` | `https://<account-id>.r2.cloudflarestorage.com` | blank for AWS S3 |
| `STORAGE_REGION` | `auto` | `ap-south-1` for AWS Mumbai |
| `STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY` | | secret |
| `SITE_NAME`, `SITE_CONTACT_EMAIL` | | |
| `ORG_LEGAL_NAME`, `ORG_REGISTRATION_NUMBER`, `ORG_TAX_EXEMPTION`, `ORG_ADDRESS` | | once the NGO is registered |

**Safety check:** in production the backend refuses to start if the JWT secret is weak, the sample admin password is
used, sample data is switched on, `FRONTEND_ORIGIN` is localhost, or email is not configured. The log says exactly what to fix.

Point `api.<domain>` at the app (custom domain in the host's settings, CNAME in Cloudflare).

## 6. Frontend (Cloudflare Pages)

1. Connect the `isha-sangha-frontend` repo. Build command `npm run build`, output folder `dist`.
2. Environment variable: `VITE_API_URL=https://api.<domain>/api/v1`.
3. Add the custom domain `<domain>` (and `www`).

`public/_redirects` makes page links like `/stories/…` work on refresh; `public/_headers` sets caching and basic security headers.

## 7. After the first deploy

- [ ] Open `https://api.<domain>/api/v1/health`: should say UP.
- [ ] Sign in at `/admin/login` with the admin account, then **remove `ADMIN_DEFAULT_PASSWORD`** from the backend settings.
- [ ] Sign in as a member with an email code and with Google.
- [ ] Submit a centre with a proof file, approve it from `/admin/review`, check it appears on the map.
- [ ] Upload an initiative photo and check it shows.
- [ ] Add real centres and teachers (the database starts empty apart from the place list in the migrations).

## Backups and updates

- The managed database keeps daily backups (7 days) with point-in-time restore. Before big changes, take a manual snapshot.
- Uploads live in the bucket; turn on R2 bucket versioning or a periodic copy if you want a second copy.
- Every push to `main` redeploys automatically. Database changes ship as new Flyway migrations and run on start.

## Before going public

The site uses the Isha name and Isha photographs. Send the permission letter to Isha Foundation and wait for
their answer before announcing the site publicly.
