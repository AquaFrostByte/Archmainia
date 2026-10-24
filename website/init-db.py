import sqlite3

DB_NAME = "leaderboard.db"

conn = sqlite3.connect(DB_NAME)

cursor = conn.cursor()

cursor.execute("""
CREATE TABLE IF NOT EXISTS scores (

    id INTEGER PRIMARY KEY AUTOINCREMENT,

    map_hash TEXT NOT NULL,

    map_name TEXT NOT NULL,

    username TEXT NOT NULL,

    score INTEGER NOT NULL,

    grade TEXT NOT NULL,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
""")

conn.commit()

conn.close()

print(f"Database '{DB_NAME}' initialized successfully.")
