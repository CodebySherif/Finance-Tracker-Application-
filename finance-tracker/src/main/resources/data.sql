INSERT INTO categories (name, type, color) VALUES ('Salary', 'INCOME', '#2F9E44') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Freelance', 'INCOME', '#2F9E44') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Other Income', 'INCOME', '#2F9E44') ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, type, color) VALUES ('Groceries', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Rent', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Utilities', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Transportation', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Dining Out', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Entertainment', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Healthcare', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
INSERT INTO categories (name, type, color) VALUES ('Other Expense', 'EXPENSE', '#E8590C') ON CONFLICT (name) DO NOTHING;
