import xml.etree.ElementTree as ET
import json
import glob
import os
import sys
import re

# Try XML first
xml_files = glob.glob("target/pit-reports/*/mutations.xml") + glob.glob("target/pit-reports/mutations.xml")

score = None

if xml_files:
    xml_file = max(xml_files, key=os.path.getctime)
    tree = ET.parse(xml_file)
    root = tree.getroot()
    killed = int(root.attrib.get("killed", 0))
    total = int(root.attrib.get("total", 1))
    score = round(killed / total * 100)
else:
    # Fallback: parse index.html
    html_files = glob.glob("target/pit-reports/*/index.html") + glob.glob("target/pit-reports/index.html")
    if not html_files:
        print("Error: No PIT report found (XML or HTML)")
        sys.exit(1)
    html_file = max(html_files, key=os.path.getctime)
    with open(html_file, "r", encoding="utf-8") as f:
        content = f.read()
        match = re.search(r'Mutation Score.*?>(\d+)%<', content)
        if match:
            score = int(match.group(1))
        else:
            print("Error: Could not parse mutation score from index.html")
            sys.exit(1)

# Generate badge JSON
badge = {
    "schemaVersion": 1,
    "label": "mutation score",
    "message": f"{score}%",
    "color": "brightgreen" if score >= 90 else "yellow" if score >= 70 else "red"
}

os.makedirs("badges", exist_ok=True)
with open("badges/mutation-badge.json", "w") as f:
    json.dump(badge, f)

print(f"Mutation score badge generated: {score}%")
