CREATE FUNCTION on_inserted_tenant() RETURNS trigger AS $$
    BEGIN
        INSERT INTO House_History(
            person_id,
            house_id,
            "date",
            "type")
            VALUES (
                NEW.id,
                NEW.house_of_residence_id,
                NOW(),
                'TENANT'
            );
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER house_history_trigger_on_inserted_tenant
    AFTER INSERT
    ON Person
    FOR EACH ROW EXECUTE FUNCTION on_inserted_tenant();
-- !!!!!!!!!!!!!!!!!!!!!


-- !!!!!!!!!!!!!!!!!!!!!
CREATE FUNCTION on_updated_house_of_residence() RETURNS trigger AS $$
    BEGIN
        IF (OLD.house_of_residence_id != NEW.house_of_residence_id) THEN
            INSERT INTO House_History(
                person_id,
                house_id,
                "date",
                "type")
                VALUES (
                    NEW.id,
                    NEW.house_of_residence_id,
                    NOW(),
                    'TENANT'
                );
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER house_history_trigger_on_updated_house_of_residence
    AFTER UPDATE
    ON Person
    FOR EACH ROW EXECUTE FUNCTION on_updated_house_of_residence();
-- !!!!!!!!!!!!!!!!!!!!!


-- !!!!!!!!!!!!!!!!!!!!!
CREATE FUNCTION on_inserted_house_owner() RETURNS trigger AS $$
    BEGIN
        INSERT INTO House_History(
            person_id,
            house_id,
            "date",
            "type")
            VALUES (
                NEW.person_id,
                NEW.house_id,
                NOW(),
                'OWNER'
            );
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER house_history_trigger_on_inserted_house_owner
    AFTER INSERT
    ON owner_to_owned_house
    FOR EACH ROW EXECUTE FUNCTION on_inserted_house_owner();
-- !!!!!!!!!!!!!!!!!!!!!
