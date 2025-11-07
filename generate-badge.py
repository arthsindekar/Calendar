import xml.etree.ElementTree as ET
import json
import glob
import sys
import os

# Find all timestamped PIT XMLs
xml_files = glob.glob("target/pit-reports/*/mutations.xml")

if not xml_files:
    print("Error: No PIT XML report found")
    sys.exit(1)

# pick the latest folder
xml_file = max(xml_files, key=os.path.getctime)

tree = ET.parse(xml_file)
root = tree.getroot()

killed = int(root.attrib.get("killed", 0))
total = int(root.attrib.get("total", 1))
score = round(killed / total * 100)

badge = {
    "schemaVersion": 1,
    "label": "mutation score",
    "message": f"{score}%",
    "color": "brightgreen" if score >= 90 else "yellow" if score >= 70 else "red"
}

os.makedirs("badges", exist_ok=True)
with open("badges/mutation-badge.json", "w") as f:
    json.dump(badge, f)
