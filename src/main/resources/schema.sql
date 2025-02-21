CREATE TABLE IF NOT EXISTS recipes (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    ingredients TEXT NOT NULL,
    instructions TEXT NOT NULL,
    servings INTEGER,
    prep_time_minutes INTEGER,
    cook_time_minutes INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS ingredients (
    id SERIAL PRIMARY KEY,
    fdc_id INTEGER UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    category_id INTEGER,
    base_unit VARCHAR(50) DEFAULT 'g',
    calories_per_100g DECIMAL,
    protein_per_100g DECIMAL,
    fat_per_100g DECIMAL,
    carbs_per_100g DECIMAL,
    fiber_per_100g DECIMAL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);