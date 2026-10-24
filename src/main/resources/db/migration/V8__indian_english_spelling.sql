-- House style: Indian English spelling in text people read (centre, programme).
-- Whole words only (\m...\M); slugs and codes are left unchanged so links keep working.

CREATE OR REPLACE FUNCTION pg_temp.indian_spelling(t TEXT) RETURNS TEXT AS $$
    SELECT regexp_replace(regexp_replace(regexp_replace(regexp_replace(
           regexp_replace(regexp_replace(regexp_replace(regexp_replace(t,
           '\mCenters\M', 'Centres', 'g'), '\mcenters\M', 'centres', 'g'),
           '\mCenter\M', 'Centre', 'g'), '\mcenter\M', 'centre', 'g'),
           '\mPrograms\M', 'Programmes', 'g'), '\mprograms\M', 'programmes', 'g'),
           '\mProgram\M', 'Programme', 'g'), '\mprogram\M', 'programme', 'g')
$$ LANGUAGE SQL IMMUTABLE;

UPDATE center SET name = pg_temp.indian_spelling(name) WHERE name ~* '\m(center|program)s?\M';
UPDATE center SET description = pg_temp.indian_spelling(description) WHERE description ~* '\m(center|program)s?\M';
UPDATE city SET description = pg_temp.indian_spelling(description) WHERE description ~* '\m(center|program)s?\M';
UPDATE teacher SET bio = pg_temp.indian_spelling(bio) WHERE bio ~* '\m(center|program)s?\M';
UPDATE program SET description = pg_temp.indian_spelling(description) WHERE description ~* '\m(center|program)s?\M';
