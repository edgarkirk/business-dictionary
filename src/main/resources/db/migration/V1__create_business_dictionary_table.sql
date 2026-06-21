CREATE TABLE business_dictionary (
    id UUID PRIMARY KEY,
    term VARCHAR(100) NOT NULL,
    normalized_term VARCHAR(100) NOT NULL,
    definition VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ux_business_dictionary_normalized_term UNIQUE (normalized_term)
);
