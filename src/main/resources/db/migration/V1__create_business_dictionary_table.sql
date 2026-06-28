CREATE TABLE business_dictionary (
    id              UUID                     NOT NULL DEFAULT gen_random_uuid(),
    term            VARCHAR(100)             NOT NULL,
    normalized_term VARCHAR(100)             NOT NULL,
    definition      VARCHAR(1000)            NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT pk_business_dictionary PRIMARY KEY (id)
);

CREATE UNIQUE INDEX ux_business_dictionary_normalized_term
    ON business_dictionary (normalized_term);
