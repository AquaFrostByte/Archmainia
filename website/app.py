from flask import Flask, jsonify, request, send_from_directory, render_template
import sqlite3
import json
import os

app = Flask(__name__, static_folder='frontend', template_folder='frontend')

ADMIN_PASSWORD = "" #Use ur Admin Password here

MAPS_FILE = "maps.json"

if not os.path.exists(MAPS_FILE):
    with open(MAPS_FILE, "w") as f:
        json.dump({}, f)

def load_maps():
    with open(MAPS_FILE, "r") as f:
        return json.load(f)

def save_maps(data):
    with open(MAPS_FILE, "w") as f:
        json.dump(data, f, indent=4)

APPROVED_MAPS = load_maps()

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/leaderboard')
def leaderboard():
    return send_from_directory('frontend', 'index_leaderboard.html')

@app.route('/<path:path>')
def static_files(path):
    return send_from_directory('frontend', path)

@app.route('/api/add-map', methods=['POST'])
def add_map():

    data = request.json

    password = data.get("password")
    map_hash = data.get("hash")
    map_name = data.get("name")

    if password != ADMIN_PASSWORD:
        return jsonify({"error": "Wrong password"}), 403

    maps = load_maps()

    maps[map_hash] = map_name

    save_maps(maps)

    global APPROVED_MAPS
    APPROVED_MAPS = maps

    return jsonify({
        "success": True
    })

@app.route('/api/submit', methods=['POST'])
def submit():

    data = request.json

    h = data.get('mapHash')
    grade = str(data.get('grade', '')).upper()

    if h not in APPROVED_MAPS:
        return jsonify({"error": "Invalid Map"}), 403

    # BLOCK FAILED SCORES
    if grade == 'F':
        return jsonify({
            "success": False,
            "message": "Failed scores are ignored"
        })

    with sqlite3.connect('leaderboard.db') as conn:

        cursor = conn.cursor()

        cursor.execute(
            """
            INSERT INTO scores
            (map_hash, map_name, username, score, grade)
            VALUES (?, ?, ?, ?, ?)
            """,
            (
                h,
                APPROVED_MAPS[h],
                data['username'],
                data['score'],
                grade
            )
        )

        conn.commit()

    return jsonify({"success": True})

@app.route('/api/leaderboard', methods=['GET'])
def get_leaderboard():

    conn = sqlite3.connect('leaderboard.db')

    cursor = conn.execute(
        """
        SELECT
            map_name,
            username,
            MAX(score) as score,
            grade

        FROM scores

        WHERE grade NOT IN ('F', 'f')

        GROUP BY username, map_name

        ORDER BY score DESC

        LIMIT 100
        """
    )

    results = [
        dict(zip(
            ['map_name', 'username', 'score', 'grade'],
            row
        ))
        for row in cursor.fetchall()
    ]

    conn.close()

    return jsonify(results)
if __name__ == '__main__':
    app.run(host='0.0.0.0', port=3000)
